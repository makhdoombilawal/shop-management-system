-- ================================================================
-- TABLE: users
-- Authentication and Authorization
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
