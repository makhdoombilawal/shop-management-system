-- ================================================================
-- TABLE: email_queue
-- Offline-First Email Dispatcher Queue
-- ================================================================
CREATE TABLE IF NOT EXISTS `email_queue` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `email_type` VARCHAR(50) NOT NULL,
  `recipient` VARCHAR(255) NOT NULL,
  `subject` VARCHAR(255) NOT NULL,
  `body` LONGTEXT NOT NULL,
  `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  `retry_count` INT NOT NULL DEFAULT 0,
  `last_retry_at` DATETIME NULL,
  `error_message` TEXT NULL,
  `created_at` DATETIME NOT NULL,
  `updated_at` DATETIME NULL,
  `sent_at` DATETIME NULL,
  `dedup_key` VARCHAR(255) NULL,
  PRIMARY KEY (`id`),
  INDEX `idx_email_status` (`status`),
  INDEX `idx_email_type` (`email_type`),
  INDEX `idx_dedup_key` (`dedup_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
