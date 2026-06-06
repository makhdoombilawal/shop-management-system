package util;

import service.EmailService;

/**
 * Background email retry service.
 * Runs periodically to process queued emails when internet becomes available.
 * This ensures offline-first email delivery with automatic retry.
 * 
 * @author Shop Management System
 * @version Enterprise Email System
 */
public class EmailQueueProcessor extends Thread {

    private static final long RETRY_INTERVAL_MS = 10 * 60 * 1000; // 10 minutes
    private static final long SHUTDOWN_WAIT_MS = 5000; // 5 seconds
    private volatile boolean running = false;
    private static EmailQueueProcessor instance;

    public EmailQueueProcessor() {
        super("EmailQueueProcessor");
        setDaemon(true);
        setPriority(Thread.MIN_PRIORITY); // Low priority background task
    }

    /**
     * Get singleton instance
     */
    public static synchronized EmailQueueProcessor getInstance() {
        if (instance == null) {
            instance = new EmailQueueProcessor();
        }
        return instance;
    }

    /**
     * Start the email retry service
     */
    public synchronized void startService() {
        if (!running) {
            running = true;
            if (!isAlive()) {
                start();
            }
            util.LoggerUtil.logInfo("✅ Email Queue Processor started");
        }
    }

    /**
     * Stop the email retry service
     */
    public synchronized void stopService() {
        running = false;
        util.LoggerUtil.logInfo("⏹ Email Queue Processor stopping...");
        
        // Wait for thread to finish
        try {
            join(SHUTDOWN_WAIT_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Main service loop
     */
    @Override
    public void run() {
        util.LoggerUtil.logInfo("📧 Email Queue Processor running...");

        while (running) {
            try {
                // Wait before checking again
                Thread.sleep(RETRY_INTERVAL_MS);

                if (!running) {
                    break;
                }

                // Check if internet is available
                if (InternetConnectivityUtil.isInternetAvailable()) {
                    processQueuedEmails();
                } else {
                    long pendingCount = EmailService.getPendingEmailCount();
                    if (pendingCount > 0) {
                        util.LoggerUtil.logInfo("⚠ No internet connection - " + pendingCount + " email(s) waiting to send");
                    }
                }

            } catch (InterruptedException e) {
                if (running) {
                    util.LoggerUtil.logError("⚠ Email Queue Processor interrupted", null);
                }
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                util.LoggerUtil.logError("❌ Email Queue Processor error: " + e.getMessage(), null);
            }
        }

        util.LoggerUtil.logInfo("📧 Email Queue Processor stopped");
    }

    /**
     * Process queued emails
     */
    private void processQueuedEmails() {
        try {
            long pendingCount = EmailService.getPendingEmailCount();
            if (pendingCount > 0) {
                util.LoggerUtil.logInfo("📧 Processing " + pendingCount + " queued email(s)...");
                int sentCount = EmailService.processQueuedEmails();
                if (sentCount > 0) {
                    util.LoggerUtil.logInfo("✅ " + sentCount + " email(s) sent successfully");
                }
            }
        } catch (Exception e) {
            util.LoggerUtil.logError("❌ Error processing queued emails: " + e.getMessage(), null);
        }
    }

    /**
     * Force immediate retry of queued emails
     */
    public void forceRetry() {
        if (running) {
            util.LoggerUtil.logInfo("📧 Forcing email retry check...");
            processQueuedEmails();
        }
    }

    /**
     * Get service status
     */
    public String getStatus() {
        return String.format(
                "Email Queue Processor\n" +
                "  Status: %s\n" +
                "  Thread Running: %s\n" +
                "  Retry Interval: %d minutes\n" +
                "  %s",
                running ? "RUNNING" : "STOPPED",
                isAlive() ? "Yes" : "No",
                RETRY_INTERVAL_MS / (60 * 1000),
                EmailService.getQueueStatistics()
        );
    }
}
