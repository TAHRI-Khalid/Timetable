package tk.tahri.timetable.tabs;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.controlsfx.glyphfont.FontAwesome;
import tk.tahri.timetable.Entities.Formateur;
import tk.tahri.timetable.dao.Database;
import tk.tahri.timetable.view.*;

public class FormateurTab extends EditorTab {

    private final BorderPane parent = new BorderPane();
    private final FontAwesome fontAwesome = new FontAwesome();
    private final FormateurMainTimeTable formateurTimeTable;
    private final GroupeSubTimetableView groupeTimeTable;
    private final EspaceSubTimetableView espaceTimeTable;

    public FormateurTab(Formateur formateur) {
        super(formateur.toString());

        groupeTimeTable = new GroupeSubTimetableView();
        espaceTimeTable = new EspaceSubTimetableView();
        formateurTimeTable = new FormateurMainTimeTable(formateur, groupeTimeTable, espaceTimeTable);

        drawMainContent();
        drawRigthSidebar();
    }

    private void drawMainContent() {

        var grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);

        grid.setAlignment(Pos.CENTER);

        Label formateurTitle = new Label("Fromateur : " + formateurTimeTable.getFormateur());
        formateurTitle.setGraphic(fontAwesome.create(FontAwesome.Glyph.USER).color(Color.WHITE).size(26));
        formateurTitle.setMaxWidth(Double.MAX_VALUE);
        formateurTitle.getStyleClass().add("formateur-title-main");
        grid.add(formateurTitle, 0, 0, 2, 1);
        grid.add(formateurTimeTable, 0, 1, 2, 1);

        Label groupeTitle = new Label("Groupe : ");
        groupeTimeTable.setTitre(groupeTitle);
        groupeTitle.setGraphic(fontAwesome.create(FontAwesome.Glyph.USERS).color(Color.WHITE).size(18));
        groupeTitle.setMaxWidth(Double.MAX_VALUE);
        groupeTitle.getStyleClass().add("groupe-title-sub");
        grid.add(groupeTitle, 0, 2);
        grid.add(groupeTimeTable, 0, 3);

        Label espaceTitle = new Label("Espace : ");
        espaceTimeTable.setTitre(espaceTitle);
        espaceTitle.setGraphic(fontAwesome.create(FontAwesome.Glyph.HOME).color(Color.WHITE).size(18));
        espaceTitle.setMaxWidth(Double.MAX_VALUE);
        espaceTitle.getStyleClass().add("espace-title-sub");
        grid.add(espaceTitle, 1, 2);
        grid.add(espaceTimeTable, 1, 3);

        parent.setCenter(grid);
        setContent(parent);

    }

    private void drawRigthSidebar() {
        var acc = new VBox();
        acc.setPrefWidth(250);
        // Groupe --------------------
        var formateurs = new TitledPane();
        formateurs.setText("Groupes");

        var scrolPane = new ScrollPane();
        scrolPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrolPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrolPane.setFitToHeight(true);
        scrolPane.setFitToWidth(true);
        scrolPane.setPadding(new Insets(10));
        var vFrm = new VBox(5);
        Database.getInstance().getGroupes().forEach(g -> {
            var groupeView = new GroupeView(g);
            groupeView.setOnMousePressed(mouseEvent -> {
                groupeTimeTable.setGroupe(groupeView.getGroupe());
                groupeTimeTable.loadSeanceViews(false);

                if (espaceTimeTable.getEspace() != null) {
                    groupeView.setOnDragDetected(event -> {
                        Dragboard db = groupeView.startDragAndDrop(TransferMode.ANY);
                        ClipboardContent content = new ClipboardContent();
                        content.putString("GROUPE");
                        db.setContent(content);
                        event.consume();
                    });
                }
            });
            groupeView.setMaxWidth(Double.MAX_VALUE);
            vFrm.getChildren().addAll(groupeView);
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

                if (groupeTimeTable.getGroupe() != null) {
                    espaceView.setOnDragDetected(event -> {
                        Dragboard db = espaceView.startDragAndDrop(TransferMode.ANY);
                        ClipboardContent content = new ClipboardContent();
                        content.putString("ESPACE");
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
        if(this.isSelected()){
           formateurTimeTable.loadSeanceViews(true);
        }
    }

    @Override
    public void print() {
        /*
        var groupeTT = new GroupeTimeTablePrint(this.formateurTimeTable.getGroupe());
        PrinterJob job = PrinterJob.createPrinterJob();
        PageLayout pageLayout = job.getPrinter().createPageLayout(Paper.A4, PageOrientation.LANDSCAPE, 40, 40, 40, 40);
        job.getJobSettings().setPageLayout(pageLayout);
        groupeTT.setPrefWidth(pageLayout.getPrintableWidth()-40);

        if (job.showPrintDialog(formateurTimeTable.getScene().getWindow())) {
            boolean success = job.printPage(groupeTT);
            if (success) {
                job.endJob();
            }
        }

         */
    }

}
