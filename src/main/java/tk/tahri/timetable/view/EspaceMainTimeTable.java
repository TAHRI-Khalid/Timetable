package tk.tahri.timetable.view;

import tk.tahri.timetable.Entities.Espace;
import tk.tahri.timetable.Entities.Seance;
import tk.tahri.timetable.Entities.SeanceType;
import tk.tahri.timetable.dao.Database;

import java.time.DayOfWeek;
import java.time.LocalTime;

public class EspaceMainTimeTable extends MainTimeTableView {
    private Espace espace;

    private final GroupeSubTimetableView groupeTimetable;
    private final FormateurSubTimetableView formateurTimetable;

    public EspaceMainTimeTable(Espace espace, GroupeSubTimetableView groupeTimetable, FormateurSubTimetableView formateurTimetable) {
        super(SeanceType.ESPACE);
        this.groupeTimetable = groupeTimetable;
        this.formateurTimetable = formateurTimetable;
        this.espace = espace;
        loadSeanceViews(true);
    }

    @Override
    protected Seance getDraggedSeance() {
        return new Seance(LocalTime.of(8, 30), LocalTime.of(11, 0), DayOfWeek.of(1),"FP", groupeTimetable.getGroupe(), formateurTimetable.getFormateur(), espace);
    }

    @Override
    protected void loadData() {
        seances.addAll(Database.getInstance().getSeances(espace));
    }

    @Override
    protected void refreshView(Seance seance) {
        groupeTimetable.setGroupe(seance.getGroupes().get(0));
        groupeTimetable.loadSeanceViews(false);
        formateurTimetable.setFormateur(seance.getFormateur());
        formateurTimetable.loadSeanceViews(false);
    }

    public Espace getEspace() {
        return espace;
    }

    public void setEspace(Espace espace) {
        this.espace = espace;
    }

}
