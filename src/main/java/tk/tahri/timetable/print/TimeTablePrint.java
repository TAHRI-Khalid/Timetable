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
        var header = new HBox(15);
        header.setAlignment(Pos.CENTER);

        try {
            var logoStream = getClass().getResourceAsStream("/images/logo.png");
            if (logoStream != null) {
                var logo = new ImageView(new Image(logoStream));
                logo.setPreserveRatio(true);
                logo.setFitHeight(45);
                header.getChildren().add(logo);
            }
        } catch (Exception ignored) {
        }

        var vBox = new VBox(3);
        HBox.setHgrow(vBox, Priority.ALWAYS);
        vBox.setAlignment(Pos.CENTER);

        var titreAr = new Text("مكتب التكوين المهني وإنعاش الشغل");
        titreAr.setFont(Font.font(Font.getDefault().getFamily(), FontWeight.BOLD, 12));
        var titreFr = new Text("Office de la Formation Professionnelle et de la Promotion du Travail");
        titreFr.setFont(Font.font(Font.getDefault().getFamily(), FontWeight.BOLD, 11));

        vBox.getChildren().addAll(titreAr, titreFr);

        if (etablissement != null && etablissement.getNom() != null && !etablissement.getNom().isBlank()) {
            var titreEtab = new Text(etablissement.getNom());
            titreEtab.setFont(Font.font(Font.getDefault().getFamily(), FontWeight.BOLD, 11));
            vBox.getChildren().add(titreEtab);
        }

        header.getChildren().add(vBox);
        return header;
    }

    protected HBox titre(String title) {
        var hBox = new HBox();
        hBox.setAlignment(Pos.CENTER);
        HBox.setHgrow(hBox, Priority.ALWAYS);
        var vBox = new VBox(3);
        vBox.setAlignment(Pos.CENTER);
        var lblTitle = new Text("EMPLOI DU TEMPS : " + (title != null ? title : ""));
        lblTitle.setFont(Font.font(Font.getDefault().getFamily(), FontWeight.BOLD, 15));

        String annee = (etablissement != null && etablissement.getAnneeDeFormation() != null)
                ? etablissement.getAnneeDeFormation()
                : "-";
        var lblAnneeDeFormation = new Text("ANNEE DE FORMATION : " + annee);
        lblAnneeDeFormation.setFont(Font.font(Font.getDefault().getFamily(), FontWeight.BOLD, 12));
        vBox.getChildren().addAll(lblTitle, lblAnneeDeFormation);
        hBox.getChildren().add(vBox);
        return hBox;
    }

    protected HBox info(GridPane... gridPane) {
        var hBox = new HBox(15);
        HBox.setHgrow(hBox, Priority.ALWAYS);
        hBox.setAlignment(Pos.TOP_CENTER);

        var grid = new GridPane();
        grid.setHgap(5);
        grid.setVgap(3);

        grid.add(new Text("Direction"), 0, 0);
        grid.add(new Text("Complexe"), 0, 1);
        grid.add(new Text("Etablissement"), 0, 2);

        String direction = (etablissement != null && etablissement.getDirection() != null) ? etablissement.getDirection() : "-";
        String complexe = (etablissement != null && etablissement.getComplexe() != null) ? etablissement.getComplexe() : "-";
        String abreviation = (etablissement != null && etablissement.getAbreviation() != null) ? etablissement.getAbreviation() : "-";

        grid.add(new Text(" : " + direction), 1, 0);
        grid.add(new Text(" : " + complexe), 1, 1);
        grid.add(new Text(" : " + abreviation), 1, 2);

        hBox.getChildren().add(grid);

        if (gridPane != null) {
            for (var gp : gridPane) {
                gp.setHgap(5);
                gp.setVgap(3);
                hBox.getChildren().add(gp);
            }
        }

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        var dateGrid = new GridPane();
        dateGrid.setHgap(5);
        dateGrid.setVgap(3);

        dateGrid.add(new Text("Date d'effet"), 0, 0);
        String dateDebutStr = (etablissement != null && etablissement.getDateDebut() != null)
                ? dtf.format(etablissement.getDateDebut())
                : "-";
        dateGrid.add(new Text(" : " + dateDebutStr), 1, 0);

        if (etablissement != null && etablissement.getDateFin() != null) {
            dateGrid.add(new Text("Date de fin"), 0, 1);
            dateGrid.add(new Text(" : " + dtf.format(etablissement.getDateFin())), 1, 1);
        }
        var duration = printTimeTableView.getDuration();
        long hours = duration != null ? duration.toHours() : 0;
        long minutes = duration != null ? duration.toMinutesPart() : 0;
        dateGrid.add(new Text("Masse horaire"), 0, 2);
        dateGrid.add(new Text(" : " + hours + "h" + String.format("%02d", minutes) + "min"), 1, 2);

        hBox.getChildren().add(dateGrid);
        return hBox;
    }

    protected HBox body() {
        var hBox = new HBox();
        hBox.setAlignment(Pos.TOP_CENTER);
        printTimeTableView.getChildren().stream().filter(node -> node instanceof SeanceView).forEach(node -> {
            var sv = (SeanceView) node;
            sv.getStyleClass().clear();

            boolean isFad = sv.getSeance() != null && "FAD".equalsIgnoreCase(sv.getSeance().getType());
            Color bgFill = isFad ? Color.web("#f1f5f9") : Color.WHITE;

            sv.setBackground(new Background(new BackgroundFill(bgFill, new CornerRadii(6), Insets.EMPTY)));
            sv.setAlignment(Pos.CENTER);
            sv.setBorder(new Border(new BorderStroke(
                    Color.BLACK,
                    isFad ? BorderStrokeStyle.DASHED : BorderStrokeStyle.SOLID,
                    new CornerRadii(6),
                    new BorderWidths(1.5)
            )));
            sv.getChildren().stream().filter(n -> n instanceof Text).forEach(txt -> {
                var text = (Text) txt;
                text.setFont(Font.font(Font.getDefault().getFamily(), FontWeight.BOLD, 10.5));
                text.setFill(Color.BLACK);
            });
        });

        hBox.getChildren().add(printTimeTableView);
        return hBox;
    }

    protected HBox footer(String... signatures) {
        var footerBox = new HBox(20);
        footerBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(footerBox, Priority.ALWAYS);
        footerBox.setPadding(new Insets(10, 5, 0, 5));

        var dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        var lblDate = new Text("Date d'édition : " + java.time.LocalDateTime.now().format(dtf));
        lblDate.setFont(Font.font(Font.getDefault().getFamily(), FontWeight.NORMAL, 9));
        lblDate.setFill(Color.DARKGRAY);

        var leftBox = new VBox(lblDate);
        leftBox.setAlignment(Pos.BOTTOM_LEFT);
        HBox.setHgrow(leftBox, Priority.ALWAYS);
        footerBox.getChildren().add(leftBox);

        if (signatures != null && signatures.length > 0) {
            var sigContainer = new HBox(20);
            sigContainer.setAlignment(Pos.CENTER_RIGHT);

            for (String sigLabel : signatures) {
                var sigBox = new VBox(22);
                sigBox.setAlignment(Pos.TOP_CENTER);
                sigBox.setPrefWidth(140);
                sigBox.setPadding(new Insets(4, 8, 4, 8));
                sigBox.setBorder(new Border(new BorderStroke(
                        Color.GRAY,
                        BorderStrokeStyle.DASHED,
                        new CornerRadii(4),
                        new BorderWidths(1)
                )));

                var txtSig = new Text(sigLabel);
                txtSig.setFont(Font.font(Font.getDefault().getFamily(), FontWeight.BOLD, 9));
                txtSig.setFill(Color.BLACK);

                sigBox.getChildren().add(txtSig);
                sigContainer.getChildren().add(sigBox);
            }
            footerBox.getChildren().add(sigContainer);
        }

        return footerBox;
    }

}
