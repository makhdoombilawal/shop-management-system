-- ================================================================
-- TABLE: products
-- Inventory Management
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
