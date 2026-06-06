package dao;

import models.entity.BarcodeEntity;
import models.entity.ProductEntity;
import org.hibernate.query.Query;
import util.HibernateUtil;

import java.util.List;

/**
 * DAO for Barcode Entity
 * Provides specialized barcode data access methods
 * 
 * @author Shop Management System
 */
public class BarcodeHibernateDAO extends GenericDAO<BarcodeEntity, Integer> {
    
    public BarcodeHibernateDAO() {
        super(BarcodeEntity.class);
    }
    
    /**
     * Override findAll to eagerly fetch product (fixes lazy loading issue)
     */
    @Override
    public List<BarcodeEntity> findAll() {
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM BarcodeEntity b LEFT JOIN FETCH b.product ORDER BY b.createdAt DESC";
            Query<BarcodeEntity> query = session.createQuery(hql, BarcodeEntity.class);
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Error finding all barcodes: " + e.getMessage(), e);
        }
    }
    
    /**
     * Find barcode by barcode number
     */
    public BarcodeEntity findByBarcodeNumber(String barcodeNumber) {
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM BarcodeEntity b LEFT JOIN FETCH b.product WHERE b.barcodeNumber = :number";
            Query<BarcodeEntity> query = session.createQuery(hql, BarcodeEntity.class);
            query.setParameter("number", barcodeNumber);
            List<BarcodeEntity> results = query.list();
            return results.isEmpty() ? null : results.get(0);
        }
    }
    
    /**
     * Find barcodes by product
     */
    public List<BarcodeEntity> findByProduct(Integer productId) {
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM BarcodeEntity b LEFT JOIN FETCH b.product WHERE b.product.productId = :productId ORDER BY b.createdAt DESC";
            Query<BarcodeEntity> query = session.createQuery(hql, BarcodeEntity.class);
            query.setParameter("productId", productId);
            return query.list();
        }
    }
    
    /**
     * Find available barcodes by product
     */
    public List<BarcodeEntity> findAvailableByProduct(Integer productId) {
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM BarcodeEntity b LEFT JOIN FETCH b.product WHERE b.product.productId = :productId " +
                        "AND b.status = 'available' ORDER BY b.createdAt DESC";
            Query<BarcodeEntity> query = session.createQuery(hql, BarcodeEntity.class);
            query.setParameter("productId", productId);
            return query.list();
        }
    }
    
    /**
     * Find barcodes by status
     */
    public List<BarcodeEntity> findByStatus(String status) {
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM BarcodeEntity b LEFT JOIN FETCH b.product WHERE b.status = :status ORDER BY b.createdAt DESC";
            Query<BarcodeEntity> query = session.createQuery(hql, BarcodeEntity.class);
            query.setParameter("status", status);
            return query.list();
        }
    }
    
    /**
     * Check if barcode exists
     */
    public boolean barcodeExists(String barcodeNumber) {
        return findByBarcodeNumber(barcodeNumber) != null;
    }
    
    /**
     * Get product by barcode number
     */
    public ProductEntity getProductByBarcode(String barcodeNumber) {
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT b.product FROM BarcodeEntity b WHERE b.barcodeNumber = :number";
            Query<ProductEntity> query = session.createQuery(hql, ProductEntity.class);
            query.setParameter("number", barcodeNumber);
            List<ProductEntity> results = query.list();
            return results.isEmpty() ? null : results.get(0);
        }
    }
    
    /**
     * Add new barcode for product
     */
    public boolean addBarcodeForProduct(Integer productId, String barcodeNumber) {
        org.hibernate.Transaction transaction = null;
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            
            // Check if barcode already exists
            if (barcodeExists(barcodeNumber)) {
                util.LoggerUtil.logInfo("⚠️ Barcode already exists: " + barcodeNumber);
                return false;
            }
            
            // Get product
            ProductEntity product = session.get(ProductEntity.class, productId);
            if (product == null) {
                util.LoggerUtil.logInfo("⚠️ Product not found: " + productId);
                return false;
            }
            
            // Create and save barcode
            BarcodeEntity barcode = new BarcodeEntity(product, barcodeNumber);
            session.save(barcode);
            
            transaction.commit();
            util.LoggerUtil.logInfo("✅ Barcode added successfully: " + barcodeNumber);
            return true;
            
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw new RuntimeException("Error adding barcode: " + e.getMessage(), e);
        }
    }
    
    /**
     * Mark barcode as sold
     */
    public boolean markAsSold(String barcodeNumber) {
        org.hibernate.Transaction transaction = null;
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            BarcodeEntity barcode = findByBarcodeNumber(barcodeNumber);
            if (barcode != null) {
                barcode.markAsSold();
                session.update(barcode);
                transaction.commit();
                return true;
            }
            return false;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw new RuntimeException("Error marking barcode as sold: " + e.getMessage(), e);
        }
    }
    
    /**
     * Mark barcode as damaged
     */
    public boolean markAsDamaged(String barcodeNumber, String reason) {
        org.hibernate.Transaction transaction = null;
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            BarcodeEntity barcode = findByBarcodeNumber(barcodeNumber);
            if (barcode != null) {
                barcode.markAsDamaged(reason);
                session.update(barcode);
                transaction.commit();
                return true;
            }
            return false;
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw new RuntimeException("Error marking barcode as damaged: " + e.getMessage(), e);
        }
    }
    
    /**
     * Count barcodes by product and status
     */
    public Long countByProductAndStatus(Integer productId, String status) {
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT COUNT(b) FROM BarcodeEntity b WHERE b.product.productId = :productId AND b.status = :status";
            Query<Long> query = session.createQuery(hql, Long.class);
            query.setParameter("productId", productId);
            query.setParameter("status", status);
            return query.uniqueResult();
        }
    }
    
    // ============================================================================
    // ENTERPRISE BARCODE SYSTEM METHODS
    // ============================================================================
    
    /**
     * Get the ONLY active barcode for a product (ENTERPRISE REQUIREMENT #1)
     * Returns null if no active barcode exists
     */
    public BarcodeEntity findActiveBarcode(Integer productId) {
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM BarcodeEntity b LEFT JOIN FETCH b.product " +
                        "WHERE b.product.productId = :productId AND b.isActive = true";
            Query<BarcodeEntity> query = session.createQuery(hql, BarcodeEntity.class);
            query.setParameter("productId", productId);
            List<BarcodeEntity> results = query.list();
            return results.isEmpty() ? null : results.get(0);
        }
    }
    
    /**
     * Set barcode as active for its product - deactivates all others
     * ENFORCES: Only ONE active barcode per product
     */
    public boolean setBarcodeAsActive(Integer barcodeId, Integer productId) {
        org.hibernate.Transaction transaction = null;
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            
            // Step 1: Deactivate all other barcodes for this product
            String hql = "UPDATE BarcodeEntity b SET b.isActive = false " +
                        "WHERE b.product.productId = :productId AND b.barcodeId != :barcodeId";
            Query<?> deactivateQuery = session.createQuery(hql);
            deactivateQuery.setParameter("productId", productId);
            deactivateQuery.setParameter("barcodeId", barcodeId);
            deactivateQuery.executeUpdate();
            
            // Step 2: Activate the specified barcode
            BarcodeEntity barcode = session.get(BarcodeEntity.class, barcodeId);
            if (barcode != null) {
                barcode.setIsActive(true);
                session.update(barcode);
                transaction.commit();
                util.LoggerUtil.logInfo("✅ Barcode " + barcode.getBarcodeNumber() + " set as ACTIVE for product " + productId);
                return true;
            }
            
            if (transaction != null) transaction.rollback();
            return false;
            
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw new RuntimeException("Error setting barcode as active: " + e.getMessage(), e);
        }
    }
    
    /**
     * OPTIMIZED SCAN: Get active barcode with product and stock info
     * ENTERPRISE REQUIREMENT #8: Optimize query for fast scanning
     */
    public BarcodeEntity scanBarcodeOptimized(String barcodeNumber) {
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Optimized query with eager loading of product and status in single query
            String hql = "FROM BarcodeEntity b LEFT JOIN FETCH b.product " +
                        "WHERE b.barcodeNumber = :number AND b.isActive = true";
            Query<BarcodeEntity> query = session.createQuery(hql, BarcodeEntity.class);
            query.setParameter("number", barcodeNumber);
            query.setReadOnly(true); // READ ONLY for performance
            query.setCacheable(true); // Cache this query
            
            List<BarcodeEntity> results = query.list();
            if (results.isEmpty()) {
                return null;
            }
            
            BarcodeEntity barcode = results.get(0);
            // Force evaluation while session open to prevent lazy loading issues
            if (barcode.getProduct() != null) {
                barcode.getProduct().getStock(); // Eagerly fetch stock
            }
            return barcode;
        }
    }
    
    /**
     * Check if product has stock (for scan validation)
     * ENTERPRISE REQUIREMENT #7: Prevent sale if stock = 0
     */
    public boolean hasStock(Integer productId) {
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT p.stock FROM ProductEntity p WHERE p.productId = :productId";
            Query<Integer> query = session.createQuery(hql, Integer.class);
            query.setParameter("productId", productId);
            Integer stock = query.uniqueResult();
            return stock != null && stock > 0;
        }
    }
    
    /**
     * Get product with active barcode - used for sales transactions
     * ENTERPRISE REQUIREMENT #5: Barcode scan returns exactly ONE product
     */
    public ProductEntity getProductByActiveBarcodeNumber(String barcodeNumber) {
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT b.product FROM BarcodeEntity b " +
                        "WHERE b.barcodeNumber = :number AND b.isActive = true";
            Query<ProductEntity> query = session.createQuery(hql, ProductEntity.class);
            query.setParameter("number", barcodeNumber);
            query.setReadOnly(true);
            query.setCacheable(true);
            
            List<ProductEntity> results = query.list();
            return results.isEmpty() ? null : results.get(0);
        }
    }
    
    /**
     * Validate barcode for scanning:
     * - Must exist
     * - Must be active
     * - Product must have stock > 0
     */
    public boolean isValidForScan(String barcodeNumber) {
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT COUNT(b) FROM BarcodeEntity b " +
                        "WHERE b.barcodeNumber = :number " +
                        "AND b.isActive = true " +
                        "AND b.product.stock > 0 " +
                        "AND b.status = 'available'";
            Query<Long> query = session.createQuery(hql, Long.class);
            query.setParameter("number", barcodeNumber);
            Long count = query.uniqueResult();
            return count != null && count > 0;
        }
    }
}
