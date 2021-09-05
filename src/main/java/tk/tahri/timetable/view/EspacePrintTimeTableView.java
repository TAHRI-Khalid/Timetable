package tk.tahri.timetable.view;


import tk.tahri.timetable.Entities.Espace;
import tk.tahri.timetable.Entities.SeanceType;
import tk.tahri.timetable.dao.Database;

public class EspacePrintTimeTableView extends PrintTimeTableView {
    private final Espace espace;

    public EspacePrintTimeTableView(Espace espace) {
        super(SeanceType.ESPACE);
        this.espace = espace;
        this.getStylesheets().add(getClass().getResource("/css/print.css").toString());
    }

    @Override
    protected void loadData() {
        if (espace != null) {
            seances.addAll(Database.getInstance().getSeances(espace));
        }
    }
}
