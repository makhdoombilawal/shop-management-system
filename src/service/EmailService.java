package service;

import dao.EmailQueueHibernateDAO;
import models.entity.EmailQueueEntity;
import util.InternetConnectivityUtil;
import util.SMTPUtil;

import java.time.LocalDate;
import java.util.List;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
/**
 * Email service for sending emails via SMTP.
 * Implements offline-first architecture with automatic queuing and retry.
 * 
 * NOTE: Simplified implementation without javax.mail dependency.
 * For production use, add javax.mail (JavaMail) API to dependencies.
 * 
 * @author Shop Management System
 * @version Enterprise Email System
 */
public class EmailService {

    private static final EmailQueueHibernateDAO emailQueueDao = new EmailQueueHibernateDAO();
    private static final ExecutorService emailExecutor = Executors.newFixedThreadPool(2);

    private EmailService() {
        // Service class
    }

    // ========================================================================
    // EMAIL SENDING
    // ========================================================================

    /**
     * Send email immediately if internet available, otherwise queue
     */
    public static boolean sendEmail(String emailType, String recipient, String subject, String body) {
        return sendEmail(emailType, recipient, subject, body, null);
    }

    /**
     * Send email immediately if internet available, otherwise queue (with dedupKey)
     */
    public static boolean sendEmail(String emailType, String recipient, String subject, String body, String dedupKey) {
        if (!SMTPUtil.isSmtpEnabled()) {
            util.LoggerUtil.logInfo("⚠ Email system disabled");
            return false;
        }

        if (!SMTPUtil.isSmtpConfigured()) {
            util.LoggerUtil.logInfo("⚠ SMTP not configured - queuing email");
            return queueEmail(emailType, recipient, subject, body, dedupKey);
        }

        // Check internet connectivity
        if (InternetConnectivityUtil.hasInternetConnection()) {
            emailExecutor.submit(() -> sendEmailViaSMTP(emailType, recipient, subject, body, dedupKey));
            return true;
        } else {
            util.LoggerUtil.logInfo("⚠ No internet - queuing email for later delivery");
            return queueEmail(emailType, recipient, subject, body, dedupKey);
        }
    }

    /**
     * Send email via SMTP server
     * NOTE: This implementation provides a placeholder for SMTP functionality.
     * To enable actual email sending, add javax.mail dependency and uncomment SMTP code.
     */
    private static boolean sendEmailViaSMTP(String emailType, String recipient, String subject, String body) {
        return sendEmailViaSMTP(emailType, recipient, subject, body, null);
    }

    private static boolean sendEmailViaSMTP(String emailType, String recipient, String subject, String body, String dedupKey) {
        try {
            Properties props = SMTPUtil.loadSMTPConfig();
            final String username = props.getProperty("mail.smtp.user");
            final String password = props.getProperty("mail.smtp.password");
            
            Session session = Session.getInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });
            
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SMTPUtil.getFromEmail(), SMTPUtil.getFromName()));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
            message.setSubject(subject);
            message.setText(body);
            
            Transport.send(message);
            
            util.LoggerUtil.logInfo("✅ Email sent successfully via SMTP: " + emailType + " -> " + recipient);
            return true;

        } catch (Exception e) {
            util.LoggerUtil.logError("❌ Error sending email: " + e.getMessage(), null);
            // Queue for later retry
            queueEmail(emailType, recipient, subject, body, dedupKey);
            return false;
        }
    }

    // ========================================================================
    // EMAIL QUEUING
    // ========================================================================

    /**
     * Queue email for offline delivery
     */
    public static boolean queueEmail(String emailType, String recipient, String subject, String body) {
        return queueEmail(emailType, recipient, subject, body, null);
    }

    /**
     * Queue email for offline delivery with deduplication key
     */
    public static boolean queueEmail(String emailType, String recipient, String subject, String body, String dedupKey) {
        try {
            // Check deduplication
            if (dedupKey != null && !dedupKey.isEmpty()) {
                if (emailQueueDao.isDuplicateEmail(dedupKey)) {
                    util.LoggerUtil.logInfo("⚠ Duplicate email blocked (dedup_key: " + dedupKey + ")");
                    return true; // Already sent/queued, treat as success
                }
            }

            EmailQueueEntity email = new EmailQueueEntity(emailType, recipient, subject, body);
            if (dedupKey != null) {
                email.setDedupKey(dedupKey);
            }
            emailQueueDao.save(email);
            util.LoggerUtil.logInfo("📧 Email queued: " + emailType + " -> " + recipient);

            return true;
        } catch (Exception e) {
            util.LoggerUtil.logError("❌ Error queuing email: " + e.getMessage(), null);
            return false;
        }
    }

    /**
     * Generate a deduplication key: subscriptionId_eventType_date
     */
    public static String generateDedupKey(Long subscriptionId, String eventType) {
        return subscriptionId + "_" + eventType + "_" + LocalDate.now();
    }

    // ========================================================================
    // BATCH EMAIL OPERATIONS
    // ========================================================================

    /**
     * Send queued emails (called periodically by background service)
     */
    public static int processQueuedEmails() {
        if (!InternetConnectivityUtil.isInternetAvailable()) {
            util.LoggerUtil.logInfo("⚠ No internet - queued emails will be retried later");
            return 0;
        }

        List<EmailQueueEntity> readyEmails = emailQueueDao.getEmailsReadyForRetry();
        
        if (readyEmails.isEmpty()) {
            return 0;
        }

        util.LoggerUtil.logInfo("📧 Processing " + readyEmails.size() + " queued email(s)...");
        int sentCount = 0;

        for (EmailQueueEntity email : readyEmails) {
            if (sendEmailViaSMTP(email.getEmailType(), email.getRecipient(), 
                                 email.getSubject(), email.getBody())) {
                emailQueueDao.markEmailAsSent(email.getId());
                sentCount++;
            } else {
                emailQueueDao.markEmailAsFailed(email.getId(), "SMTP send failed");
            }
            
            // Wait 10 seconds between emails to prevent spam/blocking
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        util.LoggerUtil.logInfo("✅ Processed queued emails: " + sentCount + " sent");
        return sentCount;
    }

    /**
     * Get pending email count
     */
    public static long getPendingEmailCount() {
        return emailQueueDao.getPendingEmailCount();
    }

    /**
     * Get email queue statistics
     */
    public static String getQueueStatistics() {
        return emailQueueDao.getQueueStatistics();
    }

    // ========================================================================
    // SPECIFIC EMAIL TYPES
    // ========================================================================

    /**
     * Send installation notification email
     */
    public static boolean sendInstallationEmail(String adminEmail, String deviceInfo, String installDate, String expiryDate) {
        String subject = "New Installation Alert - Shop Management System";
        String body = String.format(
                "Installation Notification\n\n" +
                "Installation Date: %s\n" +
                "Device Info: %s\n" +
                "Software Version: v1\n" +
                "Subscription Start: %s\n" +
                "Subscription End: %s\n\n" +
                "Please keep this information for your records.\n" +
                "Contact support if you have any questions.",
                installDate,
                deviceInfo,
                installDate,
                expiryDate
        );
        return sendEmail("INSTALLATION", adminEmail, subject, body);
    }

    /**
     * Send subscription expiry alert email
     */
    public static boolean sendExpiryAlertEmail(Long subscriptionId, String adminEmail, String deviceInfo, String expiryDate, String otpCode) {
        String subject = "OTP Verification Required - Subscription Renewal";
        String body = String.format(
                "Subscription Expiry Alert\n\n" +
                "Your subscription has expired on: %s\n" +
                "Device: %s\n" +
                "Software Version: v1\n\n" +
                "OTP for renewal verification: %s\n\n" +
                "Please provide this OTP to the client for subscription renewal.\n" +
                "OTP is valid for 15 minutes.\n\n" +
                "Contact support if you have any questions.",
                expiryDate,
                deviceInfo,
                otpCode
        );
        String dedupKey = generateDedupKey(subscriptionId, "EXPIRY");
        return sendEmail("OTP", adminEmail, subject, body, dedupKey);
    }

    /**
     * Send renewal success email
     */
    public static boolean sendRenewalSuccessEmail(String adminEmail, String newExpiryDate) {
        String subject = "Subscription Renewed Successfully";
        String body = String.format(
                "Subscription Renewal Success\n\n" +
                "The subscription has been successfully renewed.\n" +
                "New expiry date: %s\n\n" +
                "The system is now reactivated and ready for use.\n" +
                "Contact support if you have any questions.",
                newExpiryDate
        );
        return sendEmail("RENEWAL_SUCCESS", adminEmail, subject, body);
    }

    // ========================================================================
    // UTILITY METHODS
    // ========================================================================

    /**
     * Test email configuration
     */
    public static boolean testEmailConfiguration(String testEmail) {
        String subject = "Shop Management System - Email Configuration Test";
        String body = "This is a test email to verify SMTP configuration.\n" +
                      "If you received this, email delivery is working correctly.\n\n" +
                      "Test sent at: " + java.time.LocalDateTime.now();
        return sendEmail("TEST", testEmail, subject, body);
    }

    /**
     * Get email service status
     */
    public static String getServiceStatus() {
        StringBuilder status = new StringBuilder();
        status.append("Email Service Status\n");
        status.append("====================\n");
        status.append("SMTP Enabled: ").append(SMTPUtil.isSmtpEnabled()).append("\n");
        status.append("SMTP Configured: ").append(SMTPUtil.isSmtpConfigured()).append("\n");
        status.append("Internet Available: ").append(InternetConnectivityUtil.isInternetAvailable()).append("\n");
        status.append("Queue Statistics: ").append(getQueueStatistics()).append("\n");
        return status.toString();
    }
}
