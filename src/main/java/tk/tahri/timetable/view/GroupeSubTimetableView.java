package tk.tahri.timetable.view;

import tk.tahri.timetable.Entities.Groupe;
import tk.tahri.timetable.Entities.SeanceType;
import tk.tahri.timetable.dao.Database;

public class GroupeSubTimetableView extends SubTimeTableView {
    private Groupe groupe;

    public GroupeSubTimetableView() {
        super(SeanceType.GROUPE);
    }

    @Override
    public void loadData() {
        if(groupe!=null){
            seances.addAll(Database.getInstance().getSeances(groupe));
        }
    }

    public Groupe getGroupe() {
        return groupe;
    }

    public void setGroupe(Groupe groupe) {
        this.groupe = groupe;
    }
}
