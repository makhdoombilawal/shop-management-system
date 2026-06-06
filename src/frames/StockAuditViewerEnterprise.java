package frames;

import models.Session;
import models.entity.StockAuditEntity;
import models.entity.StockAuditEntity.ChangeSource;
import service.StockAuditService;
import util.EnterpriseTheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Modern Enterprise Stock Audit Viewer Frame
 * Read-only view of stock change history with filtering
 */
public class StockAuditViewerEnterprise extends BaseFrame {
    
    private final StockAuditService stockAuditService = new StockAuditService();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    // UI Components
    private JPanel headerPanel;
    private JPanel contentPanel;
    private JPanel filterPanel;
    private JPanel tablePanel;
    private JPanel statsPanel;
    
    private JTextField txtProductSearch;
    private JComboBox<String> cmbChangeSource;
    private JTextField txtStartDate;
    private JTextField txtEndDate;
    private JSpinner spinLimit;
    
    private JTable auditTable;
    private DefaultTableModel tableModel;
    
    private JLabel lblTotalRecords;
    private JLabel lblIncreases;
    private JLabel lblDecreases;
    
    private JButton btnFilter;
    private JButton btnReset;
    private JButton btnBack;
    private JButton btnRefresh;
    private JButton btnToday;
    
    public StockAuditViewerEnterprise() throws Exception {
        super();
        if (!authorized) return;
        
        setTitle("Stock Audit History - Shop Manager");
        setSize(1600, 850);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        initializeComponents();
        setupLayout();
        loadRecentAudits();
        updateStatistics();
        
        EnterpriseTheme.applyGlobalTheme();
    }
    
    private void initializeComponents() {
        // Header
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(EnterpriseTheme.PRIMARY);
        headerPanel.setPreferredSize(new Dimension(0, 80));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        
        JLabel lblTitle = new JLabel("STOCK AUDIT HISTORY");
        lblTitle.setFont(EnterpriseTheme.FONT_TITLE);
        lblTitle.setForeground(EnterpriseTheme.TEXT_ON_PRIMARY);
        
        JLabel lblUser = new JLabel("User: " + Session.getUsername() + " | Role: " + Session.getRole());
        lblUser.setFont(EnterpriseTheme.FONT_BODY);
        lblUser.setForeground(EnterpriseTheme.TEXT_ON_PRIMARY);
        
        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(lblUser, BorderLayout.EAST);
        
        // Content Panel
        contentPanel = new JPanel(new BorderLayout(0, 15));
        contentPanel.setBackground(EnterpriseTheme.BACKGROUND);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        createStatsPanel();
        createFilterPanel();
        createTablePanel();
    }
    
    private void createStatsPanel() {
        statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        statsPanel.setBackground(EnterpriseTheme.BACKGROUND);
        
        lblTotalRecords = createStatLabel("Total Records: 0", EnterpriseTheme.INFO);
        lblIncreases = createStatLabel("Increases: 0", EnterpriseTheme.SUCCESS);
        lblDecreases = createStatLabel("Decreases: 0", EnterpriseTheme.DANGER);
        
        statsPanel.add(lblTotalRecords);
        statsPanel.add(lblIncreases);
        statsPanel.add(lblDecreases);
    }
    
    private JLabel createStatLabel(String text, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(EnterpriseTheme.FONT_SUBHEADER);
        label.setForeground(color);
        label.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color, 2),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        label.setOpaque(true);
        label.setBackground(Color.WHITE);
        return label;
    }
    
    private void createFilterPanel() {
        filterPanel = new JPanel(new GridBagLayout());
        filterPanel.setBackground(EnterpriseTheme.CARD_BG);
        filterPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(EnterpriseTheme.BORDER, 1),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Filter Title
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 8;
        JLabel lblFilterTitle = new JLabel("Filters");
        lblFilterTitle.setFont(EnterpriseTheme.FONT_SUBHEADER);
        lblFilterTitle.setForeground(EnterpriseTheme.TEXT_PRIMARY);
        filterPanel.add(lblFilterTitle, gbc);
        
        gbc.gridwidth = 1;
        int row = 1;
        
        // Product Search
        gbc.gridx = 0; gbc.gridy = row;
        JLabel lblProduct = new JLabel("Product:");
        lblProduct.setFont(EnterpriseTheme.FONT_BODY);
        filterPanel.add(lblProduct, gbc);
        
        gbc.gridx = 1; gbc.gridwidth = 2;
        txtProductSearch = new JTextField(15);
        txtProductSearch.setFont(EnterpriseTheme.FONT_INPUT);
        filterPanel.add(txtProductSearch, gbc);
        
        // Change Source
        gbc.gridx = 3; gbc.gridwidth = 1;
        JLabel lblSource = new JLabel("Source:");
        lblSource.setFont(EnterpriseTheme.FONT_BODY);
        filterPanel.add(lblSource, gbc);
        
        gbc.gridx = 4;
        cmbChangeSource = new JComboBox<>(new String[]{
            "-- All Sources --", "SALE", "PURCHASE", "RETURN", "ADJUSTMENT", "DAMAGE", "SYSTEM"
        });
        cmbChangeSource.setFont(EnterpriseTheme.FONT_INPUT);
        filterPanel.add(cmbChangeSource, gbc);
        row++;
        
        // Date Range
        gbc.gridx = 0; gbc.gridy = row;
        JLabel lblStartDate = new JLabel("From Date:");
        lblStartDate.setFont(EnterpriseTheme.FONT_BODY);
        filterPanel.add(lblStartDate, gbc);
        
        gbc.gridx = 1;
        LocalDate today = LocalDate.now();
        txtStartDate = new JTextField(today.minusDays(30).format(dateFormatter), 10);
        txtStartDate.setFont(EnterpriseTheme.FONT_INPUT);
        filterPanel.add(txtStartDate, gbc);
        
        gbc.gridx = 2;
        JLabel lblEndDate = new JLabel("To Date:");
        lblEndDate.setFont(EnterpriseTheme.FONT_BODY);
        filterPanel.add(lblEndDate, gbc);
        
        gbc.gridx = 3;
        txtEndDate = new JTextField(today.format(dateFormatter), 10);
        txtEndDate.setFont(EnterpriseTheme.FONT_INPUT);
        filterPanel.add(txtEndDate, gbc);
        
        gbc.gridx = 4;
        JLabel lblLimit = new JLabel("Limit:");
        lblLimit.setFont(EnterpriseTheme.FONT_BODY);
        filterPanel.add(lblLimit, gbc);
        
        gbc.gridx = 5;
        spinLimit = new JSpinner(new SpinnerNumberModel(100, 10, 1000, 10));
        spinLimit.setFont(EnterpriseTheme.FONT_INPUT);
        filterPanel.add(spinLimit, gbc);
        row++;
        
        // Buttons
        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 6;
        gbc.insets = new Insets(15, 10, 5, 10);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(EnterpriseTheme.CARD_BG);
        
        btnFilter = new JButton("Apply Filter");
        btnReset = new JButton("Reset");
        btnToday = new JButton("Today");
        
        EnterpriseTheme.stylePrimaryButton(btnFilter);
        EnterpriseTheme.styleWarningButton(btnReset);
        EnterpriseTheme.stylePrimaryButton(btnToday);
        
        EnterpriseTheme.setStandardButtonSize(btnFilter);
        EnterpriseTheme.setStandardButtonSize(btnReset);
        EnterpriseTheme.setStandardButtonSize(btnToday);
        
        btnFilter.addActionListener(e -> applyFilters());
        btnReset.addActionListener(e -> resetFilters());
        btnToday.addActionListener(e -> loadTodayAudits());
        
        buttonPanel.add(btnFilter);
        buttonPanel.add(btnReset);
        buttonPanel.add(btnToday);
        
        filterPanel.add(buttonPanel, gbc);
    }
    
    private void createTablePanel() {
        tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(EnterpriseTheme.CARD_BG);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(EnterpriseTheme.BORDER, 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        // Table Header
        JPanel tableHeaderPanel = new JPanel(new BorderLayout());
        tableHeaderPanel.setBackground(EnterpriseTheme.CARD_BG);
        tableHeaderPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        JLabel lblTableTitle = new JLabel("Stock Change History");
        lblTableTitle.setFont(EnterpriseTheme.FONT_SUBHEADER);
        lblTableTitle.setForeground(EnterpriseTheme.TEXT_PRIMARY);
        
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setBackground(EnterpriseTheme.CARD_BG);
        
        btnRefresh = new JButton("Refresh");
        btnBack = new JButton("Back");
        
        EnterpriseTheme.stylePrimaryButton(btnRefresh);
        EnterpriseTheme.styleSecondaryButton(btnBack);
        
        EnterpriseTheme.setStandardButtonSize(btnRefresh);
        EnterpriseTheme.setStandardButtonSize(btnBack);
        
        btnRefresh.addActionListener(e -> loadRecentAudits());
        btnBack.addActionListener(e -> {
            dispose();
            try {
                new DashboardEnterprise().setVisible(true);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error opening dashboard: " + ex.getMessage());
            }
        });
        
        actionPanel.add(btnRefresh);
        actionPanel.add(btnBack);
        
        tableHeaderPanel.add(lblTableTitle, BorderLayout.WEST);
        tableHeaderPanel.add(actionPanel, BorderLayout.EAST);
        
        // Table
        String[] columns = {"Audit ID", "Product", "Before", "After", "Change", "Source", "Date", "User", "Transaction", "Remarks"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        auditTable = new JTable(tableModel);
        auditTable.setFont(EnterpriseTheme.FONT_TABLE_CELL);
        auditTable.setForeground(EnterpriseTheme.TEXT_PRIMARY);
        auditTable.setBackground(Color.WHITE);
        auditTable.setSelectionBackground(EnterpriseTheme.PRIMARY.brighter());
        auditTable.setSelectionForeground(Color.WHITE);
        auditTable.setGridColor(EnterpriseTheme.BORDER);
        auditTable.setShowGrid(true);
        auditTable.getTableHeader().setFont(EnterpriseTheme.FONT_TABLE_HEADER);
        auditTable.getTableHeader().setBackground(EnterpriseTheme.PRIMARY);
        auditTable.getTableHeader().setForeground(EnterpriseTheme.TEXT_ON_PRIMARY);
        
        EnterpriseTheme.setStandardTableRowHeight(auditTable);
        
        JScrollPane scrollPane = new JScrollPane(auditTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(EnterpriseTheme.BORDER));
        
        tablePanel.add(tableHeaderPanel, BorderLayout.NORTH);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        add(headerPanel, BorderLayout.NORTH);
        
        contentPanel.add(statsPanel, BorderLayout.NORTH);
        contentPanel.add(filterPanel, BorderLayout.CENTER);
        contentPanel.add(tablePanel, BorderLayout.SOUTH);
        
        add(contentPanel, BorderLayout.CENTER);
    }
    
    private void loadAudits(List<StockAuditEntity> audits) {
        tableModel.setRowCount(0);
        
        for (StockAuditEntity audit : audits) {
            String productName = audit.getProduct() != null ? audit.getProduct().getName() : "N/A";
            String userName = audit.getChangedBy() != null ? audit.getChangedBy().getUsername() : "System";
            String transactionId = audit.getTransaction() != null ? audit.getTransaction().getTransactionId().toString() : "N/A";
            
            Object[] row = {
                audit.getAuditId(),
                productName,
                audit.getStockBefore(),
                audit.getStockAfter(),
                audit.getChangeAmount(),
                audit.getChangeSource(),
                audit.getChangeDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                userName,
                transactionId,
                audit.getRemarks()
            };
            tableModel.addRow(row);
        }
        
        updateStatistics();
    }
    
    private void loadRecentAudits() {
        try {
            int limit = (Integer) spinLimit.getValue();
            List<StockAuditEntity> audits = stockAuditService.getRecentAudits(limit);
            loadAudits(audits);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading audits: " + e.getMessage(), 
                "Load Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadTodayAudits() {
        try {
            List<StockAuditEntity> audits = stockAuditService.getTodayStockChanges();
            loadAudits(audits);
            JOptionPane.showMessageDialog(this, 
                "Showing today's stock changes", 
                "Filter Applied", 
                JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading today's audits: " + e.getMessage(), 
                "Load Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void applyFilters() {
        try {
            List<StockAuditEntity> audits;
            
            // Filter by product name
            String productSearch = txtProductSearch.getText().trim();
            if (!productSearch.isEmpty()) {
                audits = stockAuditService.searchByProductName(productSearch);
                loadAudits(audits);
                return;
            }
            
            // Filter by change source
            String selectedSource = (String) cmbChangeSource.getSelectedItem();
            if (selectedSource != null && !selectedSource.contains("All")) {
                ChangeSource source = ChangeSource.valueOf(selectedSource);
                audits = stockAuditService.getAuditsBySource(source);
                loadAudits(audits);
                return;
            }
            
            // Filter by date range
            LocalDate startDate = LocalDate.parse(txtStartDate.getText(), dateFormatter);
            LocalDate endDate = LocalDate.parse(txtEndDate.getText(), dateFormatter);
            audits = stockAuditService.getAuditsByDateRange(startDate, endDate);
            loadAudits(audits);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error applying filters: " + e.getMessage(), 
                "Filter Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void resetFilters() {
        txtProductSearch.setText("");
        cmbChangeSource.setSelectedIndex(0);
        LocalDate today = LocalDate.now();
        txtStartDate.setText(today.minusDays(30).format(dateFormatter));
        txtEndDate.setText(today.format(dateFormatter));
        spinLimit.setValue(100);
        loadRecentAudits();
    }
    
    private void updateStatistics() {
        int totalRecords = tableModel.getRowCount();
        int increases = 0;
        int decreases = 0;
        
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            Object changeObj = tableModel.getValueAt(i, 4); // Change column
            if (changeObj != null) {
                int change = Integer.parseInt(changeObj.toString());
                if (change > 0) increases++;
                else if (change < 0) decreases++;
            }
        }
        
        lblTotalRecords.setText("Total Records: " + totalRecords);
        lblIncreases.setText("Increases: " + increases);
        lblDecreases.setText("Decreases: " + decreases);
    }
}
