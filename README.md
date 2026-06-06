# 🏪 Shop Management System
## Professional Supermart Software with Barcode Scanning & Generation

[![Java](https://img.shields.io/badge/Java-8%2B-orange)](https://www.oracle.com/java/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0%2B-blue)](https://www.mysql.com/)
[![Hibernate](https://img.shields.io/badge/Hibernate-5.x-green)](https://hibernate.org/)

**A complete, production-ready supermart management system** with barcode scanning, inventory management, point of sale, and real-time reporting. Built with Java Swing and Hibernate ORM for robust database management.

---

## 🚀 Quick Start

### Setup (3 Steps)
```powershell
# 1. Run automated setup
.\setup-system.ps1

# 2. Update password in src\hibernate.cfg.xml

# 3. Run in NetBeans (Main: frames.Login)
Username: admin
Password: admin123
```
📖 **Full Documentation:** [README.md](README.md)

---

## ✨ Features

### 🛒 Core Functionality
- **Product Management**: Complete CRUD operations for products with categories, pricing, and stock tracking
- **Customer Management**: Maintain customer database with purchase history
- **Transaction Processing**: Handle sales and purchases with multiple payment methods (Cash, Card, Mobile)
- **Barcode Scanner Integration**: Scan barcodes for quick product lookup and sales processing
- **Inventory Management**: Real-time stock tracking with low-stock alerts
- **Reporting System**: Comprehensive business reports (daily sales, profit/loss, inventory status)

### 🎯 Advanced Features
- **Hibernate ORM**: Professional database abstraction layer with JPA entities
- **Service Layer**: Clean separation of business logic
- **Transaction Management**: ACID-compliant database transactions
- **Input Validation**: Comprehensive validation for all user inputs
- **Modern UI**: Clean, professional interface using FlatLaf Look and Feel
- **Barcode Generation**: Generate and print product barcodes
- **Multi-user Support**: Role-based access control (Admin, Manager, Cashier)
- **Password Security**: BCrypt password hashing

### 📊 Reports Available
1. Daily Sales Report
2. Sales Report by Date Range
3. Inventory Status Report
4. Customer Analysis Report
5. Profit & Loss Report
6. Top Selling Products Report
7. Dashboard Summary

## 🏗️ Architecture

### Layer Structure
```
┌─────────────────────────────────────┐
│     Presentation Layer (Swing UI)   │
├─────────────────────────────────────┤
│     Service Layer (Business Logic)  │
├─────────────────────────────────────┤
│     DAO Layer (Data Access)         │
├─────────────────────────────────────┤
│     Entity Layer (JPA Entities)     │
├─────────────────────────────────────┤
│     Hibernate ORM                   │
├─────────────────────────────────────┤
│     MySQL / SQLite Database         │
└─────────────────────────────────────┘
```

### Package Structure
```
src/
├── dao/                 # Hibernate DAO layer
│   ├── GenericDAO.java
│   ├── ProductHibernateDAO.java
│   └── ...
├── service/             # Business logic layer
│   ├── ProductService.java
│   └── ...
├── models/
│   ├── Session.java
│   └── entity/          # JPA Entity classes
│       ├── ProductEntity.java
│       └── ...
├── frames/              # UI frames (Swing FlatLaf)
│   ├── DashboardEnterprise.java
│   └── ...
├── helper/              # Barcode generator helpers
├── shop/                # Main entry point (Shop.java)
└── util/                # System utilities
    ├── HibernateUtil.java
    ├── ValidationUtil.java
    └── SecurityUtil.java
```

## 🛠️ Technology Stack

- **Frontend**: Java Swing, FlatLaf Look and Feel
- **Backend**: Java 8+
- **ORM**: Hibernate 5.x
- **Database**: MySQL 8.0+
- **Build Tool**: Apache Ant / NetBeans
- **Barcode**: ZXing (Zebra Crossing)
- **PDF Generation**: iText
- **Security**: BCrypt password hashing

## 📦 Required Dependencies

Add these JAR files to your project classpath:

### Core Dependencies
1. **MySQL Connector**: `mysql-connector-j-9.4.0.jar`
2. **Hibernate Core**: `hibernate-core-5.6.x.jar`
3. **Hibernate JPA API**: `hibernate-jpa-2.1-api.jar`
4. **FlatLaf**: `flatlaf-3.6.jar`

### Barcode Libraries
5. **ZXing Core**: `core-3.4.1.jar`
6. **ZXing JavaSE**: `javase-3.4.1.jar`

### PDF & Reporting
7. **iText PDF**: `itextpdf-5.5.13.3.jar`
8. **iText 7 Kernel**: `kernel-7.2.5.jar`
9. **iText 7 Layout**: `layout-7.2.5.jar`
10. **iText 7 IO**: `io-7.2.5.jar`
11. **iText 7 Commons**: `commons-7.2.5.jar`

### Utilities
12. **BCrypt**: `bcrypt-0.10.2.jar` (for password hashing)
13. **SLF4J API**: `slf4j-api-1.7.36.jar`
14. **BridJ**: `bridj-0.7.0.jar` (for barcode scanner hardware)

## 🚀 Setup Instructions

### 1. Database Setup

**Fresh Installation:**
```sql
-- Create database
CREATE DATABASE shop;
USE shop;

-- Option 1: Let Hibernate auto-create tables (recommended for development)
-- Set hibernate.hbm2ddl.auto=update in hibernate.cfg.xml

-- Option 2: Run initialization script (includes sample data)
source sql/initialize_database.sql
```

**Existing Installation (Upgrade):**
```bash
# 1. BACKUP YOUR DATABASE FIRST!
mysqldump -u root -p shop > shop_backup_$(date +%Y%m%d).sql

# 2. Run migration script
mysql -u root -p shop < sql/migrate_password_security.sql

# 3. Passwords will be automatically upgraded on first login
```

**Sample Data:**
The initialization script includes:
- ✅ 3 default users (admin, manager, cashier)
- ✅ 25 sample products across 5 categories
- ✅ 5 sample customers
- ✅ Barcodes for all products
- ✅ Optimized indexes for performance

### 1. Database Setup

```sql
-- Create database
CREATE DATABASE shop;
USE shop;

-- Tables will be auto-created by Hibernate
-- But you can use the SQL files in sql/ folder if needed
```

### 2. Configure Database Connection

Edit `src/hibernate.cfg.xml`:
```xml
<property name="hibernate.connection.url">jdbc:mysql://127.0.0.1:3306/shop</property>
<property name="hibernate.connection.username">root</property>
<property name="hibernate.connection.password">root</property>
```

### 3. Add Dependencies

**Option A: Using NetBeans**
1. Right-click project → Properties
2. Libraries → Add JAR/Folder
3. Add all required JAR files

**Option B: Maven (if converting)**
See `MAVEN_DEPENDENCIES.md` for pom.xml configuration

### 4. Build and Run

**Using NetBeans:**
1. Open project in NetBeans
2. Build Project (F11)
3. Run Project (F6)

**Using Command Line:**
```bash
# Compile
ant compile

# Run
ant run
```

## 👥 Default Users

⚠️ **IMPORTANT: Change these passwords immediately after first login!**

The system initializes with these default accounts:

| Username | Password | Role    | Permissions |
|----------|----------|---------|-------------|
| admin    | admin123 | ADMIN   | Full system access, user management |
| manager  | manager123 | MANAGER | Reports, inventory, customers, products |
| cashier  | cashier123 | CASHIER | Sales, product view, customer lookup |

**First Login:**
- Passwords are automatically hashed on first successful login
- Change default passwords via User Management → Change Password
- All login attempts are logged for security auditing

## 📊 Logging & Monitoring

### Comprehensive Logging System

**Logged Events:**
- ✅ All login attempts (successful and failed)
- ✅ Account lockouts and unlocks
- ✅ Transaction processing (sales and purchases)
- ✅ Inventory changes and low stock alerts
- ✅ User management operations
- ✅ Password changes
- ✅ System errors and exceptions

**Log Levels:**
- `INFO` - Normal operations, successful transactions
- `WARNING` - Failed attempts, validation errors, low stock
- `SEVERE` - System errors, exceptions, critical failures

**Example Logs:**
```
[INFO] Successful login: admin (Role: ADMIN)
[INFO] Sale completed: Product=Coca Cola, Customer=John Doe, Qty=5, Amount=$7.50
[WARNING] Low stock alert: Milk - Only 8 units remaining
[WARNING] Failed login attempt: wrong password - cashier (Attempts: 3)
[SEVERE] Database connection error: Connection refused
```

**Log Location:**
- Application logs are written to console (Java Logger)
- For production: Configure log file output in logging.properties

## 👥 Default Users

The system should be initialized with these default users:

| Username | Password | Role    |
|----------|----------|---------|
| admin    | admin123 | ADMIN   |
| manager  | manager123 | MANAGER |
| cashier  | cashier123 | CASHIER |

## 🔧 Configuration

### Hibernate Configuration
Edit `hibernate.cfg.xml` to modify:
- Database connection settings
- Connection pool size
- SQL logging (show_sql)
- Schema generation strategy (hbm2ddl.auto)

### Application Settings
- Low stock threshold: Default 10 units
- Default payment methods: Cash, Card, Mobile
- Currency format: USD ($)

## 📱 Usage Guide

### For Cashiers
1. **Process Sale**: Select product (scan or search) → Enter quantity → Select payment method → Complete
2. **View Products**: Browse all products, search by name or barcode
3. **View Transactions**: See transaction history

### For Managers
- All Cashier features +
- **Add/Edit Products**: Manage product catalog
- **Add/Edit Customers**: Maintain customer database
- **View Reports**: Access all business reports
- **Manage Inventory**: Stock adjustments, low stock alerts

### For Administrators
- All Manager features +
- **User Management**: Create, edit, delete user accounts
- **System Configuration**: Modify system settings
- **Database Backup**: Export/import data

## 🔐 Security Features

### Enterprise-Grade Security
- **Password Hashing**: SHA-256 with salt and multiple iterations (10,000)
- **Login Attempt Limiting**: Maximum 5 attempts before 15-minute lockout
- **Account Lockout**: Automatic protection against brute force attacks
- **Password Strength Validation**: Enforced password complexity requirements
- **Automatic Password Upgrade**: Legacy plain text passwords upgraded on first login
- **Session Management**: User session tracking and validation
- **Input Validation**: XSS and SQL injection protection
- **Comprehensive Audit Trail**: All security events logged

### Security Configuration
```
Max Login Attempts: 5
Lockout Duration: 15 minutes
Attempt Reset Time: 30 minutes
Password Min Length: 6 characters (8+ recommended)
Required: Uppercase, lowercase, and digits for strong passwords
```

### Security Utilities
- `PasswordUtil` - Password hashing and verification
- `SecurityUtil` - Login attempt tracking and account locking
- `ValidationUtil` - Input sanitization and validation
- `LoggerUtil` - Security event logging

## 🛡️ Security Features

- **Password Hashing**: SHA-256 with salt and 10,000 iterations
- **Login Attempt Limiting**: Max 5 attempts, 15-minute lockout
- **Account Management**: Active/inactive user status
- **Role-Based Access**: Admin, Manager, Cashier permissions
- **Password Security**: BCrypt password hashing

## 🎨 Customization

### Changing Color Scheme
Edit `util/UIUtil.java`:
```java
public static final Color PRIMARY_COLOR = new Color(0, 120, 215);
public static final Color SUCCESS_COLOR = new Color(40, 167, 69);
// etc.
```

### Adding New Reports
1. Create method in `service/ReportService.java`
2. Add UI in frames/
3. Wire up in menu

## 🐛 Troubleshooting

### Database Connection Issues
- Verify MySQL is running
- Check username/password in hibernate.cfg.xml
- Ensure MySQL JDBC driver is in classpath

### Hibernate Errors
- Check entity mappings
- Verify database tables exist
- Enable SQL logging: `hibernate.show_sql=true`

### Barcode Scanner Not Working
- Check scanner driver installation
- Verify USB connection
- Test scanner with notepad first

## 📈 Future Enhancements

- [ ] Cloud database support
- [ ] Mobile app integration
- [ ] Online ordering system
- [ ] Advanced analytics dashboard
- [ ] Multi-store management
- [ ] Email receipt functionality
- [ ] Supplier management
- [ ] Employee time tracking

## 📄 License

Proprietary - All Rights Reserved

## 👨‍💻 Development Team

Shop Management System Development Team

## 📞 Support

For technical support or inquiries, please contact your system administrator.

---

**Version**: 2.0.0  
**Last Updated**: 2026  
**Built with** ❤️ **using Java & Hibernate**
