package frames;

import models.Session;
import models.entity.CustomerEntity;
import service.CustomerService;
import util.EnterpriseTheme;
import util.LoggerUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Modern Enterprise Customer Management Frame
 */
public class CustomerEnterprise extends BaseFrame {
    
    private final CustomerService customerService = new CustomerService();
    
    // UI Components
    private JPanel headerPanel;
    private JPanel contentPanel;
    private JPanel formPanel;
    private JPanel tablePanel;
    
    private JTextField txtCustomerId;
    private JTextField txtName;
    private JTextField txtPhone;
    private JTextField txtEmail;
    private JTextField txtAddress;
    private JTextArea txtRemarks;
    
    private JTable customerTable;
    private DefaultTableModel tableModel;
    
    private JButton btnAdd;
    private JButton btnUpdate;
    private JButton btnDelete;
    private JButton btnClear;
    private JButton btnBack;
    private JButton btnRefresh;
    
    public CustomerEnterprise() throws Exception {
        super();
        if (!authorized) return;
        
        setTitle("Customer Management - Shop Manager");
        setSize(1600, 900);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        initializeComponents();
        setupLayout();
        loadCustomers();
        
        EnterpriseTheme.applyGlobalTheme();
    }
    
    private void initializeComponents() {
        // Header
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(EnterpriseTheme.PRIMARY);
        headerPanel.setPreferredSize(new Dimension(0, 80));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(
            EnterpriseTheme.PADDING_LARGE,
            EnterpriseTheme.PADDING_LARGE,
            EnterpriseTheme.PADDING_LARGE,
            EnterpriseTheme.PADDING_LARGE
        ));
        
        JLabel lblTitle = new JLabel("CUSTOMER MANAGEMENT");
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
        contentPanel.setBorder(BorderFactory.createEmptyBorder(
            EnterpriseTheme.PADDING_LARGE,
            EnterpriseTheme.PADDING_LARGE,
            EnterpriseTheme.PADDING_LARGE,
            EnterpriseTheme.PADDING_LARGE
        ));
        
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
        
        // Customer ID (Hidden/Auto)
        addFormField("Customer ID:", txtCustomerId = new JTextField(), gbc, row++);
        txtCustomerId.setEnabled(false);
        EnterpriseTheme.styleTextField(txtCustomerId);
        
        // Name
        addFormField("Name:", txtName = new JTextField(20), gbc, row++);
        EnterpriseTheme.styleTextField(txtName);
        
        // Phone
        addFormField("Phone:", txtPhone = new JTextField(20), gbc, row++);
        EnterpriseTheme.styleTextField(txtPhone);
        
        // Email
        addFormField("Email:", txtEmail = new JTextField(20), gbc, row++);
        EnterpriseTheme.styleTextField(txtEmail);
        
        // Address
        addFormField("Address:", txtAddress = new JTextField(20), gbc, row++);
        EnterpriseTheme.styleTextField(txtAddress);
        
        // Remarks
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        JLabel lblRemarks = new JLabel("Remarks:");
        lblRemarks.setFont(EnterpriseTheme.FONT_BODY);
        formPanel.add(lblRemarks, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        txtRemarks = new JTextArea(4, 20);
        txtRemarks.setFont(EnterpriseTheme.FONT_BODY);
        txtRemarks.setLineWrap(true);
        txtRemarks.setWrapStyleWord(true);
        JScrollPane scrollRemarks = new JScrollPane(txtRemarks);
        EnterpriseTheme.styleScrollPane(scrollRemarks);
        formPanel.add(scrollRemarks, gbc);
        row++;
        
        // Buttons Panel
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        btnPanel.setBackground(EnterpriseTheme.CARD_BG);
        
        btnAdd = new JButton("+ Add Customer");
        btnUpdate = new JButton("Update");
        btnDelete = new JButton("Delete");
        btnClear = new JButton("Clear");
        btnRefresh = new JButton("Refresh");
        btnBack = new JButton("<< Back to Dashboard");
        
        EnterpriseTheme.stylePrimaryButton(btnAdd);
        EnterpriseTheme.styleSuccessButton(btnUpdate);
        EnterpriseTheme.styleDangerButton(btnDelete);
        EnterpriseTheme.styleSecondaryButton(btnClear);
        EnterpriseTheme.styleSecondaryButton(btnRefresh);
        EnterpriseTheme.styleSecondaryButton(btnBack);
        
        btnAdd.addActionListener(e -> addCustomer());
        btnUpdate.addActionListener(e -> updateCustomer());
        btnDelete.addActionListener(e -> deleteCustomer());
        btnClear.addActionListener(e -> clearForm());
        btnRefresh.addActionListener(e -> loadCustomers());
        btnBack.addActionListener(e -> goBackToDashboard());
        
        btnPanel.add(btnAdd);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
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
        
        JLabel lblTableTitle = new JLabel("Customer List");
        lblTableTitle.setFont(EnterpriseTheme.FONT_HEADER);
        lblTableTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        String[] columns = {"ID", "Name", "Phone", "Email", "Address", "Total Purchases", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        customerTable = new JTable(tableModel);
        EnterpriseTheme.styleTable(customerTable);
        
        // Selection listener
        customerTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = customerTable.getSelectedRow();
                if (selectedRow >= 0) {
                    populateFormFromTable(selectedRow);
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(customerTable);
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
    
    private void loadCustomers() {
        try {
            List<CustomerEntity> customers = customerService.getAllCustomers();
            tableModel.setRowCount(0);
            
            for (CustomerEntity customer : customers) {
                tableModel.addRow(new Object[]{
                    customer.getCustomerId(),
                    customer.getName(),
                    customer.getPhoneNumber(),
                    customer.getEmail(),
                    customer.getAddress(),
                    String.format("$%.2f", customer.getTotalPurchases() != null ? customer.getTotalPurchases() : 0.0),
                    customer.getStatus()
                });
            }
        } catch (Exception e) {
            LoggerUtil.logError(CustomerEnterprise.class, "Error loading customers", e);
            EnterpriseTheme.showError(this, "Failed to load customers: " + e.getMessage());
        }
    }
    
    private void populateFormFromTable(int row) {
        txtCustomerId.setText(tableModel.getValueAt(row, 0).toString());
        txtName.setText(tableModel.getValueAt(row, 1).toString());
        txtPhone.setText(tableModel.getValueAt(row, 2).toString());
        txtEmail.setText(tableModel.getValueAt(row, 3).toString());
        txtAddress.setText(tableModel.getValueAt(row, 4) != null ? tableModel.getValueAt(row, 4).toString() : "");
        txtRemarks.setText(""); // Remarks not shown in table
    }
    
    private void addCustomer() {
        try {
            // Validation
            if (!validateCustomerInput()) {
                return;
            }
            
            CustomerEntity customer = new CustomerEntity();
            customer.setName(txtName.getText().trim());
            customer.setPhoneNumber(txtPhone.getText().trim());
            customer.setEmail(txtEmail.getText().trim());
            customer.setAddress(txtAddress.getText().trim());
            customer.setRemarks(txtRemarks.getText().trim());
            customer.setStatus("active");
            
            if (customerService.addCustomer(customer)) {
                EnterpriseTheme.showSuccess(this, "Customer added successfully!");
                clearForm();
                refreshTable();
            }
        } catch (Exception e) {
            LoggerUtil.logError(CustomerEnterprise.class, "Error adding customer", e);
            EnterpriseTheme.showError(this, "Failed to add customer: " + e.getMessage());
        }
    }
    
    private void updateCustomer() {
        try {
            if (txtCustomerId.getText().trim().isEmpty()) {
                EnterpriseTheme.showWarning(this, "Please select a customer to update");
                return;
            }
            
            // Validation
            if (!validateCustomerInput()) {
                return;
            }
            
            int customerId = Integer.parseInt(txtCustomerId.getText().trim());
            CustomerEntity customer = customerService.getCustomerById(customerId).orElse(null);
            
            if (customer != null) {
                customer.setName(txtName.getText().trim());
                customer.setPhoneNumber(txtPhone.getText().trim());
                customer.setEmail(txtEmail.getText().trim());
                customer.setAddress(txtAddress.getText().trim());
                customer.setRemarks(txtRemarks.getText().trim());
                
                if (customerService.updateCustomer(customer)) {
                    EnterpriseTheme.showSuccess(this, "Customer updated successfully!");
                    clearForm();
                    refreshTable();
                }
            }
        } catch (Exception e) {
            LoggerUtil.logError(CustomerEnterprise.class, "Error updating customer", e);
            EnterpriseTheme.showError(this, "Failed to update customer: " + e.getMessage());
        }
    }
    
    private void deleteCustomer() {
        try {
            if (txtCustomerId.getText().trim().isEmpty()) {
                EnterpriseTheme.showWarning(this, "Please select a customer to delete");
                return;
            }
            
            boolean confirm = EnterpriseTheme.showConfirm(this, 
                "Are you sure you want to delete this customer?");
            
            if (confirm) {
                int customerId = Integer.parseInt(txtCustomerId.getText().trim());
                if (customerService.deleteCustomer(customerId)) {
                    EnterpriseTheme.showSuccess(this, "Customer deleted successfully!");
                    clearForm();
                    refreshTable();
                }
            }
        } catch (Exception e) {
            LoggerUtil.logError(CustomerEnterprise.class, "Error deleting customer", e);
            EnterpriseTheme.showError(this, "Failed to delete customer: " + e.getMessage());
        }
    }
    
    private boolean validateCustomerInput() {
        String name = txtName.getText().trim();
        String phone = txtPhone.getText().trim();
        String email = txtEmail.getText().trim();
        
        // Check required fields
        if (!util.ValidationUtil.isNotEmpty(name)) {
            EnterpriseTheme.showError(this, "Customer name is required");
            txtName.requestFocus();
            return false;
        }
        
        // Validate name length
        if (!util.ValidationUtil.isValidLength(name, 2, 100)) {
            EnterpriseTheme.showError(this, "Customer name must be between 2 and 100 characters");
            txtName.requestFocus();
            return false;
        }
        
        // Validate phone number
        if (!util.ValidationUtil.isNotEmpty(phone)) {
            EnterpriseTheme.showError(this, "Phone number is required");
            txtPhone.requestFocus();
            return false;
        }
        
        if (!util.ValidationUtil.isValidPhone(phone)) {
            EnterpriseTheme.showError(this, "Invalid phone number format. Use 10-15 digits.");
            txtPhone.requestFocus();
            return false;
        }
        
        // Validate email (optional but must be valid if provided)
        if (util.ValidationUtil.isNotEmpty(email) && !util.ValidationUtil.isValidEmail(email)) {
            EnterpriseTheme.showError(this, "Invalid email format");
            txtEmail.requestFocus();
            return false;
        }
        
        return true;
    }
    
    private void refreshTable() {
        loadCustomers();
    }
    
    private void clearForm() {
        txtCustomerId.setText("");
        txtName.setText("");
        txtPhone.setText("");
        txtEmail.setText("");
        txtAddress.setText("");
        txtRemarks.setText("");
        customerTable.clearSelection();
    }
    
    private void goBackToDashboard() {
        try {
            new DashboardEnterprise().setVisible(true);
            dispose();
        } catch (Exception e) {
            LoggerUtil.logError(CustomerEnterprise.class, "Error returning to dashboard", e);
            EnterpriseTheme.showError(this, "Failed to open dashboard: " + e.getMessage());
        }
    }
}
