package util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Automatic database selector that detects MySQL availability and falls back to SQLite.
 *
 * This class implements the core logic for the enterprise fallback system:
 * 1. Attempts to connect to MySQL with a short timeout
 * 2. If MySQL is available and accessible → uses MySQL (production mode)
 * 3. If MySQL is not available → falls back to SQLite (local mode)
 *
 * @author Shop Management System
 */
public class DatabaseSelector {

    private static DatabaseMode selectedMode = null;
    private static Properties config = null;
    private static final String CONFIG_FILE = "config.properties";

    /**
     * Gets the selected database mode (auto-detects on first call, caches result).
     *
     * @return DatabaseMode.MYSQL if MySQL is available, DatabaseMode.SQLITE otherwise
     */
    public static synchronized DatabaseMode getSelectedMode() {
        if (selectedMode == null) {
            selectedMode = detectDatabaseMode();
        }
        return selectedMode;
    }

    /**
     * Forces a specific database mode (overrides auto-detection).
     * Useful for testing or manual configuration.
     *
     * @param mode The database mode to use
     */
    public static synchronized void forceMode(DatabaseMode mode) {
        selectedMode = mode;
        util.LoggerUtil.logInfo("🔧 Database mode forced to: " + mode.getDisplayName());
    }

    /**
     * Resets the cached database mode, forcing re-detection on next call.
     */
    public static synchronized void reset() {
        selectedMode = null;
        config = null;
        util.LoggerUtil.logInfo("🔄 Database mode cache reset");
    }

    /**
     * Loads the configuration file.
     *
     * @return Properties object with configuration
     */
    public static Properties getConfig() {
        if (config == null) {
            config = loadConfig();
        }
        return config;
    }

    /**
     * Gets a configuration value.
     *
     * @param key Property key
     * @param defaultValue Default value if key not found
     * @return Property value or default
     */
    public static String getConfigValue(String key, String defaultValue) {
        return getConfig().getProperty(key, defaultValue);
    }

    /**
     * Core detection logic: attempts MySQL connection with timeout, falls back to SQLite.
     *
     * @return Detected database mode
     */
    private static DatabaseMode detectDatabaseMode() {
        Properties props = getConfig();

        // Check for forced mode in configuration
        String configMode = props.getProperty("db.mode", "AUTO").toUpperCase();
        if ("MYSQL".equals(configMode)) {
            util.LoggerUtil.logInfo("🔧 Database mode forced to MySQL via config.properties");
            return DatabaseMode.MYSQL;
        }
        if ("SQLITE".equals(configMode)) {
            util.LoggerUtil.logInfo("🔧 Database mode forced to SQLite via config.properties");
            return DatabaseMode.SQLITE;
        }

        // AUTO mode: try MySQL first, fallback to SQLite
        util.LoggerUtil.logInfo("🔍 Auto-detecting database availability...");

        if (tryMySQLConnection(props)) {
            util.LoggerUtil.logInfo("✅ MySQL detected and accessible → Running in PRODUCTION MODE (MySQL)");
            return DatabaseMode.MYSQL;
        } else {
            util.LoggerUtil.logInfo("⚠️  MySQL not available → Falling back to LOCAL MODE (SQLite)");
            util.LoggerUtil.logInfo("💡 To enable MySQL: Install MySQL Server and ensure it's running");
            return DatabaseMode.SQLITE;
        }
    }

    /**
     * Attempts a test connection to MySQL with short timeout.
     *
     * @param props Configuration properties
     * @return true if MySQL is accessible, false otherwise
     */
    private static boolean tryMySQLConnection(Properties props) {
        String host = props.getProperty("db.mysql.host", "localhost");
        String port = props.getProperty("db.mysql.port", "3306");
        String username = props.getProperty("db.mysql.username", "root");
        String password = props.getProperty("db.mysql.password", "");
        String timeout = props.getProperty("db.mysql.connection.timeout", "3000");

        // Build connection URL (without database name to test server availability)
        String allowKey = props.getProperty("db.mysql.allowPublicKeyRetrieval", "true");
        String serverUrl = String.format("jdbc:mysql://%s:%s/?useSSL=false&serverTimezone=UTC&connectTimeout=%s&allowPublicKeyRetrieval=%s",
                host, port, timeout, allowKey);

        try {
            // Load MySQL driver
            Class.forName(DatabaseMode.MYSQL.getDriverClass());

            // Attempt connection with short timeout
            try (Connection conn = DriverManager.getConnection(serverUrl, username, password)) {
                // Successfully connected to MySQL server
                return conn.isValid(2); // 2 second validation timeout
            }

        } catch (ClassNotFoundException e) {
            util.LoggerUtil.logError("⚠️  MySQL JDBC driver not found: " + e.getMessage(), null);
            return false;

        } catch (SQLException e) {
            // Connection failed - MySQL not available or credentials wrong
            String msg = e.getMessage().toLowerCase();
            if (msg.contains("communications link failure") || msg.contains("connection refused")) {
                util.LoggerUtil.logError("⚠️  MySQL server not running or not accessible", null);
            } else if (msg.contains("access denied")) {
                util.LoggerUtil.logError("⚠️  MySQL authentication failed - check username/password", null);
            } else {
                util.LoggerUtil.logError("⚠️  MySQL connection failed: " + e.getMessage(), null);
            }
            return false;

        } catch (Exception e) {
            util.LoggerUtil.logError("⚠️  Unexpected error during MySQL detection: " + e.getMessage(), null);
            return false;
        }
    }

    /**
     * Loads configuration from config.properties file.
     *
     * @return Properties object with configuration
     */
    private static Properties loadConfig() {
        Properties props = new Properties();

        try (InputStream input = DatabaseSelector.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                util.LoggerUtil.logError("⚠️  Configuration file not found: " + CONFIG_FILE, null);
                util.LoggerUtil.logError("💡 Using default configuration", null);
                return getDefaultConfig();
            }

            props.load(input);
            util.LoggerUtil.logInfo("✅ Configuration loaded from: " + CONFIG_FILE);

        } catch (IOException e) {
            util.LoggerUtil.logError("⚠️  Error loading configuration: " + e.getMessage(), null);
            util.LoggerUtil.logError("💡 Using default configuration", null);
            return getDefaultConfig();
        }

        return props;
    }

    /**
     * Provides default configuration if config.properties is not found.
     *
     * @return Properties object with default values
     */
    private static Properties getDefaultConfig() {
        Properties defaults = new Properties();

        // Default configuration
        defaults.setProperty("db.mode", "AUTO");

        // MySQL defaults
        defaults.setProperty("db.mysql.host", "localhost");
        defaults.setProperty("db.mysql.port", "3306");
        defaults.setProperty("db.mysql.database", "shop2");
        defaults.setProperty("db.mysql.username", "root");
        defaults.setProperty("db.mysql.password", "root");
        defaults.setProperty("db.mysql.connection.timeout", "3000");

        // SQLite defaults
        defaults.setProperty("db.sqlite.file", "shop_local.db");

        // Hibernate defaults
        defaults.setProperty("hibernate.show_sql", "false");
        defaults.setProperty("hibernate.format_sql", "true");
        defaults.setProperty("hibernate.hbm2ddl.auto", "update");
        defaults.setProperty("hibernate.connection.pool_size", "10");

        return defaults;
    }

    /**
     * Builds the JDBC connection URL for the current database mode.
     *
     * @param mode Database mode
     * @return JDBC connection URL
     */
    public static String buildConnectionUrl(DatabaseMode mode) {
        Properties props = getConfig();

        if (mode.isMySQL()) {
            String host = props.getProperty("db.mysql.host", "localhost");
            String port = props.getProperty("db.mysql.port", "3306");
            String database = props.getProperty("db.mysql.database", "shop2");
            String useSSL = props.getProperty("db.mysql.useSSL", "false");
            String timezone = props.getProperty("db.mysql.serverTimezone", "UTC");
            String allowKey = props.getProperty("db.mysql.allowPublicKeyRetrieval", "true");

            return String.format("jdbc:mysql://%s:%s/%s?useSSL=%s&serverTimezone=%s&allowPublicKeyRetrieval=%s",
                    host, port, database, useSSL, timezone, allowKey);

        } else { // SQLite
            String dbFile = props.getProperty("db.sqlite.file", "shop_local.db");
            return "jdbc:sqlite:" + dbFile;
        }
    }

    /**
     * Gets the database username for the current mode.
     *
     * @param mode Database mode
     * @return Username (empty string for SQLite)
     */
    public static String getUsername(DatabaseMode mode) {
        if (mode.isMySQL()) {
            return getConfig().getProperty("db.mysql.username", "root");
        }
        return ""; // SQLite doesn't use username
    }

    /**
     * Gets the database password for the current mode.
     *
     * @param mode Database mode
     * @return Password (empty string for SQLite)
     */
    public static String getPassword(DatabaseMode mode) {
        if (mode.isMySQL()) {
            return getConfig().getProperty("db.mysql.password", "");
        }
        return ""; // SQLite doesn't use password
    }
}
