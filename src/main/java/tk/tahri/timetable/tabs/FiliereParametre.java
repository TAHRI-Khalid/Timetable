package tk.tahri.timetable.tabs;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
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
import tk.tahri.timetable.dao.Database;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import tk.tahri.timetable.tools.FormUtils;
import tk.tahri.timetable.tools.NotificationService;

public class FiliereParametre extends EditorTab {

    private final Editor editor;
    private Filiere filiere;
    private final ObservableList<Filiere> masterData = FXCollections.observableArrayList();
    private final TableView<Filiere> table;
    private final TextField txtNom;
    private final TextField txtAbreviation;
    private final ComboBox<String> comboNiveau;
    private final ComboBox<String> comboSecteur;
    private final ComboBox<String> comboType;
    private final Label lblFormTitle = new Label("Nouvelle Filière");
    private final Label lblModeBadge = new Label("Création");
    private final Button btnSave = new Button("Ajouter");
    private final Button btnCancel = new Button("Réinitialiser");

    public FiliereParametre(Editor editor) {
        super("Filières");
        this.editor = editor;
        FontAwesome fontAwesome = new FontAwesome();

        Label groupeTitle = new Label("Les Filières ");
        groupeTitle.setGraphic(fontAwesome.create(FontAwesome.Glyph.USERS).color(Color.WHITE).size(26));
        groupeTitle.setGraphicTextGap(10);
        groupeTitle.setMaxWidth(Double.MAX_VALUE);
        groupeTitle.getStyleClass().add("groupe-title-main");

        var grid = new GridPane();
        grid.setVgap(12);
        grid.setHgap(15);

        grid.add(new Label("Nom de la filière : "), 0, 0);
        txtNom = new TextField();
        txtNom.setPromptText("Ex: Développement Digital, Gestion des Entreprises...");
        FormUtils.bindFieldValidator(txtNom);
        GridPane.setHgrow(txtNom, Priority.ALWAYS);
        grid.add(txtNom, 1, 0);

        grid.add(new Label("Abréviation : "), 2, 0);
        txtAbreviation = new TextField();
        txtAbreviation.setPromptText("Ex: DEV, GE, ID...");
        GridPane.setHgrow(txtAbreviation, Priority.ALWAYS);
        grid.add(txtAbreviation, 3, 0);

        grid.add(new Label("Niveau : "), 0, 1);
        comboNiveau = new ComboBox<>();
        GridPane.setHgrow(comboNiveau, Priority.ALWAYS);
        comboNiveau.getItems().addAll(Filiere.Niveaux);
        comboNiveau.getSelectionModel().select(0);
        grid.add(comboNiveau, 1, 1);

        grid.add(new Label("Secteur : "), 2, 1);
        comboSecteur = new ComboBox<>();
        GridPane.setHgrow(comboSecteur, Priority.ALWAYS);
        comboSecteur.getItems().addAll(Filiere.Secteurs);
        comboSecteur.getSelectionModel().select(0);
        grid.add(comboSecteur, 3, 1);

        grid.add(new Label("Type de formation : "), 0, 2);
        comboType = new ComboBox<>();
        GridPane.setHgrow(comboType, Priority.ALWAYS);
        comboType.getItems().addAll(Filiere.typeFormation);
        comboType.getSelectionModel().select(0);
        grid.add(comboType, 1, 2);

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
                "Rechercher une filière (nom, abréviation, niveau, secteur, type)...",
                (fil, term) -> (fil.getNom() != null && fil.getNom().toLowerCase().contains(term))
                        || (fil.getAbreviation() != null && fil.getAbreviation().toLowerCase().contains(term))
                        || (fil.getNiveau() != null && fil.getNiveau().toLowerCase().contains(term))
                        || (fil.getSecteur() != null && fil.getSecteur().toLowerCase().contains(term))
                        || (fil.getType() != null && fil.getType().toLowerCase().contains(term))
        );

        var colNom = new TableColumn<Filiere, String>("Nom");
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));

        var colAbreviation = new TableColumn<Filiere, String>("Abréviation");
        colAbreviation.setCellValueFactory(new PropertyValueFactory<>("abreviation"));

        var colNiveau = new TableColumn<Filiere, String>("Niveau");
        colNiveau.setCellValueFactory(new PropertyValueFactory<>("niveau"));

        var colSecteur = new TableColumn<Filiere, String>("Secteur");
        colSecteur.setCellValueFactory(new PropertyValueFactory<>("secteur"));

        var colType = new TableColumn<Filiere, String>("Type");
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));

        var colUpdate = new TableColumn<Filiere, Long>("");
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
                btn.setTooltip(new Tooltip("Modifier cette filière"));
                setGraphic(btn);
                btn.setOnAction(event -> updateForm(item));
            }
        });

        var colDelete = new TableColumn<Filiere, Long>("");
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
                btn.setTooltip(new Tooltip("Supprimer cette filière"));
                setGraphic(btn);
                btn.setOnAction(event -> deleteItem(item));
            }
        });

        table.getColumns().addAll(colNom, colAbreviation, colNiveau, colSecteur, colType, colUpdate, colDelete);
        loadData();

        var vBox = new VBox(15);
        vBox.setPadding(new Insets(15));
        vBox.getChildren().addAll(groupeTitle, formCard, searchBar, table);
        setContent(vBox);

    }

    private void loadData() {
        masterData.setAll(Database.getInstance().getFilieres());
        editor.loadMenuGroupe();
    }

    private void clearForm() {
        txtNom.clear();
        txtAbreviation.clear();
        FormUtils.setFieldError(txtNom, false);
        filiere = null;
        lblFormTitle.setText("Nouvelle Filière");
        lblModeBadge.setText("Création");
        lblModeBadge.getStyleClass().setAll("badge-mode-create");
        btnSave.setText("Ajouter");
    }

    private void saveForm() {
        var nom = txtNom.getText().trim();
        if (nom.isEmpty()) {
            FormUtils.setFieldError(txtNom, true);
            NotificationService.showWarning("Champ obligatoire", "Veuillez saisir un nom pour la filière.");
            return;
        }

        boolean isNew = (filiere == null);
        if (isNew) {
            filiere = new Filiere(
                    nom,
                    txtAbreviation.getText().trim(),
                    comboNiveau.getSelectionModel().getSelectedItem(),
                    comboSecteur.getSelectionModel().getSelectedItem(),
                    comboType.getSelectionModel().getSelectedItem()
            );
            Database.getInstance().execute("INSERT INTO filiere VALUES(null,?,?,?,?,?)",
                    filiere.getNom(), filiere.getAbreviation(), filiere.getNiveau(), filiere.getSecteur(), filiere.getType());
            NotificationService.showSuccess("Filière créée", "La filière '" + nom + "' a été ajoutée avec succès.");
        } else {
            filiere = new Filiere(
                    filiere.getId(),
                    nom,
                    txtAbreviation.getText().trim(),
                    comboNiveau.getSelectionModel().getSelectedItem(),
                    comboSecteur.getSelectionModel().getSelectedItem(),
                    comboType.getSelectionModel().getSelectedItem()
            );
            Database.getInstance().execute("UPDATE filiere SET nom=? ,abreviation=? ,niveau=? ,secteur=?,type=? WHERE id=?",
                    filiere.getNom(), filiere.getAbreviation(), filiere.getNiveau(), filiere.getSecteur(), filiere.getType(), String.valueOf(filiere.getId()));
            NotificationService.showSuccess("Filière modifiée", "La filière '" + nom + "' a été mise à jour avec succès.");
        }
        loadData();
        clearForm();
    }

    private void deleteItem(long id) {
        var target = masterData.stream().filter(f -> f.getId() == id).findFirst().orElse(null);
        String nomFiliere = (target != null) ? target.getNom() : "cette filière";

        if (Database.getInstance().isFiliereHasGroupes(id)) {
            NotificationService.showError("Suppression impossible",
                    "Impossible de supprimer la filière '" + nomFiliere + "' car elle contient des groupes d'étudiants.");
            return;
        }

        boolean confirm = NotificationService.confirmDestruction(
                "Confirmation de suppression",
                "Supprimer la filière",
                "Voulez-vous vraiment supprimer définitivement la filière '" + nomFiliere + "' ?"
        );

        if (confirm) {
            Database.getInstance().execute("DELETE FROM filiere WHERE id=?", String.valueOf(id));
            loadData();
            NotificationService.showSuccess("Filière supprimée", "La filière '" + nomFiliere + "' a été supprimée avec succès.");
        }
    }

    private void updateForm(long id) {
        masterData.stream()
                .filter(e -> e.getId() == id)
                .findFirst().ifPresent(e -> {
                    filiere = e;
                    txtNom.setText(e.getNom());
                    FormUtils.setFieldError(txtNom, false);
                    txtAbreviation.setText(e.getAbreviation());
                    comboNiveau.getSelectionModel().select(e.getNiveau());
                    comboSecteur.getSelectionModel().select(e.getSecteur());
                    comboType.getSelectionModel().select(e.getType());
                    lblFormTitle.setText("Modifier la Filière : " + e.getNom());
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
