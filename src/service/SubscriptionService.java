package service;

import dao.SubscriptionHibernateDAO;
import dao.EmailQueueHibernateDAO;
import models.entity.SubscriptionEntity;
import models.entity.EmailQueueEntity;
import util.AppConfig;
import util.DatabaseSelector;
import util.LoggerUtil;

import javax.swing.JOptionPane;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Enterprise service layer for subscription management.
 * Consolidates all subscription validation, OTP management, and email notifications.
 */
public class SubscriptionService {

    private static final SubscriptionHibernateDAO subscriptionDao = new SubscriptionHibernateDAO();
    private static final EmailQueueHibernateDAO emailQueueDao = new EmailQueueHibernateDAO();

    private SubscriptionService() {
        // Service class
    }

    public enum SubscriptionStatus {
        ACTIVE,
        WARNING,
        EXPIRED
    }

    public enum VersionStatus {
        COMPATIBLE,
        UPDATE_REQUIRED
    }

    // ========================================================================
    // DEVICE FINGERPRINTING
    // ========================================================================

    /**
     * Generates a device fingerprint/machine ID using OS, hostname, and username.
     * @deprecated Use DeviceService.generateFingerprint() instead.
     */
    @Deprecated
    public static String getMachineId() {
        try {
            String os = System.getProperty("os.name", "unknown");
            String username = System.getProperty("user.name", "unknown");
            String hostname = java.net.InetAddress.getLocalHost().getHostName();
            return String.format("%s-%s-%s", os, hostname, username).replaceAll("\\s+", "");
        } catch (Exception e) {
            LoggerUtil.logWarning(SubscriptionService.class, "Failed to generate machine ID: " + e.getMessage());
            return "unknown-device";
        }
    }

    /**
     * Verifies that the current device matches the stored device fingerprint.
     */
    public static boolean verifyDeviceBinding() {
        SubscriptionEntity subscription = subscriptionDao.getPrimarySubscription();
        if (subscription == null) {
            return true; // No subscription yet, will be created later
        }

        String storedDeviceId = subscription.getDeviceId();
        if (storedDeviceId == null || storedDeviceId.isEmpty()) {
            // Legacy data, auto-bind
            subscription.setDeviceId(DeviceService.generateFingerprint());
            subscriptionDao.save(subscription);
            return true;
        }

        String currentFingerprint = DeviceService.generateFingerprint();
        if (!storedDeviceId.equals(currentFingerprint)) {
            LoggerUtil.logError(SubscriptionService.class, "Device mismatch! Unauthorized device detected.", null);
            return false;
        }
        return true;
    }

    /**
     * Resets the device binding (SUPER_ADMIN only).
     */
    public static boolean resetDeviceBinding() {
        SubscriptionEntity subscription = subscriptionDao.getPrimarySubscription();
        if (subscription != null) {
            subscription.setDeviceId(null);
            subscriptionDao.save(subscription);
            LoggerUtil.logInfo(SubscriptionService.class, "Device binding has been reset by administrator.");
            return true;
        }
        return false;
    }

    // ========================================================================
    // CORE STATUS METHODS
    // ========================================================================

    /**
     * Checks the subscription status and bootstraps a new one if none exists.
     */
    public static SubscriptionStatus checkStatus() {
        try {
            SubscriptionEntity subscription = subscriptionDao.getPrimarySubscription();
            if (subscription == null) {
                LoggerUtil.logInfo(SubscriptionService.class, "No subscription found. Bootstrapping initial subscription...");
                
                LocalDate today = LocalDate.now();
                LocalDate expiryDate = today.plusMonths(AppConfig.EXPIRY_MONTHS);
                
                String shopName = JOptionPane.showInputDialog(
                        null,
                        "Welcome to Shop Management System!\nPlease enter your Shop Name to complete installation:",
                        "Initial Setup",
                        JOptionPane.INFORMATION_MESSAGE
                );
                
                if (shopName == null || shopName.trim().isEmpty()) {
                    shopName = "Default Shop";
                }

                subscription = new SubscriptionEntity(
                        AppConfig.APP_VERSION,
                        today,
                        expiryDate
                );
                
                // Set Device Info
                Map<String, String> deviceInfo = DeviceService.captureDeviceInfo();
                subscription.setOsName(deviceInfo.get("os_name"));
                subscription.setHostname(deviceInfo.get("hostname"));
                subscription.setMacAddress(deviceInfo.get("mac_address"));
                subscription.setInstalledBy(deviceInfo.get("username"));
                subscription.setShopName(shopName);
                subscription.setDeviceId(DeviceService.generateFingerprint());
                subscription.setMachineId(getMachineId()); // Keep for legacy compatibility
                
                subscriptionDao.save(subscription);
                
                // Trigger installation notification email
                sendInstallationEmail(subscription);
                return SubscriptionStatus.ACTIVE;
            }

            subscription.updateStatus();
            subscriptionDao.save(subscription);

            if (subscription.isExpired()) {
                return SubscriptionStatus.EXPIRED;
            }

            // Warning threshold: less than 7 days remaining
            if (subscription.getRemainingDays() < 7) {
                return SubscriptionStatus.WARNING;
            }

            return SubscriptionStatus.ACTIVE;
        } catch (Exception e) {
            LoggerUtil.logError(SubscriptionService.class, "Error checking subscription status", e);
            return SubscriptionStatus.ACTIVE; // Degraded mode safety
        }
    }

    public static boolean isExpired() {
        return checkStatus() == SubscriptionStatus.EXPIRED;
    }

    public static boolean isWarning() {
        return checkStatus() == SubscriptionStatus.WARNING;
    }

    public static long getRemainingDays() {
        try {
            SubscriptionEntity subscription = subscriptionDao.getPrimarySubscription();
            if (subscription != null) {
                return subscription.getRemainingDays();
            }
        } catch (Exception e) {
            LoggerUtil.logError(SubscriptionService.class, "Error getting remaining days", e);
        }
        return 0;
    }

    // ========================================================================
    // EXTENSION & OTP
    // ========================================================================

    /**
     * Handles subscription expiry, generates a new OTP, and queues an email if not already done.
     * Enforces single-generation guarantee and prevents duplicate emails.
     */
    public static String handleExpiry(Long subscriptionId) {
        SubscriptionEntity subscription = subscriptionDao.findById(subscriptionId).orElse(null);
        if (subscription == null || !subscription.isExpired()) {
            return null;
        }

        // Single generation guarantee
        if (Boolean.TRUE.equals(subscription.getExpiryHandled()) || "SENT".equals(subscription.getOtpStatus())) {
            util.LoggerUtil.logInfo("⚠ Expiry already handled or OTP already sent for subscription: " + subscriptionId);
            return subscription.getOtpCode(); // Return existing code
        }

        String otp = OTPService.generateOTPForSubscription(subscriptionId);
        if (otp != null) {
            // Refresh entity after OTP service updates it
            subscription = subscriptionDao.findById(subscriptionId).orElse(null);
            if (subscription != null) {
                subscription.setOtpStatus("SENT");
                subscription.setExpiryHandled(true);
                subscriptionDao.save(subscription);
                sendExpiryEmail(subscription, otp);
            }
        }
        return otp;
    }

    /**
     * Validates the provided OTP, extends the subscription on success, and logs the action.
     */
    public static OTPService.OTPVerificationResult validateOTP(Long subscriptionId, String otp) {
        OTPService.OTPVerificationResult result = OTPService.verifyAndExtendSubscription(subscriptionId, otp);
        if (result.success) {
            SubscriptionEntity subscription = subscriptionDao.findById(subscriptionId).orElse(null);
            if (subscription != null) {
                // Reset flags on successful validation
                subscription.setOtpStatus(null);
                subscription.setExpiryHandled(false);
                subscriptionDao.save(subscription);

                String adminEmail = DatabaseSelector.getConfigValue("subscription.contact.email", "admin@shop.local");
                EmailService.sendRenewalSuccessEmail(adminEmail, subscription.getExpiryDate().toString());
            }
        }
        return result;
    }

    /**
     * Extends subscription by a specified number of months (Manual Admin override).
     */
    public static boolean extendSubscription(Long subscriptionId, int months) {
        boolean extended = subscriptionDao.extendSubscription(subscriptionId, months);
        if (extended) {
            SubscriptionEntity subscription = subscriptionDao.findById(subscriptionId).orElse(null);
            if (subscription != null) {
                String adminEmail = DatabaseSelector.getConfigValue("subscription.contact.email", "admin@shop.local");
                EmailService.sendRenewalSuccessEmail(adminEmail, subscription.getExpiryDate().toString());
            }
        }
        return extended;
    }

    // ========================================================================
    // EMAIL TRIGGERS
    // ========================================================================

    public static boolean sendInstallationEmail(SubscriptionEntity subscription) {
        String adminEmail = DatabaseSelector.getConfigValue("subscription.contact.email", "admin@shop.local");
        return EmailService.sendInstallationEmail(
                adminEmail,
                subscription.getMachineId(),
                subscription.getInstallDate().toString(),
                subscription.getExpiryDate().toString()
        );
    }

    public static boolean sendExpiryEmail(SubscriptionEntity subscription, String otp) {
        String adminEmail = DatabaseSelector.getConfigValue("subscription.contact.email", "admin@shop.local");
        return EmailService.sendExpiryAlertEmail(
                subscription.getId(),
                adminEmail,
                subscription.getMachineId(),
                subscription.getExpiryDate().toString(),
                otp
        );
    }

    public static boolean sendTestEmail(String recipient) {
        return EmailService.testEmailConfiguration(recipient);
    }

    // ========================================================================
    // ADMIN OPERATIONS & DTOs
    // ========================================================================

    /**
     * Forces a refresh and synchronization of the subscription status.
     */
    public static void forceSyncStatus() {
        try {
            SubscriptionEntity subscription = subscriptionDao.getPrimarySubscription();
            if (subscription != null) {
                subscription.updateStatus();
                subscriptionDao.save(subscription);
                LoggerUtil.logInfo(SubscriptionService.class, "Subscription status force synced. Status: " + subscription.getStatus());
            }
        } catch (Exception e) {
            LoggerUtil.logError(SubscriptionService.class, "Failed to force sync subscription status", e);
        }
    }

    /**
     * Returns subscription info DTO.
     */
    public static SubscriptionInfo getSubscriptionInfo() {
        SubscriptionEntity subscription = subscriptionDao.getPrimarySubscription();
        return new SubscriptionInfo(subscription);
    }

    /**
     * Returns all queued/logged emails.
     */
    public static List<EmailQueueEntity> getEmailQueueLogs() {
        try {
            return emailQueueDao.findAll();
        } catch (Exception e) {
            LoggerUtil.logError(SubscriptionService.class, "Failed to fetch email logs", e);
            return new ArrayList<>();
        }
    }

    /**
     * Formats subscription history/activation log.
     */
    public static List<String> getActivationHistory() {
        SubscriptionEntity subscription = subscriptionDao.getPrimarySubscription();
        List<String> history = new ArrayList<>();
        if (subscription == null) {
            history.add("No subscription record found.");
            return history;
        }

        history.add(String.format("[%s] System Initialized on %s (Machine ID: %s)",
                subscription.getInstalledVersion(), subscription.getInstallDate(), subscription.getMachineId()));
        
        if (subscription.getRenewalCount() != null && subscription.getRenewalCount() > 0) {
            history.add(String.format("[%s] Renewed successfully (Total renewals: %d, Last renewal: %s, Current expiry: %s)",
                    subscription.getInstalledVersion(), subscription.getRenewalCount(),
                    subscription.getLastRenewalDate(), subscription.getExpiryDate()));
        } else {
            history.add("No renewal operations performed yet.");
        }

        return history;
    }

    // ========================================================================
    // VERSION CONTROL
    // ========================================================================

    /**
     * Checks if the database subscription version is compatible with the running application version.
     */
    public static VersionStatus checkVersionCompatibility() {
        try {
            SubscriptionEntity subscription = subscriptionDao.getPrimarySubscription();
            if (subscription == null) {
                return VersionStatus.COMPATIBLE;
            }

            String dbVersion = subscription.getInstalledVersion();
            String appVersion = AppConfig.APP_VERSION;

            if (dbVersion == null) {
                return VersionStatus.UPDATE_REQUIRED;
            }

            if (!dbVersion.equalsIgnoreCase(appVersion)) {
                try {
                    int dbVal = Integer.parseInt(dbVersion.replaceAll("[^0-9]", ""));
                    int appVal = Integer.parseInt(appVersion.replaceAll("[^0-9]", ""));
                    if (dbVal < appVal) {
                        return VersionStatus.UPDATE_REQUIRED;
                    }
                } catch (Exception e) {
                    return VersionStatus.UPDATE_REQUIRED;
                }
            }
            return VersionStatus.COMPATIBLE;
        } catch (Exception e) {
            LoggerUtil.logError(SubscriptionService.class, "Failed to verify version compatibility", e);
            return VersionStatus.COMPATIBLE; // Safety fallback
        }
    }

    // ========================================================================
    // DTO FOR SUBSCRIPTION DISPLAY
    // ========================================================================

    public static class SubscriptionInfo {
        private final Long id;
        private final String installedVersion;
        private final LocalDate installDate;
        private final LocalDate expiryDate;
        private final String status;
        private final long remainingDays;
        private final int renewalCount;
        private final LocalDate lastRenewalDate;
        private final String deviceId;
        private final String deviceName;
        private final String osName;
        private final String hostname;
        private final String macAddress;
        private final String installedBy;
        private final String shopName;
        private final String machineId; // Deprecated
        private final String otpCode;
        private final LocalDateTime otpCreatedAt;
        private final LocalDateTime otpExpiryAt;
        private final boolean otpUsed;
        private final int otpAttemptsRemaining;
        private final boolean otpCooldownActive;
        private final LocalDateTime otpLockedUntil;

        public SubscriptionInfo(SubscriptionEntity entity) {
            if (entity != null) {
                this.id = entity.getId();
                this.installedVersion = entity.getInstalledVersion();
                this.installDate = entity.getInstallDate();
                this.expiryDate = entity.getExpiryDate();
                this.status = entity.getStatus();
                this.remainingDays = entity.getRemainingDays();
                this.renewalCount = entity.getRenewalCount() != null ? entity.getRenewalCount() : 0;
                this.lastRenewalDate = entity.getLastRenewalDate();
                
                this.deviceId = entity.getDeviceId();
                this.deviceName = entity.getDeviceName();
                this.osName = entity.getOsName();
                this.hostname = entity.getHostname();
                this.macAddress = entity.getMacAddress();
                this.installedBy = entity.getInstalledBy();
                this.shopName = entity.getShopName();
                
                this.machineId = entity.getMachineId();
                this.otpCode = entity.getOtpCode();
                this.otpCreatedAt = entity.getOtpCreatedAt();
                this.otpExpiryAt = entity.getOtpExpiryAt();
                this.otpUsed = entity.getOtpUsed() != null ? entity.getOtpUsed() : false;
                this.otpAttemptsRemaining = entity.getOtpAttemptsRemaining();
                this.otpCooldownActive = entity.isOtpCooldownActive();
                this.otpLockedUntil = entity.getOtpLockedUntil();
            } else {
                this.id = null;
                this.installedVersion = "N/A";
                this.installDate = null;
                this.expiryDate = null;
                this.status = "N/A";
                this.remainingDays = 0;
                this.renewalCount = 0;
                this.lastRenewalDate = null;
                this.deviceId = "N/A";
                this.deviceName = "N/A";
                this.osName = "N/A";
                this.hostname = "N/A";
                this.macAddress = "N/A";
                this.installedBy = "N/A";
                this.shopName = "N/A";
                this.machineId = "N/A";
                this.otpCode = null;
                this.otpCreatedAt = null;
                this.otpExpiryAt = null;
                this.otpUsed = false;
                this.otpAttemptsRemaining = 0;
                this.otpCooldownActive = false;
                this.otpLockedUntil = null;
            }
        }

        public Long getId() { return id; }
        public String getInstalledVersion() { return installedVersion; }
        public LocalDate getInstallDate() { return installDate; }
        public LocalDate getExpiryDate() { return expiryDate; }
        public String getStatus() { return status; }
        public long getRemainingDays() { return remainingDays; }
        public int getRenewalCount() { return renewalCount; }
        public LocalDate getLastRenewalDate() { return lastRenewalDate; }
        public String getDeviceId() { return deviceId; }
        public String getDeviceName() { return deviceName; }
        public String getOsName() { return osName; }
        public String getHostname() { return hostname; }
        public String getMacAddress() { return macAddress; }
        public String getInstalledBy() { return installedBy; }
        public String getShopName() { return shopName; }
        public String getMachineId() { return machineId; }
        public String getOtpCode() { return otpCode; }
        public LocalDateTime getOtpCreatedAt() { return otpCreatedAt; }
        public LocalDateTime getOtpExpiryAt() { return otpExpiryAt; }
        public boolean isOtpUsed() { return otpUsed; }
        public int getOtpAttemptsRemaining() { return otpAttemptsRemaining; }
        public boolean isOtpCooldownActive() { return otpCooldownActive; }
        public LocalDateTime getOtpLockedUntil() { return otpLockedUntil; }
    }
}
