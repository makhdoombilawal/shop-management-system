package dao;

import models.entity.TransactionEntity;
import models.entity.ProductEntity;
import org.hibernate.query.Query;
import util.HibernateUtil;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DAO for Transaction Entity
 * Provides specialized transaction data access methods
 * 
 * @author Shop Management System
 */
public class TransactionHibernateDAO extends GenericDAO<TransactionEntity, Integer> {
    
    public TransactionHibernateDAO() {
        super(TransactionEntity.class);
    }
    
    /**
     * Find transactions by type
     */
    public List<TransactionEntity> findByType(String transactionType) {
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM TransactionEntity t " +
                        "LEFT JOIN FETCH t.customer " +
                        "JOIN FETCH t.product " +
                        "WHERE t.transactionType = :type ORDER BY t.transactionDate DESC";
            Query<TransactionEntity> query = session.createQuery(hql, TransactionEntity.class);
            query.setParameter("type", transactionType);
            return query.list();
        }
    }
    
    /**
     * Find transactions by date range
     */
    public List<TransactionEntity> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM TransactionEntity t " +
                        "LEFT JOIN FETCH t.customer " +
                        "JOIN FETCH t.product " +
                        "WHERE t.transactionDate BETWEEN :start AND :end ORDER BY t.transactionDate DESC";
            Query<TransactionEntity> query = session.createQuery(hql, TransactionEntity.class);
            query.setParameter("start", startDate);
            query.setParameter("end", endDate);
            return query.list();
        }
    }
    
    /**
     * Find transactions by customer
     */
    public List<TransactionEntity> findByCustomer(Integer customerId) {
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM TransactionEntity t " +
                        "LEFT JOIN FETCH t.customer " +
                        "JOIN FETCH t.product " +
                        "WHERE t.customer.customerId = :customerId ORDER BY t.transactionDate DESC";
            Query<TransactionEntity> query = session.createQuery(hql, TransactionEntity.class);
            query.setParameter("customerId", customerId);
            return query.list();
        }
    }
    
    /**
     * Find transactions by product
     */
    public List<TransactionEntity> findByProduct(Integer productId) {
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM TransactionEntity t " +
                        "LEFT JOIN FETCH t.customer " +
                        "JOIN FETCH t.product " +
                        "WHERE t.product.productId = :productId ORDER BY t.transactionDate DESC";
            Query<TransactionEntity> query = session.createQuery(hql, TransactionEntity.class);
            query.setParameter("productId", productId);
            return query.list();
        }
    }
    
    /**
     * Get today's sales
     */
    public List<TransactionEntity> getTodaySales() {
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
            LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
            
            String hql = "FROM TransactionEntity t " +
                        "LEFT JOIN FETCH t.customer " +
                        "JOIN FETCH t.product " +
                        "WHERE t.transactionType = 'SALE' " +
                        "AND t.transactionDate BETWEEN :start AND :end ORDER BY t.transactionDate DESC";
            Query<TransactionEntity> query = session.createQuery(hql, TransactionEntity.class);
            query.setParameter("start", startOfDay);
            query.setParameter("end", endOfDay);
            return query.list();
        }
    }
    
    /**
     * Get today's total sales amount
     */
    @SuppressWarnings("unchecked")
    public Double getTodayTotalSales() {
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            String sql = "SELECT COALESCE(SUM(t.total_amount), 0.0) as total FROM transactions t " +
                        "WHERE t.transaction_type = 'SALE' " +
                        "AND t.transaction_date >= CURDATE() " +
                        "AND t.transaction_date < DATE_ADD(CURDATE(), INTERVAL 1 DAY)";
            org.hibernate.query.NativeQuery<Double> query = session.createNativeQuery(sql);
            query.addScalar("total", org.hibernate.type.DoubleType.INSTANCE);
            Double result = query.uniqueResult();
            return result != null ? result : 0.0;
        }
    }
    
    /**
     * Get total sales for date range
     */
    public Double getTotalSalesByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT SUM(t.totalAmount) FROM TransactionEntity t WHERE t.transactionType = 'SALE' " +
                        "AND t.transactionDate BETWEEN :start AND :end";
            Query<Double> query = session.createQuery(hql, Double.class);
            query.setParameter("start", startDate);
            query.setParameter("end", endDate);
            Double result = query.uniqueResult();
            return result != null ? result : 0.0;
        }
    }
    
    /**
     * Get total profit for date range
     */
    public Double getTotalProfitByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT SUM((t.sellPrice - t.purchasePrice) * t.quantity) FROM TransactionEntity t " +
                        "WHERE t.transactionType = 'SALE' AND t.transactionDate BETWEEN :start AND :end";
            Query<Double> query = session.createQuery(hql, Double.class);
            query.setParameter("start", startDate);
            query.setParameter("end", endDate);
            Double result = query.uniqueResult();
            return result != null ? result : 0.0;
        }
    }
    
    /**
     * Get top selling products
     */
    public List<Object[]> getTopSellingProducts(int limit) {
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Return 4 columns: name, quantity, revenue, profit
            // Profit = totalAmount - (quantity * purchasePrice)
            String hql = "SELECT t.product.name, " +
                        "SUM(t.quantity), " +
                        "SUM(t.totalAmount), " +
                        "SUM(t.totalAmount - (t.quantity * COALESCE(t.purchasePrice, 0))) " +
                        "FROM TransactionEntity t WHERE t.transactionType = 'SALE' " +
                        "GROUP BY t.product.name ORDER BY SUM(t.quantity) DESC";
            Query<Object[]> query = session.createQuery(hql, Object[].class);
            query.setMaxResults(limit);
            return query.list();
        }
    }
    
    /**
     * Get sales by payment type
     */
    public List<Object[]> getSalesByPaymentType(LocalDateTime startDate, LocalDateTime endDate) {
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT t.paymentType, COUNT(t), SUM(t.totalAmount) " +
                        "FROM TransactionEntity t WHERE t.transactionType = 'SALE' " +
                        "AND t.transactionDate BETWEEN :start AND :end " +
                        "GROUP BY t.paymentType ORDER BY t.paymentType";
            Query<Object[]> query = session.createQuery(hql, Object[].class);
            query.setParameter("start", startDate);
            query.setParameter("end", endDate);
            return query.list();
        }
    }
    
    /**
     * Get recent transactions
     */
    public List<TransactionEntity> getRecentTransactions(int limit) {
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM TransactionEntity t " +
                        "LEFT JOIN FETCH t.customer " +
                        "JOIN FETCH t.product " +
                        "ORDER BY t.transactionDate DESC";
            Query<TransactionEntity> query = session.createQuery(hql, TransactionEntity.class);
            query.setMaxResults(limit);
            return query.list();
        }
    }
    
    /**
     * Get today's total purchases amount
     */
    @SuppressWarnings("unchecked")
    public Double getTodayTotalPurchases() {
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            String sql = "SELECT COALESCE(SUM(t.total_amount), 0.0) as total FROM transactions t " +
                        "WHERE t.transaction_type = 'PURCHASE' " +
                        "AND t.transaction_date >= CURDATE() " +
                        "AND t.transaction_date < DATE_ADD(CURDATE(), INTERVAL 1 DAY)";
            org.hibernate.query.NativeQuery<Double> query = session.createNativeQuery(sql);
            query.addScalar("total", org.hibernate.type.DoubleType.INSTANCE);
            Double result = query.uniqueResult();
            return result != null ? result : 0.0;
        }
    }
    
    /**
     * Delete transaction by ID and restore product stock
     * @param transactionId Transaction ID to delete
     * @return true if successful
     */
    public boolean deleteTransactionById(Integer transactionId) {
        org.hibernate.Session session = null;
        org.hibernate.Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            
            // Get transaction with product
            TransactionEntity transaction = session.createQuery(
                "FROM TransactionEntity t JOIN FETCH t.product WHERE t.transactionId = :id",
                TransactionEntity.class)
                .setParameter("id", transactionId)
                .uniqueResult();
                
            if (transaction != null) {
                // Restore product stock if it was a sale
                ProductEntity product = transaction.getProduct();
                if ("Sale".equalsIgnoreCase(transaction.getTransactionType())) {
                    product.setStock(product.getStock() + transaction.getQuantity());
                } else if ("Purchase".equalsIgnoreCase(transaction.getTransactionType())) {
                    product.setStock(product.getStock() - transaction.getQuantity());
                }
                session.update(product);
                
                // Delete transaction
                session.delete(transaction);
                tx.commit();
                return true;
            }
            return false;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            return false;
        } finally {
            if (session != null) session.close();
        }
    }
    
    /**
     * Get total revenue from all sales
     */
    public Double getTotalRevenue() {
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT COALESCE(SUM(t.totalAmount), 0.0) " +
                        "FROM TransactionEntity t " +
                        "WHERE t.transactionType = 'SALE'";
            Query<Double> query = session.createQuery(hql, Double.class);
            return query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return 0.0;
        }
    }
}
