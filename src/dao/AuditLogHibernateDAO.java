package dao;

import models.entity.AuditLogEntity;
import models.entity.AuditLogEntity.Action;
import models.entity.AuditLogEntity.EntityType;
import org.hibernate.query.Query;
import util.HibernateUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * DAO for Audit Log Entity
 * Provides specialized audit log data access methods
 * 
 * @author Shop Management System
 */
public class AuditLogHibernateDAO extends GenericDAO<AuditLogEntity, Integer> {
    
    public AuditLogHibernateDAO() {
        super(AuditLogEntity.class);
    }
    
    /**
     * Find audit logs by entity type
     */
    public List<AuditLogEntity> findByEntityType(EntityType entityType) {
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM AuditLogEntity al WHERE al.entityType = :type ORDER BY al.logDate DESC";
            Query<AuditLogEntity> query = session.createQuery(hql, AuditLogEntity.class);
            query.setParameter("type", entityType);
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Error finding logs by entity type: " + e.getMessage(), e);
        }
    }
    
    /**
     * Find audit logs by entity (type + ID)
     */
    public List<AuditLogEntity> findByEntity(EntityType entityType, Integer entityId) {
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM AuditLogEntity al WHERE al.entityType = :type AND al.entityId = :id ORDER BY al.logDate DESC";
            Query<AuditLogEntity> query = session.createQuery(hql, AuditLogEntity.class);
            query.setParameter("type", entityType);
            query.setParameter("id", entityId);
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Error finding logs by entity: " + e.getMessage(), e);
        }
    }
    
    /**
     * Find audit logs by action
     */
    public List<AuditLogEntity> findByAction(Action action) {
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM AuditLogEntity al WHERE al.action = :action ORDER BY al.logDate DESC";
            Query<AuditLogEntity> query = session.createQuery(hql, AuditLogEntity.class);
            query.setParameter("action", action);
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Error finding logs by action: " + e.getMessage(), e);
        }
    }
    
    /**
     * Find audit logs performed by a user
     */
    public List<AuditLogEntity> findByUser(Integer userId) {
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM AuditLogEntity al WHERE al.performedBy.userId = :userId ORDER BY al.logDate DESC";
            Query<AuditLogEntity> query = session.createQuery(hql, AuditLogEntity.class);
            query.setParameter("userId", userId);
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Error finding logs by user: " + e.getMessage(), e);
        }
    }
    
    /**
     * Find audit logs by date range
     */
    public List<AuditLogEntity> findByDateRange(LocalDate startDate, LocalDate endDate) {
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
            
            String hql = "FROM AuditLogEntity al WHERE al.logDate BETWEEN :start AND :end ORDER BY al.logDate DESC";
            Query<AuditLogEntity> query = session.createQuery(hql, AuditLogEntity.class);
            query.setParameter("start", startDateTime);
            query.setParameter("end", endDateTime);
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Error finding logs by date range: " + e.getMessage(), e);
        }
    }
    
    /**
     * Find security events (LOGIN/LOGOUT)
     */
    public List<AuditLogEntity> findSecurityEvents() {
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM AuditLogEntity al WHERE al.action IN (:login, :logout) ORDER BY al.logDate DESC";
            Query<AuditLogEntity> query = session.createQuery(hql, AuditLogEntity.class);
            query.setParameter("login", Action.LOGIN);
            query.setParameter("logout", Action.LOGOUT);
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Error finding security events: " + e.getMessage(), e);
        }
    }
    
    /**
     * Find data modification events (CREATE/UPDATE/DELETE)
     */
    public List<AuditLogEntity> findDataModifications() {
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM AuditLogEntity al WHERE al.action IN (:create, :update, :delete) ORDER BY al.logDate DESC";
            Query<AuditLogEntity> query = session.createQuery(hql, AuditLogEntity.class);
            query.setParameter("create", Action.CREATE);
            query.setParameter("update", Action.UPDATE);
            query.setParameter("delete", Action.DELETE);
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Error finding data modifications: " + e.getMessage(), e);
        }
    }
    
    /**
     * Find recent audit logs (last N records)
     */
    public List<AuditLogEntity> findRecent(int limit) {
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM AuditLogEntity al ORDER BY al.logDate DESC";
            Query<AuditLogEntity> query = session.createQuery(hql, AuditLogEntity.class);
            query.setMaxResults(limit);
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Error finding recent logs: " + e.getMessage(), e);
        }
    }
    
    /**
     * Find today's audit logs
     */
    public List<AuditLogEntity> findTodayLogs() {
        LocalDate today = LocalDate.now();
        return findByDateRange(today, today);
    }
    
    /**
     * Find logs by IP address
     */
    public List<AuditLogEntity> findByIpAddress(String ipAddress) {
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM AuditLogEntity al WHERE al.ipAddress = :ip ORDER BY al.logDate DESC";
            Query<AuditLogEntity> query = session.createQuery(hql, AuditLogEntity.class);
            query.setParameter("ip", ipAddress);
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Error finding logs by IP: " + e.getMessage(), e);
        }
    }
    
    /**
     * Count logs by entity type
     */
    public Long countByEntityType(EntityType entityType) {
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT COUNT(al) FROM AuditLogEntity al WHERE al.entityType = :type";
            Query<Long> query = session.createQuery(hql, Long.class);
            query.setParameter("type", entityType);
            return query.uniqueResult();
        } catch (Exception e) {
            throw new RuntimeException("Error counting logs: " + e.getMessage(), e);
        }
    }
    
    /**
     * Count logs by action
     */
    public Long countByAction(Action action) {
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT COUNT(al) FROM AuditLogEntity al WHERE al.action = :action";
            Query<Long> query = session.createQuery(hql, Long.class);
            query.setParameter("action", action);
            return query.uniqueResult();
        } catch (Exception e) {
            throw new RuntimeException("Error counting logs: " + e.getMessage(), e);
        }
    }
    
    /**
     * Search logs by remarks
     */
    public List<AuditLogEntity> searchByRemarks(String keyword) {
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM AuditLogEntity al WHERE LOWER(al.remarks) LIKE LOWER(:keyword) ORDER BY al.logDate DESC";
            Query<AuditLogEntity> query = session.createQuery(hql, AuditLogEntity.class);
            query.setParameter("keyword", "%" + keyword + "%");
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Error searching logs: " + e.getMessage(), e);
        }
    }
}
