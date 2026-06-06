-- ================================================================
-- TABLE: roles
-- Role-Based Access Control (ADMIN, MANAGER, CASHIER)
-- ================================================================
CREATE TABLE IF NOT EXISTS `roles` (
  `role_id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(50) NOT NULL UNIQUE,
  `description` VARCHAR(255) NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`role_id`),
  INDEX `idx_role_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
