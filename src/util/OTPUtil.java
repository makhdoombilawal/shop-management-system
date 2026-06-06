package util;

import java.security.MessageDigest;
import java.security.SecureRandom;

/**
 * OTP generation and verification utility.
 * Generates secure 4-digit OTPs and provides hashing for storage.
 * 
 * @author Shop Management System
 * @version Enterprise Subscription System
 */
public final class OTPUtil {

    private OTPUtil() {
        // Utility class
    }

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int OTP_LENGTH = 4;
    private static final int OTP_MIN = 1000;
    private static final int OTP_MAX = 9999;

    // ========================================================================
    // OTP GENERATION
    // ========================================================================

    /**
     * Generate a random 4-digit OTP
     * @return OTP as String (e.g., "1234")
     */
    public static String generateOTP() {
        int otp = OTP_MIN + RANDOM.nextInt(OTP_MAX - OTP_MIN + 1);
        return String.format("%0" + OTP_LENGTH + "d", otp);
    }

    /**
     * Generate multiple OTPs for testing
     */
    public static String[] generateMultipleOTPs(int count) {
        String[] otps = new String[count];
        for (int i = 0; i < count; i++) {
            otps[i] = generateOTP();
        }
        return otps;
    }

    // ========================================================================
    // OTP HASHING & VERIFICATION
    // ========================================================================

    /**
     * Hash OTP using SHA-256 (for secure storage)
     */
    public static String hashOTP(String otp) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(otp.getBytes());
            return bytesToHex(hash);
        } catch (Exception e) {
            util.LoggerUtil.logError("❌ Error hashing OTP: " + e.getMessage(), null);
            return null;
        }
    }

    /**
     * Verify OTP against hash
     */
    public static boolean verifyOTP(String providedOtp, String hashedOtp) {
        if (providedOtp == null || hashedOtp == null) {
            return false;
        }
        String hash = hashOTP(providedOtp);
        return hash != null && hash.equals(hashedOtp);
    }

    /**
     * Convert bytes to hexadecimal string
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    // ========================================================================
    // OTP VALIDATION
    // ========================================================================

    /**
     * Validate OTP format (must be 4 digits)
     */
    public static boolean isValidOTPFormat(String otp) {
        if (otp == null || otp.length() != OTP_LENGTH) {
            return false;
        }
        return otp.matches("\\d{" + OTP_LENGTH + "}");
    }

    /**
     * Check if OTP appears to be a test/weak OTP
     */
    public static boolean isWeakOTP(String otp) {
        if (!isValidOTPFormat(otp)) {
            return true;
        }
        // All same digits
        return otp.matches("(\\d)\\1{" + (OTP_LENGTH - 1) + "}");
    }

    /**
     * Generate and get a strong OTP (non-weak)
     */
    public static String generateStrongOTP() {
        String otp;
        do {
            otp = generateOTP();
        } while (isWeakOTP(otp));
        return otp;
    }

    // ========================================================================
    // UTILITY METHODS
    // ========================================================================

    /**
     * Get OTP expiry time in seconds (24 hours)
     */
    public static long getOTPExpirySeconds() {
        return 24 * 60 * 60; // 24 hours
    }

    /**
     * Get OTP expiry message
     */
    public static String getOTPExpiryMessage() {
        return "OTP will expire in 24 hours";
    }

    /**
     * Get max OTP attempts
     */
    public static int getMaxOtpAttempts() {
        return 3;
    }

    /**
     * Get OTP attempt lockout duration in minutes
     */
    public static int getOTPLockoutDurationMinutes() {
        return 30;
    }

    /**
     * Test OTP generation
     */
    public static void main(String[] args) {
        util.LoggerUtil.logInfo("=== OTP Generation Test ===");
        for (int i = 0; i < 5; i++) {
            String otp = generateOTP();
            String hashed = hashOTP(otp);
            util.LoggerUtil.logInfo("OTP: " + otp + " | Hashed: " + hashed.substring(0, 16) + "...");
        }

        util.LoggerUtil.logInfo("\n=== Strong OTP Test ===");
        for (int i = 0; i < 5; i++) {
            String otp = generateStrongOTP();
            util.LoggerUtil.logInfo("Strong OTP: " + otp);
        }

        util.LoggerUtil.logInfo("\n=== Weak OTP Detection ===");
        String[] testOtps = {"1111", "2222", "1234", "5678", "9999"};
        for (String otp : testOtps) {
            util.LoggerUtil.logInfo("OTP: " + otp + " | Weak: " + isWeakOTP(otp));
        }
    }
}
