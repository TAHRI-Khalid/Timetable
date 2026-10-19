package tk.tahri.timetable.tools.command;

import tk.tahri.timetable.Entities.Seance;
import tk.tahri.timetable.view.MainTimeTableView;

import java.time.DayOfWeek;
import java.time.LocalTime;

public class MoveSeanceCommand implements Command {
    private final MainTimeTableView view;
    private final Seance seance;
    private final DayOfWeek oldJour;
    private final LocalTime oldDebut;
    private final LocalTime oldFin;
    private final DayOfWeek newJour;
    private final LocalTime newDebut;
    private final LocalTime newFin;

    public MoveSeanceCommand(MainTimeTableView view, Seance seance,
                             DayOfWeek oldJour, LocalTime oldDebut, LocalTime oldFin,
                             DayOfWeek newJour, LocalTime newDebut, LocalTime newFin) {
        this.view = view;
        this.seance = seance;
        this.oldJour = oldJour;
        this.oldDebut = oldDebut;
        this.oldFin = oldFin;
        this.newJour = newJour;
        this.newDebut = newDebut;
        this.newFin = newFin;
    }

    @Override
    public void execute() {
        seance.setJour(newJour);
        seance.setDebut(newDebut);
        seance.setFin(newFin);
        view.updateSeance(seance);
    }

    @Override
    public void undo() {
        seance.setJour(oldJour);
        seance.setDebut(oldDebut);
        seance.setFin(oldFin);
        view.updateSeance(seance);
    }

    @Override
    public void redo() {
        execute();
    }

    @Override
    public String getDescription() {
        return "Déplacement de séance (" + oldJour + " -> " + newJour + ")";
    }
}
