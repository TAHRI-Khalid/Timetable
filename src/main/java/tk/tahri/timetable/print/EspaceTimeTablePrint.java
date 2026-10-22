package tk.tahri.timetable.print;

import javafx.scene.layout.*;
import javafx.scene.text.Text;
import tk.tahri.timetable.Entities.Espace;
import tk.tahri.timetable.view.EspacePrintTimeTableView;


public class EspaceTimeTablePrint extends TimeTablePrint {
    private final Espace espace;

    public EspaceTimeTablePrint(Espace espace) {
        super(10, new EspacePrintTimeTableView(espace));
        this.espace = espace;
        this.getChildren().addAll(
                header(),
                titre(espace != null ? espace.getNom() : ""),
                info(false, 30, info1()),
                body(),
                footer()
        );
    }

    private GridPane info1() {
        var grid = new GridPane();
        grid.setHgap(5);
        grid.setVgap(3);

        grid.add(new Text("Type"), 0, 0);
        grid.add(new Text("Nom"), 0, 1);

        String type = (espace != null && espace.getType() != null) ? espace.getType() : "-";
        String nom = (espace != null && espace.getNom() != null) ? espace.getNom() : "-";

        grid.add(new Text(" : " + type), 1, 0);
        grid.add(new Text(" : " + nom), 1, 1);

        return grid;
    }
}
