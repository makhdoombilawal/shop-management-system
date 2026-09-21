package util;

import dao.BarcodeHibernateDAO;
import dao.ProductHibernateDAO;
import models.Session;
import models.entity.BarcodeEntity;
import models.entity.ProductEntity;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Supermarket Barcode CSV Import/Export Utility.
 * Supports:
 *  - CSV Export of all active product-barcode mappings
 *  - CSV Import with dry-run preview validation (valid vs invalid rows)
 *  - Full import execution after user review
 *
 * CSV format (header):
 *   BarcodeNumber,ProductId,BarcodeType,IsPrimary,Status,Notes
 *
 * @author Shop Management System
 */
public class BarcodeImportExportUtil {

    // CSV column indices
    private static final int COL_BARCODE_NUMBER = 0;
    private static final int COL_PRODUCT_ID     = 1;
    private static final int COL_BARCODE_TYPE   = 2;
    private static final int COL_IS_PRIMARY     = 3;
    private static final int COL_STATUS         = 4;
    private static final int COL_NOTES          = 5;

    private static final String CSV_HEADER = "BarcodeNumber,ProductId,BarcodeType,IsPrimary,Status,Notes";

    // ─────────────────────────────────────────────────────────────
    // EXPORT
    // ─────────────────────────────────────────────────────────────

    /**
     * Exports all barcode records to a CSV file.
     *
     * @param outputFile The destination file path.
     * @return Number of rows written.
     * @throws IOException on file write errors.
     */
    public static int exportToCSV(File outputFile) throws IOException {
        BarcodeHibernateDAO barcodeDAO = new BarcodeHibernateDAO();
        List<BarcodeEntity> barcodes = barcodeDAO.findAll();

        int rowCount = 0;
        try (BufferedWriter bw = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8))) {

            // BOM for Excel compatibility
            bw.write('\uFEFF');
            bw.write(CSV_HEADER);
            bw.newLine();

            for (BarcodeEntity b : barcodes) {
                String productId = b.getProduct() != null ? String.valueOf(b.getProduct().getProductId()) : "";
                String barcodeType = b.getBarcodeType() != null ? b.getBarcodeType() : "CODE128";
                String isPrimary = (b.getIsPrimary() != null && b.getIsPrimary()) ? "true" : "false";
                String status = b.getStatus() != null ? b.getStatus() : "active";
                String notes = b.getNotes() != null ? escapeCSV(b.getNotes()) : "";

                bw.write(String.join(",",
                        escapeCSV(b.getBarcodeNumber()),
                        productId,
                        barcodeType,
                        isPrimary,
                        status,
                        notes));
                bw.newLine();
                rowCount++;
            }
        }
        return rowCount;
    }

    // ─────────────────────────────────────────────────────────────
    // IMPORT: DRY-RUN PREVIEW
    // ─────────────────────────────────────────────────────────────

    /**
     * Parses a CSV file and returns a list of validated import rows.
     * Does NOT write anything to the database — use for preview.
     *
     * @param csvFile The CSV file to parse.
     * @return List of {@link ImportRow} DTOs (each row marked valid/invalid with reason).
     * @throws IOException on file read errors.
     */
    public static List<ImportRow> previewImport(File csvFile) throws IOException {
        List<ImportRow> rows = new ArrayList<>();
        ProductHibernateDAO productDAO = new ProductHibernateDAO();
        BarcodeHibernateDAO barcodeDAO = new BarcodeHibernateDAO();

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(csvFile), StandardCharsets.UTF_8))) {

            String line = br.readLine(); // Skip header
            if (line == null) return rows; // Empty file

            int lineNumber = 1;
            while ((line = br.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                if (line.isEmpty()) continue;

                ImportRow row = parseLine(line, lineNumber, productDAO, barcodeDAO);
                rows.add(row);
            }
        }
        return rows;
    }

    /**
     * Executes the import by saving all valid rows from a preview result.
     *
     * @param previewRows    The preview rows (from {@link #previewImport}).
     * @param skipInvalid    If true, skip invalid rows and import valid ones.
     * @return ImportResult summary (imported count, skipped count, error messages).
     */
    public static ImportResult executeImport(List<ImportRow> previewRows, boolean skipInvalid) {
        if (!Session.canImportBarcodes()) {
            return new ImportResult(0, previewRows.size(),
                    List.of("PERMISSION DENIED: Only ADMIN or SUPER_ADMIN can import barcodes."));
        }

        ProductHibernateDAO productDAO = new ProductHibernateDAO();

        int imported = 0;
        int skipped = 0;
        List<String> errors = new ArrayList<>();

        for (ImportRow row : previewRows) {
            if (!row.isValid()) {
                if (skipInvalid) {
                    skipped++;
                } else {
                    errors.add("Row " + row.getLineNumber() + ": " + row.getValidationError());
                    skipped++;
                }
                continue;
            }

            try {
                Optional<ProductEntity> productOpt = productDAO.findById(row.getProductId());
                if (productOpt.isEmpty()) {
                    errors.add("Row " + row.getLineNumber() + ": Product ID " + row.getProductId() + " no longer exists.");
                    skipped++;
                    continue;
                }

                ProductEntity product = productOpt.get();

                // Check if barcode already exists
                BarcodeHibernateDAO barcodeDAO = new BarcodeHibernateDAO();
                BarcodeEntity existing = barcodeDAO.findByBarcodeNumber(row.getBarcodeNumber());
                if (existing != null) {
                    // Update existing record
                    existing.setBarcodeType(row.getBarcodeType());
                    existing.setIsPrimary(row.isPrimary());
                    existing.setStatus(row.getStatus());
                    existing.setNotes(row.getNotes());
                    barcodeDAO.update(existing);
                } else {
                    // Create new barcode
                    BarcodeEntity newBarcode = new BarcodeEntity();
                    newBarcode.setBarcodeNumber(row.getBarcodeNumber());
                    newBarcode.setProduct(product);
                    newBarcode.setBarcodeType(row.getBarcodeType());
                    newBarcode.setIsPrimary(row.isPrimary());
                    newBarcode.setStatus(row.getStatus());
                    newBarcode.setNotes(row.getNotes());
                    barcodeDAO.save(newBarcode);
                }
                imported++;

            } catch (Exception ex) {
                errors.add("Row " + row.getLineNumber() + ": Import error — " + ex.getMessage());
                skipped++;
            }
        }

        return new ImportResult(imported, skipped, errors);
    }

    // ─────────────────────────────────────────────────────────────
    // INTERNAL PARSING
    // ─────────────────────────────────────────────────────────────

    private static ImportRow parseLine(String line, int lineNumber,
                                       ProductHibernateDAO productDAO,
                                       BarcodeHibernateDAO barcodeDAO) {
        String[] cols = parseCSVLine(line);

        if (cols.length < 4) {
            return ImportRow.invalid(lineNumber, line, "Too few columns (expected at least 4).");
        }

        // Barcode number
        String barcodeNumber = BarcodeValidationUtil.normalizeBarcode(cols[COL_BARCODE_NUMBER]);
        if (barcodeNumber.isEmpty()) {
            return ImportRow.invalid(lineNumber, line, "Barcode number is empty.");
        }

        // Product ID
        int productId;
        try {
            productId = Integer.parseInt(cols[COL_PRODUCT_ID].trim());
            if (productId <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            return ImportRow.invalid(lineNumber, line, "Invalid Product ID: '" + cols[COL_PRODUCT_ID].trim() + "'.");
        }

        // Validate product exists
        Optional<ProductEntity> product = productDAO.findById(productId);
        if (product.isEmpty()) {
            return ImportRow.invalid(lineNumber, line, "Product ID " + productId + " not found.");
        }

        // Barcode type (optional, default CODE128)
        String barcodeType = cols.length > COL_BARCODE_TYPE ? cols[COL_BARCODE_TYPE].trim() : "CODE128";
        if (barcodeType.isEmpty()) barcodeType = "CODE128";

        // Is primary (optional, default false)
        boolean isPrimary = cols.length > COL_IS_PRIMARY
                && "true".equalsIgnoreCase(cols[COL_IS_PRIMARY].trim());

        // Status (optional, default active)
        String status = cols.length > COL_STATUS ? cols[COL_STATUS].trim() : "active";
        if (status.isEmpty()) status = "active";

        // Notes (optional)
        String notes = cols.length > COL_NOTES ? cols[COL_NOTES].trim() : "";

        return ImportRow.valid(lineNumber, line, barcodeNumber, productId,
                barcodeType, isPrimary, status, notes);
    }

    /**
     * Simple CSV line parser that handles quoted fields.
     */
    private static String[] parseCSVLine(String line) {
        List<String> fields = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder current = new StringBuilder();

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        fields.add(current.toString());
        return fields.toArray(new String[0]);
    }

    private static String escapeCSV(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    // ─────────────────────────────────────────────────────────────
    // DTOs
    // ─────────────────────────────────────────────────────────────

    /**
     * Represents a single parsed CSV import row with validation state.
     */
    public static class ImportRow {
        private final int lineNumber;
        private final String rawLine;
        private final boolean valid;
        private final String validationError;

        // Parsed fields (only set if valid)
        private String barcodeNumber;
        private int productId;
        private String barcodeType;
        private boolean primary;
        private String status;
        private String notes;

        private ImportRow(int lineNumber, String rawLine, boolean valid, String validationError) {
            this.lineNumber = lineNumber;
            this.rawLine = rawLine;
            this.valid = valid;
            this.validationError = validationError;
        }

        static ImportRow invalid(int lineNumber, String rawLine, String reason) {
            return new ImportRow(lineNumber, rawLine, false, reason);
        }

        static ImportRow valid(int lineNumber, String rawLine,
                               String barcodeNumber, int productId,
                               String barcodeType, boolean isPrimary,
                               String status, String notes) {
            ImportRow row = new ImportRow(lineNumber, rawLine, true, null);
            row.barcodeNumber = barcodeNumber;
            row.productId = productId;
            row.barcodeType = barcodeType;
            row.primary = isPrimary;
            row.status = status;
            row.notes = notes;
            return row;
        }

        public int getLineNumber()      { return lineNumber; }
        public String getRawLine()      { return rawLine; }
        public boolean isValid()        { return valid; }
        public String getValidationError() { return validationError; }
        public String getBarcodeNumber(){ return barcodeNumber; }
        public int getProductId()       { return productId; }
        public String getBarcodeType()  { return barcodeType; }
        public boolean isPrimary()      { return primary; }
        public String getStatus()       { return status; }
        public String getNotes()        { return notes; }
    }

    /**
     * Summary of a completed import operation.
     */
    public static class ImportResult {
        private final int importedCount;
        private final int skippedCount;
        private final List<String> errors;

        public ImportResult(int importedCount, int skippedCount, List<String> errors) {
            this.importedCount = importedCount;
            this.skippedCount = skippedCount;
            this.errors = errors != null ? errors : new ArrayList<>();
        }

        public int getImportedCount() { return importedCount; }
        public int getSkippedCount()  { return skippedCount; }
        public List<String> getErrors() { return errors; }
        public boolean hasErrors()    { return !errors.isEmpty(); }

        @Override
        public String toString() {
            return String.format("Import complete: %d imported, %d skipped%s",
                    importedCount, skippedCount,
                    errors.isEmpty() ? "." : " with " + errors.size() + " error(s).");
        }
    }
}
