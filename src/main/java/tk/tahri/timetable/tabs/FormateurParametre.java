package tk.tahri.timetable.tabs;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.PageLayout;
import javafx.print.PageOrientation;
import javafx.print.Paper;
import javafx.print.PrinterJob;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.controlsfx.glyphfont.FontAwesome;
import tk.tahri.timetable.Editor;
import tk.tahri.timetable.Entities.Formateur;
import tk.tahri.timetable.dao.Database;
import tk.tahri.timetable.print.FormateurTimeTablePrint;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import tk.tahri.timetable.tools.FormUtils;
import tk.tahri.timetable.tools.NotificationService;

import java.util.Objects;

public class FormateurParametre extends Tab {

    private Formateur formateur;
    private final ObservableList<Formateur> masterData = FXCollections.observableArrayList();
    private java.util.Map<Long, Integer> groupeCounts = new java.util.HashMap<>();
    private final TableView<Formateur> table;
    private final TextField txtNom;
    private final TextField txtPrenom;
    private final TextField txtMatricule;
    private final ComboBox<String> comboType;
    private final Label lblFormTitle = new Label("Nouveau Formateur");
    private final Label lblModeBadge = new Label("Création");
    private final Button btnSave = new Button("Ajouter");
    private final Button btnCancel = new Button("Réinitialiser");
    private final Editor editor;


    public FormateurParametre(Editor editor) {
        super("Formateurs");
        this.editor = editor;
        FontAwesome fontAwesome = new FontAwesome();

        Label formateurTitle = new Label("Les Formateurs");
        formateurTitle.setGraphic(fontAwesome.create(FontAwesome.Glyph.USER).color(Color.WHITE).size(26));
        formateurTitle.setGraphicTextGap(10);
        formateurTitle.setMaxWidth(Double.MAX_VALUE);
        formateurTitle.getStyleClass().add("formateur-title-main");

        var grid = new GridPane();
        grid.setVgap(12);
        grid.setHgap(15);

        grid.add(new Label("Nom : "), 0, 0);
        txtNom = new TextField();
        txtNom.setPromptText("Nom de famille");
        FormUtils.bindFieldValidator(txtNom);
        GridPane.setHgrow(txtNom, Priority.ALWAYS);
        grid.add(txtNom, 1, 0);

        grid.add(new Label("Prénom : "), 2, 0);
        txtPrenom = new TextField();
        txtPrenom.setPromptText("Prénom");
        GridPane.setHgrow(txtPrenom, Priority.ALWAYS);
        grid.add(txtPrenom, 3, 0);

        grid.add(new Label("Matricule : "), 0, 1);
        txtMatricule = new TextField();
        txtMatricule.setPromptText("Ex: MAT-001");
        GridPane.setHgrow(txtMatricule, Priority.ALWAYS);
        grid.add(txtMatricule, 1, 1);

        grid.add(new Label("Statut / Type : "), 2, 1);
        comboType = new ComboBox<>();
        comboType.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(comboType, Priority.ALWAYS);
        comboType.getItems().addAll(Formateur.Types);
        comboType.getSelectionModel().select(0);
        grid.add(comboType, 3, 1);

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
                "Rechercher un formateur (nom, prénom, matricule, statut)...",
                (f, term) -> (f.getNom() != null && f.getNom().toLowerCase().contains(term))
                        || (f.getPrenom() != null && f.getPrenom().toLowerCase().contains(term))
                        || (f.getMatricule() != null && f.getMatricule().toLowerCase().contains(term))
                        || (f.getType() != null && f.getType().toLowerCase().contains(term))
        );

        var colNom = new TableColumn<Formateur, String>("Nom");
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));

        var colPrenom = new TableColumn<Formateur, String>("Prénom");
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));

        var colMatricule = new TableColumn<Formateur, String>("Matricule");
        colMatricule.setCellValueFactory(new PropertyValueFactory<>("matricule"));

        var colType = new TableColumn<Formateur, String>("Type");
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));

        var colNbGroupes = new TableColumn<Formateur, Number>("Groupes affectés");
        colNbGroupes.setCellValueFactory(cellData -> {
            long id = cellData.getValue().getId();
            return new javafx.beans.property.SimpleIntegerProperty(groupeCounts.getOrDefault(id, 0));
        });
        colNbGroupes.setPrefWidth(130);
        colNbGroupes.setCellFactory(col -> new TableCell<>() {
            private final Label badge = new Label();
            {
                setAlignment(Pos.CENTER);
            }
            @Override
            protected void updateItem(Number count, boolean empty) {
                super.updateItem(count, empty);
                if (empty || count == null) {
                    setGraphic(null);
                } else {
                    int val = count.intValue();
                    badge.setText(val + (val > 1 ? " groupes" : " groupe"));
                    if (val == 0) {
                        badge.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #94a3b8; -fx-padding: 3 10 3 10; -fx-background-radius: 12px; -fx-font-size: 11px; -fx-font-weight: 600;");
                    } else {
                        badge.setStyle("-fx-background-color: #eff6ff; -fx-text-fill: #2563eb; -fx-padding: 3 10 3 10; -fx-background-radius: 12px; -fx-font-size: 11px; -fx-font-weight: 600;");
                    }
                    setGraphic(badge);
                }
            }
        });

        var colGroupes = new TableColumn<Formateur, Long>("");
        colGroupes.setCellValueFactory(new PropertyValueFactory<>("id"));
        colGroupes.setPrefWidth(48);
        colGroupes.setMaxWidth(55);
        colGroupes.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Long item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                var btn = new Button();
                btn.setGraphic(fontAwesome.create(FontAwesome.Glyph.USERS).color(Color.WHITE).size(15));
                btn.getStyleClass().add("btn-affect");
                btn.setTooltip(new Tooltip("Gérer les groupes affectés à ce formateur"));
                setGraphic(btn);
                btn.setOnAction(event -> openGroupesDialog(item));
            }
        });

        var colPrint = new TableColumn<Formateur, Long>("");
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
                btn.setOnAction(event -> printFormateurTimetable(item));
            }
        });

        var colUpdate = new TableColumn<Formateur, Long>("");
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
                btn.setTooltip(new Tooltip("Modifier ce formateur"));
                setGraphic(btn);
                btn.setOnAction(event -> updateForm(item));
            }
        });

        var colDelete = new TableColumn<Formateur, Long>("");
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
                btn.setTooltip(new Tooltip("Supprimer ce formateur"));
                setGraphic(btn);
                btn.setOnAction(event -> deleteItem(item));
            }
        });

        table.getColumns().addAll(colNom, colPrenom, colMatricule, colType, colNbGroupes, colGroupes, colPrint, colUpdate, colDelete);
        loadData();

        var vBox = new VBox(15);
        vBox.setPadding(new Insets(15));
        vBox.getChildren().addAll(formateurTitle, formCard, searchBar, table);
        setContent(vBox);

    }

    private void printFormateurTimetable(Long item) {
        var opt = masterData.stream().filter(g -> Objects.equals(g.getId(), item)).findFirst();
        if (opt.isEmpty()) return;
        var groupeTT = new FormateurTimeTablePrint(opt.get());
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
        groupeCounts = Database.getInstance().getGroupesCountByFormateur();
        masterData.setAll(Database.getInstance().getFormateurs());
        table.refresh();
        editor.loadMenuFormateur();
    }

    private void clearForm() {
        txtNom.clear();
        txtPrenom.clear();
        txtMatricule.clear();
        FormUtils.setFieldError(txtNom, false);
        formateur = null;
        lblFormTitle.setText("Nouveau Formateur");
        lblModeBadge.setText("Création");
        lblModeBadge.getStyleClass().setAll("badge-mode-create");
        btnSave.setText("Ajouter");
    }

    private void saveForm() {
        var nom = txtNom.getText().trim();
        if (nom.isEmpty()) {
            FormUtils.setFieldError(txtNom, true);
            NotificationService.showWarning("Champ obligatoire", "Veuillez saisir un nom pour le formateur.");
            return;
        }

        boolean isNew = (formateur == null);
        if (isNew) {
            formateur = new Formateur(nom, txtPrenom.getText().trim(), txtMatricule.getText().trim(), comboType.getSelectionModel().getSelectedItem());
            Database.getInstance().execute("INSERT INTO formateur (nom, prenom, matricule, type) VALUES(?,?,?,?)",
                    formateur.getNom(), formateur.getPrenom(), formateur.getMatricule(), formateur.getType());
            NotificationService.showSuccess("Formateur créé", "Le formateur '" + formateur + "' a été ajouté avec succès.");
        } else {
            formateur = new Formateur(formateur.getId(), nom, txtPrenom.getText().trim(), txtMatricule.getText().trim(), comboType.getSelectionModel().getSelectedItem());
            Database.getInstance().execute("UPDATE formateur SET nom=? ,prenom=? ,matricule=? ,type=? WHERE id=?",
                    formateur.getNom(), formateur.getPrenom(), formateur.getMatricule(), formateur.getType(), String.valueOf(formateur.getId()));
            NotificationService.showSuccess("Formateur modifié", "Le formateur '" + formateur + "' a été mis à jour avec succès.");
        }
        loadData();
        clearForm();
    }

    private void deleteItem(long id) {
        var target = masterData.stream().filter(f -> f.getId() == id).findFirst().orElse(null);
        String nomFormateur = (target != null) ? target.toString() : "ce formateur";

        if (Database.getInstance().isFormateurHasSeances(id)) {
            NotificationService.showError("Suppression impossible",
                    "Impossible de supprimer le formateur '" + nomFormateur + "' car il a des séances assignées dans l'emploi du temps.");
            return;
        }

        boolean confirm = NotificationService.confirmDestruction(
                "Confirmation de suppression",
                "Supprimer le formateur",
                "Voulez-vous vraiment supprimer définitivement le formateur '" + nomFormateur + "' ?"
        );

        if (confirm) {
            Database.getInstance().execute("DELETE FROM formateur_groupe WHERE idFormateur = ?", String.valueOf(id));
            Database.getInstance().execute("DELETE FROM formateur WHERE id= ?", String.valueOf(id));
            loadData();
            NotificationService.showSuccess("Formateur supprimé", "Le formateur '" + nomFormateur + "' a été supprimé avec succès.");
        }
    }

    private void openGroupesDialog(Long id) {
        var opt = masterData.stream().filter(f -> Objects.equals(f.getId(), id)).findFirst();
        if (opt.isEmpty()) return;
        new FormateurGroupesDialog(opt.get(), table.getScene().getWindow()).showAndWait();
        loadData();
    }

    private void updateForm(long id) {
        masterData.stream()
                .filter(e -> e.getId() == id)
                .findFirst().ifPresent(e -> {
                    formateur = e;
                    txtNom.setText(e.getNom());
                    FormUtils.setFieldError(txtNom, false);
                    txtMatricule.setText(e.getMatricule());
                    txtPrenom.setText(e.getPrenom());
                    comboType.getSelectionModel().select(e.getType());
                    lblFormTitle.setText("Modifier le Formateur : " + e);
                    lblModeBadge.setText("Modification");
                    lblModeBadge.getStyleClass().setAll("badge-mode-edit");
                    btnSave.setText("Mettre à jour");
                    txtNom.requestFocus();
                });
    }
}
