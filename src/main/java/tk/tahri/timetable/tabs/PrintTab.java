package tk.tahri.timetable.tabs;

import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.PageLayout;
import javafx.print.PageOrientation;
import javafx.print.Paper;
import javafx.print.PrinterJob;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import org.controlsfx.control.CheckListView;
import org.controlsfx.glyphfont.FontAwesome;
import tk.tahri.timetable.Entities.Espace;
import tk.tahri.timetable.Entities.Formateur;
import tk.tahri.timetable.Entities.Groupe;
import tk.tahri.timetable.dao.Database;
import tk.tahri.timetable.print.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import tk.tahri.timetable.tools.NotificationService;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.stream.IntStream;

public class PrintTab extends EditorTab {

    private final CheckListView<Groupe> groupeCheckListView;
    private final CheckListView<Formateur> formateurCheckListView;
    private final CheckListView<Espace> espaceCheckListView;

    private final List<Groupe> allGroupes = new ArrayList<>();
    private final List<Formateur> allFormateurs = new ArrayList<>();
    private final List<Espace> allEspaces = new ArrayList<>();

    private final Set<Groupe> selectedGroupes = new LinkedHashSet<>();
    private final Set<Formateur> selectedFormateurs = new LinkedHashSet<>();
    private final Set<Espace> selectedEspaces = new LinkedHashSet<>();

    private final TextField txtSearchGroupe;
    private final TextField txtSearchFormateur;
    private final TextField txtSearchEspace;

    private final Label badgeGroupe;
    private final Label badgeFormateur;
    private final Label badgeEspace;
    private final Label lblSummary;

    private boolean isUpdatingList = false;

    public PrintTab(String nom) {
        super(nom);
        FontAwesome fontAwesome = new FontAwesome();

        var mainBox = new VBox(15);
        mainBox.setPadding(new Insets(18));

        // En-tête principal
        var titleLabel = new Label("Espace d'impression");
        titleLabel.setGraphic(fontAwesome.create(FontAwesome.Glyph.PRINT).color(Color.WHITE).size(24));
        titleLabel.setGraphicTextGap(10);
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        titleLabel.getStyleClass().add("groupe-title-main");
        mainBox.getChildren().add(titleLabel);

        // Initialisation des listes et badges
        groupeCheckListView = new CheckListView<>();
        formateurCheckListView = new CheckListView<>();
        espaceCheckListView = new CheckListView<>();

        badgeGroupe = new Label("0 sélectionné");
        badgeFormateur = new Label("0 sélectionné");
        badgeEspace = new Label("0 sélectionné");

        txtSearchGroupe = new TextField();
        txtSearchFormateur = new TextField();
        txtSearchEspace = new TextField();

        // Colonne 1 : Groupes
        var colGroupes = createColumnCard(
                "Groupes",
                FontAwesome.Glyph.USERS,
                badgeGroupe,
                txtSearchGroupe,
                "Filtrer par nom ou filière...",
                groupeCheckListView,
                () -> selectAllGroupes(true),
                () -> selectAllGroupes(false)
        );

        // Colonne 2 : Formateurs
        var colFormateurs = createColumnCard(
                "Formateurs",
                FontAwesome.Glyph.USER,
                badgeFormateur,
                txtSearchFormateur,
                "Filtrer par nom, matricule...",
                formateurCheckListView,
                () -> selectAllFormateurs(true),
                () -> selectAllFormateurs(false)
        );

        // Colonne 3 : Espaces
        var colEspaces = createColumnCard(
                "Espaces",
                FontAwesome.Glyph.BUILDING,
                badgeEspace,
                txtSearchEspace,
                "Filtrer par nom ou type...",
                espaceCheckListView,
                () -> selectAllEspaces(true),
                () -> selectAllEspaces(false)
        );

        var columnsGrid = new GridPane();
        columnsGrid.setHgap(16);
        columnsGrid.setVgap(12);
        VBox.setVgrow(columnsGrid, Priority.ALWAYS);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(33.33);
        col1.setHgrow(Priority.ALWAYS);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(33.33);
        col2.setHgrow(Priority.ALWAYS);
        ColumnConstraints col3 = new ColumnConstraints();
        col3.setPercentWidth(33.34);
        col3.setHgrow(Priority.ALWAYS);
        columnsGrid.getColumnConstraints().addAll(col1, col2, col3);

        RowConstraints row = new RowConstraints();
        row.setVgrow(Priority.ALWAYS);
        columnsGrid.getRowConstraints().add(row);

        columnsGrid.add(colGroupes, 0, 0);
        columnsGrid.add(colFormateurs, 1, 0);
        columnsGrid.add(colEspaces, 2, 0);

        mainBox.getChildren().add(columnsGrid);

        // Écouteurs de recherche
        txtSearchGroupe.textProperty().addListener((obs, oldV, newV) -> filterGroupes(newV));
        txtSearchFormateur.textProperty().addListener((obs, oldV, newV) -> filterFormateurs(newV));
        txtSearchEspace.textProperty().addListener((obs, oldV, newV) -> filterEspaces(newV));

        // Écouteurs de sélection pour maintenir les Sets
        groupeCheckListView.getCheckModel().getCheckedItems().addListener((ListChangeListener<Groupe>) c -> {
            if (isUpdatingList) return;
            while (c.next()) {
                if (c.wasAdded()) selectedGroupes.addAll(c.getAddedSubList());
                if (c.wasRemoved()) selectedGroupes.removeAll(c.getRemoved());
            }
            updateCounters();
        });

        formateurCheckListView.getCheckModel().getCheckedItems().addListener((ListChangeListener<Formateur>) c -> {
            if (isUpdatingList) return;
            while (c.next()) {
                if (c.wasAdded()) selectedFormateurs.addAll(c.getAddedSubList());
                if (c.wasRemoved()) selectedFormateurs.removeAll(c.getRemoved());
            }
            updateCounters();
        });

        espaceCheckListView.getCheckModel().getCheckedItems().addListener((ListChangeListener<Espace>) c -> {
            if (isUpdatingList) return;
            while (c.next()) {
                if (c.wasAdded()) selectedEspaces.addAll(c.getAddedSubList());
                if (c.wasRemoved()) selectedEspaces.removeAll(c.getRemoved());
            }
            updateCounters();
        });

        // Barre d'actions en bas
        lblSummary = new Label("Total sélectionné : 0 emploi(s) du temps");
        lblSummary.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #334155;");

        var btnClearAll = new Button("Tout désélectionner");
        btnClearAll.getStyleClass().add("btn-action-secondary");
        btnClearAll.setGraphic(fontAwesome.create(FontAwesome.Glyph.UNDO).size(13));
        btnClearAll.setOnAction(e -> clearAllSelections());

        var btnPreview = new Button("Aperçu avant impression");
        btnPreview.getStyleClass().add("btn-action-secondary");
        btnPreview.setGraphic(fontAwesome.create(FontAwesome.Glyph.EYE).size(14));
        btnPreview.setOnAction(e -> openPrintPreview());

        var btnExportPdf = new MenuButton("Exporter en PDF");
        btnExportPdf.getStyleClass().add("btn-action-secondary");
        btnExportPdf.setGraphic(fontAwesome.create(FontAwesome.Glyph.FILE_PDF_ALT).color(Color.web("#dc2626")).size(14));

        var mSinglePdf = new MenuItem("Document PDF unique (regroupé)");
        mSinglePdf.setOnAction(e -> exportSinglePdf());

        var mSeparatePdf = new MenuItem("Dossier de fichiers PDF individuels");
        mSeparatePdf.setOnAction(e -> exportBatchPdf());

        btnExportPdf.getItems().addAll(mSinglePdf, mSeparatePdf);

        var btnPrint = new Button("Imprimer les sélections");
        btnPrint.getStyleClass().add("btn-action-primary");
        btnPrint.setGraphic(fontAwesome.create(FontAwesome.Glyph.PRINT).color(Color.WHITE).size(14));
        btnPrint.setOnAction(e -> print());

        var leftSummaryBox = new HBox(12, lblSummary, btnClearAll);
        leftSummaryBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(leftSummaryBox, Priority.ALWAYS);

        var rightActionsBox = new HBox(10, btnPreview, btnExportPdf, btnPrint);
        rightActionsBox.setAlignment(Pos.CENTER_RIGHT);

        var bottomBar = new HBox(15, leftSummaryBox, rightActionsBox);
        bottomBar.setAlignment(Pos.CENTER);
        bottomBar.setPadding(new Insets(12, 16, 12, 16));
        bottomBar.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-width: 1.5px; -fx-border-radius: 8px; -fx-background-radius: 8px;");

        mainBox.getChildren().add(bottomBar);

        this.setContent(mainBox);

        // Chargement initial des données
        selectionChanged();
    }

    private VBox createColumnCard(String title, FontAwesome.Glyph glyph, Label badge,
                                  TextField searchField, String placeholder,
                                  CheckListView<?> checkListView,
                                  Runnable onSelectAll, Runnable onDeselectAll) {
        FontAwesome fontAwesome = new FontAwesome();
        var card = new VBox(10);
        card.setPadding(new Insets(12));
        card.setStyle("-fx-background-color: #ffffff; -fx-border-color: #cbd5e1; -fx-border-width: 1.5px; " +
                "-fx-border-radius: 8px; -fx-background-radius: 8px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.04), 4, 0, 0, 1);");
        VBox.setVgrow(card, Priority.ALWAYS);

        // Header
        var titleLbl = new Label(title);
        titleLbl.setGraphic(fontAwesome.create(glyph).size(16));
        titleLbl.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        badge.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #64748b; " +
                "-fx-background-color: #f1f5f9; -fx-padding: 2 8 2 8; -fx-background-radius: 12px;");

        var headerBox = new HBox(titleLbl, new Region(), badge);
        HBox.setHgrow(headerBox.getChildren().get(1), Priority.ALWAYS);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        // Search Bar
        searchField.setPromptText(placeholder);
        searchField.getStyleClass().add("search-field");
        HBox.setHgrow(searchField, Priority.ALWAYS);

        var btnClear = new Button();
        btnClear.setGraphic(fontAwesome.create(FontAwesome.Glyph.TIMES_CIRCLE).size(13));
        btnClear.getStyleClass().add("btn-clear-search");
        btnClear.setTooltip(new Tooltip("Effacer le filtre"));
        btnClear.setOnAction(e -> searchField.clear());
        btnClear.visibleProperty().bind(searchField.textProperty().isNotEmpty());
        btnClear.managedProperty().bind(searchField.textProperty().isNotEmpty());

        var searchBar = new HBox(6, searchField, btnClear);
        searchBar.setAlignment(Pos.CENTER_LEFT);

        // List
        GridPane.setVgrow(checkListView, Priority.ALWAYS);
        VBox.setVgrow(checkListView, Priority.ALWAYS);

        // Actions
        var btnAdd = new Button("Tout cocher");
        btnAdd.setGraphic(fontAwesome.create(FontAwesome.Glyph.CHECK_SQUARE_ALT).size(13));
        btnAdd.setOnAction(e -> onSelectAll.run());

        var btnRemove = new Button("Tout décocher");
        btnRemove.setGraphic(fontAwesome.create(FontAwesome.Glyph.MINUS_SQUARE).size(13));
        btnRemove.setOnAction(e -> onDeselectAll.run());

        var actions = new HBox(8, btnAdd, btnRemove);
        actions.setAlignment(Pos.CENTER);

        card.getChildren().addAll(headerBox, searchBar, checkListView, actions);
        return card;
    }

    private void filterGroupes(String query) {
        isUpdatingList = true;
        try {
            String q = (query != null ? query.toLowerCase().trim() : "");
            var filtered = allGroupes.stream().filter(g -> {
                if (q.isEmpty()) return true;
                boolean matchNom = g.getNom() != null && g.getNom().toLowerCase().contains(q);
                boolean matchFiliere = g.getFiliere() != null && (
                        (g.getFiliere().getNom() != null && g.getFiliere().getNom().toLowerCase().contains(q)) ||
                                (g.getFiliere().getAbreviation() != null && g.getFiliere().getAbreviation().toLowerCase().contains(q))
                );
                return matchNom || matchFiliere;
            }).toList();

            groupeCheckListView.getItems().setAll(filtered);
            for (int i = 0; i < filtered.size(); i++) {
                if (selectedGroupes.contains(filtered.get(i))) {
                    groupeCheckListView.getCheckModel().check(i);
                }
            }
        } finally {
            isUpdatingList = false;
        }
    }

    private void filterFormateurs(String query) {
        isUpdatingList = true;
        try {
            String q = (query != null ? query.toLowerCase().trim() : "");
            var filtered = allFormateurs.stream().filter(f -> {
                if (q.isEmpty()) return true;
                boolean matchNom = f.getNom() != null && f.getNom().toLowerCase().contains(q);
                boolean matchPrenom = f.getPrenom() != null && f.getPrenom().toLowerCase().contains(q);
                boolean matchMat = f.getMatricule() != null && f.getMatricule().toLowerCase().contains(q);
                boolean matchType = f.getType() != null && f.getType().toLowerCase().contains(q);
                return matchNom || matchPrenom || matchMat || matchType;
            }).toList();

            formateurCheckListView.getItems().setAll(filtered);
            for (int i = 0; i < filtered.size(); i++) {
                if (selectedFormateurs.contains(filtered.get(i))) {
                    formateurCheckListView.getCheckModel().check(i);
                }
            }
        } finally {
            isUpdatingList = false;
        }
    }

    private void filterEspaces(String query) {
        isUpdatingList = true;
        try {
            String q = (query != null ? query.toLowerCase().trim() : "");
            var filtered = allEspaces.stream().filter(e -> {
                if (q.isEmpty()) return true;
                boolean matchNom = e.getNom() != null && e.getNom().toLowerCase().contains(q);
                boolean matchType = e.getType() != null && e.getType().toLowerCase().contains(q);
                return matchNom || matchType;
            }).toList();

            espaceCheckListView.getItems().setAll(filtered);
            for (int i = 0; i < filtered.size(); i++) {
                if (selectedEspaces.contains(filtered.get(i))) {
                    espaceCheckListView.getCheckModel().check(i);
                }
            }
        } finally {
            isUpdatingList = false;
        }
    }

    private void selectAllGroupes(boolean check) {
        if (check) {
            selectedGroupes.addAll(groupeCheckListView.getItems());
        } else {
            selectedGroupes.removeAll(groupeCheckListView.getItems());
        }
        isUpdatingList = true;
        try {
            if (check) {
                IntStream.range(0, groupeCheckListView.getItems().size()).forEach(groupeCheckListView.getCheckModel()::check);
            } else {
                IntStream.range(0, groupeCheckListView.getItems().size()).forEach(groupeCheckListView.getCheckModel()::clearCheck);
            }
        } finally {
            isUpdatingList = false;
        }
        updateCounters();
    }

    private void selectAllFormateurs(boolean check) {
        if (check) {
            selectedFormateurs.addAll(formateurCheckListView.getItems());
        } else {
            selectedFormateurs.removeAll(formateurCheckListView.getItems());
        }
        isUpdatingList = true;
        try {
            if (check) {
                IntStream.range(0, formateurCheckListView.getItems().size()).forEach(formateurCheckListView.getCheckModel()::check);
            } else {
                IntStream.range(0, formateurCheckListView.getItems().size()).forEach(formateurCheckListView.getCheckModel()::clearCheck);
            }
        } finally {
            isUpdatingList = false;
        }
        updateCounters();
    }

    private void selectAllEspaces(boolean check) {
        if (check) {
            selectedEspaces.addAll(espaceCheckListView.getItems());
        } else {
            selectedEspaces.removeAll(espaceCheckListView.getItems());
        }
        isUpdatingList = true;
        try {
            if (check) {
                IntStream.range(0, espaceCheckListView.getItems().size()).forEach(espaceCheckListView.getCheckModel()::check);
            } else {
                IntStream.range(0, espaceCheckListView.getItems().size()).forEach(espaceCheckListView.getCheckModel()::clearCheck);
            }
        } finally {
            isUpdatingList = false;
        }
        updateCounters();
    }

    private void clearAllSelections() {
        selectedGroupes.clear();
        selectedFormateurs.clear();
        selectedEspaces.clear();

        isUpdatingList = true;
        try {
            IntStream.range(0, groupeCheckListView.getItems().size()).forEach(groupeCheckListView.getCheckModel()::clearCheck);
            IntStream.range(0, formateurCheckListView.getItems().size()).forEach(formateurCheckListView.getCheckModel()::clearCheck);
            IntStream.range(0, espaceCheckListView.getItems().size()).forEach(espaceCheckListView.getCheckModel()::clearCheck);
        } finally {
            isUpdatingList = false;
        }
        updateCounters();
    }

    private void updateCounters() {
        int g = selectedGroupes.size();
        int f = selectedFormateurs.size();
        int e = selectedEspaces.size();
        int total = g + f + e;

        badgeGroupe.setText(g + " sélectionné" + (g > 1 ? "s" : ""));
        badgeGroupe.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; " +
                (g > 0 ? "-fx-text-fill: #059669; -fx-background-color: #ecfdf5;" : "-fx-text-fill: #64748b; -fx-background-color: #f1f5f9;") +
                " -fx-padding: 2 8 2 8; -fx-background-radius: 12px;");

        badgeFormateur.setText(f + " sélectionné" + (f > 1 ? "s" : ""));
        badgeFormateur.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; " +
                (f > 0 ? "-fx-text-fill: #059669; -fx-background-color: #ecfdf5;" : "-fx-text-fill: #64748b; -fx-background-color: #f1f5f9;") +
                " -fx-padding: 2 8 2 8; -fx-background-radius: 12px;");

        badgeEspace.setText(e + " sélectionné" + (e > 1 ? "s" : ""));
        badgeEspace.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; " +
                (e > 0 ? "-fx-text-fill: #059669; -fx-background-color: #ecfdf5;" : "-fx-text-fill: #64748b; -fx-background-color: #f1f5f9;") +
                " -fx-padding: 2 8 2 8; -fx-background-radius: 12px;");

        lblSummary.setText("Total sélectionné : " + total + " emploi(s) du temps (" +
                g + " groupes, " + f + " formateurs, " + e + " espaces)");
    }

    private Window getOwnerWindow() {
        if (getContent() != null && getContent().getScene() != null) {
            return getContent().getScene().getWindow();
        }
        if (groupeCheckListView.getScene() != null) {
            return groupeCheckListView.getScene().getWindow();
        }
        return null;
    }

    private void openPrintPreview() {
        var pages = buildPreviewPages();
        if (pages.isEmpty()) {
            NotificationService.showWarning("Sélection vide", "Veuillez sélectionner au moins un groupe, un formateur ou un espace à prévisualiser.");
            return;
        }

        var dialog = new PrintPreviewDialog(getOwnerWindow(), pages);
        dialog.show();
    }

    private List<PrintPreviewDialog.PreviewPage> buildPreviewPages() {
        List<PrintPreviewDialog.PreviewPage> pages = new ArrayList<>();
        for (var groupe : selectedGroupes) {
            pages.add(new PrintPreviewDialog.PreviewPage("Groupe", groupe.getNom(), () -> new GroupeTimeTablePrint(groupe)));
        }
        for (var formateur : selectedFormateurs) {
            pages.add(new PrintPreviewDialog.PreviewPage("Formateur", formateur.toString(), () -> new FormateurTimeTablePrint(formateur)));
        }
        for (var espace : selectedEspaces) {
            pages.add(new PrintPreviewDialog.PreviewPage("Espace", espace.getNom(), () -> new EspaceTimeTablePrint(espace)));
        }
        return pages;
    }

    @Override
    protected void selectionChanged() {
        allGroupes.clear();
        allFormateurs.clear();
        allEspaces.clear();

        allGroupes.addAll(Database.getInstance().getGroupes());
        allFormateurs.addAll(Database.getInstance().getFormateurs());
        allEspaces.addAll(Database.getInstance().getEspaces());

        filterGroupes(txtSearchGroupe != null ? txtSearchGroupe.getText() : "");
        filterFormateurs(txtSearchFormateur != null ? txtSearchFormateur.getText() : "");
        filterEspaces(txtSearchEspace != null ? txtSearchEspace.getText() : "");

        updateCounters();
    }

    @Override
    public void print() {
        int totalSelected = selectedGroupes.size() + selectedFormateurs.size() + selectedEspaces.size();

        if (totalSelected == 0) {
            NotificationService.showWarning("Sélection vide", "Veuillez sélectionner au moins un groupe, un formateur ou un espace à imprimer.");
            return;
        }

        PrinterJob job = PrinterJob.createPrinterJob();
        if (job == null) {
            NotificationService.showError("Impression impossible", "Aucun service d'impression ou imprimante n'est disponible sur ce système.");
            return;
        }

        // Configuration par défaut : A4 Paysage avec 25pt de marges
        PageLayout pageLayout = job.getPrinter().createPageLayout(Paper.A4, PageOrientation.LANDSCAPE, 25, 25, 25, 25);
        job.getJobSettings().setPageLayout(pageLayout);

        if (job.showPrintDialog(getOwnerWindow())) {
            PageLayout activeLayout = job.getJobSettings().getPageLayout();
            int printedCount = 0;
            boolean errorOccurred = false;

            for (var groupe : selectedGroupes) {
                var tt = new GroupeTimeTablePrint(groupe);
                if (printPageScaled(job, activeLayout, tt)) {
                    printedCount++;
                } else {
                    errorOccurred = true;
                    break;
                }
            }

            if (!errorOccurred) {
                for (var formateur : selectedFormateurs) {
                    var tt = new FormateurTimeTablePrint(formateur);
                    if (printPageScaled(job, activeLayout, tt)) {
                        printedCount++;
                    } else {
                        errorOccurred = true;
                        break;
                    }
                }
            }

            if (!errorOccurred) {
                for (var espace : selectedEspaces) {
                    var tt = new EspaceTimeTablePrint(espace);
                    if (printPageScaled(job, activeLayout, tt)) {
                        printedCount++;
                    } else {
                        errorOccurred = true;
                        break;
                    }
                }
            }

            if (!errorOccurred) {
                job.endJob();
                NotificationService.showSuccess("Impression terminée", printedCount + " emploi(s) du temps envoyé(s) à l'imprimante avec succès.");
            } else {
                job.cancelJob();
                NotificationService.showError("Impression interrompue", "Une erreur est survenue lors de l'envoi vers l'imprimante.");
            }
        }
    }

    private boolean printPageScaled(PrinterJob job, PageLayout pageLayout, TimeTablePrint tt) {
        double targetWidth = 730.0;
        tt.setPrefWidth(targetWidth);

        tt.applyCss();
        tt.layout();

        double printableWidth = pageLayout.getPrintableWidth();
        double printableHeight = pageLayout.getPrintableHeight();

        double contentWidth = Math.max(targetWidth, tt.prefWidth(-1));
        double contentHeight = Math.max(500.0, tt.prefHeight(contentWidth));

        double scaleX = printableWidth / contentWidth;
        double scaleY = printableHeight / contentHeight;
        double scale = Math.min(scaleX, scaleY) * 0.98;

        double scaledWidth = contentWidth * scale;
        double scaledHeight = contentHeight * scale;
        double offsetX = Math.max(0, (printableWidth - scaledWidth) / 2.0);
        double offsetY = Math.max(0, (printableHeight - scaledHeight) / 2.0);

        var translate = new Translate(offsetX, offsetY);
        var scaleTransform = new Scale(scale, scale, 0, 0);

        tt.getTransforms().addAll(translate, scaleTransform);

        boolean result = job.printPage(pageLayout, tt);

        tt.getTransforms().clear();
        return result;
    }

    private void exportSinglePdf() {
        var pages = buildPreviewPages();
        if (pages.isEmpty()) {
            NotificationService.showWarning("Sélection vide", "Veuillez sélectionner au moins un groupe, un formateur ou un espace à exporter.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer les emplois du temps en PDF");
        fileChooser.setInitialFileName("Emplois_du_temps.pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Document PDF (*.pdf)", "*.pdf"));

        File target = fileChooser.showSaveDialog(getOwnerWindow());
        if (target != null) {
            try {
                List<BufferedImage> images = new ArrayList<>();
                for (var page : pages) {
                    TimeTablePrint tt = page.supplier().get();
                    images.add(PdfExporter.renderToImage(tt));
                }
                PdfExporter.writeImagesToPdf(target, images);
                NotificationService.showSuccess("Exportation PDF réussie", pages.size() + " emploi(s) du temps exporté(s) dans : " + target.getName());
            } catch (Exception ex) {
                NotificationService.showError("Erreur d'exportation", "Impossible d'exporter le fichier PDF : " + ex.getMessage());
            }
        }
    }

    private void exportBatchPdf() {
        var pages = buildPreviewPages();
        if (pages.isEmpty()) {
            NotificationService.showWarning("Sélection vide", "Veuillez sélectionner au moins un groupe, un formateur ou un espace à exporter.");
            return;
        }

        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle("Sélectionner le dossier de destination pour les fichiers PDF");

        File targetDir = dirChooser.showDialog(getOwnerWindow());
        if (targetDir != null && targetDir.isDirectory()) {
            try {
                int count = 0;
                for (var page : pages) {
                    String safeName = page.title().replaceAll("[\\\\/:*?\"<>|]", "_").trim();
                    File file = new File(targetDir, page.category() + "_" + safeName + ".pdf");
                    TimeTablePrint tt = page.supplier().get();
                    BufferedImage img = PdfExporter.renderToImage(tt);
                    PdfExporter.writeImagesToPdf(file, List.of(img));
                    count++;
                }
                NotificationService.showSuccess("Exportation par lot terminée", count + " fichiers PDF créés avec succès dans : " + targetDir.getName());
            } catch (Exception ex) {
                NotificationService.showError("Erreur d'exportation", "Une erreur est survenue lors de l'exportation : " + ex.getMessage());
            }
        }
    }
}
