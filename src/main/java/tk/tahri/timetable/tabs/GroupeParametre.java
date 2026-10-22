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
import tk.tahri.timetable.Entities.Filiere;
import tk.tahri.timetable.Entities.Groupe;
import tk.tahri.timetable.dao.Database;
import tk.tahri.timetable.print.GroupeTimeTablePrint;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import tk.tahri.timetable.tools.FormUtils;
import tk.tahri.timetable.tools.NotificationService;

import java.util.Objects;

public class GroupeParametre extends EditorTab {

    private final Editor editor;
    private Groupe groupe;
    private final ObservableList<Groupe> masterData = FXCollections.observableArrayList();
    private final TableView<Groupe> table;
    private final TextField txtNom;
    private final ComboBox<Integer> comboAnnee;
    private final ComboBox<Filiere> comboFiliere;
    private final Label lblFormTitle = new Label("Nouveau Groupe");
    private final Label lblModeBadge = new Label("Création");
    private final Button btnSave = new Button("Ajouter");
    private final Button btnCancel = new Button("Réinitialiser");

    public GroupeParametre(Editor editor) {
        super("Groupes");
        this.editor = editor;
        FontAwesome fontAwesome = new FontAwesome();

        Label groupeTitle = new Label("Les Groupes ");
        groupeTitle.setGraphic(fontAwesome.create(FontAwesome.Glyph.USERS).color(Color.WHITE).size(26));
        groupeTitle.setGraphicTextGap(10);
        groupeTitle.setMaxWidth(Double.MAX_VALUE);
        groupeTitle.getStyleClass().add("groupe-title-main");


        var grid = new GridPane();
        grid.setVgap(12);
        grid.setHgap(15);

        grid.add(new Label("Nom du groupe : "), 0, 0);
        txtNom = new TextField();
        txtNom.setPromptText("Ex: DEV-101, TDM-201...");
        FormUtils.bindFieldValidator(txtNom);
        GridPane.setHgrow(txtNom, Priority.ALWAYS);
        grid.add(txtNom, 1, 0);

        grid.add(new Label("Filière : "), 2, 0);
        comboFiliere = new ComboBox<>();
        comboFiliere.getItems().addAll(Database.getInstance().getFilieres());
        if (!comboFiliere.getItems().isEmpty()) {
            comboFiliere.getSelectionModel().select(0);
        }
        GridPane.setHgrow(comboFiliere, Priority.ALWAYS);
        grid.add(comboFiliere, 3, 0);

        grid.add(new Label("Année : "), 4, 0);
        comboAnnee = new ComboBox<>();
        comboAnnee.getItems().addAll(1, 2, 3);
        comboAnnee.getSelectionModel().select(0);
        GridPane.setHgrow(comboAnnee, Priority.ALWAYS);
        grid.add(comboAnnee, 5, 0);

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
                "Rechercher un groupe (par nom, filière, année)...",
                (g, term) -> (g.getNom() != null && g.getNom().toLowerCase().contains(term))
                        || (g.getFiliere() != null && g.getFiliere().getNom() != null && g.getFiliere().getNom().toLowerCase().contains(term))
                        || String.valueOf(g.getAnnee()).contains(term)
        );

        var colNom = new TableColumn<Groupe, String>("Nom");
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));

        var colAnnee = new TableColumn<Groupe, String>("Année");
        colAnnee.setCellValueFactory(new PropertyValueFactory<>("annee"));

        var colFiliere = new TableColumn<Groupe, Filiere>("Filiére");
        colFiliere.setCellValueFactory(new PropertyValueFactory<>("filiere"));

        var colFormateurs = new TableColumn<Groupe, Long>("");
        colFormateurs.setCellValueFactory(new PropertyValueFactory<>("id"));
        colFormateurs.setPrefWidth(48);
        colFormateurs.setMaxWidth(55);
        colFormateurs.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Long item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                var btn = new Button();
                btn.setGraphic(fontAwesome.create(FontAwesome.Glyph.USER).color(Color.WHITE).size(15));
                btn.getStyleClass().add("btn-affect");
                btn.setTooltip(new Tooltip("Gérer les formateurs affectés à ce groupe"));
                setGraphic(btn);
                btn.setOnAction(event -> openFormateursDialog(item));
            }
        });

        var colPrint = new TableColumn<Groupe, Long>("");
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
                btn.setOnAction(event -> printGroupeTimetable(item));
            }
        });

        var colUpdate = new TableColumn<Groupe, Long>("");
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
                btn.setTooltip(new Tooltip("Modifier ce groupe"));
                setGraphic(btn);
                btn.setOnAction(event -> updateForm(item));
            }
        });

        var colDelete = new TableColumn<Groupe, Long>("");
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
                btn.setTooltip(new Tooltip("Supprimer ce groupe"));
                setGraphic(btn);
                btn.setOnAction(event -> deleteItem(item));
            }
        });

        table.getColumns().addAll(colNom, colAnnee, colFiliere, colFormateurs, colPrint, colUpdate, colDelete);
        loadData();

        var vBox = new VBox(15);
        vBox.setPadding(new Insets(15));
        vBox.getChildren().addAll(groupeTitle, formCard, searchBar, table);
        setContent(vBox);

    }

    private void printGroupeTimetable(Long item) {
        var opt = masterData.stream().filter(g -> Objects.equals(g.getId(), item)).findFirst();
        if (opt.isEmpty()) return;
        var groupeTT = new GroupeTimeTablePrint(opt.get());
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
        masterData.setAll(Database.getInstance().getGroupes());
        editor.loadMenuGroupe();
    }

    private void clearForm() {
        txtNom.clear();
        FormUtils.setFieldError(txtNom, false);
        groupe = null;
        lblFormTitle.setText("Nouveau Groupe");
        lblModeBadge.setText("Création");
        lblModeBadge.getStyleClass().setAll("badge-mode-create");
        btnSave.setText("Ajouter");
    }

    private void saveForm() {
        var nom = txtNom.getText().trim();
        if (nom.isEmpty()) {
            FormUtils.setFieldError(txtNom, true);
            NotificationService.showWarning("Champ obligatoire", "Veuillez saisir un nom pour le groupe.");
            return;
        }
        var filiere = comboFiliere.getSelectionModel().getSelectedItem();
        if (filiere == null) {
            NotificationService.showWarning("Champ obligatoire", "Veuillez sélectionner une filière.");
            return;
        }

        boolean isNew = (groupe == null);
        if (isNew) {
            groupe = new Groupe(
                    nom,
                    comboAnnee.getSelectionModel().getSelectedItem(),
                    filiere
            );
            Database.getInstance().execute("INSERT INTO groupe VALUES(null,?,?,?)",
                    groupe.getNom(), String.valueOf(groupe.getAnnee()), String.valueOf(groupe.getFiliere().getId()));
            NotificationService.showSuccess("Groupe créé", "Le groupe '" + nom + "' a été ajouté avec succès.");
        } else {
            groupe = new Groupe(
                    groupe.getId(),
                    nom,
                    comboAnnee.getSelectionModel().getSelectedItem(),
                    filiere
            );
            Database.getInstance().execute("UPDATE groupe SET nom=? ,annee=?,idFiliere=? WHERE id=?",
                    groupe.getNom(), String.valueOf(groupe.getAnnee()), String.valueOf(groupe.getFiliere().getId()), String.valueOf(groupe.getId()));
            NotificationService.showSuccess("Groupe modifié", "Le groupe '" + nom + "' a été mis à jour avec succès.");
        }
        loadData();
        clearForm();
    }

    private void deleteItem(long id) {
        var target = masterData.stream().filter(g -> g.getId() == id).findFirst().orElse(null);
        String nomGroupe = (target != null) ? target.getNom() : "ce groupe";

        if (Database.getInstance().isGroupeHasSeances(id)) {
            NotificationService.showError("Suppression impossible",
                    "Impossible de supprimer le groupe '" + nomGroupe + "' car il est associé à des séances dans l'emploi du temps.");
            return;
        }

        boolean confirm = NotificationService.confirmDestruction(
                "Confirmation de suppression",
                "Supprimer le groupe",
                "Voulez-vous vraiment supprimer définitivement le groupe '" + nomGroupe + "' ?"
        );

        if (confirm) {
            Database.getInstance().execute("DELETE FROM formateur_groupe WHERE idGroupe = ?", String.valueOf(id));
            Database.getInstance().execute("DELETE FROM groupe WHERE id=?", String.valueOf(id));
            loadData();
            NotificationService.showSuccess("Groupe supprimé", "Le groupe '" + nomGroupe + "' a été supprimé avec succès.");
        }
    }

    private void openFormateursDialog(Long id) {
        var opt = masterData.stream().filter(g -> Objects.equals(g.getId(), id)).findFirst();
        if (opt.isEmpty()) return;
        new GroupeFormateursDialog(opt.get(), table.getScene().getWindow()).showAndWait();
    }

    private void updateForm(long id) {
        masterData.stream()
                .filter(e -> e.getId() == id)
                .findFirst().ifPresent(e -> {
                    groupe = e;
                    txtNom.setText(e.getNom());
                    FormUtils.setFieldError(txtNom, false);
                    comboFiliere.getSelectionModel().select(e.getFiliere());
                    comboAnnee.getSelectionModel().select(e.getAnnee() - 1);
                    lblFormTitle.setText("Modifier le Groupe : " + e.getNom());
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
