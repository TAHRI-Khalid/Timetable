package tk.tahri.timetable.view;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import tk.tahri.timetable.Entities.Espace;
import tk.tahri.timetable.Entities.SeanceType;
import tk.tahri.timetable.dao.Database;

public class EspaceSubTimetableView extends SubTimeTableView {
    private final ObjectProperty<Espace> espaceProperty = new SimpleObjectProperty<>();

    public EspaceSubTimetableView() {
        super(SeanceType.ESPACE);
    }

    @Override
    public void loadData() {
        var espace = getEspace();
        if (espace != null) {
            seances.addAll(Database.getInstance().getSeances(espace));
            if (this.getTitleToolBar() != null) {
                this.getTitleToolBar().setMasseHoraire(this.getDuration());
            }
        }
    }

    public Espace getEspace() {
        return espaceProperty.get();
    }

    public void setEspace(Espace espace) {
        this.espaceProperty.set(espace);
        if (super.getTitleToolBar() != null) {
            super.getTitleToolBar().setTitle(espace != null ? espace.toString() : "");
        }
    }

    public ObjectProperty<Espace> espaceProperty() {
        return espaceProperty;
    }
}
