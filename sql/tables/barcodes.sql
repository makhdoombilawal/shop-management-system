-- ================================================================
-- TABLE: barcode
-- Barcode Management for Products
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
