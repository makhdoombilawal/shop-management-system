package util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JOptionPane;

/**
 * Centralized logging utility for the application
 * Uses SLF4J API with Logback backend supporting file rotation and formatting
 * 
 * @author Shop Management System
 */
public class LoggerUtil {
    
    private static final Logger DEFAULT_LOGGER = LoggerFactory.getLogger("ShopManagementSystem");
    
    private static Logger getLogger(Class<?> clazz) {
        return clazz != null ? LoggerFactory.getLogger(clazz) : DEFAULT_LOGGER;
    }

    /**
     * Log info message
     */
    public static void logInfo(String message) {
        DEFAULT_LOGGER.info(message);
    }
    
    /**
     * Log info message with class context
     */
    public static void logInfo(Class<?> clazz, String message) {
        getLogger(clazz).info(message);
    }
    
    /**
     * Log warning message
     */
    public static void logWarning(String message) {
        DEFAULT_LOGGER.warn(message);
    }
    
    /**
     * Log warning with class context
     */
    public static void logWarning(Class<?> clazz, String message) {
        getLogger(clazz).warn(message);
    }
    
    /**
     * Log error message
     */
    public static void logError(String message, Throwable throwable) {
        DEFAULT_LOGGER.error(message, throwable);
    }
    
    /**
     * Log error with class context
     */
    public static void logError(Class<?> clazz, String message, Throwable throwable) {
        getLogger(clazz).error(message, throwable);
    }
    
    /**
     * Log and show error dialog
     */
    public static void logAndShowError(String message, Throwable throwable) {
        DEFAULT_LOGGER.error(message, throwable);
        if (!isHeadlessMode()) {
            JOptionPane.showMessageDialog(null, 
                message + (throwable != null ? "\n" + throwable.getMessage() : ""),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Log debug message
     */
    public static void logDebug(String message) {
        DEFAULT_LOGGER.debug(message);
    }

    public static void logDebug(Class<?> clazz, String message) {
        getLogger(clazz).debug(message);
    }

    private static boolean isHeadlessMode() {
        return "true".equalsIgnoreCase(System.getProperty("shop.headless"))
                || java.awt.GraphicsEnvironment.isHeadless();
    }
}
