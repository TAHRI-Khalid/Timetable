package tk.tahri.timetable.view;


import tk.tahri.timetable.Entities.Groupe;
import tk.tahri.timetable.dao.Database;

public class GroupePrintTimeTableView extends PrintTimeTableView {
    private final Groupe groupe;

    public GroupePrintTimeTableView(Groupe groupe) {
        this.groupe = groupe;
        this.getStylesheets().add(getClass().getResource("/css/print.css").toString());
    }

    @Override
    protected void loadData() {
        if (groupe != null) {
            seances.addAll(Database.getInstance().getSeances(groupe));
        }
    }
}
