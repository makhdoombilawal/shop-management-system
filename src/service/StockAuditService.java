package service;

import dao.StockAuditHibernateDAO;
import dao.ProductHibernateDAO;
import dao.TransactionHibernateDAO;
import dao.UserHibernateDAO;
import models.entity.StockAuditEntity;
import models.entity.StockAuditEntity.ChangeSource;
import models.entity.ProductEntity;
import models.entity.TransactionEntity;
import models.entity.UserEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service layer for Stock Audit management
 * Provides business logic for stock tracking and auditing
 * 
 * @author Shop Management System
 */
public class StockAuditService {
    
    private final StockAuditHibernateDAO stockAuditDAO;
    private final ProductHibernateDAO productDAO;
    private final TransactionHibernateDAO transactionDAO;
    private final UserHibernateDAO userDAO;
    
    public StockAuditService() {
        this.stockAuditDAO = new StockAuditHibernateDAO();
        this.productDAO = new ProductHibernateDAO();
        this.transactionDAO = new TransactionHibernateDAO();
        this.userDAO = new UserHibernateDAO();
    }
    
    /**
     * Create a stock audit record
     * This method should be called whenever stock changes
     */
    public void createStockAudit(Integer productId, Integer stockBefore, Integer stockAfter, 
                                 ChangeSource source, Integer transactionId, Integer userId, String remarks) {
        try {
            // Validate product exists
            Optional<ProductEntity> product = productDAO.findById(productId);
            if (!product.isPresent()) {
                throw new IllegalArgumentException("Product not found");
            }
            
            // Calculate change amount
            int changeAmount = stockAfter - stockBefore;
            
            // Create audit entity
            StockAuditEntity audit = new StockAuditEntity();
            audit.setProduct(product.get());
            audit.setStockBefore(stockBefore);
            audit.setStockAfter(stockAfter);
            audit.setChangeAmount(changeAmount);
            audit.setChangeSource(source);
            audit.setChangeDate(LocalDateTime.now());
            audit.setRemarks(remarks);
            
            // Set transaction if provided
            if (transactionId != null) {
                Optional<TransactionEntity> transaction = transactionDAO.findById(transactionId);
                transaction.ifPresent(audit::setTransaction);
            }
            
            // Set user if provided
            if (userId != null) {
                Optional<UserEntity> user = userDAO.findById(userId);
                user.ifPresent(audit::setChangedBy);
            }
            
            stockAuditDAO.save(audit);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create stock audit: " + e.getMessage(), e);
        }
    }
    
    /**
     * Simplified method for transaction-based stock changes
     */
    public void recordSale(Integer productId, Integer stockBefore, Integer stockAfter, 
                          Integer transactionId, Integer userId) {
        createStockAudit(productId, stockBefore, stockAfter, ChangeSource.SALE, 
                        transactionId, userId, "Stock reduced due to sale");
    }
    
    /**
     * Record purchase/restock
     */
    public void recordPurchase(Integer productId, Integer stockBefore, Integer stockAfter, 
                              Integer userId, String remarks) {
        createStockAudit(productId, stockBefore, stockAfter, ChangeSource.PURCHASE, 
                        null, userId, remarks != null ? remarks : "Stock increased from purchase");
    }
    
    /**
     * Record return (customer returns product)
     */
    public void recordReturn(Integer productId, Integer stockBefore, Integer stockAfter, 
                            Integer transactionId, Integer userId) {
        createStockAudit(productId, stockBefore, stockAfter, ChangeSource.RETURN, 
                        transactionId, userId, "Stock increased due to return");
    }
    
    /**
     * Record manual adjustment
     */
    public void recordAdjustment(Integer productId, Integer stockBefore, Integer stockAfter, 
                                Integer userId, String remarks) {
        createStockAudit(productId, stockBefore, stockAfter, ChangeSource.ADJUSTMENT, 
                        null, userId, remarks != null ? remarks : "Manual stock adjustment");
    }
    
    /**
     * Record damage/loss
     */
    public void recordDamage(Integer productId, Integer stockBefore, Integer stockAfter, 
                            Integer userId, String remarks) {
        createStockAudit(productId, stockBefore, stockAfter, ChangeSource.DAMAGE, 
                        null, userId, remarks != null ? remarks : "Stock reduced due to damage");
    }
    
    /**
     * Record system-initiated change
     */
    public void recordSystemChange(Integer productId, Integer stockBefore, Integer stockAfter, 
                                   String remarks) {
        createStockAudit(productId, stockBefore, stockAfter, ChangeSource.SYSTEM, 
                        null, null, remarks != null ? remarks : "System-initiated stock change");
    }
    
    /**
     * Get all stock audits
     */
    public List<StockAuditEntity> getAllStockAudits() {
        try {
            return stockAuditDAO.findAll();
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve stock audits: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get stock audit by ID
     */
    public Optional<StockAuditEntity> getStockAuditById(Integer auditId) {
        try {
            if (auditId == null || auditId <= 0) {
                throw new IllegalArgumentException("Invalid audit ID");
            }
            return stockAuditDAO.findById(auditId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to find stock audit: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get stock history for a product
     */
    public List<StockAuditEntity> getProductStockHistory(Integer productId) {
        try {
            if (productId == null || productId <= 0) {
                throw new IllegalArgumentException("Invalid product ID");
            }
            return stockAuditDAO.findByProduct(productId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve product stock history: " + e.getMessage(), e);
        }
    }
    
    /**
     * Search stock audits by product name
     */
    public List<StockAuditEntity> searchByProductName(String productName) {
        try {
            if (productName == null || productName.trim().isEmpty()) {
                return getAllStockAudits();
            }
            return stockAuditDAO.findByProductName(productName.trim());
        } catch (Exception e) {
            throw new RuntimeException("Failed to search stock audits: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get stock audits by date range
     */
    public List<StockAuditEntity> getAuditsByDateRange(LocalDate startDate, LocalDate endDate) {
        try {
            if (startDate == null || endDate == null) {
                throw new IllegalArgumentException("Start date and end date are required");
            }
            if (startDate.isAfter(endDate)) {
                throw new IllegalArgumentException("Start date cannot be after end date");
            }
            return stockAuditDAO.findByDateRange(startDate, endDate);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve audits by date range: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get stock audits by change source
     */
    public List<StockAuditEntity> getAuditsBySource(ChangeSource source) {
        try {
            if (source == null) {
                throw new IllegalArgumentException("Change source is required");
            }
            return stockAuditDAO.findByChangeSource(source);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve audits by source: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get recent stock audits
     */
    public List<StockAuditEntity> getRecentAudits(int limit) {
        try {
            if (limit <= 0) {
                throw new IllegalArgumentException("Limit must be positive");
            }
            return stockAuditDAO.findRecent(limit);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve recent audits: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get today's stock changes
     */
    public List<StockAuditEntity> getTodayStockChanges() {
        try {
            return stockAuditDAO.findTodayChanges();
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve today's changes: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get stock audits for a transaction
     */
    public List<StockAuditEntity> getAuditsByTransaction(Integer transactionId) {
        try {
            if (transactionId == null || transactionId <= 0) {
                throw new IllegalArgumentException("Invalid transaction ID");
            }
            return stockAuditDAO.findByTransaction(transactionId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve audits by transaction: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get statistics for a product
     */
    public ProductStockStatistics getProductStatistics(Integer productId) {
        try {
            if (productId == null || productId <= 0) {
                throw new IllegalArgumentException("Invalid product ID");
            }
            
            Long totalChanges = stockAuditDAO.getTotalChangesForProduct(productId);
            Long increases = stockAuditDAO.getIncreaseCount(productId);
            Long decreases = stockAuditDAO.getDecreaseCount(productId);
            
            return new ProductStockStatistics(totalChanges, increases, decreases);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve product statistics: " + e.getMessage(), e);
        }
    }
    
    /**
     * Inner class to hold stock statistics
     */
    public static class ProductStockStatistics {
        private final Long totalChanges;
        private final Long increases;
        private final Long decreases;
        
        public ProductStockStatistics(Long totalChanges, Long increases, Long decreases) {
            this.totalChanges = totalChanges;
            this.increases = increases;
            this.decreases = decreases;
        }
        
        public Long getTotalChanges() { return totalChanges; }
        public Long getIncreases() { return increases; }
        public Long getDecreases() { return decreases; }
    }
}
