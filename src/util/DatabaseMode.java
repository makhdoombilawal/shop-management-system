package util;

/**
 * Enum representing supported database modes for the application.
 *
 * The application automatically detects MySQL availability and falls back
 * to SQLite if MySQL is not available (not installed or not running).
 *
 * @author Shop Management System
 */
public enum DatabaseMode {

    /**
     * MySQL database mode (production, requires MySQL Server installed and running)
     */
    MYSQL("MySQL", "com.mysql.cj.jdbc.Driver", "org.hibernate.dialect.MySQL8Dialect"),

    /**
     * SQLite database mode (fallback, local file-based, no server required)
     */
    SQLITE("SQLite", "org.sqlite.JDBC", "util.SQLiteDialect");

    private final String displayName;
    private final String driverClass;
    private final String hibernateDialect;

    DatabaseMode(String displayName, String driverClass, String hibernateDialect) {
        this.displayName = displayName;
        this.driverClass = driverClass;
        this.hibernateDialect = hibernateDialect;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDriverClass() {
        return driverClass;
    }

    public String getHibernateDialect() {
        return hibernateDialect;
    }

    public boolean isMySQL() {
        return this == MYSQL;
    }

    public boolean isSQLite() {
        return this == SQLITE;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
