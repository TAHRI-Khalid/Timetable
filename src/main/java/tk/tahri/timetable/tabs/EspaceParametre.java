package tk.tahri.timetable.tabs;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.PageLayout;
import javafx.print.PageOrientation;
import javafx.print.Paper;
import javafx.print.PrinterJob;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.controlsfx.glyphfont.FontAwesome;
import tk.tahri.timetable.Editor;
import tk.tahri.timetable.Entities.Espace;
import tk.tahri.timetable.dao.Database;
import tk.tahri.timetable.print.EspaceTimeTablePrint;
import tk.tahri.timetable.tools.Config;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import tk.tahri.timetable.tools.FormUtils;
import tk.tahri.timetable.tools.NotificationService;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;

public class EspaceParametre extends EditorTab {

    private Espace espace;
    private final ObservableList<Espace> masterData = FXCollections.observableArrayList();
    private final TableView<Espace> table;
    private final TextField txtNom;
    private final ComboBox<String> comboType;
    private final Label lblFormTitle = new Label("Nouvel Espace");
    private final Label lblModeBadge = new Label("Création");
    private final Button btnSave = new Button("Ajouter");
    private final Button btnCancel = new Button("Réinitialiser");
    private final Editor editor;

    public EspaceParametre(Editor editor) {
        super("Espaces");
        this.editor = editor;
        FontAwesome fontAwesome = new FontAwesome();

        Label espaceTitle = new Label("Les Espaces");
        espaceTitle.setGraphic(fontAwesome.create(FontAwesome.Glyph.HOME).color(Color.WHITE).size(26));
        espaceTitle.setGraphicTextGap(10);
        espaceTitle.setMaxWidth(Double.MAX_VALUE);
        espaceTitle.getStyleClass().add("espace-title-main");

        var grid = new GridPane();
        grid.setVgap(12);
        grid.setHgap(15);

        grid.add(new Label("Nom de l'espace : "), 0, 0);
        txtNom = new TextField();
        txtNom.setPromptText("Ex: Salle 101, Amphi A, Atelier Info...");
        FormUtils.bindFieldValidator(txtNom);
        GridPane.setHgrow(txtNom, Priority.ALWAYS);
        grid.add(txtNom, 1, 0);

        grid.add(new Label("Type d'espace : "), 2, 0);
        comboType = new ComboBox<String>();
        GridPane.setHgrow(comboType, Priority.ALWAYS);
        comboType.getItems().addAll(Espace.Types);
        comboType.getSelectionModel().select(0);
        grid.add(comboType, 3, 0);

        // Panneau de formulaire avec en-tête et mode
        var formCard = new VBox(12);
        formCard.getStyleClass().add("form-card");

        var formHeader = new HBox(10);
        formHeader.setAlignment(Pos.CENTER_LEFT);
        formHeader.getStyleClass().add("form-header-box");

        lblFormTitle.getStyleClass().add("form-mode-title");
        lblModeBadge.getStyleClass().add("badge-mode-create");
        formHeader.getChildren().addAll(lblFormTitle, lblModeBadge);

        btnSave.getStyleClass().add("btn-action-primary");
        btnSave.setOnAction(actionEvent -> saveForm());

        btnCancel.getStyleClass().add("btn-action-secondary");
        btnCancel.setOnAction(actionEvent -> clearForm());

        var hBox = new HBox(10, btnSave, btnCancel);
        hBox.setAlignment(Pos.CENTER_RIGHT);

        formCard.getChildren().addAll(formHeader, grid, hBox);

        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        var searchBar = FormUtils.createSearchToolbar(
                table,
                masterData,
                "Rechercher un espace (par nom, type)...",
                (esp, term) -> (esp.getNom() != null && esp.getNom().toLowerCase().contains(term))
                        || (esp.getType() != null && esp.getType().toLowerCase().contains(term))
        );

        var colNom = new TableColumn<Espace, String>("Nom");
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));

        var colType = new TableColumn<Espace, String>("Type");
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));

        var colPrint = new TableColumn<Espace, Long>("");
        colPrint.setCellValueFactory(new PropertyValueFactory<>("id"));
        colPrint.setPrefWidth(48);
        colPrint.setMaxWidth(55);
        colPrint.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Long item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                var btn = new Button();
                btn.setGraphic(fontAwesome.create(FontAwesome.Glyph.PRINT).color(Color.WHITE).size(15));
                btn.getStyleClass().add("btn-print");
                btn.setTooltip(new Tooltip("Imprimer l'emploi du temps"));
                setGraphic(btn);
                btn.setOnAction(event -> printEspaceTimetable(item));
            }
        });

        var colUpdate = new TableColumn<Espace, Long>("");
        colUpdate.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUpdate.setPrefWidth(48);
        colUpdate.setMaxWidth(55);
        colUpdate.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Long item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                var btn = new Button();
                btn.setGraphic(fontAwesome.create(FontAwesome.Glyph.PENCIL).color(Color.WHITE).size(15));
                btn.getStyleClass().add("btn-save");
                btn.setTooltip(new Tooltip("Modifier cet espace"));
                setGraphic(btn);
                btn.setOnAction(event -> updateForm(item));
            }
        });

        var colDelete = new TableColumn<Espace, Long>("");
        colDelete.setCellValueFactory(new PropertyValueFactory<>("id"));
        colDelete.setPrefWidth(48);
        colDelete.setMaxWidth(55);
        colDelete.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Long item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                var btn = new Button();
                btn.setGraphic(fontAwesome.create(FontAwesome.Glyph.TRASH).color(Color.WHITE).size(15));
                btn.getStyleClass().add("btn-delete");
                btn.setTooltip(new Tooltip("Supprimer cet espace"));
                setGraphic(btn);
                btn.setOnAction(event -> deleteItem(item));
            }
        });

        table.getColumns().addAll(colNom, colType, colPrint, colUpdate, colDelete);
        loadData();

        var vBox = new VBox(15);
        vBox.setPadding(new Insets(15));
        vBox.getChildren().addAll(espaceTitle, formCard, searchBar, table);
        setContent(vBox);

    }

    private void printEspaceTimetable(Long item) {
        var opt = masterData.stream().filter(g -> Objects.equals(g.getId(), item)).findFirst();
        if (opt.isEmpty()) return;
        var groupeTT = new EspaceTimeTablePrint(opt.get());
        PrinterJob job = PrinterJob.createPrinterJob();
        PageLayout pageLayout = job.getPrinter().createPageLayout(Paper.A4, PageOrientation.LANDSCAPE, 40, 40, 40, 40);
        job.getJobSettings().setPageLayout(pageLayout);
        groupeTT.setPrefWidth(pageLayout.getPrintableWidth());

        if (job.showPrintDialog(table.getScene().getWindow())) {
            boolean success = job.printPage(groupeTT);
            if (success) {
                job.endJob();
            }
        }
    }

    private void loadData() {
        masterData.setAll(Database.getInstance().getEspaces());
        editor.loadMenuEspace();
    }

    private void clearForm() {
        txtNom.clear();
        FormUtils.setFieldError(txtNom, false);
        espace = null;
        lblFormTitle.setText("Nouvel Espace");
        lblModeBadge.setText("Création");
        lblModeBadge.getStyleClass().setAll("badge-mode-create");
        btnSave.setText("Ajouter");
    }

    private void saveForm() {
        var nom = txtNom.getText().trim();
        if (nom.isEmpty()) {
            FormUtils.setFieldError(txtNom, true);
            NotificationService.showWarning("Champ obligatoire", "Veuillez saisir un nom pour l'espace.");
            return;
        }

        boolean isNew = (espace == null);
        if (isNew) {
            espace = new Espace(nom, comboType.getSelectionModel().getSelectedItem());
            Database.getInstance().execute("INSERT INTO espace VALUES(null,?,?)", espace.getNom(), espace.getType());
            NotificationService.showSuccess("Espace créé", "L'espace '" + nom + "' a été ajouté avec succès.");
        } else {
            espace = new Espace(espace.getId(), nom, comboType.getSelectionModel().getSelectedItem());
            Database.getInstance().execute("UPDATE espace SET nom=? , type=? WHERE id=?",
                    espace.getNom(), espace.getType(), String.valueOf(espace.getId()));
            NotificationService.showSuccess("Espace modifié", "L'espace '" + nom + "' a été mis à jour avec succès.");
        }
        loadData();
        clearForm();
    }

    private void deleteItem(long id) {
        var target = masterData.stream().filter(e -> e.getId() == id).findFirst().orElse(null);
        String nomEspace = (target != null) ? target.getNom() : "cet espace";

        if (Database.getInstance().isEspaceHasSeances(id)) {
            NotificationService.showError("Suppression impossible",
                    "Impossible de supprimer l'espace '" + nomEspace + "' car il est utilisé dans des séances planifiées.");
            return;
        }

        boolean confirm = NotificationService.confirmDestruction(
                "Confirmation de suppression",
                "Supprimer l'espace",
                "Voulez-vous vraiment supprimer définitivement l'espace '" + nomEspace + "' ?"
        );

        if (confirm) {
            Database.getInstance().execute("DELETE FROM espace WHERE id= ?", String.valueOf(id));
            loadData();
            NotificationService.showSuccess("Espace supprimé", "L'espace '" + nomEspace + "' a été supprimé avec succès.");
        }
    }

    private void updateForm(long id) {
        masterData.stream()
                .filter(e -> e.getId() == id)
                .findFirst().ifPresent(e -> {
                    espace = e;
                    txtNom.setText(e.getNom());
                    FormUtils.setFieldError(txtNom, false);
                    comboType.getSelectionModel().select(e.getType());
                    lblFormTitle.setText("Modifier l'Espace : " + e.getNom());
                    lblModeBadge.setText("Modification");
                    lblModeBadge.getStyleClass().setAll("badge-mode-edit");
                    btnSave.setText("Mettre à jour");
                    txtNom.requestFocus();
                });
    }

    @Override
    protected void selectionChanged() {

    }

    @Override
    public void print() {

    }
}
