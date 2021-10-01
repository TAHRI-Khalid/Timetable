package tk.tahri.timetable.view;

import tk.tahri.timetable.Entities.Groupe;
import tk.tahri.timetable.Entities.Seance;
import tk.tahri.timetable.Entities.SeanceType;
import tk.tahri.timetable.dao.Database;

import java.time.DayOfWeek;
import java.time.LocalTime;

public class GroupeMainTimeTable extends MainTimeTableView {
    private Groupe groupe;

    private final FormateurSubTimetableView formateurTimetable;
    private final EspaceSubTimetableView espaceTimetable;

    public GroupeMainTimeTable(Groupe groupe, TitleToolBar titleToolBar, FormateurSubTimetableView formateurTimetable, EspaceSubTimetableView espaceTimetable) {
        super(SeanceType.GROUPE);
        this.formateurTimetable = formateurTimetable;
        this.espaceTimetable = espaceTimetable;
        this.groupe = groupe;
        this.setTitleToolBar(titleToolBar);
        loadSeanceViews(true);
        this.getTitleToolBar().setTitle("Groupe : " + groupe.getNom());
        this.getTitleToolBar().setMasseHoraire(this.getDuration());

    }

    @Override
    protected Seance getDraggedSeance() {
        return new Seance(LocalTime.of(8, 30), LocalTime.of(11, 0), DayOfWeek.of(1), "FP", groupe, formateurTimetable.getFormateur(), espaceTimetable.getEspace());
    }

    @Override
    protected void loadData() {
        seances.addAll(Database.getInstance().getSeances(groupe));
    }

    @Override
    protected void refreshView(Seance seance) {
        this.getTitleToolBar().setMasseHoraire(this.getDuration());
        formateurTimetable.setFormateur(seance.getFormateur());
        formateurTimetable.loadSeanceViews(false);
        espaceTimetable.setEspace(seance.getEspace());
        espaceTimetable.loadSeanceViews(false);
    }

    public Groupe getGroupe() {
        return groupe;
    }

    public void setGroupe(Groupe groupe) {
        this.groupe = groupe;
    }
}
