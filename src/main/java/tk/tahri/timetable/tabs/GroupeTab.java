package tk.tahri.timetable.tabs;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.controlsfx.glyphfont.FontAwesome;
import tk.tahri.timetable.Entities.Groupe;
import tk.tahri.timetable.dao.Database;
import tk.tahri.timetable.view.*;

public class GroupeTab extends Tab {

    private final BorderPane parent = new BorderPane();
    private final FontAwesome fontAwesome = new FontAwesome();
    private final GroupeMainTimeTable groupeTimeTable;
    private final FormateurSubTimetableView formateurTimeTable;
    private final EspaceSubTimetableView espaceTimeTable;
    private Label groupeTitle;
    private Label formateurTitle;
    private Label espaceTitle;

    public GroupeTab(Groupe groupe) {
        super(groupe.getNom());

        formateurTimeTable = new FormateurSubTimetableView();
        espaceTimeTable = new EspaceSubTimetableView();
        groupeTimeTable = new GroupeMainTimeTable(groupe, formateurTimeTable, espaceTimeTable);

        drawMainContent();
        drawRigthSidebar();
    }

    private void drawMainContent() {

        var grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);

        grid.setAlignment(Pos.CENTER);

        groupeTitle = new Label("Groupe : " + groupeTimeTable.getGroupe().getNom());
        groupeTitle.setGraphic(fontAwesome.create(FontAwesome.Glyph.USERS).color(Color.WHITE).size(26));
        groupeTitle.setMaxWidth(Double.MAX_VALUE);
        groupeTitle.getStyleClass().add("groupe-title-main");
        grid.add(groupeTitle, 0, 0, 2, 1);
        grid.add(groupeTimeTable, 0, 1, 2, 1);

        formateurTitle = new Label("Fromateur : ");
        formateurTitle.setGraphic(fontAwesome.create(FontAwesome.Glyph.USER).color(Color.WHITE).size(18));
        formateurTitle.setMaxWidth(Double.MAX_VALUE);
        formateurTitle.getStyleClass().add("formateur-title-sub");
        grid.add(formateurTitle, 0, 2);
        grid.add(formateurTimeTable, 0, 3);

        espaceTitle = new Label("Espace : ");
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
                formateurTitle.setText("Fromateur : " + formateurView.getFormateur());
                formateurTimeTable.setFormateur(formateurView.getFormateur());
                formateurTimeTable.loadSeanceViews(false, "formateur-seance-view");

                if (espaceTimeTable.getEspace() != null) {
                    formateurView.setOnDragDetected(event -> {
                        Dragboard db = formateurView.startDragAndDrop(TransferMode.ANY);
                        ClipboardContent content = new ClipboardContent();
                        content.putString("FORMATEUR");
                        db.setContent(content);
                        event.consume();
                    });
                }
                vFrm.getChildren().forEach(node -> {
                    ((FormateurView) node).setSelectedStyle(false);
                });
                formateurView.setSelectedStyle(true);

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
                espaceTitle.setText("Espace : " + espaceView.getEspace());
                espaceTimeTable.setEspace(espaceView.getEspace());
                espaceTimeTable.loadSeanceViews(false, "espace-seance-view");

                if (formateurTimeTable.getFormateur() != null) {
                    espaceView.setOnDragDetected(event -> {
                        Dragboard db = espaceView.startDragAndDrop(TransferMode.ANY);
                        ClipboardContent content = new ClipboardContent();
                        content.putString("FORMATEUR");
                        db.setContent(content);
                        event.consume();
                    });
                }

                vEsp.getChildren().forEach(node -> {
                    ((EspaceView) node).setSelectedStyle(false);
                });
                espaceView.setSelectedStyle(true);
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

}
