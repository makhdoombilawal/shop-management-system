package dao;

import models.entity.RoleEntity;
import org.hibernate.query.Query;
import util.HibernateUtil;

import java.util.List;
import java.util.Optional;

/**
 * DAO for Role Entity
 * Provides specialized role data access methods
 * 
 * @author Shop Management System
 */
public class RoleHibernateDAO extends GenericDAO<RoleEntity, Integer> {
    
    public RoleHibernateDAO() {
        super(RoleEntity.class);
    }
    
    /**
     * Find role by name
     */
    public Optional<RoleEntity> findByName(String name) {
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM RoleEntity r WHERE r.name = :name";
            Query<RoleEntity> query = session.createQuery(hql, RoleEntity.class);
            query.setParameter("name", name);
            List<RoleEntity> results = query.list();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } catch (Exception e) {
            throw new RuntimeException("Error finding role by name: " + e.getMessage(), e);
        }
    }
    
    /**
     * Check if role name exists
     */
    public boolean roleExists(String name) {
        return findByName(name).isPresent();
    }
    
    /**
     * Get all active roles
     */
    public List<RoleEntity> findAllRoles() {
        try {
            return findAll();
        } catch (Exception e) {
            throw new RuntimeException("Error finding all roles: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get role names only (for dropdowns)
     */
    public List<String> findAllRoleNames() {
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT r.name FROM RoleEntity r ORDER BY r.name";
            Query<String> query = session.createQuery(hql, String.class);
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Error finding role names: " + e.getMessage(), e);
        }
    }
    
    /**
     * Search roles by name (partial match)
     */
    public List<RoleEntity> searchByName(String namePattern) {
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM RoleEntity r WHERE LOWER(r.name) LIKE LOWER(:pattern) ORDER BY r.name";
            Query<RoleEntity> query = session.createQuery(hql, RoleEntity.class);
            query.setParameter("pattern", "%" + namePattern + "%");
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Error searching roles: " + e.getMessage(), e);
        }
    }
    
    /**
     * Create default roles if not exist
     */
    public void createDefaultRoles() {
        org.hibernate.Transaction transaction = null;
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            
            // ADMIN role
            if (!roleExists("ADMIN")) {
                RoleEntity admin = new RoleEntity("ADMIN", "System Administrator - Full access to all features");
                session.save(admin);
            }
            
            // MANAGER role
            if (!roleExists("MANAGER")) {
                RoleEntity manager = new RoleEntity("MANAGER", "Manager - Can manage products, customers, and view reports");
                session.save(manager);
            }
            
            // CASHIER role
            if (!roleExists("CASHIER")) {
                RoleEntity cashier = new RoleEntity("CASHIER", "Cashier - Can process sales and view inventory");
                session.save(cashier);
            }
            
            transaction.commit();
            util.LoggerUtil.logInfo("✅ Default roles created successfully");
            
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw new RuntimeException("Error creating default roles: " + e.getMessage(), e);
        }
    }
}
