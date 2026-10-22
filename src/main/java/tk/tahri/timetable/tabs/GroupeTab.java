package tk.tahri.timetable.tabs;

import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.controlsfx.glyphfont.FontAwesome;
import tk.tahri.timetable.Entities.Espace;
import tk.tahri.timetable.Entities.Groupe;
import tk.tahri.timetable.dao.Database;
import tk.tahri.timetable.print.GroupeTimeTablePrint;
import tk.tahri.timetable.view.*;

public class GroupeTab extends AbstractTimetableTab {

    private final Groupe groupe;
    private final GroupeMainTimeTable groupeTimeTable;
    private final FormateurSubTimetableView formateurTimeTable;
    private final EspaceSubTimetableView espaceTimeTable;
    private final VBox formateursVBox = new VBox(5);
    private final ComboBox<Espace> espacesComboBox = new ComboBox<>();

    public GroupeTab(Groupe groupe) {
        super(groupe.getNom());
        this.groupe = groupe;

        formateurTimeTable = new FormateurSubTimetableView();
        espaceTimeTable = new EspaceSubTimetableView();

        var groupeToolBar = new TitleToolBar();
        groupeToolBar.setIcon(fontAwesome.create(FontAwesome.Glyph.USERS).color(Color.WHITE).size(26));
        groupeToolBar.getStyleClass().add("groupe-title-main");
        groupeTimeTable = new GroupeMainTimeTable(groupe, groupeToolBar, formateurTimeTable, espaceTimeTable);

        var formateurToolBar = new TitleToolBar();
        formateurToolBar.setTitle("Formateur : ");
        formateurTimeTable.setTitleToolBar(formateurToolBar);
        formateurToolBar.setIcon(fontAwesome.create(FontAwesome.Glyph.USER).color(Color.WHITE).size(18));
        formateurToolBar.getStyleClass().add("formateur-title-sub");

        var espaceToolBar = new TitleToolBar();
        espaceToolBar.setTitle("Espace : ");
        espaceTimeTable.setTitleToolBar(espaceToolBar);
        espaceToolBar.setIcon(fontAwesome.create(FontAwesome.Glyph.HOME).color(Color.WHITE).size(18));
        espaceToolBar.getStyleClass().add("espace-title-sub");

        loadFormateurs();
        setupEspacesComboBox();
        loadEspaces();

        var sidebar = new VBox(8);
        var paneEspaces = createSidebarPane("Espaces", espacesComboBox);
        var paneFormateurs = createSidebarPane("Formateurs", formateursVBox);
        VBox.setVgrow(paneFormateurs, Priority.ALWAYS);
        sidebar.getChildren().addAll(paneEspaces, paneFormateurs);

        setupLayout(groupeTimeTable, groupeToolBar,
                formateurTimeTable, formateurToolBar,
                espaceTimeTable, espaceToolBar,
                sidebar);
    }

    private void setupEspacesComboBox() {
        espacesComboBox.getStyleClass().add("espaces-combo-box");
        espacesComboBox.setMaxWidth(Double.MAX_VALUE);
        espacesComboBox.setPromptText("Sélectionner un espace...");

        espacesComboBox.setCellFactory(lv -> new ListCell<>() {
            private final FontAwesome fontAwesome = new FontAwesome();
            private final org.controlsfx.glyphfont.Glyph icon = fontAwesome.create(FontAwesome.Glyph.HOME).size(14).color(Color.web("#475569"));

            @Override
            protected void updateItem(Espace espace, boolean empty) {
                super.updateItem(espace, empty);
                if (empty || espace == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(espace.getNom() + (espace.getType() != null && !espace.getType().isEmpty() ? " (" + espace.getType() + ")" : ""));
                    setGraphic(icon);
                }
            }
        });

        espacesComboBox.setButtonCell(new ListCell<>() {
            private final FontAwesome fontAwesome = new FontAwesome();
            private final org.controlsfx.glyphfont.Glyph icon = fontAwesome.create(FontAwesome.Glyph.HOME).size(14).color(Color.web("#475569"));

            @Override
            protected void updateItem(Espace espace, boolean empty) {
                super.updateItem(espace, empty);
                if (empty || espace == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(espace.getNom() + (espace.getType() != null && !espace.getType().isEmpty() ? " (" + espace.getType() + ")" : ""));
                    setGraphic(icon);
                }
            }
        });

        espacesComboBox.setOnDragDetected(event -> {
            var selected = espacesComboBox.getSelectionModel().getSelectedItem();
            if (selected != null) {
                Dragboard db = espacesComboBox.startDragAndDrop(TransferMode.ANY);
                ClipboardContent content = new ClipboardContent();
                content.putString("ESPACE");
                db.setContent(content);
                espaceTimeTable.setEspace(selected);
                espaceTimeTable.loadSeanceViews(false);
                event.consume();
            }
        });

        espacesComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                espaceTimeTable.setEspace(newVal);
                espaceTimeTable.loadSeanceViews(false);
            }
        });

        espaceTimeTable.espaceProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                var current = espacesComboBox.getSelectionModel().getSelectedItem();
                if (current == null || current.getId() != newVal.getId()) {
                    for (Espace e : espacesComboBox.getItems()) {
                        if (e.getId() == newVal.getId()) {
                            espacesComboBox.getSelectionModel().select(e);
                            break;
                        }
                    }
                }
            }
        });
    }

    private void loadEspaces() {
        var currentSelected = espacesComboBox.getSelectionModel().getSelectedItem();
        if (currentSelected == null) {
            currentSelected = espaceTimeTable.getEspace();
        }
        var espaces = Database.getInstance().getEspaces();
        espacesComboBox.getItems().setAll(espaces);

        if (currentSelected != null) {
            for (Espace e : espaces) {
                if (e.getId() == currentSelected.getId()) {
                    espacesComboBox.getSelectionModel().select(e);
                    return;
                }
            }
        }
        if (!espaces.isEmpty()) {
            espacesComboBox.getSelectionModel().select(0);
        }
    }

    private void loadFormateurs() {
        formateursVBox.getChildren().clear();
        var formateurs = Database.getInstance().getFormateursByGroupe(groupe.getId());
        if (formateurs.isEmpty()) {
            var emptyBox = new VBox(6);
            emptyBox.setAlignment(javafx.geometry.Pos.CENTER);
            emptyBox.setPadding(new javafx.geometry.Insets(18, 10, 18, 10));
            emptyBox.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 6px; -fx-border-color: #e2e8f0; -fx-border-radius: 6px;");

            var lblNone = new javafx.scene.control.Label("Aucun formateur affecté");
            lblNone.setStyle("-fx-text-fill: #64748b; -fx-font-weight: bold; -fx-font-size: 12px;");

            var lblHint = new javafx.scene.control.Label("Ce groupe n'a aucun formateur attribué.");
            lblHint.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px; -fx-text-alignment: center;");
            lblHint.setWrapText(true);

            var btnManage = new javafx.scene.control.Button("Affecter des formateurs");
            btnManage.getStyleClass().add("btn-action-primary");
            btnManage.setStyle("-fx-font-size: 11px; -fx-padding: 4 10 4 10;");
            btnManage.setOnAction(e -> {
                var window = (getTabPane() != null && getTabPane().getScene() != null) ? getTabPane().getScene().getWindow() : null;
                new GroupeFormateursDialog(groupe, window).showAndWait();
                loadFormateurs();
                var refreshed = Database.getInstance().getFormateursByGroupe(groupe.getId());
                if (!refreshed.isEmpty() && (formateurTimeTable.getFormateur() == null || !refreshed.stream().anyMatch(f -> f.getId() == formateurTimeTable.getFormateur().getId()))) {
                    formateurTimeTable.setFormateur(refreshed.get(0));
                    formateurTimeTable.loadSeanceViews(false);
                }
            });

            emptyBox.getChildren().addAll(lblNone, lblHint, btnManage);
            formateursVBox.getChildren().add(emptyBox);
            return;
        }

        formateurs.forEach(f -> {
            var formateurView = new FormateurView(f);
            formateurView.setMaxWidth(Double.MAX_VALUE);
            formateurView.setOnMousePressed(mouseEvent -> {
                formateurTimeTable.setFormateur(formateurView.getFormateur());
                formateurTimeTable.loadSeanceViews(false);
            });
            formateurView.setOnDragDetected(event -> {
                Dragboard db = formateurView.startDragAndDrop(TransferMode.ANY);
                ClipboardContent content = new ClipboardContent();
                content.putString("FORMATEUR");
                db.setContent(content);
                formateurTimeTable.setFormateur(formateurView.getFormateur());
                formateurTimeTable.loadSeanceViews(false);
                event.consume();
            });
            formateursVBox.getChildren().add(formateurView);
        });
    }

    @Override
    protected void selectionChanged() {
        if (this.isSelected()) {
            groupeTimeTable.loadSeanceViews(true);
            if (espaceTimeTable.getEspace() != null) {
                espaceTimeTable.loadSeanceViews(false);
            }
            if (formateurTimeTable.getFormateur() != null) {
                formateurTimeTable.loadSeanceViews(false);
            }
            loadFormateurs();
            loadEspaces();
        }
    }

    @Override
    public void print() {
        printTimeTable(new GroupeTimeTablePrint(groupe));
    }

    @Override
    public MainTimeTableView getMainTimeTableView() {
        return groupeTimeTable;
    }

    @Override
    protected SubTimeTableView getSubView1() {
        return formateurTimeTable;
    }

    @Override
    protected SubTimeTableView getSubView2() {
        return espaceTimeTable;
    }
}
