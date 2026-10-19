package tk.tahri.timetable.tools.command;

import tk.tahri.timetable.Entities.Seance;
import tk.tahri.timetable.view.MainTimeTableView;

import java.time.LocalTime;

public class ResizeSeanceCommand implements Command {
    private final MainTimeTableView view;
    private final Seance seance;
    private final LocalTime oldFin;
    private final LocalTime newFin;

    public ResizeSeanceCommand(MainTimeTableView view, Seance seance, LocalTime oldFin, LocalTime newFin) {
        this.view = view;
        this.seance = seance;
        this.oldFin = oldFin;
        this.newFin = newFin;
    }

    @Override
    public void execute() {
        seance.setFin(newFin);
        view.updateSeance(seance);
    }

    @Override
    public void undo() {
        seance.setFin(oldFin);
        view.updateSeance(seance);
    }

    @Override
    public void redo() {
        execute();
    }

    @Override
    public String getDescription() {
        return "Redimensionnement (" + seance.getDebut() + " - " + newFin + ")";
    }
}
