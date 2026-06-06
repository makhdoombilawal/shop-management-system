package models.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Email queue entity for offline-first email delivery.
 * Stores unsent emails and retries when internet becomes available.
 * 
 * @author Shop Management System
 * @version Enterprise Email System
 */
@Entity
@Table(name = "email_queue")
public class EmailQueueEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email_type", nullable = false, length = 50)
    private String emailType; // INSTALLATION, EXPIRY, OTP, RENEWAL_SUCCESS

    @Column(name = "recipient", nullable = false)
    private String recipient;

    @Column(name = "subject", nullable = false)
    private String subject;

    @Column(name = "body", nullable = false, columnDefinition = "LONGTEXT")
    private String body;

    @Column(name = "status", nullable = false, length = 20)
    private String status; // PENDING, SENT, FAILED

    @Column(name = "retry_count")
    private Integer retryCount = 0;

    @Column(name = "last_retry_at")
    private LocalDateTime lastRetryAt;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "dedup_key", length = 255)
    private String dedupKey;

    // ========================================================================
    // CONSTRUCTORS
    // ========================================================================

    public EmailQueueEntity() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.retryCount = 0;
        this.status = "PENDING";
    }

    public EmailQueueEntity(String emailType, String recipient, String subject, String body) {
        this();
        this.emailType = emailType;
        this.recipient = recipient;
        this.subject = subject;
        this.body = body;
    }

    // ========================================================================
    // BUSINESS METHODS
    // ========================================================================

    /**
     * Mark email as sent
     */
    public void markAsSent() {
        this.status = "SENT";
        this.sentAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.errorMessage = null;
    }

    /**
     * Mark email as failed and increment retry count
     */
    public void markAsFailed(String errorMsg) {
        this.status = "FAILED";
        this.retryCount++;
        this.lastRetryAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.errorMessage = errorMsg;
    }

    /**
     * Reset for retry (change status back to PENDING)
     */
    public void resetForRetry() {
        if (this.retryCount < 3) {
            this.status = "PENDING";
            this.lastRetryAt = LocalDateTime.now();
            this.updatedAt = LocalDateTime.now();
            this.errorMessage = null;
        } else {
            this.status = "FAILED";
        }
    }

    /**
     * Check if email should be retried
     */
    public boolean shouldRetry() {
        return "PENDING".equals(this.status) || (this.retryCount < 3 && "FAILED".equals(this.status));
    }

    /**
     * Check if retry count exceeded
     */
    public boolean isRetryExhausted() {
        return this.retryCount >= 3;
    }

    /**
     * Check if enough time has passed for retry (backoff: 5-10 minutes between retries)
     */
    public boolean isReadyForRetry() {
        if (this.lastRetryAt == null) {
            return true; // Never tried, ready now
        }
        LocalDateTime nextRetryTime = this.lastRetryAt.plusMinutes(5 + (this.retryCount * 2));
        return LocalDateTime.now().isAfter(nextRetryTime);
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

    public String getEmailType() {
        return emailType;
    }

    public void setEmailType(String emailType) {
        this.emailType = emailType;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public LocalDateTime getLastRetryAt() {
        return lastRetryAt;
    }

    public void setLastRetryAt(LocalDateTime lastRetryAt) {
        this.lastRetryAt = lastRetryAt;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
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

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public String getDedupKey() {
        return dedupKey;
    }

    public void setDedupKey(String dedupKey) {
        this.dedupKey = dedupKey;
    }

    @Override
    public String toString() {
        return "EmailQueueEntity{" +
                "id=" + id +
                ", emailType='" + emailType + '\'' +
                ", recipient='" + recipient + '\'' +
                ", status='" + status + '\'' +
                ", retryCount=" + retryCount +
                ", createdAt=" + createdAt +
                '}';
    }
}
