package tk.tahri.timetable.view;


import tk.tahri.timetable.Entities.Formateur;
import tk.tahri.timetable.Entities.SeanceType;
import tk.tahri.timetable.dao.Database;

public class FormateurPrintTimeTableView extends PrintTimeTableView {
    private final Formateur formateur;

    public FormateurPrintTimeTableView(Formateur formateur) {
        super(SeanceType.FORMATEUR);
        this.formateur = formateur;
        this.getStylesheets().add(getClass().getResource("/css/print.css").toString());
    }

    @Override
    protected void loadData() {
        if (formateur != null) {
            seances.addAll(Database.getInstance().getSeances(formateur));
        }
    }
}
