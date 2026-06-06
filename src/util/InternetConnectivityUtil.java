package util;

import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;

/**
 * Internet connectivity utility.
 * Checks if the application has internet access for email operations.
 * Supports multiple check methods for reliability.
 * 
 * @author Shop Management System
 * @version Enterprise
 */
public final class InternetConnectivityUtil {

    private InternetConnectivityUtil() {
        // Utility class
    }

    // Cache for connectivity check (refresh every 30 seconds)
    private static long lastCheckTime = 0;
    private static boolean lastCheckResult = false;
    private static final long CACHE_DURATION_MS = 30000; // 30 seconds

    /**
     * Check if internet is available using primary method (Google DNS)
     */
    public static boolean isInternetAvailable() {
        return isInternetAvailable(true);
    }

    /**
     * Check if internet is available with optional caching
     */
    public static boolean isInternetAvailable(boolean useCache) {
        // Use cache if valid
        if (useCache && isCacheValid()) {
            return lastCheckResult;
        }

        boolean result = checkConnectivity();
        
        // Update cache
        lastCheckTime = System.currentTimeMillis();
        lastCheckResult = result;
        
        return result;
    }

    /**
     * Check connectivity using multiple methods
     */
    private static boolean checkConnectivity() {
        // Try multiple methods for reliability
        return checkGoogleDns() || checkPublicDns() || checkHttpConnection();
    }

    /**
     * Check connectivity via Google DNS (most reliable)
     */
    private static boolean checkGoogleDns() {
        try (Socket socket = new Socket()) {
            socket.connect(new java.net.InetSocketAddress("8.8.8.8", 53), 3000);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check connectivity via Cloudflare DNS
     */
    private static boolean checkPublicDns() {
        try (Socket socket = new Socket()) {
            socket.connect(new java.net.InetSocketAddress("1.1.1.1", 53), 3000);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check connectivity via HTTP connection to Google
     */
    private static boolean checkHttpConnection() {
        try {
            @SuppressWarnings("deprecation")
            URL url = new URL("http://www.google.com");
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(3000);
            connection.setReadTimeout(3000);
            connection.getInputStream().close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Check if cache is still valid
     */
    private static boolean isCacheValid() {
        return (System.currentTimeMillis() - lastCheckTime) < CACHE_DURATION_MS;
    }

    /**
     * Clear cache to force fresh check
     */
    public static void clearCache() {
        lastCheckTime = 0;
        lastCheckResult = false;
    }

    /**
     * Get connectivity status with logging
     */
    public static boolean hasInternetConnection() {
        boolean available = isInternetAvailable();
        if (available) {
            util.LoggerUtil.logInfo("✅ Internet connection available");
        } else {
            util.LoggerUtil.logInfo("⚠ No internet connection - emails will be queued");
        }
        return available;
    }

    /**
     * Check if SMTP host is reachable
     */
    public static boolean isSmtpHostReachable(String smtpHost, int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new java.net.InetSocketAddress(smtpHost, port), 5000);
            return true;
        } catch (Exception e) {
            util.LoggerUtil.logError("⚠ SMTP host " + smtpHost + ":" + port + " not reachable", null);
            return false;
        }
    }
}
