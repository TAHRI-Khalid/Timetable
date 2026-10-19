package tk.tahri.timetable.print;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.PageLayout;
import javafx.print.PageOrientation;
import javafx.print.Paper;
import javafx.print.PrinterJob;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.controlsfx.glyphfont.FontAwesome;
import tk.tahri.timetable.tools.NotificationService;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class PrintPreviewDialog extends Stage {

    public record PreviewPage(String category, String title, Supplier<TimeTablePrint> supplier) {}

    private final List<PreviewPage> pages;
    private int currentIndex = 0;

    private static final double SHEET_WIDTH = 842.0;  // A4 Paysage en points
    private static final double SHEET_HEIGHT = 595.0;
    private static final double MARGIN = 25.0;

    private final Pane sheet;
    private final Group sheetGroup;
    private final ScrollPane scrollPane;

    private final Label lblPageIndicator;
    private final Label lblItemTitle;
    private final Label lblZoom;
    private final Button btnPrev;
    private final Button btnNext;

    private double zoom = 0.95;

    public PrintPreviewDialog(Window owner, List<PreviewPage> pages) {
        this.pages = Objects.requireNonNull(pages);

        initOwner(owner);
        initModality(Modality.APPLICATION_MODAL);
        setTitle("Aperçu avant impression (" + pages.size() + " pages)");

        FontAwesome fontAwesome = new FontAwesome();

        // 1. Barre d'outils supérieure
        var toolBar = new HBox(12);
        toolBar.setAlignment(Pos.CENTER_LEFT);
        toolBar.setPadding(new Insets(10, 16, 10, 16));
        toolBar.setStyle("-fx-background-color: #ffffff; -fx-border-color: #e2e8f0; -fx-border-width: 0 0 1 0;");

        // Contrôles de navigation
        btnPrev = new Button();
        btnPrev.setGraphic(fontAwesome.create(FontAwesome.Glyph.CHEVRON_LEFT).size(13));
        btnPrev.setTooltip(new Tooltip("Page précédente (Touche ←)"));
        btnPrev.setOnAction(e -> navigate(-1));

        btnNext = new Button();
        btnNext.setGraphic(fontAwesome.create(FontAwesome.Glyph.CHEVRON_RIGHT).size(13));
        btnNext.setTooltip(new Tooltip("Page suivante (Touche →)"));
        btnNext.setOnAction(e -> navigate(1));

        lblPageIndicator = new Label();
        lblPageIndicator.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #1e293b;");

        lblItemTitle = new Label();
        lblItemTitle.setStyle("-fx-background-color: #eff6ff; -fx-text-fill: #1d4ed8; -fx-font-weight: bold; " +
                "-fx-padding: 4 10 4 10; -fx-background-radius: 6px; -fx-border-color: #bfdbfe; -fx-border-radius: 6px;");

        var navBox = new HBox(8, btnPrev, lblPageIndicator, btnNext, lblItemTitle);
        navBox.setAlignment(Pos.CENTER_LEFT);

        // Contrôles de Zoom
        var btnZoomOut = new Button();
        btnZoomOut.setGraphic(fontAwesome.create(FontAwesome.Glyph.SEARCH_MINUS).size(13));
        btnZoomOut.setTooltip(new Tooltip("Zoom arrière"));
        btnZoomOut.setOnAction(e -> applyZoom(zoom - 0.15));

        var btnZoomIn = new Button();
        btnZoomIn.setGraphic(fontAwesome.create(FontAwesome.Glyph.SEARCH_PLUS).size(13));
        btnZoomIn.setTooltip(new Tooltip("Zoom avant"));
        btnZoomIn.setOnAction(e -> applyZoom(zoom + 0.15));

        var btnZoomReset = new Button("100%");
        btnZoomReset.setTooltip(new Tooltip("Échelle 1:1"));
        btnZoomReset.setOnAction(e -> applyZoom(1.0));

        var btnZoomFit = new Button("Ajuster");
        btnZoomFit.setTooltip(new Tooltip("Ajuster à l'écran"));
        btnZoomFit.setOnAction(e -> fitToWindow());

        lblZoom = new Label("95%");
        lblZoom.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px;");

        var zoomBox = new HBox(6, btnZoomOut, btnZoomIn, btnZoomReset, btnZoomFit, lblZoom);
        zoomBox.setAlignment(Pos.CENTER);
        HBox.setHgrow(zoomBox, Priority.ALWAYS);

        // Boutons d'action
        var btnExportCurrentPdf = new Button("PDF (page)");
        btnExportCurrentPdf.setGraphic(fontAwesome.create(FontAwesome.Glyph.FILE_PDF_ALT).color(Color.web("#dc2626")).size(13));
        btnExportCurrentPdf.setTooltip(new Tooltip("Exporter la page affichée en document PDF"));
        btnExportCurrentPdf.setOnAction(e -> exportCurrentPageToPdf());

        var btnExportAllPdf = new Button("PDF (tout)");
        btnExportAllPdf.setGraphic(fontAwesome.create(FontAwesome.Glyph.FILE_PDF_ALT).color(Color.web("#dc2626")).size(13));
        btnExportAllPdf.setTooltip(new Tooltip("Exporter toutes les pages (" + pages.size() + ") dans un seul document PDF"));
        btnExportAllPdf.setOnAction(e -> exportAllPagesToPdf());

        var btnPrintCurrent = new Button("Imprimer page");
        btnPrintCurrent.setGraphic(fontAwesome.create(FontAwesome.Glyph.PRINT).size(13));
        btnPrintCurrent.setOnAction(e -> printCurrentPage());

        var btnPrintAll = new Button("Imprimer tout (" + pages.size() + ")");
        btnPrintAll.getStyleClass().add("btn-action-primary");
        btnPrintAll.setGraphic(fontAwesome.create(FontAwesome.Glyph.PRINT).color(Color.WHITE).size(13));
        btnPrintAll.setOnAction(e -> printAllPages());

        var btnClose = new Button("Fermer");
        btnClose.setOnAction(e -> close());

        var actionsBox = new HBox(8, btnExportCurrentPdf, btnExportAllPdf, btnPrintCurrent, btnPrintAll, btnClose);
        actionsBox.setAlignment(Pos.CENTER_RIGHT);

        toolBar.getChildren().addAll(navBox, zoomBox, actionsBox);

        // 2. Feuille A4 virtuelle
        sheet = new Pane();
        sheet.setPrefSize(SHEET_WIDTH, SHEET_HEIGHT);
        sheet.setMinSize(SHEET_WIDTH, SHEET_HEIGHT);
        sheet.setMaxSize(SHEET_WIDTH, SHEET_HEIGHT);
        sheet.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 4px; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.22), 14, 0, 0, 4); " +
                "-fx-border-color: #cbd5e1; -fx-border-width: 1px; -fx-border-radius: 4px;");

        sheetGroup = new Group(sheet);

        var sheetContainer = new StackPane(sheetGroup);
        sheetContainer.setPadding(new Insets(30));
        sheetContainer.setAlignment(Pos.CENTER);
        sheetContainer.setStyle("-fx-background-color: #e2e8f0;");

        scrollPane = new ScrollPane(sheetContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background-color: #e2e8f0; -fx-background: #e2e8f0;");

        var root = new BorderPane();
        root.setTop(toolBar);
        root.setCenter(scrollPane);

        Scene scene = new Scene(root, 1080, 760);
        var cssUrl = getClass().getResource("/css/style.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }

        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.LEFT || event.getCode() == KeyCode.PAGE_UP) {
                navigate(-1);
            } else if (event.getCode() == KeyCode.RIGHT || event.getCode() == KeyCode.PAGE_DOWN) {
                navigate(1);
            } else if (event.getCode() == KeyCode.ESCAPE) {
                close();
            }
        });

        setScene(scene);

        applyZoom(zoom);

        if (!pages.isEmpty()) {
            renderCurrentPage();
        }

        Platform.runLater(this::fitToWindow);
    }

    private void navigate(int delta) {
        int newIndex = currentIndex + delta;
        if (newIndex >= 0 && newIndex < pages.size()) {
            currentIndex = newIndex;
            renderCurrentPage();
        }
    }

    private void renderCurrentPage() {
        if (pages.isEmpty()) return;

        var page = pages.get(currentIndex);
        lblPageIndicator.setText("Page " + (currentIndex + 1) + " / " + pages.size());
        lblItemTitle.setText("[" + page.category() + "] " + page.title());

        btnPrev.setDisable(currentIndex == 0);
        btnNext.setDisable(currentIndex == pages.size() - 1);

        // Instanciation de l'emploi du temps
        TimeTablePrint tt = page.supplier().get();
        double targetWidth = 730.0;
        tt.setPrefWidth(targetWidth);

        tt.applyCss();
        tt.layout();

        double printableWidth = SHEET_WIDTH - (2 * MARGIN);
        double printableHeight = SHEET_HEIGHT - (2 * MARGIN);

        double contentWidth = Math.max(targetWidth, tt.prefWidth(-1));
        double contentHeight = Math.max(500.0, tt.prefHeight(contentWidth));

        double scaleX = printableWidth / contentWidth;
        double scaleY = printableHeight / contentHeight;
        double scale = Math.min(scaleX, scaleY) * 0.98;

        double scaledW = contentWidth * scale;
        double scaledH = contentHeight * scale;
        double offsetX = MARGIN + Math.max(0, (printableWidth - scaledW) / 2.0);
        double offsetY = MARGIN + Math.max(0, (printableHeight - scaledH) / 2.0);

        var translate = new Translate(offsetX, offsetY);
        var scaleTransform = new Scale(scale, scale, 0, 0);

        tt.getTransforms().setAll(translate, scaleTransform);

        sheet.getChildren().setAll(tt);
    }

    private void applyZoom(double newZoom) {
        zoom = Math.max(0.35, Math.min(2.0, newZoom));
        sheetGroup.setScaleX(zoom);
        sheetGroup.setScaleY(zoom);
        lblZoom.setText(Math.round(zoom * 100) + "%");
    }

    private void fitToWindow() {
        double viewW = scrollPane.getViewportBounds().getWidth() - 70;
        double viewH = scrollPane.getViewportBounds().getHeight() - 70;
        if (viewW > 150 && viewH > 150) {
            double fitZ = Math.min(viewW / SHEET_WIDTH, viewH / SHEET_HEIGHT);
            applyZoom(Math.max(0.4, fitZ));
        }
    }

    private void printCurrentPage() {
        if (pages.isEmpty()) return;
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job == null) {
            NotificationService.showError("Impression impossible", "Aucune imprimante disponible.");
            return;
        }

        PageLayout pageLayout = job.getPrinter().createPageLayout(Paper.A4, PageOrientation.LANDSCAPE, MARGIN, MARGIN, MARGIN, MARGIN);
        job.getJobSettings().setPageLayout(pageLayout);

        if (job.showPrintDialog(this)) {
            PageLayout activeLayout = job.getJobSettings().getPageLayout();
            TimeTablePrint tt = pages.get(currentIndex).supplier().get();
            if (printPageScaled(job, activeLayout, tt)) {
                job.endJob();
                NotificationService.showSuccess("Impression réussie", "La page a été imprimée.");
            } else {
                job.cancelJob();
                NotificationService.showError("Erreur", "L'impression de la page a échoué.");
            }
        }
    }

    private void printAllPages() {
        if (pages.isEmpty()) return;
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job == null) {
            NotificationService.showError("Impression impossible", "Aucune imprimante disponible.");
            return;
        }

        PageLayout pageLayout = job.getPrinter().createPageLayout(Paper.A4, PageOrientation.LANDSCAPE, MARGIN, MARGIN, MARGIN, MARGIN);
        job.getJobSettings().setPageLayout(pageLayout);

        if (job.showPrintDialog(this)) {
            PageLayout activeLayout = job.getJobSettings().getPageLayout();
            int count = 0;
            boolean ok = true;
            for (var page : pages) {
                TimeTablePrint tt = page.supplier().get();
                if (printPageScaled(job, activeLayout, tt)) {
                    count++;
                } else {
                    ok = false;
                    break;
                }
            }
            if (ok) {
                job.endJob();
                NotificationService.showSuccess("Impression terminée", count + " page(s) imprimée(s) avec succès.");
                close();
            } else {
                job.cancelJob();
                NotificationService.showError("Erreur", "L'impression a été interrompue.");
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

        tt.getTransforms().setAll(translate, scaleTransform);

        boolean result = job.printPage(pageLayout, tt);

        tt.getTransforms().clear();
        return result;
    }

    private void exportCurrentPageToPdf() {
        if (pages.isEmpty()) return;
        var page = pages.get(currentIndex);

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer l'emploi du temps en PDF");
        String safeName = page.title().replaceAll("[\\\\/:*?\"<>|]", "_").trim();
        fileChooser.setInitialFileName(page.category() + "_" + safeName + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Document PDF (*.pdf)", "*.pdf"));

        File target = fileChooser.showSaveDialog(this);
        if (target != null) {
            try {
                TimeTablePrint tt = page.supplier().get();
                BufferedImage img = PdfExporter.renderToImage(tt);
                PdfExporter.writeImagesToPdf(target, List.of(img));
                NotificationService.showSuccess("Exportation PDF réussie", "Fichier enregistré : " + target.getName());
            } catch (Exception ex) {
                NotificationService.showError("Erreur d'exportation", "Impossible d'exporter le fichier PDF : " + ex.getMessage());
            }
        }
    }

    private void exportAllPagesToPdf() {
        if (pages.isEmpty()) return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer tous les emplois du temps en PDF");
        fileChooser.setInitialFileName("Emplois_du_temps.pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Document PDF (*.pdf)", "*.pdf"));

        File target = fileChooser.showSaveDialog(this);
        if (target != null) {
            try {
                List<BufferedImage> images = new ArrayList<>();
                for (var page : pages) {
                    TimeTablePrint tt = page.supplier().get();
                    images.add(PdfExporter.renderToImage(tt));
                }
                PdfExporter.writeImagesToPdf(target, images);
                NotificationService.showSuccess("Exportation PDF réussie", pages.size() + " emplois du temps exportés dans : " + target.getName());
            } catch (Exception ex) {
                NotificationService.showError("Erreur d'exportation", "Impossible d'exporter le document PDF : " + ex.getMessage());
            }
        }
    }
}
