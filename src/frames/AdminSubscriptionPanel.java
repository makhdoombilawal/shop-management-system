package frames;

import models.Session;
import models.entity.EmailQueueEntity;
import service.SubscriptionService;
import util.EnterpriseTheme;
import util.LoggerUtil;
import util.InternetConnectivityUtil;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Super Admin Panel for Subscription Management.
 * Accessible only to SUPER_ADMIN role.
 */
public class AdminSubscriptionPanel extends BaseFrame {

    // UI Panels
    private JPanel sidebarPanel;
    private JPanel headerPanel;
    private JPanel mainContentPanel;
    private JPanel statusBar;
    private JLabel lblStatusMessage;

    // Section 1 Labels
    private JLabel lblCurrentVersion;
    private JLabel lblInstallDate;
    private JLabel lblExpiryDate;
    private JLabel lblRemainingDays;
    private JLabel lblStatusBadge;

    // Section Components
    private JLabel lblLastEmailInfo;
    private JLabel lblEmailStatusBadge;
    private JLabel lblInternetStatus;
    private JTable tblEmailQueue;
    private DefaultTableModel emailTableModel;

    // Section 5 Components
    private JLabel lblMachineId;
    private JLabel lblDeviceId;
    private JLabel lblShopName;
    private JLabel lblOsName;
    private JLabel lblHostname;
    private JLabel lblMacAddress;
    private JLabel lblInstalledVersionInfo;
    private JTextArea txtActivationHistory;

    public AdminSubscriptionPanel() {
        super();
        if (!authorized) {
            return;
        }

        // Access check
        if (!Session.isSuperAdmin()) {
            JOptionPane.showMessageDialog(
                    null,
                    "❌ Access Denied: Super Admin only!",
                    "Access Denied",
                    JOptionPane.ERROR_MESSAGE
            );
            dispose();
            SwingUtilities.invokeLater(() -> {
                try {
                    new DashboardEnterprise().setVisible(true);
                } catch (Exception ignored) {}
            });
            return;
        }

        // Setup Frame Settings
        setTitle("Subscription Control Panel — Super Admin");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        EnterpriseTheme.prepareFullScreenFrame(this, "Subscription Control Panel — Super Admin");

        initializeComponents();
        setupLayout();
        refreshData(false);

        LoggerUtil.logInfo(AdminSubscriptionPanel.class, "Subscription Panel opened by Super Admin: " + Session.getUsername());
    }

    private void initializeComponents() {
        // Main Container
        setLayout(new BorderLayout());

        // Header Panel
        createHeader();

        // Sidebar Panel
        createSidebar();

        // Content Area (Scrollable to fit 5 sections)
        createContentArea();

        // Status Bar at the bottom
        createStatusBar();
    }

    private void createHeader() {
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(EnterpriseTheme.HEADER_BG);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(
                EnterpriseTheme.PADDING_MEDIUM,
                EnterpriseTheme.PADDING_LARGE,
                EnterpriseTheme.PADDING_MEDIUM,
                EnterpriseTheme.PADDING_LARGE
        ));

        JPanel leftPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        leftPanel.setBackground(EnterpriseTheme.HEADER_BG);

        JLabel lblTitle = new JLabel("Subscription Control Engine");
        lblTitle.setFont(EnterpriseTheme.FONT_HEADER_LARGE);
        lblTitle.setForeground(EnterpriseTheme.TEXT_ON_DARK);

        JLabel lblSub = new JLabel("Enterprise Edition — Super Admin Panel");
        lblSub.setFont(EnterpriseTheme.FONT_SMALL);
        lblSub.setForeground(EnterpriseTheme.TEXT_MUTED);

        leftPanel.add(lblTitle);
        leftPanel.add(lblSub);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightPanel.setBackground(EnterpriseTheme.HEADER_BG);

        JLabel lblUser = new JLabel("Bilawal");
        lblUser.setFont(EnterpriseTheme.FONT_BODY_BOLD);
        lblUser.setForeground(EnterpriseTheme.TEXT_ON_DARK);

        JLabel lblRole = new JLabel("(SUPER_ADMIN)");
        lblRole.setFont(EnterpriseTheme.FONT_SMALL);
        lblRole.setForeground(EnterpriseTheme.TEXT_MUTED);

        rightPanel.add(lblUser);
        rightPanel.add(lblRole);

        headerPanel.add(leftPanel, BorderLayout.WEST);
        headerPanel.add(rightPanel, BorderLayout.EAST);
    }

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

        // Brand logo
        JLabel lblBrand = new JLabel("SHOP MANAGER", SwingConstants.CENTER);
        lblBrand.setFont(EnterpriseTheme.FONT_HEADER);
        lblBrand.setForeground(EnterpriseTheme.TEXT_ON_DARK);
        lblBrand.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblBrand.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));
        sidebarPanel.add(lblBrand);

        // Navigation Menu
        addSidebarButton("Dashboard", e -> {
            SwingUtilities.invokeLater(() -> {
                try {
                    new DashboardEnterprise().setVisible(true);
                    dispose();
                } catch (Exception ex) {
                    LoggerUtil.logError(AdminSubscriptionPanel.class, "Failed to return to Dashboard", ex);
                }
            });
        }, false);

        addSidebarButton("Subscription", e -> {}, true);

        // Space glue
        sidebarPanel.add(Box.createVerticalGlue());

        // Back button
        JButton btnBack = new JButton("Back to App");
        EnterpriseTheme.stylePrimaryButton(btnBack);
        btnBack.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnBack.setMaximumSize(new Dimension(220, 45));
        btnBack.addActionListener(e -> {
            SwingUtilities.invokeLater(() -> {
                try {
                    new DashboardEnterprise().setVisible(true);
                    dispose();
                } catch (Exception ex) {
                    LoggerUtil.logError(AdminSubscriptionPanel.class, "Failed to return to Dashboard", ex);
                }
            });
        });
        sidebarPanel.add(btnBack);
    }

    private void addSidebarButton(String text, java.awt.event.ActionListener action, boolean active) {
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

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!active) {
                    button.setBackground(new Color(55, 65, 81));
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

    private void createContentArea() {
        mainContentPanel = new JPanel();
        mainContentPanel.setLayout(new GridBagLayout());
        mainContentPanel.setBackground(EnterpriseTheme.BACKGROUND);
        mainContentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 0, 10, 0);

        // SECTION 1: Status Card
        mainContentPanel.add(createSection1Card(), gbc);
        gbc.gridy++;

        // SECTION 2: Control Panel
        mainContentPanel.add(createSection2Card(), gbc);
        gbc.gridy++;

        // Email Monitoring Panel
        mainContentPanel.add(createSection4Card(), gbc);
        gbc.gridy++;

        // License/History Panel
        mainContentPanel.add(createSection5Card(), gbc);
    }

    private void createStatusBar() {
        statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(new Color(31, 41, 55));
        statusBar.setBorder(BorderFactory.createEmptyBorder(6, 15, 6, 15));

        lblStatusMessage = new JLabel("System Ready");
        lblStatusMessage.setFont(EnterpriseTheme.FONT_SMALL);
        lblStatusMessage.setForeground(EnterpriseTheme.TEXT_MUTED);

        JLabel lblSync = new JLabel("🔄 Sync Status: Connected");
        lblSync.setFont(EnterpriseTheme.FONT_SMALL);
        lblSync.setForeground(EnterpriseTheme.TEXT_MUTED);

        statusBar.add(lblStatusMessage, BorderLayout.WEST);
        statusBar.add(lblSync, BorderLayout.EAST);
    }

    private void setupLayout() {
        add(headerPanel, BorderLayout.NORTH);
        add(sidebarPanel, BorderLayout.WEST);

        JScrollPane scrollPane = new JScrollPane(mainContentPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        add(statusBar, BorderLayout.SOUTH);
    }

    // ========================================================================
    // SECTION BUILDERS (CARD PANELS)
    // ========================================================================

    private JPanel createSection1Card() {
        JPanel card = createCardPanel();
        card.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Card Title
        JLabel title = new JLabel("Subscription Status Card");
        title.setFont(EnterpriseTheme.FONT_TITLE);
        title.setForeground(EnterpriseTheme.PRIMARY);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 4;
        card.add(title, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;

        // Version Info
        JLabel lblVerTitle = new JLabel("Current Version:");
        lblVerTitle.setFont(EnterpriseTheme.FONT_BODY_BOLD);
        card.add(lblVerTitle, gbc);

        lblCurrentVersion = new JLabel("N/A");
        lblCurrentVersion.setFont(EnterpriseTheme.FONT_BODY);
        gbc.gridx = 1;
        card.add(lblCurrentVersion, gbc);

        // Status Badge
        gbc.gridx = 2;
        JLabel lblStatTitle = new JLabel("License Status:");
        lblStatTitle.setFont(EnterpriseTheme.FONT_BODY_BOLD);
        card.add(lblStatTitle, gbc);

        lblStatusBadge = new JLabel("UNKNOWN");
        lblStatusBadge.setFont(EnterpriseTheme.FONT_BODY_BOLD);
        lblStatusBadge.setOpaque(true);
        lblStatusBadge.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        gbc.gridx = 3;
        card.add(lblStatusBadge, gbc);

        // Dates
        gbc.gridy = 2;
        gbc.gridx = 0;
        JLabel lblInstTitle = new JLabel("Installation Date:");
        lblInstTitle.setFont(EnterpriseTheme.FONT_BODY_BOLD);
        card.add(lblInstTitle, gbc);

        lblInstallDate = new JLabel("N/A");
        lblInstallDate.setFont(EnterpriseTheme.FONT_BODY);
        gbc.gridx = 1;
        card.add(lblInstallDate, gbc);

        gbc.gridx = 2;
        JLabel lblExpTitle = new JLabel("Expiry Date:");
        lblExpTitle.setFont(EnterpriseTheme.FONT_BODY_BOLD);
        card.add(lblExpTitle, gbc);

        lblExpiryDate = new JLabel("N/A");
        lblExpiryDate.setFont(EnterpriseTheme.FONT_BODY);
        gbc.gridx = 3;
        card.add(lblExpiryDate, gbc);

        // Remaining days banner
        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.gridwidth = 4;
        lblRemainingDays = new JLabel("Checking remaining duration...", SwingConstants.CENTER);
        lblRemainingDays.setFont(EnterpriseTheme.FONT_HEADER);
        lblRemainingDays.setForeground(EnterpriseTheme.TEXT_SECONDARY);
        lblRemainingDays.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(EnterpriseTheme.BORDER, 1, true),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        card.add(lblRemainingDays, gbc);

        return card;
    }

    private JPanel createSection2Card() {
        JPanel card = createCardPanel();
        card.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Title
        JLabel title = new JLabel("System Control Panel");
        title.setFont(EnterpriseTheme.FONT_TITLE);
        title.setForeground(EnterpriseTheme.PRIMARY);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 5;
        card.add(title, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;

        // Button 1: Send Test Email
        JButton btnSendTest = new JButton("Send Test Email");
        EnterpriseTheme.stylePrimaryButton(btnSendTest);
        btnSendTest.addActionListener(e -> triggerSendTestEmail());
        card.add(btnSendTest, gbc);

        // Button 2: Extend Subscription (Admin override)
        gbc.gridx = 1;
        JButton btnExtend = new JButton("Extend (+2 Months)");
        EnterpriseTheme.styleSuccessButton(btnExtend);
        btnExtend.addActionListener(e -> triggerExtension(2));
        card.add(btnExtend, gbc);

        // Button 3: Force Sync
        gbc.gridx = 2;
        JButton btnSync = new JButton("Force Sync Status");
        EnterpriseTheme.styleWarningButton(btnSync);
        btnSync.addActionListener(e -> triggerSyncStatus());
        card.add(btnSync, gbc);

        // Button 4: View Queue / Refresh table
        gbc.gridx = 3;
        JButton btnRefresh = new JButton("Refresh Queue");
        EnterpriseTheme.styleSecondaryButton(btnRefresh);
        btnRefresh.addActionListener(e -> refreshData(true));
        card.add(btnRefresh, gbc);

        // Button 6: Reset Device Binding
        gbc.gridy = 2;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JButton btnResetBinding = new JButton("Reset Device Binding");
        EnterpriseTheme.styleDangerButton(btnResetBinding);
        btnResetBinding.addActionListener(e -> triggerResetDeviceBinding());
        btnResetBinding.setEnabled(Session.isSuperAdmin());
        card.add(btnResetBinding, gbc);

        return card;
    }


    private JPanel createSection4Card() {
        JPanel card = createCardPanel();
        card.setLayout(new BorderLayout(10, 10));

        // Title and internet status indicator
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(EnterpriseTheme.PANEL_BG);

        JLabel title = new JLabel("Email Monitoring Panel");
        title.setFont(EnterpriseTheme.FONT_TITLE);
        title.setForeground(EnterpriseTheme.PRIMARY);

        lblInternetStatus = new JLabel("● Online", SwingConstants.RIGHT);
        lblInternetStatus.setFont(EnterpriseTheme.FONT_BODY_BOLD);
        lblInternetStatus.setForeground(EnterpriseTheme.SUCCESS);

        topPanel.add(title, BorderLayout.WEST);
        topPanel.add(lblInternetStatus, BorderLayout.EAST);

        card.add(topPanel, BorderLayout.NORTH);

        // Email status panel
        JPanel detailsPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        detailsPanel.setBackground(EnterpriseTheme.PANEL_BG);
        detailsPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        lblLastEmailInfo = new JLabel("Last Email Type: N/A | Recipient: N/A");
        lblLastEmailInfo.setFont(EnterpriseTheme.FONT_BODY);

        lblEmailStatusBadge = new JLabel("No emails in queue", SwingConstants.RIGHT);
        lblEmailStatusBadge.setFont(EnterpriseTheme.FONT_BODY_BOLD);

        detailsPanel.add(lblLastEmailInfo);
        detailsPanel.add(lblEmailStatusBadge);

        // Table panel
        String[] columnNames = {"ID", "Type", "Recipient", "Status", "Retries", "Created At", "Error Message"};
        emailTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblEmailQueue = new JTable(emailTableModel);
        tblEmailQueue.setFont(EnterpriseTheme.FONT_SMALL);
        tblEmailQueue.setRowHeight(32);
        tblEmailQueue.getTableHeader().setFont(EnterpriseTheme.FONT_SMALL_BOLD);

        JScrollPane scrollPane = new JScrollPane(tblEmailQueue);
        scrollPane.setPreferredSize(new Dimension(800, 200));

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(EnterpriseTheme.PANEL_BG);
        bottomPanel.add(detailsPanel, BorderLayout.NORTH);
        bottomPanel.add(scrollPane, BorderLayout.CENTER);

        card.add(bottomPanel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createSection5Card() {
        JPanel card = createCardPanel();
        card.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Title
        JLabel title = new JLabel("License Info Panel");
        title.setFont(EnterpriseTheme.FONT_TITLE);
        title.setForeground(EnterpriseTheme.PRIMARY);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        card.add(title, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;

        // Shop Name
        gbc.gridx = 0;
        JLabel lblShopTitle = new JLabel("Shop Name:");
        lblShopTitle.setFont(EnterpriseTheme.FONT_BODY_BOLD);
        card.add(lblShopTitle, gbc);

        lblShopName = new JLabel("N/A");
        lblShopName.setFont(EnterpriseTheme.FONT_BODY);
        gbc.gridx = 1;
        card.add(lblShopName, gbc);

        // Device ID
        gbc.gridy++;
        gbc.gridx = 0;
        JLabel lblDevTitle = new JLabel("Device ID (SHA-256):");
        lblDevTitle.setFont(EnterpriseTheme.FONT_BODY_BOLD);
        card.add(lblDevTitle, gbc);

        lblDeviceId = new JLabel("N/A");
        lblDeviceId.setFont(EnterpriseTheme.FONT_BODY);
        gbc.gridx = 1;
        card.add(lblDeviceId, gbc);
        
        // Machine ID (Legacy)
        gbc.gridy++;
        gbc.gridx = 0;
        JLabel lblMachTitle = new JLabel("Legacy Machine ID:");
        lblMachTitle.setFont(EnterpriseTheme.FONT_BODY_BOLD);
        card.add(lblMachTitle, gbc);

        lblMachineId = new JLabel("unknown-device");
        lblMachineId.setFont(EnterpriseTheme.FONT_BODY);
        gbc.gridx = 1;
        card.add(lblMachineId, gbc);
        
        // OS Name
        gbc.gridy++;
        gbc.gridx = 0;
        JLabel lblOsTitle = new JLabel("OS Name:");
        lblOsTitle.setFont(EnterpriseTheme.FONT_BODY_BOLD);
        card.add(lblOsTitle, gbc);

        lblOsName = new JLabel("N/A");
        lblOsName.setFont(EnterpriseTheme.FONT_BODY);
        gbc.gridx = 1;
        card.add(lblOsName, gbc);

        // Hostname
        gbc.gridy++;
        gbc.gridx = 0;
        JLabel lblHostTitle = new JLabel("Hostname:");
        lblHostTitle.setFont(EnterpriseTheme.FONT_BODY_BOLD);
        card.add(lblHostTitle, gbc);

        lblHostname = new JLabel("N/A");
        lblHostname.setFont(EnterpriseTheme.FONT_BODY);
        gbc.gridx = 1;
        card.add(lblHostname, gbc);

        // MAC Address
        gbc.gridy++;
        gbc.gridx = 0;
        JLabel lblMacTitle = new JLabel("MAC Address:");
        lblMacTitle.setFont(EnterpriseTheme.FONT_BODY_BOLD);
        card.add(lblMacTitle, gbc);

        lblMacAddress = new JLabel("N/A");
        lblMacAddress.setFont(EnterpriseTheme.FONT_BODY);
        gbc.gridx = 1;
        card.add(lblMacAddress, gbc);

        // Installed Version
        gbc.gridy++;
        gbc.gridx = 0;
        JLabel lblVerTitle = new JLabel("Installed Version:");
        lblVerTitle.setFont(EnterpriseTheme.FONT_BODY_BOLD);
        card.add(lblVerTitle, gbc);

        lblInstalledVersionInfo = new JLabel("v1");
        lblInstalledVersionInfo.setFont(EnterpriseTheme.FONT_BODY);
        gbc.gridx = 1;
        card.add(lblInstalledVersionInfo, gbc);

        // History Log Title
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        JLabel lblHistTitle = new JLabel("Activation History / Log Log:");
        lblHistTitle.setFont(EnterpriseTheme.FONT_BODY_BOLD);
        card.add(lblHistTitle, gbc);

        // Text area scrollable
        gbc.gridy++;
        txtActivationHistory = new JTextArea(8, 40);
        txtActivationHistory.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtActivationHistory.setEditable(false);
        txtActivationHistory.setBackground(new Color(245, 247, 250));
        txtActivationHistory.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(txtActivationHistory);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        card.add(scrollPane, gbc);

        return card;
    }

    // ========================================================================
    // BACKEND ACTIONS & SWINGWORKERS
    // ========================================================================

    private void setStatusBarMessage(String message) {
        lblStatusMessage.setText(message);
    }

    private void refreshData(boolean silent) {
        setStatusBarMessage("Refreshing data...");
        if (!silent) {
            lblInternetStatus.setText("● Checking...");
            lblInternetStatus.setForeground(EnterpriseTheme.TEXT_MUTED);
        }

        SwingWorker<Void, Object[]> worker = new SwingWorker<Void, Object[]>() {
            private SubscriptionService.SubscriptionInfo info;
            private boolean online;
            private List<EmailQueueEntity> emailLogs;
            private List<String> historyLogs;

            @Override
            protected Void doInBackground() throws Exception {
                // Fetch info and online status
                info = SubscriptionService.getSubscriptionInfo();
                online = InternetConnectivityUtil.isInternetAvailable(false);
                emailLogs = SubscriptionService.getEmailQueueLogs();
                historyLogs = SubscriptionService.getActivationHistory();
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    updateUIWithData(info, online, emailLogs, historyLogs);
                    setStatusBarMessage("Data synchronized successfully.");
                } catch (Exception e) {
                    setStatusBarMessage("Sync failed: " + e.getMessage());
                    LoggerUtil.logError(AdminSubscriptionPanel.class, "Failed to sync panel data", e);
                }
            }
        };
        worker.execute();
    }

    private void updateUIWithData(SubscriptionService.SubscriptionInfo info, boolean online,
                                   List<EmailQueueEntity> emailLogs, List<String> historyLogs) {
        // Section 1
        lblCurrentVersion.setText(info.getInstalledVersion());
        lblInstallDate.setText(info.getInstallDate() != null ? info.getInstallDate().toString() : "N/A");
        lblExpiryDate.setText(info.getExpiryDate() != null ? info.getExpiryDate().toString() : "N/A");
        
        long remaining = info.getRemainingDays();
        String status = info.getStatus();

        if ("EXPIRED".equalsIgnoreCase(status) || remaining <= 0) {
            lblStatusBadge.setText(" EXPIRED ");
            lblStatusBadge.setBackground(EnterpriseTheme.DANGER);
            lblStatusBadge.setForeground(Color.WHITE);
            lblRemainingDays.setText("⚠ SYSTEM EXPIRED (" + remaining + " days ago) — Renewal Required");
            lblRemainingDays.setForeground(EnterpriseTheme.DANGER);
        } else if (remaining < 7) {
            lblStatusBadge.setText(" WARNING ");
            lblStatusBadge.setBackground(EnterpriseTheme.WARNING);
            lblStatusBadge.setForeground(Color.WHITE);
            lblRemainingDays.setText("⚠ Warning: " + remaining + " days remaining. Renewal Code required soon.");
            lblRemainingDays.setForeground(EnterpriseTheme.WARNING);
        } else {
            lblStatusBadge.setText(" ACTIVE ");
            lblStatusBadge.setBackground(EnterpriseTheme.SUCCESS);
            lblStatusBadge.setForeground(Color.WHITE);
            lblRemainingDays.setText("✓ Active: " + remaining + " days remaining on commercial license");
            lblRemainingDays.setForeground(EnterpriseTheme.SUCCESS);
        }

        // Internet & Email Queue Table
        lblInternetStatus.setText(online ? "● Online" : "● Offline");
        lblInternetStatus.setForeground(online ? EnterpriseTheme.SUCCESS : EnterpriseTheme.DANGER);

        emailTableModel.setRowCount(0);
        if (!emailLogs.isEmpty()) {
            EmailQueueEntity latest = emailLogs.get(emailLogs.size() - 1);
            lblLastEmailInfo.setText(String.format("Last Email: %s -> %s", latest.getEmailType(), latest.getRecipient()));
            
            lblEmailStatusBadge.setText("Queue size: " + emailLogs.size() + " (" + latest.getStatus() + ")");
            if ("SENT".equalsIgnoreCase(latest.getStatus())) {
                lblEmailStatusBadge.setForeground(EnterpriseTheme.SUCCESS);
            } else if ("FAILED".equalsIgnoreCase(latest.getStatus())) {
                lblEmailStatusBadge.setForeground(EnterpriseTheme.DANGER);
            } else {
                lblEmailStatusBadge.setForeground(EnterpriseTheme.WARNING);
            }

            // Populate table in reverse order (newest first)
            for (int i = emailLogs.size() - 1; i >= 0; i--) {
                EmailQueueEntity email = emailLogs.get(i);
                emailTableModel.addRow(new Object[]{
                        email.getId(),
                        email.getEmailType(),
                        email.getRecipient(),
                        email.getStatus(),
                        email.getRetryCount(),
                        email.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                        email.getErrorMessage() != null ? email.getErrorMessage() : "None"
                });
            }
        } else {
            lblLastEmailInfo.setText("Last Email Type: N/A | Recipient: N/A");
            lblEmailStatusBadge.setText("No emails in queue");
            lblEmailStatusBadge.setForeground(EnterpriseTheme.TEXT_MUTED);
        }

        // Section 5
        lblShopName.setText(info.getShopName());
        lblDeviceId.setText(info.getDeviceId());
        lblOsName.setText(info.getOsName());
        lblHostname.setText(info.getHostname());
        lblMacAddress.setText(info.getMacAddress());
        lblMachineId.setText(info.getMachineId());
        lblInstalledVersionInfo.setText(info.getInstalledVersion());

        StringBuilder historyBuilder = new StringBuilder();
        for (String logLine : historyLogs) {
            historyBuilder.append(logLine).append("\n");
        }
        txtActivationHistory.setText(historyBuilder.toString());
    }

    // ========================================================================
    // ACTION TRIGGERS (SWINGWORKERS)
    // ========================================================================

    private void triggerResetDeviceBinding() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to reset the device binding?\n" +
                "This will allow the application to be run on a new device on the next startup.",
                "Confirm Device Unbind",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            setStatusBarMessage("Resetting device binding...");
            SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return SubscriptionService.resetDeviceBinding();
                }

                @Override
                protected void done() {
                    try {
                        boolean success = get();
                        if (success) {
                            EnterpriseTheme.showSuccess(AdminSubscriptionPanel.this, "Device binding has been reset.");
                        } else {
                            EnterpriseTheme.showError(AdminSubscriptionPanel.this, "Failed to reset device binding.");
                        }
                        refreshData(true);
                    } catch (Exception e) {
                        EnterpriseTheme.showError(AdminSubscriptionPanel.this, "Error: " + e.getMessage());
                    }
                }
            };
            worker.execute();
        }
    }

    private void triggerSendTestEmail() {
        String recipient = JOptionPane.showInputDialog(
                this,
                "Enter test email recipient:",
                "bilawalabbasi069@gmail.com"
        );

        if (recipient == null || recipient.trim().isEmpty()) {
            return;
        }

        setStatusBarMessage("Sending test email...");
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return SubscriptionService.sendTestEmail(recipient.trim());
            }

            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        EnterpriseTheme.showSuccess(AdminSubscriptionPanel.this, "Test email queued successfully!");
                    } else {
                        EnterpriseTheme.showError(AdminSubscriptionPanel.this, "Failed to queue test email.");
                    }
                    refreshData(true);
                } catch (Exception e) {
                    EnterpriseTheme.showError(AdminSubscriptionPanel.this, "Error: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void triggerExtension(int months) {
        SubscriptionService.SubscriptionInfo info = SubscriptionService.getSubscriptionInfo();
        if (info.getId() == null) {
            EnterpriseTheme.showError(this, "No subscription record found to extend.");
            return;
        }

        setStatusBarMessage("Extending license...");
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return SubscriptionService.extendSubscription(info.getId(), months);
            }

            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        EnterpriseTheme.showSuccess(AdminSubscriptionPanel.this, "Subscription successfully extended by " + months + " months!");
                    } else {
                        EnterpriseTheme.showError(AdminSubscriptionPanel.this, "Failed to extend subscription.");
                    }
                    refreshData(true);
                } catch (Exception e) {
                    EnterpriseTheme.showError(AdminSubscriptionPanel.this, "Error: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void triggerSyncStatus() {
        setStatusBarMessage("Syncing subscription status...");
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                SubscriptionService.forceSyncStatus();
                return null;
            }

            @Override
            protected void done() {
                EnterpriseTheme.showSuccess(AdminSubscriptionPanel.this, "Sync complete. Subscription status updated.");
                refreshData(true);
            }
        };
        worker.execute();
    }


}
