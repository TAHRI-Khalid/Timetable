package tk.tahri.timetable.tabs;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import tk.tahri.timetable.Entities.CreneauHoraire;
import tk.tahri.timetable.dao.Database;
import tk.tahri.timetable.tools.FormUtils;
import tk.tahri.timetable.tools.NotificationService;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class CreneauParametre extends EditorTab {

    private final Editor editor;
    private CreneauHoraire creneau;
    private final ObservableList<CreneauHoraire> masterData = FXCollections.observableArrayList();
    private final TableView<CreneauHoraire> table;
    private final TextField txtNom;
    private final ComboBox<String> comboDebut;
    private final ComboBox<String> comboFin;
    private final TextField txtOrdre;
    private final Label lblDureeBadge = new Label("Durée : --");
    private final Label lblFormTitle = new Label("Nouveau Créneau Horaire");
    private final Label lblModeBadge = new Label("Création");
    private final Button btnSave = new Button("Ajouter");
    private final Button btnCancel = new Button("Réinitialiser");

    public CreneauParametre(Editor editor) {
        super("Créneaux horaires");
        this.editor = editor;
        FontAwesome fontAwesome = new FontAwesome();

        Label creneauTitle = new Label("Les Créneaux Horaires");
        creneauTitle.setGraphic(fontAwesome.create(FontAwesome.Glyph.CLOCK_ALT).color(Color.WHITE).size(26));
        creneauTitle.setGraphicTextGap(10);
        creneauTitle.setMaxWidth(Double.MAX_VALUE);
        creneauTitle.getStyleClass().add("creneau-title-main");

        // Génération des heures types par pas de 15 minutes (07:00 -> 21:00)
        List<String> heures = generateTimeList();

        var grid = new GridPane();
        grid.setVgap(12);
        grid.setHgap(15);

        grid.add(new Label("Libellé du créneau : "), 0, 0);
        txtNom = new TextField();
        txtNom.setPromptText("Ex: 8:00 - 9:00, Pause...");
        FormUtils.bindFieldValidator(txtNom);
        GridPane.setHgrow(txtNom, Priority.ALWAYS);
        grid.add(txtNom, 1, 0);

        grid.add(new Label("Ordre d'affichage : "), 2, 0);
        txtOrdre = new TextField("1");
        txtOrdre.setPromptText("1, 2, 3...");
        GridPane.setHgrow(txtOrdre, Priority.ALWAYS);
        grid.add(txtOrdre, 3, 0);

        grid.add(new Label("Heure de début : "), 0, 1);
        comboDebut = new ComboBox<>(FXCollections.observableArrayList(heures));
        comboDebut.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(comboDebut, Priority.ALWAYS);
        comboDebut.setValue("08:00");
        grid.add(comboDebut, 1, 1);

        grid.add(new Label("Heure de fin : "), 2, 1);
        comboFin = new ComboBox<>(FXCollections.observableArrayList(heures));
        comboFin.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(comboFin, Priority.ALWAYS);
        comboFin.setValue("09:00");
        grid.add(comboFin, 3, 1);

        // Recalcul en direct de la durée estimée
        comboDebut.valueProperty().addListener((obs, o, n) -> updateDurationPreview());
        comboFin.valueProperty().addListener((obs, o, n) -> updateDurationPreview());
        updateDurationPreview();

        lblDureeBadge.getStyleClass().add("counter-badge");
        lblDureeBadge.setStyle("-fx-background-color: #f1f5f9; -fx-padding: 4 10 4 10; -fx-background-radius: 6px; -fx-font-weight: bold;");

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

        var hBox = new HBox(12, lblDureeBadge, btnSave, btnCancel);
        hBox.setAlignment(Pos.CENTER_RIGHT);

        formCard.getChildren().addAll(formHeader, grid, hBox);

        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        var searchBar = FormUtils.createSearchToolbar(
                table,
                masterData,
                "Rechercher un créneau horaire (nom, début, fin)...",
                (c, term) -> (c.getNom() != null && c.getNom().toLowerCase().contains(term))
                        || (c.getHeureDebutStr().contains(term))
                        || (c.getHeureFinStr().contains(term))
        );

        var btnResetDefaults = new Button("Rétablir par défaut");
        btnResetDefaults.setGraphic(fontAwesome.create(FontAwesome.Glyph.UNDO).color(Color.web("#475569")).size(14));
        btnResetDefaults.getStyleClass().add("btn-action-secondary");
        btnResetDefaults.setTooltip(new Tooltip("Rétablir les 11 créneaux horaires par défaut (8:00 - 18:30)"));
        btnResetDefaults.setOnAction(e -> {
            boolean confirm = NotificationService.confirmDestruction(
                    "Rétablir les créneaux par défaut",
                    "Confirmation de réinitialisation",
                    "Voulez-vous réinitialiser tous les créneaux avec les 11 créneaux officiels par défaut (8:00 à 18:30) ?"
            );
            if (confirm) {
                Database.getInstance().resetCreneauxToDefault();
                loadData();
                clearForm();
                NotificationService.showSuccess("Créneaux réinitialisés", "Les 11 créneaux horaires par défaut ont été rétablis avec succès.");
            }
        });
        searchBar.getChildren().add(btnResetDefaults);

        var colOrdre = new TableColumn<CreneauHoraire, Integer>("Ordre");
        colOrdre.setCellValueFactory(new PropertyValueFactory<>("ordre"));
        colOrdre.setPrefWidth(60);
        colOrdre.setMaxWidth(80);

        var colNom = new TableColumn<CreneauHoraire, String>("Libellé du créneau");
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));

        var colDebut = new TableColumn<CreneauHoraire, String>("Début");
        colDebut.setCellValueFactory(new PropertyValueFactory<>("heureDebutStr"));
        colDebut.setPrefWidth(90);
        colDebut.setMaxWidth(110);

        var colFin = new TableColumn<CreneauHoraire, String>("Fin");
        colFin.setCellValueFactory(new PropertyValueFactory<>("heureFinStr"));
        colFin.setPrefWidth(90);
        colFin.setMaxWidth(110);

        var colDuree = new TableColumn<CreneauHoraire, String>("Durée");
        colDuree.setCellValueFactory(new PropertyValueFactory<>("dureeFormattee"));
        colDuree.setPrefWidth(110);
        colDuree.setMaxWidth(130);

        var colUpdate = new TableColumn<CreneauHoraire, Long>("");
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
                btn.setTooltip(new Tooltip("Modifier ce créneau"));
                setGraphic(btn);
                btn.setOnAction(event -> updateForm(item));
            }
        });

        var colDelete = new TableColumn<CreneauHoraire, Long>("");
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
                btn.setTooltip(new Tooltip("Supprimer ce créneau"));
                setGraphic(btn);
                btn.setOnAction(event -> deleteItem(item));
            }
        });

        table.getColumns().addAll(colOrdre, colNom, colDebut, colFin, colDuree, colUpdate, colDelete);
        loadData();

        var vBox = new VBox(15);
        vBox.setPadding(new Insets(15));
        vBox.setStyle("-fx-background-color: #ffffff;");
        vBox.getChildren().addAll(creneauTitle, formCard, searchBar, table);
        setContent(vBox);
    }

    private void updateDurationPreview() {
        try {
            if (comboDebut.getValue() != null && comboFin.getValue() != null) {
                LocalTime d = LocalTime.parse(comboDebut.getValue());
                LocalTime f = LocalTime.parse(comboFin.getValue());
                long minutes = ChronoUnit.MINUTES.between(d, f);
                if (minutes > 0) {
                    long h = minutes / 60;
                    long m = minutes % 60;
                    lblDureeBadge.setText(m > 0 ? String.format("Durée : %02d H %02d Min", h, m) : String.format("Durée : %02d H", h));
                    lblDureeBadge.setStyle("-fx-background-color: #ecfdf5; -fx-text-fill: #059669; -fx-padding: 4 10 4 10; -fx-background-radius: 6px; -fx-font-weight: bold;");
                    return;
                }
            }
        } catch (DateTimeParseException ignored) {
        }
        lblDureeBadge.setText("Durée : Invalide");
        lblDureeBadge.setStyle("-fx-background-color: #fef2f2; -fx-text-fill: #ef4444; -fx-padding: 4 10 4 10; -fx-background-radius: 6px; -fx-font-weight: bold;");
    }

    private List<String> generateTimeList() {
        var list = new ArrayList<String>();
        for (int h = 7; h <= 21; h++) {
            for (int m = 0; m < 60; m += 15) {
                list.add(String.format("%02d:%02d", h, m));
            }
        }
        return list;
    }

    private void loadData() {
        masterData.setAll(Database.getInstance().getCreneaux());
    }

    private void clearForm() {
        txtNom.clear();
        txtOrdre.setText(String.valueOf(masterData.size() + 1));
        comboDebut.setValue("08:00");
        comboFin.setValue("09:00");
        FormUtils.setFieldError(txtNom, false);
        creneau = null;
        lblFormTitle.setText("Nouveau Créneau Horaire");
        lblModeBadge.setText("Création");
        lblModeBadge.getStyleClass().setAll("badge-mode-create");
        btnSave.setText("Ajouter");
    }

    private void saveForm() {
        var nom = txtNom.getText().trim();
        if (nom.isEmpty()) {
            FormUtils.setFieldError(txtNom, true);
            NotificationService.showWarning("Champ obligatoire", "Veuillez renseigner un libellé pour le créneau.");
            return;
        }

        LocalTime debut;
        LocalTime fin;
        try {
            debut = LocalTime.parse(comboDebut.getValue());
            fin = LocalTime.parse(comboFin.getValue());
        } catch (Exception e) {
            NotificationService.showWarning("Format invalide", "Veuillez sélectionner des heures valides.");
            return;
        }

        if (!fin.isAfter(debut)) {
            NotificationService.showWarning("Horaires incohérents", "L'heure de fin doit être strictement postérieure à l'heure de début.");
            return;
        }

        int ordre = 1;
        try {
            ordre = Integer.parseInt(txtOrdre.getText().trim());
        } catch (NumberFormatException ignored) {
        }

        boolean isNew = (creneau == null);
        if (isNew) {
            creneau = new CreneauHoraire(nom, debut, fin, ordre);
            Database.getInstance().execute("INSERT INTO creneau (nom, heureDebut, heureFin, ordre) VALUES(?,?,?,?)",
                    creneau.getNom(), creneau.getHeureDebutStr(), creneau.getHeureFinStr(), String.valueOf(creneau.getOrdre()));
            NotificationService.showSuccess("Créneau créé", "Le créneau '" + creneau + "' a été ajouté avec succès.");
        } else {
            creneau = new CreneauHoraire(creneau.getId(), nom, debut, fin, ordre);
            Database.getInstance().execute("UPDATE creneau SET nom=?, heureDebut=?, heureFin=?, ordre=? WHERE id=?",
                    creneau.getNom(), creneau.getHeureDebutStr(), creneau.getHeureFinStr(), String.valueOf(creneau.getOrdre()), String.valueOf(creneau.getId()));
            NotificationService.showSuccess("Créneau modifié", "Le créneau '" + creneau + "' a été mis à jour avec succès.");
        }
        loadData();
        clearForm();
    }

    private void deleteItem(long id) {
        var target = masterData.stream().filter(c -> c.getId() == id).findFirst().orElse(null);
        String nomCreneau = (target != null) ? target.toString() : "ce créneau";

        boolean confirm = NotificationService.confirmDestruction(
                "Confirmation de suppression",
                "Supprimer le créneau horaire",
                "Voulez-vous vraiment supprimer le créneau '" + nomCreneau + "' ?"
        );

        if (confirm) {
            Database.getInstance().execute("DELETE FROM creneau WHERE id= ?", String.valueOf(id));
            loadData();
            NotificationService.showSuccess("Créneau supprimé", "Le créneau '" + nomCreneau + "' a été supprimé avec succès.");
        }
    }

    private void updateForm(long id) {
        masterData.stream()
                .filter(c -> c.getId() == id)
                .findFirst().ifPresent(c -> {
                    creneau = c;
                    txtNom.setText(c.getNom());
                    FormUtils.setFieldError(txtNom, false);
                    txtOrdre.setText(String.valueOf(c.getOrdre()));
                    comboDebut.setValue(c.getHeureDebutStr());
                    comboFin.setValue(c.getHeureFinStr());
                    lblFormTitle.setText("Modifier le Créneau : " + c.getNom());
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
