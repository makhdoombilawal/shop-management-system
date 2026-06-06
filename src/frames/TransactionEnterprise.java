package frames;

import models.Session;
import models.entity.TransactionEntity;
import service.TransactionService;
import util.EnterpriseTheme;
import util.LoggerUtil;
import util.DateUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Modern Enterprise Transaction Management Frame
 */
public class TransactionEnterprise extends BaseFrame {
    
    private final TransactionService transactionService = new TransactionService();
    
    // UI Components
    private JPanel headerPanel;
    private JPanel contentPanel;
    private JPanel filterPanel;
    private JPanel tablePanel;
    
    private JComboBox<String> cmbFilterType;
    private JButton btnFilter;
    private JButton btnRefresh;
    private JButton btnBack;
    
    private JTable transactionTable;
    private DefaultTableModel tableModel;
    
    private JLabel lblTotalAmount;
    private JLabel lblTransactionCount;
    
    public TransactionEnterprise() throws Exception {
        super();
        if (!authorized) return;
        
        setTitle("Transaction History - Shop Manager");
        setSize(1700, 900);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        initializeComponents();
        setupLayout();
        loadTransactions();
        updateSummary();
        
        EnterpriseTheme.applyGlobalTheme();
    }
    
    private void initializeComponents() {
        // Header
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(EnterpriseTheme.PRIMARY);
        headerPanel.setPreferredSize(new Dimension(0, 80));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        
        JLabel lblTitle = new JLabel("TRANSACTION HISTORY");
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
        
        createFilterPanel();
        createTablePanel();
    }
    
    private void createFilterPanel() {
        filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        filterPanel.setBackground(EnterpriseTheme.CARD_BG);
        filterPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(EnterpriseTheme.BORDER, 1),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        
        JLabel lblFilter = new JLabel("Transaction Type:");
        lblFilter.setFont(EnterpriseTheme.FONT_BODY);
        
        cmbFilterType = new JComboBox<>(new String[]{"All Transactions", "Sales Only", "Purchases Only"});
        EnterpriseTheme.styleComboBox(cmbFilterType);
        
        btnFilter = new JButton("Apply Filter");
        btnRefresh = new JButton("Refresh All");
        btnBack = new JButton("<< Back to Dashboard");
        
        EnterpriseTheme.stylePrimaryButton(btnFilter);
        EnterpriseTheme.styleSecondaryButton(btnRefresh);
        EnterpriseTheme.styleSecondaryButton(btnBack);
        
        btnFilter.addActionListener(e -> applyFilter());
        btnRefresh.addActionListener(e -> { loadTransactions(); updateSummary(); });
        btnBack.addActionListener(e -> goBackToDashboard());
        
        // Summary labels
        lblTotalAmount = new JLabel("Total: $0.00");
        lblTotalAmount.setFont(EnterpriseTheme.FONT_HEADER);
        lblTotalAmount.setForeground(EnterpriseTheme.SUCCESS);
        
        lblTransactionCount = new JLabel("Count: 0");
        lblTransactionCount.setFont(EnterpriseTheme.FONT_BODY);
        
        filterPanel.add(lblFilter);
        filterPanel.add(cmbFilterType);
        filterPanel.add(btnFilter);
        filterPanel.add(btnRefresh);
        filterPanel.add(Box.createHorizontalStrut(50));
        filterPanel.add(lblTransactionCount);
        filterPanel.add(Box.createHorizontalStrut(20));
        filterPanel.add(lblTotalAmount);
        filterPanel.add(Box.createHorizontalGlue());
        filterPanel.add(btnBack);
    }
    
    private void createTablePanel() {
        tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(EnterpriseTheme.CARD_BG);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(EnterpriseTheme.BORDER, 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        JLabel lblTableTitle = new JLabel("Transaction Records");
        lblTableTitle.setFont(EnterpriseTheme.FONT_HEADER);
        lblTableTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        String[] columns = {"ID", "Date", "Type", "Product", "Quantity", "Unit Price", "Total Amount", "Payment", "Customer/Supplier"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        transactionTable = new JTable(tableModel);
        EnterpriseTheme.styleTable(transactionTable);
        
        // Set column widths
        transactionTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        transactionTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        transactionTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        transactionTable.getColumnModel().getColumn(3).setPreferredWidth(200);
        transactionTable.getColumnModel().getColumn(8).setPreferredWidth(180);
        
        JScrollPane scrollPane = new JScrollPane(transactionTable);
        EnterpriseTheme.styleScrollPane(scrollPane);
        
        tablePanel.add(lblTableTitle, BorderLayout.NORTH);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        add(headerPanel, BorderLayout.NORTH);
        
        contentPanel.add(filterPanel, BorderLayout.NORTH);
        contentPanel.add(tablePanel, BorderLayout.CENTER);
        
        add(contentPanel, BorderLayout.CENTER);
    }
    
    private void loadTransactions() {
        try {
            List<TransactionEntity> transactions = transactionService.getAllTransactions();
            displayTransactions(transactions);
        } catch (Exception e) {
            LoggerUtil.logError(TransactionEnterprise.class, "Error loading transactions", e);
            EnterpriseTheme.showError(this, "Failed to load transactions: " + e.getMessage());
        }
    }
    
    private void applyFilter() {
        try {
            String filterValue = (String) cmbFilterType.getSelectedItem();
            List<TransactionEntity> transactions;
            
            if ("Sales Only".equals(filterValue)) {
                transactions = transactionService.getTransactionsByType("SALE");
            } else if ("Purchases Only".equals(filterValue)) {
                transactions = transactionService.getTransactionsByType("PURCHASE");
            } else {
                transactions = transactionService.getAllTransactions();
            }
            
            displayTransactions(transactions);
            updateSummary();
        } catch (Exception e) {
            LoggerUtil.logError(TransactionEnterprise.class, "Error applying filter", e);
            EnterpriseTheme.showError(this, "Failed to apply filter: " + e.getMessage());
        }
    }
    
    private void displayTransactions(List<TransactionEntity> transactions) {
        tableModel.setRowCount(0);
        
        for (TransactionEntity transaction : transactions) {
            // Safely get product name (handle lazy loading issues)
            String productName = "N/A";
            try {
                if (transaction.getProduct() != null) {
                    productName = transaction.getProduct().getName();
                }
            } catch (Exception e) {
                // Handle lazy loading exception - use stored product name if available
                productName = transaction.getProductName() != null ? transaction.getProductName() : "N/A";
            }
            
            // Display customer for SALE or supplier for PURCHASE
            String entityName = "";
            String transactionType = transaction.getTransactionType();
            
            if ("SALE".equalsIgnoreCase(transactionType)) {
                // Show customer for sales
                try {
                    if (transaction.getCustomer() != null) {
                        entityName = transaction.getCustomer().getName();
                    } else {
                        entityName = transaction.getCustomerName() != null ? transaction.getCustomerName() : "Walk-in Customer";
                    }
                } catch (Exception e) {
                    entityName = transaction.getCustomerName() != null ? transaction.getCustomerName() : "Walk-in Customer";
                }
            } else if ("PURCHASE".equalsIgnoreCase(transactionType)) {
                // Show supplier for purchases
                try {
                    if (transaction.getSupplier() != null) {
                        entityName = transaction.getSupplier().getCompanyName();
                    } else {
                        entityName = "Direct Purchase";
                    }
                } catch (Exception e) {
                    entityName = "Direct Purchase";
                }
            }
            
            // Format the transaction type for display with color indicator
            String typeDisplay = transactionType;
            if ("SALE".equalsIgnoreCase(transactionType)) {
                typeDisplay = "🔷 SALE";
            } else if ("PURCHASE".equalsIgnoreCase(transactionType)) {
                typeDisplay = "🔶 PURCHASE";
            }
            
            tableModel.addRow(new Object[]{
                transaction.getTransactionId(),
                DateUtil.formatDateTime(transaction.getTransactionDate()),
                typeDisplay,
                productName,
                transaction.getQuantity(),
                String.format("Rs. %.2f", 
                    "SALE".equalsIgnoreCase(transactionType) 
                        ? transaction.getSellPrice() 
                        : transaction.getPurchasePrice()),
                String.format("Rs. %.2f", transaction.getTotalAmount()),
                transaction.getPaymentType() != null ? transaction.getPaymentType() : "N/A",
                entityName
            });
        }
    }
    
    private void updateSummary() {
        int rowCount = tableModel.getRowCount();
        double totalAmount = 0.0;
        
        for (int i = 0; i < rowCount; i++) {
            String amountStr = tableModel.getValueAt(i, 6).toString()
                .replace("Rs.", "").replace(",", "").trim();
            try {
                totalAmount += Double.parseDouble(amountStr);
            } catch (NumberFormatException e) {
                // Skip invalid amounts
            }
        }
        
        lblTransactionCount.setText(String.format("Count: %d transactions", rowCount));
        lblTotalAmount.setText(String.format("Total: Rs. %,.2f", totalAmount));
    }
    
    private void goBackToDashboard() {
        try {
            new DashboardEnterprise().setVisible(true);
            dispose();
        } catch (Exception e) {
            LoggerUtil.logError(TransactionEnterprise.class, "Error returning to dashboard", e);
            EnterpriseTheme.showError(this, "Failed to open dashboard: " + e.getMessage());
        }
    }
}
