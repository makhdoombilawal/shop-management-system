package dao;

import models.entity.EmailQueueEntity;
import util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.query.Query;
import java.util.List;

/**
 * Data Access Object for email queue management.
 * Handles all database operations for offline-first email delivery system.
 * 
 * @author Shop Management System
 * @version Enterprise Email Queue DAO
 */
public class EmailQueueHibernateDAO extends GenericDAO<EmailQueueEntity, Long> {

    public EmailQueueHibernateDAO() {
        super(EmailQueueEntity.class);
    }

    // ========================================================================
    // PENDING EMAIL OPERATIONS
    // ========================================================================

    /**
     * Get all pending emails
     */
    public List<EmailQueueEntity> getPendingEmails() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<EmailQueueEntity> query = session.createQuery(
                    "FROM EmailQueueEntity WHERE status = 'PENDING' ORDER BY createdAt ASC",
                    EmailQueueEntity.class
            );
            return query.getResultList();
        } catch (Exception e) {
            util.LoggerUtil.logError("❌ Error fetching pending emails: " + e.getMessage(), null);
            return List.of();
        }
    }

    /**
     * Get emails ready for retry (max 3 retries)
     */
    public List<EmailQueueEntity> getEmailsReadyForRetry() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<EmailQueueEntity> query = session.createQuery(
                    "FROM EmailQueueEntity WHERE (status = 'PENDING' OR status = 'FAILED') AND retryCount < 3 ORDER BY createdAt ASC",
                    EmailQueueEntity.class
            );
            List<EmailQueueEntity> allRetryable = query.getResultList();
            
            // Filter by readiness (backoff logic)
            return allRetryable.stream()
                    .filter(EmailQueueEntity::isReadyForRetry)
                    .toList();
        } catch (Exception e) {
            util.LoggerUtil.logError("❌ Error fetching emails ready for retry: " + e.getMessage(), null);
            return List.of();
        }
    }

    /**
     * Check if an email with this dedup key already exists and was sent
     */
    public boolean isDuplicateEmail(String dedupKey) {
        if (dedupKey == null || dedupKey.isEmpty()) {
            return false;
        }
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(
                    "SELECT COUNT(e) FROM EmailQueueEntity e WHERE e.dedupKey = :key AND (e.status = 'SENT' OR e.status = 'PENDING')",
                    Long.class
            );
            query.setParameter("key", dedupKey);
            return query.getSingleResult() > 0;
        } catch (Exception e) {
            util.LoggerUtil.logError("❌ Error checking dedup key: " + e.getMessage(), null);
            return false;
        }
    }

    /**
     * Get count of pending emails
     */
    public long getPendingEmailCount() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(
                    "SELECT COUNT(e) FROM EmailQueueEntity e WHERE status = 'PENDING'",
                    Long.class
            );
            return query.getSingleResult();
        } catch (Exception e) {
            util.LoggerUtil.logError("❌ Error counting pending emails: " + e.getMessage(), null);
            return 0;
        }
    }

    // ========================================================================
    // EMAIL STATUS OPERATIONS
    // ========================================================================

    /**
     * Mark email as sent
     */
    public boolean markEmailAsSent(Long emailId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            EmailQueueEntity email = session.get(EmailQueueEntity.class, emailId);
            if (email != null) {
                email.markAsSent();
                session.merge(email);
                session.getTransaction().commit();
                return true;
            }
            session.getTransaction().rollback();
            return false;
        } catch (Exception e) {
            util.LoggerUtil.logError("❌ Error marking email as sent: " + e.getMessage(), null);
            return false;
        }
    }

    /**
     * Mark email as failed with error message
     */
    public boolean markEmailAsFailed(Long emailId, String errorMessage) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            EmailQueueEntity email = session.get(EmailQueueEntity.class, emailId);
            if (email != null) {
                email.markAsFailed(errorMessage);
                session.merge(email);
                session.getTransaction().commit();
                return true;
            }
            session.getTransaction().rollback();
            return false;
        } catch (Exception e) {
            util.LoggerUtil.logError("❌ Error marking email as failed: " + e.getMessage(), null);
            return false;
        }
    }

    /**
     * Reset email for retry
     */
    public boolean resetEmailForRetry(Long emailId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            EmailQueueEntity email = session.get(EmailQueueEntity.class, emailId);
            if (email != null) {
                email.resetForRetry();
                session.merge(email);
                session.getTransaction().commit();
                return true;
            }
            session.getTransaction().rollback();
            return false;
        } catch (Exception e) {
            util.LoggerUtil.logError("❌ Error resetting email for retry: " + e.getMessage(), null);
            return false;
        }
    }

    // ========================================================================
    // EMAIL TYPE OPERATIONS
    // ========================================================================

    /**
     * Get latest email of specific type
     */
    public EmailQueueEntity getLatestEmailOfType(String emailType) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<EmailQueueEntity> query = session.createQuery(
                    "FROM EmailQueueEntity WHERE emailType = :type ORDER BY createdAt DESC LIMIT 1",
                    EmailQueueEntity.class
            );
            query.setParameter("type", emailType);
            List<EmailQueueEntity> results = query.getResultList();
            return results.isEmpty() ? null : results.get(0);
        } catch (Exception e) {
            util.LoggerUtil.logError("❌ Error fetching latest email of type " + emailType + ": " + e.getMessage(), null);
            return null;
        }
    }

    /**
     * Get all emails of specific type
     */
    public List<EmailQueueEntity> getEmailsOfType(String emailType) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<EmailQueueEntity> query = session.createQuery(
                    "FROM EmailQueueEntity WHERE emailType = :type ORDER BY createdAt DESC",
                    EmailQueueEntity.class
            );
            query.setParameter("type", emailType);
            return query.getResultList();
        } catch (Exception e) {
            util.LoggerUtil.logError("❌ Error fetching emails of type " + emailType + ": " + e.getMessage(), null);
            return List.of();
        }
    }

    /**
     * Get emails sent to specific recipient
     */
    public List<EmailQueueEntity> getEmailsForRecipient(String recipient) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<EmailQueueEntity> query = session.createQuery(
                    "FROM EmailQueueEntity WHERE recipient = :recipient ORDER BY createdAt DESC",
                    EmailQueueEntity.class
            );
            query.setParameter("recipient", recipient);
            return query.getResultList();
        } catch (Exception e) {
            util.LoggerUtil.logError("❌ Error fetching emails for recipient: " + e.getMessage(), null);
            return List.of();
        }
    }

    // ========================================================================
    // CLEANUP OPERATIONS
    // ========================================================================

    /**
     * Delete old sent emails (older than 30 days)
     */
    public boolean deleteOldSentEmails() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            Query<?> query = session.createQuery(
                    "DELETE FROM EmailQueueEntity WHERE status = 'SENT' AND sentAt < CURRENT_TIMESTAMP - INTERVAL 30 DAY"
            );
            int deletedCount = query.executeUpdate();
            session.getTransaction().commit();
            util.LoggerUtil.logInfo("✅ Deleted " + deletedCount + " old sent emails");
            return true;
        } catch (Exception e) {
            util.LoggerUtil.logError("❌ Error deleting old sent emails: " + e.getMessage(), null);
            return false;
        }
    }

    /**
     * Get email queue statistics
     */
    public String getQueueStatistics() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> pendingQuery = session.createQuery(
                    "SELECT COUNT(e) FROM EmailQueueEntity e WHERE status = 'PENDING'",
                    Long.class
            );
            Query<Long> sentQuery = session.createQuery(
                    "SELECT COUNT(e) FROM EmailQueueEntity e WHERE status = 'SENT'",
                    Long.class
            );
            Query<Long> failedQuery = session.createQuery(
                    "SELECT COUNT(e) FROM EmailQueueEntity e WHERE status = 'FAILED'",
                    Long.class
            );

            long pending = pendingQuery.getSingleResult();
            long sent = sentQuery.getSingleResult();
            long failed = failedQuery.getSingleResult();

            return String.format("Pending: %d | Sent: %d | Failed: %d", pending, sent, failed);
        } catch (Exception e) {
            util.LoggerUtil.logError("❌ Error getting queue statistics: " + e.getMessage(), null);
            return "Unable to fetch statistics";
        }
    }
}
