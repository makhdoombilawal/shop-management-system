package dao;

import models.entity.StockAuditEntity;
import models.entity.StockAuditEntity.ChangeSource;
import org.hibernate.query.Query;
import util.HibernateUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * DAO for Stock Audit Entity
 * Provides specialized stock audit data access methods
 * 
 * @author Shop Management System
 */
public class StockAuditHibernateDAO extends GenericDAO<StockAuditEntity, Integer> {
    
    public StockAuditHibernateDAO() {
        super(StockAuditEntity.class);
    }
    
    /**
     * Find stock audits by product
     */
    public List<StockAuditEntity> findByProduct(Integer productId) {
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM StockAuditEntity sa WHERE sa.product.productId = :productId ORDER BY sa.changeDate DESC";
            Query<StockAuditEntity> query = session.createQuery(hql, StockAuditEntity.class);
            query.setParameter("productId", productId);
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Error finding stock audits by product: " + e.getMessage(), e);
        }
    }
    
    /**
     * Find stock audits by product name
     */
    public List<StockAuditEntity> findByProductName(String productName) {
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM StockAuditEntity sa WHERE LOWER(sa.product.name) LIKE LOWER(:name) ORDER BY sa.changeDate DESC";
            Query<StockAuditEntity> query = session.createQuery(hql, StockAuditEntity.class);
            query.setParameter("name", "%" + productName + "%");
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Error finding stock audits by product name: " + e.getMessage(), e);
        }
    }
    
    /**
     * Find stock audits by date range
     */
    public List<StockAuditEntity> findByDateRange(LocalDate startDate, LocalDate endDate) {
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
            
            String hql = "FROM StockAuditEntity sa WHERE sa.changeDate BETWEEN :start AND :end ORDER BY sa.changeDate DESC";
            Query<StockAuditEntity> query = session.createQuery(hql, StockAuditEntity.class);
            query.setParameter("start", startDateTime);
            query.setParameter("end", endDateTime);
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Error finding stock audits by date range: " + e.getMessage(), e);
        }
    }
    
    /**
     * Find stock audits by change source
     */
    public List<StockAuditEntity> findByChangeSource(ChangeSource source) {
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM StockAuditEntity sa WHERE sa.changeSource = :source ORDER BY sa.changeDate DESC";
            Query<StockAuditEntity> query = session.createQuery(hql, StockAuditEntity.class);
            query.setParameter("source", source);
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Error finding stock audits by source: " + e.getMessage(), e);
        }
    }
    
    /**
     * Find recent stock audits (last N records)
     */
    public List<StockAuditEntity> findRecent(int limit) {
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM StockAuditEntity sa ORDER BY sa.changeDate DESC";
            Query<StockAuditEntity> query = session.createQuery(hql, StockAuditEntity.class);
            query.setMaxResults(limit);
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Error finding recent stock audits: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get total stock changes for a product
     */
    public Long getTotalChangesForProduct(Integer productId) {
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT COUNT(sa) FROM StockAuditEntity sa WHERE sa.product.productId = :productId";
            Query<Long> query = session.createQuery(hql, Long.class);
            query.setParameter("productId", productId);
            return query.uniqueResult();
        } catch (Exception e) {
            throw new RuntimeException("Error getting total changes: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get stock increase count for a product
     */
    public Long getIncreaseCount(Integer productId) {
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT COUNT(sa) FROM StockAuditEntity sa WHERE sa.product.productId = :productId AND sa.changeAmount > 0";
            Query<Long> query = session.createQuery(hql, Long.class);
            query.setParameter("productId", productId);
            return query.uniqueResult();
        } catch (Exception e) {
            throw new RuntimeException("Error getting increase count: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get stock decrease count for a product
     */
    public Long getDecreaseCount(Integer productId) {
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT COUNT(sa) FROM StockAuditEntity sa WHERE sa.product.productId = :productId AND sa.changeAmount < 0";
            Query<Long> query = session.createQuery(hql, Long.class);
            query.setParameter("productId", productId);
            return query.uniqueResult();
        } catch (Exception e) {
            throw new RuntimeException("Error getting decrease count: " + e.getMessage(), e);
        }
    }
    
    /**
     * Find today's stock changes
     */
    public List<StockAuditEntity> findTodayChanges() {
        LocalDate today = LocalDate.now();
        return findByDateRange(today, today);
    }
    
    /**
     * Find stock changes by transaction
     */
    public List<StockAuditEntity> findByTransaction(Integer transactionId) {
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM StockAuditEntity sa WHERE sa.transaction.transactionId = :transactionId ORDER BY sa.changeDate DESC";
            Query<StockAuditEntity> query = session.createQuery(hql, StockAuditEntity.class);
            query.setParameter("transactionId", transactionId);
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Error finding stock audits by transaction: " + e.getMessage(), e);
        }
    }
}
