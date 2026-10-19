package tk.tahri.timetable.print;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

/**
 * Moteur d'exportation PDF natif pour les emplois du temps.
 * Génère des documents PDF conformes à la norme ISO PDF-1.4 avec compression FlateDecode sans perte.
 */
public final class PdfExporter {

    public static final double A4_WIDTH = 841.89;   // A4 Paysage en points
    public static final double A4_HEIGHT = 595.28;
    private static final double MARGIN = 25.0;
    private static final double DPI_SCALE = 2.0;     // Qualité d'exportation 2x (haute résolution)

    private PdfExporter() {}

    /**
     * Effectue le rendu graphique d'un emploi du temps sous forme d'image haute résolution.
     */
    public static BufferedImage renderToImage(TimeTablePrint tt) {
        double targetWidth = 730.0;
        tt.setPrefWidth(targetWidth);
        tt.applyCss();
        tt.layout();

        double printableWidth = A4_WIDTH - (2 * MARGIN);
        double printableHeight = A4_HEIGHT - (2 * MARGIN);

        double contentWidth = Math.max(targetWidth, tt.prefWidth(-1));
        double contentHeight = Math.max(500.0, tt.prefHeight(contentWidth));

        double scaleX = printableWidth / contentWidth;
        double scaleY = printableHeight / contentHeight;
        double fitScale = Math.min(scaleX, scaleY) * 0.98;

        double scaledW = contentWidth * fitScale;
        double scaledH = contentHeight * fitScale;
        double offsetX = MARGIN + Math.max(0, (printableWidth - scaledW) / 2.0);
        double offsetY = MARGIN + Math.max(0, (printableHeight - scaledH) / 2.0);

        var translate = new Translate(offsetX, offsetY);
        var scaleTransform = new Scale(fitScale, fitScale, 0, 0);

        tt.getTransforms().setAll(translate, scaleTransform);

        Pane sheet = new Pane(tt);
        sheet.setPrefSize(A4_WIDTH, A4_HEIGHT);
        sheet.setMinSize(A4_WIDTH, A4_HEIGHT);
        sheet.setMaxSize(A4_WIDTH, A4_HEIGHT);
        sheet.setStyle("-fx-background-color: #ffffff;");
        sheet.applyCss();
        sheet.layout();

        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.WHITE);
        params.setTransform(new Scale(DPI_SCALE, DPI_SCALE));

        int pxW = (int) Math.round(A4_WIDTH * DPI_SCALE);
        int pxH = (int) Math.round(A4_HEIGHT * DPI_SCALE);

        WritableImage fxImage = new WritableImage(pxW, pxH);
        sheet.snapshot(params, fxImage);

        tt.getTransforms().clear();
        return SwingFXUtils.fromFXImage(fxImage, null);
    }

    /**
     * Génère un fichier PDF (unitaire ou multipage) à partir d'une liste d'images.
     */
    public static void writeImagesToPdf(File targetFile, List<BufferedImage> images) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(targetFile);
             BufferedOutputStream bos = new BufferedOutputStream(fos)) {

            CountingOutputStream cos = new CountingOutputStream(bos);
            List<Long> offsets = new ArrayList<>();

            // 1. En-tête standard PDF-1.4
            cos.write("%PDF-1.4\n%\u00e2\u00e3\u00cf\u00d3\n".getBytes(StandardCharsets.US_ASCII));

            int pageCount = images.size();

            // Objet 1 : Catalog
            offsets.add(cos.getCount());
            cos.write("1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n".getBytes(StandardCharsets.US_ASCII));

            // Objet 2 : Pages Root
            offsets.add(cos.getCount());
            StringBuilder kids = new StringBuilder();
            for (int i = 0; i < pageCount; i++) {
                int pageObjId = 3 + (i * 3);
                kids.append(pageObjId).append(" 0 R ");
            }
            String pagesObj = String.format(Locale.US,
                    "2 0 obj\n<< /Type /Pages /Kids [ %s] /Count %d >>\nendobj\n",
                    kids, pageCount);
            cos.write(pagesObj.getBytes(StandardCharsets.US_ASCII));

            // Écriture de chaque page et de son image
            for (int i = 0; i < pageCount; i++) {
                BufferedImage img = images.get(i);
                int pageObjId = 3 + (i * 3);
                int contentObjId = pageObjId + 1;
                int imageObjId = pageObjId + 2;

                // Objet Page
                offsets.add(cos.getCount());
                String pageEntry = String.format(Locale.US,
                        "%d 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 %.2f %.2f] " +
                                "/Resources << /ProcSet [/PDF /ImageC] /XObject << /Im1 %d 0 R >> >> " +
                                "/Contents %d 0 R >>\nendobj\n",
                        pageObjId, A4_WIDTH, A4_HEIGHT, imageObjId, contentObjId);
                cos.write(pageEntry.getBytes(StandardCharsets.US_ASCII));

                // Objet Content Stream
                offsets.add(cos.getCount());
                String contentStream = String.format(Locale.US,
                        "q %.2f 0 0 %.2f 0 0 cm /Im1 Do Q\n", A4_WIDTH, A4_HEIGHT);
                byte[] contentBytes = contentStream.getBytes(StandardCharsets.US_ASCII);
                String contentObj = String.format(Locale.US,
                        "%d 0 obj\n<< /Length %d >>\nstream\n%sendstream\nendobj\n",
                        contentObjId, contentBytes.length, contentStream);
                cos.write(contentObj.getBytes(StandardCharsets.US_ASCII));

                // Objet Image XObject (FlateDecode sans perte)
                offsets.add(cos.getCount());
                int w = img.getWidth();
                int h = img.getHeight();
                byte[] compressedRgb = compressRgb(img, w, h);

                String imageHeader = String.format(Locale.US,
                        "%d 0 obj\n<< /Type /XObject /Subtype /Image /Width %d /Height %d " +
                                "/ColorSpace /DeviceRGB /BitsPerComponent 8 /Filter /FlateDecode /Length %d >>\nstream\n",
                        imageObjId, w, h, compressedRgb.length);
                cos.write(imageHeader.getBytes(StandardCharsets.US_ASCII));
                cos.write(compressedRgb);
                cos.write("\nendstream\nendobj\n".getBytes(StandardCharsets.US_ASCII));
            }

            // Table de références croisées (XREF)
            long xrefOffset = cos.getCount();
            int totalObjects = 2 + (pageCount * 3);
            StringBuilder xref = new StringBuilder();
            xref.append("xref\n0 ").append(totalObjects + 1).append("\n");
            xref.append("0000000000 65535 f \n");
            for (Long offset : offsets) {
                xref.append(String.format(Locale.US, "%010d 00000 n \n", offset));
            }

            // Trailer et fin du document
            xref.append(String.format(Locale.US,
                    "trailer\n<< /Size %d /Root 1 0 R >>\nstartxref\n%d\n%%%%EOF\n",
                    totalObjects + 1, xrefOffset));

            cos.write(xref.toString().getBytes(StandardCharsets.US_ASCII));
            cos.flush();
        }
    }

    private static byte[] compressRgb(BufferedImage img, int w, int h) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(w * h);
        Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
        try (DeflaterOutputStream dos = new DeflaterOutputStream(baos, deflater)) {
            byte[] row = new byte[w * 3];
            for (int y = 0; y < h; y++) {
                int idx = 0;
                for (int x = 0; x < w; x++) {
                    int c = img.getRGB(x, y);
                    row[idx++] = (byte) ((c >> 16) & 0xFF);
                    row[idx++] = (byte) ((c >> 8) & 0xFF);
                    row[idx++] = (byte) (c & 0xFF);
                }
                dos.write(row, 0, idx);
            }
            dos.finish();
        }
        return baos.toByteArray();
    }

    /**
     * Flux pour suivre la position exacte en octets.
     */
    private static class CountingOutputStream extends FilterOutputStream {
        private long count = 0;

        public CountingOutputStream(OutputStream out) {
            super(out);
        }

        public long getCount() {
            return count;
        }

        @Override
        public void write(int b) throws IOException {
            out.write(b);
            count++;
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            out.write(b, off, len);
            count += len;
        }
    }
}
