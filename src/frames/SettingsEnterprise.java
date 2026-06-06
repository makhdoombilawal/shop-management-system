package frames;

import models.Session;
import models.entity.SettingsEntity;
import service.SettingsService;
import util.EnterpriseTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Modern Enterprise Settings Frame
 * Manage system configuration, company information, tax rates, and user preferences
 * NOW WITH REAL DATABASE PERSISTENCE!
 */
public class SettingsEnterprise extends BaseFrame {

    // Service Layer
    private SettingsService settingsService;

    // UI Components
    private JPanel headerPanel;
    private JPanel contentPanel;
    private JTabbedPane tabbedPane;

    // Company Information Tab
    private JTextField txtCompanyName;
    private JTextField txtCompanyPhone;
    private JTextField txtCompanyEmail;
    private JTextField txtCompanyAddress;
    private JTextArea txtCompanyDetails;

    // Tax Rates Tab
    private JSpinner spinnerTaxRate;
    private JSpinner spinnerDiscountRate;
    private JTextArea txtTaxNotes;

    // Receipt Templates Tab
    private JTextArea txtReceiptTemplate;
    private JCheckBox chkPrintBarcodes;
    private JCheckBox chkPrintItemDetails;

    // Backup Schedules Tab
    private JCheckBox chkAutoBackup;
    private JComboBox<String> cmbBackupFrequency;
    private JTextField txtBackupPath;

    // Developer Branding Tab
    private JCheckBox chkShowDeveloperCredit;
    private JTextField txtDeveloperName;
    private JTextField txtDeveloperContact;

    // Buttons
    private JButton btnSave;
    private JButton btnReset;
    private JButton btnBack;

    // UI Preferences Tab (Missing Field Declarations)
    private JCheckBox chkDarkMode;
    private JComboBox<String> cmbTheme;
    private JComboBox<String> cmbLanguage;

    public SettingsEnterprise() throws Exception {
        super();
        if (!authorized) return;

        // Initialize service
        this.settingsService = new SettingsService();

        setTitle("System Settings - Shop Manager");
        setSize(1000, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initializeComponents();
        setupLayout();
        loadSettings(); // Now loads from database!

        EnterpriseTheme.applyGlobalTheme();
    }

    private void initializeComponents() {
        // Header
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(EnterpriseTheme.PRIMARY);
        headerPanel.setPreferredSize(new Dimension(0, 80));
        headerPanel.setBorder(new EmptyBorder(20, 30, 20, 30));

        JLabel lblTitle = new JLabel("SYSTEM SETTINGS");
        lblTitle.setFont(EnterpriseTheme.FONT_TITLE);
        lblTitle.setForeground(EnterpriseTheme.TEXT_ON_PRIMARY);

        JLabel lblUser = new JLabel("User: " + Session.getUsername() + " | Role: " + Session.getRole());
        lblUser.setFont(EnterpriseTheme.FONT_BODY);
        lblUser.setForeground(EnterpriseTheme.TEXT_ON_PRIMARY);

        headerPanel.add(lblTitle, BorderLayout.WEST);
        headerPanel.add(lblUser, BorderLayout.EAST);

        // Content Panel
        contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBackground(EnterpriseTheme.BACKGROUND);
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Tabbed Pane with Modern Styling
        tabbedPane = new JTabbedPane();
        tabbedPane.setTabPlacement(JTabbedPane.LEFT);
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabbedPane.setBackground(new Color(250, 250, 250));
        tabbedPane.setForeground(new Color(60, 60, 60));

        // Modern tabs with icons-like styling
        tabbedPane.addTab("  \u2699 Company  ", createCompanyInfoTab());
        tabbedPane.addTab("  \u0024 Tax Rates  ", createTaxRatesTab());
        tabbedPane.addTab("  \u2399 Receipts  ", createReceiptTemplateTab());
        tabbedPane.addTab("  \u2605 Branding  ", createBrandingTab());
        tabbedPane.addTab("  \u2637 Backup  ", createBackupTab());
        tabbedPane.addTab("  \u2699 Preferences  ", createPreferencesTab());

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(EnterpriseTheme.BACKGROUND);

        btnSave = new JButton("Save Settings");
        btnSave.setFont(EnterpriseTheme.FONT_BUTTON);
        btnSave.setBackground(EnterpriseTheme.SUCCESS);
        btnSave.setForeground(EnterpriseTheme.TEXT_ON_PRIMARY);
        btnSave.setFocusPainted(false);
        btnSave.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSave.addActionListener(e -> saveSettings());

        btnReset = new JButton("Reset");
        btnReset.setFont(EnterpriseTheme.FONT_BUTTON);
        btnReset.setBackground(EnterpriseTheme.ACCENT);
        btnReset.setForeground(EnterpriseTheme.TEXT_ON_PRIMARY);
        btnReset.setFocusPainted(false);
        btnReset.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnReset.addActionListener(e -> loadSettings());

        btnBack = new JButton("Back");
        btnBack.setFont(EnterpriseTheme.FONT_BUTTON);
        btnBack.setBackground(EnterpriseTheme.DANGER);
        btnBack.setForeground(EnterpriseTheme.TEXT_ON_PRIMARY);
        btnBack.setFocusPainted(false);
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBack.addActionListener(e -> {
            this.dispose();
        });

        buttonPanel.add(btnSave);
        buttonPanel.add(btnReset);
        buttonPanel.add(btnBack);

        contentPanel.add(tabbedPane, BorderLayout.CENTER);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createCompanyInfoTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(EnterpriseTheme.CARD_BG);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        
        // Company Name
        JLabel lblName = new JLabel("Company Name:");
        lblName.setFont(EnterpriseTheme.FONT_TITLE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        panel.add(lblName, gbc);
        
        txtCompanyName = new JTextField(25);
        txtCompanyName.setFont(EnterpriseTheme.FONT_BODY);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        panel.add(txtCompanyName, gbc);
        
        // Company Phone
        JLabel lblPhone = new JLabel("Phone:");
        lblPhone.setFont(EnterpriseTheme.FONT_TITLE);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.3;
        panel.add(lblPhone, gbc);
        
        txtCompanyPhone = new JTextField(25);
        txtCompanyPhone.setFont(EnterpriseTheme.FONT_BODY);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        panel.add(txtCompanyPhone, gbc);
        
        // Company Email
        JLabel lblEmail = new JLabel("Email:");
        lblEmail.setFont(EnterpriseTheme.FONT_TITLE);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.3;
        panel.add(lblEmail, gbc);
        
        txtCompanyEmail = new JTextField(25);
        txtCompanyEmail.setFont(EnterpriseTheme.FONT_BODY);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        panel.add(txtCompanyEmail, gbc);
        
        // Company Address
        JLabel lblAddress = new JLabel("Address:");
        lblAddress.setFont(EnterpriseTheme.FONT_TITLE);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0.3;
        panel.add(lblAddress, gbc);
        
        txtCompanyAddress = new JTextField(25);
        txtCompanyAddress.setFont(EnterpriseTheme.FONT_BODY);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        panel.add(txtCompanyAddress, gbc);
        
        // Company Details
        JLabel lblDetails = new JLabel("Details:");
        lblDetails.setFont(EnterpriseTheme.FONT_TITLE);
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0.3;
        gbc.anchor = GridBagConstraints.NORTH;
        panel.add(lblDetails, gbc);
        
        txtCompanyDetails = new JTextArea(5, 25);
        txtCompanyDetails.setFont(EnterpriseTheme.FONT_BODY);
        txtCompanyDetails.setLineWrap(true);
        txtCompanyDetails.setWrapStyleWord(true);
        JScrollPane scrollDetails = new JScrollPane(txtCompanyDetails);
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(scrollDetails, gbc);
        
        return panel;
    }
    
    private JPanel createTaxRatesTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(EnterpriseTheme.CARD_BG);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        
        // Tax Rate
        JLabel lblTaxRate = new JLabel("Tax Rate (%):");
        lblTaxRate.setFont(EnterpriseTheme.FONT_TITLE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(lblTaxRate, gbc);
        
        spinnerTaxRate = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 100.0, 0.5));
        spinnerTaxRate.setFont(EnterpriseTheme.FONT_BODY);
        gbc.gridx = 1;
        panel.add(spinnerTaxRate, gbc);
        
        // Discount Rate
        JLabel lblDiscountRate = new JLabel("Default Discount (%):");
        lblDiscountRate.setFont(EnterpriseTheme.FONT_TITLE);
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(lblDiscountRate, gbc);
        
        spinnerDiscountRate = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 100.0, 0.5));
        spinnerDiscountRate.setFont(EnterpriseTheme.FONT_BODY);
        gbc.gridx = 1;
        panel.add(spinnerDiscountRate, gbc);
        
        // Tax Notes
        JLabel lblTaxNotes = new JLabel("Notes:");
        lblTaxNotes.setFont(EnterpriseTheme.FONT_TITLE);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.NORTH;
        panel.add(lblTaxNotes, gbc);
        
        txtTaxNotes = new JTextArea(5, 25);
        txtTaxNotes.setFont(EnterpriseTheme.FONT_BODY);
        txtTaxNotes.setLineWrap(true);
        txtTaxNotes.setWrapStyleWord(true);
        JScrollPane scrollNotes = new JScrollPane(txtTaxNotes);
        gbc.gridx = 1;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(scrollNotes, gbc);
        
        return panel;
    }
    
    private JPanel createReceiptTemplateTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(EnterpriseTheme.CARD_BG);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        
        // Print Options
        chkPrintBarcodes = new JCheckBox("Print Barcodes");
        chkPrintBarcodes.setFont(EnterpriseTheme.FONT_BODY);
        chkPrintBarcodes.setSelected(true);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(chkPrintBarcodes, gbc);
        
        chkPrintItemDetails = new JCheckBox("Print Item Details");
        chkPrintItemDetails.setFont(EnterpriseTheme.FONT_BODY);
        chkPrintItemDetails.setSelected(true);
        gbc.gridy = 1;
        panel.add(chkPrintItemDetails, gbc);
        
        // Receipt Template
        JLabel lblTemplate = new JLabel("Receipt Template:");
        lblTemplate.setFont(EnterpriseTheme.FONT_TITLE);
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        panel.add(lblTemplate, gbc);
        
        txtReceiptTemplate = new JTextArea(10, 40);
        txtReceiptTemplate.setFont(new Font("Monospaced", Font.PLAIN, 11));
        txtReceiptTemplate.setLineWrap(true);
        txtReceiptTemplate.setWrapStyleWord(true);
        JScrollPane scrollTemplate = new JScrollPane(txtReceiptTemplate);
        gbc.gridy = 3;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(scrollTemplate, gbc);
        
        return panel;
    }

    private JPanel createBrandingTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(EnterpriseTheme.CARD_BG);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Info Label
        JLabel lblInfo = new JLabel("<html><b>Developer Branding</b><br>Configure developer credit displayed on receipts and reports</html>");
        lblInfo.setFont(EnterpriseTheme.FONT_BODY);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(lblInfo, gbc);

        // Show Developer Credit Checkbox
        chkShowDeveloperCredit = new JCheckBox("Show Developer Credit on Receipts");
        chkShowDeveloperCredit.setFont(EnterpriseTheme.FONT_BODY);
        chkShowDeveloperCredit.setSelected(true);
        gbc.gridy = 1;
        panel.add(chkShowDeveloperCredit, gbc);

        // Developer Name
        JLabel lblDevName = new JLabel("Developer Name:");
        lblDevName.setFont(EnterpriseTheme.FONT_TITLE);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        panel.add(lblDevName, gbc);

        txtDeveloperName = new JTextField(25);
        txtDeveloperName.setFont(EnterpriseTheme.FONT_BODY);
        gbc.gridx = 1;
        panel.add(txtDeveloperName, gbc);

        // Developer Contact
        JLabel lblDevContact = new JLabel("Contact:");
        lblDevContact.setFont(EnterpriseTheme.FONT_TITLE);
        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(lblDevContact, gbc);

        txtDeveloperContact = new JTextField(25);
        txtDeveloperContact.setFont(EnterpriseTheme.FONT_BODY);
        gbc.gridx = 1;
        panel.add(txtDeveloperContact, gbc);

        // Preview
        JLabel lblPreview = new JLabel("Preview:");
        lblPreview.setFont(EnterpriseTheme.FONT_TITLE);
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        panel.add(lblPreview, gbc);

        JTextArea txtPreview = new JTextArea(4, 40);
        txtPreview.setFont(new Font("Monospaced", Font.PLAIN, 11));
        txtPreview.setEditable(false);
        txtPreview.setBackground(new Color(245, 245, 245));
        txtPreview.setText("----------------------------------------\nPowered By:\nBilawal Abbasi\nContact: +92-300-XXXXXXX\n----------------------------------------");
        JScrollPane scrollPreview = new JScrollPane(txtPreview);
        gbc.gridy = 5;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panel.add(scrollPreview, gbc);

        return panel;
    }

    private JPanel createBackupTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(EnterpriseTheme.CARD_BG);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        
        // Auto Backup Checkbox
        chkAutoBackup = new JCheckBox("Enable Automatic Backups");
        chkAutoBackup.setFont(EnterpriseTheme.FONT_BODY);
        chkAutoBackup.setSelected(true);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(chkAutoBackup, gbc);
        
        // Backup Frequency
        JLabel lblFrequency = new JLabel("Backup Frequency:");
        lblFrequency.setFont(EnterpriseTheme.FONT_TITLE);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        panel.add(lblFrequency, gbc);
        
        cmbBackupFrequency = new JComboBox<>(new String[]{"Daily", "Weekly", "Monthly"});
        cmbBackupFrequency.setFont(EnterpriseTheme.FONT_BODY);
        gbc.gridx = 1;
        panel.add(cmbBackupFrequency, gbc);
        
        // Backup Path
        JLabel lblPath = new JLabel("Backup Location:");
        lblPath.setFont(EnterpriseTheme.FONT_TITLE);
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(lblPath, gbc);
        
        txtBackupPath = new JTextField(25);
        txtBackupPath.setFont(EnterpriseTheme.FONT_BODY);
        gbc.gridx = 1;
        panel.add(txtBackupPath, gbc);
        
        return panel;
    }
    
    private JPanel createPreferencesTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(EnterpriseTheme.CARD_BG);
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        
        // Dark Mode
        chkDarkMode = new JCheckBox("Dark Mode");
        chkDarkMode.setFont(EnterpriseTheme.FONT_BODY);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(chkDarkMode, gbc);
        
        // Theme
        JLabel lblTheme = new JLabel("Theme:");
        lblTheme.setFont(EnterpriseTheme.FONT_TITLE);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        panel.add(lblTheme, gbc);
        
        cmbTheme = new JComboBox<>(new String[]{"Light", "Dark", "Blue", "Green"});
        cmbTheme.setFont(EnterpriseTheme.FONT_BODY);
        gbc.gridx = 1;
        panel.add(cmbTheme, gbc);
        
        // Language
        JLabel lblLanguage = new JLabel("Language:");
        lblLanguage.setFont(EnterpriseTheme.FONT_TITLE);
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(lblLanguage, gbc);
        
        cmbLanguage = new JComboBox<>(new String[]{"English", "Urdu", "Spanish", "French"});
        cmbLanguage.setFont(EnterpriseTheme.FONT_BODY);
        gbc.gridx = 1;
        panel.add(cmbLanguage, gbc);
        
        return panel;
    }
    

    
    private void setupLayout() {
        setLayout(new BorderLayout());
        add(headerPanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
    }

    private void loadSettings() {
        try {
            // Load from database
            SettingsEntity settings = settingsService.getSettings();

            // Company Information
            txtCompanyName.setText(settings.getCompanyName());
            txtCompanyPhone.setText(settings.getCompanyPhone());
            txtCompanyEmail.setText(settings.getCompanyEmail());
            txtCompanyAddress.setText(settings.getCompanyAddress());
            txtCompanyDetails.setText(settings.getCompanyDetails());

            // Tax Rates
            spinnerTaxRate.setValue(settings.getTaxRate());
            spinnerDiscountRate.setValue(settings.getDiscountRate());
            txtTaxNotes.setText(settings.getTaxNotes());

            // Receipt Template
            txtReceiptTemplate.setText(settings.getReceiptTemplate());
            chkPrintBarcodes.setSelected(settings.getPrintBarcodes() != null && settings.getPrintBarcodes());
            chkPrintItemDetails.setSelected(settings.getPrintItemDetails() != null && settings.getPrintItemDetails());

            // Branding
            chkShowDeveloperCredit.setSelected(settings.getShowDeveloperCredit() != null && settings.getShowDeveloperCredit());
            txtDeveloperName.setText(settings.getDeveloperName());
            txtDeveloperContact.setText(settings.getDeveloperContact());

            // Backup
            chkAutoBackup.setSelected(settings.getAutoBackup() != null && settings.getAutoBackup());
            txtBackupPath.setText(settings.getBackupPath());

            // Set backup frequency
            String frequency = settings.getBackupFrequency();
            if (frequency != null) {
                if (frequency.equals("Daily")) cmbBackupFrequency.setSelectedIndex(0);
                else if (frequency.equals("Weekly")) cmbBackupFrequency.setSelectedIndex(1);
                else if (frequency.equals("Monthly")) cmbBackupFrequency.setSelectedIndex(2);
            }

            // Preferences
            chkDarkMode.setSelected(settings.getDarkMode() != null && settings.getDarkMode());

            // Set theme
            String theme = settings.getTheme();
            if (theme != null) {
                if (theme.equals("Light")) cmbTheme.setSelectedIndex(0);
                else if (theme.equals("Dark")) cmbTheme.setSelectedIndex(1);
                else if (theme.equals("Blue")) cmbTheme.setSelectedIndex(2);
                else if (theme.equals("Green")) cmbTheme.setSelectedIndex(3);
            }

            // Set language
            String language = settings.getLanguage();
            if (language != null) {
                if (language.equals("English")) cmbLanguage.setSelectedIndex(0);
                else if (language.equals("Urdu")) cmbLanguage.setSelectedIndex(1);
                else if (language.equals("Spanish")) cmbLanguage.setSelectedIndex(2);
                else if (language.equals("French")) cmbLanguage.setSelectedIndex(3);
            }

            util.LoggerUtil.logInfo("✓ Settings loaded from database");

        } catch (Exception e) {
            EnterpriseTheme.showError(this, "Error loading settings: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void saveSettings() {
        try {
            // Load existing settings to preserve payment cycle date
            SettingsEntity existingSettings = settingsService.getSettings();
            
            // Create settings entity from UI
            SettingsEntity settings = new SettingsEntity();
            
            // Preserve payment cycle date from existing settings
            if (existingSettings != null && existingSettings.getPaymentCycleStartDate() != null) {
                settings.setPaymentCycleStartDate(existingSettings.getPaymentCycleStartDate());
            }

            // Company Information
            settings.setCompanyName(txtCompanyName.getText().trim());
            settings.setCompanyPhone(txtCompanyPhone.getText().trim());
            settings.setCompanyEmail(txtCompanyEmail.getText().trim());
            settings.setCompanyAddress(txtCompanyAddress.getText().trim());
            settings.setCompanyDetails(txtCompanyDetails.getText().trim());

            // Tax Rates
            settings.setTaxRate((Double) spinnerTaxRate.getValue());
            settings.setDiscountRate((Double) spinnerDiscountRate.getValue());
            settings.setTaxNotes(txtTaxNotes.getText().trim());

            // Receipt Template
            settings.setReceiptTemplate(txtReceiptTemplate.getText());
            settings.setPrintBarcodes(chkPrintBarcodes.isSelected());
            settings.setPrintItemDetails(chkPrintItemDetails.isSelected());

            // Branding
            settings.setShowDeveloperCredit(chkShowDeveloperCredit.isSelected());
            settings.setDeveloperName(txtDeveloperName.getText().trim());
            settings.setDeveloperContact(txtDeveloperContact.getText().trim());

            // Backup
            settings.setAutoBackup(chkAutoBackup.isSelected());
            settings.setBackupFrequency((String) cmbBackupFrequency.getSelectedItem());
            settings.setBackupPath(txtBackupPath.getText().trim());

            // Preferences
            settings.setDarkMode(chkDarkMode.isSelected());
            settings.setTheme((String) cmbTheme.getSelectedItem());
            settings.setLanguage((String) cmbLanguage.getSelectedItem());

            // Save to database
            settingsService.saveSettings(settings, Session.getUsername());

            // Show success message
            EnterpriseTheme.showSuccess(this,
                "✓ Settings saved successfully!\n\n" +
                "Company: " + settings.getCompanyName() + "\n" +
                "Tax Rate: " + settings.getTaxRate() + "%\n" +
                "Theme: " + settings.getTheme() + "\n" +
                "Developer Credit: " + (settings.getShowDeveloperCredit() ? "Enabled" : "Disabled"));

        } catch (IllegalArgumentException e) {
            // Validation error
            EnterpriseTheme.showError(this, "Validation Error: " + e.getMessage());
        } catch (Exception e) {
            EnterpriseTheme.showError(this, "Error saving settings: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
