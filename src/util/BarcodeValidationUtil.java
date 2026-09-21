package util;

import java.util.regex.Pattern;

/**
 * Supermarket Barcode Validation & Normalization Utility
 * Provides check-digit calculation and format verification for EAN-13, EAN-8, UPC-A, UPC-E, CODE128, CODE39, and QR formats.
 *
 * @author Shop Management System
 */
public class BarcodeValidationUtil {

    private static final Pattern NUMERIC_PATTERN = Pattern.compile("^[0-9]+$");
    private static final Pattern CODE39_PATTERN = Pattern.compile("^[A-Z0-9\\-. $/+%]+$");
    private static final Pattern CODE128_PATTERN = Pattern.compile("^[\\x20-\\x7E]+$");

    /**
     * Normalizes barcode string input:
     * - Trims leading and trailing whitespace
     * - Strips non-printable HID scanner control characters
     * - Preserves exact meaningful leading zeros
     */
    public static String normalizeBarcode(String barcode) {
        if (barcode == null) {
            return "";
        }
        // Remove non-printable control characters (ASCII 0-31 and 127) while keeping spaces if valid
        String cleaned = barcode.replaceAll("[\\x00-\\x1F\\x7F]", "");
        return cleaned.trim();
    }

    /**
     * Validates barcode according to its specified type.
     */
    public static ValidationResult validate(String rawBarcode, String barcodeType) {
        String barcode = normalizeBarcode(rawBarcode);

        if (barcode.isEmpty()) {
            return new ValidationResult(false, "Barcode cannot be empty or whitespace-only.");
        }

        String type = (barcodeType == null || barcodeType.trim().isEmpty()) ? "CODE128" : barcodeType.trim().toUpperCase();

        switch (type) {
            case "EAN13":
            case "EAN-13":
                return validateEAN13(barcode);
            case "EAN8":
            case "EAN-8":
                return validateEAN8(barcode);
            case "UPCA":
            case "UPC-A":
                return validateUPCA(barcode);
            case "UPCE":
            case "UPC-E":
                return validateUPCE(barcode);
            case "CODE39":
            case "CODE-39":
                return validateCode39(barcode);
            case "QR":
            case "QRCODE":
                return validateQR(barcode);
            case "CODE128":
            case "CODE-128":
            default:
                return validateCode128(barcode);
        }
    }

    /**
     * Validates EAN-13 (13 digits, Modulo 10 check digit).
     */
    public static ValidationResult validateEAN13(String barcode) {
        if (barcode.length() != 13 || !NUMERIC_PATTERN.matcher(barcode).matches()) {
            return new ValidationResult(false, "EAN-13 barcode must be exactly 13 numeric digits.");
        }
        if (!verifyModulo10CheckDigit(barcode, 13, new int[]{1, 3})) {
            return new ValidationResult(false, "Invalid EAN-13 check digit.");
        }
        return new ValidationResult(true, "Valid EAN-13 barcode.");
    }

    /**
     * Validates EAN-8 (8 digits, Modulo 10 check digit).
     */
    public static ValidationResult validateEAN8(String barcode) {
        if (barcode.length() != 8 || !NUMERIC_PATTERN.matcher(barcode).matches()) {
            return new ValidationResult(false, "EAN-8 barcode must be exactly 8 numeric digits.");
        }
        if (!verifyModulo10CheckDigit(barcode, 8, new int[]{3, 1})) {
            return new ValidationResult(false, "Invalid EAN-8 check digit.");
        }
        return new ValidationResult(true, "Valid EAN-8 barcode.");
    }

    /**
     * Validates UPC-A (12 digits, Modulo 10 check digit).
     */
    public static ValidationResult validateUPCA(String barcode) {
        if (barcode.length() != 12 || !NUMERIC_PATTERN.matcher(barcode).matches()) {
            return new ValidationResult(false, "UPC-A barcode must be exactly 12 numeric digits.");
        }
        if (!verifyModulo10CheckDigit(barcode, 12, new int[]{3, 1})) {
            return new ValidationResult(false, "Invalid UPC-A check digit.");
        }
        return new ValidationResult(true, "Valid UPC-A barcode.");
    }

    /**
     * Validates UPC-E (6, 7, or 8 numeric digits).
     */
    public static ValidationResult validateUPCE(String barcode) {
        if ((barcode.length() < 6 || barcode.length() > 8) || !NUMERIC_PATTERN.matcher(barcode).matches()) {
            return new ValidationResult(false, "UPC-E barcode must be 6 to 8 numeric digits.");
        }
        return new ValidationResult(true, "Valid UPC-E barcode.");
    }

    /**
     * Validates CODE39 (Alphanumeric A-Z, 0-9, -, ., $, /, +, %, space).
     */
    public static ValidationResult validateCode39(String barcode) {
        if (barcode.length() < 1 || barcode.length() > 50) {
            return new ValidationResult(false, "CODE39 length must be between 1 and 50 characters.");
        }
        if (!CODE39_PATTERN.matcher(barcode.toUpperCase()).matches()) {
            return new ValidationResult(false, "CODE39 contains invalid characters.");
        }
        return new ValidationResult(true, "Valid CODE39 barcode.");
    }

    /**
     * Validates CODE128 (Printable ASCII).
     */
    public static ValidationResult validateCode128(String barcode) {
        if (barcode.length() < 1 || barcode.length() > 80) {
            return new ValidationResult(false, "CODE128 length must be between 1 and 80 characters.");
        }
        if (!CODE128_PATTERN.matcher(barcode).matches()) {
            return new ValidationResult(false, "CODE128 contains unprintable ASCII characters.");
        }
        return new ValidationResult(true, "Valid CODE128 barcode.");
    }

    /**
     * Validates QR Code text.
     */
    public static ValidationResult validateQR(String barcode) {
        if (barcode.length() < 1 || barcode.length() > 500) {
            return new ValidationResult(false, "QR code content length must be between 1 and 500 characters.");
        }
        return new ValidationResult(true, "Valid QR code.");
    }

    /**
     * Generic Modulo 10 Check Digit Verification algorithm.
     */
    private static boolean verifyModulo10CheckDigit(String barcode, int expectedLength, int[] weights) {
        int sum = 0;
        int expectedCheckDigit = Character.getNumericValue(barcode.charAt(expectedLength - 1));

        for (int i = 0; i < expectedLength - 1; i++) {
            int digit = Character.getNumericValue(barcode.charAt(i));
            int weight = weights[i % 2];
            sum += digit * weight;
        }

        int calculatedCheckDigit = (10 - (sum % 10)) % 10;
        return expectedCheckDigit == calculatedCheckDigit;
    }

    /**
     * Calculates EAN-13 check digit for a 12-digit payload.
     */
    public static String calculateEAN13CheckDigit(String twelveDigits) {
        if (twelveDigits == null || twelveDigits.length() != 12 || !NUMERIC_PATTERN.matcher(twelveDigits).matches()) {
            throw new IllegalArgumentException("Requires 12 numeric digits to calculate EAN-13 check digit");
        }
        int sum = 0;
        int[] weights = new int[]{1, 3};
        for (int i = 0; i < 12; i++) {
            int digit = Character.getNumericValue(twelveDigits.charAt(i));
            sum += digit * weights[i % 2];
        }
        int checkDigit = (10 - (sum % 10)) % 10;
        return twelveDigits + checkDigit;
    }

    /**
     * Validation Result DTO.
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String message;

        public ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }
    }
}
