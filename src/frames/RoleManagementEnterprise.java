package frames;

import models.Session;
import models.entity.RoleEntity;
import service.RoleService;
import util.EnterpriseTheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Modern Enterprise Role Management Frame
 */
public class RoleManagementEnterprise extends BaseFrame {
    
    private final RoleService roleService = new RoleService();
    
    // UI Components
    private JPanel headerPanel;
    private JPanel contentPanel;
    private JPanel formPanel;
    private JPanel tablePanel;
    
    private JTextField txtRoleId;
    private JTextField txtName;
    private JTextArea txtDescription;
    
    private JTable roleTable;
    private DefaultTableModel tableModel;
    
    private JButton btnAdd;
    private JButton btnUpdate;
    private JButton btnDelete;
    private JButton btnClear;
    private JButton btnBack;
    private JButton btnRefresh;
    private JButton btnInitDefaults;
    
    public RoleManagementEnterprise() throws Exception {
        super();
        if (!authorized) return;
        
        setTitle("Role Management - Shop Manager");
        setSize(1400, 800);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        initializeComponents();
        setupLayout();
        loadRoles();
        
        EnterpriseTheme.applyGlobalTheme();
    }
    
    private void initializeComponents() {
        // Header
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(EnterpriseTheme.PRIMARY);
        headerPanel.setPreferredSize(new Dimension(0, 80));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        
        JLabel lblTitle = new JLabel("ROLE MANAGEMENT");
        lblTitle.setFont(EnterpriseTheme.FONT_TITLE);
        lblTitle.setForeground(EnterpriseTheme.TEXT_ON_PRIMARY);
        
        JLabel lblUser = new JLabel("User: " + Session.getUsername() + " | Role: " + Session.getRole());
        lblUser.setFont(EnterpriseTheme.FONT_BODY);
        lblUser.setForeground(EnterpriseTheme.TEXT_ON_PRIMARY);
        
        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(lblUser, BorderLayout.EAST);
        
        // Content Panel
        contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(EnterpriseTheme.BACKGROUND);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        createFormPanel();
        createTablePanel();
    }
    
    private void createFormPanel() {
        formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(EnterpriseTheme.CARD_BG);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(EnterpriseTheme.BORDER, 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        int row = 0;
        
        // Role ID (hidden from users, auto-generated)
        gbc.gridx = 0; gbc.gridy = row;
        JLabel lblRoleId = new JLabel("Role ID:");
        lblRoleId.setFont(EnterpriseTheme.FONT_BODY);
        lblRoleId.setForeground(EnterpriseTheme.TEXT_SECONDARY);
        formPanel.add(lblRoleId, gbc);
        
        gbc.gridx = 1;
        txtRoleId = new JTextField(20);
        txtRoleId.setFont(EnterpriseTheme.FONT_INPUT);
        txtRoleId.setEnabled(false);
        formPanel.add(txtRoleId, gbc);
        row++;
        
        // Role Name
        gbc.gridx = 0; gbc.gridy = row;
        JLabel lblName = new JLabel("Role Name: *");
        lblName.setFont(EnterpriseTheme.FONT_BODY);
        lblName.setForeground(EnterpriseTheme.TEXT_PRIMARY);
        formPanel.add(lblName, gbc);
        
        gbc.gridx = 1;
        txtName = new JTextField(20);
        txtName.setFont(EnterpriseTheme.FONT_INPUT);
        formPanel.add(txtName, gbc);
        row++;
        
        // Description
        gbc.gridx = 0; gbc.gridy = row;
        gbc.anchor = GridBagConstraints.NORTH;
        JLabel lblDescription = new JLabel("Description:");
        lblDescription.setFont(EnterpriseTheme.FONT_BODY);
        lblDescription.setForeground(EnterpriseTheme.TEXT_PRIMARY);
        formPanel.add(lblDescription, gbc);
        
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.CENTER;
        txtDescription = new JTextArea(3, 20);
        txtDescription.setFont(EnterpriseTheme.FONT_INPUT);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        JScrollPane scrollDescription = new JScrollPane(txtDescription);
        formPanel.add(scrollDescription, gbc);
        row++;
        
        // Buttons Panel
        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 8, 8, 8);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(EnterpriseTheme.CARD_BG);
        
        btnAdd = new JButton("Add Role");
        btnUpdate = new JButton("Update Role");
        btnDelete = new JButton("Delete Role");
        btnClear = new JButton("Clear Form");
        btnInitDefaults = new JButton("Init Defaults");
        
        EnterpriseTheme.styleSuccessButton(btnAdd);
        EnterpriseTheme.stylePrimaryButton(btnUpdate);
        EnterpriseTheme.styleDangerButton(btnDelete);
        EnterpriseTheme.styleWarningButton(btnClear);
        EnterpriseTheme.stylePrimaryButton(btnInitDefaults);
        
        EnterpriseTheme.setStandardButtonSize(btnAdd);
        EnterpriseTheme.setStandardButtonSize(btnUpdate);
        EnterpriseTheme.setStandardButtonSize(btnDelete);
        EnterpriseTheme.setStandardButtonSize(btnClear);
        EnterpriseTheme.setStandardButtonSize(btnInitDefaults);
        
        btnAdd.addActionListener(e -> addRole());
        btnUpdate.addActionListener(e -> updateRole());
        btnDelete.addActionListener(e -> deleteRole());
        btnClear.addActionListener(e -> clearForm());
        btnInitDefaults.addActionListener(e -> initializeDefaults());
        
        buttonPanel.add(btnAdd);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnClear);
        buttonPanel.add(btnInitDefaults);
        
        formPanel.add(buttonPanel, gbc);
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
        
        JLabel lblTableTitle = new JLabel("Existing Roles");
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
        
        btnRefresh.addActionListener(e -> refreshTable());
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
        String[] columns = {"Role ID", "Role Name", "Description"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        roleTable = new JTable(tableModel);
        roleTable.setFont(EnterpriseTheme.FONT_TABLE_CELL);
        roleTable.setForeground(EnterpriseTheme.TEXT_PRIMARY);
        roleTable.setBackground(Color.WHITE);
        roleTable.setSelectionBackground(EnterpriseTheme.PRIMARY.brighter());
        roleTable.setSelectionForeground(Color.WHITE);
        roleTable.setGridColor(EnterpriseTheme.BORDER);
        roleTable.setShowGrid(true);
        roleTable.getTableHeader().setFont(EnterpriseTheme.FONT_TABLE_HEADER);
        roleTable.getTableHeader().setBackground(EnterpriseTheme.PRIMARY);
        roleTable.getTableHeader().setForeground(EnterpriseTheme.TEXT_ON_PRIMARY);
        
        EnterpriseTheme.setStandardTableRowHeight(roleTable);
        
        roleTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 1) {
                    int row = roleTable.getSelectedRow();
                    if (row >= 0) {
                        loadRoleToForm(row);
                    }
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(roleTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(EnterpriseTheme.BORDER));
        
        tablePanel.add(tableHeaderPanel, BorderLayout.NORTH);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        add(headerPanel, BorderLayout.NORTH);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(10, 10, 10, 10);
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.35;
        gbc.weighty = 1.0;
        contentPanel.add(formPanel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.65;
        contentPanel.add(tablePanel, gbc);
        
        add(contentPanel, BorderLayout.CENTER);
    }
    
    private void loadRoles() {
        try {
            List<RoleEntity> roles = roleService.getAllRoles();
            tableModel.setRowCount(0);
            
            for (RoleEntity role : roles) {
                Object[] row = {
                    role.getRoleId(),
                    role.getName(),
                    role.getDescription()
                };
                tableModel.addRow(row);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading roles: " + e.getMessage(), 
                "Load Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadRoleToForm(int row) {
        txtRoleId.setText(tableModel.getValueAt(row, 0).toString());
        txtName.setText(tableModel.getValueAt(row, 1).toString());
        
        Object description = tableModel.getValueAt(row, 2);
        txtDescription.setText(description != null ? description.toString() : "");
    }
    
    private void addRole() {
        try {
            if (!validateRoleInput()) {
                return;
            }
            
            RoleEntity role = new RoleEntity();
            role.setName(txtName.getText().trim().toUpperCase());
            role.setDescription(txtDescription.getText().trim());
            
            roleService.createRole(role);
            
            JOptionPane.showMessageDialog(this, 
                "Role added successfully!", 
                "Success", 
                JOptionPane.INFORMATION_MESSAGE);
            
            clearForm();
            refreshTable();
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, 
                e.getMessage(), 
                "Validation Error", 
                JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error adding role: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateRole() {
        try {
            if (txtRoleId.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Please select a role to update", 
                    "Validation Error", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            if (!validateRoleInput()) {
                return;
            }
            
            RoleEntity role = new RoleEntity();
            role.setRoleId(Integer.parseInt(txtRoleId.getText().trim()));
            role.setName(txtName.getText().trim().toUpperCase());
            role.setDescription(txtDescription.getText().trim());
            
            roleService.updateRole(role);
            
            JOptionPane.showMessageDialog(this, 
                "Role updated successfully!", 
                "Success", 
                JOptionPane.INFORMATION_MESSAGE);
            
            clearForm();
            refreshTable();
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, 
                e.getMessage(), 
                "Validation Error", 
                JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error updating role: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void deleteRole() {
        try {
            if (txtRoleId.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Please select a role to delete", 
                    "Validation Error", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to delete this role?\nNote: System roles (ADMIN, MANAGER, CASHIER) cannot be deleted.", 
                "Confirm Delete", 
                JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                Integer roleId = Integer.parseInt(txtRoleId.getText().trim());
                roleService.deleteRole(roleId);
                
                JOptionPane.showMessageDialog(this, 
                    "Role deleted successfully!", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
                
                clearForm();
                refreshTable();
            }
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, 
                e.getMessage(), 
                "Deletion Error", 
                JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error deleting role: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void clearForm() {
        txtRoleId.setText("");
        txtName.setText("");
        txtDescription.setText("");
    }
    
    private void refreshTable() {
        loadRoles();
        JOptionPane.showMessageDialog(this, 
            "Table refreshed!", 
            "Success", 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void initializeDefaults() {
        try {
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Initialize default roles (ADMIN, MANAGER, CASHIER)?\nExisting roles will not be affected.", 
                "Confirm Initialization", 
                JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                roleService.initializeDefaultRoles();
                
                JOptionPane.showMessageDialog(this, 
                    "Default roles initialized successfully!", 
                    "Success", 
                    JOptionPane.INFORMATION_MESSAGE);
                
                refreshTable();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error initializing defaults: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private boolean validateRoleInput() {
        // Check name
        if (txtName.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Role name is required", 
                "Validation Error", 
                JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        if (txtName.getText().trim().length() < 2) {
            JOptionPane.showMessageDialog(this, 
                "Role name must be at least 2 characters", 
                "Validation Error", 
                JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        if (txtName.getText().trim().length() > 50) {
            JOptionPane.showMessageDialog(this, 
                "Role name cannot exceed 50 characters", 
                "Validation Error", 
                JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        if (!txtName.getText().trim().matches("^[A-Za-z0-9_]+$")) {
            JOptionPane.showMessageDialog(this, 
                "Role name can only contain letters, numbers, and underscores", 
                "Validation Error", 
                JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        // Check description length
        if (txtDescription.getText().trim().length() > 255) {
            JOptionPane.showMessageDialog(this, 
                "Description cannot exceed 255 characters", 
                "Validation Error", 
                JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        return true;
    }
}
