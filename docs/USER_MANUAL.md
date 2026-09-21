# Shop Management System - User Manual

Welcome to the **Shop Management System (Enterprise Edition)**. This user manual will guide cashiers, managers, and administrators through using the application for daily retail operations.

---

## Table of Contents
1. [Getting Started & Login](#1-getting-started--login)
2. [Dashboard Navigation](#2-dashboard-navigation)
3. [Point of Sale (POS)](#3-point-of-sale-pos)
4. [Product & Inventory Management](#4-product--inventory-management)
5. [Customer Management](#5-customer-management)
6. [Supplier & Purchase Orders](#6-supplier--purchase-orders)
7. [Transaction History](#7-transaction-history)
8. [Barcode Generation & Management](#8-barcode-generation--management)
9. [Reports & Business Intelligence](#9-reports--business-intelligence)
10. [Troubleshooting & Help](#10-troubleshooting--help)

---

## 1. Getting Started & Login

### Launching the Application
- Double-click the **ShopManager** shortcut on your Desktop or Start Menu.
- Alternatively, launch `ShopManagement.bat` or `ShopManagement.ps1` in the application directory.

### Logging In
1. Enter your **Username** and **Password**.
2. Click **Login** or press **Enter**.
3. Default credentials (if first time setup):
   - **Username**: `admin`
   - **Password**: Prompted to set on initial launch or default administrator password.
4. Account Lockout: Entering an incorrect password 5 consecutive times will temporarily lock the account for security.

---

## 2. Dashboard Navigation

Upon successful login, you will land on the **Enterprise Dashboard**:
- **Header**: Displays current date, active user, role, remaining subscription status, and a **Logout** button.
- **Metrics Grid**: Shows real-time business statistics:
  - Total Customers
  - Total Active Products
  - Today's Total Sales ($)
  - Today's Transaction Count
  - Low Stock Alerts (!)
  - Total All-Time Revenue ($)
- **Sidebar Menu**: Quick navigation to all authorized application modules based on your assigned role (ADMIN, MANAGER, CASHIER).

---

## 3. Point of Sale (POS)

The **Point of Sale** interface (`CashTransactionEnterprise`) allows cashiers to quickly process customer sales:

### Processing a Sale
1. **Search & Select Products**:
   - Use the **Search Products** text field to search by name or barcode.
   - Filter by category using the **Category** dropdown menu.
   - Click **Add to Cart** on any product card.
2. **Adjust Cart Quantities**:
   - Click **+** or **-** in the cart table to adjust quantity.
   - Click **X** to remove an item from the order.
3. **Select Customer & Payment Method**:
   - Choose a customer or select **Walk-in Customer**.
   - Select payment method (**CASH**, **CARD**, or **MOBILE**).
4. **Enter Amount Received & Calculate Change**:
   - For CASH sales, enter the **Amount Received**.
   - The system automatically calculates **Change to Return**.
5. **Complete Sale & Print Receipt**:
   - Click **COMPLETE SALE**.
   - Review confirmation details and click **OK**.
   - Choose **Print Receipt** to send the branded formatted receipt to your printer.

---

## 4. Product & Inventory Management

Access product management from the sidebar (**Products**):
- **Add Product**: Fill in product name, product type (category), purchase price, selling price, initial stock level, and remarks. Click **Add Product**.
- **Update Product**: Select a product from the table, modify details, and click **Update Product**.
- **Discontinue Product**: Select a product and click **Delete Product** to safely discontinue it (retains transaction history).
- **Stock Warnings**: Products falling below stock threshold (default 10) are highlighted in red.

---

## 5. Customer Management

Access customer management from the sidebar (**Customers**):
- Register new customers with full name, phone number, email address, and physical address.
- Track total lifetime purchases and purchase frequency per customer.
- Search customers by phone number or name for fast lookup during sales.

---

## 6. Supplier & Purchase Orders

- **Suppliers**: Manage supplier profiles, contact representatives, phone numbers, and addresses.
- **Purchase Orders**: Create purchase orders to replenish low-stock inventory. Stock levels update atomically upon purchase order completion.

---

## 7. Transaction History

View all past retail transactions under **Transactions**:
- Filter by date range, cashier, transaction type (SALE / PURCHASE), or payment method.
- View itemized breakdowns of individual sales.

---

## 8. Barcode Generation & Management

- Automatically generate standard Code128 / EAN barcodes for products.
- Print custom barcode labels for shelves or packaging.

---

## 9. Reports & Business Intelligence

*(Available for ADMIN and MANAGER roles)*
- Access **Reports** to generate:
  - Daily Sales Summary
  - Sales by Date Range
  - Low Stock Alert Reports
  - Top Selling Products Analysis
  - Inventory Valuation & Profit Margins
- Export reports to CSV or print for bookkeeping.

---

## 10. Troubleshooting & Help

- **Session Timeout**: Sessions automatically expire after 30 minutes of inactivity. Simply log back in.
- **Need Assistance**: Contact system administration or consult the [Administrator Manual](ADMIN_MANUAL.md).
