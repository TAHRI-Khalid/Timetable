package tk.tahri.timetable.view;

import tk.tahri.timetable.Entities.Groupe;
import tk.tahri.timetable.dao.Database;

public class GroupeSubTimetableView extends SubTimeTableView {
    private Groupe groupe;

    @Override
    public void loadData() {
        seances.addAll(Database.getInstance().getSeances(groupe));
    }

    public Groupe getGroupe() {
        return groupe;
    }

    public void setGroupe(Groupe groupe) {
        this.groupe = groupe;
    }
}
