package util;

import javax.swing.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Centralized logging utility for the application
 * 
 * @author Shop Management System
 */
public class LoggerUtil {
    
    private static final Logger LOGGER = Logger.getLogger("ShopManagementSystem");
    
    /**
     * Log info message
     */
    public static void logInfo(String message) {
        LOGGER.log(Level.INFO, message);
    }
    
    /**
     * Log info message with class context
     */
    public static void logInfo(Class<?> clazz, String message) {
        LOGGER.log(Level.INFO, "[" + clazz.getSimpleName() + "] " + message);
    }
    
    /**
     * Log warning message
     */
    public static void logWarning(String message) {
        LOGGER.log(Level.WARNING, message);
    }
    
    /**
     * Log warning with class context
     */
    public static void logWarning(Class<?> clazz, String message) {
        LOGGER.log(Level.WARNING, "[" + clazz.getSimpleName() + "] " + message);
    }
    
    /**
     * Log error message
     */
    public static void logError(String message, Throwable throwable) {
        LOGGER.log(Level.SEVERE, message, throwable);
    }
    
    /**
     * Log error with class context
     */
    public static void logError(Class<?> clazz, String message, Throwable throwable) {
        LOGGER.log(Level.SEVERE, "[" + clazz.getSimpleName() + "] " + message, throwable);
    }
    
    /**
     * Log and show error dialog
     */
    public static void logAndShowError(String message, Throwable throwable) {
        LOGGER.log(Level.SEVERE, message, throwable);
        JOptionPane.showMessageDialog(null, 
            message + "\n" + throwable.getMessage(),
            "Error",
            JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Log debug message (only in development)
     */
    public static void logDebug(String message) {
        if (isDebugMode()) {
            LOGGER.log(Level.FINE, "[DEBUG] " + message);
        }
    }
    
    /**
     * Check if debug mode is enabled
     */
    private static boolean isDebugMode() {
        return System.getProperty("debug") != null;
    }
}
