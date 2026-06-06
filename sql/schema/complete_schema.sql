-- ================================================================
-- ENTERPRISE SHOP MANAGEMENT SYSTEM - COMPLETE DATABASE SCHEMA
-- Full production-ready schema with audit logs, stock tracking,
2FA,
-- roles, categories, stored procedures, views, and subscription.
-- Database: shop2
-- ================================================================

-- Set up environment
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';
SET NAMES utf8mb4;

-- Create database
CREATE DATABASE IF NOT EXISTS `shop2` 
    DEFAULT CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;
USE `shop2`;

-- ================================================================
-- TABLE: roles (Role-Based Access Control)
-- ================================================================
CREATE TABLE IF NOT EXISTS `roles` (
  `role_id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(50) NOT NULL UNIQUE,
  `description` VARCHAR(255) NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`role_id`),
  INDEX `idx_role_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================================
-- TABLE: suppliers (Purchase Source Management)
-- ================================================================
CREATE TABLE IF NOT EXISTS `suppliers` (
    `supplier_id` INT NOT NULL AUTO_INCREMENT,
    `company_name` VARCHAR(200) NOT NULL,
    `contact_person` VARCHAR(100) NULL,
    `phone_number` VARCHAR(20) NULL,
    `email` VARCHAR(100) NULL,
    `address` TEXT NULL,
    `city` VARCHAR(100) NULL,
    `country` VARCHAR(100) NULL,
    `tax_number` VARCHAR(50) NULL,
    `payment_terms` VARCHAR(100) NULL,
    `credit_limit` DECIMAL(12,2) NULL,
    `current_balance` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    `remarks` TEXT NULL,
    `status` ENUM('active','inactive','blocked') NOT NULL DEFAULT 'active',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
    `last_purchase_date` TIMESTAMP NULL,
    PRIMARY KEY (`supplier_id`),
    INDEX `idx_supplier_name` (`company_name`),
    INDEX `idx_supplier_status` (`status`),
    INDEX `idx_supplier_phone` (`phone_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================================
-- TABLE: users (Authentication and Authorization)
-- ================================================================
CREATE TABLE IF NOT EXISTS `users` (
  `user_id` INT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(50) NOT NULL UNIQUE,
  `password` VARCHAR(255) NOT NULL,
  `full_name` VARCHAR(200) NULL,
  `email` VARCHAR(100) NULL,
  `role` ENUM('ADMIN','MANAGER','CASHIER') NOT NULL DEFAULT 'CASHIER',
  `is_active` BOOLEAN NOT NULL DEFAULT TRUE,
  `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
  `last_login` TIMESTAMP NULL,
  `created_by` INT NULL,
  PRIMARY KEY (`user_id`),
  INDEX `idx_username` (`username`),
  INDEX `idx_role` (`role`),
  INDEX `idx_active_deleted` (`is_active`, `is_deleted`),
  CONSTRAINT `fk_user_creator` 
    FOREIGN KEY (`created_by`) 
    REFERENCES `users`(`user_id`) 
    ON DELETE SET NULL 
    ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================================
-- TABLE: categories (Product Categories)
-- ================================================================
CREATE TABLE IF NOT EXISTS `categories` (
  `category_id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL UNIQUE,
  `description` TEXT NULL,
  `parent_category_id` INT NULL,
  `is_active` BOOLEAN NOT NULL DEFAULT TRUE,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`category_id`),
  INDEX `idx_category_name` (`name`),
  INDEX `idx_parent` (`parent_category_id`),
  CONSTRAINT `fk_category_parent` 
    FOREIGN KEY (`parent_category_id`) 
    REFERENCES `categories`(`category_id`) 
    ON DELETE SET NULL 
    ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================================
-- TABLE: products (Inventory Management)
-- ================================================================
CREATE TABLE IF NOT EXISTS `products` (
  `product_id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(200) NOT NULL,
  `category_id` INT NULL,
  `product_type` VARCHAR(100) NULL,
  `description` TEXT NULL,
  `remarks` TEXT NULL,
  `stock` INT NOT NULL DEFAULT 0,
  `min_stock_level` INT NOT NULL DEFAULT 10,
  `sell_price` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `purchase_price` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  `status` ENUM('active','discontinued','out_of_stock') NOT NULL DEFAULT 'active',
  `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
  `created_by` INT NULL,
  PRIMARY KEY (`product_id`),
  INDEX `idx_name` (`name`),
  INDEX `idx_category` (`category_id`),
  INDEX `idx_product_type` (`product_type`),
  INDEX `idx_status` (`status`),
  INDEX `idx_deleted` (`is_deleted`),
  INDEX `idx_stock_level` (`stock`),
  CONSTRAINT `fk_product_category` 
    FOREIGN KEY (`category_id`) 
    REFERENCES `categories`(`category_id`) 
    ON DELETE SET NULL 
    ON UPDATE CASCADE,
  CONSTRAINT `fk_product_creator` 
    FOREIGN KEY (`created_by`) 
    REFERENCES `users`(`user_id`) 
    ON DELETE SET NULL 
    ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================================
-- TABLE: customers (Customer Management)
-- ================================================================
CREATE TABLE IF NOT EXISTS `customers` (
  `customer_id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(200) NOT NULL,
  `phone_number` VARCHAR(20) NULL,
  `email` VARCHAR(100) NULL,
  `address` TEXT NULL,
  `city` VARCHAR(100) NULL,
  `postal_code` VARCHAR(20) NULL,
  `remarks` TEXT NULL,
  `date_created` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `last_purchase_date` TIMESTAMP NULL,
  `total_purchases` DECIMAL(12,2) DEFAULT 0.00,
  `loyalty_points` INT DEFAULT 0,
  `status` ENUM('active','inactive','blocked') NOT NULL DEFAULT 'active',
  `is_deleted` BOOLEAN NOT NULL DEFAULT FALSE,
  `updated_at` TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`customer_id`),
  INDEX `idx_name` (`name`),
  INDEX `idx_phone` (`phone_number`),
  INDEX `idx_email` (`email`),
  INDEX `idx_status` (`status`),
  INDEX `idx_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================================
-- TABLE: transactions (Sales, Purchases, and Returns)
-- ================================================================
CREATE TABLE IF NOT EXISTS `transactions` (
  `transaction_id` INT NOT NULL AUTO_INCREMENT,
  `transaction_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `transaction_type` ENUM('SALE','PURCHASE','RETURN') NOT NULL,
  `transaction_status` ENUM('completed','pending','cancelled','refunded') NOT NULL DEFAULT 'completed',
  `customer_id` INT NULL,
  `supplier_id` INT NULL,
  `product_id` INT NOT NULL,
  `quantity` INT NOT NULL,
  `sell_price` DECIMAL(10,2) NULL,
  `purchase_price` DECIMAL(10,2) NULL,
  `unit_price` DECIMAL(10,2) NOT NULL,
  `total_amount` DECIMAL(12,2) NOT NULL,
  `payment_type` ENUM('CASH','CARD','MOBILE','CREDIT') NULL,
  `currency` VARCHAR(3) DEFAULT 'USD',
  `discount_amount` DECIMAL(10,2) DEFAULT 0.00,
  `tax_amount` DECIMAL(10,2) DEFAULT 0.00,
  `remarks` TEXT NULL,
  `stock_before_transaction` INT NULL,
  `stock_after_transaction` INT NULL,
  `processed_by` INT NULL,
  `reference_transaction_id` INT NULL,
  PRIMARY KEY (`transaction_id`),
  INDEX `idx_date` (`transaction_date`),
  INDEX `idx_type` (`transaction_type`),
  INDEX `idx_status` (`transaction_status`),
  INDEX `idx_customer` (`customer_id`),
  INDEX `idx_supplier` (`supplier_id`),
  INDEX `idx_product` (`product_id`),
  INDEX `idx_processed_by` (`processed_by`),
  INDEX `idx_reference` (`reference_transaction_id`),
  INDEX `idx_type_date` (`transaction_type`, `transaction_date`),
  INDEX `idx_customer_date` (`customer_id`, `transaction_date`),
  INDEX `idx_product_date` (`product_id`, `transaction_date`),
  CONSTRAINT `fk_transaction_customer` 
    FOREIGN KEY (`customer_id`) 
    REFERENCES `customers`(`customer_id`) 
    ON DELETE SET NULL 
    ON UPDATE CASCADE,
  CONSTRAINT `fk_transaction_supplier` 
    FOREIGN KEY (`supplier_id`) 
    REFERENCES `suppliers`(`supplier_id`) 
    ON DELETE SET NULL 
    ON UPDATE CASCADE,
  CONSTRAINT `fk_transaction_product` 
    FOREIGN KEY (`product_id`) 
    REFERENCES `products`(`product_id`) 
    ON DELETE RESTRICT 
    ON UPDATE CASCADE,
  CONSTRAINT `fk_transaction_user` 
    FOREIGN KEY (`processed_by`) 
    REFERENCES `users`(`user_id`) 
    ON DELETE SET NULL 
    ON UPDATE CASCADE,
  CONSTRAINT `fk_transaction_reference` 
    FOREIGN KEY (`reference_transaction_id`) 
    REFERENCES `transactions`(`transaction_id`) 
    ON DELETE SET NULL 
    ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================================
-- TABLE: barcode (Barcode Management for Products)
-- ================================================================
CREATE TABLE IF NOT EXISTS `barcode` (
  `barcode_id` INT NOT NULL AUTO_INCREMENT,
  `product_id` INT NOT NULL,
  `barcode_number` VARCHAR(50) NOT NULL UNIQUE,
  `status` ENUM('available','sold','damaged','reserved') NOT NULL DEFAULT 'available',
  `is_active` BOOLEAN NOT NULL DEFAULT TRUE,
  `warehouse_location` VARCHAR(100) NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `sold_at` TIMESTAMP NULL,
  `remarks` TEXT NULL,
  PRIMARY KEY (`barcode_id`),
  INDEX `idx_barcode_number` (`barcode_number`),
  INDEX `idx_product` (`product_id`),
  INDEX `idx_status` (`status`),
  INDEX `idx_product_active` (`product_id`, `is_active`),
  INDEX `idx_barcode_active_status` (`barcode_number`, `is_active`, `status`),
  CONSTRAINT `fk_barcode_product` 
    FOREIGN KEY (`product_id`) 
    REFERENCES `products`(`product_id`) 
    ON DELETE CASCADE 
    ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================================
-- TABLE: stock_audit (Automatic Stock Change Tracking)
-- ================================================================
CREATE TABLE IF NOT EXISTS `stock_audit` (
  `audit_id` INT NOT NULL AUTO_INCREMENT,
  `product_id` INT NOT NULL,
  `change_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `stock_before` INT NOT NULL,
  `stock_after` INT NOT NULL,
  `change_amount` INT NOT NULL,
  `change_source` ENUM('SALE','PURCHASE','RETURN','ADJUSTMENT','DAMAGE','SYSTEM') NOT NULL,
  `transaction_id` INT NULL,
  `changed_by` INT NULL,
  `remarks` TEXT NULL,
  PRIMARY KEY (`audit_id`),
  INDEX `idx_product` (`product_id`),
  INDEX `idx_change_date` (`change_date`),
  INDEX `idx_transaction` (`transaction_id`),
  CONSTRAINT `fk_stock_audit_product` 
    FOREIGN KEY (`product_id`) 
    REFERENCES `products`(`product_id`) 
    ON DELETE CASCADE 
    ON UPDATE CASCADE,
  CONSTRAINT `fk_stock_audit_transaction` 
    FOREIGN KEY (`transaction_id`) 
    REFERENCES `transactions`(`transaction_id`) 
    ON DELETE SET NULL 
    ON UPDATE CASCADE,
  CONSTRAINT `fk_stock_audit_user` 
    FOREIGN KEY (`changed_by`) 
    REFERENCES `users`(`user_id`) 
    ON DELETE SET NULL 
    ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================================
-- TABLE: audit_log (System-Wide Audit Trail)
-- ================================================================
CREATE TABLE IF NOT EXISTS `audit_log` (
  `log_id` INT NOT NULL AUTO_INCREMENT,
  `log_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `entity_type` ENUM('USER','PRODUCT','CUSTOMER','TRANSACTION','BARCODE','CATEGORY') NOT NULL,
  `entity_id` INT NOT NULL,
  `action` ENUM('CREATE','UPDATE','DELETE','LOGIN','LOGOUT','PRICE_CHANGE','STATUS_CHANGE') NOT NULL,
  `performed_by` INT NULL,
  `old_value` TEXT NULL,
  `new_value` TEXT NULL,
  `ip_address` VARCHAR(45) NULL,
  `remarks` TEXT NULL,
  PRIMARY KEY (`log_id`),
  INDEX `idx_log_date` (`log_date`),
  INDEX `idx_entity` (`entity_type`, `entity_id`),
  INDEX `idx_action` (`action`),
  INDEX `idx_performed_by` (`performed_by`),
  CONSTRAINT `fk_audit_log_user` 
    FOREIGN KEY (`performed_by`) 
    REFERENCES `users`(`user_id`) 
    ON DELETE SET NULL 
    ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================================
-- TABLE: settings (System Configuration Store)
-- ================================================================
CREATE TABLE IF NOT EXISTS `settings` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `company_name` VARCHAR(200) NULL,
  `company_phone` VARCHAR(50) NULL,
  `company_email` VARCHAR(100) NULL,
  `company_address` VARCHAR(500) NULL,
  `company_details` VARCHAR(2000) NULL,
  `tax_rate` DOUBLE NULL,
  `discount_rate` DOUBLE NULL,
  `tax_notes` VARCHAR(2000) NULL,
  `receipt_template` VARCHAR(2000) NULL,
  `print_barcodes` BOOLEAN NULL,
  `print_item_details` BOOLEAN NULL,
  `auto_backup` BOOLEAN NULL,
  `backup_frequency` VARCHAR(50) NULL,
  `backup_path` VARCHAR(500) NULL,
  `dark_mode` BOOLEAN NULL,
  `theme` VARCHAR(50) NULL,
  `language` VARCHAR(50) NULL,
  `show_developer_credit` BOOLEAN NULL,
  `developer_name` VARCHAR(200) NULL,
  `developer_contact` VARCHAR(100) NULL,
  `updated_at` DATETIME NULL,
  `updated_by` VARCHAR(100) NULL,
  `payment_cycle_start_date` DATETIME NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================================
-- TABLE: app_subscription (License Validation & Security Fingerprint)
-- ================================================================
CREATE TABLE IF NOT EXISTS `app_subscription` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `installed_version` VARCHAR(255) NOT NULL,
  `install_date` DATE NOT NULL,
  `expiry_date` DATE NOT NULL,
  `status` VARCHAR(20) NOT NULL,
  `otp_code` VARCHAR(4) NULL,
  `otp_created_at` DATETIME NULL,
  `otp_expiry_at` DATETIME NULL,
  `otp_used` BOOLEAN NOT NULL DEFAULT FALSE,
  `last_otp_attempt_count` INT NOT NULL DEFAULT 0,
  `otp_locked_until` DATETIME NULL,
  `otp_status` VARCHAR(10) NULL,
  `expiry_handled` BOOLEAN NOT NULL DEFAULT FALSE,
  `device_id` VARCHAR(255) NULL,
  `device_name` VARCHAR(255) NULL,
  `os_name` VARCHAR(100) NULL,
  `hostname` VARCHAR(255) NULL,
  `mac_address` VARCHAR(255) NULL,
  `installed_by` VARCHAR(255) NULL,
  `shop_name` VARCHAR(255) NULL,
  `machine_id` VARCHAR(255) NULL,
  `renewal_count` INT NOT NULL DEFAULT 0,
  `last_renewal_date` DATE NULL,
  `created_at` DATETIME NOT NULL,
  `updated_at` DATETIME NULL,
  PRIMARY KEY (`id`),
  INDEX `idx_sub_status` (`status`),
  INDEX `idx_sub_expiry` (`expiry_date`),
  INDEX `idx_sub_device` (`device_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================================
-- TABLE: email_queue (Offline-First Dispatch Queue)
-- ================================================================
CREATE TABLE IF NOT EXISTS `email_queue` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `email_type` VARCHAR(50) NOT NULL,
  `recipient` VARCHAR(255) NOT NULL,
  `subject` VARCHAR(255) NOT NULL,
  `body` LONGTEXT NOT NULL,
  `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  `retry_count` INT NOT NULL DEFAULT 0,
  `last_retry_at` DATETIME NULL,
  `error_message` TEXT NULL,
  `created_at` DATETIME NOT NULL,
  `updated_at` DATETIME NULL,
  `sent_at` DATETIME NULL,
  `dedup_key` VARCHAR(255) NULL,
  PRIMARY KEY (`id`),
  INDEX `idx_email_status` (`status`),
  INDEX `idx_email_type` (`email_type`),
  INDEX `idx_dedup_key` (`dedup_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ================================================================
-- VIEWS FOR REPORTING AND ANALYTICS
-- ================================================================

-- Daily Sales Summary
CREATE OR REPLACE VIEW `v_daily_sales_summary` AS
SELECT 
    DATE(transaction_date) AS sale_date,
    COUNT(*) AS total_transactions,
    SUM(quantity) AS total_items_sold,
    SUM(total_amount) AS total_sales_amount,
    SUM(discount_amount) AS total_discounts,
    SUM(tax_amount) AS total_tax,
    SUM((unit_price - purchase_price) * quantity) AS estimated_profit,
    AVG(total_amount) AS avg_transaction_value
FROM transactions
WHERE transaction_type = 'SALE' AND transaction_status = 'completed'
GROUP BY DATE(transaction_date)
ORDER BY sale_date DESC;

-- Product Stock Status with Alerts
CREATE OR REPLACE VIEW `v_product_stock_status` AS
SELECT 
    p.product_id,
    p.name,
    p.product_type,
    c.name AS category_name,
    p.stock,
    p.min_stock_level,
    p.sell_price,
    p.purchase_price,
    (p.sell_price - p.purchase_price) AS profit_per_unit,
    ((p.sell_price - p.purchase_price) / NULLIF(p.purchase_price, 0)) * 100 AS profit_margin_pct,
    (p.stock * p.purchase_price) AS inventory_value,
    (p.stock * p.sell_price) AS potential_revenue,
    CASE 
        WHEN p.stock = 0 THEN 'OUT_OF_STOCK'
        WHEN p.stock < p.min_stock_level THEN 'LOW_STOCK'
        WHEN p.stock < (p.min_stock_level * 2) THEN 'MEDIUM_STOCK'
        ELSE 'GOOD_STOCK'
    END AS stock_level,
    p.status,
    p.created_at,
    p.updated_at
FROM products p
LEFT JOIN categories c ON p.category_id = c.category_id
WHERE p.is_deleted = FALSE
ORDER BY p.stock ASC, p.name;

-- Top Customers by Purchase Volume
CREATE OR REPLACE VIEW `v_top_customers` AS
SELECT 
    c.customer_id,
    c.name,
    c.phone_number,
    c.email,
    c.total_purchases,
    c.loyalty_points,
    c.last_purchase_date,
    COUNT(t.transaction_id) AS total_transactions,
    SUM(t.quantity) AS total_items_purchased,
    AVG(t.total_amount) AS avg_purchase_amount,
    c.status
FROM customers c
LEFT JOIN transactions t ON c.customer_id = t.customer_id 
    AND t.transaction_type = 'SALE' 
    AND t.transaction_status = 'completed'
WHERE c.is_deleted = FALSE AND c.status = 'active'
GROUP BY c.customer_id
ORDER BY c.total_purchases DESC, total_transactions DESC;

-- Top Selling Products
CREATE OR REPLACE VIEW `v_top_selling_products` AS
SELECT 
    p.product_id,
    p.name,
    p.product_type,
    c.name AS category_name,
    p.stock AS current_stock,
    COUNT(t.transaction_id) AS times_sold,
    SUM(t.quantity) AS total_quantity_sold,
    SUM(t.total_amount) AS total_revenue,
    AVG(t.unit_price) AS avg_selling_price,
    SUM((t.unit_price - t.purchase_price) * t.quantity) AS total_profit,
    p.sell_price AS current_price,
    p.status
FROM products p
LEFT JOIN categories c ON p.category_id = c.category_id
INNER JOIN transactions t ON p.product_id = t.product_id
WHERE t.transaction_type = 'SALE' 
  AND t.transaction_status = 'completed'
  AND p.is_deleted = FALSE
GROUP BY p.product_id
ORDER BY total_quantity_sold DESC, total_revenue DESC;

-- Inventory Valuation Summary
CREATE OR REPLACE VIEW `v_inventory_valuation` AS
SELECT 
    c.name AS category_name,
    COUNT(DISTINCT p.product_id) AS product_count,
    SUM(p.stock) AS total_stock,
    SUM(p.stock * p.purchase_price) AS total_purchase_value,
    SUM(p.stock * p.sell_price) AS total_selling_value,
    SUM(p.stock * (p.sell_price - p.purchase_price)) AS potential_profit
FROM products p
LEFT JOIN categories c ON p.category_id = c.category_id
WHERE p.is_deleted = FALSE AND p.status = 'active'
GROUP BY c.category_id
ORDER BY total_purchase_value DESC;

-- Recent Stock Changes
CREATE OR REPLACE VIEW `v_recent_stock_changes` AS
SELECT 
    sa.audit_id,
    sa.change_date,
    p.name AS product_name,
    sa.stock_before,
    sa.stock_after,
    sa.change_amount,
    sa.change_source,
    sa.transaction_id,
    u.username AS changed_by_user,
    sa.remarks
FROM stock_audit sa
INNER JOIN products p ON sa.product_id = p.product_id
LEFT JOIN users u ON sa.changed_by = u.user_id
ORDER BY sa.change_date DESC
LIMIT 100;

-- Low Stock Alert
CREATE OR REPLACE VIEW `v_low_stock_alert` AS
SELECT 
    p.product_id,
    p.name,
    p.product_type,
    c.name AS category_name,
    p.stock AS current_stock,
    p.min_stock_level,
    (p.min_stock_level - p.stock) AS shortage,
    p.purchase_price,
    ((p.min_stock_level - p.stock) * p.purchase_price) AS reorder_cost,
    p.status
FROM products p
LEFT JOIN categories c ON p.category_id = c.category_id
WHERE p.stock < p.min_stock_level 
  AND p.is_deleted = FALSE 
  AND p.status = 'active'
ORDER BY (p.min_stock_level - p.stock) DESC;

-- ================================================================
-- STORED PROCEDURES
-- ================================================================

DELIMITER $$

-- Process Sale with Stock Validation and Audit Trail
DROP PROCEDURE IF EXISTS `sp_process_sale`$$
CREATE PROCEDURE `sp_process_sale`(
    IN p_product_id INT,
    IN p_customer_id INT,
    IN p_quantity INT,
    IN p_unit_price DECIMAL(10,2),
    IN p_payment_type VARCHAR(20),
    IN p_discount_amount DECIMAL(10,2),
    IN p_processed_by INT,
    IN p_remarks TEXT,
    OUT p_transaction_id INT,
    OUT p_status VARCHAR(50),
    OUT p_message VARCHAR(255)
)
BEGIN
    DECLARE v_current_stock INT;
    DECLARE v_purchase_price DECIMAL(10,2);
    DECLARE v_total_amount DECIMAL(12,2);
    DECLARE v_product_status VARCHAR(20);
    DECLARE v_customer_status VARCHAR(20);
    
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SET p_status = 'ERROR';
        SET p_message = 'Transaction failed due to database error';
        SET p_transaction_id = NULL;
    END;
    
    START TRANSACTION;
    
    -- Lock product row for update
    SELECT stock, purchase_price, status 
    INTO v_current_stock, v_purchase_price, v_product_status
    FROM products 
    WHERE product_id = p_product_id 
    FOR UPDATE;
    
    -- Validate product exists and is active
    IF v_current_stock IS NULL THEN
        SET p_status = 'ERROR';
        SET p_message = 'Product not found';
        ROLLBACK;
    ELSEIF v_product_status != 'active' THEN
        SET p_status = 'ERROR';
        SET p_message = 'Product is not active';
        ROLLBACK;
    -- Validate sufficient stock
    ELSEIF v_current_stock < p_quantity THEN
        SET p_status = 'ERROR';
        SET p_message = CONCAT('Insufficient stock. Available: ', v_current_stock);
        ROLLBACK;
    ELSE
        -- Validate customer if not walk-in
        IF p_customer_id IS NOT NULL AND p_customer_id != 1 THEN
            SELECT status INTO v_customer_status
            FROM customers
            WHERE customer_id = p_customer_id;
            
            IF v_customer_status IS NULL THEN
                SET p_status = 'ERROR';
                SET p_message = 'Customer not found';
                ROLLBACK;
            ELSEIF v_customer_status != 'active' THEN
                SET p_status = 'ERROR';
                SET p_message = 'Customer account is not active';
                ROLLBACK;
            END IF;
        END IF;
        
        -- Calculate total amount
        SET v_total_amount = (p_unit_price * p_quantity) - COALESCE(p_discount_amount, 0);
        
        -- Insert transaction
        INSERT INTO transactions (
            transaction_type, transaction_status, customer_id, product_id, 
            quantity, sell_price, purchase_price, unit_price, total_amount, 
            payment_type, discount_amount, stock_before_transaction, 
            stock_after_transaction, processed_by, remarks
        ) VALUES (
            'SALE', 'completed', p_customer_id, p_product_id, 
            p_quantity, p_unit_price, v_purchase_price, p_unit_price, v_total_amount, 
            p_payment_type, p_discount_amount, v_current_stock, 
            (v_current_stock - p_quantity), p_processed_by, p_remarks
        );
        
        SET p_transaction_id = LAST_INSERT_ID();
        
        -- Update product stock
        UPDATE products 
        SET stock = stock - p_quantity,
            updated_at = CURRENT_TIMESTAMP
        WHERE product_id = p_product_id;
        
        -- Log stock change
        INSERT INTO stock_audit (
            product_id, stock_before, stock_after, change_amount, 
            change_source, transaction_id, changed_by
        ) VALUES (
            p_product_id, v_current_stock, (v_current_stock - p_quantity), 
            -p_quantity, 'SALE', p_transaction_id, p_processed_by
        );
        
        -- Update customer total purchases
        IF p_customer_id IS NOT NULL THEN
            UPDATE customers
            SET total_purchases = total_purchases + v_total_amount,
                last_purchase_date = CURRENT_TIMESTAMP,
                updated_at = CURRENT_TIMESTAMP
            WHERE customer_id = p_customer_id;
        END IF;
        
        SET p_status = 'SUCCESS';
        SET p_message = 'Sale processed successfully';
        
        COMMIT;
    END IF;
END$$

-- Process Return with Stock Restoration
DROP PROCEDURE IF EXISTS `sp_process_return`$$
CREATE PROCEDURE `sp_process_return`(
    IN p_original_transaction_id INT,
    IN p_quantity INT,
    IN p_processed_by INT,
    IN p_remarks TEXT,
    OUT p_transaction_id INT,
    OUT p_status VARCHAR(50),
    OUT p_message VARCHAR(255)
)
BEGIN
    DECLARE v_product_id INT;
    DECLARE v_customer_id INT;
    DECLARE v_unit_price DECIMAL(10,2);
    DECLARE v_purchase_price DECIMAL(10,2);
    DECLARE v_current_stock INT;
    DECLARE v_refund_amount DECIMAL(12,2);
    DECLARE v_original_quantity INT;
    
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SET p_status = 'ERROR';
        SET p_message = 'Return processing failed';
        SET p_transaction_id = NULL;
    END;
    
    START TRANSACTION;
    
    -- Get original transaction details
    SELECT product_id, customer_id, quantity, unit_price, purchase_price
    INTO v_product_id, v_customer_id, v_original_quantity, v_unit_price, v_purchase_price
    FROM transactions
    WHERE transaction_id = p_original_transaction_id
      AND transaction_type = 'SALE';
    
    -- Validate original transaction exists
    IF v_product_id IS NULL THEN
        SET p_status = 'ERROR';
        SET p_message = 'Original transaction not found';
        ROLLBACK;
    -- Validate return quantity
    ELSEIF p_quantity > v_original_quantity THEN
        SET p_status = 'ERROR';
        SET p_message = 'Return quantity exceeds original purchase';
        ROLLBACK;
    ELSE
        -- Get current stock
        SELECT stock INTO v_current_stock
        FROM products
        WHERE product_id = v_product_id
        FOR UPDATE;
        
        -- Calculate refund amount
        SET v_refund_amount = v_unit_price * p_quantity;
        
        -- Insert return transaction
        INSERT INTO transactions (
            transaction_type, transaction_status, customer_id, product_id, 
            quantity, sell_price, purchase_price, unit_price, total_amount, 
            stock_before_transaction, stock_after_transaction, 
            processed_by, reference_transaction_id, remarks
        ) VALUES (
            'RETURN', 'completed', v_customer_id, v_product_id, 
            p_quantity, v_unit_price, v_purchase_price, v_unit_price, v_refund_amount, 
            v_current_stock, (v_current_stock + p_quantity), 
            p_processed_by, p_original_transaction_id, p_remarks
        );
        
        SET p_transaction_id = LAST_INSERT_ID();
        
        -- Restore stock
        UPDATE products
        SET stock = stock + p_quantity,
            updated_at = CURRENT_TIMESTAMP
        WHERE product_id = v_product_id;
        
        -- Log stock change
        INSERT INTO stock_audit (
            product_id, stock_before, stock_after, change_amount, 
            change_source, transaction_id, changed_by
        ) VALUES (
            v_product_id, v_current_stock, (v_current_stock + p_quantity), 
            p_quantity, 'RETURN', p_transaction_id, p_processed_by
        );
        
        -- Update customer total (deduct return amount)
        IF v_customer_id IS NOT NULL THEN
            UPDATE customers
            SET total_purchases = total_purchases - v_refund_amount,
                updated_at = CURRENT_TIMESTAMP
            WHERE customer_id = v_customer_id;
        END IF;
        
        SET p_status = 'SUCCESS';
        SET p_message = 'Return processed successfully';
        
        COMMIT;
    END IF;
END$$

-- Stock Adjustment Procedure
DROP PROCEDURE IF EXISTS `sp_adjust_stock`$$
CREATE PROCEDURE `sp_adjust_stock`(
    IN p_product_id INT,
    IN p_adjustment_quantity INT,
    IN p_reason VARCHAR(20),
    IN p_adjusted_by INT,
    IN p_remarks TEXT,
    OUT p_status VARCHAR(50),
    OUT p_message VARCHAR(255)
)
BEGIN
    DECLARE v_current_stock INT;
    DECLARE v_new_stock INT;
    
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SET p_status = 'ERROR';
        SET p_message = 'Stock adjustment failed';
    END;
    
    START TRANSACTION;
    
    -- Get and lock current stock
    SELECT stock INTO v_current_stock
    FROM products
    WHERE product_id = p_product_id
    FOR UPDATE;
    
    IF v_current_stock IS NULL THEN
        SET p_status = 'ERROR';
        SET p_message = 'Product not found';
        ROLLBACK;
    ELSE
        SET v_new_stock = v_current_stock + p_adjustment_quantity;
        
        -- Prevent negative stock
        IF v_new_stock < 0 THEN
            SET p_status = 'ERROR';
            SET p_message = 'Adjustment would result in negative stock';
            ROLLBACK;
        ELSE
            -- Update stock
            UPDATE products
            SET stock = v_new_stock,
                updated_at = CURRENT_TIMESTAMP
            WHERE product_id = p_product_id;
            
            -- Log adjustment
            INSERT INTO stock_audit (
                product_id, stock_before, stock_after, change_amount, 
                change_source, changed_by, remarks
            ) VALUES (
                p_product_id, v_current_stock, v_new_stock, 
                p_adjustment_quantity, p_reason, p_adjusted_by, p_remarks
            );
            
            SET p_status = 'SUCCESS';
            SET p_message = 'Stock adjusted successfully';
            
            COMMIT;
        END IF;
    END IF;
END$$

DELIMITER ;

-- ================================================================
-- TRIGGERS FOR AUTOMATIC AUDIT LOGGING
-- ================================================================

DELIMITER $$

-- Trigger: Log product price changes
DROP TRIGGER IF EXISTS `trg_product_price_change`$$
CREATE TRIGGER `trg_product_price_change`
AFTER UPDATE ON `products`
FOR EACH ROW
BEGIN
    IF OLD.sell_price != NEW.sell_price OR OLD.purchase_price != NEW.purchase_price THEN
        INSERT INTO audit_log (entity_type, entity_id, action, old_value, new_value)
        VALUES (
            'PRODUCT',
            NEW.product_id,
            'PRICE_CHANGE',
            CONCAT('Sell: ', OLD.sell_price, ', Purchase: ', OLD.purchase_price),
            CONCAT('Sell: ', NEW.sell_price, ', Purchase: ', NEW.purchase_price)
        );
    END IF;
END$$

-- Trigger: Log product status changes
DROP TRIGGER IF EXISTS `trg_product_status_change`$$
CREATE TRIGGER `trg_product_status_change`
AFTER UPDATE ON `products`
FOR EACH ROW
BEGIN
    IF OLD.status != NEW.status THEN
        INSERT INTO audit_log (entity_type, entity_id, action, old_value, new_value)
        VALUES (
            'PRODUCT',
            NEW.product_id,
            'STATUS_CHANGE',
            OLD.status,
            NEW.status
        );
    END IF;
END$$

DELIMITER ;

-- ================================================================
-- DEFAULT SEED DATA
-- ================================================================

-- Insert default roles
INSERT INTO `roles` (`name`, `description`) VALUES
('ADMIN', 'Full system access with all privileges'),
('MANAGER', 'Manage products, view reports, manage users'),
('CASHIER', 'Process sales, view products and customers')
ON DUPLICATE KEY UPDATE description=VALUES(description);

-- Insert default users
INSERT INTO `users` (`username`, `password`, `full_name`, `role`, `is_active`) VALUES
('admin', '$2a$10$Z1eE1N54f6B0L65x25O1E.cE3.o4s0Wq.v9hB4iY5nK6m4r3L8.G6', 'System Administrator', 'ADMIN', TRUE),
('manager', '$2a$10$S9W6B3o1M3V5r6T1A8p0E.n3q7S1d.w7uY8kG6vR7q8z9J3t4h2a6', 'Store Manager', 'MANAGER', TRUE),
('cashier', '$2a$10$J5s3f2e1m7g4T6s7Y8U1O.p9oR5v1W5y7h8v3f2e4w1t3y5u6i7o8', 'Store Cashier', 'CASHIER', TRUE)
ON DUPLICATE KEY UPDATE username=username;

-- Insert default categories
INSERT INTO `categories` (`name`, `description`) VALUES
('Electronics', 'Electronic devices and accessories'),
('Groceries', 'Food and beverages'),
('Clothing', 'Apparel and fashion items'),
('Home & Garden', 'Home improvement and garden supplies'),
('Sports & Outdoors', 'Sports equipment and outdoor gear'),
('Automotive', 'Auto parts and accessories'),
('Health & Beauty', 'Healthcare and beauty products'),
('Books & Media', 'Books, music, and movies'),
('Toys & Games', 'Children toys and games'),
('Office Supplies', 'Office and stationery items')
ON DUPLICATE KEY UPDATE description=VALUES(description);

-- Insert default settings
INSERT INTO `settings` (`id`, `company_name`, `company_phone`, `company_email`, `company_address`, `company_details`, `tax_rate`, `discount_rate`, `tax_notes`, `receipt_template`, `print_barcodes`, `print_item_details`, `auto_backup`, `backup_frequency`, `backup_path`, `dark_mode`, `theme`, `language`, `show_developer_credit`, `developer_name`, `developer_contact`, `updated_at`, `payment_cycle_start_date`) VALUES
(1, 'Shop Management System', '+92-300-XXXXXXX', 'info@shopmgmt.com', 'Karachi, Pakistan', 'Professional shop management system', 17.0, 0.0, 'Standard tax rate', '========== RECEIPT ==========\nDate: {DATE}\nInvoice: {INVOICE}\n\nItems:\n{ITEMS}\n\nTotal: {TOTAL}\nTax: {TAX}\nGrand Total: {GRAND_TOTAL}\n\nThank you for your purchase!\n==============================', TRUE, TRUE, FALSE, 'Daily', 'C:\\Backups\\ShopDB', FALSE, 'Light', 'English', TRUE, 'Bilawal Abbasi', '+92-300-XXXXXXX', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE company_name=VALUES(company_name);

-- Insert default walk-in customer
INSERT INTO `customers` (`customer_id`, `name`, `phone_number`, `address`, `remarks`, `status`)
VALUES (1, 'Walk-in Customer', '000-0000000', 'N/A', 'Default walk-in customer for cash sales', 'active')
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- ================================================================
-- MAINTENANCE
-- ================================================================

-- Analyze tables for query optimization
ANALYZE TABLE users, products, customers, transactions, barcode, stock_audit, audit_log, categories, settings, app_subscription, email_queue;

-- Restore settings
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET SQL_MODE=@OLD_SQL_MODE;
