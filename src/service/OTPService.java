package service;

import dao.SubscriptionHibernateDAO;
import models.entity.SubscriptionEntity;
import util.OTPUtil;

/**
 * OTP service for subscription renewal.
 * Manages OTP generation, storage, validation, and subscription extension.
 * 
 * @author Shop Management System
 * @version Enterprise Subscription System
 */
public class OTPService {

    private static final SubscriptionHibernateDAO subscriptionDao = new SubscriptionHibernateDAO();

    private OTPService() {
        // Service class
    }

    // ========================================================================
    // OTP GENERATION & STORAGE
    // ========================================================================

    /**
     * Generate OTP for subscription renewal
     */
    public static String generateOTPForSubscription(Long subscriptionId) {
        try {
            String otp = OTPUtil.generateStrongOTP();
            
            if (subscriptionDao.updateOtp(subscriptionId, otp)) {
                util.LoggerUtil.logInfo("✅ OTP generated for subscription ID: " + subscriptionId);
                return otp;
            } else {
                util.LoggerUtil.logError("❌ Failed to save OTP to database", null);
                return null;
            }
        } catch (Exception e) {
            util.LoggerUtil.logError("❌ Error generating OTP: " + e.getMessage(), null);
            return null;
        }
    }

    /**
     * Get current OTP for subscription (if exists and not used)
     */
    public static String getCurrentOTP(Long subscriptionId) {
        try {
            SubscriptionEntity subscription = subscriptionDao.findById(subscriptionId).orElse(null);
            if (subscription == null) {
                return null;
            }

            if (subscription.getOtpUsed() || subscription.getOtpCode() == null) {
                return null;
            }

            if (subscription.isOtpExpired()) {
                util.LoggerUtil.logInfo("⚠ OTP expired");
                return null;
            }

            return subscription.getOtpCode();
        } catch (Exception e) {
            util.LoggerUtil.logError("❌ Error getting OTP: " + e.getMessage(), null);
            return null;
        }
    }

    // ========================================================================
    // OTP VERIFICATION
    // ========================================================================

    /**
     * Verify OTP and extend subscription
     */
    public static OTPVerificationResult verifyAndExtendSubscription(Long subscriptionId, String providedOtp) {
        try {
            SubscriptionEntity subscription = subscriptionDao.findById(subscriptionId).orElse(null);
            if (subscription == null) {
                return new OTPVerificationResult(false, "Subscription not found");
            }

            // Check if OTP cooldown is currently active
            if (subscription.isOtpCooldownActive()) {
                java.time.Duration duration = java.time.Duration.between(java.time.LocalDateTime.now(), subscription.getOtpLockedUntil());
                long minutes = duration.toMinutes();
                long seconds = duration.toSeconds() % 60;
                String timeStr = String.format("%02d:%02d", minutes, seconds);
                return new OTPVerificationResult(false, "Too many failed attempts. Try again in " + timeStr);
            }

            // If cooldown has expired, reset attempts
            if (subscription.getOtpLockedUntil() != null && !subscription.isOtpCooldownActive()) {
                subscription.setLastOtpAttemptCount(0);
                subscription.setOtpLockedUntil(null);
                subscriptionDao.save(subscription);
            }

            // Check if OTP was already used
            if (subscription.getOtpUsed()) {
                return new OTPVerificationResult(false, "OTP has already been used");
            }

            // Check if OTP is expired
            if (subscription.isOtpExpired()) {
                return new OTPVerificationResult(false, "OTP has expired (valid for 24 hours)");
            }

            // Validate OTP format
            if (!OTPUtil.isValidOTPFormat(providedOtp)) {
                return new OTPVerificationResult(false, "Invalid OTP format (must be 4 digits)");
            }

            // Verify OTP
            if (!providedOtp.equals(subscription.getOtpCode())) {
                int newAttempts = (subscription.getLastOtpAttemptCount() != null ? subscription.getLastOtpAttemptCount() : 0) + 1;
                subscription.setLastOtpAttemptCount(newAttempts);
                
                if (newAttempts >= 3) {
                    subscription.setOtpLockedUntil(java.time.LocalDateTime.now().plusMinutes(15));
                    subscriptionDao.save(subscription);
                    return new OTPVerificationResult(false, "Invalid OTP. Too many failed attempts. System locked for 15 minutes.");
                } else {
                    subscriptionDao.save(subscription);
                    int remaining = 3 - newAttempts;
                    return new OTPVerificationResult(false, String.format("Invalid OTP. Attempts remaining: %d", remaining));
                }
            }

            // OTP is valid - extend subscription
            int extensionMonths = 2; // Extend by 2 months
            if (subscriptionDao.verifyAndExtendSubscription(subscriptionId, providedOtp, extensionMonths)) {
                util.LoggerUtil.logInfo("✅ OTP verified and subscription extended by " + extensionMonths + " months");
                
                // Clear lockout and reset attempts on success
                SubscriptionEntity updatedSubscription = subscriptionDao.findById(subscriptionId).orElse(null);
                if (updatedSubscription != null) {
                    updatedSubscription.setOtpLockedUntil(null);
                    updatedSubscription.setLastOtpAttemptCount(0);
                    subscriptionDao.save(updatedSubscription);
                }
                
                String message = String.format(
                        "Subscription successfully extended until %s",
                        updatedSubscription.getExpiryDate()
                );
                return new OTPVerificationResult(true, message, updatedSubscription.getExpiryDate());
            } else {
                return new OTPVerificationResult(false, "Failed to extend subscription");
            }

        } catch (Exception e) {
            util.LoggerUtil.logError("❌ Error verifying OTP: " + e.getMessage(), null);
            return new OTPVerificationResult(false, "Error during OTP verification: " + e.getMessage());
        }
    }

    /**
     * Check OTP attempts remaining
     */
    public static int getOTPAttemptsRemaining(Long subscriptionId) {
        try {
            SubscriptionEntity subscription = subscriptionDao.findById(subscriptionId).orElse(null);
            if (subscription != null) {
                return subscription.getOtpAttemptsRemaining();
            }
            return 0;
        } catch (Exception e) {
            util.LoggerUtil.logError("❌ Error getting OTP attempts: " + e.getMessage(), null);
            return 0;
        }
    }

    /**
     * Check if OTP is expired
     */
    public static boolean isOTPExpired(Long subscriptionId) {
        try {
            SubscriptionEntity subscription = subscriptionDao.findById(subscriptionId).orElse(null);
            if (subscription != null) {
                return subscription.isOtpExpired();
            }
            return true;
        } catch (Exception e) {
            util.LoggerUtil.logError("❌ Error checking OTP expiry: " + e.getMessage(), null);
            return true;
        }
    }

    /**
     * Regenerate OTP (invalidates previous one)
     */
    public static String regenerateOTP(Long subscriptionId) {
        try {
            SubscriptionEntity subscription = subscriptionDao.findById(subscriptionId).orElse(null);
            if (subscription == null) {
                return null;
            }

            // Mark old OTP as used so it can't be used again
            subscription.markOtpAsUsed();
            subscriptionDao.save(subscription);

            // Generate new OTP
            return generateOTPForSubscription(subscriptionId);
        } catch (Exception e) {
            util.LoggerUtil.logError("❌ Error regenerating OTP: " + e.getMessage(), null);
            return null;
        }
    }

    // ========================================================================
    // UTILITY METHODS
    // ========================================================================

    /**
     * Get OTP status
     */
    public static String getOTPStatus(Long subscriptionId) {
        try {
            SubscriptionEntity subscription = subscriptionDao.findById(subscriptionId).orElse(null);
            if (subscription == null) {
                return "Subscription not found";
            }

            if (subscription.getOtpCode() == null) {
                return "No OTP generated";
            }

            if (subscription.getOtpUsed()) {
                return "OTP already used";
            }

            if (subscription.isOtpExpired()) {
                return "OTP expired";
            }

            int attemptsRemaining = subscription.getOtpAttemptsRemaining();
            return String.format("OTP valid - Attempts remaining: %d", attemptsRemaining);
        } catch (Exception e) {
            util.LoggerUtil.logError("❌ Error getting OTP status: " + e.getMessage(), null);
            return "Error getting OTP status";
        }
    }

    // ========================================================================
    // INNER CLASS: OTP VERIFICATION RESULT
    // ========================================================================

    /**
     * Result object for OTP verification
     */
    public static class OTPVerificationResult {
        public final boolean success;
        public final String message;
        public final java.time.LocalDate newExpiryDate;

        public OTPVerificationResult(boolean success, String message) {
            this(success, message, null);
        }

        public OTPVerificationResult(boolean success, String message, java.time.LocalDate newExpiryDate) {
            this.success = success;
            this.message = message;
            this.newExpiryDate = newExpiryDate;
        }

        @Override
        public String toString() {
            return "OTPVerificationResult{" +
                    "success=" + success +
                    ", message='" + message + '\'' +
                    ", newExpiryDate=" + newExpiryDate +
                    '}';
        }
    }
}
