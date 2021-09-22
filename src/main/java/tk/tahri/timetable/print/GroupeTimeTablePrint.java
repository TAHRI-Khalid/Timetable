package tk.tahri.timetable.print;

import javafx.scene.layout.*;
import javafx.scene.text.Text;
import tk.tahri.timetable.Entities.Groupe;
import tk.tahri.timetable.view.GroupePrintTimeTableView;

public class GroupeTimeTablePrint extends TimeTablePrint {
    private final Groupe groupe;

    public GroupeTimeTablePrint(Groupe groupe) {
        super(15, new GroupePrintTimeTableView(groupe));
        this.groupe = groupe;
        this.getChildren().addAll(header(), titre(groupe.getNom()), info(info1(), info2()), body());
    }

    private GridPane info1() {
        var grid = new GridPane();

        grid.add(new Text("Niveau"), 0, 0);
        grid.add(new Text("Secteur"), 0, 1);
        grid.add(new Text("Type de formation"), 0, 2);

        grid.add(new Text(" : " + groupe.getFiliere().getNiveau()), 1, 0);
        grid.add(new Text(" : " + groupe.getFiliere().getSecteur()), 1, 1);
        grid.add(new Text(" : " + groupe.getFiliere().getType()), 1, 2);

        return grid;
    }

    private GridPane info2() {
        var grid = new GridPane();

        grid.add(new Text("Filière"), 0, 0);
        grid.add(new Text("Groupe"), 0, 1);
        grid.add(new Text("Année"), 0, 2);

        grid.add(new Text(" : " + groupe.getFiliere().getAbreviation()), 1, 0);
        grid.add(new Text(" : " + groupe.getNom()), 1, 1);
        grid.add(new Text(" : " + groupe.getAnnee()), 1, 2);
        return grid;
    }

}
