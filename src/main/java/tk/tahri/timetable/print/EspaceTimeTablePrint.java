package tk.tahri.timetable.print;

import javafx.scene.layout.*;
import javafx.scene.text.Text;
import tk.tahri.timetable.Entities.Espace;
import tk.tahri.timetable.view.EspacePrintTimeTableView;


public class EspaceTimeTablePrint extends TimeTablePrint {
    private final Espace espace;

    public EspaceTimeTablePrint(Espace espace) {
        super(15, new EspacePrintTimeTableView(espace));
        this.espace = espace;
        this.getChildren().addAll(header(), titre(espace.getNom()), info(info1()), body());
    }


    private GridPane info1() {
        var grid = new GridPane();

        grid.add(new Text("Type"), 0, 0);
        grid.add(new Text("Nom"), 0, 1);

        grid.add(new Text(" : " + espace.getType()), 1, 0);
        grid.add(new Text(" : " + espace.getNom()), 1, 1);

        return grid;
    }

}
