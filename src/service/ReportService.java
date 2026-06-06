package service;

import dao.TransactionHibernateDAO;
import dao.ProductHibernateDAO;
import dao.CustomerHibernateDAO;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for generating business reports
 * Provides comprehensive reporting functionality for supermart management
 * 
 * @author Shop Management System
 */
public class ReportService {
    
    private final TransactionHibernateDAO transactionDAO;
    private final ProductHibernateDAO productDAO;
    private final CustomerHibernateDAO customerDAO;
    
    public ReportService() {
        this.transactionDAO = new TransactionHibernateDAO();
        this.productDAO = new ProductHibernateDAO();
        this.customerDAO = new CustomerHibernateDAO();
    }
    
    /**
     * Generate daily sales report
     */
    public Map<String, Object> generateDailySalesReport() {
        Map<String, Object> report = new HashMap<>();
        
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        
        report.put("reportDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        report.put("reportTitle", "Daily Sales Report");
        
        // Total sales
        Double totalSales = transactionDAO.getTotalSalesByDateRange(startOfDay, endOfDay);
        report.put("totalSales", totalSales);
        
        // Total profit
        Double totalProfit = transactionDAO.getTotalProfitByDateRange(startOfDay, endOfDay);
        report.put("totalProfit", totalProfit);
        
        // Transaction count
        List<?> transactions = transactionDAO.findByDateRange(startOfDay, endOfDay);
        report.put("transactionCount", transactions.size());
        
        // Sales by payment type
        List<Object[]> paymentTypeSales = transactionDAO.getSalesByPaymentType(startOfDay, endOfDay);
        report.put("salesByPaymentType", paymentTypeSales);
        
        // Top selling products
        List<Object[]> topProducts = transactionDAO.getTopSellingProducts(10);
        report.put("topSellingProducts", topProducts);
        
        return report;
    }
    
    /**
     * Generate sales report for date range
     */
    public Map<String, Object> generateSalesReport(LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> report = new HashMap<>();
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        report.put("reportTitle", "Sales Report");
        report.put("startDate", startDate.format(formatter));
        report.put("endDate", endDate.format(formatter));
        
        // Total sales
        Double totalSales = transactionDAO.getTotalSalesByDateRange(startDate, endDate);
        report.put("totalSales", totalSales);
        
        // Total profit
        Double totalProfit = transactionDAO.getTotalProfitByDateRange(startDate, endDate);
        report.put("totalProfit", totalProfit);
        
        // Profit margin
        if (totalSales > 0) {
            Double profitMargin = (totalProfit / totalSales) * 100;
            report.put("profitMargin", profitMargin);
        } else {
            report.put("profitMargin", 0.0);
        }
        
        // Transaction count
        List<?> transactions = transactionDAO.findByDateRange(startDate, endDate);
        report.put("transactionCount", transactions.size());
        
        // Average transaction value
        if (!transactions.isEmpty()) {
            Double avgTransaction = totalSales / transactions.size();
            report.put("averageTransactionValue", avgTransaction);
        } else {
            report.put("averageTransactionValue", 0.0);
        }
        
        // Sales by payment type
        List<Object[]> paymentTypeSales = transactionDAO.getSalesByPaymentType(startDate, endDate);
        report.put("salesByPaymentType", paymentTypeSales);
        
        // Top selling products
        List<Object[]> topProducts = transactionDAO.getTopSellingProducts(10);
        report.put("topSellingProducts", topProducts);
        
        return report;
    }
    
    /**
     * Generate inventory report
     */
    public Map<String, Object> generateInventoryReport() {
        Map<String, Object> report = new HashMap<>();
        
        report.put("reportDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        report.put("reportTitle", "Inventory Status Report");
        
        // Total products
        Long totalProducts = productDAO.getTotalProductsCount();
        report.put("totalProducts", totalProducts);
        
        // Total inventory value
        Double inventoryValue = productDAO.getTotalInventoryValue();
        report.put("totalInventoryValue", inventoryValue);
        
        // Low stock products (threshold: 10)
        List<?> lowStockProducts = productDAO.findLowStock(10);
        report.put("lowStockCount", lowStockProducts.size());
        report.put("lowStockProducts", lowStockProducts);
        
        // Out of stock products
        List<?> outOfStockProducts = productDAO.findOutOfStock();
        report.put("outOfStockCount", outOfStockProducts.size());
        report.put("outOfStockProducts", outOfStockProducts);
        
        // Active products
        List<?> activeProducts = productDAO.findActiveProducts();
        report.put("activeProductsCount", activeProducts.size());
        
        return report;
    }
    
    /**
     * Generate customer report
     */
    public Map<String, Object> generateCustomerReport() {
        Map<String, Object> report = new HashMap<>();
        
        report.put("reportDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        report.put("reportTitle", "Customer Analysis Report");
        
        // Total customers
        Long totalCustomers = customerDAO.getTotalCustomersCount();
        report.put("totalCustomers", totalCustomers);
        
        // Top customers
        List<?> topCustomers = customerDAO.findTopCustomers(20);
        report.put("topCustomers", topCustomers);
        
        return report;
    }
    
    /**
     * Generate profit & loss report
     */
    public Map<String, Object> generateProfitLossReport(LocalDateTime startDate, LocalDateTime endDate) {
        Map<String, Object> report = new HashMap<>();
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        report.put("reportTitle", "Profit & Loss Report");
        report.put("startDate", startDate.format(formatter));
        report.put("endDate", endDate.format(formatter));
        
        // Revenue (Sales)
        Double totalRevenue = transactionDAO.getTotalSalesByDateRange(startDate, endDate);
        report.put("totalRevenue", totalRevenue);
        
        // Cost of Goods Sold
        // Calculate from purchase transactions or from sales at purchase price
        Double cogs = totalRevenue - transactionDAO.getTotalProfitByDateRange(startDate, endDate);
        report.put("costOfGoodsSold", cogs);
        
        // Gross Profit
        Double grossProfit = transactionDAO.getTotalProfitByDateRange(startDate, endDate);
        report.put("grossProfit", grossProfit);
        
        // Gross Profit Margin
        if (totalRevenue > 0) {
            Double grossProfitMargin = (grossProfit / totalRevenue) * 100;
            report.put("grossProfitMargin", grossProfitMargin);
        } else {
            report.put("grossProfitMargin", 0.0);
        }
        
        return report;
    }
    
    /**
     * Generate dashboard summary
     */
    public Map<String, Object> generateDashboardSummary() {
        Map<String, Object> summary = new HashMap<>();
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = now.withHour(23).withMinute(59).withSecond(59);
        
        // Today's sales
        Double todaySales = transactionDAO.getTodayTotalSales();
        summary.put("todaySales", todaySales);
        
        // Today's profit
        Double todayProfit = transactionDAO.getTotalProfitByDateRange(startOfDay, endOfDay);
        summary.put("todayProfit", todayProfit);
        
        // Today's transaction count
        List<?> todayTransactions = transactionDAO.getTodaySales();
        summary.put("todayTransactionCount", todayTransactions.size());
        
        // Total inventory value
        Double inventoryValue = productDAO.getTotalInventoryValue();
        summary.put("inventoryValue", inventoryValue);
        
        // Low stock alerts
        List<?> lowStockProducts = productDAO.findLowStock(10);
        summary.put("lowStockAlerts", lowStockProducts.size());
        
        // Out of stock alerts
        List<?> outOfStockProducts = productDAO.findOutOfStock();
        summary.put("outOfStockAlerts", outOfStockProducts.size());
        
        // Total customers
        Long totalCustomers = customerDAO.getTotalCustomersCount();
        summary.put("totalCustomers", totalCustomers);
        
        // Total active products
        Long totalProducts = productDAO.getTotalProductsCount();
        summary.put("totalProducts", totalProducts);
        
        // Recent transactions
        List<?> recentTransactions = transactionDAO.getRecentTransactions(10);
        summary.put("recentTransactions", recentTransactions);
        
        return summary;
    }
    
    /**
     * Generate top products report
     */
    public Map<String, Object> generateTopProductsReport(int limit) {
        Map<String, Object> report = new HashMap<>();
        
        report.put("reportDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        report.put("reportTitle", "Top Selling Products Report");
        
        List<Object[]> topProducts = transactionDAO.getTopSellingProducts(limit);
        report.put("topProducts", topProducts);
        report.put("productCount", topProducts.size());
        
        return report;
    }
    
    /**
     * Format currency
     */
    public String formatCurrency(Double amount) {
        if (amount == null) return "$0.00";
        return String.format("$%.2f", amount);
    }
    
    /**
     * Format percentage
     */
    public String formatPercentage(Double percentage) {
        if (percentage == null) return "0.00%";
        return String.format("%.2f%%", percentage);
    }
}
