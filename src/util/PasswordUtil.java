package util;

import org.mindrot.jbcrypt.BCrypt;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Utility class for password hashing and verification
 * Uses BCrypt with automatic backward-compatibility for legacy SHA-256 hashes.
 * 
 * @author Shop Management System
 */
public class PasswordUtil {
    
    private static final String HASH_ALGORITHM = "SHA-256";
    private static final int ITERATIONS = 10000;
    private static final int BCRYPT_LOG_ROUNDS = 12;
    
    /**
     * Hash a password using BCrypt algorithm
     * 
     * @param plainPassword The password to hash
     * @return BCrypt hash string
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(BCRYPT_LOG_ROUNDS));
    }
    
    /**
     * Verify a password against a stored hash (supports BCrypt and legacy SHA-256)
     * 
     * @param plainPassword The password to verify
     * @param storedHash The stored hash
     * @return true if password matches
     */
    public static boolean verifyPassword(String plainPassword, String storedHash) {
        if (plainPassword == null || storedHash == null) {
            return false;
        }
        
        if (isBCryptHash(storedHash)) {
            try {
                return BCrypt.checkpw(plainPassword, storedHash);
            } catch (Exception e) {
                return false;
            }
        }
        
        // Legacy SHA-256 hash verification
        return verifyLegacySha256(plainPassword, storedHash);
    }

    /**
     * Legacy SHA-256 verification for backward compatibility
     */
    private static boolean verifyLegacySha256(String plainPassword, String storedHash) {
        try {
            String[] parts = storedHash.split("\\$");
            if (parts.length != 2) {
                return false;
            }
            
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[1]);
            byte[] actualHash = hashWithSalt(plainPassword, salt);
            
            return MessageDigest.isEqual(expectedHash, actualHash);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Hash password with salt using multiple iterations for legacy SHA-256 verification
     */
    private static byte[] hashWithSalt(String password, byte[] salt) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
        md.update(salt);
        byte[] hash = md.digest(password.getBytes());
        
        for (int i = 0; i < ITERATIONS; i++) {
            md.reset();
            hash = md.digest(hash);
        }
        
        return hash;
    }
    
    /**
     * Check if a hash is formatted using BCrypt
     */
    public static boolean isBCryptHash(String hash) {
        return hash != null && (hash.startsWith("$2a$") || hash.startsWith("$2b$") || hash.startsWith("$2y$"));
    }

    /**
     * Check if a hash is legacy (plain text or SHA-256) needing upgrade to BCrypt
     */
    public static boolean isLegacyHash(String hash) {
        return hash == null || !isBCryptHash(hash);
    }
    
    /**
     * Check if a password is already hashed (BCrypt or legacy SHA-256)
     */
    public static boolean isPasswordHashed(String password) {
        if (password == null) return false;
        return isBCryptHash(password) || (password.contains("$") && password.split("\\$").length == 2);
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
