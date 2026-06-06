package util;

import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.dialect.function.VarArgsSQLFunction;
import org.hibernate.type.StandardBasicTypes;

import java.sql.Types;

/**
 * Custom SQLite dialect for Hibernate 5.x.
 *
 * Provides SQLite-specific SQL generation and type mappings for Hibernate ORM.
 * Required because Hibernate 5.x does not include built-in SQLite support.
 *
 * @author Shop Management System
 */
public class SQLiteDialect extends Dialect {

    public SQLiteDialect() {
        super();

        // Register column type mappings
        registerColumnType(Types.BIT, "integer");
        registerColumnType(Types.TINYINT, "tinyint");
        registerColumnType(Types.SMALLINT, "smallint");
        registerColumnType(Types.INTEGER, "integer");
        registerColumnType(Types.BIGINT, "bigint");
        registerColumnType(Types.FLOAT, "float");
        registerColumnType(Types.REAL, "real");
        registerColumnType(Types.DOUBLE, "double");
        registerColumnType(Types.NUMERIC, "numeric");
        registerColumnType(Types.DECIMAL, "decimal");
        registerColumnType(Types.CHAR, "char");
        registerColumnType(Types.VARCHAR, "varchar");
        registerColumnType(Types.LONGVARCHAR, "longvarchar");
        registerColumnType(Types.DATE, "date");
        registerColumnType(Types.TIME, "time");
        registerColumnType(Types.TIMESTAMP, "timestamp");
        registerColumnType(Types.BINARY, "blob");
        registerColumnType(Types.VARBINARY, "blob");
        registerColumnType(Types.LONGVARBINARY, "blob");
        registerColumnType(Types.BLOB, "blob");
        registerColumnType(Types.CLOB, "clob");
        registerColumnType(Types.BOOLEAN, "integer");

        // Register common functions
        registerFunction("concat", new VarArgsSQLFunction(StandardBasicTypes.STRING, "", "||", ""));
        registerFunction("mod", new SQLFunctionTemplate(StandardBasicTypes.INTEGER, "?1 % ?2"));
        registerFunction("substr", new StandardSQLFunction("substr", StandardBasicTypes.STRING));
        registerFunction("substring", new StandardSQLFunction("substr", StandardBasicTypes.STRING));
    }

    // Identity column support
    
    public boolean supportsIdentityColumns() {
        return true;
    }

    
    public boolean hasDataTypeInIdentityColumn() {
        return false;
    }

    
    public String getIdentityColumnString() {
        return "integer";
    }

    
    public String getIdentitySelectString() {
        return "select last_insert_rowid()";
    }

    // Table operations
    
    public boolean supportsIfExistsBeforeTableName() {
        return true;
    }

    
    public boolean supportsIfExistsAfterTableName() {
        return false;
    }

    public String getDropTableString(String tableName) {
        return "drop table if exists " + tableName;
    }

    // Constraint support
    
    public boolean supportsColumnCheck() {
        return false;
    }

    
    public boolean supportsTableCheck() {
        return false;
    }

    
    public boolean supportsCascadeDelete() {
        return false;
    }

    // Timestamp operations
    
    public boolean supportsCurrentTimestampSelection() {
        return true;
    }

    
    public boolean isCurrentTimestampSelectStringCallable() {
        return false;
    }

    
    public String getCurrentTimestampSelectString() {
        return "select current_timestamp";
    }

    // Join support
    
    public boolean supportsUnionAll() {
        return true;
    }

    
    public boolean supportsOuterJoinForUpdate() {
        return false;
    }

    // ALTER TABLE support
    
    public boolean hasAlterTable() {
        return false; // SQLite has limited ALTER TABLE support
    }

    
    public boolean dropConstraints() {
        return false;
    }

    
    public String getAddColumnString() {
        return "add column";
    }

    // Locking and updates
    
    public String getForUpdateString() {
        return ""; // SQLite doesn't support SELECT FOR UPDATE
    }

    public boolean supportsLockTimeouts() {
        return false;
    }

    // Foreign key support
    
    public String getDropForeignKeyString() {
        throw new UnsupportedOperationException("SQLite doesn't support dropping foreign keys");
    }

    
    public String getAddForeignKeyConstraintString(String constraintName,
                                                    String[] foreignKey,
                                                    String referencedTable,
                                                    String[] primaryKey,
                                                    boolean referencesPrimaryKey) {
        throw new UnsupportedOperationException("SQLite handles foreign keys at table creation");
    }

    
    public String getAddPrimaryKeyConstraintString(String constraintName) {
        throw new UnsupportedOperationException("SQLite handles primary keys at table creation");
    }

    // Pagination support
    
    public boolean supportsLimit() {
        return true;
    }

    
    public boolean supportsLimitOffset() {
        return true;
    }

    
    public boolean supportsVariableLimit() {
        return true;
    }

    
    public boolean bindLimitParametersInReverseOrder() {
        return false;
    }

    
    public boolean bindLimitParametersFirst() {
        return false;
    }

    
    public String getLimitString(String query, boolean hasOffset) {
        return query + (hasOffset ? " limit ? offset ?" : " limit ?");
    }

    
    public boolean useMaxForLimit() {
        return false;
    }

    // Temporary tables
    
    public boolean supportsTemporaryTables() {
        return true;
    }

    
    public String getCreateTemporaryTableString() {
        return "create temporary table if not exists";
    }

    public String getDropTemporaryTableString() {
        return "drop table if exists";
    }

    public Boolean performTemporaryTableDDLInIsolation() {
        return Boolean.FALSE;
    }

    
    public boolean dropTemporaryTableAfterUse() {
        return false;
    }

    // Unique constraints
    
    public boolean supportsUnique() {
        return true;
    }

    
    public boolean supportsUniqueConstraintInCreateAlterTable() {
        return false;
    }

    // Boolean handling
    
    public String toBooleanValueString(boolean bool) {
        return bool ? "1" : "0";
    }

    // Sequence support
    
    public boolean supportsSequences() {
        return false;
    }

    // Tuple support
    public boolean supportsRowValueConstructorSyntax() {
        return false;
    }

    // Case sensitivity
    public boolean areStringComparisonsCaseInsensitive() {
        return true;
    }
}

