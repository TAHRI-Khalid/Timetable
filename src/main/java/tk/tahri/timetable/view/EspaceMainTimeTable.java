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

    public EspaceMainTimeTable(Espace espace, TitleToolBar titleToolBar, GroupeSubTimetableView groupeTimetable, FormateurSubTimetableView formateurTimetable) {
        super(SeanceType.ESPACE);
        this.groupeTimetable = groupeTimetable;
        this.formateurTimetable = formateurTimetable;
        this.espace = espace;
        this.setTitleToolBar(titleToolBar);
        loadSeanceViews(true);
        this.getTitleToolBar().setTitle("Espace : " + espace.getNom());
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

        var formateur = formateurTimetable.getFormateur();
        if (formateur == null) {
            var formateurs = Database.getInstance().getFormateurs();
            if (!formateurs.isEmpty()) {
                formateur = formateurs.get(0);
                formateurTimetable.setFormateur(formateur);
            }
        }

        return new Seance(debut, fin, DayOfWeek.of(1), "FP", groupe, formateur, espace);
    }

    @Override
    protected void loadData() {
        seances.addAll(Database.getInstance().getSeances(espace));
    }

    @Override
    public void refreshView(Seance seance) {
        this.getTitleToolBar().setMasseHoraire(this.getDuration());
        if (seance.getGroupes() != null && !seance.getGroupes().isEmpty()) {
            groupeTimetable.setGroupe(seance.getGroupes().get(0));
            groupeTimetable.loadSeanceViews(false);
        }
        if (seance.getFormateur() != null) {
            formateurTimetable.setFormateur(seance.getFormateur());
            formateurTimetable.loadSeanceViews(false);
        }
    }

    public Espace getEspace() {
        return espace;
    }

    public void setEspace(Espace espace) {
        this.espace = espace;
    }
}
