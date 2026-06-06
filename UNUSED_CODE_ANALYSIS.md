# Unused Code Analysis

This report documents the analysis and validation process used to identify unused codebase assets in the **Shop Management System v2.0**.

## 1. Methodology
To ensure no essential code was removed, a multi-step verification process was executed:
1. **Repository-Wide Static Analysis**: Code search was conducted to identify imports or references to every class.
2. **Self-Reference Filtering**: Classes that only import or refer to themselves or their own static main methods (such as standalone debug scripts) were flagged.
3. **Compilation Verification**: Compilation maps were checked to verify that disabling these classes does not impact other application modules.

---

## 2. Unused Java Source Classes

The following classes were verified as completely unused by the application.

### 2.1 `src/util/DbAuditInspector.java`
- **Purpose**: Standalone inspection utility with its own `main` method, designed to test entity table columns and database compatibility.
- **Analysis**: Never imported or referenced by any core services or GUI views. 
- **Recommendation**: Safe to delete. The compiled class `src/util/DbAuditInspector.class` should also be deleted.

### 2.2 `src/util/EnterpriseBackup.java`
- **Purpose**: CLI backup runner tool with its own `main` method, meant to trigger file-copy database backups.
- **Analysis**: Never imported by services or UI. Backup logic in the production application is handled by other utilities.
- **Recommendation**: Safe to delete.

### 2.3 `src/util/HibernateConfigLocator.java`
- **Purpose**: Helper configuration class locating `hibernate.cfg.xml`.
- **Analysis**: Legacy code. The active session factory configuration now uses standard resource streams or direct properties bindings defined in `HibernateUtil`.
- **Recommendation**: Safe to delete.

### 2.4 `src/service/RealWorldBarcodeService.java`
- **Purpose**: Redundant external service class targeting third-party barcode endpoints.
- **Analysis**: Standard barcodes are generated locally using the offline class `helper/BarcodeGenerator` (which leverages barcode4j). This service class is never instantiated or called.
- **Recommendation**: Safe to delete.

---

## 3. Partially Unused Assets & Exceptions (Kept Intentionally)

Certain classes might appear unused during automatic dependency analysis but must be preserved for runtime configuration or architecture compatibility.

- **`src/util/SQLiteDialect.java`**: Custom Hibernate SQL dialect helper. Essential for SQLite support even if there are no direct compile imports in active classes, as it is dynamically loaded at runtime when configured in `hibernate.cfg.xml`.
- **`src/util/DatabaseMode.java`** & **`src/util/DatabaseSelector.java`**: Support dual database modes (SQLite & MySQL). Required.
- **`src/service/DeviceService.java`**: Hardware identifier reader utility. Instantiated by `SubscriptionService` during MAC-address binding checks. Required.
