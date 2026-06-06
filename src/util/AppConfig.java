package util;

/**
 * Application configuration with version-based licensing.
 * Hardcoded version is used for license validation.
 * 
 * @author Shop Management System
 * @version Enterprise License System
 */
public final class AppConfig {

    private AppConfig() {
        // Utility class
    }

    // ============================================================================
    // VERSION & LICENSE CONFIGURATION
    // ============================================================================

    /**
     * Application version (hardcoded). Update this for new releases.
     * This value must match the DB installed_version for the app to run.
     */
    public static final String APP_VERSION = "v1";

    /**
     * Future upgrade version used for version compatibility validation.
     */
    public static final String NEXT_VERSION = "v2";

    /**
     * License expiry duration in months.
     * After this period, the app will be locked unless updated.
     */
    public static final int EXPIRY_MONTHS = 2;

    /**
     * Admin contact for license renewal.
     */
    public static final String ADMIN_CONTACT_PHONE = "+92-XXX-XXXXXXX";
    public static final String ADMIN_CONTACT_NAME = "Administrator";

    // ============================================================================
    // UI & MESSAGING
    // ============================================================================

    public static final String EXPIRY_DIALOG_TITLE = "Application Update Required";

    public static final String EXPIRY_DIALOG_MESSAGE = 
        "Your software version has expired.\n" +
        "Please update your application.\n" +
        "Contact Admin: " + ADMIN_CONTACT_PHONE;

    public static final String VERSION_MISMATCH_MESSAGE = 
        "Your software version is outdated.\n" +
        "Please install the latest version.\n" +
        "Contact Admin: " + ADMIN_CONTACT_PHONE;

    public static final String POS_WARNING_BANNER = 
        "⚠ Application expired. Update required. Contact Admin: " + ADMIN_CONTACT_PHONE;

    // ============================================================================
    // DATABASE CONFIGURATION
    // ============================================================================

    /**
     * Enable enhanced security (optional hash validation)
     */
    public static final boolean ENHANCED_SECURITY = false;

    /**
     * Grace period in days after expiry (optional)
     */
    public static final int GRACE_PERIOD_DAYS = 0;

    // ============================================================================
    // DISPLAY MESSAGES FOR POS & DASHBOARDS
    // ============================================================================

    public static String getExpiryMessage() {
        return EXPIRY_DIALOG_MESSAGE;
    }

    public static String getVersionMismatchMessage() {
        return VERSION_MISMATCH_MESSAGE;
    }

}
