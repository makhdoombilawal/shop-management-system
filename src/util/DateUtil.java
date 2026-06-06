package util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for date and time operations
 * 
 * @author Shop Management System
 */
public class DateUtil {
    
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    public static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
    public static final DateTimeFormatter REPORT_FORMAT = DateTimeFormatter.ofPattern("MMMM dd, yyyy");
    
    /**
     * Format LocalDateTime to date string
     */
    public static String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(DATE_FORMAT);
    }
    
    /**
     * Format LocalDateTime to time string
     */
    public static String formatTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(TIME_FORMAT);
    }
    
    /**
     * Format LocalDateTime to datetime string
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(DATETIME_FORMAT);
    }
    
    /**
     * Format for display purposes
     */
    public static String formatForDisplay(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(DISPLAY_FORMAT);
    }
    
    /**
     * Format for reports
     */
    public static String formatForReport(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(REPORT_FORMAT);
    }
    
    /**
     * Get start of day
     */
    public static LocalDateTime getStartOfDay(LocalDateTime dateTime) {
        return dateTime.withHour(0).withMinute(0).withSecond(0).withNano(0);
    }
    
    /**
     * Get end of day
     */
    public static LocalDateTime getEndOfDay(LocalDateTime dateTime) {
        return dateTime.withHour(23).withMinute(59).withSecond(59).withNano(999999999);
    }
    
    /**
     * Get start of today
     */
    public static LocalDateTime getStartOfToday() {
        return getStartOfDay(LocalDateTime.now());
    }
    
    /**
     * Get end of today
     */
    public static LocalDateTime getEndOfToday() {
        return getEndOfDay(LocalDateTime.now());
    }
    
    /**
     * Check if date is today
     */
    public static boolean isToday(LocalDateTime dateTime) {
        if (dateTime == null) return false;
        LocalDateTime now = LocalDateTime.now();
        return dateTime.toLocalDate().equals(now.toLocalDate());
    }
    
    /**
     * Get days between two dates
     */
    public static long getDaysBetween(LocalDateTime start, LocalDateTime end) {
        return java.time.Duration.between(start, end).toDays();
    }
}
