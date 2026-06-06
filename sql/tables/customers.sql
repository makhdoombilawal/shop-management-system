-- ================================================================
-- TABLE: customers
-- Customer CRM Profile Data
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
