# Shop Management System - Administrator Manual

This guide covers system administration, database setup, configuration, security hardening, user management, and maintenance for the **Shop Management System (Enterprise Edition)**.

---

## Table of Contents
1. [System Requirements](#1-system-requirements)
2. [Database Architecture & Setup](#2-database-architecture--setup)
3. [Configuration Files](#3-configuration-files)
4. [User Administration & RBAC](#4-user-administration--rbac)
5. [Security & Authentication Hardening](#5-security--authentication-hardening)
6. [Logging & Auditing](#6-logging--auditing)
7. [Backup & Data Integrity](#7-backup--data-integrity)

---

## 1. System Requirements

- **Operating System**: Windows 10/11 (64-bit recommended) or Windows Server 2016+
- **Java Runtime**: JDK / JRE 17 or 21 (64-bit)
- **Database Options**:
  - **Embedded (Default / Offline)**: SQLite (zero configuration, stored in `{app}/data/shop_management.db`)
  - **Enterprise Client-Server**: MySQL 8.0+ / MariaDB 10.5+
- **Memory**: Minimum 2 GB RAM (4 GB recommended)
- **Disk Space**: 200 MB for application files; additional space for database growth.

---

## 2. Database Architecture & Setup

### Dual-Database Auto-Detection
The application features auto-detection fallback (`DatabaseSelector.java` and `HibernateUtil.java`):
1. **Primary**: MySQL Database.
2. **Fallback**: SQLite Local Database. If MySQL connection fails or credentials are empty, the system gracefully falls back to embedded SQLite.

### MySQL Setup Instructions
1. Install MySQL Server 8.0+.
2. Execute initialization script:
   ```sql
   CREATE DATABASE IF NOT EXISTS shop_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   CREATE USER IF NOT EXISTS 'shop_user'@'localhost' IDENTIFIED BY 'SecurePass123!';
   GRANT ALL PRIVILEGES ON shop_management.* TO 'shop_user'@'localhost';
   FLUSH PRIVILEGES;
   ```
3. Run the schema creation scripts in `sql/tables/` followed by seeding scripts in `sql/seed/`.

---

## 3. Configuration Files

Configuration is managed via properties files in `config/`:

### `config/config.properties`
```properties
# Database connection settings
db.type=mysql
db.host=localhost
db.port=3306
db.name=shop_management
db.user=shop_user
db.password=SecurePass123!

# Application settings
tax.rate=10.0
company.name=Shop Management System
company.address=Main Retail Center
company.phone=+1-800-555-0199
```

### `config/hibernate.cfg.xml`
Defines Hibernate session factory, dialect, connection pool settings (`C3P0` / Hikari), and entity mappings.

---

## 4. User Administration & RBAC

The system enforces **Role-Based Access Control (RBAC)** across three primary roles:

| Role | Permissions & Access Scope |
| :--- | :--- |
| **SUPER_ADMIN** | Unrestricted access across all modules, configuration, and subscription licensing. |
| **ADMIN** | Full administrative rights: User management, role management, settings, inventory, reporting. |
| **MANAGER** | Product management, customer management, supplier orders, inventory reports, stock audit viewer. Cannot manage users. |
| **CASHIER** | POS terminal checkout, product search, customer lookup, new customer registration. |

---

## 5. Security & Authentication Hardening

- **BCrypt Password Hashing**: Passwords are saved as standard BCrypt hashes (`$2a$12$...`). Legacy SHA-256 hashes are automatically upgraded to BCrypt upon user login.
- **Account Lockout**: 5 failed login attempts lock the targeted user account for 15 minutes (`SecurityUtil.java`).
- **Session Timeout**: Users inactive for 30+ minutes are automatically logged out to prevent unauthorized terminal access.
- **No Backdoors**: Hardcoded backdoor accounts and default passwords have been completely removed.

---

## 6. Logging & Auditing

- **Logback SLF4J Logger**: Application events and errors are logged to `logs/shopmanagement.log` with automatic rolling file appender (10MB size limit, 30 days history).
- **System Audit Log**: Every database mutation (CREATE, UPDATE, DELETE) is captured in the `audit_logs` table with user timestamp, IP address, old values, and new values. Viewable under **System Audit Log** frame.

---

## 7. Backup & Data Integrity

### Database Backup
- **SQLite**: Simply back up the `{app}/data/shop_management.db` file while application is closed.
- **MySQL**: Run standard MySQL dump:
  ```bash
  mysqldump -u shop_user -p shop_management > backup_shop_management.sql
  ```
- **Installer Safety**: Upgrading or uninstalling via Inno Setup preserves user database files (`{app}/data`) and user custom properties (`config.properties`).
