-- ================================================================
-- TABLE: audit_log
-- System-Wide Event Auditing
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
