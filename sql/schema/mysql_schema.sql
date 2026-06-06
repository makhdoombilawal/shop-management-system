-- ================================================================
-- ENTERPRISE SHOP MANAGEMENT SYSTEM - MYSQL SCHEMA DDL
-- Table definitions and constraints only.
-- ================================================================

SET FOREIGN_KEY_CHECKS=0;

-- 1. Table: roles
CREATE TABLE IF NOT EXISTS `roles` (
  `role_id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(50) NOT NULL UNIQUE,
  `description` VARCHAR(255) NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`role_id`),
  INDEX `idx_role_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Table: suppliers
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
    INDEX `idx_supplier_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Table: users
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
  CONSTRAINT `fk_user_creator` FOREIGN KEY (`created_by`) REFERENCES `users`(`user_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Table: categories
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
  CONSTRAINT `fk_category_parent` FOREIGN KEY (`parent_category_id`) REFERENCES `categories`(`category_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Table: products
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
  INDEX `idx_status` (`status`),
  CONSTRAINT `fk_product_category` FOREIGN KEY (`category_id`) REFERENCES `categories`(`category_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_product_creator` FOREIGN KEY (`created_by`) REFERENCES `users`(`user_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Table: customers
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
  INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Table: transactions
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
  CONSTRAINT `fk_transaction_customer` FOREIGN KEY (`customer_id`) REFERENCES `customers`(`customer_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_transaction_supplier` FOREIGN KEY (`supplier_id`) REFERENCES `suppliers`(`supplier_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_transaction_product` FOREIGN KEY (`product_id`) REFERENCES `products`(`product_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_transaction_user` FOREIGN KEY (`processed_by`) REFERENCES `users`(`user_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_transaction_reference` FOREIGN KEY (`reference_transaction_id`) REFERENCES `transactions`(`transaction_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. Table: barcode
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
  CONSTRAINT `fk_barcode_product` FOREIGN KEY (`product_id`) REFERENCES `products`(`product_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. Table: stock_audit
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
  CONSTRAINT `fk_stock_audit_product` FOREIGN KEY (`product_id`) REFERENCES `products`(`product_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_stock_audit_transaction` FOREIGN KEY (`transaction_id`) REFERENCES `transactions`(`transaction_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_stock_audit_user` FOREIGN KEY (`changed_by`) REFERENCES `users`(`user_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. Table: audit_log
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
  CONSTRAINT `fk_audit_log_user` FOREIGN KEY (`performed_by`) REFERENCES `users`(`user_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 11. Table: settings
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

-- 12. Table: app_subscription
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
  INDEX `idx_sub_expiry` (`expiry_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 13. Table: email_queue
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
  INDEX `idx_dedup_key` (`dedup_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS=1;
