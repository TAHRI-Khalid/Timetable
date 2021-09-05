package tk.tahri.timetable.print;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import tk.tahri.timetable.Entities.Espace;
import tk.tahri.timetable.Entities.Groupe;
import tk.tahri.timetable.view.EspacePrintTimeTableView;
import tk.tahri.timetable.view.GroupePrintTimeTableView;

import java.util.Objects;

public class EspaceTimeTablePrint extends VBox {
    private final Espace espace;

    public EspaceTimeTablePrint(Espace espace) {
        super(15);
        this.espace = espace;
        this.setAlignment(Pos.TOP_CENTER);
        var groupeTT=new EspacePrintTimeTableView(espace);
        groupeTT.loadSeanceViews(false);
        groupeTT.setTranslateX(30);
        this.getChildren().addAll(header(), titre(), info(), groupeTT);
    }

    private HBox header() {
        var header = new HBox(10);
        var logo = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/logo.png"))));
        logo.setPreserveRatio(true);
        logo.setFitHeight(50);
        var titreAr = new Label("مكتب التكوين المهني وإنعاش الشغل");
        var titreFr = new Label("Office de la Formation Professionnelle et de la Promotion du Travail");
        var vBox = new VBox(10);
        HBox.setHgrow(vBox, Priority.ALWAYS);
        vBox.setAlignment(Pos.CENTER);
        vBox.getChildren().addAll(titreAr, titreFr);
        header.getChildren().addAll(logo, vBox);
        return header;
    }

    private Label titre() {
        var lbl = new Label("Emploi du temps : " + espace.getNom());
        lbl.setStyle("-fx-font-weight: bold; -fx-font-size: 22");
        return lbl;
    }

    private GridPane info() {
        var grid = new GridPane();
        grid.setAlignment(Pos.TOP_CENTER);
        grid.setHgap(5);

        grid.add(new Text("DR"), 0, 0);
        grid.add(new Text("Complexe"), 0, 1);
        grid.add(new Text("Etablissement"), 0, 2);

        grid.add(new Text(" : DRO"), 1, 0);
        grid.add(new Text(" : Complexe Berkane"), 1, 1);
        grid.add(new Text(" : ISTA Berkane "), 1, 2);

        var region1 = new Region();
        region1.setPrefWidth(50);
        grid.add(region1, 2, 0);

        var region2 = new Region();
        region2.setPrefWidth(50);
        grid.add(region2, 5, 0);

        grid.add(new Text("Type"), 6, 0);
        grid.add(new Text("nom"), 6, 1);

        grid.add(new Text(" : " + espace.getType()), 7, 0);
        grid.add(new Text(" : " + espace.getNom()), 7, 1);

        grid.getChildren().stream().filter(node -> node instanceof Text)
                .forEach(node -> node.setStyle("-fx-font-size: 11"));
        return grid;
    }
}
