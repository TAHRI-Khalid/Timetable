package tk.tahri.timetable.tabs;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.print.PageLayout;
import javafx.print.PageOrientation;
import javafx.print.Paper;
import javafx.print.PrinterJob;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.controlsfx.control.CheckListView;
import org.controlsfx.glyphfont.FontAwesome;
import tk.tahri.timetable.Entities.Espace;
import tk.tahri.timetable.Entities.Formateur;
import tk.tahri.timetable.Entities.Groupe;
import tk.tahri.timetable.dao.Database;
import tk.tahri.timetable.print.EspaceTimeTablePrint;
import tk.tahri.timetable.print.FormateurTimeTablePrint;
import tk.tahri.timetable.print.GroupeTimeTablePrint;
import tk.tahri.timetable.tools.Config;

import java.util.stream.IntStream;

public class PrintTab extends EditorTab {

    private final CheckListView<Groupe> groupeCheckListView;
    private final CheckListView<Formateur> formateurCheckListView;
    private final CheckListView<Espace> espaceCheckListView;

    public PrintTab(String nom) {
        super(nom);
        FontAwesome fontAwesome = new FontAwesome();
        var vBox = new VBox(15);
        vBox.setPadding(new Insets(20));

        var groupeTitle = new Label("Espace d'impression : ");
        groupeTitle.setGraphic(fontAwesome.create(FontAwesome.Glyph.PRINT).color(Color.WHITE).size(26));
        groupeTitle.setGraphicTextGap(10);
        groupeTitle.setMaxWidth(Double.MAX_VALUE);
        groupeTitle.getStyleClass().add("groupe-title-main");
        vBox.getChildren().add(groupeTitle);


        GridPane grid = new GridPane();
        grid.setVgap(15);
        grid.setHgap(20);
        VBox.setVgrow(grid,Priority.ALWAYS);
        grid.setAlignment(Pos.CENTER);

        grid.add(new Label("Groupes :"), 0, 0);
        groupeCheckListView = new CheckListView<>();
        GridPane.setVgrow(groupeCheckListView,Priority.ALWAYS);
        groupeCheckListView.getItems().addAll(Database.getInstance().getGroupes());
        grid.add(groupeCheckListView, 0, 1);
        var hBox1 = new HBox(20);
        var btnGroupeAdd = new Button();
        btnGroupeAdd.setOnAction(actionEvent ->
                IntStream.range(0, groupeCheckListView.getItems().size())
                        .forEach(groupeCheckListView.getCheckModel()::check));
        btnGroupeAdd.setGraphic(fontAwesome.create(FontAwesome.Glyph.PLUS_SQUARE).size(16));
        var btnGroupeRemove = new Button();
        btnGroupeRemove.setOnAction(actionEvent ->
                IntStream.range(0, groupeCheckListView.getItems().size())
                        .forEach(groupeCheckListView.getCheckModel()::clearCheck));
        btnGroupeRemove.setGraphic(fontAwesome.create(FontAwesome.Glyph.MINUS_SQUARE).size(16));
        hBox1.getChildren().addAll(btnGroupeAdd, btnGroupeRemove);
        hBox1.setAlignment(Pos.CENTER);
        grid.add(hBox1, 0, 2);


        grid.add(new Label("Formateurs :"), 1, 0);
        formateurCheckListView = new CheckListView<Formateur>();
        formateurCheckListView.getItems().addAll(Database.getInstance().getFormateurs());
        grid.add(formateurCheckListView, 1, 1);
        var hBox2 = new HBox(20);
        var btnFormateurAdd = new Button();
        btnFormateurAdd.setOnAction(actionEvent ->
                IntStream.range(0, formateurCheckListView.getItems().size())
                        .forEach(formateurCheckListView.getCheckModel()::check));
        btnFormateurAdd.setGraphic(fontAwesome.create(FontAwesome.Glyph.PLUS_SQUARE).size(16));
        var btnFormateurRemove = new Button();
        btnFormateurRemove.setOnAction(actionEvent ->
                IntStream.range(0, formateurCheckListView.getItems().size())
                        .forEach(formateurCheckListView.getCheckModel()::clearCheck));
        btnFormateurRemove.setGraphic(fontAwesome.create(FontAwesome.Glyph.MINUS_SQUARE).size(16));
        hBox2.getChildren().addAll(btnFormateurAdd, btnFormateurRemove);
        hBox2.setAlignment(Pos.CENTER);
        grid.add(hBox2, 1, 2);


        grid.add(new Label("Espaces :"), 2, 0);
        espaceCheckListView = new CheckListView<Espace>();
        espaceCheckListView.getItems().addAll(Database.getInstance().getEspaces());
        grid.add(espaceCheckListView, 2, 1);

        var hBox3 = new HBox(20);
        var btnEspaceAdd = new Button();
        btnEspaceAdd.setOnAction(actionEvent ->
                IntStream.range(0, espaceCheckListView.getItems().size())
                        .forEach(espaceCheckListView.getCheckModel()::check));
        btnEspaceAdd.setGraphic(fontAwesome.create(FontAwesome.Glyph.PLUS_SQUARE).size(16));
        var btnEspaceRemove = new Button();
        btnEspaceRemove.setOnAction(actionEvent ->
                IntStream.range(0, espaceCheckListView.getItems().size())
                        .forEach(espaceCheckListView.getCheckModel()::clearCheck));
        btnEspaceRemove.setGraphic(fontAwesome.create(FontAwesome.Glyph.MINUS_SQUARE).size(16));
        hBox3.getChildren().addAll(btnEspaceAdd, btnEspaceRemove);
        hBox3.setAlignment(Pos.CENTER);
        grid.add(hBox3, 2, 2);


        var btnPrint = new Button("Imprimer");
        btnPrint.setMaxWidth(Double.MAX_VALUE);
        btnPrint.setOnAction(e -> print());
        GridPane.setHalignment(btnPrint, HPos.CENTER);
        grid.add(btnPrint, 1, 3);

        vBox.getChildren().add(grid);
        this.setContent(vBox);
    }

    @Override
    protected void selectionChanged() {
        groupeCheckListView.getItems().clear();
        formateurCheckListView.getItems().clear();
        espaceCheckListView.getItems().clear();

        groupeCheckListView.getItems().addAll(Database.getInstance().getGroupes());
        formateurCheckListView.getItems().addAll(Database.getInstance().getFormateurs());
        espaceCheckListView.getItems().addAll(Database.getInstance().getEspaces());
    }

    @Override
    public void print() {
        PrinterJob job = PrinterJob.createPrinterJob();
        PageLayout pageLayout = job.getPrinter().createPageLayout(Paper.A4, PageOrientation.LANDSCAPE, 40, 40, 40, 40);
        job.getJobSettings().setPageLayout(pageLayout);

        if (job.showPrintDialog(groupeCheckListView.getScene().getWindow())) {
            groupeCheckListView.getCheckModel().getCheckedItems().forEach(groupe -> {
                var TT = new GroupeTimeTablePrint(groupe);
                TT.setPrefWidth(pageLayout.getPrintableWidth());
                job.printPage(TT);
            });
            formateurCheckListView.getCheckModel().getCheckedItems().forEach(formateur -> {
                var TT = new FormateurTimeTablePrint(formateur);
                TT.setPrefWidth(pageLayout.getPrintableWidth() );
                job.printPage(TT);
            });
            espaceCheckListView.getCheckModel().getCheckedItems().forEach(espace -> {
                var TT = new EspaceTimeTablePrint(espace);
                TT.setPrefWidth(pageLayout.getPrintableWidth());
                job.printPage(TT);
            });

            job.endJob();
        }
    }
}
