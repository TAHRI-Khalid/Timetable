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

    public FormateurMainTimeTable(Formateur formateur,TitleToolBar titleToolBar, GroupeSubTimetableView groupeTimetable, EspaceSubTimetableView espaceTimetable) {
        super(SeanceType.FORMATEUR);
        this.groupeTimetable = groupeTimetable;
        this.espaceTimetable = espaceTimetable;
        this.formateur = formateur;
        this.setTitleToolBar(titleToolBar);
        loadSeanceViews(true);
        this.getTitleToolBar().setTitle("Formateur : " + formateur);
        this.getTitleToolBar().setMasseHoraire(this.getDuration());
    }

    @Override
    protected Seance getDraggedSeance() {
        return new Seance(LocalTime.of(8, 30), LocalTime.of(11, 0), DayOfWeek.of(1),"FP", groupeTimetable.getGroupe(), formateur, espaceTimetable.getEspace());
    }

    @Override
    protected void loadData() {
        seances.addAll(Database.getInstance().getSeances(formateur));
    }

    @Override
    protected void refreshView(Seance seance) {
        this.getTitleToolBar().setMasseHoraire(this.getDuration());
        groupeTimetable.setGroupe(seance.getGroupes().get(0));
        groupeTimetable.loadSeanceViews(false);
        espaceTimetable.setEspace(seance.getEspace());
        espaceTimetable.loadSeanceViews(false);
    }

    public Formateur getFormateur() {
        return formateur;
    }

    public void setFormateur(Formateur formateur) {
        this.formateur = formateur;
    }


}
