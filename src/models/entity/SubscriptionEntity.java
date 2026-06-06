package models.entity;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Enterprise subscription entity with OTP-based renewal system.
 * Tracks 2-month subscription expiry and manages OTP verification.
 * 
 * @author Shop Management System
 * @version Enterprise Subscription System
 */
@Entity
@Table(name = "app_subscription")
public class SubscriptionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "installed_version", nullable = false)
    private String installedVersion;

    @Column(name = "install_date", nullable = false)
    private LocalDate installDate;

    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @Column(name = "status", nullable = false, length = 20)
    private String status; // ACTIVE, EXPIRED

    @Column(name = "otp_code", length = 4)
    private String otpCode; // Hashed OTP (4-digit)

    @Column(name = "otp_created_at")
    private LocalDateTime otpCreatedAt;

    @Column(name = "otp_expiry_at")
    private LocalDateTime otpExpiryAt;

    @Column(name = "otp_used")
    private Boolean otpUsed = false;

    @Column(name = "last_otp_attempt_count")
    private Integer lastOtpAttemptCount = 0;

    @Column(name = "otp_locked_until")
    private LocalDateTime otpLockedUntil;

    @Column(name = "otp_status", length = 10)
    private String otpStatus; // NULL, PENDING, SENT, USED

    @Column(name = "expiry_handled")
    private Boolean expiryHandled = false;

    @Column(name = "device_id", length = 255)
    private String deviceId;

    @Column(name = "device_name", length = 255)
    private String deviceName;

    @Column(name = "os_name", length = 100)
    private String osName;

    @Column(name = "hostname", length = 255)
    private String hostname;

    @Column(name = "mac_address", length = 255)
    private String macAddress;

    @Column(name = "installed_by", length = 255)
    private String installedBy;

    @Column(name = "shop_name", length = 255)
    private String shopName;

    @Column(name = "machine_id", length = 255)
    private String machineId; // Deprecated, kept for backward compatibility

    @Column(name = "renewal_count", nullable = false)
    private Integer renewalCount = 0;

    @Column(name = "last_renewal_date")
    private LocalDate lastRenewalDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ========================================================================
    // CONSTRUCTORS
    // ========================================================================

    public SubscriptionEntity() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.otpUsed = false;
        this.lastOtpAttemptCount = 0;
        this.renewalCount = 0;
    }

    public SubscriptionEntity(String installedVersion, LocalDate installDate, LocalDate expiryDate) {
        this();
        this.installedVersion = installedVersion;
        this.installDate = installDate;
        this.expiryDate = expiryDate;
        this.status = "ACTIVE";
    }

    // ========================================================================
    // BUSINESS METHODS
    // ========================================================================

    /**
     * Check if subscription is expired
     */
    public boolean isExpired() {
        return LocalDate.now().isAfter(this.expiryDate);
    }

    /**
     * Check if subscription is active
     */
    public boolean isActive() {
        return "ACTIVE".equals(this.status) && !isExpired();
    }

    /**
     * Update subscription status based on expiry date
     */
    public void updateStatus() {
        this.status = isExpired() ? "EXPIRED" : "ACTIVE";
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Set OTP code (should be hashed in production)
     */
    public void setNewOTP(String otpCode) {
        this.otpCode = otpCode;
        this.otpCreatedAt = LocalDateTime.now();
        this.otpExpiryAt = this.otpCreatedAt.plusHours(24);
        this.otpUsed = false;
        this.otpStatus = "PENDING";
        this.lastOtpAttemptCount = 0;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Verify OTP and extend subscription
     */
    public boolean verifyAndExtendSubscription(String providedOtp, int extensionMonths) {
        if (this.otpUsed) {
            return false;
        }

        if (!Objects.equals(this.otpCode, providedOtp)) {
            this.lastOtpAttemptCount++;
            this.updatedAt = LocalDateTime.now();
            return false;
        }

        // OTP is correct - extend subscription
        this.expiryDate = this.expiryDate.plusMonths(extensionMonths);
        this.otpUsed = true;
        this.status = "ACTIVE";
        this.updatedAt = LocalDateTime.now();
        return true;
    }

    /**
     * Mark OTP as used
     */
    public void markOtpAsUsed() {
        this.otpUsed = true;
        this.otpStatus = "USED";
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Get remaining days in subscription
     */
    public long getRemainingDays() {
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), this.expiryDate);
    }

    /**
     * Check if OTP is expired (valid for 24 hours)
     */
    public boolean isOtpExpired() {
        if (this.otpExpiryAt == null) {
            return true;
        }
        return LocalDateTime.now().isAfter(this.otpExpiryAt);
    }

    /**
     * Get OTP attempt remaining (3 attempts allowed)
     */
    public int getOtpAttemptsRemaining() {
        return Math.max(0, 3 - (this.lastOtpAttemptCount != null ? this.lastOtpAttemptCount : 0));
    }

    /**
     * Check if OTP cooldown is currently active.
     */
    public boolean isOtpCooldownActive() {
        return this.otpLockedUntil != null && LocalDateTime.now().isBefore(this.otpLockedUntil);
    }

    // ========================================================================
    // GETTERS & SETTERS
    // ========================================================================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getInstalledVersion() {
        return installedVersion;
    }

    public void setInstalledVersion(String installedVersion) {
        this.installedVersion = installedVersion;
    }

    public LocalDate getInstallDate() {
        return installDate;
    }

    public void setInstallDate(LocalDate installDate) {
        this.installDate = installDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOtpCode() {
        return otpCode;
    }

    public void setOtpCode(String otpCode) {
        this.otpCode = otpCode;
    }

    public LocalDateTime getOtpCreatedAt() {
        return otpCreatedAt;
    }

    public void setOtpCreatedAt(LocalDateTime otpCreatedAt) {
        this.otpCreatedAt = otpCreatedAt;
    }

    public LocalDateTime getOtpExpiryAt() {
        return otpExpiryAt;
    }

    public void setOtpExpiryAt(LocalDateTime otpExpiryAt) {
        this.otpExpiryAt = otpExpiryAt;
    }

    public Boolean getOtpUsed() {
        return otpUsed;
    }

    public void setOtpUsed(Boolean otpUsed) {
        this.otpUsed = otpUsed;
    }

    public Integer getLastOtpAttemptCount() {
        return lastOtpAttemptCount;
    }

    public void setLastOtpAttemptCount(Integer lastOtpAttemptCount) {
        this.lastOtpAttemptCount = lastOtpAttemptCount;
    }

    public LocalDateTime getOtpLockedUntil() {
        return otpLockedUntil;
    }

    public void setOtpLockedUntil(LocalDateTime otpLockedUntil) {
        this.otpLockedUntil = otpLockedUntil;
    }

    public String getMachineId() {
        return machineId;
    }

    public void setMachineId(String machineId) {
        this.machineId = machineId;
    }

    public Integer getRenewalCount() {
        return renewalCount;
    }

    public void setRenewalCount(Integer renewalCount) {
        this.renewalCount = renewalCount;
    }

    public LocalDate getLastRenewalDate() {
        return lastRenewalDate;
    }

    public void setLastRenewalDate(LocalDate lastRenewalDate) {
        this.lastRenewalDate = lastRenewalDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getOtpStatus() {
        return otpStatus;
    }

    public void setOtpStatus(String otpStatus) {
        this.otpStatus = otpStatus;
    }

    public Boolean getExpiryHandled() {
        return expiryHandled;
    }

    public void setExpiryHandled(Boolean expiryHandled) {
        this.expiryHandled = expiryHandled;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getOsName() {
        return osName;
    }

    public void setOsName(String osName) {
        this.osName = osName;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getInstalledBy() {
        return installedBy;
    }

    public void setInstalledBy(String installedBy) {
        this.installedBy = installedBy;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    @Override
    public String toString() {
        return "SubscriptionEntity{" +
                "id=" + id +
                ", installedVersion='" + installedVersion + '\'' +
                ", installDate=" + installDate +
                ", expiryDate=" + expiryDate +
                ", status='" + status + '\'' +
                ", otpUsed=" + otpUsed +
                ", remainingDays=" + getRemainingDays() +
                ", otpLockedUntil=" + otpLockedUntil +
                ", machineId='" + machineId + '\'' +
                ", renewalCount=" + renewalCount +
                ", lastRenewalDate=" + lastRenewalDate +
                '}';
    }
}
