package helper;

import com.google.zxing.*;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import util.BarcodeValidationUtil;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Hashtable;

/**
 * Supermarket Multi-Format Barcode Generator Utility.
 * Supports CODE128, EAN-13, EAN-8, UPC-A, CODE39, and QR Code generation.
 *
 * @author Shop Management System
 */
public class BarcodeGenerator {

    /**
     * Default barcode generation overload (CODE128, 300x100).
     */
    public static String generateBarcodeImage(String barcodeData) {
        return generateBarcodeImage(barcodeData, "CODE128", 300, 100);
    }

    /**
     * Multi-format barcode image generator saving PNG to barcodes directory.
     */
    public static String generateBarcodeImage(String barcodeData, String barcodeType, int width, int height) {
        try {
            BufferedImage image = generateBarcodeBufferedImage(barcodeData, barcodeType, width, height);
            if (image == null) return null;

            String normalized = BarcodeValidationUtil.normalizeBarcode(barcodeData);
            String projectDir = System.getProperty("user.dir");
            File dir = new File(projectDir, "barcodes");

            if (!dir.exists() && !dir.mkdirs()) {
                util.LoggerUtil.logError("❌ Failed to create barcodes directory: " + dir.getAbsolutePath(), null);
                return null;
            }

            Path filePath = Paths.get(dir.getAbsolutePath(), normalized + ".png");
            javax.imageio.ImageIO.write(image, "png", filePath.toFile());

            util.LoggerUtil.logInfo("✅ Barcode image generated (" + barcodeType + "): " + filePath);
            return filePath.toString();

        } catch (Exception e) {
            util.LoggerUtil.logError(BarcodeGenerator.class, "Error generating barcode image for: " + barcodeData, e);
            return null;
        }
    }

    /**
     * Multi-format in-memory BufferedImage barcode renderer.
     */
    public static BufferedImage generateBarcodeBufferedImage(String barcodeData, String barcodeType, int width, int height) {
        try {
            String normalized = BarcodeValidationUtil.normalizeBarcode(barcodeData);
            BarcodeFormat format = mapBarcodeFormat(barcodeType);

            Hashtable<Object, Object> hints = new Hashtable<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            try {
                java.lang.reflect.Field marginField = EncodeHintType.class.getField("MARGIN");
                Object marginKey = marginField.get(null);
                if (marginKey != null) {
                    hints.put(marginKey, 1);
                }
            } catch (Throwable ignored) {}

            if (format == BarcodeFormat.QR_CODE) {
                hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            }

            @SuppressWarnings("rawtypes")
            BitMatrix bitMatrix = new MultiFormatWriter().encode(
                    normalized, format, width, height, (Hashtable) hints
            );

            return MatrixToImageWriter.toBufferedImage(bitMatrix);

        } catch (Exception e) {
            util.LoggerUtil.logError(BarcodeGenerator.class, "Error rendering BufferedImage (" + barcodeType + ") for: " + barcodeData, e);
            return null;
        }
    }

    /**
     * Map barcode type string to ZXing BarcodeFormat enum.
     */
    public static BarcodeFormat mapBarcodeFormat(String typeStr) {
        if (typeStr == null) return BarcodeFormat.CODE_128;
        switch (typeStr.trim().toUpperCase()) {
            case "EAN13":
            case "EAN-13":
                return BarcodeFormat.EAN_13;
            case "EAN8":
            case "EAN-8":
                return BarcodeFormat.EAN_8;
            case "UPCA":
            case "UPC-A":
                return BarcodeFormat.UPC_A;
            case "UPCE":
            case "UPC-E":
                return BarcodeFormat.UPC_E;
            case "CODE39":
            case "CODE-39":
                return BarcodeFormat.CODE_39;
            case "QR":
            case "QRCODE":
            case "QR_CODE":
                return BarcodeFormat.QR_CODE;
            case "CODE128":
            case "CODE-128":
            default:
                return BarcodeFormat.CODE_128;
        }
    }

    /**
     * Print barcode image to default printer.
     */
    public static void printBarcode(String barcodeNumber) {
        printBarcode(barcodeNumber, "CODE128");
    }

    /**
     * Print barcode with specified format to default printer.
     */
    public static void printBarcode(String barcodeNumber, String barcodeType) {
        try {
            String imagePath = generateBarcodeImage(barcodeNumber, barcodeType, 300, 100);
            if (imagePath == null) return;

            final Image img = javax.imageio.ImageIO.read(new File(imagePath));
            java.awt.print.PrinterJob job = java.awt.print.PrinterJob.getPrinterJob();

            job.setPrintable((graphics, pageFormat, pageIndex) -> {
                if (pageIndex > 0) return java.awt.print.Printable.NO_SUCH_PAGE;

                Graphics2D g2d = (Graphics2D) graphics;
                g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
                g2d.drawImage(img, 10, 10, 200, 70, null);
                g2d.setFont(new Font("Monospaced", Font.PLAIN, 10));
                g2d.drawString(barcodeNumber, 50, 90);

                return java.awt.print.Printable.PAGE_EXISTS;
            });

            if (job.printDialog()) {
                job.print();
                util.LoggerUtil.logInfo("✅ Barcode sent to printer: " + barcodeNumber);
            }
        } catch (Exception e) {
            util.LoggerUtil.logError(BarcodeGenerator.class, "Error printing barcode: " + barcodeNumber, e);
        }
    }
}
