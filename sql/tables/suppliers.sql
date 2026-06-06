-- ================================================================
-- TABLE: suppliers
-- Purchase Source Management
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
