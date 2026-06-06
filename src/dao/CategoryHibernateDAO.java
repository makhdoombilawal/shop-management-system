package dao;

import models.entity.CategoryEntity;
import org.hibernate.query.Query;
import util.HibernateUtil;

import java.util.List;
import java.util.Optional;

/**
 * DAO for Category Entity
 * Provides specialized category data access methods
 * 
 * @author Shop Management System
 */
public class CategoryHibernateDAO extends GenericDAO<CategoryEntity, Integer> {
    
    public CategoryHibernateDAO() {
        super(CategoryEntity.class);
    }
    
    /**
     * Find category by name
     */
    public Optional<CategoryEntity> findByName(String name) {
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM CategoryEntity c WHERE c.name = :name";
            Query<CategoryEntity> query = session.createQuery(hql, CategoryEntity.class);
            query.setParameter("name", name);
            List<CategoryEntity> results = query.list();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } catch (Exception e) {
            throw new RuntimeException("Error finding category by name: " + e.getMessage(), e);
        }
    }
    
    /**
     * Check if category name exists
     */
    public boolean categoryExists(String name) {
        return findByName(name).isPresent();
    }
    
    /**
     * Get all active categories
     */
    public List<CategoryEntity> findAllActive() {
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM CategoryEntity c WHERE c.isActive = true ORDER BY c.name";
            Query<CategoryEntity> query = session.createQuery(hql, CategoryEntity.class);
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Error finding active categories: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get all top-level categories (no parent)
     */
    public List<CategoryEntity> findTopLevelCategories() {
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM CategoryEntity c WHERE c.parentCategory IS NULL AND c.isActive = true ORDER BY c.name";
            Query<CategoryEntity> query = session.createQuery(hql, CategoryEntity.class);
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Error finding top-level categories: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get subcategories of a parent
     */
    public List<CategoryEntity> findSubcategories(Integer parentCategoryId) {
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM CategoryEntity c WHERE c.parentCategory.categoryId = :parentId AND c.isActive = true ORDER BY c.name";
            Query<CategoryEntity> query = session.createQuery(hql, CategoryEntity.class);
            query.setParameter("parentId", parentCategoryId);
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Error finding subcategories: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get category names only (for dropdowns)
     */
    public List<String> findAllCategoryNames() {
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT c.name FROM CategoryEntity c WHERE c.isActive = true ORDER BY c.name";
            Query<String> query = session.createQuery(hql, String.class);
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Error finding category names: " + e.getMessage(), e);
        }
    }
    
    /**
     * Search categories by name (partial match)
     */
    public List<CategoryEntity> searchByName(String namePattern) {
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM CategoryEntity c WHERE LOWER(c.name) LIKE LOWER(:pattern) AND c.isActive = true ORDER BY c.name";
            Query<CategoryEntity> query = session.createQuery(hql, CategoryEntity.class);
            query.setParameter("pattern", "%" + namePattern + "%");
            return query.list();
        } catch (Exception e) {
            throw new RuntimeException("Error searching categories: " + e.getMessage(), e);
        }
    }
    
    /**
     * Count products in category
     */
    public Long countProductsInCategory(Integer categoryId) {
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT COUNT(p) FROM ProductEntity p WHERE p.category.categoryId = :categoryId";
            Query<Long> query = session.createQuery(hql, Long.class);
            query.setParameter("categoryId", categoryId);
            return query.uniqueResult();
        } catch (Exception e) {
            throw new RuntimeException("Error counting products in category: " + e.getMessage(), e);
        }
    }
    
    /**
     * Deactivate category (soft delete)
     */
    public boolean deactivateCategory(Integer categoryId) {
        org.hibernate.Transaction transaction = null;
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            
            CategoryEntity category = session.get(CategoryEntity.class, categoryId);
            if (category != null) {
                category.setIsActive(false);
                session.update(category);
                transaction.commit();
                return true;
            }
            return false;
            
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw new RuntimeException("Error deactivating category: " + e.getMessage(), e);
        }
    }
    
    /**
     * Create default categories if not exist
     */
    public void createDefaultCategories() {
        org.hibernate.Transaction transaction = null;
        try (org.hibernate.Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            
            String[] defaultCategories = {
                "Electronics", "Food & Beverages", "Clothing", "Home & Garden",
                "Health & Beauty", "Sports & Outdoors", "Books & Media", "Toys & Games",
                "Office Supplies", "Automotive", "Other"
            };
            
            for (String categoryName : defaultCategories) {
                if (!categoryExists(categoryName)) {
                    CategoryEntity category = new CategoryEntity(categoryName, "Default category: " + categoryName);
                    session.save(category);
                }
            }
            
            transaction.commit();
            util.LoggerUtil.logInfo("✅ Default categories created successfully");
            
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            throw new RuntimeException("Error creating default categories: " + e.getMessage(), e);
        }
    }
}
