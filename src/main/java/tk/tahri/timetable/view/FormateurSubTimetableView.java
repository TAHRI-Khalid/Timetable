package tk.tahri.timetable.view;

import tk.tahri.timetable.Entities.Formateur;
import tk.tahri.timetable.Entities.SeanceType;
import tk.tahri.timetable.dao.Database;

public class FormateurSubTimetableView extends SubTimeTableView {
    private Formateur formateur;
    public FormateurSubTimetableView( ) {
       super(SeanceType.FORMATEUR);
    }

    @Override
    public void loadData() {
        if(formateur!=null ){
            seances.addAll(Database.getInstance().getSeances(formateur));
        }
    }

    public Formateur getFormateur() {
        return formateur;
    }

    public void setFormateur(Formateur formateur) {
        this.formateur = formateur;
        super.Titre.setText("Fromateur : "+formateur.toString());
    }
}
