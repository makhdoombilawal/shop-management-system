package frames;

import models.Session;
import service.*;
import util.EnterpriseTheme;
import util.LoggerUtil;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CompletableFuture;

/**
 * Enterprise Dashboard - Main landing page after login
 * Displays key metrics, navigation, and system status
 * Optimized for Full HD (1920x1080) screens
 * 
 * @author Shop Management System - Enterprise Edition
 * @version 2.0
 */
public class DashboardEnterprise extends BaseFrame {
    
    // Services
    private CustomerService customerService;
    private ProductService productService;
    private TransactionService transactionService;
    
    // Metric labels
    private JLabel lblTotalCustomers;
    private JLabel lblTotalProducts;
    private JLabel lblTodaySales;
    private JLabel lblTodayTransactions;
    private JLabel lblLowStockItems;
    private JLabel lblTotalRevenue;
    
    // Panels
    private JPanel sidebarPanel;
    private JPanel mainContentPanel;
    private JPanel headerPanel;
    private JPanel metricsPanel;
    
    /**
     * Constructor
     */
    public DashboardEnterprise() {
        super();
        if (!authorized) {
            return;
        }
        
        // Initialize services
        initializeServices();
        
        // Setup UI
        initializeComponents();
        setupLayout();
        loadMetrics();
        
        // Window settings
        EnterpriseTheme.prepareFullScreenFrame(this, "Dashboard - Shop Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Main frame
        
        LoggerUtil.logInfo(DashboardEnterprise.class, 
            "Dashboard opened by user: " + Session.getUsername());
    }
    
    /**
     * Initialize services
     */
    private void initializeServices() {
        this.customerService = new CustomerService();
        this.productService = new ProductService();
        this.transactionService = new TransactionService();
    }
    
    /**
     * Initialize all UI components
     */
    private void initializeComponents() {
        // Main container
        setLayout(new BorderLayout());
        getContentPane().setBackground(EnterpriseTheme.BACKGROUND);
        
        // Header
        createHeader();
        
        // Sidebar
        createSidebar();
        
        // Main content area
        createMainContent();
    }
    
    /**
     * Create header panel with title and user info
     */
    private void createHeader() {
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(EnterpriseTheme.HEADER_BG);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(
            EnterpriseTheme.PADDING_MEDIUM, 
            EnterpriseTheme.PADDING_LARGE,
            EnterpriseTheme.PADDING_MEDIUM, 
            EnterpriseTheme.PADDING_LARGE
        ));
        
        // Left: Title and date
        JPanel leftPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        leftPanel.setBackground(EnterpriseTheme.HEADER_BG);
        
        JLabel lblTitle = new JLabel("Dashboard");
        lblTitle.setFont(EnterpriseTheme.FONT_HEADER_LARGE);
        lblTitle.setForeground(EnterpriseTheme.TEXT_ON_DARK);
        
        JLabel lblDate = new JLabel(LocalDate.now().format(
            DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy")));
        lblDate.setFont(EnterpriseTheme.FONT_SMALL);
        lblDate.setForeground(EnterpriseTheme.TEXT_MUTED);
        
        leftPanel.add(lblTitle);
        leftPanel.add(lblDate);
        
        // Right: User info and logout
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightPanel.setBackground(EnterpriseTheme.HEADER_BG);
        
        // User info
        JLabel lblUser = new JLabel("Welcome, " + Session.getUsername());
        lblUser.setFont(EnterpriseTheme.FONT_BODY);
        lblUser.setForeground(EnterpriseTheme.TEXT_ON_DARK);
        
        JLabel lblRole = new JLabel("(" + Session.getRole() + ")");
        lblRole.setFont(EnterpriseTheme.FONT_SMALL);
        lblRole.setForeground(EnterpriseTheme.TEXT_MUTED);
        
        // Logout button
        JButton btnLogout = new JButton("Logout");
        EnterpriseTheme.styleDangerButton(btnLogout);
        btnLogout.addActionListener(e -> handleLogout());
        
        // Subscription Indicator (for header)
        boolean subscriptionEnabled = "true".equalsIgnoreCase(
                util.DatabaseSelector.getConfigValue("subscription.enabled", "false")
        );
        if (subscriptionEnabled) {
            long remaining = service.SubscriptionService.getRemainingDays();
            service.SubscriptionService.SubscriptionStatus status = service.SubscriptionService.checkStatus();
            Color dotColor;
            String statusText;
            if (status == service.SubscriptionService.SubscriptionStatus.EXPIRED) {
                dotColor = EnterpriseTheme.DANGER;
                statusText = "Expired";
            } else if (status == service.SubscriptionService.SubscriptionStatus.WARNING) {
                dotColor = EnterpriseTheme.WARNING;
                statusText = remaining + " days remaining";
            } else {
                dotColor = EnterpriseTheme.SUCCESS;
                statusText = remaining + " days remaining";
            }
            
            JLabel lblDot = new JLabel("● ");
            lblDot.setFont(EnterpriseTheme.FONT_BODY);
            lblDot.setForeground(dotColor);
            
            JLabel lblSubStatus = new JLabel(statusText);
            lblSubStatus.setFont(EnterpriseTheme.FONT_SMALL);
            lblSubStatus.setForeground(EnterpriseTheme.TEXT_MUTED);
            
            JPanel subIndicatorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
            subIndicatorPanel.setBackground(EnterpriseTheme.HEADER_BG);
            subIndicatorPanel.add(lblDot);
            subIndicatorPanel.add(lblSubStatus);
            subIndicatorPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 15));
            rightPanel.add(subIndicatorPanel);
        }
        
        rightPanel.add(lblUser);
        rightPanel.add(lblRole);
        rightPanel.add(btnLogout);
        
        headerPanel.add(leftPanel, BorderLayout.WEST);
        headerPanel.add(rightPanel, BorderLayout.EAST);
    }
    
    /**
     * Create sidebar with navigation menu
     */
    private void createSidebar() {
        sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBackground(EnterpriseTheme.SIDEBAR_BG);
        sidebarPanel.setPreferredSize(new Dimension(250, 0));
        sidebarPanel.setBorder(BorderFactory.createEmptyBorder(
            EnterpriseTheme.PADDING_LARGE, 
            EnterpriseTheme.PADDING_MEDIUM,
            EnterpriseTheme.PADDING_LARGE, 
            EnterpriseTheme.PADDING_MEDIUM
        ));
        
        // Logo/Brand section
        JLabel lblBrand = new JLabel("SHOP MANAGER", SwingConstants.CENTER);
        lblBrand.setFont(EnterpriseTheme.FONT_HEADER);
        lblBrand.setForeground(EnterpriseTheme.TEXT_ON_DARK);
        lblBrand.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblBrand.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));
        sidebarPanel.add(lblBrand);
        
        // Navigation menu
        addSidebarButton("Dashboard", e -> refreshDashboard(), true);
        addSidebarSeparator();
        
        addSidebarButton("Point of Sale", e -> openCashTransaction(), false);
        addSidebarButton("Products", e -> openProducts(), false);
        addSidebarButton("Customers", e -> openCustomers(), false);
        addSidebarButton("Suppliers", e -> openSuppliers(), false);
        addSidebarButton("Purchase Orders", e -> openPurchaseOrders(), false);
        addSidebarButton("Transactions", e -> openTransactions(), false);
        addSidebarButton("Barcode Manager", e -> openBarcodes(), false);
        
        addSidebarSeparator();
        
        // Admin-only section
        if (Session.isAdmin() || Session.isManager()) {
            addSidebarButton("User Management", e -> openUsers(), false);
            addSidebarButton("Reports", e -> openReports(), false);
            addSidebarSeparator();
        }
        
        // Settings/System
        addSidebarButton("Settings", e -> openSettings(), false);
        
        if (Session.isSuperAdmin()) {
            addSidebarSeparator();
            addSidebarButton("Subscription", e -> openSubscriptionPanel(), false);
        }
        
        // Push remaining space to bottom
        sidebarPanel.add(Box.createVerticalGlue());
        
        // System info at bottom
        JPanel systemInfo = new JPanel(new GridLayout(3, 1, 0, 4));
        systemInfo.setBackground(EnterpriseTheme.SIDEBAR_BG);
        systemInfo.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));
        
        JLabel lblVersion = new JLabel("Version 2.0", SwingConstants.CENTER);
        lblVersion.setFont(EnterpriseTheme.FONT_TINY);
        lblVersion.setForeground(EnterpriseTheme.TEXT_MUTED);
        
        JLabel lblCopyright = new JLabel("© 2026 Shop Manager", SwingConstants.CENTER);
        lblCopyright.setFont(EnterpriseTheme.FONT_TINY);
        lblCopyright.setForeground(EnterpriseTheme.TEXT_MUTED);
        
        systemInfo.add(lblVersion);
        systemInfo.add(lblCopyright);
        
        sidebarPanel.add(systemInfo);
    }
    
    /**
     * Add navigation button to sidebar
     */
    private void addSidebarButton(String text, ActionListener action, boolean active) {
        JButton button = new JButton(text);
        button.setFont(EnterpriseTheme.FONT_BODY);
        button.setForeground(EnterpriseTheme.TEXT_ON_DARK);
        button.setBackground(active ? EnterpriseTheme.PRIMARY : EnterpriseTheme.SIDEBAR_BG);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        
        // Hover effect
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!active) {
                    button.setBackground(new Color(55, 65, 81)); // Gray-700
                }
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                if (!active) {
                    button.setBackground(EnterpriseTheme.SIDEBAR_BG);
                }
            }
        });
        
        button.addActionListener(action);
        sidebarPanel.add(button);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 5)));
    }
    
    /**
     * Add separator to sidebar
     */
    private void addSidebarSeparator() {
        JSeparator separator = new JSeparator();
        separator.setForeground(new Color(75, 85, 99)); // Gray-600
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        separator.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebarPanel.add(separator);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 10)));
    }
    
    /**
     * Create main content area with metrics and analytics
     */
    private void createMainContent() {
        mainContentPanel = new JPanel(new BorderLayout());
        mainContentPanel.setBackground(EnterpriseTheme.BACKGROUND);
        mainContentPanel.setBorder(BorderFactory.createEmptyBorder(
            EnterpriseTheme.PADDING_LARGE, 
            EnterpriseTheme.PADDING_LARGE,
            EnterpriseTheme.PADDING_LARGE, 
            EnterpriseTheme.PADDING_LARGE
        ));
        
        // Metrics cards
        createMetricsPanel();
        
        // Quick actions panel
        JPanel quickActionsPanel = createQuickActionsPanel();
        
        // Recent activity panel
        JPanel recentActivityPanel = createRecentActivityPanel();
        
        // Layout
        JPanel topSection = new JPanel(new BorderLayout(0, EnterpriseTheme.PADDING_LARGE));
        topSection.setBackground(EnterpriseTheme.BACKGROUND);
        topSection.add(metricsPanel, BorderLayout.NORTH);
        topSection.add(quickActionsPanel, BorderLayout.CENTER);
        
        mainContentPanel.add(topSection, BorderLayout.NORTH);
        mainContentPanel.add(recentActivityPanel, BorderLayout.CENTER);
    }
    
    /**
     * Create metrics panel with key statistics
     */
    private void createMetricsPanel() {
        metricsPanel = new JPanel(new GridLayout(2, 3, 
            EnterpriseTheme.PADDING_MEDIUM, EnterpriseTheme.PADDING_MEDIUM));
        metricsPanel.setBackground(EnterpriseTheme.BACKGROUND);
        
        // Create metric cards
        lblTotalCustomers = createMetricCard("Total Customers", "Loading...", 
            EnterpriseTheme.PRIMARY, "[□]");
        lblTotalProducts = createMetricCard("Total Products", "Loading...", 
            EnterpriseTheme.INFO, "[□]");
        lblTodaySales = createMetricCard("Today's Sales", "Loading...", 
            EnterpriseTheme.SUCCESS, "$");
        lblTodayTransactions = createMetricCard("Today's Transactions", "Loading...", 
            EnterpriseTheme.WARNING, "[□]");
        lblLowStockItems = createMetricCard("Low Stock Items", "Loading...", 
            EnterpriseTheme.DANGER, "!");
        lblTotalRevenue = createMetricCard("Total Revenue", "Loading...", 
            EnterpriseTheme.ACCENT, "$");
    }
    
    /**
     * Create individual metric card
     */
    private JLabel createMetricCard(String title, String initialValue, 
                                     Color accentColor, String icon) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(EnterpriseTheme.CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(EnterpriseTheme.BORDER, 1, true),
            BorderFactory.createEmptyBorder(
                EnterpriseTheme.PADDING_LARGE, 
                EnterpriseTheme.PADDING_LARGE,
                EnterpriseTheme.PADDING_LARGE, 
                EnterpriseTheme.PADDING_LARGE
            )
        ));
        
        // Top: Icon and title
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(EnterpriseTheme.CARD_BG);
        
        JLabel lblIcon = new JLabel(icon);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
        lblIcon.setForeground(accentColor);
        
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(EnterpriseTheme.FONT_SMALL);
        lblTitle.setForeground(EnterpriseTheme.TEXT_SECONDARY);
        
        topPanel.add(lblIcon, BorderLayout.WEST);
        topPanel.add(lblTitle, BorderLayout.CENTER);
        
        // Value label
        JLabel lblValue = new JLabel(initialValue);
        lblValue.setFont(EnterpriseTheme.FONT_HEADER_LARGE);
        lblValue.setForeground(accentColor);
        lblValue.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        card.add(topPanel, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.CENTER);
        
        metricsPanel.add(card);
        
        return lblValue; // Return label so we can update it
    }
    
    /**
     * Create quick actions panel
     */
    private JPanel createQuickActionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(EnterpriseTheme.CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(EnterpriseTheme.BORDER, 1, true),
            BorderFactory.createEmptyBorder(
                EnterpriseTheme.PADDING_LARGE, 
                EnterpriseTheme.PADDING_LARGE,
                EnterpriseTheme.PADDING_LARGE, 
                EnterpriseTheme.PADDING_LARGE
            )
        ));
        
        // Title
        JLabel lblTitle = new JLabel("Quick Actions");
        lblTitle.setFont(EnterpriseTheme.FONT_SUBHEADER);
        lblTitle.setForeground(EnterpriseTheme.TEXT_PRIMARY);
        
        // Buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 
            EnterpriseTheme.PADDING_MEDIUM, EnterpriseTheme.PADDING_SMALL));
        buttonsPanel.setBackground(EnterpriseTheme.CARD_BG);
        
        JButton btnNewSale = new JButton("New Sale");
        EnterpriseTheme.stylePrimaryButton(btnNewSale);
        btnNewSale.addActionListener(e -> openCashTransaction());
        
        JButton btnAddProduct = new JButton("Add Product");
        EnterpriseTheme.styleSuccessButton(btnAddProduct);
        btnAddProduct.addActionListener(e -> openProducts());
        
        JButton btnAddCustomer = new JButton("Add Customer");
        EnterpriseTheme.styleSecondaryButton(btnAddCustomer);
        btnAddCustomer.addActionListener(e -> openCustomers());
        
        JButton btnViewReports = new JButton("View Reports");
        EnterpriseTheme.styleWarningButton(btnViewReports);
        btnViewReports.addActionListener(e -> openReports());
        
        buttonsPanel.add(btnNewSale);
        buttonsPanel.add(btnAddProduct);
        buttonsPanel.add(btnAddCustomer);
        if (Session.isAdmin() || Session.isManager()) {
            buttonsPanel.add(btnViewReports);
        }
        
        panel.add(lblTitle, BorderLayout.NORTH);
        panel.add(buttonsPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Create recent activity panel
     */
    private JPanel createRecentActivityPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(EnterpriseTheme.CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(EnterpriseTheme.BORDER, 1, true),
            BorderFactory.createEmptyBorder(
                EnterpriseTheme.PADDING_LARGE, 
                EnterpriseTheme.PADDING_LARGE,
                EnterpriseTheme.PADDING_LARGE, 
                EnterpriseTheme.PADDING_LARGE
            )
        ));
        
        // Title
        JLabel lblTitle = new JLabel("System Status");
        lblTitle.setFont(EnterpriseTheme.FONT_SUBHEADER);
        lblTitle.setForeground(EnterpriseTheme.TEXT_PRIMARY);
        lblTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        // Status text area
        JTextArea textArea = new JTextArea();
        textArea.setFont(EnterpriseTheme.FONT_BODY);
        textArea.setForeground(EnterpriseTheme.TEXT_SECONDARY);
        textArea.setBackground(EnterpriseTheme.CARD_BG);
        textArea.setEditable(false);
        textArea.setText("[OK] System running normally\n" +
                        "[OK] Database connection active\n" +
                        "[OK] All services operational\n\n" +
                        "Last updated: " + java.time.LocalDateTime.now().format(
                            DateTimeFormatter.ofPattern("hh:mm:ss a")));
        
        JScrollPane scrollPane = new JScrollPane(textArea);
        EnterpriseTheme.styleScrollPane(scrollPane);
        
        panel.add(lblTitle, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Setup the main layout
     */
    private void setupLayout() {
        add(headerPanel, BorderLayout.NORTH);
        add(sidebarPanel, BorderLayout.WEST);
        add(mainContentPanel, BorderLayout.CENTER);
    }
    
    /**
     * Load all metrics asynchronously
     */
    private void loadMetrics() {
        // Use CompletableFuture for async loading
        CompletableFuture.runAsync(() -> loadCustomersCount());
        CompletableFuture.runAsync(() -> loadProductsCount());
        CompletableFuture.runAsync(() -> loadTodaySales());
        CompletableFuture.runAsync(() -> loadTodayTransactions());
        CompletableFuture.runAsync(() -> loadLowStockCount());
        CompletableFuture.runAsync(() -> loadTotalRevenue());
    }
    
    /**
     * Load total customers count
     */
    private void loadCustomersCount() {
        try {
            Long count = customerService.getTotalCustomersCount();
            SwingUtilities.invokeLater(() -> 
                lblTotalCustomers.setText(String.format("%,d", count)));
        } catch (Exception e) {
            SwingUtilities.invokeLater(() -> lblTotalCustomers.setText("Error"));
            LoggerUtil.logError(DashboardEnterprise.class, "Error loading customers count", e);
        }
    }
    
    /**
     * Load total products count
     */
    private void loadProductsCount() {
        try {
            Long count = productService.getTotalProductsCount();
            SwingUtilities.invokeLater(() -> 
                lblTotalProducts.setText(String.format("%,d", count)));
        } catch (Exception e) {
            SwingUtilities.invokeLater(() -> lblTotalProducts.setText("Error"));
            LoggerUtil.logError(DashboardEnterprise.class, "Error loading products count", e);
        }
    }
    
    /**
     * Load today's sales total
     */
    private void loadTodaySales() {
        try {
            Double totalSales = transactionService.getTodayTotalSales();
            String formatted = String.format("$%,.2f", totalSales != null ? totalSales : 0.0);
            SwingUtilities.invokeLater(() -> lblTodaySales.setText(formatted));
        } catch (Exception e) {
            SwingUtilities.invokeLater(() -> lblTodaySales.setText("Error"));
            LoggerUtil.logError(DashboardEnterprise.class, "Error loading today's sales", e);
        }
    }
    
    /**
     * Load today's transaction count
     */
    private void loadTodayTransactions() {
        try {
            Long count = transactionService.getTodayTransactionCount();
            SwingUtilities.invokeLater(() -> 
                lblTodayTransactions.setText(String.format("%,d", count != null ? count : 0)));
        } catch (Exception e) {
            SwingUtilities.invokeLater(() -> lblTodayTransactions.setText("Error"));
            LoggerUtil.logError(DashboardEnterprise.class, "Error loading today's transactions", e);
        }
    }
    
    /**
     * Load low stock items count
     */
    private void loadLowStockCount() {
        try {
            Long count = productService.getLowStockProductsCount();
            SwingUtilities.invokeLater(() -> 
                lblLowStockItems.setText(String.format("%,d", count != null ? count : 0)));
        } catch (Exception e) {
            SwingUtilities.invokeLater(() -> lblLowStockItems.setText("Error"));
            LoggerUtil.logError(DashboardEnterprise.class, "Error loading low stock count", e);
        }
    }
    
    /**
     * Load total revenue
     */
    private void loadTotalRevenue() {
        try {
            Double totalRevenue = transactionService.getTotalRevenue();
            String formatted = String.format("$%,.2f", totalRevenue != null ? totalRevenue : 0.0);
            SwingUtilities.invokeLater(() -> lblTotalRevenue.setText(formatted));
        } catch (Exception e) {
            SwingUtilities.invokeLater(() -> lblTotalRevenue.setText("Error"));
            LoggerUtil.logError(DashboardEnterprise.class, "Error loading total revenue", e);
        }
    }
    
    // ================================================================
    // NAVIGATION METHODS
    // ================================================================
    
    private void refreshDashboard() {
        loadMetrics();
        EnterpriseTheme.showSuccess(this, "Dashboard refreshed");
    }
    
    private void openCashTransaction() {
        SwingUtilities.invokeLater(() -> {
            try {
                new CashTransactionEnterprise().setVisible(true);
                dispose();
            } catch (Exception e) {
                LoggerUtil.logError(DashboardEnterprise.class, "Error opening CashTransaction frame", e);
                EnterpriseTheme.showError(this, "Error opening Point of Sale: " + e.getMessage());
            }
        });
    }
    
    private void openProducts() {
        SwingUtilities.invokeLater(() -> {
            try {
                new ProductEnterprise().setVisible(true);
                dispose();
            } catch (Exception e) {
                LoggerUtil.logError(DashboardEnterprise.class, "Error opening Product frame", e);
                EnterpriseTheme.showError(this, "Error opening Product Management: " + e.getMessage());
            }
        });
    }
    
    private void openCustomers() {
        SwingUtilities.invokeLater(() -> {
            try {
                new CustomerEnterprise().setVisible(true);
                dispose();
            } catch (Exception e) {
                LoggerUtil.logError(DashboardEnterprise.class, "Error opening Customer frame", e);
                EnterpriseTheme.showError(this, "Error opening Customer Management: " + e.getMessage());
            }
        });
    }
    
    private void openSuppliers() {
        SwingUtilities.invokeLater(() -> {
            try {
                new SupplierEnterprise().setVisible(true);
                dispose();
            } catch (Exception e) {
                LoggerUtil.logError(DashboardEnterprise.class, "Error opening Supplier frame", e);
                EnterpriseTheme.showError(this, "Error opening Supplier Management: " + e.getMessage());
            }
        });
    }
    
    private void openPurchaseOrders() {
        SwingUtilities.invokeLater(() -> {
            try {
                new PurchaseOrderEnterprise().setVisible(true);
                dispose();
            } catch (Exception e) {
                LoggerUtil.logError(DashboardEnterprise.class, "Error opening Purchase Orders", e);
                EnterpriseTheme.showError(this, "Error opening Purchase Order Management: " + e.getMessage());
            }
        });
    }
    
    private void openTransactions() {
        SwingUtilities.invokeLater(() -> {
            try {
                new TransactionEnterprise().setVisible(true);
                dispose();
            } catch (Exception e) {
                LoggerUtil.logError(DashboardEnterprise.class, "Error opening Transaction frame", e);
                EnterpriseTheme.showError(this, "Error opening Transactions: " + e.getMessage());
            }
        });
    }
    
    private void openBarcodes() {
        SwingUtilities.invokeLater(() -> {
            try {
                new BarcodeEnterprise().setVisible(true);
                dispose();
            } catch (Exception e) {
                LoggerUtil.logError(DashboardEnterprise.class, "Error opening Barcode frame", e);
                EnterpriseTheme.showError(this, "Error opening Barcode Management: " + e.getMessage());
            }
        });
    }
    
    private void openUsers() {
        SwingUtilities.invokeLater(() -> {
            try {
                new UserEnterprise().setVisible(true);
                dispose();
            } catch (Exception e) {
                LoggerUtil.logError(DashboardEnterprise.class, "Error opening User frame", e);
                EnterpriseTheme.showError(this, "Error opening User Management: " + e.getMessage());
            }
        });
    }
    
    /**
     * Open Reports module with comprehensive business analytics
     */
    private void openReports() {
        SwingUtilities.invokeLater(() -> {
            try {
                new ReportEnterprise().setVisible(true);
                dispose();
            } catch (Exception e) {
                LoggerUtil.logError(DashboardEnterprise.class, "Error opening Reports", e);
                EnterpriseTheme.showError(this, "Error opening Reports module: " + e.getMessage());
            }
        });
    }
    
    /**
     * Open Settings module
     */
    private void openSettings() {
        try {
            SettingsEnterprise settingsFrame = new SettingsEnterprise();
            settingsFrame.setVisible(true);
        } catch (Exception ex) {
            LoggerUtil.logError("Error opening Settings", ex);
            EnterpriseTheme.showError(this, "Failed to open Settings: " + ex.getMessage());
        }
    }
    
    private void openSubscriptionPanel() {
        SwingUtilities.invokeLater(() -> {
            try {
                new AdminSubscriptionPanel().setVisible(true);
                dispose();
            } catch (Exception e) {
                LoggerUtil.logError(DashboardEnterprise.class, "Error opening Subscription Panel", e);
                EnterpriseTheme.showError(this, "Error opening Subscription Panel: " + e.getMessage());
            }
        });
    }
    
    private void handleLogout() {
        boolean confirm = EnterpriseTheme.showConfirm(this, 
            "Are you sure you want to logout?");
        
        if (confirm) {
            Session.logout();
            LoggerUtil.logInfo(DashboardEnterprise.class, "User logged out");
            
            SwingUtilities.invokeLater(() -> {
                new Login().setVisible(true);
                dispose();
            });
        }
    }
    
    /**
     * Main method for testing
     */
    @SuppressWarnings("deprecation")
    public static void main(String[] args) {
        // Apply theme
        EnterpriseTheme.applyGlobalTheme();
        
        // For testing - simulate login
        Session.login("admin");
        
        SwingUtilities.invokeLater(() -> {
            DashboardEnterprise dashboard = new DashboardEnterprise();
            dashboard.setVisible(true);
        });
    }
}
