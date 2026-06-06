package frames;

import models.Session;
import models.entity.ProductEntity;
import models.entity.CustomerEntity;
import service.ProductService;
import service.CustomerService;
import service.TransactionService;
import service.SettingsService;
import util.EnterpriseTheme;
import util.LoggerUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.List;
import java.util.ArrayList;

/**
 * Modern Enterprise Point of Sale Interface
 * Full-featured POS system for processing sales transactions
 */
public class CashTransactionEnterprise extends BaseFrame {
    
    // Services
    private final ProductService productService = new ProductService();
    private final CustomerService customerService = new CustomerService();
    private final TransactionService transactionService = new TransactionService();
    private final SettingsService settingsService = new SettingsService();

    // UI Components - Left Panel (Product Selection)
    private JTextField txtSearch;
    private JPanel productGridPanel;
    private JComboBox<String> cmbCategory;
    private List<ProductEntity> allProducts;
    private List<ProductEntity> filteredProducts;
    private int currentPage = 0;
    private int itemsPerPage = 12; // 3 columns x 4 rows
    private JLabel lblPageInfo;
    private JButton btnPrevPage;
    private JButton btnNextPage;
    
    // UI Components - Right Panel (Cart/Checkout)
    private DefaultTableModel cartTableModel;
    private JTable cartTable;
    private JLabel lblSubtotal;
    private JLabel lblTax;
    private JLabel lblTaxText; // Dynamic tax rate label
    private JLabel lblTotal;
    private JLabel lblItemCount;
    private JComboBox<String> cmbCustomer;
    private JComboBox<String> cmbPaymentMethod;
    private JTextField txtAmountReceived;
    private JLabel lblChange;
    private JLabel lblChangeAmount;
    private JButton btnCompleteSale;
    private JButton btnClearCart;
    
    // Cart data
    private List<CartItem> cartItems = new ArrayList<>();
    private double taxRate; // Load from settings

    public CashTransactionEnterprise() throws Exception {
        super();
        if (!authorized) return;

        // Load tax rate from settings
        taxRate = settingsService.getTaxRate() / 100.0; // Convert percentage to decimal

        setTitle("Point of Sale - Shop Manager");
        EnterpriseTheme.prepareFullScreenFrame(this, "Point of Sale");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        initializeComponents();
        loadProducts();
        loadCustomers();

        // Ensure tax rate label shows current settings value
        refreshTaxRate();

        EnterpriseTheme.applyGlobalTheme();
    }
    
    private void initializeComponents() {
        setLayout(new BorderLayout());
        
        // Header
        createHeader();
        
        // Main content (split pane)
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(1050);
        splitPane.setBackground(EnterpriseTheme.BACKGROUND);
        
        // Left panel - Product selection
        splitPane.setLeftComponent(createProductPanel());
        
        // Right panel - Cart & checkout
        splitPane.setRightComponent(createCartPanel());
        
        add(splitPane, BorderLayout.CENTER);
    }
    
    private void createHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(EnterpriseTheme.SUCCESS);
        headerPanel.setPreferredSize(new Dimension(0, 70));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 30, 15, 30));
        
        JLabel lblTitle = new JLabel("POS - POINT OF SALE");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(Color.WHITE);
        
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightPanel.setBackground(EnterpriseTheme.SUCCESS);
        
        JLabel lblUser = new JLabel(Session.getUsername() + " (" + Session.getRole() + ")");
        lblUser.setFont(EnterpriseTheme.FONT_BODY_BOLD);
        lblUser.setForeground(Color.WHITE);
        
        JButton btnBack = new JButton("Back to Dashboard");
        EnterpriseTheme.styleSecondaryButton(btnBack);
        btnBack.setForeground(EnterpriseTheme.SUCCESS);
        btnBack.setBackground(Color.WHITE);
        btnBack.addActionListener(e -> goBackToDashboard());
        
        rightPanel.add(lblUser);
        rightPanel.add(btnBack);
        
        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(rightPanel, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);
    }
    
    private JPanel createProductPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(EnterpriseTheme.BACKGROUND);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 10));
        
        // Search and filter panel
        JPanel searchPanel = new JPanel(new BorderLayout(10, 0));
        searchPanel.setBackground(EnterpriseTheme.CARD_BG);
        searchPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(EnterpriseTheme.BORDER, 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel lblSearch = new JLabel("Search Products:");
        lblSearch.setFont(EnterpriseTheme.FONT_BODY_BOLD);
        
        txtSearch = new JTextField();
        EnterpriseTheme.styleTextField(txtSearch);
        txtSearch.setPreferredSize(new Dimension(300, 35));
        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                filterProducts();
            }
        });
        
        JLabel lblCategory = new JLabel("Category:");
        lblCategory.setFont(EnterpriseTheme.FONT_BODY);
        
        cmbCategory = new JComboBox<>(new String[]{"All Categories"});
        EnterpriseTheme.styleComboBox(cmbCategory);
        cmbCategory.setPreferredSize(new Dimension(180, 35));
        cmbCategory.addActionListener(e -> filterProducts());
        
        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        topRow.setBackground(EnterpriseTheme.CARD_BG);
        topRow.add(lblSearch);
        topRow.add(txtSearch);
        topRow.add(Box.createHorizontalStrut(20));
        topRow.add(lblCategory);
        topRow.add(cmbCategory);
        
        searchPanel.add(topRow, BorderLayout.CENTER);
        
        // Product grid
        JPanel gridContainer = new JPanel(new BorderLayout());
        gridContainer.setBackground(EnterpriseTheme.BACKGROUND);
        
        // Product header with count
        JPanel productHeaderPanel = new JPanel(new BorderLayout());
        productHeaderPanel.setBackground(EnterpriseTheme.BACKGROUND);
        productHeaderPanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
        
        JLabel lblProducts = new JLabel("Available Products");
        lblProducts.setFont(EnterpriseTheme.FONT_SUBHEADER);
        
        lblPageInfo = new JLabel("Page 1 of 1 (0 items)");
        lblPageInfo.setFont(EnterpriseTheme.FONT_SMALL);
        lblPageInfo.setForeground(EnterpriseTheme.TEXT_SECONDARY);
        
        productHeaderPanel.add(lblProducts, BorderLayout.WEST);
        productHeaderPanel.add(lblPageInfo, BorderLayout.EAST);
        
        productGridPanel = new JPanel(new GridLayout(4, 3, 15, 15));
        productGridPanel.setBackground(EnterpriseTheme.BACKGROUND);
        productGridPanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
        
        JScrollPane scrollPane = new JScrollPane(productGridPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        // Pagination controls
        JPanel paginationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        paginationPanel.setBackground(EnterpriseTheme.BACKGROUND);
        
        btnPrevPage = new JButton("<< Previous");
        EnterpriseTheme.styleSecondaryButton(btnPrevPage);
        btnPrevPage.addActionListener(e -> {
            if (currentPage > 0) {
                currentPage--;
                displayProducts();
            }
        });
        
        btnNextPage = new JButton("Next >>");
        EnterpriseTheme.styleSecondaryButton(btnNextPage);
        btnNextPage.addActionListener(e -> {
            int totalPages = (int) Math.ceil((double) filteredProducts.size() / itemsPerPage);
            if (currentPage < totalPages - 1) {
                currentPage++;
                displayProducts();
            }
        });
        
        paginationPanel.add(btnPrevPage);
        paginationPanel.add(btnNextPage);
        
        gridContainer.add(productHeaderPanel, BorderLayout.NORTH);
        gridContainer.add(scrollPane, BorderLayout.CENTER);
        gridContainer.add(paginationPanel, BorderLayout.SOUTH);
        
        panel.add(searchPanel, BorderLayout.NORTH);
        panel.add(gridContainer, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createCartPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(EnterpriseTheme.CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(EnterpriseTheme.BORDER, 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        // Cart header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(EnterpriseTheme.CARD_BG);
        
        JLabel lblCartTitle = new JLabel("Current Order");
        lblCartTitle.setFont(EnterpriseTheme.FONT_HEADER);
        
        lblItemCount = new JLabel("0 items");
        lblItemCount.setFont(EnterpriseTheme.FONT_BODY);
        lblItemCount.setForeground(EnterpriseTheme.TEXT_SECONDARY);
        
        headerPanel.add(lblCartTitle, BorderLayout.WEST);
        headerPanel.add(lblItemCount, BorderLayout.EAST);
        
        // Cart table
        String[] columns = {"Product", "Price", "Qty -", "Qty", "Qty +", "Total", "Remove"};
        cartTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 2 || column == 4 || column == 6; // Qty buttons and remove
            }
        };
        
        cartTable = new JTable(cartTableModel);
        EnterpriseTheme.styleTable(cartTable);
        cartTable.getColumnModel().getColumn(0).setPreferredWidth(160);
        cartTable.getColumnModel().getColumn(1).setPreferredWidth(70);
        cartTable.getColumnModel().getColumn(2).setPreferredWidth(40);
        cartTable.getColumnModel().getColumn(3).setPreferredWidth(40);
        cartTable.getColumnModel().getColumn(4).setPreferredWidth(40);
        cartTable.getColumnModel().getColumn(5).setPreferredWidth(80);
        cartTable.getColumnModel().getColumn(6).setPreferredWidth(70);
        cartTable.setRowHeight(40);
        
        // Add mouse listener for button clicks
        cartTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = cartTable.rowAtPoint(evt.getPoint());
                int col = cartTable.columnAtPoint(evt.getPoint());
                if (row >= 0 && row < cartItems.size()) {
                    if (col == 2) { // Decrease quantity
                        decreaseQuantity(row);
                    } else if (col == 4) { // Increase quantity
                        increaseQuantity(row);
                    } else if (col == 6) { // Remove button
                        removeFromCart(row);
                    }
                }
            }
        });
        
        // Center align numeric columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        cartTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        cartTable.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        cartTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        cartTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        cartTable.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        cartTable.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);
        
        JScrollPane cartScrollPane = new JScrollPane(cartTable);
        cartScrollPane.setPreferredSize(new Dimension(0, 350));
        
        // Totals panel
        JPanel totalsPanel = createTotalsPanel();
        
        // Customer and payment panel
        JPanel checkoutPanel = createCheckoutPanel();
        
        // Buttons panel
        JPanel buttonsPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        buttonsPanel.setBackground(EnterpriseTheme.CARD_BG);
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        btnClearCart = new JButton("Clear Cart");
        EnterpriseTheme.styleDangerButton(btnClearCart);
        btnClearCart.addActionListener(e -> clearCart());
        
        btnCompleteSale = new JButton("COMPLETE SALE");
        EnterpriseTheme.styleSuccessButton(btnCompleteSale);
        btnCompleteSale.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnCompleteSale.addActionListener(e -> completeSale());
        
        buttonsPanel.add(btnClearCart);
        buttonsPanel.add(btnCompleteSale);
        
        // Assemble cart panel
        panel.add(headerPanel, BorderLayout.NORTH);
        
        JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
        centerPanel.setBackground(EnterpriseTheme.CARD_BG);
        centerPanel.add(cartScrollPane, BorderLayout.CENTER);
        centerPanel.add(totalsPanel, BorderLayout.SOUTH);
        
        panel.add(centerPanel, BorderLayout.CENTER);
        
        JPanel bottomPanel = new JPanel(new BorderLayout(0, 10));
        bottomPanel.setBackground(EnterpriseTheme.CARD_BG);
        bottomPanel.add(checkoutPanel, BorderLayout.CENTER);
        bottomPanel.add(buttonsPanel, BorderLayout.SOUTH);
        
        panel.add(bottomPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createTotalsPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 8));
        panel.setBackground(new Color(249, 250, 251));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(EnterpriseTheme.BORDER, 1),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        
        JLabel lblSubtotalText = new JLabel("Subtotal:");
        lblSubtotalText.setFont(EnterpriseTheme.FONT_BODY);
        lblSubtotal = new JLabel("$0.00");
        lblSubtotal.setFont(EnterpriseTheme.FONT_BODY_BOLD);
        lblSubtotal.setHorizontalAlignment(SwingConstants.RIGHT);

        // Dynamic tax label based on settings
        lblTaxText = new JLabel(String.format("Tax (%.0f%%):", taxRate * 100));
        lblTaxText.setFont(EnterpriseTheme.FONT_BODY);
        lblTax = new JLabel("$0.00");
        lblTax.setFont(EnterpriseTheme.FONT_BODY_BOLD);
        lblTax.setHorizontalAlignment(SwingConstants.RIGHT);
        
        JLabel lblTotalText = new JLabel("TOTAL:");
        lblTotalText.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTotal = new JLabel("$0.00");
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTotal.setForeground(EnterpriseTheme.SUCCESS);
        lblTotal.setHorizontalAlignment(SwingConstants.RIGHT);
        
        panel.add(lblSubtotalText);
        panel.add(lblSubtotal);
        panel.add(lblTaxText);
        panel.add(lblTax);
        panel.add(lblTotalText);
        panel.add(lblTotal);
        
        return panel;
    }
    
    private JPanel createCheckoutPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(EnterpriseTheme.CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(EnterpriseTheme.BORDER, 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Row 0: Customer Selection
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        JLabel lblCustomer = new JLabel("Customer:");
        lblCustomer.setFont(EnterpriseTheme.FONT_BODY_BOLD);
        panel.add(lblCustomer, gbc);
        
        gbc.gridx = 1; gbc.weightx = 0.7;
        cmbCustomer = new JComboBox<>();
        EnterpriseTheme.styleComboBox(cmbCustomer);
        cmbCustomer.setPreferredSize(new Dimension(200, 35));
        panel.add(cmbCustomer, gbc);
        
        // Row 1: Payment Method
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        JLabel lblPayment = new JLabel("Payment Method:");
        lblPayment.setFont(EnterpriseTheme.FONT_BODY_BOLD);
        panel.add(lblPayment, gbc);
        
        gbc.gridx = 1; gbc.weightx = 0.7;
        cmbPaymentMethod = new JComboBox<>(new String[]{"CASH", "CARD", "MOBILE"});
        EnterpriseTheme.styleComboBox(cmbPaymentMethod);
        cmbPaymentMethod.setPreferredSize(new Dimension(200, 35));
        cmbPaymentMethod.addActionListener(e -> updatePaymentFields());
        panel.add(cmbPaymentMethod, gbc);
        
        // Row 2: Amount Received (for CASH only)
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.3;
        JLabel lblAmountReceived = new JLabel("Amount Received:");
        lblAmountReceived.setFont(EnterpriseTheme.FONT_BODY_BOLD);
        lblAmountReceived.setForeground(EnterpriseTheme.PRIMARY);
        panel.add(lblAmountReceived, gbc);
        
        gbc.gridx = 1; gbc.weightx = 0.7;
        txtAmountReceived = new JTextField("0.00");
        EnterpriseTheme.styleTextField(txtAmountReceived);
        txtAmountReceived.setFont(new Font("Segoe UI", Font.BOLD, 16));
        txtAmountReceived.setPreferredSize(new Dimension(200, 40));
        txtAmountReceived.setHorizontalAlignment(JTextField.RIGHT);
        txtAmountReceived.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                calculateChange();
            }
        });
        panel.add(txtAmountReceived, gbc);
        
        // Row 3: Change to Return
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.3;
        lblChange = new JLabel("Change to Return:");
        lblChange.setFont(EnterpriseTheme.FONT_BODY_BOLD);
        lblChange.setForeground(EnterpriseTheme.SUCCESS);
        panel.add(lblChange, gbc);
        
        gbc.gridx = 1; gbc.weightx = 0.7;
        lblChangeAmount = new JLabel("$0.00");
        lblChangeAmount.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblChangeAmount.setForeground(EnterpriseTheme.SUCCESS);
        lblChangeAmount.setHorizontalAlignment(SwingConstants.RIGHT);
        panel.add(lblChangeAmount, gbc);
        
        return panel;
    }
    
    private void updatePaymentFields() {
        txtAmountReceived.setEnabled(true);
        lblChange.setEnabled(true);
        lblChangeAmount.setEnabled(true);
        
        if (!"CASH".equals(cmbPaymentMethod.getSelectedItem())) {
            String totalStr = lblTotal.getText().replace("$", "").replace(",", "");
            txtAmountReceived.setText(totalStr);
        } else {
            txtAmountReceived.setText("");
        }
        
        calculateChange();
    }
    
    private void calculateChange() {
        try {
            double total = Double.parseDouble(lblTotal.getText().replace("$", "").replace(",", ""));
            double received = Double.parseDouble(txtAmountReceived.getText().replace("$", "").replace(",", ""));
            double change = received - total;
            
            lblChangeAmount.setText(String.format("$%.2f", change));
            
            if (change < 0) {
                lblChangeAmount.setForeground(EnterpriseTheme.DANGER);
                lblChange.setText("Insufficient Amount:");
                btnCompleteSale.setEnabled(false);
            } else {
                lblChangeAmount.setForeground(EnterpriseTheme.SUCCESS);
                lblChange.setText("Change to Return:");
                btnCompleteSale.setEnabled(!cartItems.isEmpty());
            }
        } catch (NumberFormatException e) {
            lblChangeAmount.setText("$0.00");
            lblChangeAmount.setForeground(EnterpriseTheme.DANGER);
            btnCompleteSale.setEnabled(false);
        }
    }
    
    private void loadProducts() {
        try {
            allProducts = productService.getAllProducts();
            filteredProducts = new ArrayList<>(allProducts);
            
            // Populate dynamic categories from products
            java.util.Set<String> categories = new java.util.TreeSet<>();
            for (ProductEntity p : allProducts) {
                if (p.getProductType() != null && !p.getProductType().trim().isEmpty()) {
                    categories.add(p.getProductType().trim());
                }
            }
            
            cmbCategory.removeAllItems();
            cmbCategory.addItem("All Categories");
            for (String cat : categories) {
                cmbCategory.addItem(cat);
            }
            
            currentPage = 0;
            displayProducts();
        } catch (Exception e) {
            LoggerUtil.logError(CashTransactionEnterprise.class, "Error loading products", e);
            EnterpriseTheme.showError(this, "Failed to load products: " + e.getMessage());
        }
    }
    
    private void loadCustomers() {
        try {
            List<CustomerEntity> customers = customerService.getAllCustomers();
            cmbCustomer.addItem("Walk-in Customer");
            for (CustomerEntity customer : customers) {
                cmbCustomer.addItem(customer.getName() + " (" + customer.getPhoneNumber() + ")");
            }
        } catch (Exception e) {
            LoggerUtil.logError(CashTransactionEnterprise.class, "Error loading customers", e);
        }
    }
    
    private void filterProducts() {
        String searchText = txtSearch.getText().toLowerCase().trim();
        String category = (String) cmbCategory.getSelectedItem();
        
        filteredProducts.clear();
        
        for (ProductEntity product : allProducts) {
            // Only show active products
            if (!"active".equalsIgnoreCase(product.getStatus())) {
                continue;
            }
            
            boolean matchesSearch = searchText.isEmpty() || 
                product.getName().toLowerCase().contains(searchText) ||
                (product.getProductType() != null && product.getProductType().toLowerCase().contains(searchText)) ||
                (product.getRemarks() != null && product.getRemarks().toLowerCase().contains(searchText));
            
            boolean matchesCategory = "All Categories".equals(category) || 
                (product.getProductType() != null && product.getProductType().equalsIgnoreCase(category));
            
            if (matchesSearch && matchesCategory) {
                filteredProducts.add(product);
            }
        }
        
        currentPage = 0; // Reset to first page after filter
        displayProducts();
    }
    
    private void displayProducts() {
        productGridPanel.removeAll();
        
        int totalProducts = filteredProducts.size();
        int totalPages = (int) Math.ceil((double) totalProducts / itemsPerPage);
        if (totalPages == 0) totalPages = 1;
        
        // Calculate pagination
        int startIndex = currentPage * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, totalProducts);
        
        // Display only products for current page
        for (int i = startIndex; i < endIndex; i++) {
            ProductEntity product = filteredProducts.get(i);
            JPanel productCard = createProductCard(product);
            productGridPanel.add(productCard);
        }
        
        // Fill empty slots to maintain grid layout
        int displayedCount = endIndex - startIndex;
        for (int i = displayedCount; i < itemsPerPage; i++) {
            JPanel emptyCard = new JPanel();
            emptyCard.setBackground(EnterpriseTheme.BACKGROUND);
            emptyCard.setBorder(null);
            productGridPanel.add(emptyCard);
        }
        
        // Update pagination controls
        lblPageInfo.setText(String.format("Page %d of %d (%d items)", 
            currentPage + 1, totalPages, totalProducts));
        btnPrevPage.setEnabled(currentPage > 0);
        btnNextPage.setEnabled(currentPage < totalPages - 1);
        
        productGridPanel.revalidate();
        productGridPanel.repaint();
    }
    
    private JPanel createProductCard(ProductEntity product) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(EnterpriseTheme.CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(EnterpriseTheme.BORDER, 1),
            BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Product info
        JPanel infoPanel = new JPanel(new GridLayout(4, 1, 0, 4));
        infoPanel.setBackground(EnterpriseTheme.CARD_BG);
        
        JLabel lblName = new JLabel(product.getName());
        lblName.setFont(EnterpriseTheme.FONT_BODY_BOLD);
        
        JLabel lblType = new JLabel(product.getProductType() != null ? product.getProductType() : "General");
        lblType.setFont(EnterpriseTheme.FONT_SMALL);
        lblType.setForeground(EnterpriseTheme.TEXT_SECONDARY);
        
        JLabel lblPrice = new JLabel(String.format("$%.2f", product.getSellPrice()));
        lblPrice.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblPrice.setForeground(EnterpriseTheme.SUCCESS);
        
        JLabel lblStock = new JLabel("Stock: " + product.getStock());
        lblStock.setFont(EnterpriseTheme.FONT_SMALL);
        lblStock.setForeground(product.getStock() < 10 ? EnterpriseTheme.DANGER : EnterpriseTheme.TEXT_SECONDARY);
        
        infoPanel.add(lblName);
        infoPanel.add(lblType);
        infoPanel.add(lblPrice);
        infoPanel.add(lblStock);
        
        // Add to cart button
        JButton btnAdd = new JButton("Add to Cart");
        EnterpriseTheme.stylePrimaryButton(btnAdd);
        btnAdd.setFont(EnterpriseTheme.FONT_SMALL_BOLD);
        btnAdd.addActionListener(e -> addToCart(product));
        
        card.add(infoPanel, BorderLayout.CENTER);
        card.add(btnAdd, BorderLayout.SOUTH);
        
        // Hover effect
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                card.setBackground(EnterpriseTheme.HOVER_BG);
                infoPanel.setBackground(EnterpriseTheme.HOVER_BG);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                card.setBackground(EnterpriseTheme.CARD_BG);
                infoPanel.setBackground(EnterpriseTheme.CARD_BG);
            }
        });
        
        return card;
    }
    
    private void addToCart(ProductEntity product) {
        if (product.getStock() <= 0) {
            EnterpriseTheme.showWarning(this, "Product is out of stock!");
            return;
        }
        
        // Check if product already in cart
        for (CartItem item : cartItems) {
            if (item.product.getProductId().equals(product.getProductId())) {
                if (item.quantity < product.getStock()) {
                    item.quantity++;
                    updateCartDisplay();
                    return;
                } else {
                    EnterpriseTheme.showWarning(this, "Cannot add more than available stock!");
                    return;
                }
            }
        }
        
        // Add new item to cart
        cartItems.add(new CartItem(product, 1));
        updateCartDisplay();
    }
    
    private void removeFromCart(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < cartItems.size()) {
            CartItem item = cartItems.get(rowIndex);
            boolean confirm = EnterpriseTheme.showConfirm(this, 
                "Remove " + item.product.getName() + " from cart?");
            if (confirm) {
                cartItems.remove(rowIndex);
                updateCartDisplay();
            }
        }
    }

    /**
     * Refresh tax rate from settings and update UI label
     * Ensures POS always shows current tax rate from settings
     */
    private void refreshTaxRate() {
        try {
            // Get current tax rate from settings
            double newTaxRate = settingsService.getTaxRate() / 100.0;

            // Update tax rate if it changed
            if (Math.abs(newTaxRate - this.taxRate) > 0.001) { // Small epsilon for double comparison
                this.taxRate = newTaxRate;

                // Update tax label text to show current rate
                if (lblTaxText != null) {
                    lblTaxText.setText(String.format("Tax (%.0f%%):", taxRate * 100));
                }

                util.LoggerUtil.logInfo("✅ Tax rate updated to: " + String.format("%.1f%%", taxRate * 100));
            }
        } catch (Exception e) {
            util.LoggerUtil.logError("Error refreshing tax rate: " + e.getMessage(), null);
        }
    }

    private void updateCartDisplay() {
        // Refresh tax rate from settings (ensures current rate is used)
        refreshTaxRate();

        cartTableModel.setRowCount(0);
        double subtotal = 0;
        
        for (int i = 0; i < cartItems.size(); i++) {
            CartItem item = cartItems.get(i);
            double itemTotal = item.product.getSellPrice() * item.quantity;
            subtotal += itemTotal;
            
            cartTableModel.addRow(new Object[]{
                item.product.getName(),
                String.format("$%.2f", item.product.getSellPrice()),
                "-",
                item.quantity,
                "+",
                String.format("$%.2f", itemTotal),
                "X"
            });
        }
        
        double tax = subtotal * taxRate;
        double total = subtotal + tax;
        
        lblSubtotal.setText(String.format("$%.2f", subtotal));
        lblTax.setText(String.format("$%.2f", tax));
        lblTotal.setText(String.format("$%.2f", total));
        lblItemCount.setText(cartItems.size() + " items");
        
        // Recalculate change when cart updates
        calculateChange();
    }
    
    private void increaseQuantity(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < cartItems.size()) {
            CartItem item = cartItems.get(rowIndex);
            if (item.quantity < item.product.getStock()) {
                item.quantity++;
                updateCartDisplay();
            } else {
                EnterpriseTheme.showWarning(this, 
                    "Cannot add more!\nOnly " + item.product.getStock() + " units available in stock.");
            }
        }
    }
    
    private void decreaseQuantity(int rowIndex) {
        if (rowIndex >= 0 && rowIndex < cartItems.size()) {
            CartItem item = cartItems.get(rowIndex);
            if (item.quantity > 1) {
                item.quantity--;
                updateCartDisplay();
            } else {
                // If quantity is 1, ask to remove
                removeFromCart(rowIndex);
            }
        }
    }
    
    private void clearCart() {
        if (cartItems.isEmpty()) {
            return;
        }
        
        boolean confirm = EnterpriseTheme.showConfirm(this, "Clear all items from cart?");
        if (confirm) {
            cartItems.clear();
            updateCartDisplay();
        }
    }
    
    private void completeSale() {
        if (cartItems.isEmpty()) {
            EnterpriseTheme.showWarning(this, "Cart is empty! Add products to complete sale.");
            return;
        }
        
        String paymentMethod = (String) cmbPaymentMethod.getSelectedItem();
        double total = Double.parseDouble(lblTotal.getText().replace("$", "").replace(",", ""));
        
        // Validate payment
        try {
            double received = Double.parseDouble(txtAmountReceived.getText().replace("$", "").replace(",", ""));
            if (received < total) {
                EnterpriseTheme.showError(this, 
                    String.format("Insufficient payment!\n\nTotal Due: $%.2f\nAmount Received: $%.2f\nShort by: $%.2f",
                    total, received, (total - received)));
                txtAmountReceived.requestFocus();
                txtAmountReceived.selectAll();
                return;
            }
        } catch (NumberFormatException e) {
            EnterpriseTheme.showError(this, "Please enter a valid amount received!");
            txtAmountReceived.requestFocus();
            txtAmountReceived.selectAll();
            return;
        }
        
        // Show confirmation dialog
        StringBuilder confirmMsg = new StringBuilder();
        confirmMsg.append("=== SALE CONFIRMATION ===\n\n");
        confirmMsg.append(String.format("Items: %d\n", cartItems.size()));
        confirmMsg.append(String.format("Total Amount: $%.2f\n", total));
        confirmMsg.append(String.format("Payment Method: %s\n", paymentMethod));
        
        double received = Double.parseDouble(txtAmountReceived.getText().replace("$", "").replace(",", ""));
        double change = received - total;
        confirmMsg.append(String.format("Amount Received: $%.2f\n", received));
        confirmMsg.append(String.format("Change to Return: $%.2f\n", change));
        
        confirmMsg.append("\nProceed with sale?");
        
        boolean confirm = EnterpriseTheme.showConfirm(this, confirmMsg.toString());
        if (!confirm) {
            return;
        }
        
        // Process the sale
        try {
            // Build enhanced receipt with company info and branding
            StringBuilder receiptMsg = new StringBuilder();

            // ---------- HEADER: Company Information ----------
            String companyInfo = settingsService.getCompanyInfoForReceipt();
            receiptMsg.append("=".repeat(40)).append("\n");
            receiptMsg.append(companyInfo);
            receiptMsg.append("=".repeat(40)).append("\n\n");

            // ---------- RECEIPT DETAILS ----------
            receiptMsg.append("RECEIPT\n");
            receiptMsg.append(String.format("Date: %s\n", java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a"))));
            receiptMsg.append(String.format("Cashier: %s\n", Session.getUsername()));
            receiptMsg.append(String.format("Customer: %s\n", cmbCustomer.getSelectedItem()));
            receiptMsg.append(String.format("Payment: %s\n\n", paymentMethod));

            // ---------- ITEMS ----------
            receiptMsg.append("ITEMS:\n");
            receiptMsg.append("-".repeat(40) + "\n");

            // Process each item as a transaction
            for (CartItem item : cartItems) {
                Integer customerId = null;
                // If not walk-in customer, extract customer ID (would need implementation)

                transactionService.processSale(
                    item.product.getProductId(),
                    customerId,
                    item.quantity,
                    paymentMethod,
                    "POS Sale by " + Session.getUsername()
                );

                receiptMsg.append(String.format("%-20s x%d  @$%.2f = $%.2f\n",
                    item.product.getName().length() > 20 ?
                        item.product.getName().substring(0, 17) + "..." :
                        item.product.getName(),
                    item.quantity,
                    item.product.getSellPrice(),
                    item.product.getSellPrice() * item.quantity));
            }

            // ---------- TOTALS ----------
            receiptMsg.append("-".repeat(40) + "\n");
            receiptMsg.append(String.format("Subtotal: %28s\n", lblSubtotal.getText()));
            receiptMsg.append(String.format("Tax (%.0f%%): %27s\n", taxRate * 100, lblTax.getText()));
            receiptMsg.append(String.format("TOTAL: %31s\n", lblTotal.getText()));

            // ---------- PAYMENT INFO ----------
            receiptMsg.append(String.format("\nAmount Received: %19s\n", String.format("$%.2f", received)));
            receiptMsg.append(String.format("Change Returned: %19s\n", String.format("$%.2f", change)));

            receiptMsg.append("\n" + "=".repeat(40));
            receiptMsg.append("\n  Thank you for your business!");
            receiptMsg.append("\n" + "=".repeat(40));

            // ---------- DEVELOPER BRANDING ----------
            String developerCredit = settingsService.getDeveloperCredit();
            if (!developerCredit.isEmpty()) {
                receiptMsg.append("\n").append(developerCredit);
            }

            receiptMsg.append("\n");

            // Show receipt in dialog with print option
            Object[] options = {"Print Receipt", "Close"};
            int choice = JOptionPane.showOptionDialog(
                this,
                receiptMsg.toString(),
                "Sale Completed - Receipt",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[1]
            );

            // If user selected "Print Receipt"
            if (choice == 0) {
                printReceipt(receiptMsg.toString());
            }

            // Clear cart and refresh
            cartItems.clear();
            txtAmountReceived.setText("0.00");
            updateCartDisplay();
            loadProducts(); // Refresh product stock
            cmbCustomer.setSelectedIndex(0); // Reset to walk-in

        } catch (Exception e) {
            LoggerUtil.logError(CashTransactionEnterprise.class, "Error completing sale", e);
            EnterpriseTheme.showError(this, "Failed to complete sale: " + e.getMessage());
        }
    }

    /**
     * Print receipt using Java Print Service
     */
    private void printReceipt(String receiptText) {
        try {
            PrinterJob printerJob = PrinterJob.getPrinterJob();
            printerJob.setJobName("Receipt Print");

            printerJob.setPrintable(new Printable() {
                @Override
                public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
                    if (pageIndex > 0) {
                        return NO_SUCH_PAGE;
                    }

                    Graphics2D g2d = (Graphics2D) graphics;
                    g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

                    // Use monospaced font for receipt
                    Font font = new Font("Courier New", Font.PLAIN, 10);
                    g2d.setFont(font);
                    g2d.setColor(Color.BLACK);

                    // Split text into lines and draw
                    String[] lines = receiptText.split("\n");
                    int y = 20;
                    FontMetrics metrics = g2d.getFontMetrics(font);
                    int lineHeight = metrics.getHeight();

                    for (String line : lines) {
                        g2d.drawString(line, 10, y);
                        y += lineHeight;
                    }

                    return PAGE_EXISTS;
                }
            });

            // Show print dialog
            if (printerJob.printDialog()) {
                printerJob.print();
                EnterpriseTheme.showSuccess(this, "Receipt sent to printer successfully!");
            }

        } catch (PrinterException e) {
            LoggerUtil.logError(CashTransactionEnterprise.class, "Error printing receipt", e);
            EnterpriseTheme.showError(this, "Failed to print receipt: " + e.getMessage());
        }
    }
    
    private void goBackToDashboard() {
        try {
            new DashboardEnterprise().setVisible(true);
            dispose();
        } catch (Exception e) {
            LoggerUtil.logError(CashTransactionEnterprise.class, "Error returning to dashboard", e);
            EnterpriseTheme.showError(this, "Failed to open dashboard: " + e.getMessage());
        }
    }
    
    /**
     * Helper class to represent items in the shopping cart
     */
    private static class CartItem {
        ProductEntity product;
        int quantity;
        
        CartItem(ProductEntity product, int quantity) {
            this.product = product;
            this.quantity = quantity;
        }
    }
}
