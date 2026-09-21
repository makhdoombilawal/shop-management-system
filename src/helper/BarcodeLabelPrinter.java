package helper;

import models.entity.ProductEntity;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

/**
 * Supermarket Barcode Label Designer & Batch Printer.
 * Renders thermal stickers, multi-up label grids, and print previews.
 *
 * @author Shop Management System
 */
public class BarcodeLabelPrinter {

    /**
     * Print batch labels for a product.
     *
     * @param product       Product details (Name, Price, SKU, Unit, Brand)
     * @param barcodeNumber Barcode number string
     * @param barcodeType   Barcode format type (e.g. CODE128, EAN13)
     * @param labelCount    Number of labels to print
     * @param parentComponent Parent frame for dialogs
     */
    public static void printProductLabels(ProductEntity product, String barcodeNumber, String barcodeType, int labelCount, Component parentComponent) {
        if (product == null || barcodeNumber == null || barcodeNumber.trim().isEmpty()) {
            JOptionPane.showMessageDialog(parentComponent, "❌ Invalid product or barcode for printing.", "Print Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (labelCount <= 0) {
            JOptionPane.showMessageDialog(parentComponent, "❌ Label count must be at least 1.", "Print Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        PrinterJob job = PrinterJob.getPrinterJob();
        if (job.getPrintService() == null) {
            JOptionPane.showMessageDialog(parentComponent, "⚠️ No print service detected. Please install or select a valid printer.", "Printer Not Available", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Show print preview modal dialog before printing
        showPrintPreviewDialog(product, barcodeNumber, barcodeType, labelCount, parentComponent, job);
    }

    /**
     * Show Label Print Preview Modal Dialog.
     */
    private static void showPrintPreviewDialog(ProductEntity product, String barcodeNumber, String barcodeType, int labelCount, Component parentComponent, PrinterJob job) {
        JDialog previewDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(parentComponent), "Barcode Label Print Preview (" + labelCount + " Labels)", true);
        previewDialog.setSize(550, 650);
        previewDialog.setLocationRelativeTo(parentComponent);
        previewDialog.setLayout(new BorderLayout());

        // Header info
        JPanel infoPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Product Label Details"),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        infoPanel.add(new JLabel("Product Name:"));
        infoPanel.add(new JLabel(product.getName()));
        infoPanel.add(new JLabel("Barcode Number:"));
        infoPanel.add(new JLabel(barcodeNumber + " (" + (barcodeType != null ? barcodeType : "CODE128") + ")"));
        infoPanel.add(new JLabel("Sale Price / Unit:"));
        infoPanel.add(new JLabel(String.format("Rs. %.2f / %s", product.getSellPrice() != null ? product.getSellPrice() : 0.0, product.getUnit() != null ? product.getUnit() : "Piece")));
        infoPanel.add(new JLabel("Quantity to Print:"));
        infoPanel.add(new JLabel(labelCount + " Label(s)"));

        // Live Label Preview Panel
        JPanel labelCanvas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                renderSingleLabel(g2d, product, barcodeNumber, barcodeType, 20, 20, 280, 160);
            }
        };
        labelCanvas.setPreferredSize(new Dimension(320, 200));
        labelCanvas.setBackground(Color.WHITE);
        labelCanvas.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(15, 15, 15, 15),
                BorderFactory.createLineBorder(Color.GRAY, 1)
        ));

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        JButton btnPrint = new JButton("🖨️ Print " + labelCount + " Label(s)");
        JButton btnCancel = new JButton("Cancel");

        btnPrint.setBackground(new Color(40, 167, 69));
        btnPrint.setForeground(Color.WHITE);
        btnPrint.setFont(new Font("Segoe UI", Font.BOLD, 13));

        btnPrint.addActionListener(e -> {
            previewDialog.dispose();
            executePrintJob(job, product, barcodeNumber, barcodeType, labelCount, parentComponent);
        });

        btnCancel.addActionListener(e -> previewDialog.dispose());

        btnPanel.add(btnCancel);
        btnPanel.add(btnPrint);

        previewDialog.add(infoPanel, BorderLayout.NORTH);
        previewDialog.add(labelCanvas, BorderLayout.CENTER);
        previewDialog.add(btnPanel, BorderLayout.SOUTH);

        previewDialog.setVisible(true);
    }

    /**
     * Executes PrinterJob execution loop.
     */
    private static void executePrintJob(PrinterJob job, ProductEntity product, String barcodeNumber, String barcodeType, int labelCount, Component parentComponent) {
        job.setPrintable(new PrintableLabelDocument(product, barcodeNumber, barcodeType, labelCount));

        if (job.printDialog()) {
            try {
                job.print();
                JOptionPane.showMessageDialog(parentComponent, "✅ Sent " + labelCount + " label(s) to printer successfully.", "Print Successful", JOptionPane.INFORMATION_MESSAGE);
                util.LoggerUtil.logInfo("✅ Printed " + labelCount + " barcode label(s) for product: " + product.getName());
            } catch (PrinterException ex) {
                util.LoggerUtil.logError(BarcodeLabelPrinter.class, "PrinterException printing labels", ex);
                JOptionPane.showMessageDialog(parentComponent, "❌ Printing failed: " + ex.getMessage(), "Print Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Render a single product sticker label on Graphics2D context.
     */
    public static void renderSingleLabel(Graphics2D g2d, ProductEntity product, String barcodeNumber, String barcodeType, int x, int y, int width, int height) {
        // Draw sticker boundary box
        g2d.setColor(Color.WHITE);
        g2d.fillRect(x, y, width, height);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x, y, width, height);

        // Product Name (Truncated if long)
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 12));
        String name = product.getName() != null ? product.getName() : "Product";
        if (name.length() > 25) name = name.substring(0, 22) + "...";
        g2d.drawString(name, x + 10, y + 20);

        // Render Barcode Image
        BufferedImage barcodeImg = BarcodeGenerator.generateBarcodeBufferedImage(barcodeNumber, barcodeType, width - 30, 60);
        if (barcodeImg != null) {
            g2d.drawImage(barcodeImg, x + 15, y + 30, width - 30, 55, null);
        }

        // Barcode Text Number & SKU
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 10));
        String skuText = product.getSku() != null ? "SKU: " + product.getSku() : "";
        g2d.drawString(barcodeNumber + " " + skuText, x + 15, y + 100);

        // Price & Unit Highlight
        g2d.setFont(new Font("Segoe UI", Font.BOLD, 14));
        Double price = product.getSellPrice() != null ? product.getSellPrice() : 0.0;
        String unit = product.getUnit() != null ? product.getUnit() : "Piece";
        String priceText = String.format("Rs. %.2f / %s", price, unit);
        g2d.drawString(priceText, x + 10, y + 125);
    }

    /**
     * Printable Implementation for multi-label printing layout.
     */
    private static class PrintableLabelDocument implements Printable {
        private final ProductEntity product;
        private final String barcodeNumber;
        private final String barcodeType;
        private final int totalLabels;

        public PrintableLabelDocument(ProductEntity product, String barcodeNumber, String barcodeType, int totalLabels) {
            this.product = product;
            this.barcodeNumber = barcodeNumber;
            this.barcodeType = barcodeType;
            this.totalLabels = totalLabels;
        }

        @Override
        public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
            int labelsPerPage = 12; // 3 columns x 4 rows per page
            int totalPages = (int) Math.ceil((double) totalLabels / labelsPerPage);

            if (pageIndex >= totalPages) {
                return NO_SUCH_PAGE;
            }

            Graphics2D g2d = (Graphics2D) graphics;
            g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

            int labelWidth = 160;
            int labelHeight = 130;
            int marginX = 15;
            int marginY = 15;
            int gapX = 15;
            int gapY = 15;

            int startIndex = pageIndex * labelsPerPage;
            int endIndex = Math.min(startIndex + labelsPerPage, totalLabels);

            for (int i = startIndex; i < endIndex; i++) {
                int localIndex = i - startIndex;
                int col = localIndex % 3;
                int row = localIndex / 3;

                int x = marginX + col * (labelWidth + gapX);
                int y = marginY + row * (labelHeight + gapY);

                renderSingleLabel(g2d, product, barcodeNumber, barcodeType, x, y, labelWidth, labelHeight);
            }

            return PAGE_EXISTS;
        }
    }
}
