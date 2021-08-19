package tk.tahri.timetable.view;

import tk.tahri.timetable.Entities.Formateur;
import tk.tahri.timetable.dao.Database;

public class FormateurSubTimetableView extends SubTimeTableView {
    private Formateur formateur;

    public FormateurSubTimetableView( ) {
       super();
    }

    @Override
    public void loadData() {
        seances.addAll(Database.getInstance().getSeances(formateur));
    }

    public Formateur getFormateur() {
        return formateur;
    }

    public void setFormateur(Formateur formateur) {
        this.formateur = formateur;
    }
}
