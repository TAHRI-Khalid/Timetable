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

    public FormateurMainTimeTable(Formateur formateur, TitleToolBar titleToolBar, GroupeSubTimetableView groupeTimetable, EspaceSubTimetableView espaceTimetable) {
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
        var creneaux = Database.getInstance().getCreneaux();
        LocalTime debut = (creneaux != null && !creneaux.isEmpty()) ? creneaux.get(0).getHeureDebut() : LocalTime.of(8, 0);
        LocalTime fin = (creneaux != null && !creneaux.isEmpty()) ? creneaux.get(0).getHeureFin() : LocalTime.of(9, 0);

        var groupe = groupeTimetable.getGroupe();
        if (groupe == null) {
            var groupes = Database.getInstance().getGroupes();
            if (!groupes.isEmpty()) {
                groupe = groupes.get(0);
                groupeTimetable.setGroupe(groupe);
            }
        }

        var espace = espaceTimetable.getEspace();
        if (espace == null) {
            var espaces = Database.getInstance().getEspaces();
            if (!espaces.isEmpty()) {
                espace = espaces.get(0);
                espaceTimetable.setEspace(espace);
            }
        }

        return new Seance(debut, fin, DayOfWeek.of(1), "FP", groupe, formateur, espace);
    }

    @Override
    protected void loadData() {
        seances.addAll(Database.getInstance().getSeances(formateur));
    }

    @Override
    public void refreshView(Seance seance) {
        this.getTitleToolBar().setMasseHoraire(this.getDuration());
        if (seance.getGroupes() != null && !seance.getGroupes().isEmpty()) {
            groupeTimetable.setGroupe(seance.getGroupes().get(0));
            groupeTimetable.loadSeanceViews(false);
        }
        if (seance.getEspace() != null) {
            espaceTimetable.setEspace(seance.getEspace());
            espaceTimetable.loadSeanceViews(false);
        }
    }

    public Formateur getFormateur() {
        return formateur;
    }

    public void setFormateur(Formateur formateur) {
        this.formateur = formateur;
    }
}
