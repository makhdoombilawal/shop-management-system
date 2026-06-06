package frames;

import models.Session;
import models.entity.SupplierEntity;
import service.SupplierService;
import util.EnterpriseTheme;
import util.LoggerUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Modern Enterprise Supplier Management Frame
 */
public class SupplierEnterprise extends BaseFrame {
    
    private final SupplierService supplierService = new SupplierService();
    
    // UI Components
    private JPanel headerPanel;
    private JPanel contentPanel;
    private JPanel formPanel;
    private JPanel tablePanel;
    
    private JTextField txtSupplierId;
    private JTextField txtCompanyName;
    private JTextField txtContactPerson;
    private JTextField txtPhone;
    private JTextField txtEmail;
    private JTextField txtAddress;
    private JTextField txtCity;
    private JTextField txtCountry;
    private JTextField txtTaxNumber;
    private JTextField txtPaymentTerms;
    private JTextField txtCreditLimit;
    private JTextArea txtRemarks;
    
    private JTable supplierTable;
    private DefaultTableModel tableModel;
    
    private JButton btnAdd;
    private JButton btnUpdate;
    private JButton btnDelete;
    private JButton btnClear;
    private JButton btnBack;
    private JButton btnRefresh;
    
    public SupplierEnterprise() throws Exception {
        super();
        if (!authorized) return;
        
        setTitle("Supplier Management - Shop Manager");
        setSize(1600, 900);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        initializeComponents();
        setupLayout();
        loadSuppliers();
        
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
        
        JLabel lblTitle = new JLabel("SUPPLIER MANAGEMENT");
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
        
        // Supplier ID (Hidden/Auto)
        addFormField("Supplier ID:", txtSupplierId = new JTextField(), gbc, row++);
        txtSupplierId.setEnabled(false);
        EnterpriseTheme.styleTextField(txtSupplierId);
        
        // Company Name
        addFormField("Company Name:*", txtCompanyName = new JTextField(20), gbc, row++);
        EnterpriseTheme.styleTextField(txtCompanyName);
        
        // Contact Person
        addFormField("Contact Person:", txtContactPerson = new JTextField(20), gbc, row++);
        EnterpriseTheme.styleTextField(txtContactPerson);
        
        // Phone
        addFormField("Phone:", txtPhone = new JTextField(20), gbc, row++);
        EnterpriseTheme.styleTextField(txtPhone);
        
        // Email
        addFormField("Email:", txtEmail = new JTextField(20), gbc, row++);
        EnterpriseTheme.styleTextField(txtEmail);
        
        // Address
        addFormField("Address:", txtAddress = new JTextField(20), gbc, row++);
        EnterpriseTheme.styleTextField(txtAddress);
        
        // City
        addFormField("City:", txtCity = new JTextField(20), gbc, row++);
        EnterpriseTheme.styleTextField(txtCity);
        
        // Country
        addFormField("Country:", txtCountry = new JTextField(20), gbc, row++);
        EnterpriseTheme.styleTextField(txtCountry);
        
        // Tax Number
        addFormField("Tax Number:", txtTaxNumber = new JTextField(20), gbc, row++);
        EnterpriseTheme.styleTextField(txtTaxNumber);
        
        // Payment Terms
        addFormField("Payment Terms:", txtPaymentTerms = new JTextField(20), gbc, row++);
        txtPaymentTerms.setToolTipText("e.g., Net 30, Net 60, COD");
        EnterpriseTheme.styleTextField(txtPaymentTerms);
        
        // Credit Limit
        addFormField("Credit Limit ($):", txtCreditLimit = new JTextField(20), gbc, row++);
        txtCreditLimit.setToolTipText("Maximum credit allowed for this supplier");
        EnterpriseTheme.styleTextField(txtCreditLimit);
        
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
        txtRemarks = new JTextArea(3, 20);
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
        
        btnAdd = new JButton("+ Add Supplier");
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
        
        btnAdd.addActionListener(e -> addSupplier());
        btnUpdate.addActionListener(e -> updateSupplier());
        btnDelete.addActionListener(e -> deleteSupplier());
        btnClear.addActionListener(e -> clearForm());
        btnRefresh.addActionListener(e -> loadSuppliers());
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
        
        JLabel lblTableTitle = new JLabel("Supplier List");
        lblTableTitle.setFont(EnterpriseTheme.FONT_HEADER);
        lblTableTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        String[] columns = {"ID", "Company Name", "Contact Person", "Phone", "Email", "City", "Balance", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        supplierTable = new JTable(tableModel);
        EnterpriseTheme.styleTable(supplierTable);
        
        // Selection listener
        supplierTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = supplierTable.getSelectedRow();
                if (selectedRow >= 0) {
                    populateFormFromTable(selectedRow);
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(supplierTable);
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
    
    private void loadSuppliers() {
        try {
            List<SupplierEntity> suppliers = supplierService.getAllSuppliers();
            tableModel.setRowCount(0);
            
            for (SupplierEntity supplier : suppliers) {
                tableModel.addRow(new Object[]{
                    supplier.getSupplierId(),
                    supplier.getCompanyName(),
                    supplier.getContactPerson(),
                    supplier.getPhoneNumber(),
                    supplier.getEmail(),
                    supplier.getCity(),
                    String.format("$%.2f", supplier.getCurrentBalance() != null ? supplier.getCurrentBalance() : 0.0),
                    supplier.getStatus()
                });
            }
        } catch (Exception e) {
            LoggerUtil.logError(SupplierEnterprise.class, "Error loading suppliers", e);
            EnterpriseTheme.showError(this, "Failed to load suppliers: " + e.getMessage());
        }
    }
    
    private void populateFormFromTable(int row) {
        try {
            int supplierId = Integer.parseInt(tableModel.getValueAt(row, 0).toString());
            SupplierEntity supplier = supplierService.getSupplierById(supplierId).orElse(null);
            
            if (supplier != null) {
                txtSupplierId.setText(String.valueOf(supplier.getSupplierId()));
                txtCompanyName.setText(supplier.getCompanyName());
                txtContactPerson.setText(supplier.getContactPerson() != null ? supplier.getContactPerson() : "");
                txtPhone.setText(supplier.getPhoneNumber() != null ? supplier.getPhoneNumber() : "");
                txtEmail.setText(supplier.getEmail() != null ? supplier.getEmail() : "");
                txtAddress.setText(supplier.getAddress() != null ? supplier.getAddress() : "");
                txtCity.setText(supplier.getCity() != null ? supplier.getCity() : "");
                txtCountry.setText(supplier.getCountry() != null ? supplier.getCountry() : "");
                txtTaxNumber.setText(supplier.getTaxNumber() != null ? supplier.getTaxNumber() : "");
                txtPaymentTerms.setText(supplier.getPaymentTerms() != null ? supplier.getPaymentTerms() : "");
                txtCreditLimit.setText(supplier.getCreditLimit() != null ? String.valueOf(supplier.getCreditLimit()) : "");
                txtRemarks.setText(supplier.getRemarks() != null ? supplier.getRemarks() : "");
            }
        } catch (Exception e) {
            LoggerUtil.logError(SupplierEnterprise.class, "Error loading supplier details", e);
        }
    }
    
    private void addSupplier() {
        try {
            // Validation
            if (!validateSupplierInput()) {
                return;
            }
            
            String companyName = txtCompanyName.getText().trim();
            String contactPerson = txtContactPerson.getText().trim();
            String phone = txtPhone.getText().trim();
            String email = txtEmail.getText().trim();
            String address = txtAddress.getText().trim() + 
                           (txtCity.getText().trim().isEmpty() ? "" : ", " + txtCity.getText().trim()) +
                           (txtCountry.getText().trim().isEmpty() ? "" : ", " + txtCountry.getText().trim());
            String paymentTerms = txtPaymentTerms.getText().trim();
            String remarks = txtRemarks.getText().trim();
            
            Double creditLimit = null;
            if (!txtCreditLimit.getText().trim().isEmpty()) {
                creditLimit = Double.parseDouble(txtCreditLimit.getText().trim());
            }
            
            SupplierEntity supplier = supplierService.addSupplier(
                companyName, contactPerson, phone, email, address, 
                paymentTerms, creditLimit, remarks
            );
            
            if (supplier != null) {
                EnterpriseTheme.showSuccess(this, "Supplier added successfully!");
                clearForm();
                loadSuppliers();
            }
        } catch (NumberFormatException e) {
            EnterpriseTheme.showError(this, "Invalid credit limit. Please enter a valid number.");
        } catch (Exception e) {
            LoggerUtil.logError(SupplierEnterprise.class, "Error adding supplier", e);
            EnterpriseTheme.showError(this, "Failed to add supplier: " + e.getMessage());
        }
    }
    
    private void updateSupplier() {
        try {
            if (txtSupplierId.getText().trim().isEmpty()) {
                EnterpriseTheme.showWarning(this, "Please select a supplier to update");
                return;
            }
            
            // Validation
            if (!validateSupplierInput()) {
                return;
            }
            
            int supplierId = Integer.parseInt(txtSupplierId.getText().trim());
            SupplierEntity supplier = supplierService.getSupplierById(supplierId).orElse(null);
            
            if (supplier != null) {
                supplier.setCompanyName(txtCompanyName.getText().trim());
                supplier.setContactPerson(txtContactPerson.getText().trim());
                supplier.setPhoneNumber(txtPhone.getText().trim());
                supplier.setEmail(txtEmail.getText().trim());
                supplier.setAddress(txtAddress.getText().trim());
                supplier.setCity(txtCity.getText().trim());
                supplier.setCountry(txtCountry.getText().trim());
                supplier.setTaxNumber(txtTaxNumber.getText().trim());
                supplier.setPaymentTerms(txtPaymentTerms.getText().trim());
                supplier.setRemarks(txtRemarks.getText().trim());
                
                if (!txtCreditLimit.getText().trim().isEmpty()) {
                    supplier.setCreditLimit(Double.parseDouble(txtCreditLimit.getText().trim()));
                }
                
                if (supplierService.updateSupplier(supplier)) {
                    EnterpriseTheme.showSuccess(this, "Supplier updated successfully!");
                    clearForm();
                    loadSuppliers();
                }
            }
        } catch (NumberFormatException e) {
            EnterpriseTheme.showError(this, "Invalid credit limit. Please enter a valid number.");
        } catch (Exception e) {
            LoggerUtil.logError(SupplierEnterprise.class, "Error updating supplier", e);
            EnterpriseTheme.showError(this, "Failed to update supplier: " + e.getMessage());
        }
    }
    
    private void deleteSupplier() {
        try {
            if (txtSupplierId.getText().trim().isEmpty()) {
                EnterpriseTheme.showWarning(this, "Please select a supplier to delete");
                return;
            }
            
            boolean confirm = EnterpriseTheme.showConfirm(this, 
                "Are you sure you want to deactivate this supplier?");
            
            if (confirm) {
                int supplierId = Integer.parseInt(txtSupplierId.getText().trim());
                if (supplierService.deleteSupplier(supplierId)) {
                    EnterpriseTheme.showSuccess(this, "Supplier deactivated successfully!");
                    clearForm();
                    loadSuppliers();
                }
            }
        } catch (Exception e) {
            LoggerUtil.logError(SupplierEnterprise.class, "Error deleting supplier", e);
            EnterpriseTheme.showError(this, "Failed to delete supplier: " + e.getMessage());
        }
    }
    
    private boolean validateSupplierInput() {
        String companyName = txtCompanyName.getText().trim();
        
        // Check required fields
        if (!util.ValidationUtil.isNotEmpty(companyName)) {
            EnterpriseTheme.showError(this, "Company name is required");
            txtCompanyName.requestFocus();
            return false;
        }
        
        // Validate email if provided
        String email = txtEmail.getText().trim();
        if (util.ValidationUtil.isNotEmpty(email) && !util.ValidationUtil.isValidEmail(email)) {
            EnterpriseTheme.showError(this, "Invalid email format");
            txtEmail.requestFocus();
            return false;
        }
        
        // Validate credit limit if provided
        String creditLimitStr = txtCreditLimit.getText().trim();
        if (util.ValidationUtil.isNotEmpty(creditLimitStr)) {
            try {
                double creditLimit = Double.parseDouble(creditLimitStr);
                if (creditLimit < 0) {
                    EnterpriseTheme.showError(this, "Credit limit cannot be negative");
                    txtCreditLimit.requestFocus();
                    return false;
                }
            } catch (NumberFormatException e) {
                EnterpriseTheme.showError(this, "Credit limit must be a valid number");
                txtCreditLimit.requestFocus();
                return false;
            }
        }
        
        return true;
    }
    
    private void clearForm() {
        txtSupplierId.setText("");
        txtCompanyName.setText("");
        txtContactPerson.setText("");
        txtPhone.setText("");
        txtEmail.setText("");
        txtAddress.setText("");
        txtCity.setText("");
        txtCountry.setText("");
        txtTaxNumber.setText("");
        txtPaymentTerms.setText("");
        txtCreditLimit.setText("");
        txtRemarks.setText("");
        supplierTable.clearSelection();
    }
    

    private void goBackToDashboard() {
        this.dispose();
        try {
            new DashboardEnterprise().setVisible(true);
        } catch (Exception e) {
            LoggerUtil.logError(SupplierEnterprise.class, "Error opening dashboard", e);
        }
    }
}
