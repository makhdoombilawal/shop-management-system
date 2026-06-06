package dao;

import org.hibernate.query.Query;
import util.HibernateUtil;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * Generic DAO with common CRUD operations
 * Provides base functionality for all entity DAOs
 * 
 * @param <T> Entity type
 * @param <ID> Primary key type
 * @author Shop Management System
 */
public abstract class GenericDAO<T, ID extends Serializable> {
    
    private final Class<T> entityClass;
    
    protected GenericDAO(Class<T> entityClass) {
        this.entityClass = entityClass;
    }
    
    /**
     * Save or update an entity
     */
    public T save(T entity) {
        org.hibernate.Transaction transaction = null;
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.saveOrUpdate(entity);
            transaction.commit();
            return entity;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error saving entity: " + e.getMessage(), e);
        }
    }
    
    /**
     * Find entity by ID
     */
    public Optional<T> findById(ID id) {
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            T entity = session.get(entityClass, id);
            return Optional.ofNullable(entity);
        } catch (Exception e) {
            throw new RuntimeException("Error finding entity by ID: " + e.getMessage(), e);
        }
    }
    
    /**
     * Find all entities
     */
    public List<T> findAll() {
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM " + entityClass.getSimpleName();
            Query<T> query = session.createQuery(hql, entityClass);
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Error finding all entities: " + e.getMessage(), e);
        }
    }
    
    /**
     * Update an entity
     */
    public T update(T entity) {
        org.hibernate.Transaction transaction = null;
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.update(entity);
            transaction.commit();
            return entity;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error updating entity: " + e.getMessage(), e);
        }
    }
    
    /**
     * Delete an entity
     */
    public void delete(T entity) {
        org.hibernate.Transaction transaction = null;
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.delete(entity);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error deleting entity: " + e.getMessage(), e);
        }
    }
    
    /**
     * Delete entity by ID
     */
    public void deleteById(ID id) {
        org.hibernate.Transaction transaction = null;
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            T entity = session.get(entityClass, id);
            if (entity != null) {
                session.delete(entity);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Error deleting entity by ID: " + e.getMessage(), e);
        }
    }
    
    /**
     * Count total entities
     */
    public long count() {
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT COUNT(e) FROM " + entityClass.getSimpleName() + " e";
            Query<Long> query = session.createQuery(hql, Long.class);
            return query.uniqueResult();
        } catch (Exception e) {
            throw new RuntimeException("Error counting entities: " + e.getMessage(), e);
        }
    }
    
    /**
     * Check if entity exists by ID
     */
    public boolean existsById(ID id) {
        return findById(id).isPresent();
    }
    
    /**
     * Execute HQL query
     */
    protected List<T> executeQuery(String hql, Object... params) {
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<T> query = session.createQuery(hql, entityClass);
            for (int i = 0; i < params.length; i++) {
                query.setParameter(i, params[i]);
            }
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Error executing query: " + e.getMessage(), e);
        }
    }
    
    /**
     * Execute native SQL query
     */
    protected List<?> executeNativeQuery(String sql, Object... params) {
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<?> query = session.createNativeQuery(sql);
            for (int i = 0; i < params.length; i++) {
                query.setParameter(i, params[i]);
            }
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Error executing native query: " + e.getMessage(), e);
        }
    }
}
