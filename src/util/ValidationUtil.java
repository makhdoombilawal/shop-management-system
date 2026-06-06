package util;

import java.util.regex.Pattern;

/**
 * Utility class for input validation across the application
 * Provides centralized validation logic for consistent data quality
 * 
 * @author Shop Management System
 */
public class ValidationUtil {
    
    // Regex patterns for validation
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^[+]?[0-9]{10,15}$"
    );
    
    private static final Pattern BARCODE_PATTERN = Pattern.compile(
        "^[0-9]{8,13}$"  // Standard barcode formats (EAN-8, EAN-13, UPC)
    );
    
    /**
     * Validate email address format
     */
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }
    
    /**
     * Validate phone number format
     */
    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone.trim()).matches();
    }
    
    /**
     * Validate barcode format
     */
    public static boolean isValidBarcode(String barcode) {
        return barcode != null && BARCODE_PATTERN.matcher(barcode.trim()).matches();
    }
    
    /**
     * Check if string is not null and not empty
     */
    public static boolean isNotEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }
    
    /**
     * Validate positive number
     */
    public static boolean isPositiveNumber(double value) {
        return value > 0;
    }
    
    /**
     * Validate non-negative number
     */
    public static boolean isNonNegativeNumber(double value) {
        return value >= 0;
    }
    
    /**
     * Validate positive integer
     */
    public static boolean isPositiveInteger(int value) {
        return value > 0;
    }
    
    /**
     * Validate price range (reasonable limits)
     */
    public static boolean isValidPrice(double price) {
        return price >= 0 && price <= 1000000; // Max price limit
    }
    
    /**
     * Validate stock quantity
     */
    public static boolean isValidStock(int stock) {
        return stock >= 0 && stock <= 1000000; // Max stock limit
    }
    
    /**
     * Sanitize string input (remove dangerous characters)
     */
    public static String sanitizeInput(String input) {
        if (input == null) return "";
        return input.trim()
                    .replaceAll("[<>\"']", "") // Remove potential XSS characters
                    .replaceAll("--", "");     // Remove SQL comment markers
    }
    
    /**
     * Validate string length
     */
    public static boolean isValidLength(String str, int minLength, int maxLength) {
        if (str == null) return false;
        int length = str.trim().length();
        return length >= minLength && length <= maxLength;
    }
}
