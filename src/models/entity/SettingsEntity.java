package models.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Settings Entity - Stores system configuration
 * Single row pattern: Only one settings record exists (ID=1)
 */
@Entity
@Table(name = "settings")
public class SettingsEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // Company Information
    @Column(name = "company_name", length = 200)
    private String companyName;

    @Column(name = "company_phone", length = 50)
    private String companyPhone;

    @Column(name = "company_email", length = 100)
    private String companyEmail;

    @Column(name = "company_address", length = 500)
    private String companyAddress;

    @Column(name = "company_details", length = 2000)
    private String companyDetails;

    // Tax and Pricing
    @Column(name = "tax_rate")
    private Double taxRate;

    @Column(name = "discount_rate")
    private Double discountRate;

    @Column(name = "tax_notes", length = 2000)
    private String taxNotes;

    // Receipt Configuration
    @Column(name = "receipt_template", length = 2000)
    private String receiptTemplate;

    @Column(name = "print_barcodes")
    private Boolean printBarcodes;

    @Column(name = "print_item_details")
    private Boolean printItemDetails;

    // Backup Configuration
    @Column(name = "auto_backup")
    private Boolean autoBackup;

    @Column(name = "backup_frequency", length = 50)
    private String backupFrequency;

    @Column(name = "backup_path", length = 500)
    private String backupPath;

    // User Preferences
    @Column(name = "dark_mode")
    private Boolean darkMode;

    @Column(name = "theme", length = 50)
    private String theme;

    @Column(name = "language", length = 50)
    private String language;

    // Developer Branding (NEW)
    @Column(name = "show_developer_credit")
    private Boolean showDeveloperCredit;

    @Column(name = "developer_name", length = 200)
    private String developerName;

    @Column(name = "developer_contact", length = 100)
    private String developerContact;

    // Audit
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    // Payment Cycle Management (NEW)
    @Column(name = "payment_cycle_start_date")
    private LocalDateTime paymentCycleStartDate;

    // Constructors
    public SettingsEntity() {
        // Default values
        this.companyName = "Shop Management System";
        this.companyPhone = "+92-300-XXXXXXX";
        this.companyEmail = "info@shopmgmt.com";
        this.companyAddress = "Karachi, Pakistan";
        this.companyDetails = "Professional shop management system";
        this.taxRate = 17.0;
        this.discountRate = 0.0;
        this.taxNotes = "Standard tax rate";
        this.receiptTemplate = "========== RECEIPT ==========\nDate: {DATE}\nInvoice: {INVOICE}\n\nItems:\n{ITEMS}\n\nTotal: {TOTAL}\nTax: {TAX}\nGrand Total: {GRAND_TOTAL}\n\nThank you for your purchase!\n==============================";
        this.printBarcodes = true;
        this.printItemDetails = true;
        this.autoBackup = false;
        this.backupFrequency = "Daily";
        this.backupPath = "C:\\Backups\\ShopDB";
        this.darkMode = false;
        this.theme = "Light";
        this.language = "English";
        this.showDeveloperCredit = true;
        this.developerName = "Bilawal Abbasi";
        this.developerContact = "+92-300-XXXXXXX";
        this.updatedAt = LocalDateTime.now();
        this.paymentCycleStartDate = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanyPhone() {
        return companyPhone;
    }

    public void setCompanyPhone(String companyPhone) {
        this.companyPhone = companyPhone;
    }

    public String getCompanyEmail() {
        return companyEmail;
    }

    public void setCompanyEmail(String companyEmail) {
        this.companyEmail = companyEmail;
    }

    public String getCompanyAddress() {
        return companyAddress;
    }

    public void setCompanyAddress(String companyAddress) {
        this.companyAddress = companyAddress;
    }

    public String getCompanyDetails() {
        return companyDetails;
    }

    public void setCompanyDetails(String companyDetails) {
        this.companyDetails = companyDetails;
    }

    public Double getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(Double taxRate) {
        this.taxRate = taxRate;
    }

    public Double getDiscountRate() {
        return discountRate;
    }

    public void setDiscountRate(Double discountRate) {
        this.discountRate = discountRate;
    }

    public String getTaxNotes() {
        return taxNotes;
    }

    public void setTaxNotes(String taxNotes) {
        this.taxNotes = taxNotes;
    }

    public String getReceiptTemplate() {
        return receiptTemplate;
    }

    public void setReceiptTemplate(String receiptTemplate) {
        this.receiptTemplate = receiptTemplate;
    }

    public Boolean getPrintBarcodes() {
        return printBarcodes;
    }

    public void setPrintBarcodes(Boolean printBarcodes) {
        this.printBarcodes = printBarcodes;
    }

    public Boolean getPrintItemDetails() {
        return printItemDetails;
    }

    public void setPrintItemDetails(Boolean printItemDetails) {
        this.printItemDetails = printItemDetails;
    }

    public Boolean getAutoBackup() {
        return autoBackup;
    }

    public void setAutoBackup(Boolean autoBackup) {
        this.autoBackup = autoBackup;
    }

    public String getBackupFrequency() {
        return backupFrequency;
    }

    public void setBackupFrequency(String backupFrequency) {
        this.backupFrequency = backupFrequency;
    }

    public String getBackupPath() {
        return backupPath;
    }

    public void setBackupPath(String backupPath) {
        this.backupPath = backupPath;
    }

    public Boolean getDarkMode() {
        return darkMode;
    }

    public void setDarkMode(Boolean darkMode) {
        this.darkMode = darkMode;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Boolean getShowDeveloperCredit() {
        return showDeveloperCredit;
    }

    public void setShowDeveloperCredit(Boolean showDeveloperCredit) {
        this.showDeveloperCredit = showDeveloperCredit;
    }

    public String getDeveloperName() {
        return developerName;
    }

    public void setDeveloperName(String developerName) {
        this.developerName = developerName;
    }

    public String getDeveloperContact() {
        return developerContact;
    }

    public void setDeveloperContact(String developerContact) {
        this.developerContact = developerContact;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public LocalDateTime getPaymentCycleStartDate() {
        return paymentCycleStartDate;
    }

    public void setPaymentCycleStartDate(LocalDateTime paymentCycleStartDate) {
        this.paymentCycleStartDate = paymentCycleStartDate;
    }
}
