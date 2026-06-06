package util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * SMTP email configuration utility.
 * Loads and manages SMTP settings for email delivery.
 * 
 * @author Shop Management System
 * @version Enterprise Email System
 */
public final class SMTPUtil {

    private static final String CONFIG_FILE = "SMTPConfig.properties";
    private static Properties smtpProperties;

    private SMTPUtil() {
        // Utility class
    }

    private static synchronized void initialize() {
        if (smtpProperties == null) {
            smtpProperties = new Properties();
            try (FileInputStream in = new FileInputStream(CONFIG_FILE)) {
                smtpProperties.load(in);
            } catch (IOException e) {
                LoggerUtil.logError(SMTPUtil.class, "Failed to load " + CONFIG_FILE, e);
            }
        }
    }

    public static String getConfigValue(String key, String defaultValue) {
        initialize();
        return smtpProperties.getProperty(key, defaultValue);
    }

    // ========================================================================
    // SMTP CONFIGURATION KEYS
    // ========================================================================

    public static final String SMTP_HOST = "smtp.host";
    public static final String SMTP_PORT = "smtp.port";
    public static final String SMTP_USERNAME = "smtp.username";
    public static final String SMTP_PASSWORD = "smtp.password";
    public static final String SMTP_USE_TLS = "smtp.starttls.enable";
    public static final String SMTP_USE_SSL = "smtp.use_ssl";
    public static final String SMTP_FROM_EMAIL = "smtp.from_email";
    public static final String SMTP_FROM_NAME = "smtp.from_name";
    public static final String SMTP_ENABLED = "smtp.enabled";

    // Removed unused default getter methods for specific providers

    // ========================================================================
    // LOAD FROM CONFIG
    // ========================================================================

    /**
     * Load SMTP configuration from application config
     */
    public static Properties loadSMTPConfig() {
        Properties props = new Properties();
        
        String host = getConfigValue(SMTP_HOST, "smtp.gmail.com");
        String portStr = getConfigValue(SMTP_PORT, "587");
        String username = getConfigValue(SMTP_USERNAME, "");
        String password = getConfigValue(SMTP_PASSWORD, "");
        String useTls = getConfigValue(SMTP_USE_TLS, "true");
        String useSsl = getConfigValue(SMTP_USE_SSL, "false");

        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", portStr);
        props.put("mail.smtp.auth", "true");
        
        if ("true".equalsIgnoreCase(useTls)) {
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.starttls.required", "true");
        }
        
        if ("true".equalsIgnoreCase(useSsl)) {
            props.put("mail.smtp.socketFactory.port", portStr);
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        }
        
        props.put("mail.smtp.user", username);
        props.put("mail.smtp.password", password);
        
        return props;
    }

    // ========================================================================
    // SMTP SETTINGS GETTERS
    // ========================================================================

    public static String getSmtpHost() {
        return getConfigValue(SMTP_HOST, "smtp.gmail.com");
    }

    public static int getSmtpPort() {
        try {
            return Integer.parseInt(getConfigValue(SMTP_PORT, "587"));
        } catch (NumberFormatException e) {
            return 587;
        }
    }

    public static String getSmtpUsername() {
        return getConfigValue(SMTP_USERNAME, "");
    }

    public static String getSmtpPassword() {
        return getConfigValue(SMTP_PASSWORD, "");
    }

    public static String getFromEmail() {
        return getConfigValue(SMTP_FROM_EMAIL, getSmtpUsername());
    }

    public static String getFromName() {
        return getConfigValue(SMTP_FROM_NAME, "Shop Management System");
    }

    public static boolean isSmtpEnabled() {
        return "true".equalsIgnoreCase(getConfigValue(SMTP_ENABLED, "true"));
    }

    public static boolean isSmtpConfigured() {
        String username = getSmtpUsername();
        String password = getSmtpPassword();
        return username != null && !username.isEmpty() && password != null && !password.isEmpty();
    }

    // ========================================================================
    // VALIDATION
    // ========================================================================

    /**
     * Validate SMTP configuration
     */
    public static boolean validateSmtpConfig() {
        if (!isSmtpEnabled()) {
            util.LoggerUtil.logInfo("⚠ SMTP is disabled in configuration");
            return false;
        }

        if (!isSmtpConfigured()) {
            util.LoggerUtil.logInfo("⚠ SMTP credentials not configured");
            return false;
        }

        String host = getSmtpHost();
        int port = getSmtpPort();

        if (host == null || host.isEmpty()) {
            util.LoggerUtil.logInfo("❌ SMTP host not configured");
            return false;
        }

        util.LoggerUtil.logInfo("✅ SMTP configuration valid: " + host + ":" + port);
        return true;
    }

    /**
     * Get SMTP configuration summary
     */
    public static String getConfigSummary() {
        return String.format(
                "SMTP Configuration:\n" +
                "  Host: %s\n" +
                "  Port: %d\n" +
                "  Username: %s\n" +
                "  From: %s <%s>\n" +
                "  TLS: %s\n" +
                "  SSL: %s\n" +
                "  Enabled: %s",
                getSmtpHost(),
                getSmtpPort(),
                getSmtpUsername(),
                getFromName(),
                getFromEmail(),
                getConfigValue(SMTP_USE_TLS, "true"),
                getConfigValue(SMTP_USE_SSL, "false"),
                isSmtpEnabled()
        );
    }
}
