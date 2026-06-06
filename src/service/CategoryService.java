package service;

import dao.CategoryHibernateDAO;
import models.entity.CategoryEntity;

import java.util.List;
import java.util.Optional;

/**
 * Service layer for Category management
 * Provides business logic for product category operations
 * 
 * @author Shop Management System
 */
public class CategoryService {
    
    private final CategoryHibernateDAO categoryDAO;
    
    public CategoryService() {
        this.categoryDAO = new CategoryHibernateDAO();
    }
    
    /**
     * Get all categories
     */
    public List<CategoryEntity> getAllCategories() {
        try {
            return categoryDAO.findAll();
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve categories: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get all active categories only
     */
    public List<CategoryEntity> getActiveCategories() {
        try {
            return categoryDAO.findAllActive();
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve active categories: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get category by ID
     */
    public Optional<CategoryEntity> getCategoryById(Integer categoryId) {
        try {
            if (categoryId == null || categoryId <= 0) {
                throw new IllegalArgumentException("Invalid category ID");
            }
            return categoryDAO.findById(categoryId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to find category: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get category by name
     */
    public Optional<CategoryEntity> getCategoryByName(String categoryName) {
        try {
            if (categoryName == null || categoryName.trim().isEmpty()) {
                throw new IllegalArgumentException("Category name cannot be empty");
            }
            return categoryDAO.findByName(categoryName.trim());
        } catch (Exception e) {
            throw new RuntimeException("Failed to find category: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get all category names (for dropdowns)
     */
    public List<String> getAllCategoryNames() {
        try {
            return categoryDAO.findAllCategoryNames();
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve category names: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get top-level categories (no parent)
     */
    public List<CategoryEntity> getTopLevelCategories() {
        try {
            return categoryDAO.findTopLevelCategories();
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve top-level categories: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get subcategories of a parent category
     */
    public List<CategoryEntity> getSubcategories(Integer parentCategoryId) {
        try {
            if (parentCategoryId == null || parentCategoryId <= 0) {
                throw new IllegalArgumentException("Invalid parent category ID");
            }
            return categoryDAO.findSubcategories(parentCategoryId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve subcategories: " + e.getMessage(), e);
        }
    }
    
    /**
     * Create a new category
     */
    public void createCategory(CategoryEntity category) {
        try {
            // Validate category data
            validateCategory(category);
            
            // Check if category name already exists
            if (categoryDAO.categoryExists(category.getName())) {
                throw new IllegalArgumentException("Category with name '" + category.getName() + "' already exists");
            }
            
            // Validate parent category if specified
            if (category.getParentCategory() != null) {
                Integer parentId = category.getParentCategory().getCategoryId();
                if (parentId != null && !categoryDAO.findById(parentId).isPresent()) {
                    throw new IllegalArgumentException("Parent category not found");
                }
            }
            
            // Set active by default if not specified
            if (category.getIsActive() == null) {
                category.setIsActive(true);
            }
            
            categoryDAO.save(category);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create category: " + e.getMessage(), e);
        }
    }
    
    /**
     * Update an existing category
     */
    public void updateCategory(CategoryEntity category) {
        try {
            // Validate category data
            validateCategory(category);
            
            if (category.getCategoryId() == null || category.getCategoryId() <= 0) {
                throw new IllegalArgumentException("Invalid category ID for update");
            }
            
            // Check if category exists
            Optional<CategoryEntity> existing = categoryDAO.findById(category.getCategoryId());
            if (!existing.isPresent()) {
                throw new IllegalArgumentException("Category not found");
            }
            
            // Check if new name already exists (excluding current category)
            Optional<CategoryEntity> categoryWithSameName = categoryDAO.findByName(category.getName());
            if (categoryWithSameName.isPresent() && !categoryWithSameName.get().getCategoryId().equals(category.getCategoryId())) {
                throw new IllegalArgumentException("Category with name '" + category.getName() + "' already exists");
            }
            
            // Prevent circular reference (category cannot be its own parent)
            if (category.getParentCategory() != null) {
                Integer parentId = category.getParentCategory().getCategoryId();
                if (category.getCategoryId().equals(parentId)) {
                    throw new IllegalArgumentException("Category cannot be its own parent");
                }
                
                // Validate parent exists
                if (!categoryDAO.findById(parentId).isPresent()) {
                    throw new IllegalArgumentException("Parent category not found");
                }
            }
            
            categoryDAO.update(category);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update category: " + e.getMessage(), e);
        }
    }
    
    /**
     * Delete a category (hard delete)
     */
    public void deleteCategory(Integer categoryId) {
        try {
            if (categoryId == null || categoryId <= 0) {
                throw new IllegalArgumentException("Invalid category ID");
            }
            
            Optional<CategoryEntity> category = categoryDAO.findById(categoryId);
            if (!category.isPresent()) {
                throw new IllegalArgumentException("Category not found");
            }
            
            // Check if category has subcategories
            List<CategoryEntity> subcategories = categoryDAO.findSubcategories(categoryId);
            if (!subcategories.isEmpty()) {
                throw new IllegalArgumentException("Cannot delete category with subcategories. Delete or reassign subcategories first.");
            }
            
            // Check if category has products
            Long productCount = categoryDAO.countProductsInCategory(categoryId);
            if (productCount > 0) {
                throw new IllegalArgumentException("Cannot delete category with " + productCount + " product(s). Reassign products first.");
            }
            
            categoryDAO.deleteById(categoryId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete category: " + e.getMessage(), e);
        }
    }
    
    /**
     * Deactivate a category (soft delete)
     */
    public void deactivateCategory(Integer categoryId) {
        try {
            if (categoryId == null || categoryId <= 0) {
                throw new IllegalArgumentException("Invalid category ID");
            }
            
            Optional<CategoryEntity> category = categoryDAO.findById(categoryId);
            if (!category.isPresent()) {
                throw new IllegalArgumentException("Category not found");
            }
            
            categoryDAO.deactivateCategory(categoryId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deactivate category: " + e.getMessage(), e);
        }
    }
    
    /**
     * Search categories by name
     */
    public List<CategoryEntity> searchCategories(String keyword) {
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                return getAllCategories();
            }
            return categoryDAO.searchByName(keyword.trim());
        } catch (Exception e) {
            throw new RuntimeException("Failed to search categories: " + e.getMessage(), e);
        }
    }
    
    /**
     * Check if a category exists
     */
    public boolean categoryExists(String categoryName) {
        try {
            if (categoryName == null || categoryName.trim().isEmpty()) {
                return false;
            }
            return categoryDAO.categoryExists(categoryName.trim());
        } catch (Exception e) {
            throw new RuntimeException("Failed to check category existence: " + e.getMessage(), e);
        }
    }
    
    /**
     * Count products in a category
     */
    public Long countProductsInCategory(Integer categoryId) {
        try {
            if (categoryId == null || categoryId <= 0) {
                throw new IllegalArgumentException("Invalid category ID");
            }
            return categoryDAO.countProductsInCategory(categoryId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to count products: " + e.getMessage(), e);
        }
    }
    
    /**
     * Initialize default categories if they don't exist
     */
    public void initializeDefaultCategories() {
        try {
            categoryDAO.createDefaultCategories();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize default categories: " + e.getMessage(), e);
        }
    }
    
    /**
     * Validate category entity
     */
    private void validateCategory(CategoryEntity category) {
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }
        
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Category name is required");
        }
        
        if (category.getName().length() < 2 || category.getName().length() > 100) {
            throw new IllegalArgumentException("Category name must be between 2 and 100 characters");
        }
        
        // Description is optional but has length limit if provided
        if (category.getDescription() != null && category.getDescription().length() > 255) {
            throw new IllegalArgumentException("Category description cannot exceed 255 characters");
        }
    }
}
