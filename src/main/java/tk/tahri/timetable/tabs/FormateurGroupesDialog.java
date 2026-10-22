package tk.tahri.timetable.tabs;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.controlsfx.glyphfont.FontAwesome;
import tk.tahri.timetable.Entities.Formateur;
import tk.tahri.timetable.Entities.Groupe;
import tk.tahri.timetable.dao.Database;
import tk.tahri.timetable.tools.NotificationService;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class FormateurGroupesDialog extends Stage {

    public static class GroupeItem {
        private final Groupe groupe;
        private final BooleanProperty selected = new SimpleBooleanProperty();

        public GroupeItem(Groupe groupe, boolean selected) {
            this.groupe = groupe;
            this.selected.set(selected);
        }

        public Groupe getGroupe() {
            return groupe;
        }

        public BooleanProperty selectedProperty() {
            return selected;
        }

        public boolean isSelected() {
            return selected.get();
        }

        public void setSelected(boolean val) {
            this.selected.set(val);
        }
    }

    private final Formateur formateur;
    private final ObservableList<GroupeItem> masterData = FXCollections.observableArrayList();
    private final FilteredList<GroupeItem> filteredData;
    private final Label lblCount = new Label();

    public FormateurGroupesDialog(Formateur formateur, Window owner) {
        this.formateur = Objects.requireNonNull(formateur);
        if (owner != null) {
            initOwner(owner);
        }
        initModality(Modality.APPLICATION_MODAL);
        setTitle("Affectation des groupes - " + formateur);

        FontAwesome fontAwesome = new FontAwesome();

        // En-tête
        var headerBox = new VBox(4);
        headerBox.getStyleClass().add("formateur-title-main");
        headerBox.setPadding(new Insets(12, 16, 12, 16));

        var lblTitle = new Label("Groupes affectés au formateur : " + formateur);
        lblTitle.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 15px; -fx-font-weight: bold;");
        lblTitle.setGraphic(fontAwesome.create(FontAwesome.Glyph.USERS).color(Color.WHITE).size(18));
        lblTitle.setGraphicTextGap(10);

        var lblSubtitle = new Label("Sélectionnez les groupes pris en charge par ce formateur pour son emploi du temps.");
        lblSubtitle.setStyle("-fx-text-fill: #e2e8f0; -fx-font-size: 12px;");

        headerBox.getChildren().addAll(lblTitle, lblSubtitle);

        // Données
        var assignedIds = new HashSet<>(Database.getInstance().getGroupeIdsForFormateur(formateur.getId()));
        for (Groupe g : Database.getInstance().getGroupes()) {
            var item = new GroupeItem(g, assignedIds.contains(g.getId()));
            item.selectedProperty().addListener((obs, o, n) -> updateCountLabel());
            masterData.add(item);
        }

        filteredData = new FilteredList<>(masterData, p -> true);

        // Barre d'outils / Recherche
        var txtSearch = new TextField();
        txtSearch.setPromptText("Rechercher par groupe, filière, niveau...");
        HBox.setHgrow(txtSearch, Priority.ALWAYS);
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> {
            String term = (newVal == null) ? "" : newVal.trim().toLowerCase();
            filteredData.setPredicate(item -> {
                if (term.isEmpty()) return true;
                Groupe g = item.getGroupe();
                if (g.getNom() != null && g.getNom().toLowerCase().contains(term)) return true;
                if (g.getFiliere() != null) {
                    if (g.getFiliere().getNom() != null && g.getFiliere().getNom().toLowerCase().contains(term)) return true;
                    if (g.getFiliere().getAbreviation() != null && g.getFiliere().getAbreviation().toLowerCase().contains(term)) return true;
                    if (g.getFiliere().getNiveau() != null && g.getFiliere().getNiveau().toLowerCase().contains(term)) return true;
                }
                return false;
            });
        });

        var btnSelectAll = new Button("Tout cocher");
        btnSelectAll.getStyleClass().add("btn-action-secondary");
        btnSelectAll.setOnAction(e -> {
            for (GroupeItem item : filteredData) {
                item.setSelected(true);
            }
        });

        var btnDeselectAll = new Button("Tout décocher");
        btnDeselectAll.getStyleClass().add("btn-action-secondary");
        btnDeselectAll.setOnAction(e -> {
            for (GroupeItem item : filteredData) {
                item.setSelected(false);
            }
        });

        lblCount.setStyle("-fx-text-fill: #475569; -fx-font-size: 12px; -fx-font-weight: 600;");
        updateCountLabel();

        var toolBar = new HBox(10, txtSearch, btnSelectAll, btnDeselectAll);
        toolBar.setAlignment(Pos.CENTER_LEFT);
        toolBar.setPadding(new Insets(10, 15, 0, 15));

        // TableView
        TableView<GroupeItem> table = new TableView<>();
        table.setItems(filteredData);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<GroupeItem, Boolean> colCheck = new TableColumn<>("");
        colCheck.setPrefWidth(42);
        colCheck.setMaxWidth(48);
        colCheck.setCellValueFactory(cellData -> cellData.getValue().selectedProperty());
        colCheck.setCellFactory(col -> new TableCell<>() {
            private final CheckBox checkBox = new CheckBox();
            {
                checkBox.setAlignment(Pos.CENTER);
                setAlignment(Pos.CENTER);
            }
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    GroupeItem rowItem = getTableRow().getItem();
                    checkBox.selectedProperty().unbindBidirectional(rowItem.selectedProperty());
                    checkBox.setSelected(rowItem.isSelected());
                    checkBox.selectedProperty().bindBidirectional(rowItem.selectedProperty());
                    setGraphic(checkBox);
                }
            }
        });

        TableColumn<GroupeItem, String> colNom = new TableColumn<>("Groupe");
        colNom.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getGroupe().getNom()));

        TableColumn<GroupeItem, String> colFiliere = new TableColumn<>("Filière");
        colFiliere.setCellValueFactory(cellData -> {
            var filiere = cellData.getValue().getGroupe().getFiliere();
            return new javafx.beans.property.SimpleStringProperty(filiere != null ? filiere.getNom() : "");
        });

        TableColumn<GroupeItem, String> colNiveau = new TableColumn<>("Niveau");
        colNiveau.setCellValueFactory(cellData -> {
            var filiere = cellData.getValue().getGroupe().getFiliere();
            return new javafx.beans.property.SimpleStringProperty(filiere != null ? filiere.getNiveau() : "");
        });

        TableColumn<GroupeItem, String> colAnnee = new TableColumn<>("Année");
        colAnnee.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getGroupe().getAnnee() + "A"));
        colAnnee.setMaxWidth(70);

        table.getColumns().addAll(colCheck, colNom, colFiliere, colNiveau, colAnnee);

        table.setRowFactory(tv -> {
            TableRow<GroupeItem> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 1 && !(event.getTarget() instanceof CheckBox)) {
                    GroupeItem item = row.getItem();
                    if (item != null) {
                        item.setSelected(!item.isSelected());
                    }
                }
            });
            return row;
        });

        // Pied de page
        var btnSave = new Button("Enregistrer l'affectation");
        btnSave.getStyleClass().add("btn-action-primary");
        btnSave.setOnAction(e -> save());

        var btnCancel = new Button("Annuler");
        btnCancel.getStyleClass().add("btn-action-secondary");
        btnCancel.setOnAction(e -> close());

        var footerBox = new HBox(12, lblCount, new Region(), btnSave, btnCancel);
        HBox.setHgrow(footerBox.getChildren().get(1), Priority.ALWAYS);
        footerBox.setAlignment(Pos.CENTER_LEFT);
        footerBox.setPadding(new Insets(12, 15, 12, 15));
        footerBox.setStyle("-fx-border-color: #e2e8f0; -fx-border-width: 1 0 0 0; -fx-background-color: #f8fafc;");

        var tableWrapper = new VBox(table);
        tableWrapper.setPadding(new Insets(10, 15, 10, 15));
        VBox.setVgrow(tableWrapper, Priority.ALWAYS);

        var root = new VBox(headerBox, toolBar, tableWrapper, footerBox);
        root.setStyle("-fx-background-color: #ffffff;");

        var scene = new Scene(root, 700, 520);
        var css = getClass().getResource("/css/style.css");
        if (css != null) {
            scene.getStylesheets().add(css.toString());
        }
        setScene(scene);
    }

    private void updateCountLabel() {
        long count = masterData.stream().filter(GroupeItem::isSelected).count();
        lblCount.setText(count + " groupe(s) sélectionné(s) sur " + masterData.size());
    }

    private void save() {
        List<Long> selectedIds = masterData.stream()
                .filter(GroupeItem::isSelected)
                .map(i -> i.getGroupe().getId())
                .toList();

        Database.getInstance().setGroupesForFormateur(formateur.getId(), selectedIds);
        NotificationService.showSuccess("Affectation enregistrée",
                selectedIds.size() + " groupe(s) affecté(s) au formateur " + formateur);
        close();
    }
}
