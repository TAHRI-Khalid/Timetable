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
import javafx.scene.transform.Scale;
import org.controlsfx.glyphfont.FontAwesome;
import tk.tahri.timetable.Entities.Groupe;
import tk.tahri.timetable.dao.Database;
import tk.tahri.timetable.print.GroupeTimeTablePrint;
import tk.tahri.timetable.view.*;

public class GroupeTab extends EditorTab {

    private final BorderPane parent = new BorderPane();
    private final FontAwesome fontAwesome = new FontAwesome();
    private final GroupeMainTimeTable groupeTimeTable;
    private final FormateurSubTimetableView formateurTimeTable;
    private final EspaceSubTimetableView espaceTimeTable;

    public GroupeTab(Groupe groupe) {
        super(groupe.getNom());

        formateurTimeTable = new FormateurSubTimetableView();
        espaceTimeTable = new EspaceSubTimetableView();
        groupeTimeTable = new GroupeMainTimeTable(groupe, formateurTimeTable, espaceTimeTable);

        drawMainContent();
        drawRigthSidebar();
    }

    private void drawMainContent() {
        var stack = new StackPane();
        stack.setPadding(new Insets(10));
        var grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);

        Label groupeTitle = new Label("Groupe : " + groupeTimeTable.getGroupe().getNom());
        groupeTitle.setGraphic(fontAwesome.create(FontAwesome.Glyph.USERS).color(Color.WHITE).size(22));
        groupeTitle.setMaxWidth(Double.MAX_VALUE);
        groupeTitle.getStyleClass().add("groupe-title-main");
        grid.add(groupeTitle, 0, 0, 2, 1);
        grid.add(groupeTimeTable, 0, 1, 2, 1);

        Label formateurTitle = new Label("Fromateur : ");
        formateurTimeTable.setTitre(formateurTitle);
        formateurTitle.setGraphic(fontAwesome.create(FontAwesome.Glyph.USER).color(Color.WHITE).size(18));
        formateurTitle.setMaxWidth(Double.MAX_VALUE);
        formateurTitle.getStyleClass().add("formateur-title-sub");
        grid.add(formateurTitle, 0, 2);
        grid.add(formateurTimeTable, 0, 3);

        Label espaceTitle = new Label("Espace : ");
        espaceTimeTable.setTitre(espaceTitle);
        espaceTitle.setGraphic(fontAwesome.create(FontAwesome.Glyph.HOME).color(Color.WHITE).size(18));
        espaceTitle.setMaxWidth(Double.MAX_VALUE);
        espaceTitle.getStyleClass().add("espace-title-sub");
        grid.add(espaceTitle, 1, 2);
        grid.add(espaceTimeTable, 1, 3);

        stack.getChildren().add(grid);
        parent.setCenter(stack);
        setContent(parent);

        grid.widthProperty().addListener((observableValue, number, t1) -> {
            grid.getTransforms().clear();
            var scaleX =  stack.getWidth()/1110;
            var scaleY = stack.getHeight()/700;
            var scale = Math.min(scaleX, scaleY);
            grid.getTransforms().add(new Scale(scale, scale));

        });

        grid.heightProperty().addListener((observableValue, number, t1) -> {
            grid.getTransforms().clear();
            var scaleX =  stack.getWidth()/1120;
            var scaleY = stack.getHeight()/720;
            var scale = Math.min(scaleX, scaleY);
            grid.getTransforms().add(new Scale(scale, scale));
        });

    }

    private void drawRigthSidebar() {
        var acc = new VBox();
        acc.setPrefWidth(250);
        // Groupe --------------------
        var formateurs = new TitledPane();
        formateurs.setText("Formateurs");

        var scrolPane = new ScrollPane();
        scrolPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrolPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrolPane.setFitToHeight(true);
        scrolPane.setFitToWidth(true);
        scrolPane.setPadding(new Insets(10));
        var vFrm = new VBox(5);
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
            vFrm.getChildren().addAll(formateurView);
        });
        scrolPane.setContent(vFrm);
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
        var vEsp = new VBox(5);
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
            vEsp.getChildren().addAll(espaceView);
        });
        scrolPane2.setContent(vEsp);
        espaces.setContent(scrolPane2);

        acc.getChildren().addAll(formateurs, espaces);
        formateurs.setExpanded(true);
        espaces.setExpanded(true);
        parent.setRight(acc);
    }

    @Override
    protected void selectionChanged() {
        if (this.isSelected()) {
            groupeTimeTable.loadSeanceViews(true);
        }
    }

    @Override
    public void print() {
        var groupeTT = new GroupeTimeTablePrint(this.groupeTimeTable.getGroupe());
        PrinterJob job = PrinterJob.createPrinterJob();
        PageLayout pageLayout = job.getPrinter().createPageLayout(Paper.A4, PageOrientation.LANDSCAPE, 40, 40, 40, 40);
        job.getJobSettings().setPageLayout(pageLayout);
        groupeTT.setPrefWidth(pageLayout.getPrintableWidth() - 40);

        if (job.showPrintDialog(groupeTimeTable.getScene().getWindow())) {
            boolean success = job.printPage(groupeTT);
            if (success) {
                job.endJob();
            }
        }
    }

}
