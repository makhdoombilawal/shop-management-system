package models.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * JPA Entity for Audit Log (System-Wide Audit Trail)
 * Logs all critical operations for compliance and security
 * 
 * @author Shop Management System
 */
@Entity
@Table(name = "audit_log", indexes = {
    @Index(name = "idx_log_date", columnList = "log_date"),
    @Index(name = "idx_entity", columnList = "entity_type, entity_id"),
    @Index(name = "idx_action", columnList = "action"),
    @Index(name = "idx_performed_by", columnList = "performed_by")
})
public class AuditLogEntity implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Integer logId;
    
    @Column(name = "log_date", nullable = false)
    private LocalDateTime logDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false, length = 20)
    private EntityType entityType;
    
    @Column(name = "entity_id", nullable = false)
    private Integer entityId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 20)
    private Action action;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_by")
    private UserEntity performedBy;
    
    @Column(name = "old_value", length = 2000)
    private String oldValue;
    
    @Column(name = "new_value", length = 2000)
    private String newValue;
    
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    @Column(name = "remarks", length = 2000)
    private String remarks;
    
    // Enums
    public enum EntityType {
        USER, PRODUCT, CUSTOMER, TRANSACTION, BARCODE, CATEGORY
    }
    
    public enum Action {
        CREATE, UPDATE, DELETE, LOGIN, LOGOUT, PRICE_CHANGE, STATUS_CHANGE
    }
    
    // Constructors
    public AuditLogEntity() {
        this.logDate = LocalDateTime.now();
    }
    
    public AuditLogEntity(EntityType entityType, Integer entityId, Action action, 
                         UserEntity performedBy) {
        this();
        this.entityType = entityType;
        this.entityId = entityId;
        this.action = action;
        this.performedBy = performedBy;
    }
    
    // Lifecycle callback
    @PrePersist
    protected void onCreate() {
        if (this.logDate == null) {
            this.logDate = LocalDateTime.now();
        }
    }
    
    // Business methods
    public boolean isSecurityEvent() {
        return action == Action.LOGIN || action == Action.LOGOUT;
    }
    
    public boolean isDataModification() {
        return action == Action.CREATE || action == Action.UPDATE || action == Action.DELETE;
    }
    
    public boolean hasValueChange() {
        return oldValue != null && newValue != null;
    }
    
    public String getPerformedByName() {
        return performedBy != null ? performedBy.getUsername() : "SYSTEM";
    }
    
    // Static factory methods
    public static AuditLogEntity logCreate(EntityType entityType, Integer entityId, 
                                          UserEntity performer, String newValue) {
        AuditLogEntity log = new AuditLogEntity(entityType, entityId, Action.CREATE, performer);
        log.setNewValue(newValue);
        return log;
    }
    
    public static AuditLogEntity logUpdate(EntityType entityType, Integer entityId, 
                                          UserEntity performer, String oldValue, String newValue) {
        AuditLogEntity log = new AuditLogEntity(entityType, entityId, Action.UPDATE, performer);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        return log;
    }
    
    public static AuditLogEntity logDelete(EntityType entityType, Integer entityId, 
                                          UserEntity performer, String oldValue) {
        AuditLogEntity log = new AuditLogEntity(entityType, entityId, Action.DELETE, performer);
        log.setOldValue(oldValue);
        return log;
    }
    
    public static AuditLogEntity logLogin(Integer userId, UserEntity performer, String ipAddress) {
        AuditLogEntity log = new AuditLogEntity(EntityType.USER, userId, Action.LOGIN, performer);
        log.setIpAddress(ipAddress);
        return log;
    }
    
    // Getters and Setters
    public Integer getLogId() {
        return logId;
    }
    
    public void setLogId(Integer logId) {
        this.logId = logId;
    }
    
    public LocalDateTime getLogDate() {
        return logDate;
    }
    
    public void setLogDate(LocalDateTime logDate) {
        this.logDate = logDate;
    }
    
    public EntityType getEntityType() {
        return entityType;
    }
    
    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }
    
    public Integer getEntityId() {
        return entityId;
    }
    
    public void setEntityId(Integer entityId) {
        this.entityId = entityId;
    }
    
    public Action getAction() {
        return action;
    }
    
    public void setAction(Action action) {
        this.action = action;
    }
    
    public UserEntity getPerformedBy() {
        return performedBy;
    }
    
    public void setPerformedBy(UserEntity performedBy) {
        this.performedBy = performedBy;
    }
    
    public String getOldValue() {
        return oldValue;
    }
    
    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }
    
    public String getNewValue() {
        return newValue;
    }
    
    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public String getRemarks() {
        return remarks;
    }
    
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
    
    @Override
    public String toString() {
        return "AuditLogEntity{" +
                "logId=" + logId +
                ", entityType=" + entityType +
                ", entityId=" + entityId +
                ", action=" + action +
                ", logDate=" + logDate +
                ", performedBy=" + getPerformedByName() +
                '}';
    }
}
