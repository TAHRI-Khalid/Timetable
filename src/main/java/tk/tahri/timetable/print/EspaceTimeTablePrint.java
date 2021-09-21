package tk.tahri.timetable.print;

import javafx.geometry.Pos;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import tk.tahri.timetable.Entities.Espace;
import tk.tahri.timetable.view.EspacePrintTimeTableView;

import java.time.Duration;


public class EspaceTimeTablePrint extends TimeTablePrint {
    private final Espace espace;

    public EspaceTimeTablePrint(Espace espace) {
        super(15);
        this.espace = espace;
        this.setAlignment(Pos.TOP_CENTER);
        var TT = new EspacePrintTimeTableView(espace);

        TT.loadSeanceViews(false);
        TT.setTranslateX(30);
        this.getChildren().addAll(header(), titre(espace.getNom()), info(info1(TT.getDuration())), TT);
    }


    private GridPane info1(Duration duration) {
        var grid = new GridPane();

        grid.add(new Text("Type"), 0, 0);
        grid.add(new Text("nom"), 0, 1);
        grid.add(new Text("Masse horaire"), 0, 2);

        grid.add(new Text(" : " + espace.getType()), 1, 0);
        grid.add(new Text(" : " + espace.getNom()), 1, 1);
        grid.add(new Text(" : " + duration.toHours() +"h"+duration.toMinutesPart()+"min"), 1, 2);

        return grid;
    }

}
