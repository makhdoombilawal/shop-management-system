package dao;

import models.entity.SupplierEntity;
import org.hibernate.query.Query;
import util.HibernateUtil;

import java.util.List;

/**
 * DAO for Supplier Entity
 * Provides database access methods for supplier management
 * 
 * @author Shop Management System - Enterprise Edition
 */
public class SupplierHibernateDAO extends GenericDAO<SupplierEntity, Integer> {
    
    public SupplierHibernateDAO() {
        super(SupplierEntity.class);
    }
    
    /**
     * Find active suppliers
     */
    public List<SupplierEntity> findActiveSuppliers() {
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM SupplierEntity s WHERE s.status = 'active' ORDER BY s.companyName";
            Query<SupplierEntity> query = session.createQuery(hql, SupplierEntity.class);
            return query.list();
        }
    }
    
    /**
     * Find suppliers by company name (partial match)
     */
    public List<SupplierEntity> findByCompanyName(String companyName) {
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM SupplierEntity s WHERE LOWER(s.companyName) LIKE LOWER(:name) ORDER BY s.companyName";
            Query<SupplierEntity> query = session.createQuery(hql, SupplierEntity.class);
            query.setParameter("name", "%" + companyName + "%");
            return query.list();
        }
    }
    
    /**
     * Find suppliers by status
     */
    public List<SupplierEntity> findByStatus(String status) {
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM SupplierEntity s WHERE s.status = :status ORDER BY s.companyName";
            Query<SupplierEntity> query = session.createQuery(hql, SupplierEntity.class);
            query.setParameter("status", status);
            return query.list();
        }
    }
    
    /**
     * Find suppliers with balance over credit limit
     */
    public List<SupplierEntity> findOverCreditLimit() {
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM SupplierEntity s WHERE s.currentBalance > s.creditLimit AND s.creditLimit IS NOT NULL ORDER BY s.currentBalance DESC";
            Query<SupplierEntity> query = session.createQuery(hql, SupplierEntity.class);
            return query.list();
        }
    }
    
    /**
     * Get suppliers sorted by total purchase value
     */
    public List<SupplierEntity> findTopSuppliers(int limit) {
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM SupplierEntity s WHERE s.status = 'active' ORDER BY s.currentBalance DESC";
            Query<SupplierEntity> query = session.createQuery(hql, SupplierEntity.class);
            query.setMaxResults(limit);
            return query.list();
        }
    }
    
    /**
     * Check if supplier company name already exists
     */
    public boolean supplierExists(String companyName) {
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT COUNT(s) FROM SupplierEntity s WHERE LOWER(s.companyName) = LOWER(:name)";
            Query<Long> query = session.createQuery(hql, Long.class);
            query.setParameter("name", companyName);
            Long count = query.uniqueResult();
            return count != null && count > 0;
        }
    }
    
    /**
     * Update supplier's last purchase date
     */
    public boolean updateLastPurchaseDate(Integer supplierId) {
        org.hibernate.Transaction transaction = null;
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            
            SupplierEntity supplier = session.get(SupplierEntity.class, supplierId);
            if (supplier != null) {
                supplier.updateLastPurchase();
                session.update(supplier);
                transaction.commit();
                return true;
            }
            return false;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw new RuntimeException("Error updating supplier purchase date: " + e.getMessage(), e);
        }
    }
    
    /**
     * Update supplier balance
     */
    public boolean updateBalance(Integer supplierId, Double amount, boolean add) {
        org.hibernate.Transaction transaction = null;
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            
            SupplierEntity supplier = session.get(SupplierEntity.class, supplierId);
            if (supplier != null) {
                if (add) {
                    supplier.addToBalance(amount);
                } else {
                    supplier.subtractFromBalance(amount);
                }
                session.update(supplier);
                transaction.commit();
                return true;
            }
            return false;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw new RuntimeException("Error updating supplier balance: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get total number of active suppliers
     */
    public long countActiveSuppliers() {
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT COUNT(s) FROM SupplierEntity s WHERE s.status = 'active'";
            Query<Long> query = session.createQuery(hql, Long.class);
            Long count = query.uniqueResult();
            return count != null ? count : 0;
        }
    }
}
