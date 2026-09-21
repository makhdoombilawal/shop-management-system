package service;

import dao.TransactionHibernateDAO;
import models.entity.TransactionEntity;
import models.entity.ProductEntity;
import models.entity.CustomerEntity;
import models.entity.SupplierEntity;
import util.ValidationUtil;
import util.LoggerUtil;

import javax.swing.JOptionPane;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service layer for Transaction business logic
 * Handles validation and business rules for transactions with comprehensive logging
 * 
 * @author Shop Management System
 */
public class TransactionService {
    
    private final TransactionHibernateDAO transactionDAO;
    
    public TransactionService() {
        this.transactionDAO = new TransactionHibernateDAO();
    }
    
    /**
     * Process a sale transaction with comprehensive validation and logging
     */
    public TransactionEntity processSale(Integer productId, Integer customerId, 
                                         Integer quantity, String paymentType, String remarks) {
        if (!ValidationUtil.isPositiveInteger(quantity)) {
            String error = "Invalid quantity: " + quantity;
            LoggerUtil.logWarning(TransactionService.class, error);
            showError("Invalid quantity!");
            return null;
        }
        
        org.hibernate.Transaction tx = null;
        try (org.hibernate.Session session = util.HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            ProductEntity product = session.get(ProductEntity.class, productId);
            if (product == null) {
                LoggerUtil.logWarning(TransactionService.class, "Product not found: " + productId);
                showError("Product not found!");
                return null;
            }

            if (product.getStock() < quantity) {
                String msg = String.format("Insufficient stock for product %s. Requested: %d, Available: %d",
                    product.getName(), quantity, product.getStock());
                LoggerUtil.logWarning(TransactionService.class, msg);
                showError("Insufficient stock! Available: " + product.getStock() + " units");
                return null;
            }

            if ("discontinued".equalsIgnoreCase(product.getStatus())) {
                LoggerUtil.logWarning(TransactionService.class, 
                    "Attempted sale of discontinued product: " + product.getName());
                showError("This product has been discontinued!");
                return null;
            }

            CustomerEntity customer = null;
            if (customerId != null && customerId > 0) {
                customer = session.get(CustomerEntity.class, customerId);
            }

            int newStock = product.getStock() - quantity;
            product.setStock(newStock);

            TransactionEntity transaction = new TransactionEntity();
            transaction.setTransactionType("SALE");
            transaction.setProduct(product);
            transaction.setCustomer(customer);
            transaction.setQuantity(quantity);
            transaction.setSellPrice(product.getSellPrice());
            transaction.setPurchasePrice(product.getPurchasePrice());
            transaction.setTotalAmount(quantity * product.getSellPrice());
            transaction.setPaymentType(paymentType);
            transaction.setRemarks(remarks);
            transaction.setStockAfterTransaction(newStock);

            session.save(transaction);
            session.update(product);

            if (customer != null) {
                customer.updateLastPurchase(transaction.getTotalAmount());
                session.update(customer);
            }

            tx.commit();

            if (customer != null) {
                LoggerUtil.logInfo(TransactionService.class, 
                    String.format("Sale completed: Product=%s, Customer=%s, Qty=%d, Amount=$%.2f, Payment=%s",
                        product.getName(), customer.getName(), quantity, 
                        transaction.getTotalAmount(), paymentType));
            } else {
                LoggerUtil.logInfo(TransactionService.class, 
                    String.format("Sale completed (Walk-in): Product=%s, Qty=%d, Amount=$%.2f, Payment=%s",
                        product.getName(), quantity, transaction.getTotalAmount(), paymentType));
            }

            if (newStock <= 10) {
                LoggerUtil.logWarning(TransactionService.class, 
                    String.format("Low stock alert: %s - Only %d units remaining", 
                        product.getName(), newStock));
            }

            return transaction;
        } catch (Exception e) {
            if (tx != null && tx.getStatus().canRollback()) {
                try { tx.rollback(); } catch (Exception rbEx) { /* ignore */ }
            }
            LoggerUtil.logError(TransactionService.class, "Error processing sale", e);
            showError("Error processing sale: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Process a purchase transaction (WITHOUT supplier tracking - legacy)
     */
    public TransactionEntity processPurchase(Integer productId, Integer quantity, 
                                             Double purchasePrice, String remarks) {
        return processPurchase(productId, null, quantity, purchasePrice, remarks);
    }
    
    /**
     * Process a purchase transaction WITH supplier tracking
     * This is the preferred method for all new purchases
     */
    public TransactionEntity processPurchase(Integer productId, Integer supplierId,
                                             Integer quantity, Double purchasePrice, String remarks) {
        if (!ValidationUtil.isPositiveInteger(quantity)) {
            LoggerUtil.logWarning(TransactionService.class, "Invalid quantity for purchase: " + quantity);
            showError("Invalid quantity!");
            return null;
        }
        
        if (!ValidationUtil.isPositiveNumber(purchasePrice)) {
            LoggerUtil.logWarning(TransactionService.class, "Invalid purchase price: " + purchasePrice);
            showError("Invalid purchase price!");
            return null;
        }
        
        org.hibernate.Transaction tx = null;
        try (org.hibernate.Session session = util.HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            ProductEntity product = session.get(ProductEntity.class, productId);
            if (product == null) {
                LoggerUtil.logWarning(TransactionService.class, "Product not found for purchase: " + productId);
                showError("Product not found!");
                return null;
            }
            
            int oldStock = product.getStock();

            SupplierEntity supplier = null;
            if (supplierId != null && supplierId > 0) {
                supplier = session.get(SupplierEntity.class, supplierId);
                if (supplier == null) {
                    LoggerUtil.logWarning(TransactionService.class, "Supplier not found: " + supplierId);
                    showError("Supplier not found!");
                    return null;
                }
                if (!supplier.isActive()) {
                    showError("Supplier '" + supplier.getCompanyName() + "' is inactive!");
                    return null;
                }
            }

            TransactionEntity transaction = new TransactionEntity();
            transaction.setTransactionType("PURCHASE");
            transaction.setProduct(product);
            transaction.setSupplier(supplier);
            transaction.setQuantity(quantity);
            transaction.setPurchasePrice(purchasePrice);
            transaction.setTotalAmount(quantity * purchasePrice);
            transaction.setRemarks(remarks);

            int newStock = product.getStock() + quantity;
            product.setStock(newStock);
            transaction.setStockAfterTransaction(newStock);
            product.setPurchasePrice(purchasePrice);

            session.save(transaction);
            session.update(product);

            if (supplier != null) {
                supplier.updateLastPurchase();
                session.update(supplier);
            }

            tx.commit();

            String supplierInfo = supplier != null ? 
                ", Supplier=" + supplier.getCompanyName() : "";

            LoggerUtil.logInfo(TransactionService.class, 
                String.format("Purchase completed: Product=%s%s, Qty=%d, Price=$%.2f, TotalCost=$%.2f, Stock: %d -> %d",
                    product.getName(), supplierInfo, quantity, purchasePrice, 
                    transaction.getTotalAmount(), oldStock, newStock));

            return transaction;
        } catch (Exception e) {
            if (tx != null && tx.getStatus().canRollback()) {
                try { tx.rollback(); } catch (Exception rbEx) { /* ignore */ }
            }
            LoggerUtil.logError(TransactionService.class, "Error processing purchase", e);
            showError("Error processing purchase: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Get all transactions
     */
    public List<TransactionEntity> getAllTransactions() {
        try {
            return transactionDAO.findAll();
        } catch (Exception e) {
            showError("Error loading transactions: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Get transactions by type
     */
    public List<TransactionEntity> getTransactionsByType(String type) {
        try {
            return transactionDAO.findByType(type);
        } catch (Exception e) {
            showError("Error loading transactions: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Get transactions by date range
     */
    public List<TransactionEntity> getTransactionsByDateRange(LocalDateTime start, LocalDateTime end) {
        try {
            return transactionDAO.findByDateRange(start, end);
        } catch (Exception e) {
            showError("Error loading transactions: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Get today's sales
     */
    public List<TransactionEntity> getTodaySales() {
        try {
            return transactionDAO.getTodaySales();
        } catch (Exception e) {
            showError("Error loading today's sales: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Get today's transactions (all types)
     */
    public List<TransactionEntity> getTodayTransactions() {
        try {
            LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
            LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
            return transactionDAO.findByDateRange(startOfDay, endOfDay);
        } catch (Exception e) {
            showError("Error loading today's transactions: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Get today's total sales amount
     */
    public Double getTodayTotalSales() {
        try {
            return transactionDAO.getTodayTotalSales();
        } catch (Exception e) {
            showError("Error calculating sales: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Get today's total purchases amount
     */
    public Double getTodayTotalPurchases() {
        try {
            return transactionDAO.getTodayTotalPurchases();
        } catch (Exception e) {
            showError("Error calculating purchases: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Get total transactions count
     */
    public Long getTotalTransactionsCount() {
        try {
            return transactionDAO.count();
        } catch (Exception e) {
            showError("Error counting transactions: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Get total sales for date range
     */
    public Double getTotalSalesByDateRange(LocalDateTime start, LocalDateTime end) {
        try {
            return transactionDAO.getTotalSalesByDateRange(start, end);
        } catch (Exception e) {
            showError("Error calculating sales: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Get total profit for date range
     */
    public Double getTotalProfitByDateRange(LocalDateTime start, LocalDateTime end) {
        try {
            return transactionDAO.getTotalProfitByDateRange(start, end);
        } catch (Exception e) {
            showError("Error calculating profit: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Get top selling products
     */
    public List<Object[]> getTopSellingProducts(int limit) {
        try {
            return transactionDAO.getTopSellingProducts(limit);
        } catch (Exception e) {
            showError("Error loading top products: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Get sales by payment type
     */
    public List<Object[]> getSalesByPaymentType(LocalDateTime start, LocalDateTime end) {
        try {
            return transactionDAO.getSalesByPaymentType(start, end);
        } catch (Exception e) {
            showError("Error loading sales by payment type: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Get recent transactions
     */
    public List<TransactionEntity> getRecentTransactions(int limit) {
        try {
            return transactionDAO.getRecentTransactions(limit);
        } catch (Exception e) {
            showError("Error loading recent transactions: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Get customer transactions
     */
    public List<TransactionEntity> getCustomerTransactions(Integer customerId) {
        try {
            return transactionDAO.findByCustomer(customerId);
        } catch (Exception e) {
            showError("Error loading customer transactions: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Get product transactions
     */
    public List<TransactionEntity> getProductTransactions(Integer productId) {
        try {
            return transactionDAO.findByProduct(productId);
        } catch (Exception e) {
            showError("Error loading product transactions: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Delete transaction by ID
     * @param transactionId Transaction ID to delete
     * @return true if successful
     */
    public boolean deleteTransaction(Integer transactionId) {
        try {
            if (transactionId == null || transactionId <= 0) {
                showError("Invalid transaction ID!");
                return false;
            }
            
            boolean deleted = transactionDAO.deleteTransactionById(transactionId);
            if (!deleted) {
                showError("Transaction not found or could not be deleted!");
            }
            return deleted;
        } catch (Exception e) {
            LoggerUtil.logError(TransactionService.class, "Error deleting transaction", e);
            showError("Error deleting transaction: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get transaction by ID
     * @param transactionId Transaction ID
     * @return Optional containing transaction if found
     */
    public Optional<TransactionEntity> getTransactionById(Integer transactionId) {
        try {
            return transactionDAO.findById(transactionId);
        } catch (Exception e) {
            LoggerUtil.logError(TransactionService.class, "Error loading transaction by ID: " + transactionId, e);
            return Optional.empty();
        }
    }
    
    /**
     * Get today's transaction count
     */
    public Long getTodayTransactionCount() {
        try {
            List<TransactionEntity> todayTransactions = getTodayTransactions();
            return (long) todayTransactions.size();
        } catch (Exception e) {
            showError("Error counting today's transactions: " + e.getMessage());
            return 0L;
        }
    }
    
    /**
     * Get total revenue from all sales
     */
    public Double getTotalRevenue() {
        try {
            return transactionDAO.getTotalRevenue();
        } catch (Exception e) {
            showError("Error calculating total revenue: " + e.getMessage());
            return 0.0;
        }
    }
    
    /**
     * Show error message
     */
    private void showError(String message) {
        JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
