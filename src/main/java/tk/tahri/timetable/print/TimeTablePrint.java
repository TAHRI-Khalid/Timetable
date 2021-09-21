package tk.tahri.timetable.print;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import tk.tahri.timetable.Entities.Etablissement;
import tk.tahri.timetable.dao.Database;

import java.util.Objects;

public abstract class TimeTablePrint extends VBox {
    protected Etablissement etablissement;

    public TimeTablePrint(int space) {
        super(space);
        this.getStylesheets().add(getClass().getResource("/css/print.css").toString());
        etablissement = Database.getInstance().getItablissment();
    }

    protected HBox header() {
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

    protected HBox titre(String title) {
        var hBox = new HBox();
        hBox.setAlignment(Pos.CENTER);
        var vBox = new VBox();
        vBox.setAlignment(Pos.CENTER);
        var lblTitle = new Label("EMPLOI DU TEMPS : " + title);
        lblTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 20");
        var lblAnneeDeFormation = new Label("ANNEE DE FORMATION : 2020/2021");
        lblAnneeDeFormation.setStyle("-fx-font-weight: bold; -fx-font-size: 14");
        vBox.getChildren().addAll(lblTitle, lblAnneeDeFormation);
        hBox.getChildren().add(vBox);
        return hBox;
    }

    protected HBox info(GridPane... gridPane) {
        var hBox = new HBox();
        hBox.getStyleClass().add("timetable-info");
        var grid = new GridPane();

        grid.add(new Text("DR"), 0, 0);
        grid.add(new Text("Complexe"), 0, 1);
        grid.add(new Text("Etablissement"), 0, 2);

        grid.add(new Text(" : DRO"), 1, 0);
        grid.add(new Text(" : Complexe Berkane"), 1, 1);
        grid.add(new Text(" : " + etablissement.getAbreviation()), 1, 2);

        hBox.getChildren().add(grid);
        hBox.getChildren().addAll(gridPane);

        grid = new GridPane();
        grid.add(new Text("Date d'effet"), 0, 0);
        grid.add(new Text(" : " + etablissement.getDateDebut()), 1, 0);

        if (etablissement.getDateFin()!=null){
            grid.add(new Text("Date de fin"), 0, 1);
            grid.add(new Text(" : " + etablissement.getDateFin()), 1, 1);
        }
        hBox.getChildren().add(grid);
        return hBox;
    }

}
