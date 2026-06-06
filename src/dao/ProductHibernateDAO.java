package dao;

import models.entity.ProductEntity;
import org.hibernate.query.Query;
import util.HibernateUtil;

import java.util.List;

/**
 * DAO for Product Entity
 * Provides specialized product data access methods
 * 
 * @author Shop Management System
 */
public class ProductHibernateDAO extends GenericDAO<ProductEntity, Integer> {
    
    public ProductHibernateDAO() {
        super(ProductEntity.class);
    }
    
    /**
     * Find products by name (partial match)
     */
    public List<ProductEntity> findByName(String name) {
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM ProductEntity p WHERE LOWER(p.name) LIKE LOWER(:name)";
            Query<ProductEntity> query = session.createQuery(hql, ProductEntity.class);
            query.setParameter("name", "%" + name + "%");
            return query.list();
        }
    }
    
    /**
     * Find products by type
     */
    public List<ProductEntity> findByType(String productType) {
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM ProductEntity p WHERE p.productType = :type";
            Query<ProductEntity> query = session.createQuery(hql, ProductEntity.class);
            query.setParameter("type", productType);
            return query.list();
        }
    }
    
    /**
     * Find low stock products
     */
    public List<ProductEntity> findLowStock(int threshold) {
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM ProductEntity p WHERE p.stock < :threshold AND p.status = 'active' ORDER BY p.stock ASC";
            Query<ProductEntity> query = session.createQuery(hql, ProductEntity.class);
            query.setParameter("threshold", threshold);
            return query.list();
        }
    }
    
    /**
     * Find out of stock products
     */
    public List<ProductEntity> findOutOfStock() {
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM ProductEntity p WHERE p.stock = 0 AND p.status = 'active'";
            Query<ProductEntity> query = session.createQuery(hql, ProductEntity.class);
            return query.list();
        }
    }
    
    /**
     * Find active products
     */
    public List<ProductEntity> findActiveProducts() {
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM ProductEntity p WHERE p.status = 'active' ORDER BY p.name";
            Query<ProductEntity> query = session.createQuery(hql, ProductEntity.class);
            return query.list();
        }
    }
    
    /**
     * Get all product types
     */
    public List<String> getAllProductTypes() {
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT DISTINCT p.productType FROM ProductEntity p WHERE p.productType IS NOT NULL ORDER BY p.productType";
            Query<String> query = session.createQuery(hql, String.class);
            return query.list();
        }
    }
    
    /**
     * Update product stock
     */
    public boolean updateStock(Integer productId, Integer newStock) {
        org.hibernate.Transaction transaction = null;
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            ProductEntity product = session.get(ProductEntity.class, productId);
            if (product != null) {
                product.setStock(newStock);
                session.update(product);
                transaction.commit();
                return true;
            }
            return false;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw new RuntimeException("Error updating stock: " + e.getMessage(), e);
        }
    }
    
    /**
     * Increase product stock
     */
    public boolean increaseStock(Integer productId, Integer quantity) {
        org.hibernate.Transaction transaction = null;
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            ProductEntity product = session.get(ProductEntity.class, productId);
            if (product != null) {
                product.setStock(product.getStock() + quantity);
                session.update(product);
                transaction.commit();
                return true;
            }
            return false;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw new RuntimeException("Error increasing stock: " + e.getMessage(), e);
        }
    }
    
    /**
     * Decrease product stock
     */
    public boolean decreaseStock(Integer productId, Integer quantity) {
        org.hibernate.Transaction transaction = null;
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            ProductEntity product = session.get(ProductEntity.class, productId);
            if (product != null && product.getStock() >= quantity) {
                product.setStock(product.getStock() - quantity);
                session.update(product);
                transaction.commit();
                return true;
            }
            return false;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw new RuntimeException("Error decreasing stock: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get total inventory value
     */
    public Double getTotalInventoryValue() {
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT SUM(p.stock * p.purchasePrice) FROM ProductEntity p WHERE p.status = 'active'";
            Query<Double> query = session.createQuery(hql, Double.class);
            Double result = query.uniqueResult();
            return result != null ? result : 0.0;
        }
    }
    
    /**
     * Get total products count
     */
    public Long getTotalProductsCount() {
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT COUNT(p) FROM ProductEntity p WHERE p.status = 'active'";
            Query<Long> query = session.createQuery(hql, Long.class);
            return query.uniqueResult();
        }
    }
}
