package dao;

import models.entity.SubscriptionEntity;
import util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.query.Query;
import java.time.LocalDate;
import java.util.List;

/**
 * Data Access Object for subscription management.
 * Handles all database operations for subscription tracking and OTP management.
 * 
 * @author Shop Management System
 * @version Enterprise Subscription DAO
 */
public class SubscriptionHibernateDAO extends GenericDAO<SubscriptionEntity, Long> {

    public SubscriptionHibernateDAO() {
        super(SubscriptionEntity.class);
    }

    // ========================================================================
    // PRIMARY SUBSCRIPTION OPERATIONS
    // ========================================================================

    /**
     * Get the primary (or only) subscription record
     */
    public SubscriptionEntity getPrimarySubscription() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<SubscriptionEntity> query = session.createQuery(
                    "FROM SubscriptionEntity ORDER BY id ASC",
                    SubscriptionEntity.class
            );
            query.setMaxResults(1);
            List<SubscriptionEntity> results = query.getResultList();
            return results.isEmpty() ? null : results.get(0);
        } catch (Exception e) {
            util.LoggerUtil.logError("❌ Error fetching primary subscription: " + e.getMessage(), null);
            return null;
        }
    }

    /**
     * Check if subscription exists
     */
    public boolean hasActiveSubscription() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery(
                    "SELECT COUNT(s) FROM SubscriptionEntity s WHERE s.status = 'ACTIVE'",
                    Long.class
            );
            return query.getSingleResult() > 0;
        } catch (Exception e) {
            util.LoggerUtil.logError("❌ Error checking subscription: " + e.getMessage(), null);
            return false;
        }
    }

    /**
     * Get subscription by version
     */
    public SubscriptionEntity getSubscriptionByVersion(String version) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<SubscriptionEntity> query = session.createQuery(
                    "FROM SubscriptionEntity WHERE installedVersion = :version",
                    SubscriptionEntity.class
            );
            query.setParameter("version", version);
            List<SubscriptionEntity> results = query.getResultList();
            return results.isEmpty() ? null : results.get(0);
        } catch (Exception e) {
            util.LoggerUtil.logError("❌ Error fetching subscription by version: " + e.getMessage(), null);
            return null;
        }
    }

    // ========================================================================
    // EXPIRY & STATUS OPERATIONS
    // ========================================================================

    /**
     * Find all expired subscriptions
     */
    public List<SubscriptionEntity> getExpiredSubscriptions() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<SubscriptionEntity> query = session.createQuery(
                    "FROM SubscriptionEntity WHERE expiryDate <= :today ORDER BY expiryDate ASC",
                    SubscriptionEntity.class
            );
            query.setParameter("today", LocalDate.now());
            return query.getResultList();
        } catch (Exception e) {
            util.LoggerUtil.logError("❌ Error fetching expired subscriptions: " + e.getMessage(), null);
            return List.of();
        }
    }

    /**
     * Update subscription status
     */
    public boolean updateSubscriptionStatus(Long subscriptionId, String newStatus) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            SubscriptionEntity subscription = session.get(SubscriptionEntity.class, subscriptionId);
            if (subscription != null) {
                subscription.setStatus(newStatus);
                subscription.updateStatus();
                session.merge(subscription);
                session.getTransaction().commit();
                return true;
            }
            session.getTransaction().rollback();
            return false;
        } catch (Exception e) {
            util.LoggerUtil.logError("❌ Error updating subscription status: " + e.getMessage(), null);
            return false;
        }
    }

    // ========================================================================
    // OTP OPERATIONS
    // ========================================================================

    /**
     * Get subscription with pending OTP
     */
    public SubscriptionEntity getSubscriptionWithPendingOtp() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<SubscriptionEntity> query = session.createQuery(
                    "FROM SubscriptionEntity WHERE otpCode IS NOT NULL AND otpUsed = false ORDER BY otpGeneratedAt DESC LIMIT 1",
                    SubscriptionEntity.class
            );
            List<SubscriptionEntity> results = query.getResultList();
            return results.isEmpty() ? null : results.get(0);
        } catch (Exception e) {
            util.LoggerUtil.logError("❌ Error fetching subscription with pending OTP: " + e.getMessage(), null);
            return null;
        }
    }

    /**
     * Update OTP for subscription
     */
    public boolean updateOtp(Long subscriptionId, String otpCode) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            SubscriptionEntity subscription = session.get(SubscriptionEntity.class, subscriptionId);
            if (subscription != null) {
                subscription.setNewOTP(otpCode);
                subscription.setOtpLockedUntil(null);
                session.merge(subscription);
                session.getTransaction().commit();
                return true;
            }
            session.getTransaction().rollback();
            return false;
        } catch (Exception e) {
            util.LoggerUtil.logError("❌ Error updating OTP: " + e.getMessage(), null);
            return false;
        }
    }

    /**
     * Verify OTP and extend subscription
     */
    public boolean verifyAndExtendSubscription(Long subscriptionId, String providedOtp, int extensionMonths) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            SubscriptionEntity subscription = session.get(SubscriptionEntity.class, subscriptionId);
            if (subscription != null) {
                boolean verified = subscription.verifyAndExtendSubscription(providedOtp, extensionMonths);
                if (verified) {
                    subscription.setRenewalCount((subscription.getRenewalCount() != null ? subscription.getRenewalCount() : 0) + 1);
                    subscription.setLastRenewalDate(LocalDate.now());
                    session.merge(subscription);
                    session.getTransaction().commit();
                    return true;
                }
            }
            session.getTransaction().rollback();
            return false;
        } catch (Exception e) {
            util.LoggerUtil.logError("❌ Error verifying OTP: " + e.getMessage(), null);
            return false;
        }
    }

    /**
     * Mark OTP as used
     */
    public boolean markOtpAsUsed(Long subscriptionId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            SubscriptionEntity subscription = session.get(SubscriptionEntity.class, subscriptionId);
            if (subscription != null) {
                subscription.markOtpAsUsed();
                session.merge(subscription);
                session.getTransaction().commit();
                return true;
            }
            session.getTransaction().rollback();
            return false;
        } catch (Exception e) {
            util.LoggerUtil.logError("❌ Error marking OTP as used: " + e.getMessage(), null);
            return false;
        }
    }

    /**
     * Increment OTP attempt count and return remaining attempts
     */
    public int getOtpAttemptsRemaining(Long subscriptionId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            SubscriptionEntity subscription = session.get(SubscriptionEntity.class, subscriptionId);
            if (subscription != null) {
                return subscription.getOtpAttemptsRemaining();
            }
            return 0;
        } catch (Exception e) {
            util.LoggerUtil.logError("❌ Error getting OTP attempts: " + e.getMessage(), null);
            return 0;
        }
    }

    // ========================================================================
    // SUBSCRIPTION EXTENSION
    // ========================================================================

    /**
     * Extend subscription by months
     */
    public boolean extendSubscription(Long subscriptionId, int monthsToAdd) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            SubscriptionEntity subscription = session.get(SubscriptionEntity.class, subscriptionId);
            if (subscription != null) {
                LocalDate newExpiryDate = subscription.getExpiryDate().plusMonths(monthsToAdd);
                subscription.setExpiryDate(newExpiryDate);
                subscription.setStatus("ACTIVE");
                subscription.updateStatus();
                subscription.setRenewalCount((subscription.getRenewalCount() != null ? subscription.getRenewalCount() : 0) + 1);
                subscription.setLastRenewalDate(LocalDate.now());
                session.merge(subscription);
                session.getTransaction().commit();
                return true;
            }
            session.getTransaction().rollback();
            return false;
        } catch (Exception e) {
            util.LoggerUtil.logError("❌ Error extending subscription: " + e.getMessage(), null);
            return false;
        }
    }

    /**
     * Get subscription details for display
     */
    public String getSubscriptionInfo(Long subscriptionId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            SubscriptionEntity subscription = session.get(SubscriptionEntity.class, subscriptionId);
            if (subscription != null) {
                return String.format(
                        "Version: %s | Status: %s | Expires: %s | Days Remaining: %d",
                        subscription.getInstalledVersion(),
                        subscription.getStatus(),
                        subscription.getExpiryDate(),
                        subscription.getRemainingDays()
                );
            }
            return "No subscription found";
        } catch (Exception e) {
            util.LoggerUtil.logError("❌ Error fetching subscription info: " + e.getMessage(), null);
            return "Error fetching subscription";
        }
    }
}
