package dao;

import models.entity.SettingsEntity;
import org.hibernate.Session;
import org.hibernate.Transaction;
import util.HibernateUtil;

import java.time.LocalDateTime;

/**
 * Settings DAO - Manages system settings persistence
 * Uses singleton pattern (only one settings record with ID=1)
 */
public class SettingsHibernateDAO {

    /**
     * Get system settings (creates default if doesn't exist)
     * @return SettingsEntity
     */
    public SettingsEntity getSettings() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;

        try {
            tx = session.beginTransaction();

            // Try to get existing settings (ID=1)
            SettingsEntity settings = session.get(SettingsEntity.class, 1L);

            // If no settings exist, create default
            if (settings == null) {
                settings = new SettingsEntity();
                settings.setId(1L); // Force ID=1 for singleton pattern
                session.save(settings);
                tx.commit();
                util.LoggerUtil.logInfo("✓ Default settings created");
            } else {
                tx.commit();
            }

            return settings;

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Error loading settings: " + e.getMessage(), e);
        } finally {
            session.close();
        }
    }

    /**
     * Save/Update settings
     * @param settings SettingsEntity to save
     * @param updatedBy Username of user making changes
     */
    public void saveSettings(SettingsEntity settings, String updatedBy) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;

        try {
            tx = session.beginTransaction();

            // Force ID=1 for singleton pattern
            settings.setId(1L);
            settings.setUpdatedAt(LocalDateTime.now());
            settings.setUpdatedBy(updatedBy);

            // Use merge to handle both insert and update
            session.merge(settings);

            tx.commit();
            util.LoggerUtil.logInfo("✓ Settings saved successfully by: " + updatedBy);

        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Error saving settings: " + e.getMessage(), e);
        } finally {
            session.close();
        }
    }

    /**
     * Reset to default settings
     */
    public void resetToDefaults(String updatedBy) {
        SettingsEntity defaults = new SettingsEntity();
        defaults.setUpdatedBy(updatedBy);
        saveSettings(defaults, updatedBy);
    }

    /**
     * Get specific setting value by field name (helper method)
     */
    public <T> T getSetting(String fieldName, Class<T> type) {
        try {
            SettingsEntity settings = getSettings();
            java.lang.reflect.Method getter = SettingsEntity.class.getMethod(
                "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1)
            );
            return type.cast(getter.invoke(settings));
        } catch (Exception e) {
            throw new RuntimeException("Error getting setting: " + fieldName, e);
        }
    }
}
