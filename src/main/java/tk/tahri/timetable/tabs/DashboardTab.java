package tk.tahri.timetable.tabs;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.controlsfx.glyphfont.FontAwesome;
import tk.tahri.timetable.dao.Database;
import tk.tahri.timetable.tools.Config;

public class DashboardTab extends EditorTab {
    private final Label lblFormateur;
    private final Label lblGroupe;
    private final Label lblEspace;
    private final Label lblSeance;
    private final BarChart<String, Number> bc;
    private final PieChart pieChart;

    public DashboardTab(String nom) {
        super(nom);
        this.setClosable(false);
        FontAwesome fontAwesome = new FontAwesome();
        var vBox = new VBox(20);
        vBox.setPadding(new Insets(20));

        var groupeTitle = new Label("Tableau de bord : " + Config.getProperty("file"));
        groupeTitle.setGraphic(fontAwesome.create(FontAwesome.Glyph.DASHBOARD).color(Color.WHITE).size(26));
        groupeTitle.setGraphicTextGap(10);
        groupeTitle.setMaxWidth(Double.MAX_VALUE);
        groupeTitle.getStyleClass().add("groupe-title-main");
        vBox.getChildren().add(groupeTitle);


        var cards = new HBox(50);
        vBox.getChildren().add(cards);
        cards.setAlignment(Pos.CENTER);


        lblFormateur = new Label("16");
        lblFormateur.setPrefSize(220, 120);
        lblFormateur.setStyle("""
                -fx-background-color: #038C4C;
                -fx-padding: 20;
                -fx-text-fill: #fff;
                -fx-background-radius: 10;
                -fx-font-size: 44;
                -fx-font-weight: bold;

                """);
        lblFormateur.setGraphic(fontAwesome.create(FontAwesome.Glyph.USER).color(Color.WHITE).size(52));
        lblFormateur.setGraphicTextGap(50);


        lblGroupe = new Label("54");
        lblGroupe.setPrefSize(220, 120);
        lblGroupe.setStyle("""
                -fx-background-color: #1C588C;
                -fx-padding: 20;
                -fx-text-fill: #fff;
                -fx-background-radius: 10;
                -fx-font-size: 44;
                -fx-font-weight: bold;
                """);
        lblGroupe.setGraphic(fontAwesome.create(FontAwesome.Glyph.USERS).color(Color.WHITE).size(52));
        lblGroupe.setGraphicTextGap(50);

        lblEspace = new Label("12");
        lblEspace.setPrefSize(220, 120);
        lblEspace.setStyle("""
                -fx-background-color: #8D9AA6;
                -fx-padding: 20;
                -fx-text-fill: #fff;
                -fx-background-radius: 10;
                -fx-font-size: 44;
                -fx-font-weight: bold;
                """);
        lblEspace.setGraphic(fontAwesome.create(FontAwesome.Glyph.HOME).color(Color.WHITE).size(52));
        lblEspace.setGraphicTextGap(50);


        lblSeance = new Label("234");
        lblSeance.setPrefSize(220, 120);
        lblSeance.setStyle("""
                -fx-background-color: #e36b40;
                -fx-padding: 20;
                -fx-text-fill: #fff;
                -fx-background-radius: 10;
                -fx-font-size: 44;
                -fx-font-weight: bold;
                """);
        lblSeance.setGraphic(fontAwesome.create(FontAwesome.Glyph.CALENDAR).color(Color.WHITE).size(52));
        lblSeance.setGraphicTextGap(50);

        cards.getChildren().addAll(lblFormateur, lblGroupe, lblEspace, lblSeance);

        var hBox=new HBox(10);

        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        bc = new BarChart<>(xAxis, yAxis);
        bc.setTitle("Occupation des espaces");
        bc.setLegendVisible(false);
        xAxis.setLabel("Espace");
        yAxis.setLabel("Heurs/semaine");
        HBox.setHgrow(bc, Priority.ALWAYS);

        pieChart = new PieChart();
        pieChart.setTitle("Taux d'occupation");
        pieChart.setLegendVisible(false);

        hBox.getChildren().addAll(bc,pieChart);

        vBox.getChildren().add(hBox);
        this.setContent(vBox);
    }

    @Override
    protected void selectionChanged() {
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
        var pieChartData = FXCollections.observableArrayList(
                new PieChart.Data("Occupation", occupe),
                new PieChart.Data("libére", total - occupe));
        pieChart.setData(pieChartData);

    }

    @Override
    public void print() {

    }
}
