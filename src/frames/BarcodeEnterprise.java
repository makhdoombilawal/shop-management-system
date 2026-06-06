package frames;

import models.Session;
import models.entity.BarcodeEntity;
import models.entity.ProductEntity;
import service.BarcodeService;
import service.ProductService;
import util.EnterpriseTheme;
import util.LoggerUtil;
import util.ValidationUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Modern Enterprise Barcode Management Frame
 * Full CRUD functionality for barcode management
 */
public class BarcodeEnterprise extends BaseFrame {
    
    private final BarcodeService barcodeService = new BarcodeService();
    private final ProductService productService = new ProductService();
    
    // UI Components
    private JPanel headerPanel;
    private JPanel contentPanel;
    private JPanel formPanel;
    private JPanel tablePanel;
    private JPanel productOverviewPanel; // New panel for products without barcodes
    
    private JTextField txtBarcodeId;
    private JTextField txtBarcodeNumber;
    private JComboBox<String> cmbProduct;
    private JComboBox<String> cmbStatus;
    private JTextField txtScanInput;
    private JTextArea txtRemarks;
    
    private JTable barcodeTable;
    private DefaultTableModel tableModel;
    
    private JTable productOverviewTable; // New table for product overview
    private DefaultTableModel productOverviewModel; // New table model
    
    private JButton btnAdd;
    private JButton btnUpdate;
    private JButton btnDelete;
    private JButton btnClear;
    private JButton btnRefresh;
    private JButton btnGenerate;
    private JButton btnScan;
    private JButton btnMarkSold;
    private JButton btnPrint;
    private JButton btnBack;
    
    public BarcodeEnterprise() throws Exception {
        super();
        if (!authorized) return;
        
        setTitle("Barcode Management - Shop Manager");
        setSize(1700, 900);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        initializeComponents();
        setupLayout();
        loadBarcodes();
        loadProducts();
        loadProductOverview(); // Load products with barcode status
        
        EnterpriseTheme.applyGlobalTheme();
    }
    
    private void initializeComponents() {
        // Header
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(EnterpriseTheme.PRIMARY);
        headerPanel.setPreferredSize(new Dimension(0, 80));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        
        JLabel lblTitle = new JLabel("BARCODE MANAGEMENT");
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
        createProductOverviewPanel(); // Add product overview panel
    }
    
    private void createFormPanel() {
        formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(EnterpriseTheme.CARD_BG);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(EnterpriseTheme.BORDER, 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6); // Reduced spacing
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // Barcode ID (Hidden/Auto)
        addFormField("Barcode ID:", txtBarcodeId = new JTextField(), gbc, row++);
        txtBarcodeId.setEnabled(false);
        EnterpriseTheme.styleTextField(txtBarcodeId);

        // Barcode Number
        addFormField("Barcode Number:", txtBarcodeNumber = new JTextField(20), gbc, row++);
        EnterpriseTheme.styleTextField(txtBarcodeNumber);
        txtBarcodeNumber.setToolTipText("Enter 8-13 digit barcode number");

        // Product
        gbc.gridx = 0;
        gbc.gridy = row;
        JLabel lblProduct = new JLabel("Product:");
        lblProduct.setFont(EnterpriseTheme.FONT_BODY);
        formPanel.add(lblProduct, gbc);

        gbc.gridx = 1;
        cmbProduct = new JComboBox<>();
        EnterpriseTheme.styleComboBox(cmbProduct);
        formPanel.add(cmbProduct, gbc);
        row++;

        // Status
        gbc.gridx = 0;
        gbc.gridy = row;
        JLabel lblStatus = new JLabel("Status:");
        lblStatus.setFont(EnterpriseTheme.FONT_BODY);
        formPanel.add(lblStatus, gbc);

        gbc.gridx = 1;
        cmbStatus = new JComboBox<>(new String[]{"available", "sold", "damaged"});
        EnterpriseTheme.styleComboBox(cmbStatus);
        formPanel.add(cmbStatus, gbc);
        row++;

        // Scan Input
        addFormField("Scan Barcode:", txtScanInput = new JTextField(20), gbc, row++);
        EnterpriseTheme.styleTextField(txtScanInput);
        txtScanInput.setToolTipText("Scan or type barcode to search");

        // Remarks (FIXED LAYOUT)
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        JLabel lblRemarks = new JLabel("Remarks:");
        lblRemarks.setFont(EnterpriseTheme.FONT_BODY);
        formPanel.add(lblRemarks, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 0.3; // Reduced from 0.5 to 0.3
        gbc.fill = GridBagConstraints.BOTH;
        txtRemarks = new JTextArea(2, 20); // Reduced from 3 to 2 rows
        txtRemarks.setFont(EnterpriseTheme.FONT_BODY);
        txtRemarks.setLineWrap(true);
        txtRemarks.setWrapStyleWord(true);
        txtRemarks.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        JScrollPane scrollRemarks = new JScrollPane(txtRemarks);
        scrollRemarks.setPreferredSize(new Dimension(200, 50)); // FIXED: Set explicit size
        EnterpriseTheme.styleScrollPane(scrollRemarks);
        formPanel.add(scrollRemarks, gbc);
        row++;

        // Buttons Panel (More compact)
        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 6, 6, 6);

        JPanel btnPanel = new JPanel(new GridLayout(3, 3, 5, 5)); // Changed to 3x3 grid
        btnPanel.setBackground(EnterpriseTheme.CARD_BG);

        btnAdd = new JButton("Add");
        btnUpdate = new JButton("Update");
        btnDelete = new JButton("Delete");
        btnClear = new JButton("Clear");
        btnRefresh = new JButton("Refresh");
        btnGenerate = new JButton("Generate");
        btnScan = new JButton("Scan");
        btnMarkSold = new JButton("Mark Sold");
        btnPrint = new JButton("Print");

        // Add bulk cleanup button
        JButton btnCleanup = new JButton("🗑️ Cleanup");
        btnCleanup.setToolTipText("Remove duplicate barcodes for selected product");
        EnterpriseTheme.styleWarningButton(btnCleanup);
        btnCleanup.addActionListener(e -> cleanupDuplicateBarcodes());

        EnterpriseTheme.stylePrimaryButton(btnAdd);
        EnterpriseTheme.styleSuccessButton(btnUpdate);
        EnterpriseTheme.styleDangerButton(btnDelete);
        EnterpriseTheme.styleSecondaryButton(btnClear);
        EnterpriseTheme.styleSecondaryButton(btnRefresh);
        EnterpriseTheme.stylePrimaryButton(btnGenerate);
        EnterpriseTheme.styleSuccessButton(btnScan);
        EnterpriseTheme.styleWarningButton(btnMarkSold);
        EnterpriseTheme.stylePrimaryButton(btnPrint);

        btnAdd.addActionListener(e -> addBarcode());
        btnUpdate.addActionListener(e -> updateBarcode());
        btnDelete.addActionListener(e -> deleteBarcode());
        btnClear.addActionListener(e -> clearForm());
        btnRefresh.addActionListener(e -> { loadBarcodes(); loadProductOverview(); });
        btnGenerate.addActionListener(e -> generateBarcodes());
        btnScan.addActionListener(e -> scanBarcode());
        btnMarkSold.addActionListener(e -> markBarcodeAsSold());
        btnPrint.addActionListener(e -> printBarcode());

        btnPanel.add(btnAdd);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnClear);
        btnPanel.add(btnGenerate);
        btnPanel.add(btnScan);
        btnPanel.add(btnMarkSold);
        btnPanel.add(btnPrint);
        btnPanel.add(btnCleanup);

        formPanel.add(btnPanel, gbc);

        // Back button separate
        gbc.gridy = row;
        gbc.insets = new Insets(8, 6, 6, 6);
        JPanel backPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        backPanel.setBackground(EnterpriseTheme.CARD_BG);

        btnBack = new JButton("Back to Dashboard");
        EnterpriseTheme.styleSecondaryButton(btnBack);
        btnBack.addActionListener(e -> goBackToDashboard());
        backPanel.add(btnBack);

        formPanel.add(backPanel, gbc);
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
        
        JLabel lblTableTitle = new JLabel("Barcode List");
        lblTableTitle.setFont(EnterpriseTheme.FONT_HEADER);
        lblTableTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        String[] columns = {"ID", "Barcode Number", "Product", "Status", "Created Date"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        barcodeTable = new JTable(tableModel);
        EnterpriseTheme.styleTable(barcodeTable);
        
        // Selection listener
        barcodeTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = barcodeTable.getSelectedRow();
                if (selectedRow >= 0) {
                    populateFormFromTable(selectedRow);
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(barcodeTable);
        EnterpriseTheme.styleScrollPane(scrollPane);
        
        tablePanel.add(lblTableTitle, BorderLayout.NORTH);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
    }
    
    /**
     * Create Product Overview Panel - Shows all products with their barcode status
     */
    private void createProductOverviewPanel() {
        productOverviewPanel = new JPanel(new BorderLayout());
        productOverviewPanel.setBackground(EnterpriseTheme.CARD_BG);
        productOverviewPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(EnterpriseTheme.BORDER, 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        // Header with title and generate button
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(EnterpriseTheme.CARD_BG);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        JLabel lblTableTitle = new JLabel("Products & Barcode Overview");
        lblTableTitle.setFont(EnterpriseTheme.FONT_HEADER);
        headerPanel.add(lblTableTitle, BorderLayout.WEST);
        
        JButton btnQuickGenerate = new JButton("Generate for Selected Product");
        btnQuickGenerate.setPreferredSize(new Dimension(220, 35));
        EnterpriseTheme.styleSuccessButton(btnQuickGenerate);
        btnQuickGenerate.addActionListener(e -> quickGenerateBarcode());
        headerPanel.add(btnQuickGenerate, BorderLayout.EAST);
        
        // Table columns: Product ID, Name, Type, Stock, Barcode Status, Barcode Numbers
        String[] columns = {"Product ID", "Product Name", "Type", "Stock", "Has Barcode?", "Barcode Number(s)"};
        productOverviewModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        productOverviewTable = new JTable(productOverviewModel);
        EnterpriseTheme.styleTable(productOverviewTable);
        
        // Make "Barcode Number(s)" column wider
        productOverviewTable.getColumnModel().getColumn(5).setPreferredWidth(300);
        
        JScrollPane scrollPane = new JScrollPane(productOverviewTable);
        EnterpriseTheme.styleScrollPane(scrollPane);
        
        productOverviewPanel.add(headerPanel, BorderLayout.NORTH);
        productOverviewPanel.add(scrollPane, BorderLayout.CENTER);
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout());
        add(headerPanel, BorderLayout.NORTH);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.BOTH;
        
        // Form on top left
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.35;
        gbc.weighty = 0.5;
        contentPanel.add(formPanel, gbc);
        
        // Barcode table on top right
        gbc.gridx = 1;
        gbc.weightx = 0.65;
        contentPanel.add(tablePanel, gbc);
        
        // Product overview spanning bottom
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 0.5;
        contentPanel.add(productOverviewPanel, gbc);
        
        add(contentPanel, BorderLayout.CENTER);
    }
    
    private void loadProducts() {
        try {
            List<ProductEntity> products = productService.getAllProducts();
            cmbProduct.removeAllItems();
            cmbProduct.addItem("-- Select Product --");
            
            for (ProductEntity product : products) {
                cmbProduct.addItem(product.getProductId() + " - " + product.getName());
            }
        } catch (Exception e) {
            LoggerUtil.logError(BarcodeEnterprise.class, "Error loading products", e);
        }
    }
    
    private void loadBarcodes() {
        try {
            List<BarcodeEntity> barcodes = barcodeService.getAllBarcodes();
            tableModel.setRowCount(0);
            
            for (BarcodeEntity barcode : barcodes) {
                String productName = barcode.getProduct() != null ? 
                    barcode.getProduct().getName() : "N/A";
                
                tableModel.addRow(new Object[]{
                    barcode.getBarcodeId(),
                    barcode.getBarcodeNumber(),
                    productName,
                    barcode.getStatus(),
                    barcode.getCreatedAt() != null ? 
                        barcode.getCreatedAt().toString() : "N/A"
                });
            }
        } catch (Exception e) {
            LoggerUtil.logError(BarcodeEnterprise.class, "Error loading barcodes", e);
            EnterpriseTheme.showError(this, "Failed to load barcodes: " + e.getMessage());
        }
    }
    
    /**
     * Load product overview showing which products have barcodes and which don't
     */
    private void loadProductOverview() {
        try {
            List<ProductEntity> products = productService.getAllProducts();
            List<BarcodeEntity> allBarcodes = barcodeService.getAllBarcodes();
            
            productOverviewModel.setRowCount(0);
            
            for (ProductEntity product : products) {
                Integer productId = product.getProductId();
                String productName = product.getName();
                String productType = product.getProductType() != null ? product.getProductType() : "N/A";
                Integer stock = product.getStock();
                
                // Find all barcodes for this product
                List<String> barcodesForProduct = new ArrayList<>();
                for (BarcodeEntity barcode : allBarcodes) {
                    if (barcode.getProduct() != null && 
                        barcode.getProduct().getProductId().equals(productId)) {
                        barcodesForProduct.add(barcode.getBarcodeNumber());
                    }
                }
                
                String hasBarcodes = barcodesForProduct.isEmpty() ? "❌ NO" : "✅ YES (" + barcodesForProduct.size() + ")";
                String barcodeNumbers = barcodesForProduct.isEmpty() ? 
                    "⚠️ No barcodes generated" : 
                    String.join(", ", barcodesForProduct);
                
                productOverviewModel.addRow(new Object[]{
                    productId,
                    productName,
                    productType,
                    stock,
                    hasBarcodes,
                    barcodeNumbers
                });
            }
            
            // Highlight rows without barcodes
            productOverviewTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value,
                        boolean isSelected, boolean hasFocus, int row, int column) {
                    Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    
                    String hasBarcodes = table.getValueAt(row, 4).toString();
                    if (hasBarcodes.contains("NO")) {
                        c.setBackground(new Color(255, 240, 240)); // Light red for products without barcodes
                    } else {
                        c.setBackground(Color.WHITE);
                    }
                    
                    if (isSelected) {
                        c.setBackground(EnterpriseTheme.PRIMARY);
                        c.setForeground(Color.WHITE);
                    }
                    
                    return c;
                }
            });
            
        } catch (Exception e) {
            LoggerUtil.logError(BarcodeEnterprise.class, "Error loading product overview", e);
            EnterpriseTheme.showError(this, "Failed to load product overview: " + e.getMessage());
        }
    }
    
    /**
     * Quick generate barcode for selected product in overview table
     */
    private void quickGenerateBarcode() {
        int selectedRow = productOverviewTable.getSelectedRow();
        if (selectedRow == -1) {
            EnterpriseTheme.showError(this, "Please select a product from the overview table");
            return;
        }
        
        try {
            Integer productId = (Integer) productOverviewModel.getValueAt(selectedRow, 0);
            String productName = productOverviewModel.getValueAt(selectedRow, 1).toString();
            
            // Ask how many barcodes to generate
            String countStr = JOptionPane.showInputDialog(
                this,
                "Enter number of barcodes to generate for:\n" + productName + "\n\n(1-100)",
                "Generate Barcodes",
                JOptionPane.QUESTION_MESSAGE
            );
            
            if (countStr == null || countStr.trim().isEmpty()) {
                return; // User cancelled
            }
            
            int count = Integer.parseInt(countStr);
            if (count < 1 || count > 100) {
                EnterpriseTheme.showError(this, "Please enter a number between 1 and 100");
                return;
            }
            
            // Generate barcodes
            boolean success = barcodeService.generateMultipleBarcodes(productId, count);
            
            if (success) {
                EnterpriseTheme.showSuccess(this, 
                    "Successfully generated " + count + " barcode(s) for:\n" + productName);
            } else {
                EnterpriseTheme.showError(this, "Failed to generate barcodes for: " + productName);
            }
            
            // Refresh both tables
            loadBarcodes();
            loadProductOverview();
            
        } catch (NumberFormatException e) {
            EnterpriseTheme.showError(this, "Invalid number format");
        } catch (Exception e) {
            LoggerUtil.logError(BarcodeEnterprise.class, "Error generating barcodes", e);
            EnterpriseTheme.showError(this, "Failed to generate barcodes: " + e.getMessage());
        }
    }
    
    private void populateFormFromTable(int row) {
        txtBarcodeId.setText(tableModel.getValueAt(row, 0).toString());
        txtBarcodeNumber.setText(tableModel.getValueAt(row, 1).toString());

        String productName = tableModel.getValueAt(row, 2).toString();
        for (int i = 0; i < cmbProduct.getItemCount(); i++) {
            if (cmbProduct.getItemAt(i).contains(productName)) {
                cmbProduct.setSelectedIndex(i);
                break;
            }
        }

        cmbStatus.setSelectedItem(tableModel.getValueAt(row, 3).toString());

        // Get the full barcode entity to access remarks
        try {
            int barcodeId = Integer.parseInt(txtBarcodeId.getText());
            List<BarcodeEntity> allBarcodes = barcodeService.getAllBarcodes();
            for (BarcodeEntity barcode : allBarcodes) {
                if (barcode.getBarcodeId().equals(barcodeId)) {
                    String remarks = barcode.getRemarks();
                    txtRemarks.setText(remarks != null ? remarks : "");
                    break;
                }
            }
        } catch (Exception e) {
            txtRemarks.setText(""); // Clear if error
            LoggerUtil.logError(BarcodeEnterprise.class, "Error loading barcode remarks", e);
        }
    }
    
    private void addBarcode() {
        try {
            // Validation
            if (!validateBarcodeInput()) {
                return;
            }

            String barcodeNumber = txtBarcodeNumber.getText().trim();
            Integer productId = extractProductId();

            if (productId == null) {
                EnterpriseTheme.showError(this, "Please select a product");
                return;
            }

            // Check for existing barcode with same number
            List<BarcodeEntity> existingBarcodes = barcodeService.getAllBarcodes();
            for (BarcodeEntity existing : existingBarcodes) {
                if (existing.getBarcodeNumber().equals(barcodeNumber)) {
                    EnterpriseTheme.showError(this, "Barcode number already exists: " + barcodeNumber);
                    return;
                }
            }

            if (barcodeService.addBarcode(productId, barcodeNumber)) {
                EnterpriseTheme.showSuccess(this, "Barcode added successfully!");
                clearForm();
                loadBarcodes();
                loadProductOverview();
            }
        } catch (Exception e) {
            LoggerUtil.logError(BarcodeEnterprise.class, "Error adding barcode", e);
            EnterpriseTheme.showError(this, "Failed to add barcode: " + e.getMessage());
        }
    }

    private void updateBarcode() {
        try {
            if (txtBarcodeId.getText().trim().isEmpty()) {
                EnterpriseTheme.showWarning(this, "Please select a barcode to update");
                return;
            }

            if (!validateBarcodeInput()) {
                return;
            }
            String barcodeNumber = txtBarcodeNumber.getText().trim();
            String status = (String) cmbStatus.getSelectedItem();

            if (barcodeService.updateBarcodeStatus(barcodeNumber, status)) {
                EnterpriseTheme.showSuccess(this, "Barcode updated successfully!");
                clearForm();
                loadBarcodes();
                loadProductOverview();
            }
        } catch (Exception e) {
            LoggerUtil.logError(BarcodeEnterprise.class, "Error updating barcode", e);
            EnterpriseTheme.showError(this, "Failed to update barcode: " + e.getMessage());
        }
    }
    
    private void deleteBarcode() {
        try {
            if (txtBarcodeId.getText().trim().isEmpty()) {
                EnterpriseTheme.showWarning(this, "Please select a barcode to delete");
                return;
            }
            
            boolean confirm = EnterpriseTheme.showConfirm(this, 
                "Are you sure you want to delete this barcode?");
            
            if (confirm) {
                int barcodeId = Integer.parseInt(txtBarcodeId.getText().trim());
                if (barcodeService.deleteBarcode(barcodeId)) {
                    EnterpriseTheme.showSuccess(this, "Barcode deleted successfully!");
                    clearForm();
                    loadBarcodes();
                }
            }
        } catch (Exception e) {
            LoggerUtil.logError(BarcodeEnterprise.class, "Error deleting barcode", e);
            EnterpriseTheme.showError(this, "Failed to delete barcode: " + e.getMessage());
        }
    }
    
    private void scanBarcode() {
        try {
            String barcodeNumber = txtScanInput.getText().trim();
            
            if (!ValidationUtil.isNotEmpty(barcodeNumber)) {
                EnterpriseTheme.showWarning(this, "Please enter a barcode to scan");
                return;
            }
            
            ProductEntity product = barcodeService.scanBarcode(barcodeNumber);
            
            if (product != null) {
                String message = String.format(
                    "Barcode Scanned Successfully!\n\n" +
                    "Product: %s\n" +
                    "Type: %s\n" +
                    "Price: $%.2f\n" +
                    "Stock: %d",
                    product.getName(),
                    product.getProductType(),
                    product.getSellPrice(),
                    product.getStock()
                );
                EnterpriseTheme.showSuccess(this, message);
                txtScanInput.setText("");
            }
        } catch (Exception e) {
            LoggerUtil.logError(BarcodeEnterprise.class, "Error scanning barcode", e);
            EnterpriseTheme.showError(this, "Scan failed: " + e.getMessage());
        }
    }
    
    /**
     * Clean up duplicate barcodes for selected product
     */
    private void cleanupDuplicateBarcodes() {
        try {
            Integer productId = extractProductId();

            if (productId == null) {
                EnterpriseTheme.showError(this, "Please select a product first");
                return;
            }

            // Get all barcodes for this product
            List<BarcodeEntity> allBarcodes = barcodeService.getAllBarcodes();
            List<BarcodeEntity> productBarcodes = new ArrayList<>();

            String productName = "";
            for (BarcodeEntity barcode : allBarcodes) {
                if (barcode.getProduct() != null &&
                    barcode.getProduct().getProductId().equals(productId)) {
                    productBarcodes.add(barcode);
                    if (productName.isEmpty()) {
                        productName = barcode.getProduct().getName();
                    }
                }
            }

            if (productBarcodes.size() <= 1) {
                EnterpriseTheme.showWarning(this,
                    "Product '" + productName + "' has only " + productBarcodes.size() + " barcode(s).\nNo cleanup needed.");
                return;
            }

            // Show cleanup options
            String[] options = {"Keep 1 Available", "Keep 2 Available", "Keep 3 Available", "Cancel"};
            int choice = JOptionPane.showOptionDialog(
                this,
                String.format("Product: %s\nTotal Barcodes: %d\n\nHow many barcodes do you want to keep?",
                    productName, productBarcodes.size()),
                "Cleanup Duplicate Barcodes",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
            );

            if (choice == 3 || choice == -1) {
                return; // User cancelled
            }

            int keepCount = choice + 1;

            // Separate available and sold barcodes
            List<BarcodeEntity> availableBarcodes = new ArrayList<>();
            List<BarcodeEntity> soldBarcodes = new ArrayList<>();

            for (BarcodeEntity barcode : productBarcodes) {
                if ("available".equals(barcode.getStatus())) {
                    availableBarcodes.add(barcode);
                } else {
                    soldBarcodes.add(barcode);
                }
            }

            // Keep specified number of available barcodes, delete the rest
            List<BarcodeEntity> toDelete = new ArrayList<>();
            if (availableBarcodes.size() > keepCount) {
                for (int i = keepCount; i < availableBarcodes.size(); i++) {
                    toDelete.add(availableBarcodes.get(i));
                }
            }

            if (toDelete.isEmpty()) {
                EnterpriseTheme.showWarning(this,
                    "No cleanup needed. Product has " + availableBarcodes.size() + " available barcodes.");
                return;
            }

            // Confirm deletion
            boolean confirm = EnterpriseTheme.showConfirm(this,
                String.format("Delete %d duplicate barcode(s) for '%s'?\n\nKeeping: %d available + %d sold barcodes",
                    toDelete.size(), productName, keepCount, soldBarcodes.size()));

            if (confirm) {
                int deletedCount = 0;
                for (BarcodeEntity barcode : toDelete) {
                    if (barcodeService.deleteBarcode(barcode.getBarcodeId())) {
                        deletedCount++;
                    }
                }

                EnterpriseTheme.showSuccess(this,
                    String.format("✅ Cleanup Complete!\n\nDeleted: %d duplicate barcodes\nRemaining: %d barcodes for '%s'",
                        deletedCount, productBarcodes.size() - deletedCount, productName));

                // Refresh tables
                loadBarcodes();
                loadProductOverview();
                clearForm();
            }

        } catch (Exception e) {
            LoggerUtil.logError(BarcodeEnterprise.class, "Error cleaning up barcodes", e);
            EnterpriseTheme.showError(this, "Cleanup failed: " + e.getMessage());
        }
    }

    private void generateBarcodes() {
        try {
            Integer productId = extractProductId();

            if (productId == null) {
                EnterpriseTheme.showError(this, "Please select a product");
                return;
            }

            // Check existing barcodes for this product
            List<BarcodeEntity> allBarcodes = barcodeService.getAllBarcodes();
            int existingCount = 0;
            String productName = "";

            for (BarcodeEntity barcode : allBarcodes) {
                if (barcode.getProduct() != null &&
                    barcode.getProduct().getProductId().equals(productId)) {
                    existingCount++;
                    if (productName.isEmpty()) {
                        productName = barcode.getProduct().getName();
                    }
                }
            }

            // Get product name if not found
            if (productName.isEmpty()) {
                try {
                    Optional<ProductEntity> product = productService.getProductById(productId);
                    productName = product.map(ProductEntity::getName).orElse("Unknown Product");
                } catch (Exception e) {
                    productName = "Product ID: " + productId;
                }
            }

            // Show warning if product already has many barcodes
            if (existingCount > 5) {
                boolean proceed = EnterpriseTheme.showConfirm(this,
                    String.format("⚠️ WARNING: Product '%s' already has %d barcodes!\n\nGenerating more barcodes may create duplicates.\nDo you want to continue?",
                        productName, existingCount));

                if (!proceed) {
                    return;
                }
            }

            String input = JOptionPane.showInputDialog(this,
                String.format("Product: %s\nExisting Barcodes: %d\n\nHow many NEW barcodes to generate? (1-10)",
                    productName, existingCount), "1");

            if (input != null && !input.trim().isEmpty()) {
                int count = Integer.parseInt(input.trim());

                if (count <= 0 || count > 10) {
                    EnterpriseTheme.showError(this, "Please enter a number between 1 and 10");
                    return;
                }

                // Ask for confirmation if total will be high
                int totalAfter = existingCount + count;
                if (totalAfter > 10) {
                    boolean proceed = EnterpriseTheme.showConfirm(this,
                        String.format("This will create %d total barcodes for '%s'.\nRecommended: 1-3 barcodes per product.\n\nContinue?",
                            totalAfter, productName));

                    if (!proceed) {
                        return;
                    }
                }

                if (barcodeService.generateMultipleBarcodes(productId, count)) {
                    EnterpriseTheme.showSuccess(this,
                        String.format("✅ Generated %d new barcode(s)!\n\nProduct: %s\nTotal Barcodes: %d",
                            count, productName, totalAfter));
                    loadBarcodes();
                    loadProductOverview();
                }
            }
        } catch (NumberFormatException e) {
            EnterpriseTheme.showError(this, "Invalid number format");
        } catch (Exception e) {
            LoggerUtil.logError(BarcodeEnterprise.class, "Error generating barcodes", e);
            EnterpriseTheme.showError(this, "Failed to generate barcodes: " + e.getMessage());
        }
    }
    
    private void markBarcodeAsSold() {
        try {
            String barcodeNumber = txtBarcodeNumber.getText().trim();
            
            if (!ValidationUtil.isNotEmpty(barcodeNumber)) {
                EnterpriseTheme.showWarning(this, "Please enter or select a barcode");
                return;
            }
            
            if (barcodeService.markAsSold(barcodeNumber)) {
                EnterpriseTheme.showSuccess(this, "Barcode marked as sold!");
                clearForm();
                loadBarcodes();
            }
        } catch (Exception e) {
            LoggerUtil.logError(BarcodeEnterprise.class, "Error marking barcode as sold", e);
            EnterpriseTheme.showError(this, "Failed to mark as sold: " + e.getMessage());
        }
    }
    
    private void printBarcode() {
        try {
            String barcodeNumber = txtBarcodeNumber.getText().trim();
            if (!ValidationUtil.isNotEmpty(barcodeNumber)) {
                EnterpriseTheme.showWarning(this, "Please enter or select a barcode to print");
                return;
            }
            
            barcodeService.printBarcode(barcodeNumber);
        } catch (Exception e) {
            LoggerUtil.logError(BarcodeEnterprise.class, "Error printing barcode", e);
            EnterpriseTheme.showError(this, "Print failed: " + e.getMessage());
        }
    }
    
    private boolean validateBarcodeInput() {
        String barcodeNumber = txtBarcodeNumber.getText().trim();
        
        if (!ValidationUtil.isNotEmpty(barcodeNumber)) {
            EnterpriseTheme.showError(this, "Barcode number is required");
            return false;
        }
        
        if (!ValidationUtil.isValidBarcode(barcodeNumber)) {
            EnterpriseTheme.showError(this, 
                "Invalid barcode format! Please enter 8-13 digit numbers.");
            return false;
        }
        
        return true;
    }
    
    private Integer extractProductId() {
        String selected = (String) cmbProduct.getSelectedItem();
        
        if (selected == null || selected.startsWith("--")) {
            return null;
        }
        
        try {
            String idPart = selected.split(" - ")[0];
            return Integer.parseInt(idPart);
        } catch (Exception e) {
            return null;
        }
    }
    
    private void clearForm() {
        txtBarcodeId.setText("");
        txtBarcodeNumber.setText("");
        txtScanInput.setText("");
        txtRemarks.setText("");
        cmbProduct.setSelectedIndex(0);
        cmbStatus.setSelectedIndex(0);
    }
    
    private void goBackToDashboard() {
        try {
            new DashboardEnterprise().setVisible(true);
            dispose();
        } catch (Exception e) {
            LoggerUtil.logError(BarcodeEnterprise.class, "Error returning to dashboard", e);
            EnterpriseTheme.showError(this, "Failed to open dashboard: " + e.getMessage());
        }
    }
}
