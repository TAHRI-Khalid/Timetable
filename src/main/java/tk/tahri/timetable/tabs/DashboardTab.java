package tk.tahri.timetable.tabs;

import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.controlsfx.glyphfont.FontAwesome;
import tk.tahri.timetable.Editor;
import tk.tahri.timetable.Entities.Dashboard;
import tk.tahri.timetable.Entities.Formateur;
import tk.tahri.timetable.Entities.Groupe;
import tk.tahri.timetable.dao.Database;
import tk.tahri.timetable.tools.Config;

import java.io.File;
import java.util.Objects;

public class DashboardTab extends EditorTab {

    private Editor editor;

    // KPI Labels
    private Label lblVolume;
    private Label lblVolumeSub;
    private Label lblOccupation;
    private Label lblOccupationSub;
    private Label lblFormateur;
    private Label lblFormateurSub;
    private Label lblVacation;
    private Label lblVacationSub;
    private Label lblGroupe;
    private Label lblGroupeSub;

    // Institution labels
    private Label lblEtabNom;
    private Label lblEtabSub;
    private Label lblBadgeAnnee;
    private Label lblBadgeFichier;

    // Tables & Badges
    private final TableView<Dashboard.FormateurMasseHoraire> tableFormateurs = new TableView<>();
    private final TableView<Dashboard.GroupeMasseHoraire> tableGroupes = new TableView<>();
    private final Label lblBadgeCountFormateurs = new Label();
    private final Label lblBadgeCountGroupes = new Label();

    public DashboardTab(String nom) {
        super(nom);
        this.setClosable(false);
    }

    public DashboardTab(String nom, Editor editor) {
        this(nom);
        this.editor = editor;
    }

    @Override
    protected void selectionChanged() {
        if (Config.getProperty("file").isEmpty() && Config.getProperty("temp").isEmpty()) {
            this.setContent(noData());
        } else {
            this.setContent(dashboard());
            refreshData();
        }
    }

    private void refreshData() {
        var d = Database.getInstance().getDashboard();
        var etab = Database.getInstance().getEtablissement();

        // 1. Institution
        if (etab != null && etab.getNom() != null && !etab.getNom().isBlank()) {
            lblEtabNom.setText(etab.getNom());
            String sub = "";
            if (etab.getComplexe() != null && !etab.getComplexe().isBlank()) sub += etab.getComplexe() + "  •  ";
            if (etab.getDirection() != null && !etab.getDirection().isBlank()) sub += etab.getDirection();
            lblEtabSub.setText(sub);
            lblBadgeAnnee.setText("Année : " + (etab.getAnneeDeFormation() != null ? etab.getAnneeDeFormation() : "En cours"));
        } else {
            lblEtabNom.setText("Tableau de bord de Direction");
            lblEtabSub.setText("Vue d'ensemble et pilotage pédagogique");
            lblBadgeAnnee.setText("Année de formation");
        }

        String filePath = Config.getProperty("file");
        if (filePath != null && !filePath.isBlank()) {
            lblBadgeFichier.setText(new File(filePath).getName());
        } else {
            lblBadgeFichier.setText("Nouveau projet");
        }

        // 2. Cartes KPIs
        lblVolume.setText(String.format("%.1f h", d.getTotalHeuresPlanifiees()));
        lblVolumeSub.setText(String.format("Moyenne : %.1f h / groupe", d.getMoyenneHeuresParGroupe()));

        lblOccupation.setText(String.format("%.1f%%", d.getTauxOccupationGlobal()));
        lblOccupationSub.setText(d.getNombreEspaces() + " salles & ateliers");

        lblFormateur.setText(String.valueOf(d.getNombreFormateurs()));
        lblFormateurSub.setText(d.getNombreFormateursPermanents() + " Perm.  •  " + d.getNombreFormateursVacataires() + " Vac.");

        double totalH = d.getTotalHeuresPlanifiees();
        double partVac = (totalH > 0) ? (d.getHeuresVacataires() / totalH) * 100.0 : 0.0;
        lblVacation.setText(String.format("%.0f%%", partVac));
        lblVacationSub.setText(String.format("%.1f h vacataires", d.getHeuresVacataires()));

        lblGroupe.setText(String.valueOf(d.getNombreGroupes()));
        lblGroupeSub.setText(d.getNombreFilieres() + " filières  •  " + d.getNombreSeances() + " séances");

        // 3. Tables des masses horaires
        tableFormateurs.getItems().setAll(d.getMasseHoraireFormateurs());
        lblBadgeCountFormateurs.setText(d.getMasseHoraireFormateurs().size() + " formateurs");

        tableGroupes.getItems().setAll(d.getMasseHoraireGroupes());
        lblBadgeCountGroupes.setText(d.getMasseHoraireGroupes().size() + " groupes");
    }

    @Override
    public void print() {
    }

    private ScrollPane dashboard() {
        FontAwesome fontAwesome = new FontAwesome();
        var vBox = new VBox(20);
        vBox.setPadding(new Insets(20));

        // 1. Bannière Institutionnelle
        var banner = new HBox(15);
        banner.setAlignment(Pos.CENTER_LEFT);
        banner.getStyleClass().add("institution-banner");

        var iconEtab = fontAwesome.create(FontAwesome.Glyph.INSTITUTION).color(Color.WHITE).size(30);

        var vTitles = new VBox(4);
        lblEtabNom = new Label("Institut de Formation");
        lblEtabNom.getStyleClass().add("institution-title");
        lblEtabSub = new Label("Direction Régionale  •  Complexe");
        lblEtabSub.getStyleClass().add("institution-subtitle");
        vTitles.getChildren().addAll(lblEtabNom, lblEtabSub);
        HBox.setHgrow(vTitles, Priority.ALWAYS);

        lblBadgeAnnee = new Label("2025/2026");
        lblBadgeAnnee.getStyleClass().add("institution-badge");

        lblBadgeFichier = new Label("Fichier actif");
        lblBadgeFichier.getStyleClass().add("institution-badge");
        lblBadgeFichier.setGraphic(fontAwesome.create(FontAwesome.Glyph.FILE_ALT).color(Color.WHITE).size(12));

        var badgesBox = new HBox(10, lblBadgeAnnee, lblBadgeFichier);
        badgesBox.setAlignment(Pos.CENTER_RIGHT);

        banner.getChildren().addAll(iconEtab, vTitles, badgesBox);
        vBox.getChildren().add(banner);

        // 2. Grille de 5 Cartes KPIs
        var cards = new HBox(14);
        cards.setAlignment(Pos.CENTER);

        // Carte 1 : Volume Global
        lblVolume = new Label("0.0 h");
        lblVolume.getStyleClass().add("kpi-number");
        lblVolume.setGraphic(fontAwesome.create(FontAwesome.Glyph.CALENDAR).color(Color.WHITE).size(30));
        lblVolume.setGraphicTextGap(16);
        lblVolumeSub = new Label("Charge hebdomadaire");
        lblVolumeSub.getStyleClass().add("kpi-subtext");
        var card1 = createCard("kpi-card-seance", "Volume Hebdomadaire", lblVolume, lblVolumeSub);

        // Carte 2 : Taux d'Occupation
        lblOccupation = new Label("0.0%");
        lblOccupation.getStyleClass().add("kpi-number");
        lblOccupation.setGraphic(fontAwesome.create(FontAwesome.Glyph.PIE_CHART).color(Color.WHITE).size(30));
        lblOccupation.setGraphicTextGap(16);
        lblOccupationSub = new Label("Occupation des locaux");
        lblOccupationSub.getStyleClass().add("kpi-subtext");
        var card2 = createCard("kpi-card-occupation", "Taux d'Occupation", lblOccupation, lblOccupationSub);

        // Carte 3 : Formateurs
        lblFormateur = new Label("0");
        lblFormateur.getStyleClass().add("kpi-number");
        lblFormateur.setGraphic(fontAwesome.create(FontAwesome.Glyph.USER).color(Color.WHITE).size(30));
        lblFormateur.setGraphicTextGap(16);
        lblFormateurSub = new Label("Corps professoral");
        lblFormateurSub.getStyleClass().add("kpi-subtext");
        var card3 = createCard("kpi-card-formateur", "Formateurs", lblFormateur, lblFormateurSub);

        // Carte 4 : Vacations / Budget
        lblVacation = new Label("0%");
        lblVacation.getStyleClass().add("kpi-number");
        lblVacation.setGraphic(fontAwesome.create(FontAwesome.Glyph.CLOCK_ALT).color(Color.WHITE).size(30));
        lblVacation.setGraphicTextGap(16);
        lblVacationSub = new Label("Part des vacations");
        lblVacationSub.getStyleClass().add("kpi-subtext");
        var card4 = createCard("kpi-card-espace", "Taux de Vacation", lblVacation, lblVacationSub);

        // Carte 5 : Groupes
        lblGroupe = new Label("0");
        lblGroupe.getStyleClass().add("kpi-number");
        lblGroupe.setGraphic(fontAwesome.create(FontAwesome.Glyph.USERS).color(Color.WHITE).size(30));
        lblGroupe.setGraphicTextGap(16);
        lblGroupeSub = new Label("Groupes formés");
        lblGroupeSub.getStyleClass().add("kpi-subtext");
        var card5 = createCard("kpi-card-groupe", "Groupes d'Étudiants", lblGroupe, lblGroupeSub);

        cards.getChildren().addAll(card1, card2, card3, card4, card5);
        vBox.getChildren().add(cards);

        // 3. Section Masses Horaires (Plein écran : Listes Formateurs & Groupes)
        setupFormateursTable();
        setupGroupesTable();

        var tablesBox = new HBox(16);
        tablesBox.setAlignment(Pos.CENTER);
        VBox.setVgrow(tablesBox, Priority.ALWAYS);

        var cardFormateurs = createTableCard("Masse horaire des Formateurs", FontAwesome.Glyph.USER, lblBadgeCountFormateurs, tableFormateurs);
        HBox.setHgrow(cardFormateurs, Priority.ALWAYS);

        var cardGroupes = createTableCard("Masse horaire des Groupes", FontAwesome.Glyph.USERS, lblBadgeCountGroupes, tableGroupes);
        HBox.setHgrow(cardGroupes, Priority.ALWAYS);

        tablesBox.getChildren().addAll(cardFormateurs, cardGroupes);
        vBox.getChildren().add(tablesBox);

        var scrollPane = new ScrollPane(vBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-padding: 0;");
        return scrollPane;
    }

    private void setupFormateursTable() {
        tableFormateurs.getColumns().clear();

        var colNom = new TableColumn<Dashboard.FormateurMasseHoraire, String>("Formateur");
        colNom.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNomComplet()));

        var colType = new TableColumn<Dashboard.FormateurMasseHoraire, String>("Statut");
        colType.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getType()));
        colType.setPrefWidth(95);
        colType.setMaxWidth(115);
        colType.setCellFactory(col -> new TableCell<>() {
            private final Label badge = new Label();
            {
                setAlignment(Pos.CENTER);
            }
            @Override
            protected void updateItem(String type, boolean empty) {
                super.updateItem(type, empty);
                if (empty || type == null) {
                    setGraphic(null);
                } else {
                    badge.setText(type);
                    if ("Permanent".equalsIgnoreCase(type)) {
                        badge.setStyle("-fx-background-color: #ecfdf5; -fx-text-fill: #059669; -fx-padding: 3 10 3 10; -fx-background-radius: 10px; -fx-font-size: 11px; -fx-font-weight: 600;");
                    } else {
                        badge.setStyle("-fx-background-color: #eff6ff; -fx-text-fill: #2563eb; -fx-padding: 3 10 3 10; -fx-background-radius: 10px; -fx-font-size: 11px; -fx-font-weight: 600;");
                    }
                    setGraphic(badge);
                }
            }
        });

        var colDuree = new TableColumn<Dashboard.FormateurMasseHoraire, String>("Masse horaire");
        colDuree.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFormattedDuration()));
        colDuree.setPrefWidth(125);
        colDuree.setMaxWidth(140);
        colDuree.setCellFactory(col -> new TableCell<>() {
            private final Label badge = new Label();
            {
                setAlignment(Pos.CENTER);
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    var rowItem = getTableRow() != null ? getTableRow().getItem() : null;
                    badge.setText(item);
                    if (rowItem != null && rowItem.getHeuresDecimales() == 0) {
                        badge.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #94a3b8; -fx-padding: 4 12 4 12; -fx-background-radius: 12px; -fx-font-size: 12px; -fx-font-weight: bold;");
                    } else {
                        badge.setStyle("-fx-background-color: #ecfdf5; -fx-text-fill: #047857; -fx-padding: 4 12 4 12; -fx-background-radius: 12px; -fx-font-size: 12px; -fx-font-weight: bold;");
                    }
                    setGraphic(badge);
                }
            }
        });

        var colAction = new TableColumn<Dashboard.FormateurMasseHoraire, Void>("");
        colAction.setPrefWidth(42);
        colAction.setMaxWidth(48);
        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button();
            private final FontAwesome fa = new FontAwesome();
            {
                btn.setGraphic(fa.create(FontAwesome.Glyph.EXTERNAL_LINK).color(Color.web("#2563eb")).size(13));
                btn.setStyle("-fx-background-color: transparent; -fx-padding: 3; -fx-cursor: hand;");
                btn.setTooltip(new Tooltip("Ouvrir l'emploi du temps de ce formateur"));
                setAlignment(Pos.CENTER);
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    btn.setOnAction(e -> openFormateurTab(getTableRow().getItem().formateur()));
                    setGraphic(btn);
                }
            }
        });

        tableFormateurs.getColumns().addAll(colNom, colType, colDuree, colAction);

        tableFormateurs.setRowFactory(tv -> {
            TableRow<Dashboard.FormateurMasseHoraire> row = new TableRow<>();
            row.setCursor(javafx.scene.Cursor.HAND);
            row.setTooltip(new Tooltip("Cliquer pour ouvrir l'emploi du temps"));
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getButton() == javafx.scene.input.MouseButton.PRIMARY) {
                    openFormateurTab(row.getItem().formateur());
                }
            });
            return row;
        });
    }

    private void setupGroupesTable() {
        tableGroupes.getColumns().clear();

        var colNom = new TableColumn<Dashboard.GroupeMasseHoraire, String>("Groupe");
        colNom.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNom()));

        var colFiliere = new TableColumn<Dashboard.GroupeMasseHoraire, String>("Filière");
        colFiliere.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFiliereAbreviation()));
        colFiliere.setPrefWidth(95);
        colFiliere.setMaxWidth(125);

        var colAnnee = new TableColumn<Dashboard.GroupeMasseHoraire, String>("Année");
        colAnnee.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAnnee() + "A"));
        colAnnee.setPrefWidth(65);
        colAnnee.setMaxWidth(75);
        colAnnee.setStyle("-fx-alignment: CENTER;");

        var colDuree = new TableColumn<Dashboard.GroupeMasseHoraire, String>("Masse horaire");
        colDuree.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFormattedDuration()));
        colDuree.setPrefWidth(125);
        colDuree.setMaxWidth(140);
        colDuree.setCellFactory(col -> new TableCell<>() {
            private final Label badge = new Label();
            {
                setAlignment(Pos.CENTER);
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    var rowItem = getTableRow() != null ? getTableRow().getItem() : null;
                    badge.setText(item);
                    if (rowItem != null && rowItem.getHeuresDecimales() == 0) {
                        badge.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #94a3b8; -fx-padding: 4 12 4 12; -fx-background-radius: 12px; -fx-font-size: 12px; -fx-font-weight: bold;");
                    } else {
                        badge.setStyle("-fx-background-color: #eff6ff; -fx-text-fill: #1d4ed8; -fx-padding: 4 12 4 12; -fx-background-radius: 12px; -fx-font-size: 12px; -fx-font-weight: bold;");
                    }
                    setGraphic(badge);
                }
            }
        });

        var colAction = new TableColumn<Dashboard.GroupeMasseHoraire, Void>("");
        colAction.setPrefWidth(42);
        colAction.setMaxWidth(48);
        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button();
            private final FontAwesome fa = new FontAwesome();
            {
                btn.setGraphic(fa.create(FontAwesome.Glyph.EXTERNAL_LINK).color(Color.web("#2563eb")).size(13));
                btn.setStyle("-fx-background-color: transparent; -fx-padding: 3; -fx-cursor: hand;");
                btn.setTooltip(new Tooltip("Ouvrir l'emploi du temps de ce groupe"));
                setAlignment(Pos.CENTER);
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    btn.setOnAction(e -> openGroupeTab(getTableRow().getItem().groupe()));
                    setGraphic(btn);
                }
            }
        });

        tableGroupes.getColumns().addAll(colNom, colFiliere, colAnnee, colDuree, colAction);

        tableGroupes.setRowFactory(tv -> {
            TableRow<Dashboard.GroupeMasseHoraire> row = new TableRow<>();
            row.setCursor(javafx.scene.Cursor.HAND);
            row.setTooltip(new Tooltip("Cliquer pour ouvrir l'emploi du temps"));
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getButton() == javafx.scene.input.MouseButton.PRIMARY) {
                    openGroupeTab(row.getItem().groupe());
                }
            });
            return row;
        });
    }

    private void openFormateurTab(Formateur formateur) {
        if (formateur == null) return;
        if (editor != null) {
            editor.loadTab(new FormateurTab(formateur));
        } else if (getTabPane() != null) {
            var tab = new FormateurTab(formateur);
            getTabPane().getTabs().stream()
                    .filter(t -> t.getText().equals(tab.getText()))
                    .findFirst()
                    .ifPresentOrElse(getTabPane().getSelectionModel()::select,
                            () -> {
                                getTabPane().getTabs().add(tab);
                                getTabPane().getSelectionModel().select(tab);
                            });
        }
    }

    private void openGroupeTab(Groupe groupe) {
        if (groupe == null) return;
        if (editor != null) {
            editor.loadTab(new GroupeTab(groupe));
        } else if (getTabPane() != null) {
            var tab = new GroupeTab(groupe);
            getTabPane().getTabs().stream()
                    .filter(t -> t.getText().equals(tab.getText()))
                    .findFirst()
                    .ifPresentOrElse(getTabPane().getSelectionModel()::select,
                            () -> {
                                getTabPane().getTabs().add(tab);
                                getTabPane().getSelectionModel().select(tab);
                            });
        }
    }

    private VBox createTableCard(String title, FontAwesome.Glyph glyph, Label badge, TableView<?> table) {
        var card = new VBox(10);
        card.getStyleClass().add("chart-card");
        card.setPrefHeight(460);
        VBox.setVgrow(card, Priority.ALWAYS);

        var header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        var fontAwesome = new FontAwesome();
        var icon = fontAwesome.create(glyph).color(Color.web("#2563eb")).size(16);

        var lbl = new Label(title);
        lbl.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        lbl.setGraphic(icon);
        lbl.setGraphicTextGap(10);

        var spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        badge.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #475569; -fx-padding: 3 8 3 8; -fx-background-radius: 10px; -fx-font-size: 11px; -fx-font-weight: 600;");

        header.getChildren().addAll(lbl, spacer, badge);

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(table, Priority.ALWAYS);

        card.getChildren().addAll(header, table);
        return card;
    }

    private VBox createCard(String styleClass, String title, Label lblValue, Label lblSub) {
        var card = new VBox(4);
        card.getStyleClass().addAll("kpi-card", styleClass);
        HBox.setHgrow(card, Priority.ALWAYS);

        var lblTitle = new Label(title);
        lblTitle.getStyleClass().add("kpi-label");

        card.getChildren().addAll(lblValue, lblTitle, lblSub);
        return card;
    }

    private VBox noData() {
        var vBox = new VBox(15);
        vBox.setAlignment(Pos.CENTER);
        vBox.setPadding(new Insets(40));
        var image = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/notdata.png"))));
        image.setFitWidth(180);
        image.setPreserveRatio(true);
        var lblTitle = new Label("Aucun emploi du temps actif");
        lblTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #475569;");
        var lblSubtitle = new Label("Ouvrez un fichier existant ou créez un nouveau projet depuis le menu Fichier.");
        lblSubtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #94a3b8;");
        vBox.getChildren().addAll(image, lblTitle, lblSubtitle);
        return vBox;
    }
}
