-- ============================================================================
-- ENTERPRISE SUBSCRIPTION STABILITY FIX - Schema Migration
-- ============================================================================

-- 1. Add otp_status to prevent duplicate OTP generation
ALTER TABLE app_subscription ADD COLUMN IF NOT EXISTS otp_status VARCHAR(10) DEFAULT NULL;
-- Values: NULL (no OTP), PENDING (generated but not emailed), SENT (email dispatched), USED (verified)

-- 2. Add expiry_handled flag to prevent expiry trigger loop
ALTER TABLE app_subscription ADD COLUMN IF NOT EXISTS expiry_handled BOOLEAN DEFAULT FALSE;

-- 3. Add dedup_key to email_queue for email deduplication
ALTER TABLE email_queue ADD COLUMN IF NOT EXISTS dedup_key VARCHAR(255) DEFAULT NULL;
ALTER TABLE email_queue ADD INDEX IF NOT EXISTS idx_dedup_key (dedup_key);
