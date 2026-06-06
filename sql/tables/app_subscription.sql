-- ================================================================
-- TABLE: app_subscription
-- Subscription Validation and Security Bindings
-- ================================================================
CREATE TABLE IF NOT EXISTS `app_subscription` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `installed_version` VARCHAR(255) NOT NULL,
  `install_date` DATE NOT NULL,
  `expiry_date` DATE NOT NULL,
  `status` VARCHAR(20) NOT NULL,
  `otp_code` VARCHAR(4) NULL,
  `otp_created_at` DATETIME NULL,
  `otp_expiry_at` DATETIME NULL,
  `otp_used` BOOLEAN NOT NULL DEFAULT FALSE,
  `last_otp_attempt_count` INT NOT NULL DEFAULT 0,
  `otp_locked_until` DATETIME NULL,
  `otp_status` VARCHAR(10) NULL,
  `expiry_handled` BOOLEAN NOT NULL DEFAULT FALSE,
  `device_id` VARCHAR(255) NULL,
  `device_name` VARCHAR(255) NULL,
  `os_name` VARCHAR(100) NULL,
  `hostname` VARCHAR(255) NULL,
  `mac_address` VARCHAR(255) NULL,
  `installed_by` VARCHAR(255) NULL,
  `shop_name` VARCHAR(255) NULL,
  `machine_id` VARCHAR(255) NULL,
  `renewal_count` INT NOT NULL DEFAULT 0,
  `last_renewal_date` DATE NULL,
  `created_at` DATETIME NOT NULL,
  `updated_at` DATETIME NULL,
  PRIMARY KEY (`id`),
  INDEX `idx_sub_status` (`status`),
  INDEX `idx_sub_expiry` (`expiry_date`),
  INDEX `idx_sub_device` (`device_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
