package frames;

import models.Session;
import models.entity.ProductEntity;
import models.entity.SupplierEntity;
import models.entity.TransactionEntity;
import service.ProductService;
import service.SupplierService;
import service.TransactionService;
import util.EnterpriseTheme;
import util.LoggerUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Purchase Order Management Frame
 * Allows purchasing products from suppliers with stock updates
 */
public class PurchaseOrderEnterprise extends BaseFrame {
    
    // Services
    private final ProductService productService = new ProductService();
    private final SupplierService supplierService = new SupplierService();
    private final TransactionService transactionService = new TransactionService();
    
    // UI Components - Left Panel
    private JPanel headerPanel;
    private JComboBox<String> cmbSupplier;
    private JComboBox<String> cmbProduct;
    private JTextField txtQuantity;
    private JTextField txtPurchasePrice;
    private JTextArea txtRemarks;
    private JButton btnAddToOrder;
    private JButton btnClearForm;
    
    // UI Components - Right Panel
    private DefaultTableModel orderTableModel;
    private JTable orderTable;
    private JLabel lblTotalItems;
    private JLabel lblTotalAmount;
    private JButton btnCompletePurchase;
    private JButton btnClearOrder;
    private JButton btnBack;
    
    // Data
    private List<SupplierEntity> suppliers;
    private List<ProductEntity> products;
    private List<PurchaseOrderItem> orderItems = new ArrayList<>();
    
    public PurchaseOrderEnterprise() throws Exception {
        super();
        if (!authorized) return;
        
        setTitle("Purchase Orders - Shop Manager");
        setSize(1600, 900);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        initializeComponents();
        loadSuppliers();
        loadProducts();
        
        EnterpriseTheme.applyGlobalTheme();
    }
    
    private void initializeComponents() {
        setLayout(new BorderLayout());
        
        // Header
        createHeader();
        
        // Main content (split pane)
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(700);
        splitPane.setBackground(EnterpriseTheme.BACKGROUND);
        
        // Left panel - Product selection
        splitPane.setLeftComponent(createProductSelectionPanel());
        
        // Right panel - Order summary
        splitPane.setRightComponent(createOrderSummaryPanel());
        
        add(splitPane, BorderLayout.CENTER);
    }
    
    private void createHeader() {
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(EnterpriseTheme.PRIMARY);
        headerPanel.setPreferredSize(new Dimension(0, 80));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(
            EnterpriseTheme.PADDING_LARGE,
            EnterpriseTheme.PADDING_LARGE,
            EnterpriseTheme.PADDING_LARGE,
            EnterpriseTheme.PADDING_LARGE
        ));
        
        JLabel lblTitle = new JLabel("PURCHASE ORDERS");
        lblTitle.setFont(EnterpriseTheme.FONT_TITLE);
        lblTitle.setForeground(EnterpriseTheme.TEXT_ON_PRIMARY);
        
        JLabel lblUser = new JLabel("User: " + Session.getUsername() + " | Role: " + Session.getRole());
        lblUser.setFont(EnterpriseTheme.FONT_BODY);
        lblUser.setForeground(EnterpriseTheme.TEXT_ON_PRIMARY);
        
        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(lblUser, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);
    }
    
    private JPanel createProductSelectionPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(EnterpriseTheme.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 10));
        
        // Title
        JLabel lblPanelTitle = new JLabel("Add Products to Purchase Order");
        lblPanelTitle.setFont(EnterpriseTheme.FONT_HEADER);
        lblPanelTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(EnterpriseTheme.CARD_BG);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(EnterpriseTheme.BORDER, 1),
            BorderFactory.createEmptyBorder(30, 30, 30, 30)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        
        int row = 0;
        
        // Supplier Selection (REQUIRED)
        gbc.gridx = 0; gbc.gridy = row;
        JLabel lblSupplier = new JLabel("Supplier:*");
        lblSupplier.setFont(EnterpriseTheme.FONT_BODY_BOLD);
        formPanel.add(lblSupplier, gbc);
        
        gbc.gridx = 1;
        cmbSupplier = new JComboBox<>();
        cmbSupplier.setFont(EnterpriseTheme.FONT_BODY);
        EnterpriseTheme.styleComboBox(cmbSupplier);
        formPanel.add(cmbSupplier, gbc);
        row++;
        
        // Product Selection
        gbc.gridx = 0; gbc.gridy = row;
        JLabel lblProduct = new JLabel("Product:*");
        lblProduct.setFont(EnterpriseTheme.FONT_BODY_BOLD);
        formPanel.add(lblProduct, gbc);
        
        gbc.gridx = 1;
        cmbProduct = new JComboBox<>();
        cmbProduct.setFont(EnterpriseTheme.FONT_BODY);
        cmbProduct.addActionListener(e -> onProductSelected());
        EnterpriseTheme.styleComboBox(cmbProduct);
        formPanel.add(cmbProduct, gbc);
        row++;
        
        // Quantity
        gbc.gridx = 0; gbc.gridy = row;
        JLabel lblQuantity = new JLabel("Quantity:*");
        lblQuantity.setFont(EnterpriseTheme.FONT_BODY_BOLD);
        formPanel.add(lblQuantity, gbc);
        
        gbc.gridx = 1;
        txtQuantity = new JTextField();
        txtQuantity.setFont(EnterpriseTheme.FONT_BODY);
        EnterpriseTheme.styleTextField(txtQuantity);
        formPanel.add(txtQuantity, gbc);
        row++;
        
        // Purchase Price
        gbc.gridx = 0; gbc.gridy = row;
        JLabel lblPrice = new JLabel("Purchase Price:*");
        lblPrice.setFont(EnterpriseTheme.FONT_BODY_BOLD);
        formPanel.add(lblPrice, gbc);
        
        gbc.gridx = 1;
        txtPurchasePrice = new JTextField();
        txtPurchasePrice.setFont(EnterpriseTheme.FONT_BODY);
        EnterpriseTheme.styleTextField(txtPurchasePrice);
        formPanel.add(txtPurchasePrice, gbc);
        row++;
        
        // Remarks
        gbc.gridx = 0; gbc.gridy = row;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        JLabel lblRemarks = new JLabel("Remarks:");
        lblRemarks.setFont(EnterpriseTheme.FONT_BODY_BOLD);
        formPanel.add(lblRemarks, gbc);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        txtRemarks = new JTextArea(4, 20);
        txtRemarks.setFont(EnterpriseTheme.FONT_BODY);
        txtRemarks.setLineWrap(true);
        txtRemarks.setWrapStyleWord(true);
        JScrollPane scrollRemarks = new JScrollPane(txtRemarks);
        EnterpriseTheme.styleScrollPane(scrollRemarks);
        formPanel.add(scrollRemarks, gbc);
        row++;
        
        // Buttons
        gbc.gridx = 0; gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        btnPanel.setBackground(EnterpriseTheme.CARD_BG);
        
        btnAddToOrder = new JButton("Add to Order");
        btnClearForm = new JButton("Clear Form");
        
        EnterpriseTheme.stylePrimaryButton(btnAddToOrder);
        EnterpriseTheme.styleSecondaryButton(btnClearForm);
        
        btnAddToOrder.addActionListener(e -> addProductToOrder());
        btnClearForm.addActionListener(e -> clearForm());
        
        btnPanel.add(btnAddToOrder);
        btnPanel.add(btnClearForm);
        
        formPanel.add(btnPanel, gbc);
        
        // Add to main panel
        panel.add(lblPanelTitle, BorderLayout.NORTH);
        panel.add(formPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createOrderSummaryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(EnterpriseTheme.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 20));
        
        // Title
        JLabel lblPanelTitle = new JLabel("Purchase Order Summary");
        lblPanelTitle.setFont(EnterpriseTheme.FONT_HEADER);
        lblPanelTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        // Table
        String[] columns = {"Product", "Quantity", "Unit Price", "Total", "Current Stock"};
        orderTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        orderTable = new JTable(orderTableModel);
        EnterpriseTheme.styleTable(orderTable);
        
        JScrollPane scrollPane = new JScrollPane(orderTable);
        EnterpriseTheme.styleScrollPane(scrollPane);
        
        // Summary panel
        JPanel summaryPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        summaryPanel.setBackground(EnterpriseTheme.CARD_BG);
        summaryPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(EnterpriseTheme.BORDER, 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        lblTotalItems = new JLabel("Total Items: 0");
        lblTotalItems.setFont(EnterpriseTheme.FONT_BODY_BOLD);
        
        lblTotalAmount = new JLabel("Total Amount: Rs. 0.00");
        lblTotalAmount.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTotalAmount.setForeground(EnterpriseTheme.SUCCESS);
        
        summaryPanel.add(lblTotalItems);
        summaryPanel.add(lblTotalAmount);
        
        // Action buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        actionPanel.setBackground(EnterpriseTheme.BACKGROUND);
        
        btnCompletePurchase = new JButton("Complete Purchase Order");
        btnClearOrder = new JButton("Clear Order");
        btnBack = new JButton("<< Back to Dashboard");
        
        EnterpriseTheme.styleSuccessButton(btnCompletePurchase);
        EnterpriseTheme.styleDangerButton(btnClearOrder);
        EnterpriseTheme.styleSecondaryButton(btnBack);
        
        btnCompletePurchase.addActionListener(e -> completePurchaseOrder());
        btnClearOrder.addActionListener(e -> clearOrder());
        btnBack.addActionListener(e -> goBackToDashboard());
        
        actionPanel.add(btnClearOrder);
        actionPanel.add(btnCompletePurchase);
        actionPanel.add(btnBack);
        
        // Layout
        JPanel centerPanel = new JPanel(new BorderLayout(0, 15));
        centerPanel.setBackground(EnterpriseTheme.BACKGROUND);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        centerPanel.add(summaryPanel, BorderLayout.SOUTH);
        
        panel.add(lblPanelTitle, BorderLayout.NORTH);
        panel.add(centerPanel, BorderLayout.CENTER);
        panel.add(actionPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void loadSuppliers() {
        try {
            suppliers = supplierService.getActiveSuppliers();
            cmbSupplier.removeAllItems();
            cmbSupplier.addItem("-- Select Supplier --");
            
            for (SupplierEntity supplier : suppliers) {
                cmbSupplier.addItem(supplier.getCompanyName());
            }
        } catch (Exception e) {
            LoggerUtil.logError(PurchaseOrderEnterprise.class, "Error loading suppliers", e);
            EnterpriseTheme.showError(this, "Error loading suppliers: " + e.getMessage());
        }
    }
    
    private void loadProducts() {
        try {
            products = productService.getActiveProducts();
            cmbProduct.removeAllItems();
            cmbProduct.addItem("-- Select Product --");
            
            for (ProductEntity product : products) {
                cmbProduct.addItem(product.getName());
            }
        } catch (Exception e) {
            LoggerUtil.logError(PurchaseOrderEnterprise.class, "Error loading products", e);
            EnterpriseTheme.showError(this, "Error loading products: " + e.getMessage());
        }
    }
    
    private void onProductSelected() {
        int selectedIndex = cmbProduct.getSelectedIndex();
        if (selectedIndex > 0) {
            ProductEntity product = products.get(selectedIndex - 1);
            // Set purchase price from product
            txtPurchasePrice.setText(String.valueOf(product.getPurchasePrice()));
        }
    }
    
    private void addProductToOrder() {
        try {
            // Validate supplier
            if (cmbSupplier.getSelectedIndex() <= 0) {
                EnterpriseTheme.showError(this, "Please select a supplier!");
                cmbSupplier.requestFocus();
                return;
            }
            
            // Validate product
            if (cmbProduct.getSelectedIndex() <= 0) {
                EnterpriseTheme.showError(this, "Please select a product!");
                cmbProduct.requestFocus();
                return;
            }
            
            // Validate quantity
            String quantityStr = txtQuantity.getText().trim();
            if (quantityStr.isEmpty()) {
                EnterpriseTheme.showError(this, "Please enter quantity!");
                txtQuantity.requestFocus();
                return;
            }
            
            int quantity;
            try {
                quantity = Integer.parseInt(quantityStr);
                if (quantity <= 0) {
                    EnterpriseTheme.showError(this, "Quantity must be greater than 0!");
                    txtQuantity.requestFocus();
                    return;
                }
            } catch (NumberFormatException e) {
                EnterpriseTheme.showError(this, "Invalid quantity format!");
                txtQuantity.requestFocus();
                return;
            }
            
            // Validate price
            String priceStr = txtPurchasePrice.getText().trim();
            if (priceStr.isEmpty()) {
                EnterpriseTheme.showError(this, "Please enter purchase price!");
                txtPurchasePrice.requestFocus();
                return;
            }
            
            double price;
            try {
                price = Double.parseDouble(priceStr);
                if (price <= 0) {
                    EnterpriseTheme.showError(this, "Price must be greater than 0!");
                    txtPurchasePrice.requestFocus();
                    return;
                }
            } catch (NumberFormatException e) {
                EnterpriseTheme.showError(this, "Invalid price format!");
                txtPurchasePrice.requestFocus();
                return;
            }
            
            // Get selected product
            ProductEntity product = products.get(cmbProduct.getSelectedIndex() - 1);
            
            // Check if product already in order
            for (PurchaseOrderItem item : orderItems) {
                if (item.product.getProductId().equals(product.getProductId())) {
                    EnterpriseTheme.showError(this, 
                        "Product already in order! Remove it first to change quantity.");
                    return;
                }
            }
            
            // Add to order
            PurchaseOrderItem item = new PurchaseOrderItem();
            item.product = product;
            item.quantity = quantity;
            item.purchasePrice = price;
            item.total = quantity * price;
            
            orderItems.add(item);
            
            // Update table
            orderTableModel.addRow(new Object[]{
                product.getName(),
                quantity,
                String.format("Rs. %.2f", price),
                String.format("Rs. %.2f", item.total),
                product.getStock()
            });
            
            updateSummary();
            clearForm();
            
            EnterpriseTheme.showSuccess(this, "Product added to purchase order!");
            
        } catch (Exception e) {
            LoggerUtil.logError(PurchaseOrderEnterprise.class, "Error adding product to order", e);
            EnterpriseTheme.showError(this, "Error: " + e.getMessage());
        }
    }
    
    private void completePurchaseOrder() {
        try {
            // Validate supplier selected
            if (cmbSupplier.getSelectedIndex() <= 0) {
                EnterpriseTheme.showError(this, "Please select a supplier!");
                return;
            }
            
            // Validate order has items
            if (orderItems.isEmpty()) {
                EnterpriseTheme.showError(this, "Please add products to the order!");
                return;
            }
            
            // Confirm
            boolean confirm = EnterpriseTheme.showConfirm(this,
                String.format("Complete purchase order?\n\nTotal Items: %d\nTotal Amount: Rs. %.2f",
                    orderItems.size(), calculateTotalAmount()));
            
            if (!confirm) {
                return;
            }
            
            // Get supplier
            SupplierEntity supplier = suppliers.get(cmbSupplier.getSelectedIndex() - 1);
            String remarks = txtRemarks.getText().trim();
            
            // Process each purchase
            int successCount = 0;
            for (PurchaseOrderItem item : orderItems) {
                TransactionEntity transaction = transactionService.processPurchase(
                    item.product.getProductId(),
                    supplier.getSupplierId(),
                    item.quantity,
                    item.purchasePrice,
                    remarks
                );
                
                if (transaction != null) {
                    successCount++;
                }
            }
            
            if (successCount == orderItems.size()) {
                EnterpriseTheme.showSuccess(this,
                    String.format("Purchase order completed successfully!\n\n" +
                        "Supplier: %s\n" +
                        "Products: %d\n" +
                        "Total Amount: Rs. %.2f\n\n" +
                        "Stock levels have been updated.",
                        supplier.getCompanyName(),
                        successCount,
                        calculateTotalAmount()));
                
                clearOrder();
                loadProducts(); // Reload to update stock display
            } else {
                EnterpriseTheme.showWarning(this,
                    String.format("Partial completion: %d of %d items processed",
                        successCount, orderItems.size()));
            }
            
        } catch (Exception e) {
            LoggerUtil.logError(PurchaseOrderEnterprise.class, "Error completing purchase order", e);
            EnterpriseTheme.showError(this, "Error completing purchase: " + e.getMessage());
        }
    }
    
    private void clearOrder() {
        orderItems.clear();
        orderTableModel.setRowCount(0);
        updateSummary();
        clearForm();
    }
    
    private void clearForm() {
        cmbProduct.setSelectedIndex(0);
        txtQuantity.setText("");
        txtPurchasePrice.setText("");
        txtRemarks.setText("");
    }
    
    private void updateSummary() {
        int totalItems = orderItems.size();
        double totalAmount = calculateTotalAmount();
        
        lblTotalItems.setText("Total Items: " + totalItems);
        lblTotalAmount.setText(String.format("Total Amount: Rs. %.2f", totalAmount));
    }
    
    private double calculateTotalAmount() {
        double total = 0.0;
        for (PurchaseOrderItem item : orderItems) {
            total += item.total;
        }
        return total;
    }
    
    private void goBackToDashboard() {
        if (!orderItems.isEmpty()) {
            boolean confirm = EnterpriseTheme.showConfirm(this,
                "You have items in the order. Discard and go back?");
            if (!confirm) {
                return;
            }
        }
        
        this.dispose();
        try {
            new DashboardEnterprise().setVisible(true);
        } catch (Exception e) {
            LoggerUtil.logError(PurchaseOrderEnterprise.class, "Error opening dashboard", e);
        }
    }
    
    // Inner class for order items
    private static class PurchaseOrderItem {
        ProductEntity product;
        int quantity;
        double purchasePrice;
        double total;
    }
}
