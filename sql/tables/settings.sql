-- ================================================================
-- TABLE: settings
-- System Configuration Store (Single-Row Table)
-- ================================================================
CREATE TABLE IF NOT EXISTS `settings` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `company_name` VARCHAR(200) NULL,
  `company_phone` VARCHAR(50) NULL,
  `company_email` VARCHAR(100) NULL,
  `company_address` VARCHAR(500) NULL,
  `company_details` VARCHAR(2000) NULL,
  `tax_rate` DOUBLE NULL,
  `discount_rate` DOUBLE NULL,
  `tax_notes` VARCHAR(2000) NULL,
  `receipt_template` VARCHAR(2000) NULL,
  `print_barcodes` BOOLEAN NULL,
  `print_item_details` BOOLEAN NULL,
  `auto_backup` BOOLEAN NULL,
  `backup_frequency` VARCHAR(50) NULL,
  `backup_path` VARCHAR(500) NULL,
  `dark_mode` BOOLEAN NULL,
  `theme` VARCHAR(50) NULL,
  `language` VARCHAR(50) NULL,
  `show_developer_credit` BOOLEAN NULL,
  `developer_name` VARCHAR(200) NULL,
  `developer_contact` VARCHAR(100) NULL,
  `updated_at` DATETIME NULL,
  `updated_by` VARCHAR(100) NULL,
  `payment_cycle_start_date` DATETIME NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
