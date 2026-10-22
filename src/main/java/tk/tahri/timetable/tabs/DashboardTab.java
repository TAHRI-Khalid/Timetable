package tk.tahri.timetable.tabs;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.controlsfx.glyphfont.FontAwesome;
import tk.tahri.timetable.Entities.Dashboard;
import tk.tahri.timetable.Entities.Etablissement;
import tk.tahri.timetable.dao.Database;
import tk.tahri.timetable.tools.Config;

import java.io.File;
import java.util.Objects;

public class DashboardTab extends EditorTab {

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

    // Charts
    private BarChart<String, Number> bc;
    private PieChart pc;

    // Alerts container
    private VBox alertsContainer;

    // Institution labels
    private Label lblEtabNom;
    private Label lblEtabSub;
    private Label lblBadgeAnnee;
    private Label lblBadgeFichier;

    public DashboardTab(String nom) {
        super(nom);
        this.setClosable(false);
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

        // 3. BarChart (Espaces)
        bc.getData().clear();
        var seriesEspaces = new XYChart.Series<String, Number>();
        d.getBarChartEspace().forEach((espace, aLong) ->
                seriesEspaces.getData().add(new XYChart.Data<>(espace.getNom(), aLong / 3600.0)));
        bc.getData().add(seriesEspaces);

        // 4. PieChart (Filières ou Formateurs)
        pc.getData().clear();
        if (!d.getHeuresParFiliere().isEmpty()) {
            pc.setTitle("Volume horaire par Filière");
            d.getHeuresParFiliere().forEach((fil, h) -> {
                if (h > 0) {
                    pc.getData().add(new PieChart.Data(fil + " (" + String.format("%.0fh", h) + ")", h));
                }
            });
        } else if (d.getHeuresPermanents() > 0 || d.getHeuresVacataires() > 0) {
            pc.setTitle("Répartition Permanents vs Vacataires");
            if (d.getHeuresPermanents() > 0) {
                pc.getData().add(new PieChart.Data("Permanents (" + String.format("%.0fh", d.getHeuresPermanents()) + ")", d.getHeuresPermanents()));
            }
            if (d.getHeuresVacataires() > 0) {
                pc.getData().add(new PieChart.Data("Vacataires (" + String.format("%.0fh", d.getHeuresVacataires()) + ")", d.getHeuresVacataires()));
            }
        }

        // 5. Alertes & Aide à la décision
        renderAlerts(d);
    }

    private void renderAlerts(Dashboard d) {
        alertsContainer.getChildren().clear();
        FontAwesome fontAwesome = new FontAwesome();

        int nbAlertes = 0;

        if (d.getNbFormateursSansGroupe() > 0) {
            nbAlertes++;
            var chip = createAlertChip(
                    fontAwesome.create(FontAwesome.Glyph.WARNING).color(Color.web("#d97706")).size(16),
                    d.getNbFormateursSansGroupe() + " formateur(s) sans aucun groupe affecté",
                    "Affectez des groupes via 'Paramètres > Formateurs' pour inclure ces intervenants dans l'emploi du temps.",
                    "alert-chip-warning"
            );
            alertsContainer.getChildren().add(chip);
        }

        if (d.getNbGroupesSansFormateur() > 0) {
            nbAlertes++;
            var chip = createAlertChip(
                    fontAwesome.create(FontAwesome.Glyph.EXCLAMATION_CIRCLE).color(Color.web("#dc2626")).size(16),
                    d.getNbGroupesSansFormateur() + " groupe(s) sans formateur affecté",
                    "Ces groupes ne disposent d'aucun formateur rattaché dans la table d'affectations.",
                    "alert-chip-danger"
            );
            alertsContainer.getChildren().add(chip);
        }

        if (d.getNbGroupesSousDotes() > 0) {
            nbAlertes++;
            var chip = createAlertChip(
                    fontAwesome.create(FontAwesome.Glyph.INFO_CIRCLE).color(Color.web("#2563eb")).size(16),
                    d.getNbGroupesSousDotes() + " groupe(s) en sous-charge (< 20h planifiées)",
                    "Certains groupes n'ont pas encore atteint le quota hebdomadaire minimal d'heures dispensées.",
                    "alert-chip-warning"
            );
            alertsContainer.getChildren().add(chip);
        }

        if (nbAlertes == 0) {
            var chip = createAlertChip(
                    fontAwesome.create(FontAwesome.Glyph.CHECK_CIRCLE).color(Color.web("#16a34a")).size(16),
                    "Emploi du temps équilibré et conforme",
                    "Tous les groupes et formateurs ont des affectations valides. Aucune anomalie détectée.",
                    "alert-chip-success"
            );
            alertsContainer.getChildren().add(chip);
        }
    }

    private HBox createAlertChip(javafx.scene.Node icon, String title, String detail, String styleClass) {
        var box = new HBox(12);
        box.setAlignment(Pos.CENTER_LEFT);
        box.getStyleClass().add(styleClass);

        var v = new VBox(2);
        var lblT = new Label(title);
        lblT.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #1e293b;");
        var lblD = new Label(detail);
        lblD.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");
        v.getChildren().addAll(lblT, lblD);

        box.getChildren().addAll(icon, v);
        return box;
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

        // 3. Section Graphiques (Double Chart)
        var chartsBox = new HBox(16);
        chartsBox.setAlignment(Pos.CENTER);

        // BarChart (Occupation des espaces)
        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        bc = new BarChart<>(xAxis, yAxis);
        bc.setTitle("Occupation hebdomadaire des Espaces");
        bc.setLegendVisible(false);
        bc.setAnimated(false);
        xAxis.setLabel("Espace");
        yAxis.setLabel("Heures / semaine");
        VBox.setVgrow(bc, Priority.ALWAYS);

        var chartContainerLeft = new VBox(bc);
        chartContainerLeft.getStyleClass().add("chart-card");
        chartContainerLeft.setPrefHeight(340);
        HBox.setHgrow(chartContainerLeft, Priority.ALWAYS);

        // PieChart (Répartition par Filière)
        pc = new PieChart();
        pc.setTitle("Répartition des Heures par Filière");
        pc.setAnimated(false);
        pc.setLegendVisible(true);
        VBox.setVgrow(pc, Priority.ALWAYS);

        var chartContainerRight = new VBox(pc);
        chartContainerRight.getStyleClass().add("chart-card");
        chartContainerRight.setPrefHeight(340);
        chartContainerRight.setPrefWidth(420);
        chartContainerRight.setMaxWidth(480);
        HBox.setHgrow(chartContainerRight, Priority.ALWAYS);

        chartsBox.getChildren().addAll(chartContainerLeft, chartContainerRight);
        vBox.getChildren().add(chartsBox);

        // 4. Section Alertes et Décisions de Direction
        var alertCard = new VBox(10);
        alertCard.getStyleClass().add("alert-card");

        var lblAlertHeader = new Label("Pilotage & Alertes Opérationnelles");
        lblAlertHeader.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        lblAlertHeader.setGraphic(fontAwesome.create(FontAwesome.Glyph.BELL).color(Color.web("#3b82f6")).size(16));
        lblAlertHeader.setGraphicTextGap(10);

        alertsContainer = new VBox(8);
        alertCard.getChildren().addAll(lblAlertHeader, alertsContainer);
        vBox.getChildren().add(alertCard);

        var scrollPane = new ScrollPane(vBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-padding: 0;");
        return scrollPane;
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
