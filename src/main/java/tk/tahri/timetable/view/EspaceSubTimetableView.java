package tk.tahri.timetable.view;

import tk.tahri.timetable.Entities.Espace;
import tk.tahri.timetable.dao.Database;

public class EspaceSubTimetableView extends SubTimeTableView {
    private Espace espace;

    public EspaceSubTimetableView() {
        super();
    }

    @Override
    public void loadData() {
        if(espace!=null ){
            seances.addAll(Database.getInstance().getSeances(espace));
        }
    }

    public Espace getEspace() {
        return espace;
    }

    public void setEspace(Espace espace) {
        this.espace = espace;
    }
}
