-- ================================================================
-- TABLE: stock_audit
-- Stock Adjustment and Change Auditing
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
