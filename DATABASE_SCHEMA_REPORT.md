# Database Schema Report

This report documents the restructured database directory layout, schema definitions, and seed data scripts for the **Shop Management System v2.0**.

## 1. Directory Structure

The `sql/` directory has been reorganized into a standardized modular layout:
```text
sql/
├── migrations/
│   ├── device_binding_otp_alter.sql       -- OTP & MAC bindings columns
│   ├── subscription_alter.sql              -- Lockout and fingerprint columns
│   └── subscription_stability_alter.sql    -- Email dedup & stability flags
├── schema/
│   ├── complete_schema.sql                 -- Full production script (Tables, Views, Procedures, Seed)
│   ├── mysql_schema.sql                    -- Pure MySQL tables and constraints DDL
│   └── sqlite_schema.sql                   -- SQLite-compatible fallback tables DDL
├── seed/
│   ├── default_categories.sql              -- Default inventory category inserts
│   ├── default_roles.sql                   -- Default access control roles inserts
│   ├── default_settings.sql                -- Default company settings insert
│   └── default_users.sql                   -- Default users credentials inserts
└── tables/
    ├── app_subscription.sql                -- app_subscription table schema
    ├── audit_logs.sql                      -- audit_log table schema
    ├── barcodes.sql                        -- barcode table schema
    ├── categories.sql                      -- categories table schema
    ├── customers.sql                       -- customers table schema
    ├── email_queue.sql                     -- email_queue table schema
    ├── products.sql                        -- products table schema
    ├── roles.sql                           -- roles table schema
    ├── settings.sql                        -- settings table schema
    ├── stock_audits.sql                    -- stock_audit table schema
    ├── suppliers.sql                       -- suppliers table schema
    ├── transactions.sql                    -- transactions table schema
    └── users.sql                           -- users table schema
```

---

## 2. Table Definitions (13 Core Tables)

| Table | JPA Entity | Purpose |
|---|---|---|
| `roles` | `RoleEntity` | Roles access levels (ADMIN, MANAGER, CASHIER). |
| `suppliers` | `SupplierEntity` | External purchasing source profiles. |
| `users` | `UserEntity` | Employee credentials and account state. |
| `categories` | `CategoryEntity` | Hierarchical products catalog grouping. |
| `products` | `ProductEntity` | Standard catalog stock levels and unit prices. |
| `customers` | `CustomerEntity` | CRM customer tracking and reward loyalty points. |
| `transactions` | `TransactionEntity` | Log of point-of-sale registers, purchase logs. |
| `barcode` | `BarcodeEntity` | Track specific unique product serial barcodes. |
| `stock_audit` | `StockAuditEntity` | Track all system/manual stock levels changes. |
| `audit_log` | `AuditLogEntity` | Event auditor tracking employee actions. |
| `settings` | `SettingsEntity` | Global key-value system settings parameters. |
| `app_subscription` | `SubscriptionEntity` | License locking, machine security bindings. |
| `email_queue` | `EmailQueueEntity` | Dispatcher storage of offline notification emails. |

---

## 3. Database Adaptability (Dual-Mode Support)

The application supports both enterprise MySQL and file-fallback SQLite databases:
- **MySQL**: Deployed in production. Utilizes advanced features (Stored Procedures for transactions execution, automatic database triggers for price change logs, and Foreign Keys constraint management).
- **SQLite**: Automatic developer/local fallback database mode. Leverages lightweight, file-based schema architecture.
