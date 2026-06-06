-- ================================================================
-- TABLE: transactions
-- Sales, Purchases, and Returns Transactions Registry
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
