# Database Usage Report

This report presents a validation matrix cross-referencing all 13 JPA/Hibernate entities in the **Shop Management System v2.0** with their respective Data Access Objects (DAOs), business services, and database tables.

## 1. Entity-DAO-Service-Table Matrix

| # | JPA Entity Class | Hibernate DAO Class | Business Service Class | Database Table |
|---|---|---|---|---|
| 1 | `AuditLogEntity` | `AuditLogHibernateDAO` | `AuditLogService` | `audit_log` |
| 2 | `BarcodeEntity` | `BarcodeHibernateDAO` | `BarcodeService` | `barcode` |
| 3 | `CategoryEntity` | `CategoryHibernateDAO` | `CategoryService` | `categories` |
| 4 | `CustomerEntity` | `CustomerHibernateDAO` | `CustomerService` | `customers` |
| 5 | `EmailQueueEntity` | `EmailQueueHibernateDAO` | `EmailService` | `email_queue` |
| 6 | `ProductEntity` | `ProductHibernateDAO` | `ProductService` | `products` |
| 7 | `RoleEntity` | `RoleHibernateDAO` | `RoleService` | `roles` |
| 8 | `SettingsEntity` | `SettingsHibernateDAO` | `SettingsService` | `settings` |
| 9 | `StockAuditEntity` | `StockAuditHibernateDAO` | `StockAuditService` | `stock_audit` |
| 10 | `SubscriptionEntity` | `SubscriptionHibernateDAO` | `SubscriptionService` | `app_subscription` |
| 11 | `SupplierEntity` | `SupplierHibernateDAO` | `SupplierService` | `suppliers` |
| 12 | `TransactionEntity` | `TransactionHibernateDAO` | `TransactionService` | `transactions` |
| 13 | `UserEntity` | `UserHibernateDAO` | `UserService` | `users` |

---

## 2. Validation & Column Mapping Audits

Every entity class has been audited to confirm it mirrors the primary SQL schema tables.

### 2.1 Column Type Compatibility
- **Numeric IDs**: All auto-incrementing surrogate keys use Hibernate `GenerationType.IDENTITY` corresponding to `INT AUTO_INCREMENT` (MySQL) or `INTEGER AUTOINCREMENT` (SQLite).
- **Date/Time Columns**: 
  - `LocalDate` properties (e.g. `installDate`, `expiryDate`) correspond to standard `DATE` fields.
  - `LocalDateTime` properties (e.g. `createdAt`, `updatedAt`, `otpCreatedAt`) correspond to `DATETIME` or `TIMESTAMP` fields.
- **Enumerations**: Custom enum fields like `status` or `role` are stored in database columns as string text types, with validation constraints enforced at the service layer or database column DDL.
- **Large Text Fields**: Long structures (such as email `body`) use Hibernate `@Column(columnDefinition = "LONGTEXT")` to support heavy content payloads.
