-- ============================================================================
-- ALTER MIGRATION SCRIPT FOR ENTERPRISE SUBSCRIPTION SYSTEM
-- Adds new columns to the app_subscription table for OTP lockout, machine fingerprinting, and renewal tracking.
-- ============================================================================

-- Add OTP cooldown/lockout field
ALTER TABLE app_subscription 
    ADD COLUMN IF NOT EXISTS otp_locked_until TIMESTAMP NULL;

-- Add machine fingerprint column  
ALTER TABLE app_subscription 
    ADD COLUMN IF NOT EXISTS machine_id VARCHAR(255) NULL;

-- Add renewal tracking columns
ALTER TABLE app_subscription 
    ADD COLUMN IF NOT EXISTS renewal_count INT NOT NULL DEFAULT 0;
ALTER TABLE app_subscription 
    ADD COLUMN IF NOT EXISTS last_renewal_date DATE NULL;

-- Add indexes for optimization
CREATE INDEX IF NOT EXISTS idx_sub_status ON app_subscription(status);
CREATE INDEX IF NOT EXISTS idx_sub_expiry ON app_subscription(expiry_date);
CREATE INDEX IF NOT EXISTS idx_email_status ON email_queue(status);
CREATE INDEX IF NOT EXISTS idx_email_type ON email_queue(email_type);
