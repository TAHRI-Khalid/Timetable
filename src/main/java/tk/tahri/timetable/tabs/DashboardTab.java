package tk.tahri.timetable.tabs;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.controlsfx.glyphfont.FontAwesome;
import tk.tahri.timetable.dao.Database;
import tk.tahri.timetable.tools.Config;

import java.util.Objects;

public class DashboardTab extends EditorTab {

    private Label lblFormateur;
    private Label lblGroupe;
    private Label lblEspace;
    private Label lblSeance;
    private Label lblOccupation;
    private BarChart<String, Number> bc;

    public DashboardTab(String nom) {
        super(nom);
        this.setClosable(false);
    }


    @Override
    protected void selectionChanged() {
        if (Config.getProperty("file").isEmpty() && Config.getProperty("temp").isEmpty()) {
            this.setContent(noData());
        } else {
            this.setContent(dashboard());
            var d = Database.getInstance().getDashboard();
            lblEspace.setText(String.valueOf(d.getNombreEspaces()));
            lblFormateur.setText(String.valueOf(d.getNombreFormateurs()));
            lblGroupe.setText(String.valueOf(d.getNombreGroupes()));
            lblSeance.setText(String.valueOf(d.getNombreSeances()));

            bc.getData().clear();
            var series1 = new XYChart.Series();
            d.getBarChartEspace().forEach((espace, aLong) -> {
                series1.getData().add(new XYChart.Data(espace.getNom(), aLong / 3600));
            });
            bc.getData().add(series1);

            var total = d.getBarChartEspace().size() * 10 * 6;
            var occupe = d.getBarChartEspace().values().stream().mapToLong(Long::longValue).sum() / 3600;

            lblOccupation.setText(String.format("%.1f%%", (float)occupe / total * 100));

        }
    }

    @Override
    public void print() {

    }

    private VBox dashboard() {
        FontAwesome fontAwesome = new FontAwesome();
        var vBox = new VBox(20);
        vBox.setPadding(new Insets(20));

        var groupeTitle = new Label("Tableau de bord : " + Config.getProperty("file"));
        groupeTitle.setGraphic(fontAwesome.create(FontAwesome.Glyph.DASHBOARD).color(Color.WHITE).size(26));
        groupeTitle.setGraphicTextGap(10);
        groupeTitle.setMaxWidth(Double.MAX_VALUE);
        groupeTitle.getStyleClass().add("groupe-title-main");
        vBox.getChildren().add(groupeTitle);


        var cards = new HBox(40);
        vBox.getChildren().add(cards);
        cards.setAlignment(Pos.CENTER);

        var vBox1 = new VBox(10);
        lblFormateur = new Label();
        lblFormateur.setStyle("""
                -fx-text-fill: #fff;
                -fx-font-size: 44;
                -fx-font-weight: bold;
                """);
        lblFormateur.setGraphic(fontAwesome.create(FontAwesome.Glyph.USER).color(Color.WHITE).size(52));
        lblFormateur.setGraphicTextGap(50);
        var lbl1 = new Label("Formateurs");
        lbl1.setStyle("""
                -fx-text-fill: #fff;
                -fx-font-size: 16;
                -fx-font-weight: bold;
                """);
        vBox1.setStyle("""
                -fx-background-color: #038C4C;
                -fx-padding: 12;
                -fx-background-radius: 10;
                -fx-alignment: center;
                -fx-pref-width: 230;
                """);
        vBox1.getChildren().addAll(lblFormateur, lbl1);


        var vBox2 = new VBox(10);
        lblGroupe = new Label();
        lblGroupe.setStyle("""
                -fx-text-fill: #fff;
                -fx-font-size: 44;
                -fx-font-weight: bold;
                """);
        lblGroupe.setGraphic(fontAwesome.create(FontAwesome.Glyph.USERS).color(Color.WHITE).size(52));
        lblGroupe.setGraphicTextGap(50);
        var lbl2 = new Label("Groupes");
        lbl2.setStyle("""
                -fx-text-fill: #fff;
                -fx-font-size: 16;
                -fx-font-weight: bold;
                """);
        vBox2.setStyle("""
                -fx-background-color: #1C588C;
                -fx-padding: 12;
                -fx-background-radius: 10;
                -fx-alignment: center;
                -fx-pref-width: 230;
                """);
        vBox2.getChildren().addAll(lblGroupe, lbl2);


        var vBox3 = new VBox(10);
        lblEspace = new Label();
        lblEspace.setStyle("""
                -fx-text-fill: #fff;
                -fx-font-size: 44;
                -fx-font-weight: bold;
                """);
        lblEspace.setGraphic(fontAwesome.create(FontAwesome.Glyph.HOME).color(Color.WHITE).size(52));
        lblEspace.setGraphicTextGap(50);
        var lbl3 = new Label("Espaces");
        lbl3.setStyle("""
                -fx-text-fill: #fff;
                -fx-font-size: 16;
                -fx-font-weight: bold;
                """);
        vBox3.setStyle("""
                -fx-background-color: #8D9AA6;
                -fx-padding: 12;
                -fx-background-radius: 10;
                -fx-alignment: center;
                -fx-pref-width: 230;
                """);
        vBox3.getChildren().addAll(lblEspace, lbl3);


        var vBox4 = new VBox(10);
        lblSeance = new Label();
        lblSeance.setStyle("""
                -fx-text-fill: #fff;
                -fx-font-size: 44;
                -fx-font-weight: bold;
                """);
        lblSeance.setGraphic(fontAwesome.create(FontAwesome.Glyph.CALENDAR).color(Color.WHITE).size(52));
        lblSeance.setGraphicTextGap(50);
        var lbl4 = new Label("Séances");
        lbl4.setStyle("""
                -fx-text-fill: #fff;
                -fx-font-size: 16;
                -fx-font-weight: bold;
                """);
        vBox4.setStyle("""
                -fx-background-color: #e36b40;
                -fx-padding: 12;
                -fx-background-radius: 10;
                -fx-alignment: center;
                -fx-pref-width: 230;
                """);
        vBox4.getChildren().addAll(lblSeance, lbl4);


        var vBox5 = new VBox(10);
        lblOccupation = new Label();
        lblOccupation.setStyle("""
                -fx-text-fill: #fff;
                -fx-font-size: 42;
                -fx-font-weight: bold;
                """);
        lblOccupation.setGraphic(fontAwesome.create(FontAwesome.Glyph.PIE_CHART).color(Color.WHITE).size(52));
        lblOccupation.setGraphicTextGap(5);
        var lbl5 = new Label("Taux d'occupation");
        lbl5.setStyle("""
                -fx-text-fill: #fff;
                -fx-font-size: 16;
                -fx-font-weight: bold;
                """);
        vBox5.setStyle("""
                -fx-background-color: #e36b40;
                -fx-padding: 5;
                -fx-background-radius: 10;
                -fx-alignment: center;
                -fx-pref-width: 230;
                """);
        vBox5.getChildren().addAll(lblOccupation, lbl5);

        cards.getChildren().addAll(vBox1, vBox2, vBox3, vBox4, vBox5);


        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        bc = new BarChart<>(xAxis, yAxis);
        bc.setTitle("Occupation des espaces");
        bc.setLegendVisible(false);
        xAxis.setLabel("Espace");
        yAxis.setLabel("Heurs/semaine");
        HBox.setHgrow(bc, Priority.ALWAYS);

        vBox.getChildren().add(bc);
        return vBox;
    }

    private VBox noData() {
        var vBox = new VBox(20);
        vBox.setAlignment(Pos.CENTER);
        vBox.setPadding(new Insets(20));
        var image = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/notdata.png"))));
        vBox.getChildren().add(image);
        return vBox;
    }
}
