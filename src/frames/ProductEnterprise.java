package frames;

import models.Session;
import models.entity.ProductEntity;
import models.entity.CategoryEntity;
import service.ProductService;
import service.CategoryService;
import util.EnterpriseTheme;
import util.LoggerUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Modern Enterprise Product Management Frame
 */
public class ProductEnterprise extends BaseFrame {
    
    private final ProductService productService = new ProductService();
    private final CategoryService categoryService = new CategoryService();
    
    // UI Components
    private JPanel headerPanel;
    private JPanel contentPanel;
    private JPanel formPanel;
    private JPanel tablePanel;
    
    private JTextField txtProductId;
    private JTextField txtName;
    private JComboBox<String> cmbCategory;
    private JTextField txtStock;
    private JTextField txtSellPrice;
    private JTextField txtPurchasePrice;
    private JComboBox<String> cmbStatus;
    private JTextArea txtRemarks;
    
    private JTable productTable;
    private DefaultTableModel tableModel;
    
    private JButton btnAdd;
    private JButton btnUpdate;
    private JButton btnDelete;
    private JButton btnClear;
    private JButton btnBack;
    private JButton btnRefresh;
    private JButton btnManageCategories;
    
    public ProductEnterprise() throws Exception {
        super();
        if (!authorized) return;
        
        setTitle("Product Management - Shop Manager");
        setSize(1700, 900);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        initializeComponents();
        setupLayout();
        loadProducts();
        
        EnterpriseTheme.applyGlobalTheme();
    }
    
    private void initializeComponents() {
        // Header
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(EnterpriseTheme.PRIMARY);
        headerPanel.setPreferredSize(new Dimension(0, 80));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        
        JLabel lblTitle = new JLabel("PRODUCT MANAGEMENT");
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
        
        // Product ID (Hidden/Auto)
        addFormField("Product ID:", txtProductId = new JTextField(), gbc, row++);
        txtProductId.setEnabled(false);
        EnterpriseTheme.styleTextField(txtProductId);
        
        // Name
        addFormField("Product Name:", txtName = new JTextField(20), gbc, row++);
        EnterpriseTheme.styleTextField(txtName);
        
        // Category (replacing Product Type with dropdown)
        gbc.gridx = 0; gbc.gridy = row;
        JLabel lblCategory = new JLabel("Category:");
        lblCategory.setFont(EnterpriseTheme.FONT_BODY);
        lblCategory.setForeground(EnterpriseTheme.TEXT_PRIMARY);
        formPanel.add(lblCategory, gbc);
        
        gbc.gridx = 1;
        JPanel categoryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        categoryPanel.setBackground(EnterpriseTheme.CARD_BG);
        
        cmbCategory = new JComboBox<>();
        cmbCategory.setFont(EnterpriseTheme.FONT_INPUT);
        cmbCategory.setPreferredSize(new Dimension(200, 35));
        loadCategories();
        categoryPanel.add(cmbCategory);
        
        JButton btnAddCategory = new JButton("+ Add");
        btnAddCategory.setPreferredSize(new Dimension(80, 35));
        EnterpriseTheme.styleSuccessButton(btnAddCategory);
        btnAddCategory.addActionListener(e -> addNewCategory());
        categoryPanel.add(btnAddCategory);
        
        formPanel.add(categoryPanel, gbc);
        row++;
        
        // Stock
        addFormField("Stock Quantity:", txtStock = new JTextField(20), gbc, row++);
        EnterpriseTheme.styleTextField(txtStock);
        
        // Purchase Price
        addFormField("Purchase Price:", txtPurchasePrice = new JTextField(20), gbc, row++);
        EnterpriseTheme.styleTextField(txtPurchasePrice);
        
        // Sell Price
        addFormField("Sell Price:", txtSellPrice = new JTextField(20), gbc, row++);
        EnterpriseTheme.styleTextField(txtSellPrice);
        
        // Status
        gbc.gridx = 0;
        gbc.gridy = row;
        JLabel lblStatus = new JLabel("Status:");
        lblStatus.setFont(EnterpriseTheme.FONT_BODY);
        formPanel.add(lblStatus, gbc);
        
        gbc.gridx = 1;
        cmbStatus = new JComboBox<>(new String[]{"active", "discontinued"});
        EnterpriseTheme.styleComboBox(cmbStatus);
        formPanel.add(cmbStatus, gbc);
        row++;
        
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
        
        btnAdd = new JButton("+ Add Product");
        btnUpdate = new JButton("Update");
        btnDelete = new JButton("Delete");
        btnClear = new JButton("Clear");
        btnRefresh = new JButton("Refresh");
        btnBack = new JButton("<< Back to Dashboard");
        btnManageCategories = new JButton("Manage Categories");
        
        EnterpriseTheme.stylePrimaryButton(btnAdd);
        EnterpriseTheme.styleSuccessButton(btnUpdate);
        EnterpriseTheme.styleDangerButton(btnDelete);
        EnterpriseTheme.styleSecondaryButton(btnClear);
        EnterpriseTheme.styleSecondaryButton(btnRefresh);
        EnterpriseTheme.styleSecondaryButton(btnBack);
        EnterpriseTheme.styleWarningButton(btnManageCategories);
        
        btnAdd.addActionListener(e -> addProduct());
        btnUpdate.addActionListener(e -> updateProduct());
        btnDelete.addActionListener(e -> deleteProduct());
        btnClear.addActionListener(e -> clearForm());
        btnRefresh.addActionListener(e -> loadProducts());
        btnBack.addActionListener(e -> goBackToDashboard());
        btnManageCategories.addActionListener(e -> openCategoryManagement());
        
        btnPanel.add(btnAdd);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnClear);
        btnPanel.add(btnRefresh);
        btnPanel.add(btnManageCategories);
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
        
        JLabel lblTableTitle = new JLabel("Product List");
        lblTableTitle.setFont(EnterpriseTheme.FONT_HEADER);
        lblTableTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        String[] columns = {"ID", "Name", "Type", "Stock", "Purchase Price", "Sell Price", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        productTable = new JTable(tableModel);
        EnterpriseTheme.styleTable(productTable);
        
        // Selection listener
        productTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = productTable.getSelectedRow();
                if (selectedRow >= 0) {
                    populateFormFromTable(selectedRow);
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(productTable);
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
    
    private void loadProducts() {
        try {
            List<ProductEntity> products = productService.getAllProducts();
            tableModel.setRowCount(0);
            
            for (ProductEntity product : products) {
                // TEMPORARILY DISABLED - CategoryEntity integration
                String categoryName = (product.getProductType() != null ? product.getProductType() : "N/A");
                
                tableModel.addRow(new Object[]{
                    product.getProductId(),
                    product.getName(),
                    categoryName,
                    product.getStock(),
                    String.format("$%.2f", product.getPurchasePrice()),
                    String.format("$%.2f", product.getSellPrice()),
                    product.getStatus()
                });
            }
        } catch (Exception e) {
            LoggerUtil.logError(ProductEnterprise.class, "Error loading products", e);
            EnterpriseTheme.showError(this, "Failed to load products: " + e.getMessage());
        }
    }
    
    private void loadCategories() {
        try {
            cmbCategory.removeAllItems();
            cmbCategory.addItem("-- Select Category --");
            
            List<CategoryEntity> categories = categoryService.getAllCategories();
            for (CategoryEntity category : categories) {
                if (category.getIsActive()) {
                    cmbCategory.addItem(category.getCategoryId() + " - " + category.getName());
                }
            }
        } catch (Exception e) {
            LoggerUtil.logError(ProductEnterprise.class, "Error loading categories", e);
            // Add default categories if database load fails
            cmbCategory.addItem("Electronics");
            cmbCategory.addItem("Groceries");
            cmbCategory.addItem("Other");
        }
    }
    
    private void populateFormFromTable(int row) {
        txtProductId.setText(tableModel.getValueAt(row, 0).toString());
        txtName.setText(tableModel.getValueAt(row, 1).toString());
        cmbCategory.setSelectedItem(tableModel.getValueAt(row, 2).toString());
        txtStock.setText(tableModel.getValueAt(row, 3).toString());
        
        // Remove $ and parse
        String purchasePrice = tableModel.getValueAt(row, 4).toString().replace("$", "");
        String sellPrice = tableModel.getValueAt(row, 5).toString().replace("$", "");
        txtPurchasePrice.setText(purchasePrice);
        txtSellPrice.setText(sellPrice);
        
        cmbStatus.setSelectedItem(tableModel.getValueAt(row, 6).toString());
        txtRemarks.setText(""); // Remarks not shown in table
    }
    
    private void addProduct() {
        try {
            // Validation
            if (!validateProductInput()) {
                return;
            }
            
            ProductEntity product = new ProductEntity();
            product.setName(txtName.getText().trim());
            
            // Set category from dropdown
            String selectedCategory = (String) cmbCategory.getSelectedItem();
            if (selectedCategory != null && !selectedCategory.contains("Select Category")) {
                Integer categoryId = extractCategoryId(selectedCategory);
                if (categoryId != null) {
                    product.setCategoryId(categoryId);
                }
                product.setProductType(selectedCategory); // Set legacy field for display
            }
            
            product.setStock(Integer.parseInt(txtStock.getText().trim()));
            product.setPurchasePrice(Double.parseDouble(txtPurchasePrice.getText().trim()));
            product.setSellPrice(Double.parseDouble(txtSellPrice.getText().trim()));
            product.setStatus((String) cmbStatus.getSelectedItem());
            product.setRemarks(txtRemarks.getText().trim());
            
            if (productService.addProduct(product)) {
                EnterpriseTheme.showSuccess(this, "Product added successfully!");
                clearForm();
                refreshTable();
            }
        } catch (NumberFormatException e) {
            EnterpriseTheme.showError(this, "Invalid number format for stock or price");
        } catch (Exception e) {
            LoggerUtil.logError(ProductEnterprise.class, "Error adding product", e);
            EnterpriseTheme.showError(this, "Failed to add product: " + e.getMessage());
        }
    }
    
    private void updateProduct() {
        try {
            if (txtProductId.getText().trim().isEmpty()) {
                EnterpriseTheme.showWarning(this, "Please select a product to update");
                return;
            }
            
            // Validation
            if (!validateProductInput()) {
                return;
            }
            
            int productId = Integer.parseInt(txtProductId.getText().trim());
            ProductEntity product = productService.getProductById(productId).orElse(null);
            
            if (product != null) {
                product.setName(txtName.getText().trim());
                
                // Set category from dropdown
                String selectedCategory = (String) cmbCategory.getSelectedItem();
                if (selectedCategory != null && !selectedCategory.contains("Select Category")) {
                    Integer categoryId = extractCategoryId(selectedCategory);
                    if (categoryId != null) {
                        product.setCategoryId(categoryId);
                    }
                    product.setProductType(selectedCategory); // Set legacy field for display
                }
                
                product.setStock(Integer.parseInt(txtStock.getText().trim()));
                product.setPurchasePrice(Double.parseDouble(txtPurchasePrice.getText().trim()));
                product.setSellPrice(Double.parseDouble(txtSellPrice.getText().trim()));
                product.setStatus((String) cmbStatus.getSelectedItem());
                product.setRemarks(txtRemarks.getText().trim());
                
                if (productService.updateProductBoolean(product)) {
                    EnterpriseTheme.showSuccess(this, "Product updated successfully!");
                    clearForm();
                    refreshTable();
                }
            }
        } catch (NumberFormatException e) {
            EnterpriseTheme.showError(this, "Invalid number format for stock or price");
        } catch (Exception e) {
            LoggerUtil.logError(ProductEnterprise.class, "Error updating product", e);
            EnterpriseTheme.showError(this, "Failed to update product: " + e.getMessage());
        }
    }
    
    private void deleteProduct() {
        try {
            if (txtProductId.getText().trim().isEmpty()) {
                EnterpriseTheme.showWarning(this, "Please select a product to delete");
                return;
            }
            
            boolean confirm = EnterpriseTheme.showConfirm(this, 
                "Are you sure you want to delete this product?");
            
            if (confirm) {
                int productId = Integer.parseInt(txtProductId.getText().trim());
                if (productService.deleteProduct(productId)) {
                    EnterpriseTheme.showSuccess(this, "Product deleted successfully!");
                    clearForm();
                    refreshTable();
                }
            }
        } catch (Exception e) {
            LoggerUtil.logError(ProductEnterprise.class, "Error deleting product", e);
            EnterpriseTheme.showError(this, "Failed to delete product: " + e.getMessage());
        }
    }
    
    private boolean validateProductInput() {
        String name = txtName.getText().trim();
        String selectedCategory = (String) cmbCategory.getSelectedItem();
        String stock = txtStock.getText().trim();
        String purchasePrice = txtPurchasePrice.getText().trim();
        String sellPrice = txtSellPrice.getText().trim();
        
        // Check required fields
        if (!util.ValidationUtil.isNotEmpty(name)) {
            EnterpriseTheme.showError(this, "Product name is required");
            txtName.requestFocus();
            return false;
        }
        
        if (selectedCategory == null || selectedCategory.contains("Select Category")) {
            EnterpriseTheme.showError(this, "Please select a category");
            cmbCategory.requestFocus();
            return false;
        }
        
        // Validate stock
        try {
            int stockValue = Integer.parseInt(stock);
            if (!util.ValidationUtil.isValidStock(stockValue)) {
                EnterpriseTheme.showError(this, "Stock must be between 0 and 1,000,000");
                txtStock.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            EnterpriseTheme.showError(this, "Stock must be a valid number");
            txtStock.requestFocus();
            return false;
        }
        
        // Validate purchase price
        try {
            double pPrice = Double.parseDouble(purchasePrice);
            if (!util.ValidationUtil.isValidPrice(pPrice)) {
                EnterpriseTheme.showError(this, "Purchase price must be between 0 and 1,000,000");
                txtPurchasePrice.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            EnterpriseTheme.showError(this, "Purchase price must be a valid number");
            txtPurchasePrice.requestFocus();
            return false;
        }
        
        // Validate sell price
        try {
            double sPrice = Double.parseDouble(sellPrice);
            if (!util.ValidationUtil.isValidPrice(sPrice)) {
                EnterpriseTheme.showError(this, "Sell price must be between 0 and 1,000,000");
                txtSellPrice.requestFocus();
                return false;
            }
            
            double pPrice = Double.parseDouble(purchasePrice);
            if (sPrice < pPrice) {
                EnterpriseTheme.showWarning(this, 
                    "Sell price is lower than purchase price. This will result in a loss.");
            }
        } catch (NumberFormatException e) {
            EnterpriseTheme.showError(this, "Sell price must be a valid number");
            txtSellPrice.requestFocus();
            return false;
        }
        
        return true;
    }
    
    private void refreshTable() {
        loadProducts();
    }
    
    private void clearForm() {
        txtProductId.setText("");
        txtName.setText("");
        cmbCategory.setSelectedIndex(0); // Reset to "-- Select Category --"
        txtStock.setText("");
        txtPurchasePrice.setText("");
        txtSellPrice.setText("");
        cmbStatus.setSelectedIndex(0);
        txtRemarks.setText("");
        productTable.clearSelection();
    }
    
    private void goBackToDashboard() {
        try {
            new DashboardEnterprise().setVisible(true);
            dispose();
        } catch (Exception e) {
            LoggerUtil.logError(ProductEnterprise.class, "Error returning to dashboard", e);
            EnterpriseTheme.showError(this, "Failed to open dashboard: " + e.getMessage());
        }
    }
    
    private void openCategoryManagement() {
        // TEMPORARILY DISABLED - CategoryEntity mapping issues
        JOptionPane.showMessageDialog(this, 
            "Category Management is temporarily unavailable.\n\n" +
            "You can still add categories using the '+ Add' button next to the category dropdown.\n\n" +
            "For now, categories will be managed in-memory only.",
            "Category Management",
            JOptionPane.INFORMATION_MESSAGE);
        
        /*
        try {
            new CategoryManagementEnterprise().setVisible(true);
        } catch (Exception e) {
            LoggerUtil.logError(ProductEnterprise.class, "Error opening Category Management", e);
            EnterpriseTheme.showError(this, "Failed to open Category Management: " + e.getMessage());
        }
        */
    }
    
    /**
     * Add a new category on-the-fly
     */
    private void addNewCategory() {
        String newCategoryName = JOptionPane.showInputDialog(this, 
            "Enter new category name:", 
            "Add Category", 
            JOptionPane.PLAIN_MESSAGE);
        
        if (newCategoryName != null && !newCategoryName.trim().isEmpty()) {
            newCategoryName = newCategoryName.trim();
            
            try {
                // Check if category already exists
                List<CategoryEntity> categories = categoryService.getAllCategories();
                for (CategoryEntity cat : categories) {
                    if (cat.getName().equalsIgnoreCase(newCategoryName)) {
                        EnterpriseTheme.showWarning(this, "Category '" + newCategoryName + "' already exists!");
                        return;
                    }
                }
                
                // Create and save new category
                CategoryEntity newCategory = new CategoryEntity(newCategoryName, "User-created category");
                categoryService.createCategory(newCategory);
                EnterpriseTheme.showSuccess(this, "Category '" + newCategoryName + "' added successfully!");
                loadCategories(); // Reload dropdown
                // Select the newly added category
                for (int i = 0; i < cmbCategory.getItemCount(); i++) {
                    if (cmbCategory.getItemAt(i).contains(newCategoryName)) {
                        cmbCategory.setSelectedIndex(i);
                        break;
                    }
                }
            } catch (Exception e) {
                LoggerUtil.logError(ProductEnterprise.class, "Error adding category", e);
                EnterpriseTheme.showError(this, "Error adding category: " + e.getMessage());
            }
        }
    }
    
    /**
     * Extract category ID from dropdown text (format: "ID - Name")
     */
    private Integer extractCategoryId(String selectedText) {
        if (selectedText != null && selectedText.contains(" - ")) {
            try {
                return Integer.parseInt(selectedText.split(" - ")[0].trim());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
