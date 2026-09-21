# Shop Management System - Developer Guide

This guide provides technical reference for developers maintaining, extending, or building the **Shop Management System (Enterprise Edition)**.

---

## Table of Contents
1. [Architecture Overview](#1-architecture-overview)
2. [Package Structure](#2-package-structure)
3. [Database & Data Access (Hibernate DAO)](#3-database--data-access-hibernate-dao)
4. [Atomic Transactions](#4-atomic-transactions)
5. [Security & Authentication Architecture](#5-security--authentication-architecture)
6. [UI Layer (Java Swing & FlatLaf)](#6-ui-layer-java-swing--flatlaf)
7. [Build & Release Process](#7-build--release-process)

---

## 1. Architecture Overview

The system is built as a modular 3-tier Java Swing desktop application:
- **Presentation Layer**: Java Swing frames (`src/frames/`) styled with `FlatLaf` (Light/Dark themes) and `EnterpriseTheme`.
- **Service Layer**: Business logic services (`src/service/`) validating data and coordinating multi-DAO transactions.
- **Data Access Layer**: Generic Hibernate DAOs (`src/dao/`) interacting with JPA/Hibernate entities (`src/models/entity/`).

```
+-------------------------------------------------------+
|                Java Swing UI Frames                   |
| (Dashboard, POS, Product, Customer, Reports, etc.)    |
+---------------------------+---------------------------+
                            |
                            v
+-------------------------------------------------------+
|                    Service Layer                      |
| (TransactionService, UserService, ProductService, etc)|
+---------------------------+---------------------------+
                            |
                            v
+-------------------------------------------------------+
|                   Hibernate DAO                       |
| (GenericDAO, ProductDAO, CustomerDAO, TransactionDAO) |
+---------------------------+---------------------------+
                            |
                            v
+-------------------------------------------------------+
|               MySQL / SQLite Database                 |
+-------------------------------------------------------+
```

---

## 2. Package Structure

```
src/
├── dao/                  # Data Access Objects (Hibernate JPA)
│   ├── GenericDAO.java
│   ├── ProductHibernateDAO.java
│   ├── CustomerHibernateDAO.java
│   ├── UserHibernateDAO.java
│   └── TransactionHibernateDAO.java
├── frames/               # Swing GUI Enterprise Frames
│   ├── BaseFrame.java
│   ├── DashboardEnterprise.java
│   ├── CashTransactionEnterprise.java
│   ├── ProductEnterprise.java
│   ├── UserEnterprise.java
│   └── ReportEnterprise.java
├── models/               # Domain Models & Session Context
│   ├── Session.java
│   └── entity/           # JPA Annotations for DB Entities
├── service/              # Business Logic & Validation Services
│   ├── TransactionService.java
│   ├── ProductService.java
│   └── UserService.java
├── util/                 # Cross-Cutting Utilities & Bootstrapping
│   ├── HibernateUtil.java
│   ├── DatabaseSelector.java
│   ├── PasswordUtil.java
│   ├── SecurityUtil.java
│   └── LoggerUtil.java
└── shop/
    └── Shop.java         # Main Application Launcher
```

---

## 3. Database & Data Access (Hibernate DAO)

- **`GenericDAO<T, ID>`**: Abstract DAO providing base CRUD operations (`save`, `update`, `delete`, `findById`, `findAll`).
- **Session-Aware Overloads**: `save(T entity, Session session)` and `update(T entity, Session session)` allow passing explicit Hibernate sessions for multi-DAO atomic transactions.

---

## 4. Atomic Transactions

Critical operations like **`processSale`** and **`processPurchase`** in `TransactionService.java` run within single Hibernate transaction blocks to guarantee data integrity across products, customer accounts, and transaction logs:

```java
Transaction tx = session.beginTransaction();
try {
    // 1. Save Transaction Entity
    transactionDAO.save(transaction, session);
    // 2. Decrease Stock
    productDAO.decreaseStock(productId, quantity, session);
    // 3. Update Customer Purchase Totals
    customerDAO.updatePurchaseInfo(customerId, totalAmount, session);
    
    tx.commit();
} catch (Exception e) {
    if (tx != null && tx.getStatus().canRollback()) tx.rollback();
    throw e;
}
```

---

## 5. Security & Authentication Architecture

- **Built-in Super Admin Authentication (`UserService.java`)**:
  - Exact credential verification for built-in Super Admin account (`Bilawal`).
  - Username comparison is case-insensitive (`"Bilawal".equalsIgnoreCase(username)`).
  - Password comparison is strictly exact and case-sensitive (`"breakthewall".equals(password)`).
  - Integrates with `SecurityUtil` lockout: 5 consecutive failed login attempts lock the account.
  - Successfully authenticates as `SUPER_ADMIN` role with full system RBAC privileges.
  - No credentials printed in UI, logs, reports, or installer popups.

- **Password Hashing (`PasswordUtil.java`)**:
  - `hashPassword(plain)`: Uses BCrypt (`BCrypt.hashpw(plain, BCrypt.gensalt(12))`).
  - `checkPassword(plain, hashed)`: Supports both BCrypt (`$2a$`) and legacy SHA-256 (`salt$hash`) verification.
  - Automatic migration upgrades legacy hashes to BCrypt upon successful login.
- **Session Context (`models/Session.java`)**:
  - Global static session holding authenticated user entity, role, and permissions.
  - Automatically expires after 30 minutes of inactivity (`MAX_INACTIVE_INTERVAL_MS`).

---

## 6. UI Layer (Java Swing & FlatLaf)

- **`BaseFrame.java`**: Parent frame for all application windows. Checks `Session.isLoggedIn()` and `authorized` flag upon construction.
- **`EnterpriseTheme.java`**: Centralized design system with standard color tokens, typography, button hover effects, and smooth table styling.
- **Thread Safety**: Heavy database operations use `SwingWorker` or `CompletableFuture` to run off the Event Dispatch Thread (EDT).

---

## 7. Build & Release Process

### Automated Compilation Batch Script
Run `build-executable-PRODUCTION.bat` from project root:
- Cleans build outputs (`build/` and `dist/`).
- Compiles Java source files with dependency classpath.
- Bundles `dist/shop-management.jar`.
- Copies libraries into `dist/lib/`.
- Generates `ShopManagement.bat` and `ShopManagement.ps1` launch scripts.

### Installer Compilation (Inno Setup)
Run PowerShell script:
```powershell
.\create_installer.ps1
```
Output executable is created in `dist/installer/ShopManager_Installer_v2.0.exe`.
