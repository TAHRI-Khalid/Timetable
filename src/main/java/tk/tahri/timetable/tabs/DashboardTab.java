package tk.tahri.timetable.tabs;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.controlsfx.glyphfont.FontAwesome;
import tk.tahri.timetable.dao.Database;
import tk.tahri.timetable.tools.Config;

import java.io.File;
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
            var series1 = new XYChart.Series<String, Number>();
            d.getBarChartEspace().forEach((espace, aLong) -> series1.getData().add(new XYChart.Data<>(espace.getNom(), aLong / 3600)));
            bc.getData().add(series1);

            var total = d.getBarChartEspace().size() * 10 * 6;
            var occupe = d.getBarChartEspace().values().stream().mapToLong(Long::longValue).sum() / 3600;

            lblOccupation.setText(total > 0 ? String.format("%.1f%%", (float) occupe / total * 100) : "0.0%");
        }
    }

    @Override
    public void print() {

    }

    private ScrollPane dashboard() {
        FontAwesome fontAwesome = new FontAwesome();
        var vBox = new VBox(20);
        vBox.setPadding(new Insets(20));

        String filePath = Config.getProperty("file");
        String titleText = "Tableau de bord";
        if (filePath != null && !filePath.isBlank()) {
            File f = new File(filePath);
            titleText += " — " + f.getName();
        }

        var groupeTitle = new Label(titleText);
        groupeTitle.setGraphic(fontAwesome.create(FontAwesome.Glyph.DASHBOARD).color(Color.WHITE).size(22));
        groupeTitle.setGraphicTextGap(10);
        groupeTitle.setMaxWidth(Double.MAX_VALUE);
        groupeTitle.getStyleClass().add("groupe-title-main");
        vBox.getChildren().add(groupeTitle);

        var cards = new HBox(16);
        cards.setAlignment(Pos.CENTER);
        vBox.getChildren().add(cards);

        // 1. Formateurs
        var vBox1 = new VBox(6);
        vBox1.getStyleClass().addAll("kpi-card", "kpi-card-formateur");
        HBox.setHgrow(vBox1, Priority.ALWAYS);
        lblFormateur = new Label("0");
        lblFormateur.getStyleClass().add("kpi-number");
        lblFormateur.setGraphic(fontAwesome.create(FontAwesome.Glyph.USER).color(Color.WHITE).size(36));
        lblFormateur.setGraphicTextGap(20);
        var lbl1 = new Label("Formateurs");
        lbl1.getStyleClass().add("kpi-label");
        vBox1.getChildren().addAll(lblFormateur, lbl1);

        // 2. Groupes
        var vBox2 = new VBox(6);
        vBox2.getStyleClass().addAll("kpi-card", "kpi-card-groupe");
        HBox.setHgrow(vBox2, Priority.ALWAYS);
        lblGroupe = new Label("0");
        lblGroupe.getStyleClass().add("kpi-number");
        lblGroupe.setGraphic(fontAwesome.create(FontAwesome.Glyph.USERS).color(Color.WHITE).size(36));
        lblGroupe.setGraphicTextGap(20);
        var lbl2 = new Label("Groupes");
        lbl2.getStyleClass().add("kpi-label");
        vBox2.getChildren().addAll(lblGroupe, lbl2);

        // 3. Espaces
        var vBox3 = new VBox(6);
        vBox3.getStyleClass().addAll("kpi-card", "kpi-card-espace");
        HBox.setHgrow(vBox3, Priority.ALWAYS);
        lblEspace = new Label("0");
        lblEspace.getStyleClass().add("kpi-number");
        lblEspace.setGraphic(fontAwesome.create(FontAwesome.Glyph.HOME).color(Color.WHITE).size(36));
        lblEspace.setGraphicTextGap(20);
        var lbl3 = new Label("Espaces");
        lbl3.getStyleClass().add("kpi-label");
        vBox3.getChildren().addAll(lblEspace, lbl3);

        // 4. Séances
        var vBox4 = new VBox(6);
        vBox4.getStyleClass().addAll("kpi-card", "kpi-card-seance");
        HBox.setHgrow(vBox4, Priority.ALWAYS);
        lblSeance = new Label("0");
        lblSeance.getStyleClass().add("kpi-number");
        lblSeance.setGraphic(fontAwesome.create(FontAwesome.Glyph.CALENDAR).color(Color.WHITE).size(36));
        lblSeance.setGraphicTextGap(20);
        var lbl4 = new Label("Séances");
        lbl4.getStyleClass().add("kpi-label");
        vBox4.getChildren().addAll(lblSeance, lbl4);

        // 5. Taux d'occupation
        var vBox5 = new VBox(6);
        vBox5.getStyleClass().addAll("kpi-card", "kpi-card-occupation");
        HBox.setHgrow(vBox5, Priority.ALWAYS);
        lblOccupation = new Label("0.0%");
        lblOccupation.getStyleClass().add("kpi-number");
        lblOccupation.setGraphic(fontAwesome.create(FontAwesome.Glyph.PIE_CHART).color(Color.WHITE).size(36));
        lblOccupation.setGraphicTextGap(16);
        var lbl5 = new Label("Taux d'occupation");
        lbl5.getStyleClass().add("kpi-label");
        vBox5.getChildren().addAll(lblOccupation, lbl5);

        cards.getChildren().addAll(vBox1, vBox2, vBox3, vBox4, vBox5);

        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        bc = new BarChart<>(xAxis, yAxis);
        bc.setTitle("Occupation des espaces");
        bc.setLegendVisible(false);
        bc.setAnimated(false);
        xAxis.setLabel("Espace");
        yAxis.setLabel("Heures / semaine");
        VBox.setVgrow(bc, Priority.ALWAYS);

        var chartContainer = new VBox(bc);
        chartContainer.getStyleClass().add("chart-card");
        VBox.setVgrow(chartContainer, Priority.ALWAYS);
        vBox.getChildren().add(chartContainer);

        var scrollPane = new ScrollPane(vBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-padding: 0;");
        return scrollPane;
    }

    private VBox noData() {
        var vBox = new VBox(15);
        vBox.setAlignment(Pos.CENTER);
        vBox.setPadding(new Insets(40));
        var image = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/notdata.png"))));
        image.setFitWidth(180);
        image.setPreserveRatio(true);
        var lblTitle = new Label("Aucun emploi du temps actif");
        lblTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #475569;");
        var lblSubtitle = new Label("Ouvrez un fichier existant ou créez un nouveau projet depuis le menu Fichier.");
        lblSubtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #94a3b8;");
        vBox.getChildren().addAll(image, lblTitle, lblSubtitle);
        return vBox;
    }
}
