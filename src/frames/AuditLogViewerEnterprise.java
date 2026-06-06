package frames;

import models.Session;
import models.entity.AuditLogEntity;
import models.entity.AuditLogEntity.Action;
import models.entity.AuditLogEntity.EntityType;
import service.AuditLogService;
import util.EnterpriseTheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Modern Enterprise Audit Log Viewer Frame
 * Read-only view of system audit trail with filtering
 */
public class AuditLogViewerEnterprise extends BaseFrame {
    
    private final AuditLogService auditLogService = new AuditLogService();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    // UI Components
    private JPanel headerPanel;
    private JPanel contentPanel;
    private JPanel filterPanel;
    private JPanel tablePanel;
    private JPanel statsPanel;
    
    private JComboBox<String> cmbEntityType;
    private JComboBox<String> cmbAction;
    private JTextField txtStartDate;
    private JTextField txtEndDate;
    private JTextField txtSearchRemarks;
    private JSpinner spinLimit;
    
    private JTable auditTable;
    private DefaultTableModel tableModel;
    
    private JLabel lblTotalLogs;
    private JLabel lblCreates;
    private JLabel lblUpdates;
    private JLabel lblDeletes;
    private JLabel lblLogins;
    
    private JButton btnFilter;
    private JButton btnReset;
    private JButton btnBack;
    private JButton btnRefresh;
    private JButton btnToday;
    private JButton btnSecurity;
    private JButton btnDataMods;
    
    public AuditLogViewerEnterprise() throws Exception {
        super();
        if (!authorized) return;
        
        setTitle("System Audit Log - Shop Manager");
        setSize(1700, 900);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        initializeComponents();
        setupLayout();
        loadRecentLogs();
        updateStatistics();
        
        EnterpriseTheme.applyGlobalTheme();
    }
    
    private void initializeComponents() {
        // Header
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(EnterpriseTheme.PRIMARY);
        headerPanel.setPreferredSize(new Dimension(0, 80));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        
        JLabel lblTitle = new JLabel("SYSTEM AUDIT LOG");
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
        statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        statsPanel.setBackground(EnterpriseTheme.BACKGROUND);
        
        AuditLogService.AuditStatistics stats = auditLogService.getAuditStatistics();
        
        lblTotalLogs = createStatLabel("Total Logs: " + stats.getTotalLogs(), EnterpriseTheme.INFO);
        lblCreates = createStatLabel("Creates: " + stats.getCreates(), EnterpriseTheme.SUCCESS);
        lblUpdates = createStatLabel("Updates: " + stats.getUpdates(), EnterpriseTheme.WARNING);
        lblDeletes = createStatLabel("Deletes: " + stats.getDeletes(), EnterpriseTheme.DANGER);
        lblLogins = createStatLabel("Logins: " + stats.getLogins(), EnterpriseTheme.PRIMARY);
        
        statsPanel.add(lblTotalLogs);
        statsPanel.add(lblCreates);
        statsPanel.add(lblUpdates);
        statsPanel.add(lblDeletes);
        statsPanel.add(lblLogins);
    }
    
    private JLabel createStatLabel(String text, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 12));
        label.setForeground(color);
        label.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color, 2),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
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
        JLabel lblFilterTitle = new JLabel("Filters & Quick Actions");
        lblFilterTitle.setFont(EnterpriseTheme.FONT_SUBHEADER);
        lblFilterTitle.setForeground(EnterpriseTheme.TEXT_PRIMARY);
        filterPanel.add(lblFilterTitle, gbc);
        
        gbc.gridwidth = 1;
        int row = 1;
        
        // Entity Type
        gbc.gridx = 0; gbc.gridy = row;
        JLabel lblEntity = new JLabel("Entity:");
        lblEntity.setFont(EnterpriseTheme.FONT_BODY);
        filterPanel.add(lblEntity, gbc);
        
        gbc.gridx = 1;
        cmbEntityType = new JComboBox<>(new String[]{
            "-- All Entities --", "USER", "PRODUCT", "CUSTOMER", "TRANSACTION", "BARCODE", "CATEGORY"
        });
        cmbEntityType.setFont(EnterpriseTheme.FONT_INPUT);
        filterPanel.add(cmbEntityType, gbc);
        
        // Action
        gbc.gridx = 2;
        JLabel lblAction = new JLabel("Action:");
        lblAction.setFont(EnterpriseTheme.FONT_BODY);
        filterPanel.add(lblAction, gbc);
        
        gbc.gridx = 3;
        cmbAction = new JComboBox<>(new String[]{
            "-- All Actions --", "CREATE", "UPDATE", "DELETE", "LOGIN", "LOGOUT", "PRICE_CHANGE", "STATUS_CHANGE"
        });
        cmbAction.setFont(EnterpriseTheme.FONT_INPUT);
        filterPanel.add(cmbAction, gbc);
        
        // Search Remarks
        gbc.gridx = 4;
        JLabel lblSearch = new JLabel("Search:");
        lblSearch.setFont(EnterpriseTheme.FONT_BODY);
        filterPanel.add(lblSearch, gbc);
        
        gbc.gridx = 5; gbc.gridwidth = 2;
        txtSearchRemarks = new JTextField(15);
        txtSearchRemarks.setFont(EnterpriseTheme.FONT_INPUT);
        filterPanel.add(txtSearchRemarks, gbc);
        row++;
        
        gbc.gridwidth = 1;
        
        // Date Range
        gbc.gridx = 0; gbc.gridy = row;
        JLabel lblStartDate = new JLabel("From Date:");
        lblStartDate.setFont(EnterpriseTheme.FONT_BODY);
        filterPanel.add(lblStartDate, gbc);
        
        gbc.gridx = 1;
        LocalDate today = LocalDate.now();
        txtStartDate = new JTextField(today.minusDays(7).format(dateFormatter), 10);
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
        gbc.gridwidth = 7;
        gbc.insets = new Insets(15, 10, 5, 10);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        buttonPanel.setBackground(EnterpriseTheme.CARD_BG);
        
        btnFilter = new JButton("Apply Filter");
        btnReset = new JButton("Reset");
        btnToday = new JButton("Today");
        btnSecurity = new JButton("Security Events");
        btnDataMods = new JButton("Data Changes");
        
        EnterpriseTheme.stylePrimaryButton(btnFilter);
        EnterpriseTheme.styleWarningButton(btnReset);
        EnterpriseTheme.stylePrimaryButton(btnToday);
        EnterpriseTheme.styleDangerButton(btnSecurity);
        EnterpriseTheme.styleSuccessButton(btnDataMods);
        
        EnterpriseTheme.setStandardButtonSize(btnFilter);
        EnterpriseTheme.setStandardButtonSize(btnReset);
        EnterpriseTheme.setStandardButtonSize(btnToday);
        EnterpriseTheme.setStandardButtonSize(btnSecurity);
        EnterpriseTheme.setStandardButtonSize(btnDataMods);
        
        btnFilter.addActionListener(e -> applyFilters());
        btnReset.addActionListener(e -> resetFilters());
        btnToday.addActionListener(e -> loadTodayLogs());
        btnSecurity.addActionListener(e -> loadSecurityEvents());
        btnDataMods.addActionListener(e -> loadDataModifications());
        
        buttonPanel.add(btnFilter);
        buttonPanel.add(btnReset);
        buttonPanel.add(btnToday);
        buttonPanel.add(btnSecurity);
        buttonPanel.add(btnDataMods);
        
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
        
        JLabel lblTableTitle = new JLabel("Audit Trail");
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
        
        btnRefresh.addActionListener(e -> loadRecentLogs());
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
        String[] columns = {"Log ID", "Entity Type", "Entity ID", "Action", "Old Value", "New Value", "Date Time", "User", "IP Address", "Remarks"};
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
        
        // Set column widths
        auditTable.getColumnModel().getColumn(0).setPreferredWidth(60);  // Log ID
        auditTable.getColumnModel().getColumn(1).setPreferredWidth(100); // Entity Type
        auditTable.getColumnModel().getColumn(2).setPreferredWidth(70);  // Entity ID
        auditTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Action
        auditTable.getColumnModel().getColumn(4).setPreferredWidth(150); // Old Value
        auditTable.getColumnModel().getColumn(5).setPreferredWidth(150); // New Value
        auditTable.getColumnModel().getColumn(6).setPreferredWidth(140); // Date Time
        auditTable.getColumnModel().getColumn(7).setPreferredWidth(100); // User
        auditTable.getColumnModel().getColumn(8).setPreferredWidth(110); // IP
        auditTable.getColumnModel().getColumn(9).setPreferredWidth(200); // Remarks
        
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
    
    private void loadLogs(List<AuditLogEntity> logs) {
        tableModel.setRowCount(0);
        
        for (AuditLogEntity log : logs) {
            String userName = log.getPerformedBy() != null ? log.getPerformedBy().getUsername() : "System";
            
            Object[] row = {
                log.getLogId(),
                log.getEntityType(),
                log.getEntityId(),
                log.getAction(),
                truncate(log.getOldValue(), 30),
                truncate(log.getNewValue(), 30),
                log.getLogDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                userName,
                log.getIpAddress() != null ? log.getIpAddress() : "N/A",
                truncate(log.getRemarks(), 40)
            };
            tableModel.addRow(row);
        }
    }
    
    private String truncate(String text, int maxLength) {
        if (text == null || text.isEmpty()) return "N/A";
        return text.length() > maxLength ? text.substring(0, maxLength) + "..." : text;
    }
    
    private void loadRecentLogs() {
        try {
            int limit = (Integer) spinLimit.getValue();
            List<AuditLogEntity> logs = auditLogService.getRecentLogs(limit);
            loadLogs(logs);
            updateStatistics();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading logs: " + e.getMessage(), 
                "Load Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadTodayLogs() {
        try {
            List<AuditLogEntity> logs = auditLogService.getTodayLogs();
            loadLogs(logs);
            JOptionPane.showMessageDialog(this, 
                "Showing today's audit logs", 
                "Filter Applied", 
                JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading today's logs: " + e.getMessage(), 
                "Load Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadSecurityEvents() {
        try {
            List<AuditLogEntity> logs = auditLogService.getSecurityEvents();
            loadLogs(logs);
            JOptionPane.showMessageDialog(this, 
                "Showing security events (LOGIN/LOGOUT)", 
                "Filter Applied", 
                JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading security events: " + e.getMessage(), 
                "Load Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadDataModifications() {
        try {
            List<AuditLogEntity> logs = auditLogService.getDataModifications();
            loadLogs(logs);
            JOptionPane.showMessageDialog(this, 
                "Showing data modifications (CREATE/UPDATE/DELETE)", 
                "Filter Applied", 
                JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading data modifications: " + e.getMessage(), 
                "Load Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void applyFilters() {
        try {
            // Search by remarks
            String searchText = txtSearchRemarks.getText().trim();
            if (!searchText.isEmpty()) {
                List<AuditLogEntity> logs = auditLogService.searchByRemarks(searchText);
                loadLogs(logs);
                return;
            }
            
            // Filter by entity type
            String selectedEntity = (String) cmbEntityType.getSelectedItem();
            if (selectedEntity != null && !selectedEntity.contains("All")) {
                EntityType entityType = EntityType.valueOf(selectedEntity);
                List<AuditLogEntity> logs = auditLogService.getLogsByEntityType(entityType);
                loadLogs(logs);
                return;
            }
            
            // Filter by action
            String selectedAction = (String) cmbAction.getSelectedItem();
            if (selectedAction != null && !selectedAction.contains("All")) {
                Action action = Action.valueOf(selectedAction);
                List<AuditLogEntity> logs = auditLogService.getLogsByAction(action);
                loadLogs(logs);
                return;
            }
            
            // Filter by date range
            LocalDate startDate = LocalDate.parse(txtStartDate.getText(), dateFormatter);
            LocalDate endDate = LocalDate.parse(txtEndDate.getText(), dateFormatter);
            List<AuditLogEntity> logs = auditLogService.getLogsByDateRange(startDate, endDate);
            loadLogs(logs);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error applying filters: " + e.getMessage(), 
                "Filter Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void resetFilters() {
        cmbEntityType.setSelectedIndex(0);
        cmbAction.setSelectedIndex(0);
        txtSearchRemarks.setText("");
        LocalDate today = LocalDate.now();
        txtStartDate.setText(today.minusDays(7).format(dateFormatter));
        txtEndDate.setText(today.format(dateFormatter));
        spinLimit.setValue(100);
        loadRecentLogs();
    }
    
    private void updateStatistics() {
        try {
            AuditLogService.AuditStatistics stats = auditLogService.getAuditStatistics();
            lblTotalLogs.setText("Total Logs: " + stats.getTotalLogs());
            lblCreates.setText("Creates: " + stats.getCreates());
            lblUpdates.setText("Updates: " + stats.getUpdates());
            lblDeletes.setText("Deletes: " + stats.getDeletes());
            lblLogins.setText("Logins: " + stats.getLogins());
        } catch (Exception e) {
            // Silently fail to avoid disrupting the UI
        }
    }
}
