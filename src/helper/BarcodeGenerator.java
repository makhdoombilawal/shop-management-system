package helper;

import com.google.zxing.*;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import java.io.File;
import java.nio.file.*;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Font;

/**
 * Utility class for generating barcode images.
 * @author Bilawal
 */
public class BarcodeGenerator {

    /**
     * Generate barcode image and save to project's barcodes directory
     * Uses relative path for cross-platform compatibility
     * 
     * @param barcodeData The barcode number to encode
     * @return Full path to generated barcode image, or null if error
     */
    public static String generateBarcodeImage(String barcodeData) {
        try {
            int width = 300;
            int height = 100;
            String imageFormat = "png";

            // Use relative path - works on any system
            String projectDir = System.getProperty("user.dir");
            File dir = new File(projectDir, "barcodes");
            
            // Create directory if it doesn't exist
            if (!dir.exists()) {
                boolean created = dir.mkdirs();
                if (!created) {
                    util.LoggerUtil.logError("❌ Failed to create barcodes directory: " + dir.getAbsolutePath(), null);
                    return null;
                }
            }

            // Output file path
            Path filePath = Paths.get(dir.getAbsolutePath(), barcodeData + ".png");

            // Generate barcode
            BitMatrix bitMatrix = new MultiFormatWriter().encode(
                barcodeData, BarcodeFormat.CODE_128, width, height
            );
            MatrixToImageWriter.writeToPath(bitMatrix, imageFormat, filePath);

            util.LoggerUtil.logInfo("✅ Barcode image generated: " + filePath);
            return filePath.toString();
        } catch (Exception e) {
            e.printStackTrace();
            util.LoggerUtil.logError("❌ Error generating barcode: " + e.getMessage(), null);
            return null;
        }
    }

    /**
     * Print barcode image to the default printer
     * 
     * @param barcodeNumber The barcode number to print
     */
    public static void printBarcode(String barcodeNumber) {
        try {
            String imagePath = generateBarcodeImage(barcodeNumber);
            if (imagePath == null) return;

            final java.awt.Image img = javax.imageio.ImageIO.read(new File(imagePath));
            
            java.awt.print.PrinterJob job = java.awt.print.PrinterJob.getPrinterJob();
            job.setPrintable(new java.awt.print.Printable() {
                @Override
                public int print(Graphics graphics, java.awt.print.PageFormat pageFormat, int pageIndex) 
                        throws java.awt.print.PrinterException {
                    if (pageIndex > 0) {
                        return NO_SUCH_PAGE;
                    }

                    Graphics2D g2d = (Graphics2D) graphics;
                    g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

                    // Center barcode on the page (or top-left for standard labels)
                    // Regular 1D barcode size on label
                    g2d.drawImage(img, 10, 10, 200, 70, null);
                    g2d.setFont(new Font("Monospaced", Font.PLAIN, 10));
                    g2d.drawString(barcodeNumber, 50, 90);

                    return PAGE_EXISTS;
                }
            });

            if (job.printDialog()) {
                job.print();
                util.LoggerUtil.logInfo("✅ Barcode sent to printer: " + barcodeNumber);
            }

        } catch (Exception e) {
            e.printStackTrace();
            util.LoggerUtil.logError("❌ Error printing barcode: " + e.getMessage(), null);
        }
    }
}
