package dao;

import models.entity.CustomerEntity;
import org.hibernate.query.Query;
import util.HibernateUtil;

import java.util.List;

/**
 * DAO for Customer Entity
 * Provides specialized customer data access methods
 * 
 * @author Shop Management System
 */
public class CustomerHibernateDAO extends GenericDAO<CustomerEntity, Integer> {
    
    public CustomerHibernateDAO() {
        super(CustomerEntity.class);
    }
    
    /**
     * Find customer by name (partial match)
     */
    public List<CustomerEntity> findByName(String name) {
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM CustomerEntity c WHERE LOWER(c.name) LIKE LOWER(:name) AND c.status = 'active'";
            Query<CustomerEntity> query = session.createQuery(hql, CustomerEntity.class);
            query.setParameter("name", "%" + name + "%");
            return query.list();
        }
    }
    
    /**
     * Find customer by phone number
     */
    public List<CustomerEntity> findByPhoneNumber(String phone) {
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM CustomerEntity c WHERE c.phoneNumber LIKE :phone";
            Query<CustomerEntity> query = session.createQuery(hql, CustomerEntity.class);
            query.setParameter("phone", "%" + phone + "%");
            return query.list();
        }
    }
    
    /**
     * Find customer by email
     */
    public CustomerEntity findByEmail(String email) {
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM CustomerEntity c WHERE c.email = :email";
            Query<CustomerEntity> query = session.createQuery(hql, CustomerEntity.class);
            query.setParameter("email", email);
            List<CustomerEntity> results = query.list();
            return results.isEmpty() ? null : results.get(0);
        }
    }
    
    /**
     * Find active customers
     */
    public List<CustomerEntity> findActiveCustomers() {
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM CustomerEntity c WHERE c.status = 'active' ORDER BY c.name";
            Query<CustomerEntity> query = session.createQuery(hql, CustomerEntity.class);
            return query.list();
        }
    }
    
    /**
     * Find top customers by purchase amount
     */
    public List<CustomerEntity> findTopCustomers(int limit) {
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM CustomerEntity c WHERE c.status = 'active' ORDER BY c.totalPurchases DESC";
            Query<CustomerEntity> query = session.createQuery(hql, CustomerEntity.class);
            query.setMaxResults(limit);
            return query.list();
        }
    }
    
    /**
     * Get total customers count
     */
    public Long getTotalCustomersCount() {
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT COUNT(c) FROM CustomerEntity c WHERE c.status = 'active'";
            Query<Long> query = session.createQuery(hql, Long.class);
            return query.uniqueResult();
        }
    }
    
    /**
     * Update customer purchase info
     */
    public boolean updatePurchaseInfo(Integer customerId, Double amount) {
        org.hibernate.Transaction transaction = null;
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            CustomerEntity customer = session.get(CustomerEntity.class, customerId);
            if (customer != null) {
                customer.updateLastPurchase(amount);
                session.update(customer);
                transaction.commit();
                return true;
            }
            return false;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw new RuntimeException("Error updating purchase info: " + e.getMessage(), e);
        }
    }
    
    /**
     * Deactivate customer
     */
    public boolean deactivateCustomer(Integer customerId) {
        org.hibernate.Transaction transaction = null;
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            CustomerEntity customer = session.get(CustomerEntity.class, customerId);
            if (customer != null) {
                customer.setStatus("inactive");
                session.update(customer);
                transaction.commit();
                return true;
            }
            return false;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw new RuntimeException("Error deactivating customer: " + e.getMessage(), e);
        }
    }
}
