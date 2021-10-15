package tk.tahri.timetable.print;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import tk.tahri.timetable.Entities.Etablissement;
import tk.tahri.timetable.dao.Database;
import tk.tahri.timetable.view.PrintTimeTableView;
import tk.tahri.timetable.view.SeanceView;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public abstract class TimeTablePrint extends VBox {
    protected final Etablissement etablissement;
    protected final PrintTimeTableView printTimeTableView;

    public TimeTablePrint(int space, PrintTimeTableView printTimeTableView) {
        super(space);
        this.printTimeTableView = printTimeTableView;
        this.printTimeTableView.loadSeanceViews(false);
        this.setAlignment(Pos.TOP_CENTER);
        etablissement = Database.getInstance().getItablissment();
    }

    protected HBox header() {
        var header = new HBox(10);
        var logo = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/logo.png"))));
        logo.setPreserveRatio(true);
        logo.setFitHeight(50);
        var titreAr = new Text("مكتب التكوين المهني وإنعاش الشغل");
        var titreFr = new Text("Office de la Formation Professionnelle et de la Promotion du Travail");
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
        HBox.setHgrow(hBox, Priority.ALWAYS);
        var vBox = new VBox();
        vBox.setAlignment(Pos.CENTER);
        var lblTitle = new Text("EMPLOI DU TEMPS : " + title);
        lblTitle.setFont(Font.font(Font.getDefault().getFamily(), FontWeight.BOLD, 16));
        var lblAnneeDeFormation = new Text("ANNEE DE FORMATION : " + etablissement.getAnneeDeFormation());
        lblAnneeDeFormation.setFont(Font.font(Font.getDefault().getFamily(), FontWeight.BOLD, 14));
        vBox.getChildren().addAll(lblTitle, lblAnneeDeFormation);
        hBox.getChildren().add(vBox);
        return hBox;
    }

    protected HBox info(GridPane... gridPane) {
        var hBox = new HBox(15);
        HBox.setHgrow(hBox, Priority.ALWAYS);
        hBox.setAlignment(Pos.TOP_CENTER);

        var grid = new GridPane();

        grid.add(new Text("Direction"), 0, 0);
        grid.add(new Text("Complexe"), 0, 1);
        grid.add(new Text("Etablissement"), 0, 2);

        grid.add(new Text(" : " + etablissement.getDirection()), 1, 0);
        grid.add(new Text(" : " + etablissement.getComplexe()), 1, 1);
        grid.add(new Text(" : " + etablissement.getAbreviation()), 1, 2);

        hBox.getChildren().add(grid);
        hBox.getChildren().addAll(gridPane);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        grid = new GridPane();
        grid.add(new Text("Date d'effet"), 0, 0);
        grid.add(new Text(" : " + dtf.format(etablissement.getDateDebut())), 1, 0);

        if (etablissement.getDateFin() != null) {
            grid.add(new Text("Date de fin"), 0, 1);
            grid.add(new Text(" : " + dtf.format(etablissement.getDateFin())), 1, 1);
        }
        var duration = printTimeTableView.getDuration();
        grid.add(new Text("Masse horaire"), 0, 2);
        grid.add(new Text(" : " + duration.toHours() + "h" + duration.toMinutesPart() + "min"), 1, 2);

        hBox.getChildren().add(grid);
        return hBox;
    }

    protected HBox body() {
        var hBox = new HBox();
        hBox.setAlignment(Pos.TOP_CENTER);

        printTimeTableView.getChildren().stream().filter(node -> node instanceof SeanceView).forEach(node -> {
            var sv = (SeanceView) node;
            sv.getStyleClass().clear();
            sv.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(7), Insets.EMPTY)));
            sv.setAlignment(Pos.CENTER);
            sv.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, new CornerRadii(7), new BorderWidths(1.5))));
            sv.getChildren().stream().filter(n -> n instanceof Text).forEach(txt -> {
                var text = (Text) txt;
                text.setFont(Font.font(Font.getDefault().getFamily(), FontWeight.BOLD, 11));
                text.setFill(Color.BLACK);
            });
        });

        hBox.getChildren().add(printTimeTableView);
        return hBox;
    }

}
