package dao;

import models.entity.UserEntity;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import util.HibernateUtil;

import java.util.List;
import java.util.Optional;

/**
 * Hibernate DAO for User operations
 * Handles all database operations for users
 */
public class UserHibernateDAO extends GenericDAO<UserEntity, Integer> {

    public UserHibernateDAO() {
        super(UserEntity.class);
    }

    /**
     * Find user by username
     * @param username Username to search
     * @return Optional containing user if found
     */
    public Optional<UserEntity> findByUsername(String username) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query<UserEntity> query = session.createQuery(
                "FROM UserEntity WHERE username = :username", UserEntity.class);
            query.setParameter("username", username);
            return query.uniqueResultOptional();
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        } finally {
            if (session != null) session.close();
        }
    }

    /**
     * Authenticate user with username and password
     * @param username Username
     * @param password Password (should be hashed)
     * @return Optional containing user if authentication successful
     */
    public Optional<UserEntity> authenticate(String username, String password) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query<UserEntity> query = session.createQuery(
                "FROM UserEntity WHERE username = :username AND password = :password AND isActive = true", 
                UserEntity.class);
            query.setParameter("username", username);
            query.setParameter("password", password);
            
            Optional<UserEntity> userOpt = query.uniqueResultOptional();
            
            // Update last login if found
            if (userOpt.isPresent()) {
                Transaction tx = session.beginTransaction();
                try {
                    UserEntity user = userOpt.get();
                    user.updateLastLogin();
                    session.update(user);
                    tx.commit();
                } catch (Exception e) {
                    if (tx != null) tx.rollback();
                    e.printStackTrace();
                }
            }
            
            return userOpt;
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        } finally {
            if (session != null) session.close();
        }
    }

    /**
     * Get all active users
     * @return List of active users
     */
    public List<UserEntity> getAllActiveUsers() {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query<UserEntity> query = session.createQuery(
                "FROM UserEntity WHERE isActive = true ORDER BY username", UserEntity.class);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        } finally {
            if (session != null) session.close();
        }
    }

    /**
     * Get users by role
     * @param role Role to filter (ADMIN, MANAGER, CASHIER)
     * @return List of users with specified role
     */
    public List<UserEntity> getUsersByRole(String role) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            Query<UserEntity> query = session.createQuery(
                "FROM UserEntity WHERE role = :role AND isActive = true ORDER BY username", 
                UserEntity.class);
            query.setParameter("role", role);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        } finally {
            if (session != null) session.close();
        }
    }

    /**
     * Deactivate user (soft delete)
     * @param userId User ID to deactivate
     * @return true if successful
     */
    public boolean deactivateUser(Integer userId) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            
            UserEntity user = session.get(UserEntity.class, userId);
            if (user != null) {
                user.setIsActive(false);
                session.update(user);
                tx.commit();
                return true;
            }
            return false;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            return false;
        } finally {
            if (session != null) session.close();
        }
    }

    /**
     * Activate user
     * @param userId User ID to activate
     * @return true if successful
     */
    public boolean activateUser(Integer userId) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            
            UserEntity user = session.get(UserEntity.class, userId);
            if (user != null) {
                user.setIsActive(true);
                session.update(user);
                tx.commit();
                return true;
            }
            return false;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            return false;
        } finally {
            if (session != null) session.close();
        }
    }

    /**
     * Check if username already exists
     * @param username Username to check
     * @return true if exists
     */
    public boolean usernameExists(String username) {
        return findByUsername(username).isPresent();
    }

    /**
     * Change user password
     * @param userId User ID
     * @param newPassword New password (should be hashed)
     * @return true if successful
     */
    public boolean changePassword(Integer userId, String newPassword) {
        Session session = null;
        Transaction tx = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();
            
            UserEntity user = session.get(UserEntity.class, userId);
            if (user != null) {
                user.setPassword(newPassword);
                session.update(user);
                tx.commit();
                return true;
            }
            return false;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            return false;
        } finally {
            if (session != null) session.close();
        }
    }
}
