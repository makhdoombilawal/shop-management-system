# Project File Inventory

This inventory documents all files in the **Shop Management System v2.0** repository, classifying them by component, purpose, and status.

## Status Classifications
- **USED**: Active file necessary for building, running, or configuring the application.
- **UNUSED (DELETED)**: Unnecessary file that is safe to delete.
- **TEMPORARY (DELETED)**: Build artifact or temporary debug file that should be deleted.
- **IGNORED**: Kept locally but excluded from version control via `.gitignore`.

---

## 1. Source Code (`src/`)

### 1.1 Entities (`src/models/entity/`)
All entities represent Hibernate mappings to the database.

| File Path | Description | Status |
|---|---|---|
| `src/models/entity/AuditLogEntity.java` | Mapping for the `audit_logs` table. | **USED** |
| `src/models/entity/BarcodeEntity.java` | Mapping for the `barcodes` table. | **USED** |
| `src/models/entity/CategoryEntity.java` | Mapping for the `categories` table. | **USED** |
| `src/models/entity/CustomerEntity.java` | Mapping for the `customers` table. | **USED** |
| `src/models/entity/EmailQueueEntity.java` | Mapping for the `email_queue` table. | **USED** |
| `src/models/entity/ProductEntity.java` | Mapping for the `products` table. | **USED** |
| `src/models/entity/RoleEntity.java` | Mapping for the `roles` table. | **USED** |
| `src/models/entity/SettingsEntity.java` | Mapping for the `settings` table. | **USED** |
| `src/models/entity/StockAuditEntity.java` | Mapping for the `stock_audits` table. | **USED** |
| `src/models/entity/SubscriptionEntity.java` | Mapping for the `app_subscription` table. | **USED** |
| `src/models/entity/SupplierEntity.java` | Mapping for the `suppliers` table. | **USED** |
| `src/models/entity/TransactionEntity.java` | Mapping for the `transactions` table. | **USED** |
| `src/models/entity/UserEntity.java` | Mapping for the `users` table. | **USED** |

### 1.2 Data Access Objects (`src/dao/`)
DAOs encapsulate Hibernate query logic.

| File Path | Description | Status |
|---|---|---|
| `src/dao/GenericDAO.java` | Common generic DAO implementation. | **USED** |
| `src/dao/AuditLogHibernateDAO.java` | Data access for `AuditLogEntity`. | **USED** |
| `src/dao/BarcodeHibernateDAO.java` | Data access for `BarcodeEntity`. | **USED** |
| `src/dao/CategoryHibernateDAO.java` | Data access for `CategoryEntity`. | **USED** |
| `src/dao/CustomerHibernateDAO.java` | Data access for `CustomerEntity`. | **USED** |
| `src/dao/EmailQueueHibernateDAO.java` | Data access for `EmailQueueEntity`. | **USED** |
| `src/dao/ProductHibernateDAO.java` | Data access for `ProductEntity`. | **USED** |
| `src/dao/RoleHibernateDAO.java` | Data access for `RoleEntity`. | **USED** |
| `src/dao/SettingsHibernateDAO.java` | Data access for `SettingsEntity`. | **USED** |
| `src/dao/StockAuditHibernateDAO.java` | Data access for `StockAuditEntity`. | **USED** |
| `src/dao/SubscriptionHibernateDAO.java` | Data access for `SubscriptionEntity`. | **USED** |
| `src/dao/SupplierHibernateDAO.java` | Data access for `SupplierEntity`. | **USED** |
| `src/dao/TransactionHibernateDAO.java` | Data access for `TransactionEntity`. | **USED** |
| `src/dao/UserHibernateDAO.java` | Data access for `UserEntity`. | **USED** |

### 1.3 Service Layer (`src/service/`)
Implements business rules and coordinates between UI frames and DAOs.

| File Path | Description | Status |
|---|---|---|
| `src/service/AuditLogService.java` | Handles system audit trail logs. | **USED** |
| `src/service/BarcodeService.java` | Handles product barcode registry. | **USED** |
| `src/service/CategoryService.java` | Category catalog service. | **USED** |
| `src/service/CustomerService.java` | Customer relationship service. | **USED** |
| `src/service/DeviceService.java` | Checks system/hardware MAC bindings. | **USED** |
| `src/service/EmailService.java` | Dispatches emails via JavaMail. | **USED** |
| `src/service/OTPService.java` | Generates and validates OTPs. | **USED** |
| `src/service/ProductService.java` | Inventory/product management. | **USED** |
| `src/service/RealWorldBarcodeService.java` | Legacy/redundant API client. Self-referencing only. | **UNUSED (DELETED)** |
| `src/service/ReportService.java` | Aggregates PDF/Excel report data. | **USED** |
| `src/service/RoleService.java` | Role definitions & permissions. | **USED** |
| `src/service/SettingsService.java` | Reads/writes app setting fields. | **USED** |
| `src/service/StockAuditService.java` | Manages manual stock audit inspections. | **USED** |
| `src/service/SubscriptionService.java` | Checks, validates, and activates subscriptions. | **USED** |
| `src/service/SupplierService.java` | Supplier relations and supply log. | **USED** |
| `src/service/TransactionService.java` | Registers cashier purchases and orders. | **USED** |
| `src/service/UserService.java` | User creation, verification, and hash checking. | **USED** |

### 1.4 User Interface (`src/frames/`)
Java Swing frames/panels.

| File Path | Description | Status |
|---|---|---|
| `src/frames/BaseFrame.java` | Parent Swing class containing window setup helper routines. | **USED** |
| `src/frames/Login.java` | Login screen frame. | **USED** |
| `src/frames/Login.form` | Swing form file for GUI builder. | **USED** |
| `src/frames/DashboardEnterprise.java` | Main workspace hub frame. | **USED** |
| `src/frames/AdminSubscriptionPanel.java` | Subscription tracking page inside dashboard. | **USED** |
| `src/frames/AuditLogViewerEnterprise.java` | Window listing system audit log tracks. | **USED** |
| `src/frames/BarcodeEnterprise.java` | Dialog/pane for generation and printing of barcodes. | **USED** |
| `src/frames/CashTransactionEnterprise.java` | Main Point of Sale checkout form. | **USED** |
| `src/frames/CategoryManagementEnterprise.java` | Dialogue managing categories catalog. | **USED** |
| `src/frames/CustomerEnterprise.java` | Interface for tracking customer files. | **USED** |
| `src/frames/OTPEntryDialog.java` | Pop-up dialog requesting two-factor verification OTP code. | **USED** |
| `src/frames/ProductEnterprise.java` | Interface managing products catalog. | **USED** |
| `src/frames/PurchaseOrderEnterprise.java` | Interface managing supplier purchase files. | **USED** |
| `src/frames/ReportEnterprise.java` | Graphical reports center dashboard. | **USED** |
| `src/frames/RoleManagementEnterprise.java` | Form for configuring user role permissions. | **USED** |
| `src/frames/SettingsEnterprise.java` | Form for updating system configuration fields. | **USED** |
| `src/frames/StockAuditViewerEnterprise.java` | Auditing screen showing inventory adjustments. | **USED** |
| `src/frames/SubscriptionExpiryDialog.java` | Popup informing user of subscription lock. | **USED** |
| `src/frames/SupplierEnterprise.java` | Pane for managing supplier contact directories. | **USED** |
| `src/frames/TransactionEnterprise.java` | Searchable grid viewer of historical sales. | **USED** |
| `src/frames/UserEnterprise.java` | User manager frame. | **USED** |

### 1.5 Utilities (`src/util/`)
Utility and helper classes.

| File Path | Description | Status |
|---|---|---|
| `src/util/AppConfig.java` | Configurations wrapper. | **USED** |
| `src/util/DatabaseMode.java` | DB selector mode enum. | **USED** |
| `src/util/DatabaseSelector.java` | Utility choosing database schema. | **USED** |
| `src/util/DateUtil.java` | Format helper functions. | **USED** |
| `src/util/DbAuditInspector.java` | Standalone debug tool, self-referencing only. | **UNUSED (DELETED)** |
| `src/util/DbAuditInspector.class` | Compiled class file present in the src tree. | **UNUSED (DELETED)** |
| `src/util/DbBootstrapper.java` | Database tables & constraint auto-creator. | **USED** |
| `src/util/EmailQueueProcessor.java` | Async processor sending queued emails. | **USED** |
| `src/util/EnterpriseBackup.java` | Standalone backup engine (manual/command line), never referenced. | **UNUSED (DELETED)** |
| `src/util/EnterpriseTheme.java` | Swing FlatLaf skin initialization utility. | **USED** |
| `src/util/HibernateConfigLocator.java` | Old helper class checking configuration locations, never imported. | **UNUSED (DELETED)** |
| `src/util/HibernateUtil.java` | Creates and shares Hibernate session factories. | **USED** |
| `src/util/InternetConnectivityUtil.java` | Utility checking connection for email tasks. | **USED** |
| `src/util/LoggerUtil.java` | Logging wrapper. | **USED** |
| `src/util/OTPUtil.java` | Helper for email otp. | **USED** |
| `src/util/PasswordUtil.java` | Bcrypt hashing wrapper. | **USED** |
| `src/util/SecurityUtil.java` | Encryption wrapper routines. | **USED** |
| `src/util/SMTPUtil.java` | Mail server configuration provider. | **USED** |
| `src/util/SQLiteDialect.java` | Standard SQLite custom dialect class. | **USED** |
| `src/util/ValidationUtil.java` | Common fields validations checks. | **USED** |

### 1.6 Configuration & Main Entry (`src/`)
| File Path | Description | Status |
|---|---|---|
| `src/config.properties` | Backup/fallback config template. | **USED** |
| `src/hibernate.cfg.xml` | Primary Hibernate settings file. | **USED** |
| `src/shop/Shop.java` | Application main entry point class. | **USED** |

---

## 2. Configuration & Build Files (Root)

| File Path | Description | Status |
|---|---|---|
| `pom.xml` | Maven build specification file. | **USED** |
| `.classpath` | Eclipse project classpath. | **USED** |
| `.project` | Eclipse project descriptor. | **USED** |
| `.settings/` | Directory containing Eclipse/WTP configuration options. | **USED** |
| `config.properties` | Dynamic application runtime settings. | **USED** |
| `SMTPConfig.properties` | Sensitive SMTP mail configurations. | **IGNORED / SECURED** |
| `manifest.mf` | Standalone executable execution manifest details. | **USED** |
| `launch4j-config.xml` | Setup wrapper converting jar to exe. | **USED** |
| `installer_setup_enterprise.iss` | Inno Setup installer compilation script. | **USED** |
| `LICENSE.txt` | License file template. | **USED** |

---

## 3. Scripts

| File Path | Description | Status |
|---|---|---|
| `build.bat` | Short batch file compiler. | **USED** |
| `build-automated.ps1` | Automation script for compilation. | **USED** |
| `build-executable-PRODUCTION.bat` | Production executable builder script. | **USED** |
| `build-installer.bat` | Script compiling Inno Setup installer. | **USED** |
| `cleanup_and_build.ps1` | Developer script clean-rebuilding app. | **USED** |
| `create_installer.ps1` | Inno Setup launcher script. | **USED** |
| `download-dependencies.ps1` | Automated dependencies retriever. | **USED** |
| `find_iscc.ps1` | Utility locating Inno Setup compiler paths. | **USED** |
| `install.bat` | Runs local setups. | **USED** |
| `run-app.bat` | Script running main class via classpath. | **USED** |
| `run-app-jar.bat` | Script executing built package jar. | **USED** |

---

## 4. Libraries (`lib/`)
All libraries listed in `lib/` are required compile-time/runtime dependencies.

| File Path | Description | Status |
|---|---|---|
| `lib/*.jar` | Java package dependencies (Hibernate, MySQL connector, FlatLaf, etc.). | **USED** |

---

## 5. Resources (`resources/`)

| File Path | Description | Status |
|---|---|---|
| `resources/config/application.properties` | Default configurations resources template. | **USED** |
| `resources/icons/*` | Icon visual resources (shop, database, help, splash). | **USED** |
| `resources/installer/PREINSTALL_INFO.txt` | Readme information shown before setup starts. | **USED** |
| `resources/installer/POSTINSTALL_INFO.txt` | Information shown when setup finishes. | **USED** |
| `resources/launchers/launch4j_config.xml` | Internal XML template for launch4j build. | **USED** |
| `resources/launchers/ShopManager.bat` | Fallback batch execution tool. | **USED** |
| `resources/launchers/ShopManager.exe` | Compiled executable wrapper launcher. | **USED** |
| `resources/launchers/ShopManager.vbs` | Silent launcher wrapper utility. | **USED** |
| `resources/scripts/db_init.ps1` | SQLite/MySQL startup database configuration tool. | **USED** |
| `resources/scripts/initialize_database.bat` | Command-line db bootstrap initiator. | **USED** |

---

## 6. SQL scripts (`sql/`)

| File Path | Description | Status |
|---|---|---|
| `sql/enterprise_schema_complete.sql` | Legacy consolidated database database script. | **UNUSED (DELETED)** |
| `sql/backup_app_license.sql` | Leftover script from deprecated license manager. | **UNUSED (DELETED)** |
| `sql/drop_app_license.sql` | Leftover script from deprecated license manager. | **UNUSED (DELETED)** |
| `sql/rollback_app_license.sql` | Leftover script from deprecated license manager. | **UNUSED (DELETED)** |
| `sql/enhancements_optional.sql` | Obsolete/optional table improvements script. | **UNUSED (DELETED)** |
| `sql/device_binding_otp_alter.sql` | Migration adding OTP verification support. | **USED (MIGRATED)** |
| `sql/subscription_alter.sql` | Migration adding subscription table column details. | **USED (MIGRATED)** |
| `sql/subscription_stability_alter.sql` | Migration adding license tracking indices. | **USED (MIGRATED)** |

---

## 7. Temporary, Legacy & Output Files (To Be Cleaned)

| File Path | Description / Location | Status |
|---|---|---|
| `BUILD_AND_TEST_GUIDE.md` | Dev documentation file at project root. | **UNUSED (DELETED)** |
| `DATABASE_AUDIT_REPORT.md` | Dev documentation file at project root. | **UNUSED (DELETED)** |
| `DEPLOYMENT_GUIDE.md` | Dev documentation file at project root. | **UNUSED (DELETED)** |
| `DOCUMENTATION_INDEX.md` | Dev documentation file at project root. | **UNUSED (DELETED)** |
| `ENTERPRISE_SUBSCRIPTION_IMPLEMENTATION.md` | Dev documentation file at project root. | **UNUSED (DELETED)** |
| `ERROR_FIXES_REPORT.md` | Dev documentation file at project root. | **UNUSED (DELETED)** |
| `FINAL_STATUS_REPORT.md` | Dev documentation file at project root. | **UNUSED (DELETED)** |
| `IMPLEMENTATION_REPORT.md` | Dev documentation file at project root. | **UNUSED (DELETED)** |
| `MYSQL_DEPLOYMENT_SOLUTION.md` | Dev documentation file at project root. | **UNUSED (DELETED)** |
| `QUICK_START_MYSQL.md` | Dev documentation file at project root. | **UNUSED (DELETED)** |
| `SUBSCRIPTION_QUICK_REFERENCE.md` | Dev documentation file at project root. | **UNUSED (DELETED)** |
| `build.log` | Raw build logging output. | **TEMPORARY (DELETED)** |
| `db_audit_output.txt` | Standalone DB audit check logging output. | **TEMPORARY (DELETED)** |
| `sources_manual.txt` | Class list helper file. | **TEMPORARY (DELETED)** |
| `dist/shop-management.jar.backup` | Duplicate backup artifact. | **TEMPORARY (DELETED)** |
| `dist/installer/ShopManager_Installer_v2.0.exe` | Compiled setup binary (31MB). | **TEMPORARY (DELETED)** |
| `scratch/` | Directory with old temporary query files. | **TEMPORARY (DELETED)** |
| `temp_build/` | Empty directory wrapper. | **TEMPORARY (DELETED)** |
| `bin/` | Duplicate compiler classes cache. | **TEMPORARY (DELETED)** |
| `ShopManagement/` | 31MB local jpackage execution bundle. | **TEMPORARY (DELETED)** |
| `barcodes/*.png` | Cache of barcode images generated locally. | **TEMPORARY (DELETED)** |
