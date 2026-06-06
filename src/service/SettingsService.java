package service;

import dao.SettingsHibernateDAO;
import models.entity.SettingsEntity;
import java.time.LocalDateTime;

/**
 * Settings Service - Business logic for system settings
 */
public class SettingsService {

    public static final int PAYMENT_CYCLE_MONTHS = 2;

    private final SettingsHibernateDAO dao;

    public SettingsService() {
        this.dao = new SettingsHibernateDAO();
    }

    /**
     * Get current system settings
     * @return SettingsEntity
     */
    public SettingsEntity getSettings() {
        return dao.getSettings();
    }

    /**
     * Save system settings
     * @param settings SettingsEntity to save
     * @param username Username of user making changes
     */
    public void saveSettings(SettingsEntity settings, String username) {
        // Validation
        validateSettings(settings);

        // Save
        dao.saveSettings(settings, username);
    }

    /**
     * Reset to default settings
     * @param username Username of user resetting settings
     */
    public void resetToDefaults(String username) {
        dao.resetToDefaults(username);
    }

    /**
     * Get tax rate
     * @return Tax rate percentage (e.g., 17.0 for 17%)
     */
    public double getTaxRate() {
        SettingsEntity settings = dao.getSettings();
        return settings.getTaxRate() != null ? settings.getTaxRate() : 17.0;
    }

    /**
     * Get discount rate
     * @return Discount rate percentage
     */
    public double getDiscountRate() {
        SettingsEntity settings = dao.getSettings();
        return settings.getDiscountRate() != null ? settings.getDiscountRate() : 0.0;
    }

    /**
     * Get receipt template
     * @return Receipt template string
     */
    public String getReceiptTemplate() {
        SettingsEntity settings = dao.getSettings();
        return settings.getReceiptTemplate();
    }

    /**
     * Get company name
     * @return Company name
     */
    public String getCompanyName() {
        SettingsEntity settings = dao.getSettings();
        return settings.getCompanyName();
    }

    /**
     * Get company details for receipt
     * @return formatted company info
     */
    public String getCompanyInfoForReceipt() {
        SettingsEntity settings = dao.getSettings();
        StringBuilder info = new StringBuilder();
        info.append(settings.getCompanyName()).append("\n");
        if (settings.getCompanyAddress() != null && !settings.getCompanyAddress().isEmpty()) {
            info.append(settings.getCompanyAddress()).append("\n");
        }
        if (settings.getCompanyPhone() != null && !settings.getCompanyPhone().isEmpty()) {
            info.append("Tel: ").append(settings.getCompanyPhone()).append("\n");
        }
        if (settings.getCompanyEmail() != null && !settings.getCompanyEmail().isEmpty()) {
            info.append("Email: ").append(settings.getCompanyEmail()).append("\n");
        }
        return info.toString();
    }

    /**
     * Get developer credit (for receipts)
     * @return formatted developer credit or empty string if disabled
     */
    public String getDeveloperCredit() {
        SettingsEntity settings = dao.getSettings();

        if (settings.getShowDeveloperCredit() == null || !settings.getShowDeveloperCredit()) {
            return "";
        }

        StringBuilder credit = new StringBuilder();
        credit.append("\n").append("─".repeat(40)).append("\n");
        credit.append("Powered By:\n");

        if (settings.getDeveloperName() != null && !settings.getDeveloperName().isEmpty()) {
            credit.append(settings.getDeveloperName()).append("\n");
        }

        if (settings.getDeveloperContact() != null && !settings.getDeveloperContact().isEmpty()) {
            credit.append("Contact: ").append(settings.getDeveloperContact()).append("\n");
        }

        credit.append("─".repeat(40));
        return credit.toString();
    }

    /**
     * Validate settings before saving
     */
    private void validateSettings(SettingsEntity settings) {
        if (settings.getTaxRate() < 0 || settings.getTaxRate() > 100) {
            throw new IllegalArgumentException("Tax rate must be between 0 and 100");
        }

        if (settings.getDiscountRate() < 0 || settings.getDiscountRate() > 100) {
            throw new IllegalArgumentException("Discount rate must be between 0 and 100");
        }

        if (settings.getCompanyName() == null || settings.getCompanyName().trim().isEmpty()) {
            throw new IllegalArgumentException("Company name is required");
        }
    }

    /**
     * Reset payment cycle to current date
     * Only for SUPER_ADMIN users
     */
    public LocalDateTime resetPaymentCycle(String username) {
        SettingsEntity settings = dao.getSettings();
        LocalDateTime newDate = LocalDateTime.now();
        settings.setPaymentCycleStartDate(newDate);
        dao.saveSettings(settings, username);
        return newDate;
    }

    /**
     * Get payment cycle start date
     */
    public LocalDateTime getPaymentCycleStartDate() {
        SettingsEntity settings = dao.getSettings();
        if (settings.getPaymentCycleStartDate() == null) {
            LocalDateTime initializedDate = LocalDateTime.now();
            settings.setPaymentCycleStartDate(initializedDate);
            dao.saveSettings(settings, "SYSTEM_INIT");
            return initializedDate;
        }
        return settings.getPaymentCycleStartDate();
    }

    /**
     * Get next payment due date based on cycle start date.
     * Default cycle: 2 months.
     */
    public LocalDateTime getPaymentCycleDueDate() {
        return getPaymentCycleStartDate().plusMonths(PAYMENT_CYCLE_MONTHS);
    }

    /**
     * Returns true when current date/time is on or after due date.
     */
    public boolean isPaymentCycleDueOrExpired() {
        LocalDateTime now = LocalDateTime.now();
        return !now.isBefore(getPaymentCycleDueDate());
    }
}
