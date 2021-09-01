package tk.tahri.timetable.view;

import tk.tahri.timetable.Entities.Groupe;
import tk.tahri.timetable.Entities.Seance;
import tk.tahri.timetable.dao.Database;

import java.time.DayOfWeek;
import java.time.LocalTime;

public class GroupeMainTimeTable extends MainTimeTableView {
    private Groupe groupe;

    private final FormateurSubTimetableView formateurTimetable;
    private final EspaceSubTimetableView espaceTimetable;

    public GroupeMainTimeTable(Groupe groupe, FormateurSubTimetableView formateurTimetable, EspaceSubTimetableView espaceTimetable) {
        this.formateurTimetable = formateurTimetable;
        this.espaceTimetable = espaceTimetable;
        this.groupe = groupe;
        loadSeanceViews(true, "groupe-seance-view");
    }

    @Override
    protected Seance getDraggedSeance() {
        return new Seance(LocalTime.of(8, 30), LocalTime.of(11, 0), DayOfWeek.of(1), groupe, formateurTimetable.getFormateur(), espaceTimetable.getEspace());
    }

    @Override
    protected void loadData() {
        seances.addAll(Database.getInstance().getSeances(groupe));
    }

    @Override
    protected void saveSeance(SeanceView seanceView) {
        var s = seanceView.getSeance();
        Database.getInstance().execute("INSERT INTO seance VALUES(null,%d,%d,%d,%d,%d,%d)"
                .formatted(s.getDebut().toSecondOfDay(), s.getFin().toSecondOfDay(), s.getJour().getValue(),
                        s.getGroupe().getId(), s.getFormateur().getId(), s.getEspace().getId()));

        refreshView();
    }

    @Override
    protected void updateSeance(SeanceView seanceView) {
        var s = seanceView.getSeance();
        Database.getInstance().execute("UPDATE seance set debut=%d, fin=%d, jour=%d WHERE id=%d"
                .formatted(s.getDebut().toSecondOfDay(),
                        s.getFin().toSecondOfDay(),
                        s.getJour().getValue(),
                        s.getId()));

        refreshView();
    }

    @Override
    protected void deleteSeance(SeanceView seanceView) {
        var s = seanceView.getSeance();
        Database.getInstance().execute("DELETE FROM seance WHERE id=%d".formatted(s.getId()));
        refreshView();
    }

    @Override
    protected void refreshView() {
        loadSeanceViews(true, "groupe-seance-view");
        formateurTimetable.loadSeanceViews(false, "formateur-seance-view");
        espaceTimetable.loadSeanceViews(false, "espace-seance-view");
    }

    public Groupe getGroupe() {
        return groupe;
    }

    public void setGroupe(Groupe groupe) {
        this.groupe = groupe;
    }


}
