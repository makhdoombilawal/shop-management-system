package util;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Security utility for handling login attempts and account lockouts
 * Prevents brute force attacks by limiting login attempts
 * 
 * @author Shop Management System
 */
public class SecurityUtil {
    
    // Store failed login attempts: username -> LoginAttempt
    private static final Map<String, LoginAttempt> loginAttempts = new ConcurrentHashMap<>();
    
    // Configuration
    private static final int MAX_ATTEMPTS = 5;
    private static final int LOCKOUT_DURATION_MINUTES = 15;
    private static final int ATTEMPT_RESET_MINUTES = 30;
    
    /**
     * Record a failed login attempt
     * @param username The username that failed to login
     * @return true if account is now locked
     */
    public static boolean recordFailedAttempt(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        
        username = username.toLowerCase().trim();
        LoginAttempt attempt = loginAttempts.get(username);
        
        if (attempt == null) {
            attempt = new LoginAttempt();
            loginAttempts.put(username, attempt);
        }
        
        attempt.incrementFailed();
        
        // Check if account should be locked
        if (attempt.getFailedAttempts() >= MAX_ATTEMPTS) {
            attempt.lockAccount();
            LoggerUtil.logWarning(SecurityUtil.class, 
                "Account locked due to too many failed attempts: " + username);
            return true;
        }
        
        return false;
    }
    
    /**
     * Record a successful login (resets failed attempts)
     * @param username The username that successfully logged in
     */
    public static void recordSuccessfulLogin(String username) {
        if (username == null || username.trim().isEmpty()) {
            return;
        }
        
        username = username.toLowerCase().trim();
        loginAttempts.remove(username); // Clear failed attempts
        
        LoggerUtil.logInfo(SecurityUtil.class, "Successful login: " + username);
    }
    
    /**
     * Check if account is locked
     * @param username The username to check
     * @return true if account is locked
     */
    public static boolean isAccountLocked(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        
        username = username.toLowerCase().trim();
        LoginAttempt attempt = loginAttempts.get(username);
        
        if (attempt == null) {
            return false;
        }
        
        // Check if lockout period has expired
        if (attempt.isLocked()) {
            long minutesLocked = ChronoUnit.MINUTES.between(
                attempt.getLockoutTime(), LocalDateTime.now());
            
            if (minutesLocked >= LOCKOUT_DURATION_MINUTES) {
                // Unlock account
                attempt.unlock();
                LoggerUtil.logInfo(SecurityUtil.class, 
                    "Account automatically unlocked: " + username);
                return false;
            }
            return true;
        }
        
        return false;
    }
    
    /**
     * Get number of failed attempts for a username
     * @param username The username to check
     * @return Number of failed attempts
     */
    public static int getFailedAttempts(String username) {
        if (username == null || username.trim().isEmpty()) {
            return 0;
        }
        
        username = username.toLowerCase().trim();
        LoginAttempt attempt = loginAttempts.get(username);
        
        return (attempt != null) ? attempt.getFailedAttempts() : 0;
    }
    
    /**
     * Get remaining lockout time in minutes
     * @param username The username to check
     * @return Minutes remaining in lockout period (0 if not locked)
     */
    public static long getRemainingLockoutMinutes(String username) {
        if (username == null || username.trim().isEmpty()) {
            return 0;
        }
        
        username = username.toLowerCase().trim();
        LoginAttempt attempt = loginAttempts.get(username);
        
        if (attempt != null && attempt.isLocked()) {
            long minutesLocked = ChronoUnit.MINUTES.between(
                attempt.getLockoutTime(), LocalDateTime.now());
            long remaining = LOCKOUT_DURATION_MINUTES - minutesLocked;
            return Math.max(0, remaining);
        }
        
        return 0;
    }
    
    /**
     * Manually unlock an account (for admin use)
     * @param username The username to unlock
     */
    public static void unlockAccount(String username) {
        if (username == null || username.trim().isEmpty()) {
            return;
        }
        
        username = username.toLowerCase().trim();
        loginAttempts.remove(username);
        
        LoggerUtil.logInfo(SecurityUtil.class, 
            "Account manually unlocked: " + username);
    }
    
    /**
     * Clean up old login attempts (should be called periodically)
     */
    public static void cleanupOldAttempts() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(ATTEMPT_RESET_MINUTES);
        
        loginAttempts.entrySet().removeIf(entry -> {
            LoginAttempt attempt = entry.getValue();
            return !attempt.isLocked() && 
                   attempt.getLastAttemptTime().isBefore(cutoffTime);
        });
    }
    
    /**
     * Inner class to track login attempts
     */
    private static class LoginAttempt {
        private int failedAttempts;
        private LocalDateTime lastAttemptTime;
        private LocalDateTime lockoutTime;
        private boolean locked;
        
        public LoginAttempt() {
            this.failedAttempts = 0;
            this.lastAttemptTime = LocalDateTime.now();
            this.locked = false;
        }
        
        public void incrementFailed() {
            this.failedAttempts++;
            this.lastAttemptTime = LocalDateTime.now();
        }
        
        public void lockAccount() {
            this.locked = true;
            this.lockoutTime = LocalDateTime.now();
        }
        
        public void unlock() {
            this.locked = false;
            this.failedAttempts = 0;
            this.lockoutTime = null;
        }
        
        public int getFailedAttempts() {
            return failedAttempts;
        }
        
        public LocalDateTime getLastAttemptTime() {
            return lastAttemptTime;
        }
        
        public LocalDateTime getLockoutTime() {
            return lockoutTime;
        }
        
        public boolean isLocked() {
            return locked;
        }
    }
    
    /**
     * Get configuration values (for display in admin panel)
     */
    public static String getSecurityConfig() {
        return String.format(
            "Security Configuration:\n" +
            "- Max login attempts: %d\n" +
            "- Lockout duration: %d minutes\n" +
            "- Attempt reset time: %d minutes",
            MAX_ATTEMPTS, LOCKOUT_DURATION_MINUTES, ATTEMPT_RESET_MINUTES
        );
    }
}
