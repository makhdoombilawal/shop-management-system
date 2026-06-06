-- ================================================================
-- ENTERPRISE SHOP MANAGEMENT SYSTEM - SQLITE SCHEMA DDL
-- Translated for SQLite compatibility.
-- ================================================================

PRAGMA foreign_keys = OFF;

-- 1. Table: roles
CREATE TABLE IF NOT EXISTS `roles` (
  `role_id` INTEGER PRIMARY KEY AUTOINCREMENT,
  `name` TEXT NOT NULL UNIQUE,
  `description` TEXT NULL,
  `created_at` TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS `idx_role_name` ON `roles` (`name`);

-- 2. Table: suppliers
CREATE TABLE IF NOT EXISTS `suppliers` (
    `supplier_id` INTEGER PRIMARY KEY AUTOINCREMENT,
    `company_name` TEXT NOT NULL,
    `contact_person` TEXT NULL,
    `phone_number` TEXT NULL,
    `email` TEXT NULL,
    `address` TEXT NULL,
    `city` TEXT NULL,
    `country` TEXT NULL,
    `tax_number` TEXT NULL,
    `payment_terms` TEXT NULL,
    `credit_limit` REAL NULL,
    `current_balance` REAL NOT NULL DEFAULT 0.00,
    `remarks` TEXT NULL,
    `status` TEXT NOT NULL DEFAULT 'active',
    `created_at` TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TEXT NULL,
    `last_purchase_date` TEXT NULL
);
CREATE INDEX IF NOT EXISTS `idx_supplier_name` ON `suppliers` (`company_name`);
CREATE INDEX IF NOT EXISTS `idx_supplier_status` ON `suppliers` (`status`);

-- 3. Table: users
CREATE TABLE IF NOT EXISTS `users` (
  `user_id` INTEGER PRIMARY KEY AUTOINCREMENT,
  `username` TEXT NOT NULL UNIQUE,
  `password` TEXT NOT NULL,
  `full_name` TEXT NULL,
  `email` TEXT NULL,
  `role` TEXT NOT NULL DEFAULT 'CASHIER',
  `is_active` INTEGER NOT NULL DEFAULT 1,
  `is_deleted` INTEGER NOT NULL DEFAULT 0,
  `created_at` TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TEXT NULL,
  `last_login` TEXT NULL,
  `created_by` INTEGER NULL,
  FOREIGN KEY (`created_by`) REFERENCES `users`(`user_id`) ON DELETE SET NULL ON UPDATE CASCADE
);
CREATE INDEX IF NOT EXISTS `idx_username` ON `users` (`username`);
CREATE INDEX IF NOT EXISTS `idx_role` ON `users` (`role`);

-- 4. Table: categories
CREATE TABLE IF NOT EXISTS `categories` (
  `category_id` INTEGER PRIMARY KEY AUTOINCREMENT,
  `name` TEXT NOT NULL UNIQUE,
  `description` TEXT NULL,
  `parent_category_id` INTEGER NULL,
  `is_active` INTEGER NOT NULL DEFAULT 1,
  `created_at` TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TEXT NULL,
  FOREIGN KEY (`parent_category_id`) REFERENCES `categories`(`category_id`) ON DELETE SET NULL ON UPDATE CASCADE
);
CREATE INDEX IF NOT EXISTS `idx_category_name` ON `categories` (`name`);

-- 5. Table: products
CREATE TABLE IF NOT EXISTS `products` (
  `product_id` INTEGER PRIMARY KEY AUTOINCREMENT,
  `name` TEXT NOT NULL,
  `category_id` INTEGER NULL,
  `product_type` TEXT NULL,
  `description` TEXT NULL,
  `remarks` TEXT NULL,
  `stock` INTEGER NOT NULL DEFAULT 0,
  `min_stock_level` INTEGER NOT NULL DEFAULT 10,
  `sell_price` REAL NOT NULL DEFAULT 0.00,
  `purchase_price` REAL NOT NULL DEFAULT 0.00,
  `status` TEXT NOT NULL DEFAULT 'active',
  `is_deleted` INTEGER NOT NULL DEFAULT 0,
  `created_at` TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TEXT NULL,
  `created_by` INTEGER NULL,
  FOREIGN KEY (`category_id`) REFERENCES `categories`(`category_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  FOREIGN KEY (`created_by`) REFERENCES `users`(`user_id`) ON DELETE SET NULL ON UPDATE CASCADE
);
CREATE INDEX IF NOT EXISTS `idx_name` ON `products` (`name`);
CREATE INDEX IF NOT EXISTS `idx_category` ON `products` (`category_id`);
CREATE INDEX IF NOT EXISTS `idx_status` ON `products` (`status`);

-- 6. Table: customers
CREATE TABLE IF NOT EXISTS `customers` (
  `customer_id` INTEGER PRIMARY KEY AUTOINCREMENT,
  `name` TEXT NOT NULL,
  `phone_number` TEXT NULL,
  `email` TEXT NULL,
  `address` TEXT NULL,
  `city` TEXT NULL,
  `postal_code` TEXT NULL,
  `remarks` TEXT NULL,
  `date_created` TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `last_purchase_date` TEXT NULL,
  `total_purchases` REAL DEFAULT 0.00,
  `loyalty_points` INTEGER DEFAULT 0,
  `status` TEXT NOT NULL DEFAULT 'active',
  `is_deleted` INTEGER NOT NULL DEFAULT 0,
  `updated_at` TEXT NULL
);
CREATE INDEX IF NOT EXISTS `idx_customer_name` ON `customers` (`name`);
CREATE INDEX IF NOT EXISTS `idx_customer_phone` ON `customers` (`phone_number`);
CREATE INDEX IF NOT EXISTS `idx_customer_status` ON `customers` (`status`);

-- 7. Table: transactions
CREATE TABLE IF NOT EXISTS `transactions` (
  `transaction_id` INTEGER PRIMARY KEY AUTOINCREMENT,
  `transaction_date` TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `transaction_type` TEXT NOT NULL,
  `transaction_status` TEXT NOT NULL DEFAULT 'completed',
  `customer_id` INTEGER NULL,
  `supplier_id` INTEGER NULL,
  `product_id` INTEGER NOT NULL,
  `quantity` INTEGER NOT NULL,
  `sell_price` REAL NULL,
  `purchase_price` REAL NULL,
  `unit_price` REAL NOT NULL,
  `total_amount` REAL NOT NULL,
  `payment_type` TEXT NULL,
  `currency` TEXT DEFAULT 'USD',
  `discount_amount` REAL DEFAULT 0.00,
  `tax_amount` REAL DEFAULT 0.00,
  `remarks` TEXT NULL,
  `stock_before_transaction` INTEGER NULL,
  `stock_after_transaction` INTEGER NULL,
  `processed_by` INTEGER NULL,
  `reference_transaction_id` INTEGER NULL,
  FOREIGN KEY (`customer_id`) REFERENCES `customers`(`customer_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  FOREIGN KEY (`supplier_id`) REFERENCES `suppliers`(`supplier_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  FOREIGN KEY (`product_id`) REFERENCES `products`(`product_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  FOREIGN KEY (`processed_by`) REFERENCES `users`(`user_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  FOREIGN KEY (`reference_transaction_id`) REFERENCES `transactions`(`transaction_id`) ON DELETE SET NULL ON UPDATE CASCADE
);
CREATE INDEX IF NOT EXISTS `idx_trans_date` ON `transactions` (`transaction_date`);
CREATE INDEX IF NOT EXISTS `idx_trans_type` ON `transactions` (`transaction_type`);
CREATE INDEX IF NOT EXISTS `idx_trans_status` ON `transactions` (`transaction_status`);

-- 8. Table: barcode
CREATE TABLE IF NOT EXISTS `barcode` (
  `barcode_id` INTEGER PRIMARY KEY AUTOINCREMENT,
  `product_id` INTEGER NOT NULL,
  `barcode_number` TEXT NOT NULL UNIQUE,
  `status` TEXT NOT NULL DEFAULT 'available',
  `is_active` INTEGER NOT NULL DEFAULT 1,
  `warehouse_location` TEXT NULL,
  `created_at` TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `sold_at` TEXT NULL,
  `remarks` TEXT NULL,
  FOREIGN KEY (`product_id`) REFERENCES `products`(`product_id`) ON DELETE CASCADE ON UPDATE CASCADE
);
CREATE INDEX IF NOT EXISTS `idx_barcode_num` ON `barcode` (`barcode_number`);

-- 9. Table: stock_audit
CREATE TABLE IF NOT EXISTS `stock_audit` (
  `audit_id` INTEGER PRIMARY KEY AUTOINCREMENT,
  `product_id` INTEGER NOT NULL,
  `change_date` TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `stock_before` INTEGER NOT NULL,
  `stock_after` INTEGER NOT NULL,
  `change_amount` INTEGER NOT NULL,
  `change_source` TEXT NOT NULL,
  `transaction_id` INTEGER NULL,
  `changed_by` INTEGER NULL,
  `remarks` TEXT NULL,
  FOREIGN KEY (`product_id`) REFERENCES `products`(`product_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  FOREIGN KEY (`transaction_id`) REFERENCES `transactions`(`transaction_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  FOREIGN KEY (`changed_by`) REFERENCES `users`(`user_id`) ON DELETE SET NULL ON UPDATE CASCADE
);
CREATE INDEX IF NOT EXISTS `idx_sa_product` ON `stock_audit` (`product_id`);
CREATE INDEX IF NOT EXISTS `idx_sa_change_date` ON `stock_audit` (`change_date`);

-- 10. Table: audit_log
CREATE TABLE IF NOT EXISTS `audit_log` (
  `log_id` INTEGER PRIMARY KEY AUTOINCREMENT,
  `log_date` TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `entity_type` TEXT NOT NULL,
  `entity_id` INTEGER NOT NULL,
  `action` TEXT NOT NULL,
  `performed_by` INTEGER NULL,
  `old_value` TEXT NULL,
  `new_value` TEXT NULL,
  `ip_address` TEXT NULL,
  `remarks` TEXT NULL,
  FOREIGN KEY (`performed_by`) REFERENCES `users`(`user_id`) ON DELETE SET NULL ON UPDATE CASCADE
);
CREATE INDEX IF NOT EXISTS `idx_al_log_date` ON `audit_log` (`log_date`);

-- 11. Table: settings
CREATE TABLE IF NOT EXISTS `settings` (
  `id` INTEGER PRIMARY KEY AUTOINCREMENT,
  `company_name` TEXT NULL,
  `company_phone` TEXT NULL,
  `company_email` TEXT NULL,
  `company_address` TEXT NULL,
  `company_details` TEXT NULL,
  `tax_rate` REAL NULL,
  `discount_rate` REAL NULL,
  `tax_notes` TEXT NULL,
  `receipt_template` TEXT NULL,
  `print_barcodes` INTEGER NULL,
  `print_item_details` INTEGER NULL,
  `auto_backup` INTEGER NULL,
  `backup_frequency` TEXT NULL,
  `backup_path` TEXT NULL,
  `dark_mode` INTEGER NULL,
  `theme` TEXT NULL,
  `language` TEXT NULL,
  `show_developer_credit` INTEGER NULL,
  `developer_name` TEXT NULL,
  `developer_contact` TEXT NULL,
  `updated_at` TEXT NULL,
  `updated_by` TEXT NULL,
  `payment_cycle_start_date` TEXT NULL
);

-- 12. Table: app_subscription
CREATE TABLE IF NOT EXISTS `app_subscription` (
  `id` INTEGER PRIMARY KEY AUTOINCREMENT,
  `installed_version` TEXT NOT NULL,
  `install_date` TEXT NOT NULL,
  `expiry_date` TEXT NOT NULL,
  `status` TEXT NOT NULL,
  `otp_code` TEXT NULL,
  `otp_created_at` TEXT NULL,
  `otp_expiry_at` TEXT NULL,
  `otp_used` INTEGER NOT NULL DEFAULT 0,
  `last_otp_attempt_count` INTEGER NOT NULL DEFAULT 0,
  `otp_locked_until` TEXT NULL,
  `otp_status` TEXT NULL,
  `expiry_handled` INTEGER NOT NULL DEFAULT 0,
  `device_id` TEXT NULL,
  `device_name` TEXT NULL,
  `os_name` TEXT NULL,
  `hostname` TEXT NULL,
  `mac_address` TEXT NULL,
  `installed_by` TEXT NULL,
  `shop_name` TEXT NULL,
  `machine_id` TEXT NULL,
  `renewal_count` INTEGER NOT NULL DEFAULT 0,
  `last_renewal_date` TEXT NULL,
  `created_at` TEXT NOT NULL,
  `updated_at` TEXT NULL
);
CREATE INDEX IF NOT EXISTS `idx_sub_status` ON `app_subscription` (`status`);
CREATE INDEX IF NOT EXISTS `idx_sub_expiry` ON `app_subscription` (`expiry_date`);

-- 13. Table: email_queue
CREATE TABLE IF NOT EXISTS `email_queue` (
  `id` INTEGER PRIMARY KEY AUTOINCREMENT,
  `email_type` TEXT NOT NULL,
  `recipient` TEXT NOT NULL,
  `subject` TEXT NOT NULL,
  `body` TEXT NOT NULL,
  `status` TEXT NOT NULL DEFAULT 'PENDING',
  `retry_count` INTEGER NOT NULL DEFAULT 0,
  `last_retry_at` TEXT NULL,
  `error_message` TEXT NULL,
  `created_at` TEXT NOT NULL,
  `updated_at` TEXT NULL,
  `sent_at` TEXT NULL,
  `dedup_key` TEXT NULL
);
CREATE INDEX IF NOT EXISTS `idx_email_status` ON `email_queue` (`status`);
CREATE INDEX IF NOT EXISTS `idx_email_dedup` ON `email_queue` (`dedup_key`);

PRAGMA foreign_keys = ON;
