package tk.tahri.timetable.tools.command;

import tk.tahri.timetable.Entities.Espace;
import tk.tahri.timetable.Entities.Formateur;
import tk.tahri.timetable.Entities.Groupe;
import tk.tahri.timetable.Entities.Seance;
import tk.tahri.timetable.dao.Database;
import tk.tahri.timetable.view.MainTimeTableView;

import java.util.ArrayList;
import java.util.List;

public class UpdateSeanceDetailsCommand implements Command {
    private final MainTimeTableView view;
    private final Seance seance;
    private final Formateur oldFormateur, newFormateur;
    private final Espace oldEspace, newEspace;
    private final String oldType, newType;
    private final List<Groupe> oldGroupes, newGroupes;

    public UpdateSeanceDetailsCommand(MainTimeTableView view, Seance seance,
                                      Formateur oldFormateur, Formateur newFormateur,
                                      Espace oldEspace, Espace newEspace,
                                      String oldType, String newType,
                                      List<Groupe> oldGroupes, List<Groupe> newGroupes) {
        this.view = view;
        this.seance = seance;
        this.oldFormateur = oldFormateur;
        this.newFormateur = newFormateur;
        this.oldEspace = oldEspace;
        this.newEspace = newEspace;
        this.oldType = oldType;
        this.newType = newType;
        this.oldGroupes = oldGroupes != null ? new ArrayList<>(oldGroupes) : new ArrayList<>();
        this.newGroupes = newGroupes != null ? new ArrayList<>(newGroupes) : new ArrayList<>();
    }

    @Override
    public void execute() {
        applyState(newFormateur, newEspace, newType, newGroupes);
    }

    @Override
    public void undo() {
        applyState(oldFormateur, oldEspace, oldType, oldGroupes);
    }

    private void applyState(Formateur formateur, Espace espace, String type, List<Groupe> groupes) {
        seance.setFormateur(formateur);
        seance.setEspace(espace);
        seance.setType(type);
        if (!groupes.isEmpty()) {
            Database.getInstance().execute("DELETE FROM seance_groupe WHERE idSeance=?", String.valueOf(seance.getId()));
            for (Groupe g : groupes) {
                Database.getInstance().execute("INSERT INTO seance_groupe VALUES(?,?)",
                        String.valueOf(g.getId()),
                        String.valueOf(seance.getId()));
            }
            seance.getGroupes().clear();
            seance.getGroupes().addAll(groupes);
        }
        view.updateSeance(seance);
    }

    @Override
    public String getDescription() {
        return "Modification des détails de séance";
    }
}
