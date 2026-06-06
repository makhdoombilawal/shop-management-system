package frames;

import models.Session;
import service.ReportService;
import util.EnterpriseTheme;
import util.LoggerUtil;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Enterprise Reports Module
 * Provides comprehensive business intelligence and reporting
 */
public class ReportEnterprise extends BaseFrame {
    
    private final ReportService reportService = new ReportService();
    
    // UI Components
    private JPanel headerPanel;
    private JPanel contentPanel;
    private JPanel controlPanel;
    private JPanel resultsPanel;
    
    private JComboBox<String> cmbReportType;
    private JDateChooser dateFrom;
    private JDateChooser dateTo;
    private JButton btnGenerate;
    private JButton btnExport;
    private JButton btnRefresh;
    private JButton btnBack;
    
    private JTable reportTable;
    private DefaultTableModel tableModel;
    private JTextArea txtReportSummary;
    
    // Report types
    private static final String[] REPORT_TYPES = {
        "Daily Sales Summary",
        "Sales by Date Range",
        "Inventory Status",
        "Low Stock Alert",
        "Customer Analysis",
        "Top Selling Products",
        "Profit Analysis",
        "Stock Valuation"
    };
    
    public ReportEnterprise() throws Exception {
        super();
        if (!authorized) return;
        
        setTitle("Business Reports - Shop Manager");
        setSize(1700, 900);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        initializeComponents();
        setupLayout();
        
        EnterpriseTheme.applyGlobalTheme();
        
        LoggerUtil.logInfo(ReportEnterprise.class, "Reports module opened by: " + Session.getUsername());
    }
    
    private void initializeComponents() {
        // Header
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(EnterpriseTheme.PRIMARY);
        headerPanel.setPreferredSize(new Dimension(0, 80));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        
        JLabel lblTitle = new JLabel("BUSINESS REPORTS & ANALYTICS");
        lblTitle.setFont(EnterpriseTheme.FONT_TITLE);
        lblTitle.setForeground(EnterpriseTheme.TEXT_ON_PRIMARY);
        
        JLabel lblUser = new JLabel("User: " + Session.getUsername() + " | Role: " + Session.getRole());
        lblUser.setFont(EnterpriseTheme.FONT_BODY);
        lblUser.setForeground(EnterpriseTheme.TEXT_ON_PRIMARY);
        
        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(lblUser, BorderLayout.EAST);
        
        // Content Panel
        contentPanel = new JPanel(new BorderLayout(0, 20));
        contentPanel.setBackground(EnterpriseTheme.BACKGROUND);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        createControlPanel();
        createResultsPanel();
    }
    
    private void createControlPanel() {
        controlPanel = new JPanel(new GridBagLayout());
        controlPanel.setBackground(EnterpriseTheme.CARD_BG);
        controlPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(EnterpriseTheme.BORDER, 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);
        
        // Report Type
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel lblReportType = new JLabel("Report Type:");
        lblReportType.setFont(EnterpriseTheme.FONT_BODY_BOLD);
        controlPanel.add(lblReportType, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        cmbReportType = new JComboBox<>(REPORT_TYPES);
        EnterpriseTheme.styleComboBox(cmbReportType);
        cmbReportType.setPreferredSize(new Dimension(250, 35));

        // Add action listener to auto-generate report when report type changes
        cmbReportType.addActionListener(e -> {
            String selectedReportType = (String) cmbReportType.getSelectedItem();
            if ("Sales by Date Range".equals(selectedReportType) ||
                "Daily Sales Summary".equals(selectedReportType) ||
                "Profit Analysis".equals(selectedReportType)) {

                // Automatically generate date-sensitive reports when selected
                util.LoggerUtil.logInfo("📊 Report type changed to: " + selectedReportType + " - Auto-generating report");
                SwingUtilities.invokeLater(() -> {
                    try {
                        generateReport();
                    } catch (Exception ex) {
                        util.LoggerUtil.logError("Error auto-generating report: " + ex.getMessage(), null);
                    }
                });
            }
        });

        controlPanel.add(cmbReportType, gbc);
        
        // Date From
        gbc.gridx = 2;
        gbc.weightx = 0;
        JLabel lblFrom = new JLabel("From Date:");
        lblFrom.setFont(EnterpriseTheme.FONT_BODY);
        controlPanel.add(lblFrom, gbc);
        
        gbc.gridx = 3;
        gbc.weightx = 0.5;
        dateFrom = new JDateChooser();
        dateFrom.setDateFormatString("yyyy-MM-dd");
        dateFrom.setPreferredSize(new Dimension(150, 35));
        // Set to 30 days ago
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        dateFrom.setDate(Date.from(thirtyDaysAgo.atStartOfDay(ZoneId.systemDefault()).toInstant()));

        // Add property change listener to auto-regenerate report when date changes
        dateFrom.addPropertyChangeListener("date", e -> {
            Date newValue = (Date) e.getNewValue();
            Date oldValue = (Date) e.getOldValue();

            if (newValue != null && !newValue.equals(oldValue)) {
                // Auto-regenerate report for any date-sensitive report types
                String currentReportType = (String) cmbReportType.getSelectedItem();
                util.LoggerUtil.logInfo("🔧 PropertyChangeEvent fired:");
                util.LoggerUtil.logInfo("   - From Date changed from: " + oldValue + " to: " + newValue);
                util.LoggerUtil.logInfo("   - Current report type: " + currentReportType);

                if ("Sales by Date Range".equals(currentReportType) ||
                    "Daily Sales Summary".equals(currentReportType) ||
                    "Profit Analysis".equals(currentReportType)) {

                    util.LoggerUtil.logInfo("📅 FROM DATE CHANGED - Auto-regenerating report for: " + currentReportType);
                    SwingUtilities.invokeLater(() -> {
                        try {
                            util.LoggerUtil.logInfo("🔄 Executing generateReport() for From Date change...");
                            generateReport();
                        } catch (Exception ex) {
                            util.LoggerUtil.logError("❌ Error auto-regenerating report: " + ex.getMessage(), null);
                            ex.printStackTrace();
                        }
                    });
                } else {
                    util.LoggerUtil.logInfo("ℹ️ Date changed but report type '" + currentReportType + "' is not date-sensitive - skipping auto-generation");
                }
            }
        });

        controlPanel.add(dateFrom, gbc);
        
        // Date To
        gbc.gridx = 4;
        gbc.weightx = 0;
        JLabel lblTo = new JLabel("To Date:");
        lblTo.setFont(EnterpriseTheme.FONT_BODY);
        controlPanel.add(lblTo, gbc);
        
        gbc.gridx = 5;
        gbc.weightx = 0.5;
        dateTo = new JDateChooser();
        dateTo.setDateFormatString("yyyy-MM-dd");
        dateTo.setDate(new Date());
        dateTo.setPreferredSize(new Dimension(150, 35));

        // Add property change listener to auto-regenerate report when date changes
        dateTo.addPropertyChangeListener("date", e -> {
            Date newValue = (Date) e.getNewValue();
            Date oldValue = (Date) e.getOldValue();

            if (newValue != null && !newValue.equals(oldValue)) {
                // Auto-regenerate report for any date-sensitive report types
                String currentReportType = (String) cmbReportType.getSelectedItem();
                util.LoggerUtil.logInfo("🔧 PropertyChangeEvent fired:");
                util.LoggerUtil.logInfo("   - To Date changed from: " + oldValue + " to: " + newValue);
                util.LoggerUtil.logInfo("   - Current report type: " + currentReportType);

                if ("Sales by Date Range".equals(currentReportType) ||
                    "Daily Sales Summary".equals(currentReportType) ||
                    "Profit Analysis".equals(currentReportType)) {

                    util.LoggerUtil.logInfo("📅 TO DATE CHANGED - Auto-regenerating report for: " + currentReportType);
                    SwingUtilities.invokeLater(() -> {
                        try {
                            util.LoggerUtil.logInfo("🔄 Executing generateReport() for To Date change...");
                            generateReport();
                        } catch (Exception ex) {
                            util.LoggerUtil.logError("❌ Error auto-regenerating report: " + ex.getMessage(), null);
                            ex.printStackTrace();
                        }
                    });
                } else {
                    util.LoggerUtil.logInfo("ℹ️ Date changed but report type '" + currentReportType + "' is not date-sensitive - skipping auto-generation");
                }
            }
        });

        controlPanel.add(dateTo, gbc);
        
        // Buttons
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 6;
        gbc.weightx = 1.0;
        
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        btnPanel.setBackground(EnterpriseTheme.CARD_BG);
        
        btnGenerate = new JButton("Generate Report");
        btnRefresh = new JButton("Refresh");
        btnExport = new JButton("Export to PDF");
        btnBack = new JButton("<< Back to Dashboard");
        
        EnterpriseTheme.stylePrimaryButton(btnGenerate);
        EnterpriseTheme.styleSecondaryButton(btnRefresh);
        EnterpriseTheme.styleSuccessButton(btnExport);
        EnterpriseTheme.styleSecondaryButton(btnBack);
        
        btnGenerate.addActionListener(e -> generateReport());
        btnRefresh.addActionListener(e -> generateReport());
        btnExport.addActionListener(e -> exportToPDF());
        btnBack.addActionListener(e -> goBackToDashboard());
        
        btnPanel.add(btnGenerate);
        btnPanel.add(btnRefresh);
        btnPanel.add(btnExport);
        btnPanel.add(Box.createHorizontalStrut(50));
        btnPanel.add(btnBack);
        
        controlPanel.add(btnPanel, gbc);
    }
    
    private void createResultsPanel() {
        resultsPanel = new JPanel(new BorderLayout(0, 10));
        resultsPanel.setBackground(EnterpriseTheme.CARD_BG);
        resultsPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(EnterpriseTheme.BORDER, 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        // Summary panel
        JPanel summaryPanel = new JPanel(new BorderLayout());
        summaryPanel.setBackground(EnterpriseTheme.CARD_BG);
        
        JLabel lblSummary = new JLabel("Report Summary");
        lblSummary.setFont(EnterpriseTheme.FONT_HEADER);
        lblSummary.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        txtReportSummary = new JTextArea(4, 50);
        txtReportSummary.setFont(EnterpriseTheme.FONT_BODY);
        txtReportSummary.setEditable(false);
        txtReportSummary.setBackground(EnterpriseTheme.BACKGROUND);
        txtReportSummary.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JScrollPane summaryScroll = new JScrollPane(txtReportSummary);
        
        summaryPanel.add(lblSummary, BorderLayout.NORTH);
        summaryPanel.add(summaryScroll, BorderLayout.CENTER);
        
        // Table panel
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(EnterpriseTheme.CARD_BG);
        tablePanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        JLabel lblTable = new JLabel("Detailed Report Data");
        lblTable.setFont(EnterpriseTheme.FONT_HEADER);
        lblTable.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        String[] columns = {"Item", "Value", "Details"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        reportTable = new JTable(tableModel);
        EnterpriseTheme.styleTable(reportTable);
        JScrollPane scrollPane = new JScrollPane(reportTable);
        EnterpriseTheme.styleScrollPane(scrollPane);
        
        tablePanel.add(lblTable, BorderLayout.NORTH);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        resultsPanel.add(summaryPanel, BorderLayout.NORTH);
        resultsPanel.add(tablePanel, BorderLayout.CENTER);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        add(headerPanel, BorderLayout.NORTH);
        
        contentPanel.add(controlPanel, BorderLayout.NORTH);
        contentPanel.add(resultsPanel, BorderLayout.CENTER);
        
        add(contentPanel, BorderLayout.CENTER);
    }
    
    private void generateReport() {
        if (cmbReportType == null || tableModel == null || txtReportSummary == null) {
            return; // UI components not fully initialized yet
        }
        String reportType = (String) cmbReportType.getSelectedItem();
        if (reportType == null) {
            return;
        }
        
        try {
            tableModel.setRowCount(0);
            txtReportSummary.setText("Generating report...");
            
            switch (reportType) {
                case "Daily Sales Summary":
                    generateDailySalesReport();
                    break;
                case "Inventory Status":
                    generateInventoryReport();
                    break;
                case "Low Stock Alert":
                    generateLowStockReport();
                    break;
                case "Top Selling Products":
                    generateTopSellingReport();
                    break;
                case "Customer Analysis":
                    generateCustomerAnalysisReport();
                    break;
                case "Sales by Date Range":
                    generateSalesByDateRangeReport();
                    break;
                case "Profit Analysis":
                    generateProfitAnalysisReport();
                    break;
                case "Stock Valuation":
                    generateStockValuationReport();
                    break;
                default:
                    txtReportSummary.setText("Report type: " + reportType + "\n\nPlease select a valid report type from the dropdown.");
                    break;
            }
            
            LoggerUtil.logInfo(ReportEnterprise.class, "Generated report: " + reportType);
            
        } catch (Exception e) {
            LoggerUtil.logError(ReportEnterprise.class, "Error generating report", e);
            EnterpriseTheme.showError(this, "Error generating report: " + e.getMessage());
        }
    }
    
    private void generateDailySalesReport() {
        Map<String, Object> report = reportService.generateDailySalesReport();

        StringBuilder summary = new StringBuilder();
        summary.append("DAILY SALES REPORT - ").append(LocalDate.now()).append("\n\n");
        summary.append("Total Sales: $").append(String.format("%.2f", report.get("totalSales"))).append("\n");
        summary.append("Total Transactions: ").append(report.get("transactionCount")).append("\n");
        summary.append("Total Profit: $").append(String.format("%.2f", report.get("totalProfit"))).append("\n");

        // Calculate average transaction if we have transactions
        Double totalSales = (Double) report.get("totalSales");
        Integer transactionCount = (Integer) report.get("transactionCount");
        double avgTransaction = (transactionCount > 0) ? totalSales / transactionCount : 0.0;
        summary.append("Average Transaction: $").append(String.format("%.2f", avgTransaction)).append("\n");

        txtReportSummary.setText(summary.toString());

        tableModel.setColumnIdentifiers(new String[]{"Metric", "Value", "Details"});
        tableModel.addRow(new Object[]{"Total Sales", String.format("$%.2f", report.get("totalSales")), "Revenue for today"});
        tableModel.addRow(new Object[]{"Total Transactions", report.get("transactionCount"), "Number of sales"});
        tableModel.addRow(new Object[]{"Total Profit", String.format("$%.2f", report.get("totalProfit")), "Profit earned"});
        tableModel.addRow(new Object[]{"Average Transaction", String.format("$%.2f", avgTransaction), "Per transaction"});
    }
    
    private void generateInventoryReport() {
        Map<String, Object> report = reportService.generateInventoryReport();
        
        StringBuilder summary = new StringBuilder();
        summary.append("INVENTORY STATUS REPORT\n\n");
        summary.append("Total Products: ").append(report.get("totalProducts")).append("\n");
        summary.append("Total Stock Value: $").append(String.format("%.2f", report.get("totalInventoryValue"))).append("\n");
        summary.append("Low Stock Items: ").append(report.get("lowStockCount")).append("\n");
        
        txtReportSummary.setText(summary.toString());
        
        tableModel.addRow(new Object[]{"Total Products", report.get("totalProducts"), "Active products"});
        tableModel.addRow(new Object[]{"Total Stock Units", report.get("totalStockUnits"), "All items"});
        tableModel.addRow(new Object[]{"Inventory Value", String.format("$%.2f", report.get("totalInventoryValue")), "Current value"});
        tableModel.addRow(new Object[]{"Low Stock Items", report.get("lowStockCount"), "Needs reorder"});
    }
    
    private void generateLowStockReport() {
        Map<String, Object> report = reportService.generateInventoryReport();
        
        List<Map<String, Object>> products = new java.util.ArrayList<>();
        Object lowStockObj = report.get("lowStockProducts");
        
        if (lowStockObj instanceof List) {
            List<?> rawList = (List<?>) lowStockObj;
            
            // Convert ProductEntity objects to Map
            for (Object item : rawList) {
                if (item instanceof models.entity.ProductEntity) {
                    models.entity.ProductEntity product = (models.entity.ProductEntity) item;
                    Map<String, Object> productMap = new java.util.HashMap<>();
                    productMap.put("productId", product.getProductId());
                    productMap.put("productName", product.getName());
                    productMap.put("currentStock", product.getStock());
                    productMap.put("sellPrice", product.getSellPrice());
                    products.add(productMap);
                }
            }
        }
        
        txtReportSummary.setText("LOW STOCK ALERT\n\n" + products.size() + " products need restocking");
        
        tableModel.setColumnIdentifiers(new String[]{"Product ID", "Product Name", "Current Stock", "Min Level", "Action"});
        
        for (Map<String, Object> product : products) {
            tableModel.addRow(new Object[]{
                product.get("productId"),
                product.get("productName"),
                product.get("currentStock"),
                product.get("minStockLevel"),
                "Reorder Required"
            });
        }
    }
    
    private void generateTopSellingReport() {
        try {
            Map<String, Object> report = reportService.generateTopProductsReport(10);

            List<Map<String, Object>> products = new java.util.ArrayList<>();
            Object topProductsObj = report.get("topProducts");

            if (topProductsObj instanceof List) {
                List<?> rawList = (List<?>) topProductsObj;

                // Convert Object[] arrays to Map - safely handle different array lengths
                for (Object item : rawList) {
                    if (item instanceof Object[]) {
                        Object[] row = (Object[]) item;
                        Map<String, Object> productMap = new java.util.HashMap<>();

                        // Safely extract data based on available columns
                        if (row.length > 0) {
                            productMap.put("productName", row[0] != null ? row[0].toString() : "Unknown Product");
                        }
                        if (row.length > 1) {
                            productMap.put("totalQuantitySold", row[1] != null ? ((Number) row[1]).longValue() : 0L);
                        }
                        if (row.length > 2) {
                            productMap.put("totalRevenue", row[2] != null ? ((Number) row[2]).doubleValue() : 0.0);
                        }
                        if (row.length > 3) {
                            productMap.put("totalProfit", row[3] != null ? ((Number) row[3]).doubleValue() : 0.0);
                        } else {
                            // Calculate profit if not available (revenue - cost estimate)
                            double revenue = row.length > 2 && row[2] != null ? ((Number) row[2]).doubleValue() : 0.0;
                            productMap.put("totalProfit", revenue * 0.3); // Estimate 30% profit margin
                        }

                        products.add(productMap);
                    }
                }
            }

            StringBuilder summary = new StringBuilder();
            summary.append("TOP SELLING PRODUCTS REPORT\n\n");
            summary.append("Showing top ").append(products.size()).append(" best-selling items\n");
            summary.append("Ranked by total quantity sold\n\n");

            if (products.isEmpty()) {
                summary.append("No sales data available for the selected period.");
            } else {
                double totalRevenue = products.stream()
                    .mapToDouble(p -> (Double) p.getOrDefault("totalRevenue", 0.0))
                    .sum();
                summary.append("Total Revenue from Top Products: $").append(String.format("%.2f", totalRevenue));
            }

            txtReportSummary.setText(summary.toString());

            tableModel.setColumnIdentifiers(new String[]{"Product Name", "Quantity Sold", "Revenue", "Profit", "Profit Margin"});

            for (Map<String, Object> product : products) {
                double revenue = (Double) product.getOrDefault("totalRevenue", 0.0);
                double profit = (Double) product.getOrDefault("totalProfit", 0.0);
                double profitMargin = revenue > 0 ? (profit / revenue) * 100 : 0.0;

                tableModel.addRow(new Object[]{
                    product.getOrDefault("productName", "Unknown"),
                    product.getOrDefault("totalQuantitySold", 0L),
                    String.format("$%.2f", revenue),
                    String.format("$%.2f", profit),
                    String.format("%.1f%%", profitMargin)
                });
            }

        } catch (Exception e) {
            LoggerUtil.logError(ReportEnterprise.class, "Error generating top selling report", e);
            txtReportSummary.setText("Error generating Top Selling Products report: " + e.getMessage() +
                                   "\n\nPlease check if there is sales data available.");
            EnterpriseTheme.showError(this, "Error generating report: " + e.getMessage());
        }
    }
    
    private void generateCustomerAnalysisReport() {
        Map<String, Object> report = reportService.generateCustomerReport();
        
        List<Map<String, Object>> customers = new java.util.ArrayList<>();
        Object topCustomersObj = report.get("topCustomers");
        
        if (topCustomersObj instanceof List) {
            List<?> rawList = (List<?>) topCustomersObj;
            
            // Convert CustomerEntity objects to Map
            for (Object item : rawList) {
                if (item instanceof models.entity.CustomerEntity) {
                    models.entity.CustomerEntity customer = (models.entity.CustomerEntity) item;
                    Map<String, Object> customerMap = new java.util.HashMap<>();
                    customerMap.put("customerId", customer.getCustomerId());
                    customerMap.put("customerName", customer.getName());
                    customerMap.put("totalPurchases", customer.getTotalPurchases() != null ? customer.getTotalPurchases() : 0.0);
                    customerMap.put("transactionCount", 0); // Need to calculate from transactions
                    customerMap.put("averagePurchase", customer.getTotalPurchases() != null ? customer.getTotalPurchases() : 0.0);
                    customers.add(customerMap);
                }
            }
        }
        
        txtReportSummary.setText("TOP CUSTOMERS ANALYSIS\n\nShowing top " + customers.size() + " customers by purchase volume");
        
        tableModel.setColumnIdentifiers(new String[]{"Customer", "Total Purchases", "Transaction Count", "Average Purchase"});
        
        for (Map<String, Object> customer : customers) {
            tableModel.addRow(new Object[]{
                customer.get("customerName"),
                String.format("$%.2f", customer.get("totalPurchases")),
                customer.get("transactionCount"),
                String.format("$%.2f", customer.get("averagePurchase"))
            });
        }
    }

    /**
     * Generate Sales by Date Range Report
     */
    private void generateSalesByDateRangeReport() {
        try {
            // Get date range from UI
            if (dateFrom.getDate() == null || dateTo.getDate() == null) {
                txtReportSummary.setText("Error: Please select both From and To dates.");
                return;
            }
            LocalDate fromDate = dateFrom.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate toDate = dateTo.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

            // Convert LocalDate to LocalDateTime for service call
            java.time.LocalDateTime startDateTime = fromDate.atStartOfDay();
            java.time.LocalDateTime endDateTime = toDate.atTime(23, 59, 59);

            // Use report service to get data with actual date range
            Map<String, Object> dateRangeReport = reportService.generateSalesReport(startDateTime, endDateTime);

            StringBuilder summary = new StringBuilder();
            summary.append("SALES BY DATE RANGE\n\n");
            summary.append("Period: ").append(fromDate).append(" to ").append(toDate).append("\n\n");
            summary.append("Total Sales: $").append(String.format("%.2f", dateRangeReport.get("totalSales"))).append("\n");
            summary.append("Total Transactions: ").append(dateRangeReport.get("transactionCount")).append("\n");
            summary.append("Total Profit: $").append(String.format("%.2f", dateRangeReport.get("totalProfit"))).append("\n");
            summary.append("Profit Margin: ").append(String.format("%.1f%%", dateRangeReport.get("profitMargin"))).append("\n");
            summary.append("Average Transaction: $").append(String.format("%.2f", dateRangeReport.get("averageTransactionValue"))).append("\n");

            txtReportSummary.setText(summary.toString());

            // Show breakdown in table
            tableModel.setColumnIdentifiers(new String[]{"Metric", "Value", "Details"});
            tableModel.addRow(new Object[]{"Total Sales", String.format("$%.2f", dateRangeReport.get("totalSales")), "Revenue for selected period"});
            tableModel.addRow(new Object[]{"Total Transactions", dateRangeReport.get("transactionCount"), "Number of sales"});
            tableModel.addRow(new Object[]{"Total Profit", String.format("$%.2f", dateRangeReport.get("totalProfit")), "Profit earned"});
            tableModel.addRow(new Object[]{"Profit Margin", String.format("%.1f%%", dateRangeReport.get("profitMargin")), "Percentage profit"});
            tableModel.addRow(new Object[]{"Average Transaction", String.format("$%.2f", dateRangeReport.get("averageTransactionValue")), "Per transaction"});

        } catch (Exception e) {
            txtReportSummary.setText("Error generating Sales by Date Range report: " + e.getMessage());
            LoggerUtil.logError(ReportEnterprise.class, "Error generating sales by date range report", e);
        }
    }

    /**
     * Generate Profit Analysis Report
     */
    private void generateProfitAnalysisReport() {
        try {
            // Get top selling products data and calculate profit
            Map<String, Object> report = reportService.generateTopProductsReport(10);

            List<Map<String, Object>> products = new java.util.ArrayList<>();
            Object topProductsObj = report.get("topProducts");

            double totalRevenue = 0.0;
            double totalProfit = 0.0;

            if (topProductsObj instanceof List) {
                List<?> rawList = (List<?>) topProductsObj;

                for (Object item : rawList) {
                    if (item instanceof Object[]) {
                        Object[] row = (Object[]) item;
                        Map<String, Object> productMap = new java.util.HashMap<>();

                        if (row.length >= 4) {
                            productMap.put("productName", row[0] != null ? row[0].toString() : "Unknown");
                            productMap.put("totalQuantitySold", row[1] != null ? ((Number) row[1]).longValue() : 0L);
                            double revenue = row[2] != null ? ((Number) row[2]).doubleValue() : 0.0;
                            double profit = row[3] != null ? ((Number) row[3]).doubleValue() : 0.0;

                            productMap.put("totalRevenue", revenue);
                            productMap.put("totalProfit", profit);
                            productMap.put("profitMargin", revenue > 0 ? (profit / revenue * 100) : 0.0);

                            totalRevenue += revenue;
                            totalProfit += profit;

                            products.add(productMap);
                        }
                    }
                }
            }

            double overallProfitMargin = totalRevenue > 0 ? (totalProfit / totalRevenue * 100) : 0.0;

            StringBuilder summary = new StringBuilder();
            summary.append("PROFIT ANALYSIS\n\n");
            summary.append("Total Revenue: $").append(String.format("%.2f", totalRevenue)).append("\n");
            summary.append("Total Profit: $").append(String.format("%.2f", totalProfit)).append("\n");
            summary.append("Overall Profit Margin: ").append(String.format("%.1f%%", overallProfitMargin)).append("\n");

            txtReportSummary.setText(summary.toString());

            tableModel.setColumnIdentifiers(new String[]{"Product", "Revenue", "Profit", "Margin %"});

            for (Map<String, Object> product : products) {
                tableModel.addRow(new Object[]{
                        product.get("productName"),
                        String.format("$%.2f", product.get("totalRevenue")),
                        String.format("$%.2f", product.get("totalProfit")),
                        String.format("%.1f%%", product.get("profitMargin"))
                });
            }

        } catch (Exception e) {
            txtReportSummary.setText("Error generating Profit Analysis report: " + e.getMessage());
            LoggerUtil.logError(ReportEnterprise.class, "Error generating profit analysis report", e);
        }
    }

    /**
     * Generate Stock Valuation Report
     */
    private void generateStockValuationReport() {
        try {
            Map<String, Object> report = reportService.generateInventoryReport();

            List<Map<String, Object>> products = new java.util.ArrayList<>();
            Object inventoryObj = report.get("inventoryList");

            double totalValuation = 0.0;
            int totalItems = 0;

            if (inventoryObj instanceof List) {
                List<?> rawList = (List<?>) inventoryObj;

                for (Object item : rawList) {
                    if (item instanceof models.entity.ProductEntity) {
                        models.entity.ProductEntity product = (models.entity.ProductEntity) item;
                        Map<String, Object> productMap = new java.util.HashMap<>();

                        int stock = product.getStock() != null ? product.getStock() : 0;
                        double costPrice = product.getPurchasePrice() != null ? product.getPurchasePrice() : 0.0;
                        double sellPrice = product.getSellPrice() != null ? product.getSellPrice() : 0.0;
                        double costValue = stock * costPrice;
                        double sellValue = stock * sellPrice;

                        productMap.put("productName", product.getName());
                        productMap.put("stock", stock);
                        productMap.put("costPrice", costPrice);
                        productMap.put("costValue", costValue);
                        productMap.put("sellValue", sellValue);

                        totalValuation += costValue;
                        totalItems += stock;

                        products.add(productMap);
                    }
                }
            }

            StringBuilder summary = new StringBuilder();
            summary.append("STOCK VALUATION REPORT\n\n");
            summary.append("Total Items in Stock: ").append(totalItems).append("\n");
            summary.append("Total Stock Value (Cost): $").append(String.format("%.2f", totalValuation)).append("\n");
            summary.append("Number of Products: ").append(products.size()).append("\n");

            txtReportSummary.setText(summary.toString());

            tableModel.setColumnIdentifiers(new String[]{"Product", "Stock", "Cost Price", "Cost Value", "Sell Value"});

            for (Map<String, Object> product : products) {
                tableModel.addRow(new Object[]{
                        product.get("productName"),
                        product.get("stock"),
                        String.format("$%.2f", product.get("costPrice")),
                        String.format("$%.2f", product.get("costValue")),
                        String.format("$%.2f", product.get("sellValue"))
                });
            }

        } catch (Exception e) {
            txtReportSummary.setText("Error generating Stock Valuation report: " + e.getMessage());
            LoggerUtil.logError(ReportEnterprise.class, "Error generating stock valuation report", e);
        }
    }

    private void exportToPDF() {
        EnterpriseTheme.showWarning(this, "PDF Export functionality will be implemented in the next version.\n\nFor now, you can print the report using Ctrl+P.");
    }
    
    private void goBackToDashboard() {
        try {
            new DashboardEnterprise().setVisible(true);
            dispose();
        } catch (Exception e) {
            LoggerUtil.logError(ReportEnterprise.class, "Error returning to dashboard", e);
            EnterpriseTheme.showError(this, "Failed to open dashboard: " + e.getMessage());
        }
    }
}

/**
 * Enhanced JDateChooser implementation for date selection with user input capability
 * Supports both manual date entry and date picker buttons
 */
class JDateChooser extends JPanel {
    private JTextField dateField;
    private Date selectedDate;
    private SimpleDateFormat dateFormat;
    private java.beans.PropertyChangeSupport propertyChangeSupport;

    public JDateChooser() {
        setLayout(new BorderLayout());
        propertyChangeSupport = new java.beans.PropertyChangeSupport(this);

        // Create main date input field
        dateField = new JTextField(10);
        dateField.setEditable(true);  // FIXED: Make editable so users can type dates
        dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        // Add date validation and change detection
        dateField.addActionListener(e -> parseDateFromField());
        dateField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                parseDateFromField();
            }
        });

        // Create date picker buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 0));
        buttonPanel.setPreferredSize(new Dimension(80, 35));

        JButton btnPrevDay = new JButton("◀");
        JButton btnCalendar = new JButton("📅");
        JButton btnNextDay = new JButton("▶");

        btnPrevDay.setPreferredSize(new Dimension(25, 30));
        btnCalendar.setPreferredSize(new Dimension(25, 30));
        btnNextDay.setPreferredSize(new Dimension(25, 30));

        btnPrevDay.setToolTipText("Previous Day");
        btnCalendar.setToolTipText("Today");
        btnNextDay.setToolTipText("Next Day");

        // Date navigation functionality
        btnPrevDay.addActionListener(e -> adjustDate(-1));
        btnCalendar.addActionListener(e -> setDateToToday());
        btnNextDay.addActionListener(e -> adjustDate(1));

        buttonPanel.add(btnPrevDay);
        buttonPanel.add(btnCalendar);
        buttonPanel.add(btnNextDay);

        add(dateField, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.EAST);

        // Initialize with current date
        selectedDate = new Date();
        updateDateField();
    }

    public void setDate(Date date) {
        Date oldValue = this.selectedDate;
        this.selectedDate = date;
        updateDateField();

        // Fire property change event for listeners
        propertyChangeSupport.firePropertyChange("date", oldValue, date);
    }

    public Date getDate() {
        return selectedDate;
    }

    public void setDateFormatString(String format) {
        this.dateFormat = new SimpleDateFormat(format);
        updateDateField();
    }

    /**
     * Add PropertyChangeListener to detect date changes
     */
    public void addPropertyChangeListener(String propertyName, java.beans.PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
    }

    /**
     * Parse date from user input in text field
     */
    private void parseDateFromField() {
        String dateText = dateField.getText().trim();
        try {
            Date newDate = dateFormat.parse(dateText);
            if (!newDate.equals(selectedDate)) {
                setDate(newDate); // This will fire the property change event
                util.LoggerUtil.logInfo("📅 Date parsed from user input: " + dateText + " -> " + newDate);
            }
        } catch (java.text.ParseException e) {
            // Invalid date format - revert to previous valid date
            util.LoggerUtil.logError("Invalid date format: " + dateText + ". Expected format: yyyy-MM-dd", null);
            updateDateField(); // Revert to current selected date

            // Show user-friendly error
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this,
                    "Invalid date format: " + dateText + "\nPlease use format: yyyy-MM-dd\nExample: 2024-03-27",
                    "Date Format Error",
                    JOptionPane.WARNING_MESSAGE);
            });
        }
    }

    /**
     * Adjust date by specified number of days
     */
    private void adjustDate(int days) {
        if (selectedDate != null) {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTime(selectedDate);
            cal.add(java.util.Calendar.DAY_OF_MONTH, days);
            setDate(cal.getTime());
            util.LoggerUtil.logInfo("📅 Date adjusted by " + days + " days to: " + selectedDate);
        }
    }

    /**
     * Set date to today
     */
    private void setDateToToday() {
        setDate(new Date());
        util.LoggerUtil.logInfo("📅 Date set to today: " + selectedDate);
    }

    private void updateDateField() {
        if (selectedDate != null) {
            String formattedDate = dateFormat.format(selectedDate);
            if (!formattedDate.equals(dateField.getText())) {
                dateField.setText(formattedDate);
            }
        }
    }
}
