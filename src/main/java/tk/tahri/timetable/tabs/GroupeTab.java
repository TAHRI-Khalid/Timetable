package tk.tahri.timetable.tabs;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.PageLayout;
import javafx.print.PageOrientation;
import javafx.print.Paper;
import javafx.print.PrinterJob;
import javafx.scene.control.*;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.controlsfx.glyphfont.FontAwesome;
import tk.tahri.timetable.Entities.Groupe;
import tk.tahri.timetable.dao.Database;
import tk.tahri.timetable.print.GroupeTimeTablePrint;
import tk.tahri.timetable.view.*;

public class GroupeTab extends EditorTab {

    private final FontAwesome fontAwesome = new FontAwesome();
    private final GroupeMainTimeTable groupeTimeTable;
    private final FormateurSubTimetableView formateurTimeTable;
    private final EspaceSubTimetableView espaceTimeTable;
    private final GridPane grid;
    private VBox formateursVBox;
    private VBox espacesVBox;

    public GroupeTab(Groupe groupe) {
        super(groupe.getNom());
        grid = new GridPane();
        setContent(grid);

        ColumnConstraints column1 = new ColumnConstraints();
        column1.setPercentWidth(43);
        ColumnConstraints column2 = new ColumnConstraints();
        column2.setPercentWidth(43);
        ColumnConstraints column3 = new ColumnConstraints();
        column3.setPercentWidth(14);
        grid.getColumnConstraints().addAll(column1, column2, column3);

        RowConstraints rowConstraints1 = new RowConstraints();
        rowConstraints1.setPercentHeight(7);
        RowConstraints rowConstraints2 = new RowConstraints();
        rowConstraints2.setPercentHeight(50);
        RowConstraints rowConstraints3 = new RowConstraints();
        rowConstraints3.setPercentHeight(6);
        RowConstraints rowConstraints4 = new RowConstraints();
        rowConstraints4.setPercentHeight(37);
        grid.getRowConstraints().addAll(rowConstraints1, rowConstraints2, rowConstraints3, rowConstraints4);

        formateurTimeTable = new FormateurSubTimetableView();
        espaceTimeTable = new EspaceSubTimetableView();

        var groupeToolBar = new TitleToolBar();
        groupeToolBar.setIcon(fontAwesome.create(FontAwesome.Glyph.USERS).color(Color.WHITE).size(26));
        groupeToolBar.getStyleClass().add("groupe-title-main");
        groupeTimeTable = new GroupeMainTimeTable(groupe, groupeToolBar, formateurTimeTable, espaceTimeTable);
        grid.add(groupeToolBar, 0, 0, 2, 1);

        drawMainContent();
        drawRigthSidebar();

    }

    private void drawMainContent() {

        var box = new VBox(groupeTimeTable);
        box.setAlignment(Pos.CENTER);
        var box2 = new HBox(box);
        box2.setAlignment(Pos.CENTER);
        grid.add(box2, 0, 1, 2, 1);

        var formateurToolBar = new TitleToolBar();
        formateurToolBar.setTitle("Fromateur :");
        formateurTimeTable.setTitleToolBar(formateurToolBar);
        formateurToolBar.setIcon(fontAwesome.create(FontAwesome.Glyph.USER).color(Color.WHITE).size(18));
        formateurToolBar.getStyleClass().add("formateur-title-sub");
        grid.add(formateurToolBar, 0, 2);

        box = new VBox(formateurTimeTable);
        box.setAlignment(Pos.CENTER);
        box2 = new HBox(box);
        box2.setAlignment(Pos.CENTER);
        grid.add(box2, 0, 3);


        var espaceToolBar = new TitleToolBar();
        espaceToolBar.setTitle("Espace : ");
        espaceTimeTable.setTitleToolBar(espaceToolBar);
        espaceToolBar.setIcon(fontAwesome.create(FontAwesome.Glyph.HOME).color(Color.WHITE).size(18));
        espaceToolBar.getStyleClass().add("espace-title-sub");
        grid.add(espaceToolBar, 1, 2);

        box = new VBox(espaceTimeTable);
        box.setAlignment(Pos.CENTER);
        box2 = new HBox(box);
        box2.setAlignment(Pos.CENTER);
        grid.add(box2, 1, 3);

        grid.widthProperty().addListener((observableValue, number, t1) -> resize());
        grid.heightProperty().addListener((observableValue, number, t1) -> resize());

    }


    private void resize() {

        var scaleX = (grid.getWidth() - grid.getWidth() * 14 / 100) / 1150;
        var scaleY = grid.getHeight() / 750;
        var scale = Math.min(scaleX, scaleY);

        formateurTimeTable.setScaleX(scale);
        formateurTimeTable.setScaleY(scale);

        espaceTimeTable.setScaleX(scale);
        espaceTimeTable.setScaleY(scale);

        groupeTimeTable.setScaleX(scale);
        groupeTimeTable.setScaleY(scale);
    }


    private void drawRigthSidebar() {
        var acc = new VBox();
        // Groupe --------------------
        var formateurs = new TitledPane();
        formateurs.setText("Formateurs");

        var scrolPane = new ScrollPane();
        scrolPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrolPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrolPane.setFitToHeight(true);
        scrolPane.setFitToWidth(true);
        scrolPane.setPadding(new Insets(10));
        formateursVBox = new VBox(5);
        loadFormateurs();
        scrolPane.setContent(formateursVBox);
        formateurs.setContent(scrolPane);

        // Espace --------------------
        var espaces = new TitledPane();
        espaces.setText("Espaces");
        var scrolPane2 = new ScrollPane();
        scrolPane2.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrolPane2.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrolPane2.setFitToHeight(true);
        scrolPane2.setFitToWidth(true);
        scrolPane2.setPadding(new Insets(10));
        espacesVBox = new VBox(5);
        loadEspaces();
        scrolPane2.setContent(espacesVBox);
        espaces.setContent(scrolPane2);

        acc.getChildren().addAll(formateurs, espaces);
        formateurs.setExpanded(true);
        espaces.setExpanded(true);
        //parent.setRight(acc);
        grid.add(acc, 2, 0, 1, 4);

    }

    private void loadEspaces() {
        espacesVBox.getChildren().clear();
        Database.getInstance().getEspaces().forEach(e -> {
            var espaceView = new EspaceView(e);
            espaceView.setOnMousePressed(mouseEvent -> {
                espaceTimeTable.setEspace(espaceView.getEspace());
                espaceTimeTable.loadSeanceViews(false);

                if (formateurTimeTable.getFormateur() != null) {
                    espaceView.setOnDragDetected(event -> {
                        Dragboard db = espaceView.startDragAndDrop(TransferMode.ANY);
                        ClipboardContent content = new ClipboardContent();
                        content.putString("FORMATEUR");
                        db.setContent(content);
                        event.consume();
                    });
                }
            });
            espaceView.setMaxWidth(Double.MAX_VALUE);
            espacesVBox.getChildren().addAll(espaceView);
        });
    }

    private void loadFormateurs() {
        formateursVBox.getChildren().clear();
        Database.getInstance().getFormateurs().forEach(f -> {
            var formateurView = new FormateurView(f);
            formateurView.setOnMousePressed(mouseEvent -> {
                formateurTimeTable.setFormateur(formateurView.getFormateur());
                formateurTimeTable.loadSeanceViews(false);

                if (espaceTimeTable.getEspace() != null) {
                    formateurView.setOnDragDetected(event -> {
                        Dragboard db = formateurView.startDragAndDrop(TransferMode.ANY);
                        ClipboardContent content = new ClipboardContent();
                        content.putString("FORMATEUR");
                        db.setContent(content);
                        event.consume();
                    });
                }
            });
            formateurView.setMaxWidth(Double.MAX_VALUE);
            formateursVBox.getChildren().addAll(formateurView);
        });
    }

    @Override
    protected void selectionChanged() {
        if (this.isSelected()) {
            groupeTimeTable.loadSeanceViews(true);
            if (espaceTimeTable.getEspace() != null)
                espaceTimeTable.loadSeanceViews(false);
            if (formateurTimeTable.getFormateur() != null)
                formateurTimeTable.loadSeanceViews(false);
            loadFormateurs();
            loadEspaces();
        }
    }

    @Override
    public void print() {
        var groupeTT = new GroupeTimeTablePrint(this.groupeTimeTable.getGroupe());
        PrinterJob job = PrinterJob.createPrinterJob();
        PageLayout pageLayout = job.getPrinter().createPageLayout(Paper.A4, PageOrientation.LANDSCAPE, 40, 40, 40, 40);
        job.getJobSettings().setPageLayout(pageLayout);
        groupeTT.setPrefWidth(pageLayout.getPrintableWidth());

        if (job.showPrintDialog(groupeTimeTable.getScene().getWindow())) {
            boolean success = job.printPage(groupeTT);
            if (success) {
                job.endJob();
            }
        }
    }
}
