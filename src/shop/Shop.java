package shop;

import frames.Login;
import frames.SubscriptionExpiryDialog;
import frames.OTPEntryDialog;
import com.formdev.flatlaf.FlatLightLaf;
import service.SubscriptionService;
import util.AppConfig;
import util.EmailQueueProcessor;
import java.time.LocalDate;

import javax.swing.*;
import java.awt.*;
import java.awt.GraphicsEnvironment;

public class Shop {

    public static void main(String[] args) throws Exception {

        // Installer / CI can run a headless initialization pass
        if (args != null) {
            for (String arg : args) {
                if ("--initialize-db".equalsIgnoreCase(arg) || "--init-db".equalsIgnoreCase(arg)) {
                    util.DbBootstrapper.runInitializeOnly();
                    return;
                }
            }
        }

        // Ensured enterprise validation (already run above or in DB initialization logic)

        // Ensure DB + seed data before showing login
        util.DbBootstrapper.ensureDatabaseReadyInteractive();

        // ✅ ENTERPRISE SUBSCRIPTION MONITORING (NEW)
        if (!validateSubscriptionAndHandle()) {
            return; // Subscription expired and not renewed
        }

        // 🔹 Apply FlatLaf Look & Feel
        UIManager.setLookAndFeel(new FlatLightLaf());

        // 🔹 Rounded corners and UI tweaks
        UIManager.put("Component.arc", 20);
        UIManager.put("TextComponent.arc", 20);
        UIManager.put("Button.arc", 25);
        UIManager.put("ScrollBar.thumbArc", 15);

        UIManager.put("TextField.selectionBackground", new Color(100, 149, 237));
        UIManager.put("TextField.selectionForeground", Color.WHITE);
        UIManager.put("TextField.background", new Color(245, 248, 255));
        UIManager.put("TextField.foreground", new Color(50, 50, 50));
        UIManager.put("TextField.placeholderForeground", new Color(150, 150, 150));

        UIManager.put("Button.background", new Color(0, 120, 215));
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("Button.hoverBackground", new Color(30, 144, 255));
        UIManager.put("Button.pressedBackground", new Color(0, 90, 180));
        UIManager.put("Button.focusedBackground", new Color(25, 120, 210));
        UIManager.put("Button.font", new javax.swing.plaf.FontUIResource("Segoe UI", Font.BOLD, 14));

        UIManager.put("ScrollBar.thumb", new Color(180, 180, 200));
        UIManager.put("ScrollBar.thumbHover", new Color(140, 140, 180));

        UIManager.put("Table.showHorizontalLines", true);
        UIManager.put("Table.showVerticalLines", false);
        UIManager.put("Table.intercellSpacing", new java.awt.Dimension(0, 1));
        UIManager.put("Table.selectionBackground", new Color(204, 229, 255));
        UIManager.put("Table.selectionForeground", Color.BLACK);
        UIManager.put("Panel.background", new Color(250, 252, 255));
        UIManager.put("Table.background", Color.WHITE);

        // 🔹 Start Email Retry Service (for offline email delivery)
        // 4. Start Background Email Queue Processor
        EmailQueueProcessor.getInstance().startService();

        // 🔹 Launch Login directly
        SwingUtilities.invokeLater(() -> {
            new Login().setVisible(true);
        });
    }
    
    // Removed ENTERPRISE LICENSE VALIDATION block
    
    // ========================================================================
    // ENTERPRISE SUBSCRIPTION MONITORING (OTP-BASED RENEWAL)
    // ========================================================================

    /**
     * Validate subscription and handle expiry with OTP-based renewal
     * Returns true if subscription is valid and app can run, false if expired/blocked
     */
    private static boolean validateSubscriptionAndHandle() {
        try {
            // Check if subscription feature is enabled
            boolean subscriptionEnabled = "true".equalsIgnoreCase(
                    util.DatabaseSelector.getConfigValue("subscription.enabled", "false")
            );

            if (!subscriptionEnabled) {
                util.LoggerUtil.logInfo("✅ Subscription system disabled");
                return true;
            }

            // Perform version compatibility check first
            if (SubscriptionService.checkVersionCompatibility() == SubscriptionService.VersionStatus.UPDATE_REQUIRED) {
                util.LoggerUtil.logInfo("❌ Version mismatch - update required");
                if (!isHeadlessMode()) {
                    JOptionPane.showMessageDialog(null,
                            AppConfig.VERSION_MISMATCH_MESSAGE,
                            "Update Required",
                            JOptionPane.ERROR_MESSAGE);
                }
                return false;
            }

            // Verify Device Binding (Anti-Fraud Check)
            if (!SubscriptionService.verifyDeviceBinding()) {
                util.LoggerUtil.logInfo("❌ Unauthorized device detected.");
                if (!isHeadlessMode()) {
                    JOptionPane.showMessageDialog(null,
                            "Unauthorized device detected. Contact administrator.",
                            "Security Alert",
                            JOptionPane.ERROR_MESSAGE);
                }
                return false;
            }

            // Check subscription status
            SubscriptionService.SubscriptionStatus status = SubscriptionService.checkStatus();
            
            if (status == SubscriptionService.SubscriptionStatus.EXPIRED) {
                util.LoggerUtil.logInfo("❌ Subscription expired. Showing renewal blocking dialog...");
                
                // Get primary subscription info to get the ID
                SubscriptionService.SubscriptionInfo info = SubscriptionService.getSubscriptionInfo();
                if (info.getId() == null) {
                    util.LoggerUtil.logError("❌ Failed to fetch subscription for renewal", null);
                    return false;
                }

                // Generate OTP for renewal using the centralized, single-generation expiry handler
                String otp = SubscriptionService.handleExpiry(info.getId());
                if (otp == null) {
                    util.LoggerUtil.logError("❌ Failed to handle expiry and generate OTP", null);
                    return false;
                }

                // Show blocking renewal dialog
                return showSubscriptionExpiryDialog(info);
            } else if (status == SubscriptionService.SubscriptionStatus.WARNING) {
                long daysRemaining = SubscriptionService.getRemainingDays();
                util.LoggerUtil.logInfo("⚠ Subscription warning - expires in " + daysRemaining + " days");
            } else {
                long daysRemaining = SubscriptionService.getRemainingDays();
                util.LoggerUtil.logInfo("✅ Subscription active - expires in " + daysRemaining + " days");
            }

            return true;
        } catch (Exception e) {
            util.LoggerUtil.logError("❌ Subscription validation error: " + e.getMessage(), null);
            return true; // Allow app to run even if subscription check fails (safety fallback)
        }
    }

    /**
     * Show subscription expiry dialog with OTP entry
     * Returns true if subscription was renewed, false if app should be blocked
     */
    private static boolean showSubscriptionExpiryDialog(SubscriptionService.SubscriptionInfo subscription) {
        if (isHeadlessMode()) {
            util.LoggerUtil.logError("❌ Application expired and in headless mode", null);
            return false;
        }

        // Create callback for OTP verification
        final boolean[] renewed = {false};

        OTPEntryDialog.OTPVerificationCallback callback = new OTPEntryDialog.OTPVerificationCallback() {
            @Override
            public void onVerificationSuccess(LocalDate newExpiryDate) {
                util.LoggerUtil.logInfo("✅ Subscription renewed until: " + newExpiryDate);
                renewed[0] = true;
            }

            @Override
            public void onVerificationFailed(String errorMessage) {
                util.LoggerUtil.logError("❌ OTP verification failed: " + errorMessage, null);
                renewed[0] = false;
            }
        };

        // Show expiry dialog
        SubscriptionExpiryDialog.showExpiryDialog(subscription.getId(), subscription.getExpiryDate(), callback);

        // Block app if subscription not renewed
        return renewed[0];
    }

    /**
     * Check if running in headless mode (CLI / non-GUI)
     */
    private static boolean isHeadlessMode() {
        return GraphicsEnvironment.isHeadless();
    }
}
