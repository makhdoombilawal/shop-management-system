package util;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

import javax.swing.JOptionPane;
import java.util.Properties;

/**
 * Hibernate utility with automatic MySQL/SQLite fallback support.
 *
 * This utility automatically detects database availability and configures Hibernate accordingly:
 * - If MySQL is available → uses MySQL (production mode)
 * - If MySQL is not available → falls back to SQLite (local mode)
 *
 * Configuration is dynamic (not static hibernate.cfg.xml) and controlled by DatabaseSelector.
 *
 * @author Shop Management System
 */
public class HibernateUtil {

    private static SessionFactory sessionFactory;
    private static ServiceRegistry serviceRegistry;
    private static DatabaseMode currentDatabaseMode;

    private static boolean isHeadlessMode() {
        return "true".equalsIgnoreCase(System.getProperty("shop.headless"))
                || java.awt.GraphicsEnvironment.isHeadless();
    }

    private static synchronized void buildIfNeeded() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            return;
        }

        try {
            // Detect which database to use
            currentDatabaseMode = DatabaseSelector.getSelectedMode();

            util.LoggerUtil.logInfo("═══════════════════════════════════════════════════════");
            util.LoggerUtil.logInfo("🗄️  Database Mode: " + currentDatabaseMode.getDisplayName());
            util.LoggerUtil.logInfo("═══════════════════════════════════════════════════════");

            // Build Hibernate configuration dynamically
            Configuration configuration = buildDynamicConfiguration(currentDatabaseMode);

            // Allow runtime overrides (useful for installer / deployments)
            applyRuntimeOverrides(configuration);

            // Add all entity classes
            addEntityClasses(configuration);

            // Build SessionFactory
            serviceRegistry = new StandardServiceRegistryBuilder()
                    .applySettings(configuration.getProperties())
                    .build();

            sessionFactory = configuration.buildSessionFactory(serviceRegistry);

            util.LoggerUtil.logInfo("✅ Hibernate SessionFactory created successfully");
            util.LoggerUtil.logInfo("✅ Connected to: " + currentDatabaseMode.getDisplayName());
            util.LoggerUtil.logInfo("═══════════════════════════════════════════════════════");

        } catch (Throwable ex) {
            util.LoggerUtil.logError("❌ SessionFactory creation failed: " + ex, null);
            ex.printStackTrace();

            if (!isHeadlessMode()) {
                showErrorDialog(ex);
            }

            throw new RuntimeException(ex);
        }
    }

    /**
     * Builds Hibernate configuration dynamically based on the detected database mode.
     */
    private static Configuration buildDynamicConfiguration(DatabaseMode mode) {
        Configuration config = new Configuration();
        Properties props = DatabaseSelector.getConfig();

        // Database-specific settings
        if (mode.isMySQL()) {
            configureMySQLMode(config, props);
        } else {
            configureSQLiteMode(config, props);
        }

        // Common Hibernate settings
        config.setProperty("hibernate.show_sql", props.getProperty("hibernate.show_sql", "false"));
        config.setProperty("hibernate.format_sql", props.getProperty("hibernate.format_sql", "true"));
        config.setProperty("hibernate.use_sql_comments", "true");
        config.setProperty("hibernate.hbm2ddl.auto", props.getProperty("hibernate.hbm2ddl.auto", "validate"));
        config.setProperty("hibernate.current_session_context_class", "thread");
        config.setProperty("hibernate.cache.use_second_level_cache", "false");
        config.setProperty("hibernate.jdbc.batch_size", props.getProperty("hibernate.jdbc.batch_size", "30"));
        config.setProperty("hibernate.order_inserts", props.getProperty("hibernate.order_inserts", "true"));
        config.setProperty("hibernate.order_updates", props.getProperty("hibernate.order_updates", "true"));
        config.setProperty("hibernate.jdbc.fetch_size", props.getProperty("hibernate.jdbc.fetch_size", "100"));
        config.setProperty("hibernate.default_batch_fetch_size", props.getProperty("hibernate.default_batch_fetch_size", "50"));
        config.setProperty("hibernate.connection.provider_disables_autocommit", "true");

        return config;
    }

    /**
     * Configures Hibernate for MySQL mode.
     */
    private static void configureMySQLMode(Configuration config, Properties props) {
        config.setProperty("hibernate.connection.driver_class", DatabaseMode.MYSQL.getDriverClass());
        config.setProperty("hibernate.dialect", DatabaseMode.MYSQL.getHibernateDialect());
        config.setProperty("hibernate.connection.url", DatabaseSelector.buildConnectionUrl(DatabaseMode.MYSQL));
        config.setProperty("hibernate.connection.username", DatabaseSelector.getUsername(DatabaseMode.MYSQL));
        config.setProperty("hibernate.connection.password", DatabaseSelector.getPassword(DatabaseMode.MYSQL));
        config.setProperty("hibernate.connection.pool_size", props.getProperty("hibernate.connection.pool_size", "10"));

        if (isClassPresent("org.hibernate.c3p0.internal.C3P0ConnectionProvider")) {
            config.setProperty("hibernate.c3p0.min_size", props.getProperty("hibernate.c3p0.min_size", "5"));
            config.setProperty("hibernate.c3p0.max_size", props.getProperty("hibernate.c3p0.max_size", "30"));
            config.setProperty("hibernate.c3p0.timeout", props.getProperty("hibernate.c3p0.timeout", "300"));
            config.setProperty("hibernate.c3p0.max_statements", props.getProperty("hibernate.c3p0.max_statements", "100"));
            config.setProperty("hibernate.c3p0.idle_test_period", props.getProperty("hibernate.c3p0.idle_test_period", "120"));
        } else {
            util.LoggerUtil.logInfo("⚠️ C3P0 provider not found, using default Hibernate connection provider");
        }
    }

    private static boolean isClassPresent(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Configures Hibernate for SQLite mode.
     */
    private static void configureSQLiteMode(Configuration config, Properties props) {
        config.setProperty("hibernate.connection.driver_class", DatabaseMode.SQLITE.getDriverClass());
        config.setProperty("hibernate.dialect", DatabaseMode.SQLITE.getHibernateDialect());
        config.setProperty("hibernate.connection.url", DatabaseSelector.buildConnectionUrl(DatabaseMode.SQLITE));
        config.setProperty("hibernate.connection.username", "");
        config.setProperty("hibernate.connection.password", "");

        // SQLite-specific optimizations
        config.setProperty("hibernate.connection.pool_size", "1"); // SQLite works best with single connection
        config.setProperty("hibernate.jdbc.batch_size", "50");

        // SQLite pragma settings via connection URL properties
        String sqliteUrl = DatabaseSelector.buildConnectionUrl(DatabaseMode.SQLITE);
        sqliteUrl += "?journal_mode=" + props.getProperty("db.sqlite.journal_mode", "WAL");
        sqliteUrl += "&synchronous=" + props.getProperty("db.sqlite.synchronous", "NORMAL");
        sqliteUrl += "&foreign_keys=" + props.getProperty("db.sqlite.foreign_keys", "ON");
        config.setProperty("hibernate.connection.url", sqliteUrl);
    }

    /**
     * Applies runtime overrides from system properties.
     */
    private static void applyRuntimeOverrides(Configuration config) {
        String url = System.getProperty("shop.db.url");
        String user = System.getProperty("shop.db.user");
        String pass = System.getProperty("shop.db.password");

        if (url != null && !url.trim().isEmpty()) {
            config.setProperty("hibernate.connection.url", url.trim());
            util.LoggerUtil.logInfo("🔧 Runtime override: connection URL");
        }
        if (user != null && !user.trim().isEmpty()) {
            config.setProperty("hibernate.connection.username", user.trim());
            util.LoggerUtil.logInfo("🔧 Runtime override: username");
        }
        if (pass != null && !pass.trim().isEmpty()) {
            config.setProperty("hibernate.connection.password", pass.trim());
            util.LoggerUtil.logInfo("🔧 Runtime override: password");
        }
    }

    /**
     * Adds all entity classes to Hibernate configuration.
     * ORDER MATTERS - dependencies must come first!
     */
    private static void addEntityClasses(Configuration config) {
        config.addAnnotatedClass(models.entity.SubscriptionEntity.class);
        config.addAnnotatedClass(models.entity.EmailQueueEntity.class);
        config.addAnnotatedClass(models.entity.RoleEntity.class);
        config.addAnnotatedClass(models.entity.CategoryEntity.class);
        config.addAnnotatedClass(models.entity.ProductEntity.class);
        config.addAnnotatedClass(models.entity.CustomerEntity.class);
        config.addAnnotatedClass(models.entity.SupplierEntity.class);
        config.addAnnotatedClass(models.entity.UserEntity.class);
        config.addAnnotatedClass(models.entity.BarcodeEntity.class);
        config.addAnnotatedClass(models.entity.TransactionEntity.class);
        config.addAnnotatedClass(models.entity.StockAuditEntity.class);
        config.addAnnotatedClass(models.entity.AuditLogEntity.class);
        config.addAnnotatedClass(models.entity.SettingsEntity.class);
    }

    /**
     * Shows user-friendly error dialog.
     */
    private static void showErrorDialog(Throwable ex) {
        String message = "Database connection failed!\n\n";

        if (currentDatabaseMode == DatabaseMode.SQLITE) {
            message += "SQLite fallback also failed.\n";
            message += "This is unusual - please check:\n";
            message += "• SQLite JDBC driver is present in lib/\n";
            message += "• Application has write permissions\n";
            message += "• No file locks on database file\n\n";
        } else {
            message += "Attempted to connect to: " + currentDatabaseMode.getDisplayName() + "\n\n";
            message += "Please verify:\n";
            message += "• MySQL Server is installed and running\n";
            message += "• Credentials are correct\n";
            message += "• Port 3306 is accessible\n\n";
        }

        message += "Error: " + ex.getMessage();

        JOptionPane.showMessageDialog(null,
                message,
                "Database Connection Error",
                JOptionPane.ERROR_MESSAGE);
    }

    public static SessionFactory getSessionFactory() {
        buildIfNeeded();
        return sessionFactory;
    }

    public static void shutdown() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
            util.LoggerUtil.logInfo("✅ SessionFactory closed successfully!");
        }
        if (serviceRegistry != null) {
            StandardServiceRegistryBuilder.destroy(serviceRegistry);
        }
        sessionFactory = null;
        serviceRegistry = null;
        currentDatabaseMode = null;
    }

    public static boolean isSessionFactoryOpen() {
        return sessionFactory != null && !sessionFactory.isClosed();
    }

    /**
     * Gets the current database mode.
     *
     * @return Current database mode (MySQL or SQLite)
     */
    public static DatabaseMode getCurrentDatabaseMode() {
        return currentDatabaseMode;
    }

    /**
     * Checks if running in MySQL mode.
     *
     * @return true if using MySQL, false if using SQLite
     */
    public static boolean isMySQL() {
        return currentDatabaseMode != null && currentDatabaseMode.isMySQL();
    }

    /**
     * Checks if running in SQLite mode.
     *
     * @return true if using SQLite, false if using MySQL
     */
    public static boolean isSQLite() {
        return currentDatabaseMode != null && currentDatabaseMode.isSQLite();
    }
}
