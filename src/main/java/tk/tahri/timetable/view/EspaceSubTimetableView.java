package tk.tahri.timetable.view;

import tk.tahri.timetable.Entities.Espace;
import tk.tahri.timetable.Entities.SeanceType;
import tk.tahri.timetable.dao.Database;

public class EspaceSubTimetableView extends SubTimeTableView {
    private Espace espace;
    public EspaceSubTimetableView() {
        super(SeanceType.ESPACE);
    }

    @Override
    public void loadData() {
        if (espace != null) {
            seances.addAll(Database.getInstance().getSeances(espace));
            this.getTitleToolBar().setMasseHoraire(this.getDuration());
        }
    }

    public Espace getEspace() {
        return espace;
    }

    public void setEspace(Espace espace) {
        this.espace = espace;
        super.getTitleToolBar().setTitle(espace.toString());
    }
}
