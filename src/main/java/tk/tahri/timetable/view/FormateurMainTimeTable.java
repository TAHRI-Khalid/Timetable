package tk.tahri.timetable.view;

import tk.tahri.timetable.Entities.Formateur;
import tk.tahri.timetable.Entities.Seance;
import tk.tahri.timetable.Entities.SeanceType;
import tk.tahri.timetable.dao.Database;
import java.time.DayOfWeek;
import java.time.LocalTime;

public class FormateurMainTimeTable extends MainTimeTableView {
    private Formateur formateur;

    private final GroupeSubTimetableView groupeTimetable;
    private final EspaceSubTimetableView espaceTimetable;

    public FormateurMainTimeTable(Formateur formateur, GroupeSubTimetableView groupeTimetable, EspaceSubTimetableView espaceTimetable) {
        super(SeanceType.FORMATEUR);
        this.groupeTimetable = groupeTimetable;
        this.espaceTimetable = espaceTimetable;
        this.formateur = formateur;
        loadSeanceViews(true);
    }

    @Override
    protected Seance getDraggedSeance() {
        return new Seance(LocalTime.of(8, 30), LocalTime.of(11, 0), DayOfWeek.of(1), groupeTimetable.getGroupe(), formateur, espaceTimetable.getEspace());
    }

    @Override
    protected void loadData() {
        seances.addAll(Database.getInstance().getSeances(formateur));
    }

    @Override
    protected void saveSeance(SeanceView seanceView) {
        var s = seanceView.getSeance();
        Database.getInstance().execute("INSERT INTO seance VALUES(null,%d,%d,%d,%d,%d,%d)"
                .formatted(s.getDebut().toSecondOfDay(), s.getFin().toSecondOfDay(), s.getJour().getValue(),
                        s.getGroupe().getId(), s.getFormateur().getId(), s.getEspace().getId()));
        loadSeanceViews(true);
        refreshView(seanceView);
    }

    @Override
    protected void updateSeance(SeanceView seanceView) {
        var s = seanceView.getSeance();
        Database.getInstance().execute("UPDATE seance set debut=%d, fin=%d, jour=%d WHERE id=%d"
                .formatted(s.getDebut().toSecondOfDay(),
                        s.getFin().toSecondOfDay(),
                        s.getJour().getValue(),
                        s.getId()));
        loadSeanceViews(true);
        refreshView(seanceView);
    }

    @Override
    protected void deleteSeance(SeanceView seanceView) {
        var s = seanceView.getSeance();
        Database.getInstance().execute("DELETE FROM seance WHERE id=%d".formatted(s.getId()));
        loadSeanceViews(true);
        refreshView(seanceView);
    }

    @Override
    protected void refreshView(SeanceView seanceView) {
        groupeTimetable.setGroupe(seanceView.getSeance().getGroupe());
        groupeTimetable.loadSeanceViews(false);
        espaceTimetable.setEspace(seanceView.getSeance().getEspace());
        espaceTimetable.loadSeanceViews(false);
    }

    public Formateur getFormateur() {
        return formateur;
    }

    public void setFormateur(Formateur formateur) {
        this.formateur = formateur;
    }


}
