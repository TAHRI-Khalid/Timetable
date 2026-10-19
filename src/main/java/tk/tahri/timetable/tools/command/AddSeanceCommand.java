package tk.tahri.timetable.tools.command;

import tk.tahri.timetable.Entities.Groupe;
import tk.tahri.timetable.Entities.Seance;
import tk.tahri.timetable.dao.Database;
import tk.tahri.timetable.view.MainTimeTableView;

import java.util.ArrayList;
import java.util.List;

public class AddSeanceCommand implements Command {
    private final MainTimeTableView view;
    private final Seance seance;
    private final List<Groupe> groupes;
    private long savedId;

    public AddSeanceCommand(MainTimeTableView view, Seance seance) {
        this.view = view;
        this.seance = seance;
        this.savedId = seance.getId();
        this.groupes = new ArrayList<>(seance.getGroupes());
    }

    @Override
    public void execute() {
        int id = Database.getInstance().execute(
                "INSERT INTO seance VALUES(null,?,?,?,?,?,?)",
                String.valueOf(seance.getDebut().toSecondOfDay()),
                String.valueOf(seance.getFin().toSecondOfDay()),
                String.valueOf(seance.getJour().getValue()),
                seance.getType(),
                String.valueOf(seance.getFormateur().getId()),
                String.valueOf(seance.getEspace().getId())
        );
        seance.setId(id);
        savedId = id;
        for (Groupe g : groupes) {
            Database.getInstance().execute("INSERT INTO seance_groupe VALUES(?,?)",
                    String.valueOf(g.getId()),
                    String.valueOf(id));
        }
        seance.getGroupes().clear();
        seance.getGroupes().addAll(groupes);
        view.loadSeanceViews(true);
        view.refreshView(seance);
    }

    @Override
    public void undo() {
        seance.setId(savedId);
        view.deleteSeance(seance);
    }

    @Override
    public void redo() {
        execute();
    }

    @Override
    public String getDescription() {
        String info = (seance.getFormateur() != null ? seance.getFormateur().getNom() : "");
        return "Ajout de séance " + info;
    }
}
