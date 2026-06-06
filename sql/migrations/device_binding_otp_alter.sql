-- ============================================================================
-- ENTERPRISE SUBSCRIPTION - DEVICE BINDING & OTP UPDATE
-- ============================================================================

-- Add new columns to app_subscription
ALTER TABLE app_subscription 
    ADD COLUMN IF NOT EXISTS device_id VARCHAR(255),
    ADD COLUMN IF NOT EXISTS device_name VARCHAR(255),
    ADD COLUMN IF NOT EXISTS os_name VARCHAR(100),
    ADD COLUMN IF NOT EXISTS hostname VARCHAR(255),
    ADD COLUMN IF NOT EXISTS mac_address VARCHAR(255),
    ADD COLUMN IF NOT EXISTS installed_by VARCHAR(255),
    ADD COLUMN IF NOT EXISTS shop_name VARCHAR(255),
    ADD COLUMN IF NOT EXISTS otp_created_at TIMESTAMP NULL,
    ADD COLUMN IF NOT EXISTS otp_expiry_at TIMESTAMP NULL;

-- Optional: Create index on device_id for faster lookups during startup
CREATE INDEX idx_sub_device_id ON app_subscription(device_id);
