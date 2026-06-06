package service;

import dao.ProductHibernateDAO;
import models.entity.ProductEntity;
import util.ValidationUtil;

import javax.swing.JOptionPane;
import java.util.List;
import java.util.Optional;

/**
 * Service layer for Product business logic
 * Handles validation and business rules for products
 * 
 * @author Shop Management System
 */
public class ProductService {
    
    private final ProductHibernateDAO productDAO;
    
    public ProductService() {
        this.productDAO = new ProductHibernateDAO();
    }
    
    /**
     * Get all products
     */
    public List<ProductEntity> getAllProducts() {
        try {
            return productDAO.findAll();
        } catch (Exception e) {
            showError("Error loading products: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Get product by ID
     */
    public Optional<ProductEntity> getProductById(Integer productId) {
        try {
            return productDAO.findById(productId);
        } catch (Exception e) {
            showError("Error finding product: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Add product with validation (returns boolean for UI compatibility)
     */
    public boolean addProduct(ProductEntity product) {
        try {
            ProductEntity saved = saveProduct(product);
            return saved != null;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Save or update product with validation
     */
    public ProductEntity saveProduct(ProductEntity product) {
        // Validate product data
        if (!validateProduct(product)) {
            return null;
        }
        
        try {
            return productDAO.save(product);
        } catch (Exception e) {
            showError("Error saving product: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Update product (returns ProductEntity)
     */
    public ProductEntity updateProduct(ProductEntity product) {
        // Validate product data
        if (!validateProduct(product)) {
            return null;
        }
        
        try {
            return productDAO.update(product);
        } catch (Exception e) {
            showError("Error updating product: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Update product (boolean version for backward compatibility)
     */
    public boolean updateProductBoolean(ProductEntity product) {
        ProductEntity updated = updateProduct(product);
        return updated != null;
    }
    
    /**
     * Delete product
     */
    public boolean deleteProduct(Integer productId) {
        try {
            Optional<ProductEntity> product = productDAO.findById(productId);
            if (product.isPresent()) {
                // Soft delete - mark as discontinued instead of physical delete
                ProductEntity p = product.get();
                p.setStatus("discontinued");
                productDAO.update(p);
                return true;
            }
            return false;
        } catch (Exception e) {
            showError("Error deleting product: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Search products by name
     */
    public List<ProductEntity> searchByName(String name) {
        try {
            return productDAO.findByName(name);
        } catch (Exception e) {
            showError("Error searching products: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Get products by type
     */
    public List<ProductEntity> getProductsByType(String type) {
        try {
            return productDAO.findByType(type);
        } catch (Exception e) {
            showError("Error loading products by type: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Get low stock products
     */
    public List<ProductEntity> getLowStockProducts(int threshold) {
        try {
            return productDAO.findLowStock(threshold);
        } catch (Exception e) {
            showError("Error loading low stock products: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Get out of stock products
     */
    public List<ProductEntity> getOutOfStockProducts() {
        try {
            return productDAO.findOutOfStock();
        } catch (Exception e) {
            showError("Error loading out of stock products: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Update stock after sale
     */
    public boolean processSale(Integer productId, Integer quantity) {
        try {
            Optional<ProductEntity> productOpt = productDAO.findById(productId);
            if (productOpt.isPresent()) {
                ProductEntity product = productOpt.get();
                
                // Check if enough stock
                if (product.getStock() < quantity) {
                    showError("Insufficient stock! Available: " + product.getStock());
                    return false;
                }
                
                // Decrease stock
                return productDAO.decreaseStock(productId, quantity);
            }
            return false;
        } catch (Exception e) {
            showError("Error processing sale: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Update stock after purchase
     */
    public boolean processPurchase(Integer productId, Integer quantity) {
        try {
            return productDAO.increaseStock(productId, quantity);
        } catch (Exception e) {
            showError("Error processing purchase: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Get all product types
     */
    public List<String> getAllProductTypes() {
        try {
            return productDAO.getAllProductTypes();
        } catch (Exception e) {
            showError("Error loading product types: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Get active products only
     */
    public List<ProductEntity> getActiveProducts() {
        try {
            return productDAO.findActiveProducts();
        } catch (Exception e) {
            showError("Error loading active products: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Get total inventory value
     */
    public Double getTotalInventoryValue() {
        try {
            return productDAO.getTotalInventoryValue();
        } catch (Exception e) {
            showError("Error calculating inventory value: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Get total products count
     */
    public Long getTotalProductsCount() {
        try {
            return productDAO.count();
        } catch (Exception e) {
            showError("Error counting products: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Validate product data
     */
    private boolean validateProduct(ProductEntity product) {
        if (!ValidationUtil.isNotEmpty(product.getName())) {
            showError("Product name is required!");
            return false;
        }
        
        if (!ValidationUtil.isValidLength(product.getName(), 2, 200)) {
            showError("Product name must be between 2 and 200 characters!");
            return false;
        }
        
        if (product.getStock() == null || !ValidationUtil.isValidStock(product.getStock())) {
            showError("Invalid stock quantity!");
            return false;
        }
        
        if (product.getSellPrice() == null || !ValidationUtil.isValidPrice(product.getSellPrice())) {
            showError("Invalid sell price!");
            return false;
        }
        
        if (product.getPurchasePrice() == null || !ValidationUtil.isValidPrice(product.getPurchasePrice())) {
            showError("Invalid purchase price!");
            return false;
        }
        
        if (product.getSellPrice() < product.getPurchasePrice()) {
            int confirm = JOptionPane.showConfirmDialog(null,
                "Sell price is less than purchase price. This will result in a loss!\nDo you want to continue?",
                "Warning: Negative Profit",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            
            return confirm == JOptionPane.YES_OPTION;
        }
        
        return true;
    }
    
    /**
     * Get count of low stock products
     */
    public Long getLowStockProductsCount() {
        try {
            List<ProductEntity> lowStockProducts = getLowStockProducts(10); // threshold of 10
            return (long) lowStockProducts.size();
        } catch (Exception e) {
            showError("Error counting low stock products: " + e.getMessage());
            return 0L;
        }
    }
    
    /**
     * Show error message
     */
    private void showError(String message) {
        JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
