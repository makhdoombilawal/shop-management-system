package service;

import dao.AuditLogHibernateDAO;
import dao.UserHibernateDAO;
import models.entity.AuditLogEntity;
import models.entity.AuditLogEntity.Action;
import models.entity.AuditLogEntity.EntityType;
import models.entity.UserEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service layer for Audit Log management
 * Provides business logic for system-wide audit trail
 * 
 * @author Shop Management System
 */
public class AuditLogService {
    
    private final AuditLogHibernateDAO auditLogDAO;
    private final UserHibernateDAO userDAO;
    
    public AuditLogService() {
        this.auditLogDAO = new AuditLogHibernateDAO();
        this.userDAO = new UserHibernateDAO();
    }
    
    /**
     * Create a generic audit log entry
     */
    public void createAuditLog(EntityType entityType, Integer entityId, Action action,
                              String oldValue, String newValue, Integer userId, 
                              String ipAddress, String remarks) {
        try {
            AuditLogEntity audit = new AuditLogEntity();
            audit.setEntityType(entityType);
            audit.setEntityId(entityId);
            audit.setAction(action);
            audit.setOldValue(oldValue);
            audit.setNewValue(newValue);
            audit.setLogDate(LocalDateTime.now());
            audit.setIpAddress(ipAddress);
            audit.setRemarks(remarks);
            
            // Set user if provided
            if (userId != null) {
                Optional<UserEntity> user = userDAO.findById(userId);
                user.ifPresent(audit::setPerformedBy);
            }
            
            auditLogDAO.save(audit);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create audit log: " + e.getMessage(), e);
        }
    }
    
    /**
     * Log a CREATE action
     */
    public void logCreate(EntityType entityType, Integer entityId, String newValue, 
                         Integer userId, String ipAddress) {
        createAuditLog(entityType, entityId, Action.CREATE, null, newValue, 
                      userId, ipAddress, entityType + " created");
    }
    
    /**
     * Log an UPDATE action
     */
    public void logUpdate(EntityType entityType, Integer entityId, String oldValue, 
                         String newValue, Integer userId, String ipAddress) {
        createAuditLog(entityType, entityId, Action.UPDATE, oldValue, newValue, 
                      userId, ipAddress, entityType + " updated");
    }
    
    /**
     * Log a DELETE action
     */
    public void logDelete(EntityType entityType, Integer entityId, String oldValue, 
                         Integer userId, String ipAddress) {
        createAuditLog(entityType, entityId, Action.DELETE, oldValue, null, 
                      userId, ipAddress, entityType + " deleted");
    }
    
    /**
     * Log a LOGIN action
     */
    public void logLogin(Integer userId, String ipAddress, boolean success) {
        String remarks = success ? "User login successful" : "User login failed";
        createAuditLog(EntityType.USER, userId, Action.LOGIN, null, null, 
                      userId, ipAddress, remarks);
    }
    
    /**
     * Log a LOGOUT action
     */
    public void logLogout(Integer userId, String ipAddress) {
        createAuditLog(EntityType.USER, userId, Action.LOGOUT, null, null, 
                      userId, ipAddress, "User logout");
    }
    
    /**
     * Log a PRICE_CHANGE action
     */
    public void logPriceChange(Integer productId, String oldPrice, String newPrice, 
                              Integer userId, String ipAddress) {
        String remarks = "Product price changed from " + oldPrice + " to " + newPrice;
        createAuditLog(EntityType.PRODUCT, productId, Action.PRICE_CHANGE, oldPrice, newPrice, 
                      userId, ipAddress, remarks);
    }
    
    /**
     * Log a STATUS_CHANGE action
     */
    public void logStatusChange(EntityType entityType, Integer entityId, 
                               String oldStatus, String newStatus, 
                               Integer userId, String ipAddress) {
        String remarks = entityType + " status changed from " + oldStatus + " to " + newStatus;
        createAuditLog(entityType, entityId, Action.STATUS_CHANGE, oldStatus, newStatus, 
                      userId, ipAddress, remarks);
    }
    
    /**
     * Get all audit logs
     */
    public List<AuditLogEntity> getAllAuditLogs() {
        try {
            return auditLogDAO.findAll();
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve audit logs: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get audit log by ID
     */
    public Optional<AuditLogEntity> getAuditLogById(Integer logId) {
        try {
            if (logId == null || logId <= 0) {
                throw new IllegalArgumentException("Invalid log ID");
            }
            return auditLogDAO.findById(logId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to find audit log: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get audit logs by entity type
     */
    public List<AuditLogEntity> getLogsByEntityType(EntityType entityType) {
        try {
            if (entityType == null) {
                throw new IllegalArgumentException("Entity type is required");
            }
            return auditLogDAO.findByEntityType(entityType);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve logs by entity type: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get audit logs for a specific entity
     */
    public List<AuditLogEntity> getLogsForEntity(EntityType entityType, Integer entityId) {
        try {
            if (entityType == null || entityId == null || entityId <= 0) {
                throw new IllegalArgumentException("Entity type and ID are required");
            }
            return auditLogDAO.findByEntity(entityType, entityId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve logs for entity: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get audit logs by action
     */
    public List<AuditLogEntity> getLogsByAction(Action action) {
        try {
            if (action == null) {
                throw new IllegalArgumentException("Action is required");
            }
            return auditLogDAO.findByAction(action);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve logs by action: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get audit logs by user
     */
    public List<AuditLogEntity> getLogsByUser(Integer userId) {
        try {
            if (userId == null || userId <= 0) {
                throw new IllegalArgumentException("Invalid user ID");
            }
            return auditLogDAO.findByUser(userId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve logs by user: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get audit logs by date range
     */
    public List<AuditLogEntity> getLogsByDateRange(LocalDate startDate, LocalDate endDate) {
        try {
            if (startDate == null || endDate == null) {
                throw new IllegalArgumentException("Start date and end date are required");
            }
            if (startDate.isAfter(endDate)) {
                throw new IllegalArgumentException("Start date cannot be after end date");
            }
            return auditLogDAO.findByDateRange(startDate, endDate);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve logs by date range: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get security events (logins/logouts)
     */
    public List<AuditLogEntity> getSecurityEvents() {
        try {
            return auditLogDAO.findSecurityEvents();
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve security events: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get data modification events (create/update/delete)
     */
    public List<AuditLogEntity> getDataModifications() {
        try {
            return auditLogDAO.findDataModifications();
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve data modifications: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get recent audit logs
     */
    public List<AuditLogEntity> getRecentLogs(int limit) {
        try {
            if (limit <= 0) {
                throw new IllegalArgumentException("Limit must be positive");
            }
            return auditLogDAO.findRecent(limit);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve recent logs: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get today's audit logs
     */
    public List<AuditLogEntity> getTodayLogs() {
        try {
            return auditLogDAO.findTodayLogs();
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve today's logs: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get audit logs by IP address
     */
    public List<AuditLogEntity> getLogsByIpAddress(String ipAddress) {
        try {
            if (ipAddress == null || ipAddress.trim().isEmpty()) {
                throw new IllegalArgumentException("IP address is required");
            }
            return auditLogDAO.findByIpAddress(ipAddress.trim());
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve logs by IP address: " + e.getMessage(), e);
        }
    }
    
    /**
     * Search audit logs by remarks
     */
    public List<AuditLogEntity> searchByRemarks(String keyword) {
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                return getAllAuditLogs();
            }
            return auditLogDAO.searchByRemarks(keyword.trim());
        } catch (Exception e) {
            throw new RuntimeException("Failed to search logs: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get audit statistics
     */
    public AuditStatistics getAuditStatistics() {
        try {
            long userLogs = auditLogDAO.countByEntityType(EntityType.USER);
            long productLogs = auditLogDAO.countByEntityType(EntityType.PRODUCT);
            long customerLogs = auditLogDAO.countByEntityType(EntityType.CUSTOMER);
            long transactionLogs = auditLogDAO.countByEntityType(EntityType.TRANSACTION);
            
            long creates = auditLogDAO.countByAction(Action.CREATE);
            long updates = auditLogDAO.countByAction(Action.UPDATE);
            long deletes = auditLogDAO.countByAction(Action.DELETE);
            long logins = auditLogDAO.countByAction(Action.LOGIN);
            
            return new AuditStatistics(userLogs, productLogs, customerLogs, transactionLogs,
                                      creates, updates, deletes, logins);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve audit statistics: " + e.getMessage(), e);
        }
    }
    
    /**
     * Inner class to hold audit statistics
     */
    public static class AuditStatistics {
        private final long userLogs;
        private final long productLogs;
        private final long customerLogs;
        private final long transactionLogs;
        private final long creates;
        private final long updates;
        private final long deletes;
        private final long logins;
        
        public AuditStatistics(long userLogs, long productLogs, long customerLogs, 
                              long transactionLogs, long creates, long updates, 
                              long deletes, long logins) {
            this.userLogs = userLogs;
            this.productLogs = productLogs;
            this.customerLogs = customerLogs;
            this.transactionLogs = transactionLogs;
            this.creates = creates;
            this.updates = updates;
            this.deletes = deletes;
            this.logins = logins;
        }
        
        public long getUserLogs() { return userLogs; }
        public long getProductLogs() { return productLogs; }
        public long getCustomerLogs() { return customerLogs; }
        public long getTransactionLogs() { return transactionLogs; }
        public long getCreates() { return creates; }
        public long getUpdates() { return updates; }
        public long getDeletes() { return deletes; }
        public long getLogins() { return logins; }
        public long getTotalLogs() { return userLogs + productLogs + customerLogs + transactionLogs; }
    }
}
