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
import tk.tahri.timetable.Entities.Formateur;
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

        var stack = new StackPane(grid);
        stack.setAlignment(Pos.CENTER);
        parent.setCenter(stack);
        setContent(parent);
    }

    private void drawRigthSidebar() {
        var acc = new VBox();
        acc.setPrefWidth(250);
        // Groupe --------------------
        var formateurs = new TitledPane();
        var btnAdd = new Button();
        btnAdd.setOnMouseClicked(actionEvent -> {
            ContextMenu contextMenu = new ContextMenu();

            contextMenu.getItems().clear();
            var list = Database.getInstance().getFormateurs();
            list.stream().map(Formateur::getType).distinct().forEach(type -> {
                var menu = new Menu(type);
                contextMenu.getItems().add(menu);
                list.stream().filter(groupe -> groupe.getType().equals(type)).forEach(formateur -> {
                    var submenu = new MenuItem(formateur.toString());
                    menu.getItems().add(submenu);
                });
            });
            contextMenu.show(btnAdd, actionEvent.getScreenX() - 20, actionEvent.getScreenY());
        });
        btnAdd.setPadding(new Insets(2));
        btnAdd.setGraphic(fontAwesome.create(FontAwesome.Glyph.PLUS_CIRCLE).size(13));
        var lblTitle = new Label("Formateurs ");

        var header = new BorderPane();
        header.setLeft(lblTitle);
        header.setRight(btnAdd);
        header.prefWidthProperty().bind(formateurs.widthProperty().subtract(25));
        formateurs.setGraphic(header);
        formateurs.setExpanded(true);
        formateurs.setCollapsible(false);

        var scrolPane = new ScrollPane();
        scrolPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrolPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrolPane.setFitToHeight(true);
        scrolPane.setFitToWidth(true);
        scrolPane.setPadding(new Insets(10));
        var vFrm = new VBox(5);
        Database.getInstance().getFormateurs().forEach(f -> {
            var lbl = new FormateurView(f);
            lbl.setOnMousePressed(mouseEvent -> {
                formateurTitle.setText("Fromateur : " + lbl.getFormateur());
                formateurTimeTable.setFormateur(lbl.getFormateur());
                formateurTimeTable.loadSeances(false);

                if (espaceTimeTable.getEspace() != null) {
                    lbl.setOnDragDetected(event -> {
                        Dragboard db = lbl.startDragAndDrop(TransferMode.ANY);
                        ClipboardContent content = new ClipboardContent();
                        content.putString("FORMATEUR");
                        db.setContent(content);
                        event.consume();
                    });
                }

            });
            lbl.setMaxWidth(Double.MAX_VALUE);

            vFrm.getChildren().addAll(lbl);
        });
        scrolPane.setContent(vFrm);
        formateurs.setContent(scrolPane);

        // Espace --------------------
        var espaces = new TitledPane();
        espaces.setText("Espaces");
        espaces.setCollapsible(false);
        var scrolPane2 = new ScrollPane();
        scrolPane2.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrolPane2.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrolPane2.setFitToHeight(true);
        scrolPane2.setFitToWidth(true);
        scrolPane2.setPadding(new Insets(10));
        var vEsp = new VBox(5);
        Database.getInstance().getEspaces().forEach(e -> {
            var lbl = new EspaceView(e);
            lbl.setOnMousePressed(mouseEvent -> {
                espaceTitle.setText("Espace : " + lbl.getEspace());
                espaceTimeTable.setEspace(lbl.getEspace());
                espaceTimeTable.loadSeances(false);

                if (formateurTimeTable.getFormateur() != null) {
                    lbl.setOnDragDetected(event -> {
                        Dragboard db = lbl.startDragAndDrop(TransferMode.ANY);
                        ClipboardContent content = new ClipboardContent();
                        content.putString("FORMATEUR");
                        db.setContent(content);
                        event.consume();
                    });
                }
            });
            lbl.setMaxWidth(Double.MAX_VALUE);
            vEsp.getChildren().addAll(lbl);
        });
        scrolPane2.setContent(vEsp);
        espaces.setContent(scrolPane2);

        acc.getChildren().addAll(formateurs, espaces);
        formateurs.setExpanded(true);
        espaces.setExpanded(true);
        parent.setRight(acc);
    }

}
