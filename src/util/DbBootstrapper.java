package util;

import service.CategoryService;
import service.RoleService;
import service.UserService;

import javax.swing.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Database bootstrapper with automatic MySQL/SQLite fallback support.
 *
 * Handles database setup for both MySQL (production) and SQLite (local fallback) modes:
 * - MySQL: Creates database if missing, verifies connection, seeds defaults
 * - SQLite: Auto-creates file-based database via Hibernate, seeds defaults
 *
 * @author Shop Management System
 */
public final class DbBootstrapper {

    private DbBootstrapper() {
    }

    public static void ensureDatabaseReadyInteractive() {
        try {
            ensureDatabaseReady();
        } catch (Exception e) {
            // Only show UI if not explicitly headless
            if (!isHeadlessMode()) {
                showUserFriendlyErrorDialog(e);
            }
            throw e;
        }
    }

    /**
     * Ensures database is ready for use:
     * 1. Detects database mode (MySQL or SQLite)
     * 2. Creates database if needed (MySQL only)
     * 3. Initializes Hibernate SessionFactory
     * 4. Seeds default data (roles, categories, admin user)
     */
    public static void ensureDatabaseReady() {
        DatabaseMode mode = DatabaseSelector.getSelectedMode();

        util.LoggerUtil.logInfo("═══════════════════════════════════════════════════════");
        util.LoggerUtil.logInfo("🚀 Bootstrapping database: " + mode.getDisplayName());
        util.LoggerUtil.logInfo("═══════════════════════════════════════════════════════");

        // Handle database creation based on mode
        if (mode.isMySQL()) {
            ensureMySQLDatabaseReady();
        } else {
            ensureSQLiteDatabaseReady();
        }

        // Seed default data (idempotent - safe to run multiple times)
        util.LoggerUtil.logInfo("📊 Seeding default data...");
        new RoleService().initializeDefaultRoles();
        new CategoryService().initializeDefaultCategories();
        new UserService().initializeDefaultAdmin();
        util.LoggerUtil.logInfo("✅ Database ready and initialized");
        util.LoggerUtil.logInfo("═══════════════════════════════════════════════════════");
    }

    /**
     * Ensures MySQL database is ready.
     */
    private static void ensureMySQLDatabaseReady() {
        try {
            // Try Hibernate directly first (fast path)
            HibernateUtil.getSessionFactory();
        } catch (RuntimeException ex) {
            // If database doesn't exist, create it and retry
            if (looksLikeUnknownDatabase(ex)) {
                util.LoggerUtil.logInfo("📦 MySQL database not found - creating...");
                createMySQLDatabase();
                // Retry
                HibernateUtil.shutdown();
                HibernateUtil.getSessionFactory();
                util.LoggerUtil.logInfo("✅ MySQL database created successfully");
            } else {
                throw ex;
            }
        }
    }

    /**
     * Ensures SQLite database is ready.
     */
    private static void ensureSQLiteDatabaseReady() {
        // SQLite auto-creates database file via Hibernate (hbm2ddl.auto=update)
        // Just initialize Hibernate - it will create the file and tables
        try {
            HibernateUtil.getSessionFactory();
            util.LoggerUtil.logInfo("✅ SQLite database initialized (file-based, no server required)");
        } catch (RuntimeException ex) {
            util.LoggerUtil.logError("❌ Failed to initialize SQLite database", null);
            throw new RuntimeException("SQLite initialization failed. Ensure application has write permissions.", ex);
        }
    }

    /**
     * Creates MySQL database if it doesn't exist.
     */
    private static void createMySQLDatabase() {
        String host = DatabaseSelector.getConfigValue("db.mysql.host", "localhost");
        String port = DatabaseSelector.getConfigValue("db.mysql.port", "3306");
        String database = DatabaseSelector.getConfigValue("db.mysql.database", "shop2");
        String username = DatabaseSelector.getConfigValue("db.mysql.username", "root");
        String password = DatabaseSelector.getConfigValue("db.mysql.password", "");

        // Connect to MySQL server (without database name)
        String serverUrl = String.format("jdbc:mysql://%s:%s/?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true",
                host, port);

        try (Connection conn = DriverManager.getConnection(serverUrl, username, password);
             Statement st = conn.createStatement()) {

            String sql = "CREATE DATABASE IF NOT EXISTS `" + database + "` " +
                    "DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci";
            st.execute(sql);
            util.LoggerUtil.logInfo("✅ MySQL database '" + database + "' created/verified");

        } catch (SQLException e) {
            throw new RuntimeException("Failed to create MySQL database '" + database + "'", e);
        }
    }

    /**
     * Shows user-friendly error dialog with troubleshooting guidance.
     */
    private static void showUserFriendlyErrorDialog(Exception e) {
        DatabaseMode attemptedMode = DatabaseSelector.getSelectedMode();
        String errorMessage = buildUserFriendlyErrorMessage(e, attemptedMode);

        JTextArea messageArea = new JTextArea(errorMessage);
        messageArea.setEditable(false);
        messageArea.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
        messageArea.setBackground(null);
        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(messageArea);
        scrollPane.setPreferredSize(new java.awt.Dimension(550, 350));

        JOptionPane.showMessageDialog(null,
                scrollPane,
                "Database Setup Failed",
                JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Builds user-friendly error message with troubleshooting steps.
     */
    private static String buildUserFriendlyErrorMessage(Exception e, DatabaseMode mode) {
        StringBuilder msg = new StringBuilder();

        msg.append("❌ DATABASE CONNECTION FAILED\n");
        msg.append("══════════════════════════════════════════════\n\n");
        msg.append("Attempted Mode: ").append(mode.getDisplayName()).append("\n\n");

        String rootMsg = rootMessage(e).toLowerCase();

        if (mode.isMySQL()) {
            // MySQL-specific troubleshooting
            if (rootMsg.contains("communications link failure") ||
                rootMsg.contains("connection refused") ||
                rootMsg.contains("unable to open jdbc connection")) {

                msg.append("⚠️  MySQL Server is not running or not installed.\n\n");
                msg.append("TROUBLESHOOTING STEPS:\n\n");
                msg.append("1️⃣ Install MySQL Server 8.0+\n");
                msg.append("   Download: https://dev.mysql.com/downloads/mysql/\n\n");
                msg.append("2️⃣ Start MySQL Service:\n");
                msg.append("   • Windows: Services → MySQL80 → Start\n");
                msg.append("   • Or run: net start MySQL80\n\n");
                msg.append("3️⃣ Restart this application\n\n");
                msg.append("💡 TIP: Application will automatically fall back to\n");
                msg.append("   SQLite if MySQL is not available.\n");

            } else if (rootMsg.contains("access denied")) {

                msg.append("⚠️  MySQL authentication failed.\n\n");
                msg.append("TROUBLESHOOTING STEPS:\n\n");
                msg.append("1️⃣ Check MySQL credentials in config.properties\n");
                msg.append("   Default: username=root, password=root\n\n");
                msg.append("2️⃣ Reset MySQL root password if needed\n\n");
                msg.append("3️⃣ Restart this application\n");

            } else if (rootMsg.contains("unknown database")) {

                msg.append("⚠️  Database does not exist.\n\n");
                msg.append("Note: Application normally creates this automatically.\n\n");
                msg.append("MANUAL FIX:\n");
                msg.append("1️⃣ Open MySQL Command Line Client\n");
                msg.append("2️⃣ Run: CREATE DATABASE shop2;\n");
                msg.append("3️⃣ Restart this application\n");

            } else {
                msg.append("⚠️  Unexpected MySQL error.\n\n");
                msg.append("TROUBLESHOOTING:\n\n");
                msg.append("• Verify MySQL Server is installed and running\n");
                msg.append("• Check firewall/antivirus settings\n");
                msg.append("• Ensure port 3306 is accessible\n");
                msg.append("• Review config.properties\n");
            }

        } else {
            // SQLite-specific troubleshooting
            msg.append("⚠️  SQLite initialization failed.\n\n");
            msg.append("TROUBLESHOOTING STEPS:\n\n");
            msg.append("1️⃣ Ensure SQLite JDBC driver is in lib/ folder\n");
            msg.append("   File: sqlite-jdbc-3.x.x.jar\n\n");
            msg.append("2️⃣ Verify application has write permissions\n");
            msg.append("   SQLite needs to create database file\n\n");
            msg.append("3️⃣ Check if database file is locked by another process\n");
        }

        msg.append("\n══════════════════════════════════════════════\n");
        msg.append("TECHNICAL ERROR DETAILS:\n");
        msg.append(rootMessage(e));

        return msg.toString();
    }

    /**
     * Headless entry for installer.
     */
    public static void runInitializeOnly() {
        System.setProperty("shop.headless", "true");
        System.setProperty("java.awt.headless", "true");
        ensureDatabaseReady();
    }

    private static boolean isHeadlessMode() {
        return "true".equalsIgnoreCase(System.getProperty("shop.headless")) || java.awt.GraphicsEnvironment.isHeadless();
    }

    private static boolean looksLikeUnknownDatabase(Throwable t) {
        String msg = rootMessage(t);
        return msg != null && msg.toLowerCase().contains("unknown database");
    }

    private static String rootMessage(Throwable t) {
        Throwable cur = t;
        while (cur.getCause() != null) cur = cur.getCause();
        return cur.getMessage();
    }
}
