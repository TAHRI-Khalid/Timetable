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
import tk.tahri.timetable.Entities.Formateur;
import tk.tahri.timetable.dao.Database;
import tk.tahri.timetable.print.FormateurTimeTablePrint;
import tk.tahri.timetable.view.*;

public class FormateurTab extends AbstractTimetableTab {

    private final Formateur formateur;
    private final FormateurMainTimeTable formateurTimeTable;
    private final GroupeSubTimetableView groupeTimeTable;
    private final EspaceSubTimetableView espaceTimeTable;
    private final VBox groupesVBox = new VBox(5);
    private final ComboBox<Espace> espacesComboBox = new ComboBox<>();

    public FormateurTab(Formateur formateur) {
        super(formateur.toString());
        this.formateur = formateur;

        groupeTimeTable = new GroupeSubTimetableView();
        espaceTimeTable = new EspaceSubTimetableView();

        var formateurToolBar = new TitleToolBar();
        formateurToolBar.setIcon(fontAwesome.create(FontAwesome.Glyph.USER).color(Color.WHITE).size(26));
        formateurToolBar.getStyleClass().add("formateur-title-main");
        formateurTimeTable = new FormateurMainTimeTable(formateur, formateurToolBar, groupeTimeTable, espaceTimeTable);

        var groupeToolBar = new TitleToolBar();
        groupeToolBar.setTitle("Groupe : ");
        groupeTimeTable.setTitleToolBar(groupeToolBar);
        groupeToolBar.setIcon(fontAwesome.create(FontAwesome.Glyph.USERS).color(Color.WHITE).size(18));
        groupeToolBar.getStyleClass().add("groupe-title-sub");

        var espaceToolBar = new TitleToolBar();
        espaceToolBar.setTitle("Espace : ");
        espaceTimeTable.setTitleToolBar(espaceToolBar);
        espaceToolBar.setIcon(fontAwesome.create(FontAwesome.Glyph.HOME).color(Color.WHITE).size(18));
        espaceToolBar.getStyleClass().add("espace-title-sub");

        loadGroupes();
        setupEspacesComboBox();
        loadEspaces();

        var sidebar = new VBox(8);
        var paneEspaces = createSidebarPane("Espaces", espacesComboBox);
        var paneGroupes = createSidebarPane("Groupes", groupesVBox);
        VBox.setVgrow(paneGroupes, Priority.ALWAYS);
        sidebar.getChildren().addAll(paneEspaces, paneGroupes);

        setupLayout(formateurTimeTable, formateurToolBar,
                groupeTimeTable, groupeToolBar,
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

    private void loadGroupes() {
        groupesVBox.getChildren().clear();
        Database.getInstance().getGroupes().forEach(g -> {
            var groupeView = new GroupeView(g);
            groupeView.setMaxWidth(Double.MAX_VALUE);
            groupeView.setOnMousePressed(mouseEvent -> {
                groupeTimeTable.setGroupe(groupeView.getGroupe());
                groupeTimeTable.loadSeanceViews(false);
            });
            groupeView.setOnDragDetected(event -> {
                Dragboard db = groupeView.startDragAndDrop(TransferMode.ANY);
                ClipboardContent content = new ClipboardContent();
                content.putString("GROUPE");
                db.setContent(content);
                groupeTimeTable.setGroupe(groupeView.getGroupe());
                groupeTimeTable.loadSeanceViews(false);
                event.consume();
            });
            groupesVBox.getChildren().add(groupeView);
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

    @Override
    protected void selectionChanged() {
        if (this.isSelected()) {
            formateurTimeTable.loadSeanceViews(true);
            if (espaceTimeTable.getEspace() != null) {
                espaceTimeTable.loadSeanceViews(false);
            }
            if (groupeTimeTable.getGroupe() != null) {
                groupeTimeTable.loadSeanceViews(false);
            }
            loadGroupes();
            loadEspaces();
        }
    }

    @Override
    public void print() {
        printTimeTable(new FormateurTimeTablePrint(formateur));
    }

    @Override
    public MainTimeTableView getMainTimeTableView() {
        return formateurTimeTable;
    }

    @Override
    protected SubTimeTableView getSubView1() {
        return groupeTimeTable;
    }

    @Override
    protected SubTimeTableView getSubView2() {
        return espaceTimeTable;
    }
}
