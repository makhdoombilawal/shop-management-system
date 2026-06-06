package util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class for password hashing and verification
 * Uses SHA-256 with salt for secure password storage
 * 
 * NOTE: For production, consider using BCrypt library for even better security
 * This implementation provides good security without external dependencies
 * 
 * @author Shop Management System
 */
public class PasswordUtil {
    
    private static final String HASH_ALGORITHM = "SHA-256";
    private static final int SALT_LENGTH = 16;
    private static final int ITERATIONS = 10000; // PBKDF2-like iterations
    
    /**
     * Hash a password with a randomly generated salt
     * Format: salt$hash
     * 
     * @param plainPassword The password to hash
     * @return Salted hash string
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        
        try {
            // Generate random salt
            byte[] salt = generateSalt();
            
            // Hash password with salt
            byte[] hash = hashWithSalt(plainPassword, salt);
            
            // Return format: base64(salt)$base64(hash)
            return Base64.getEncoder().encodeToString(salt) + "$" + 
                   Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
    
    /**
     * Verify a password against a stored hash
     * 
     * @param plainPassword The password to verify
     * @param storedHash The stored hash (format: salt$hash)
     * @return true if password matches
     */
    public static boolean verifyPassword(String plainPassword, String storedHash) {
        if (plainPassword == null || storedHash == null) {
            return false;
        }
        
        try {
            // Split stored hash into salt and hash parts
            String[] parts = storedHash.split("\\$");
            if (parts.length != 2) {
                return false; // Invalid format
            }
            
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[1]);
            
            // Hash the input password with the same salt
            byte[] actualHash = hashWithSalt(plainPassword, salt);
            
            // Compare hashes
            return MessageDigest.isEqual(expectedHash, actualHash);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Generate a random salt
     */
    private static byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return salt;
    }
    
    /**
     * Hash password with salt using multiple iterations for security
     */
    private static byte[] hashWithSalt(String password, byte[] salt) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
        md.update(salt);
        byte[] hash = md.digest(password.getBytes());
        
        // Apply multiple iterations for additional security
        for (int i = 0; i < ITERATIONS; i++) {
            md.reset();
            hash = md.digest(hash);
        }
        
        return hash;
    }
    
    /**
     * Check if a password is already hashed (contains salt separator)
     */
    public static boolean isPasswordHashed(String password) {
        return password != null && password.contains("$") && password.split("\\$").length == 2;
    }
    
    /**
     * Validate password strength
     * Returns true if password meets minimum requirements
     */
    public static boolean isStrongPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            if (Character.isLowerCase(c)) hasLower = true;
            if (Character.isDigit(c)) hasDigit = true;
        }
        
        return hasUpper && hasLower && hasDigit;
    }
    
    /**
     * Get password strength message
     */
    public static String getPasswordStrengthMessage(String password) {
        if (password == null || password.isEmpty()) {
            return "Password is required";
        }
        if (password.length() < 6) {
            return "Password must be at least 6 characters long";
        }
        if (password.length() < 8) {
            return "Password should be at least 8 characters for better security";
        }
        if (!password.matches(".*[A-Z].*")) {
            return "Password should contain at least one uppercase letter";
        }
        if (!password.matches(".*[a-z].*")) {
            return "Password should contain at least one lowercase letter";
        }
        if (!password.matches(".*\\d.*")) {
            return "Password should contain at least one digit";
        }
        return "Password is strong";
    }
    
    /**
     * Get password strength score (0-5)
     */
    public static int getPasswordStrength(String password) {
        if (password == null || password.isEmpty()) return 0;
        
        int score = 0;
        if (password.length() >= 6) score++;
        if (password.length() >= 8) score++;
        if (password.length() >= 12) score++;
        if (password.matches(".*[A-Z].*")) score++;
        if (password.matches(".*[a-z].*")) score++;
        if (password.matches(".*\\d.*")) score++;
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) score++;
        
        return Math.min(score, 5); // Cap at 5
    }
}
