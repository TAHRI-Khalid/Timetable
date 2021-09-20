package tk.tahri.timetable.print;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import tk.tahri.timetable.Entities.Formateur;
import tk.tahri.timetable.view.FormateurPrintTimeTableView;

import java.util.Objects;

public class FormateurTimeTablePrint extends TimeTablePrint {
    private final Formateur formateur;

    public FormateurTimeTablePrint(Formateur formateur) {
        super(15);
        this.formateur = formateur;
        this.setAlignment(Pos.TOP_CENTER);
        var groupeTT=new FormateurPrintTimeTableView(formateur);
        groupeTT.loadSeanceViews(false);
        groupeTT.setTranslateX(30);
        this.getChildren().addAll(header(), titre(), info(), groupeTT);
    }


    protected Label titre() {
        var lbl = new Label("Emploi du temps : " + formateur.toString());
        lbl.setStyle("-fx-font-weight: bold; -fx-font-size: 22");
        return lbl;
    }

    protected GridPane info() {
        var grid = new GridPane();
        grid.setAlignment(Pos.TOP_CENTER);
        grid.setHgap(5);

        grid.add(new Text("DR"), 0, 0);
        grid.add(new Text("Complexe"), 0, 1);
        grid.add(new Text("Etablissement"), 0, 2);

        grid.add(new Text(" : DRO"), 1, 0);
        grid.add(new Text(" : Complexe Berkane"), 1, 1);
        grid.add(new Text(" : " + etablissement.getAbreviation()), 1, 2);

        var region1 = new Region();
        region1.setPrefWidth(40);
        grid.add(region1, 2, 0);

        grid.add(new Text("Statut"), 3, 0);

        grid.add(new Text(" : " + formateur.getType()), 4, 0);


        var region2 = new Region();
        region2.setPrefWidth(40);
        grid.add(region2, 5, 0);

        grid.add(new Text("Matrecule"), 6, 0);
        grid.add(new Text("Nom"), 6, 1);
        grid.add(new Text("Prénom"), 6, 2);

        grid.add(new Text(" : " + formateur.getMatricule()), 7, 0);
        grid.add(new Text(" : " + formateur.getNom()), 7, 1);
        grid.add(new Text(" : " + formateur.getPrenom()), 7, 2);

        var region3 = new Region();
        region3.setPrefWidth(40);
        grid.add(region3, 8, 0);

        grid.add(new Text("Date d'application"), 9, 0);

        grid.add(new Text(" : " + etablissement.getDateDebut()), 10, 0);

        grid.getChildren().stream().filter(node -> node instanceof Text)
                .forEach(node -> node.setStyle("-fx-font-size: 11"));
        return grid;
    }
}
