-- ================================================================
-- TABLE: categories
-- Product Categories hierarchy
-- ================================================================
CREATE TABLE IF NOT EXISTS `categories` (
  `category_id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL UNIQUE,
  `description` TEXT NULL,
  `parent_category_id` INT NULL,
  `is_active` BOOLEAN NOT NULL DEFAULT TRUE,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`category_id`),
  INDEX `idx_category_name` (`name`),
  INDEX `idx_parent` (`parent_category_id`),
  CONSTRAINT `fk_category_parent` 
    FOREIGN KEY (`parent_category_id`) 
    REFERENCES `categories`(`category_id`) 
    ON DELETE SET NULL 
    ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
