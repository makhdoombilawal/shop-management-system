package frames;

import models.Session;
import models.entity.UserEntity;
import service.UserService;
import util.EnterpriseTheme;
import util.LoggerUtil;
import util.PasswordUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Modern Enterprise User Management Frame (Admin Only)
 */
public class UserEnterprise extends BaseFrame {
    
    private final UserService userService = new UserService();
    private final service.SettingsService settingsService = new service.SettingsService();
    
    // UI Components
    private JPanel headerPanel;
    private JPanel contentPanel;
    private JPanel formPanel;
    private JPanel tablePanel;
    
    private JTextField txtUserId;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JTextField txtFullName;
    private JTextField txtEmail;
    private JComboBox<String> cmbRole;
    private JCheckBox chkIsActive;
    
    private JTable userTable;
    private DefaultTableModel tableModel;
    
    private JButton btnAdd;
    private JButton btnUpdate;
    private JButton btnDelete;
    private JButton btnClear;
    private JButton btnBack;
    private JButton btnRefresh;
    private JButton btnResetPassword;
    
    public UserEnterprise() throws Exception {
        super();
        if (!authorized) return;
        
        // Only admins can access this
        if (!Session.isAdmin()) {
            EnterpriseTheme.showError(null, "Access Denied: Admin privileges required");
            dispose();
            return;
        }
        
        setTitle("User Management - Shop Manager");
        setSize(1700, 900);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        initializeComponents();
        setupLayout();
        loadUsers();
        showPaymentLifecycleAlertIfNeeded();
        
        EnterpriseTheme.applyGlobalTheme();
    }

    private void showPaymentLifecycleAlertIfNeeded() {
        try {
            if (Session.isSuperAdmin()) {
                return;
            }

            LocalDateTime dueDate = settingsService.getPaymentCycleDueDate();
            if (settingsService.isPaymentCycleDueOrExpired()) {
                EnterpriseTheme.showWarning(this,
                    "Payment lifecycle alert!\n\n" +
                    "Next payment was due on: " + dueDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "\n" +
                    "Please contact Super Admin for payment renewal.\n\n" +
                    "If there is any issue in application, contact Super Admin.");
            }
        } catch (Exception e) {
            LoggerUtil.logError(UserEnterprise.class, "Failed to check payment lifecycle alert", e);
        }
    }
    
    private void initializeComponents() {
        // Header
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(EnterpriseTheme.DANGER);
        headerPanel.setPreferredSize(new Dimension(0, 80));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        
        JLabel lblTitle = new JLabel("USER MANAGEMENT (Admin)");
        lblTitle.setFont(EnterpriseTheme.FONT_TITLE);
        lblTitle.setForeground(Color.WHITE);
        
        JLabel lblUser = new JLabel("Admin: " + Session.getUsername());
        lblUser.setFont(EnterpriseTheme.FONT_BODY);
        lblUser.setForeground(Color.WHITE);
        
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
        
        // User ID (Hidden/Auto)
        addFormField("User ID:", txtUserId = new JTextField(), gbc, row++);
        txtUserId.setEnabled(false);
        EnterpriseTheme.styleTextField(txtUserId);
        
        // Username
        addFormField("Username:", txtUsername = new JTextField(20), gbc, row++);
        EnterpriseTheme.styleTextField(txtUsername);
        
        // Password
        gbc.gridx = 0;
        gbc.gridy = row;
        JLabel lblPassword = new JLabel("Password:");
        lblPassword.setFont(EnterpriseTheme.FONT_BODY);
        formPanel.add(lblPassword, gbc);
        
        gbc.gridx = 1;
        txtPassword = new JPasswordField(20);
        EnterpriseTheme.styleTextField(txtPassword);
        formPanel.add(txtPassword, gbc);
        row++;
        
        // Full Name
        addFormField("Full Name:", txtFullName = new JTextField(20), gbc, row++);
        EnterpriseTheme.styleTextField(txtFullName);
        
        // Email
        addFormField("Email:", txtEmail = new JTextField(20), gbc, row++);
        EnterpriseTheme.styleTextField(txtEmail);
        
        // Role
        gbc.gridx = 0;
        gbc.gridy = row;
        JLabel lblRole = new JLabel("Role:");
        lblRole.setFont(EnterpriseTheme.FONT_BODY);
        formPanel.add(lblRole, gbc);
        
        gbc.gridx = 1;
        cmbRole = new JComboBox<>(new String[]{"ADMIN", "MANAGER", "CASHIER"});
        EnterpriseTheme.styleComboBox(cmbRole);
        // TEMPORARILY DISABLED - loadRoles(); // Load roles dynamically from database (after combo box is created)
        formPanel.add(cmbRole, gbc);
        row++;
        
        // Is Active Checkbox
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        chkIsActive = new JCheckBox("Active User");
        chkIsActive.setFont(EnterpriseTheme.FONT_BODY);
        chkIsActive.setSelected(true);
        chkIsActive.setBackground(EnterpriseTheme.CARD_BG);
        formPanel.add(chkIsActive, gbc);
        gbc.gridwidth = 1;
        row++;
        
        // Buttons Panel
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        btnPanel.setBackground(EnterpriseTheme.CARD_BG);
        
        btnAdd = new JButton("+ Add User");
        btnUpdate = new JButton("Update");
        btnDelete = new JButton("Delete");
        btnResetPassword = new JButton("Reset Password");
        btnClear = new JButton("Clear");
        btnRefresh = new JButton("Refresh");
        btnBack = new JButton("<< Back to Dashboard");
        
        EnterpriseTheme.stylePrimaryButton(btnAdd);
        EnterpriseTheme.styleSuccessButton(btnUpdate);
        EnterpriseTheme.styleDangerButton(btnDelete);
        EnterpriseTheme.styleWarningButton(btnResetPassword);
        EnterpriseTheme.styleSecondaryButton(btnClear);
        EnterpriseTheme.styleSecondaryButton(btnRefresh);
        EnterpriseTheme.styleSecondaryButton(btnBack);
        
        btnAdd.addActionListener(e -> addUser());
        btnUpdate.addActionListener(e -> updateUser());
        btnDelete.addActionListener(e -> deleteUser());
        btnResetPassword.addActionListener(e -> resetPassword());
        btnClear.addActionListener(e -> clearForm());
        btnRefresh.addActionListener(e -> loadUsers());
        btnBack.addActionListener(e -> goBackToDashboard());
        
        btnPanel.add(btnAdd);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnResetPassword);
        btnPanel.add(btnClear);
        btnPanel.add(btnRefresh);
        btnPanel.add(btnBack);
        
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(btnPanel, gbc);
    }
    
    private void addFormField(String label, JTextField field, GridBagConstraints gbc, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        
        JLabel lbl = new JLabel(label);
        lbl.setFont(EnterpriseTheme.FONT_BODY);
        formPanel.add(lbl, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(field, gbc);
    }
    
    private void createTablePanel() {
        tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(EnterpriseTheme.CARD_BG);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(EnterpriseTheme.BORDER, 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        JLabel lblTableTitle = new JLabel("User List");
        lblTableTitle.setFont(EnterpriseTheme.FONT_HEADER);
        lblTableTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        String[] columns = {"ID", "Username", "Full Name", "Email", "Role", "Active", "Last Login"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        userTable = new JTable(tableModel);
        EnterpriseTheme.styleTable(userTable);
        
        // Selection listener
        userTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = userTable.getSelectedRow();
                if (selectedRow >= 0) {
                    populateFormFromTable(selectedRow);
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(userTable);
        EnterpriseTheme.styleScrollPane(scrollPane);
        
        tablePanel.add(lblTableTitle, BorderLayout.NORTH);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        add(headerPanel, BorderLayout.NORTH);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.BOTH;
        
        // Form on left
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.35;
        gbc.weighty = 1.0;
        contentPanel.add(formPanel, gbc);
        
        // Table on right
        gbc.gridx = 1;
        gbc.weightx = 0.65;
        contentPanel.add(tablePanel, gbc);
        
        add(contentPanel, BorderLayout.CENTER);
    }
    
    private void loadUsers() {
        try {
            List<UserEntity> users = userService.getAllUsers();
            tableModel.setRowCount(0);
            
            for (UserEntity user : users) {
                tableModel.addRow(new Object[]{
                    user.getUserId(),
                    user.getUsername(),
                    user.getFullName(),
                    user.getEmail(),
                    user.getRole(),
                    user.getIsActive() ? "✓" : "✗",
                    user.getLastLogin() != null ? util.DateUtil.formatDateTime(user.getLastLogin()) : "Never"
                });
            }
        } catch (Exception e) {
            LoggerUtil.logError(UserEnterprise.class, "Error loading users", e);
            EnterpriseTheme.showError(this, "Failed to load users: " + e.getMessage());
        }
    }
    
    private void populateFormFromTable(int row) {
        Object value = tableModel.getValueAt(row, 0);
        txtUserId.setText(value != null ? value.toString() : "");
        
        value = tableModel.getValueAt(row, 1);
        txtUsername.setText(value != null ? value.toString() : "");
        
        txtPassword.setText(""); // Don't populate password
        
        value = tableModel.getValueAt(row, 2);
        txtFullName.setText(value != null ? value.toString() : "");
        
        value = tableModel.getValueAt(row, 3);
        txtEmail.setText(value != null ? value.toString() : "");
        
        value = tableModel.getValueAt(row, 4);
        if (value != null) {
            cmbRole.setSelectedItem(value.toString());
        }
        
        value = tableModel.getValueAt(row, 5);
        chkIsActive.setSelected(value != null && value.toString().equals("✓"));
    }
    
    private void addUser() {
        try {
            String password = new String(txtPassword.getPassword());
            if (password.isEmpty()) {
                EnterpriseTheme.showWarning(this, "Password is required for new users");
                return;
            }
            
            UserEntity user = new UserEntity();
            user.setUsername(txtUsername.getText().trim());
            user.setPassword(password); // Will be hashed in registerUser
            user.setFullName(txtFullName.getText().trim());
            user.setEmail(txtEmail.getText().trim());
            user.setRole((String) cmbRole.getSelectedItem());
            user.setIsActive(chkIsActive.isSelected());
            
            if (userService.registerUser(user)) {
                EnterpriseTheme.showSuccess(this, "User added successfully!");
                clearForm();
                loadUsers();
            }
        } catch (Exception e) {
            LoggerUtil.logError(UserEnterprise.class, "Error adding user", e);
            EnterpriseTheme.showError(this, "Failed to add user: " + e.getMessage());
        }
    }
    
    private void updateUser() {
        try {
            if (txtUserId.getText().trim().isEmpty()) {
                EnterpriseTheme.showWarning(this, "Please select a user to update");
                return;
            }
            
            int userId = Integer.parseInt(txtUserId.getText().trim());
            UserEntity user = userService.getUserById(userId).orElse(null);
            
            if (user != null) {
                user.setUsername(txtUsername.getText().trim());
                user.setFullName(txtFullName.getText().trim());
                user.setEmail(txtEmail.getText().trim());
                user.setRole((String) cmbRole.getSelectedItem());
                user.setIsActive(chkIsActive.isSelected());
                
                // Only update password if provided
                String password = new String(txtPassword.getPassword());
                if (!password.isEmpty()) {
                    user.setPassword(PasswordUtil.hashPassword(password));
                }
                
                if (userService.updateUser(user)) {
                    EnterpriseTheme.showSuccess(this, "User updated successfully!");
                    clearForm();
                    loadUsers();
                }
            }
        } catch (Exception e) {
            LoggerUtil.logError(UserEnterprise.class, "Error updating user", e);
            EnterpriseTheme.showError(this, "Failed to update user: " + e.getMessage());
        }
    }
    
    private void deleteUser() {
        try {
            if (txtUserId.getText().trim().isEmpty()) {
                EnterpriseTheme.showWarning(this, "Please select a user to delete");
                return;
            }
            
            // Prevent deleting current user
            int userId = Integer.parseInt(txtUserId.getText().trim());
            if (userId == Session.getUserId()) {
                EnterpriseTheme.showWarning(this, "Cannot delete your own account!");
                return;
            }
            
            boolean confirm = EnterpriseTheme.showConfirm(this, 
                "Are you sure you want to deactivate this user?");
            
            if (confirm) {
                if (userService.deactivateUser(userId)) {
                    EnterpriseTheme.showSuccess(this, "User deactivated successfully!");
                    clearForm();
                    loadUsers();
                }
            }
        } catch (Exception e) {
            LoggerUtil.logError(UserEnterprise.class, "Error deactivating user", e);
            EnterpriseTheme.showError(this, "Failed to deactivate user: " + e.getMessage());
        }
    }
    
    private void resetPassword() {
        try {
            if (txtUserId.getText().trim().isEmpty()) {
                EnterpriseTheme.showWarning(this, "Please select a user");
                return;
            }
            
            String newPassword = JOptionPane.showInputDialog(this, 
                "Enter new password:", 
                "Reset Password", 
                JOptionPane.PLAIN_MESSAGE);
            
            if (newPassword != null && !newPassword.trim().isEmpty()) {
                int userId = Integer.parseInt(txtUserId.getText().trim());
                UserEntity user = userService.getUserById(userId).orElse(null);
                
                if (user != null) {
                    user.setPassword(PasswordUtil.hashPassword(newPassword));
                    if (userService.updateUser(user)) {
                        EnterpriseTheme.showSuccess(this, "Password reset successfully!");
                    }
                }
            }
        } catch (Exception e) {
            LoggerUtil.logError(UserEnterprise.class, "Error resetting password", e);
            EnterpriseTheme.showError(this, "Failed to reset password: " + e.getMessage());
        }
    }
    
    private void clearForm() {
        txtUserId.setText("");
        txtUsername.setText("");
        txtPassword.setText("");
        txtFullName.setText("");
        txtEmail.setText("");
        cmbRole.setSelectedIndex(0);
        chkIsActive.setSelected(true);
        userTable.clearSelection();
    }
    
    private void goBackToDashboard() {
        try {
            new DashboardEnterprise().setVisible(true);
            dispose();
        } catch (Exception e) {
            LoggerUtil.logError(UserEnterprise.class, "Error returning to dashboard", e);
            EnterpriseTheme.showError(this, "Failed to open dashboard: " + e.getMessage());
        }
    }
}
