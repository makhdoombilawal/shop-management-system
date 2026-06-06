package service;

import dao.BarcodeHibernateDAO;
import dao.ProductHibernateDAO;
import models.entity.BarcodeEntity;
import models.entity.ProductEntity;
import util.ValidationUtil;
import helper.BarcodeGenerator;

import javax.swing.JOptionPane;
import java.util.List;
import java.util.Optional;

/**
 * Service layer for Barcode business logic
 * Handles barcode generation, validation, and scanning
 * 
 * @author Shop Management System
 */
public class BarcodeService {
    
    private final BarcodeHibernateDAO barcodeDAO;
    private final ProductHibernateDAO productDAO;
    
    public BarcodeService() {
        this.barcodeDAO = new BarcodeHibernateDAO();
        this.productDAO = new ProductHibernateDAO();
    }
    
    /**
     * Add barcode for product
     */
    public boolean addBarcode(Integer productId, String barcodeNumber) {
        try {
            // Validate barcode format
            if (!ValidationUtil.isValidBarcode(barcodeNumber)) {
                showError("Invalid barcode format! Use 8-13 digit numbers.");
                return false;
            }
            
            // Check if barcode already exists
            if (barcodeDAO.barcodeExists(barcodeNumber)) {
                showError("Barcode already exists in the system!");
                return false;
            }
            
            // Check if product exists
            Optional<ProductEntity> productOpt = productDAO.findById(productId);
            if (!productOpt.isPresent()) {
                showError("Product not found!");
                return false;
            }
            
            // Add barcode
            return barcodeDAO.addBarcodeForProduct(productId, barcodeNumber);
            
        } catch (Exception e) {
            showError("Error adding barcode: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Get product by scanning barcode
     */
    public ProductEntity scanBarcode(String barcodeNumber) {
        try {
            if (!ValidationUtil.isNotEmpty(barcodeNumber)) {
                showError("Barcode cannot be empty!");
                return null;
            }
            
            // ENTERPRISE: Use optimized scan query
            BarcodeEntity barcode = barcodeDAO.scanBarcodeOptimized(barcodeNumber);
            if (barcode == null) {
                showError("❌ Barcode not found in system!");
                return null;
            }
            
            // ENTERPRISE REQUIREMENT #7: Check stock before allowing scan
            if (barcode.getProduct() == null || barcode.getProduct().getStock() <= 0) {
                showError("❌ Product OUT OF STOCK - Cannot process sale!");
                return null;
            }
            
            // ENTERPRISE: Check if barcode is active (only ONE active per product)
            if (!barcode.isActivated()) {
                showError("❌ This barcode is INACTIVE! An active barcode is required.");
                return null;
            }
            
            // ENTERPRISE: Check barcode status
            if (!barcode.isAvailable()) {
                showError("❌ Barcode status: " + barcode.getStatus() + " - Cannot scan!");
                return null;
            }
            
            util.LoggerUtil.logInfo("✅ SCAN SUCCESS: " + barcode.getBarcodeNumber() + " → " + barcode.getProduct().getName() + " (Stock: " + barcode.getProduct().getStock() + ")");
            return barcode.getProduct();
            
        } catch (Exception e) {
            showError("Error scanning barcode: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Get barcode information
     */
    public BarcodeEntity getBarcodeInfo(String barcodeNumber) {
        try {
            return barcodeDAO.findByBarcodeNumber(barcodeNumber);
        } catch (Exception e) {
            showError("Error retrieving barcode: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Get all barcodes for a product
     */
    public List<BarcodeEntity> getProductBarcodes(Integer productId) {
        try {
            return barcodeDAO.findByProduct(productId);
        } catch (Exception e) {
            showError("Error loading barcodes: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Get available barcodes for a product
     */
    public List<BarcodeEntity> getAvailableBarcodes(Integer productId) {
        try {
            return barcodeDAO.findAvailableByProduct(productId);
        } catch (Exception e) {
            showError("Error loading available barcodes: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Mark barcode as sold
     */
    public boolean markAsSold(String barcodeNumber) {
        try {
            return barcodeDAO.markAsSold(barcodeNumber);
        } catch (Exception e) {
            showError("Error updating barcode: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Mark barcode as damaged
     */
    public boolean markAsDamaged(String barcodeNumber, String reason) {
        try {
            return barcodeDAO.markAsDamaged(barcodeNumber, reason);
        } catch (Exception e) {
            showError("Error updating barcode: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Generate barcode image for product
     */
    public boolean generateBarcodeImage(Integer productId, String barcodeNumber, String outputPath) {
        try {
            Optional<ProductEntity> productOpt = productDAO.findById(productId);
            if (!productOpt.isPresent()) {
                showError("Product not found!");
                return false;
            }
            
            productOpt.get(); // validated existence above
            String generatedPath = BarcodeGenerator.generateBarcodeImage(barcodeNumber);
            return generatedPath != null;
            
        } catch (Exception e) {
            showError("Error generating barcode image: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Print barcode for product
     */
    public boolean printBarcode(String barcodeNumber) {
        try {
            BarcodeGenerator.printBarcode(barcodeNumber);
            return true;
        } catch (Exception e) {
            showError("Error printing barcode: " + e.getMessage());
            return false;
        }
    }

    /**
     * Generate multiple barcodes for a product
     */
    public boolean generateMultipleBarcodes(Integer productId, int count) {
        try {
            Optional<ProductEntity> productOpt = productDAO.findById(productId);
            if (!productOpt.isPresent()) {
                showError("Product not found!");
                return false;
            }
            
            int successCount = 0;
            
            for (int i = 0; i < count; i++) {
                // Generate unique barcode number
                String barcodeNumber = generateUniqueBarcodeNumber();
                
                // Add to database
                if (barcodeDAO.addBarcodeForProduct(productId, barcodeNumber)) {
                    successCount++;
                    
                    // Generate barcode image
                    String generatedPath = BarcodeGenerator.generateBarcodeImage(barcodeNumber);
                    if (generatedPath != null) {
                        util.LoggerUtil.logInfo("Generated barcode: " + generatedPath);
                    }
                }
            }
            
            JOptionPane.showMessageDialog(null, 
                successCount + " out of " + count + " barcodes generated successfully!",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
            
            return successCount > 0;
            
        } catch (Exception e) {
            showError("Error generating barcodes: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Generate unique barcode number
     */
    private String generateUniqueBarcodeNumber() {
        String barcodeNumber;
        int attempts = 0;
        do {
            barcodeNumber = String.format("%013d", System.currentTimeMillis() % 10000000000000L);
            attempts++;
            if (attempts > 100) {
                throw new RuntimeException("Unable to generate unique barcode after 100 attempts");
            }
        } while (barcodeDAO.barcodeExists(barcodeNumber));
        
        return barcodeNumber;
    }
    
    /**
     * Check barcode availability
     */
    public boolean isBarcodeAvailable(String barcodeNumber) {
        try {
            BarcodeEntity barcode = barcodeDAO.findByBarcodeNumber(barcodeNumber);
            return barcode != null && barcode.isAvailable();
        } catch (Exception e) {
            showError("Error checking barcode: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get all barcodes
     */
    public List<BarcodeEntity> getAllBarcodes() {
        try {
            return barcodeDAO.findAll();
        } catch (Exception e) {
            showError("Error loading barcodes: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Update barcode status
     */
    public boolean updateBarcodeStatus(String barcodeNumber, String status) {
        try {
            BarcodeEntity barcode = barcodeDAO.findByBarcodeNumber(barcodeNumber);
            if (barcode == null) {
                showError("Barcode not found!");
                return false;
            }
            
            if ("sold".equalsIgnoreCase(status)) {
                return markAsSold(barcodeNumber);
            } else {
                barcode.setStatus(status);
                return barcodeDAO.update(barcode) != null;
            }
        } catch (Exception e) {
            showError("Error updating barcode: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Delete barcode
     */
    public boolean deleteBarcode(Integer barcodeId) {
        try {
            barcodeDAO.deleteById(barcodeId);
            return true;
        } catch (Exception e) {
            showError("Error deleting barcode: " + e.getMessage());
            return false;
        }
    }
    
    // ============================================================================
    // ENTERPRISE BARCODE SYSTEM METHODS
    // ============================================================================
    
    /**
     * ENTERPRISE #1: Get the ONLY active barcode for a product
     * Enforces: Each product has maximum ONE active barcode
     */
    public BarcodeEntity getActiveBarcode(Integer productId) {
        try {
            BarcodeEntity activeBarcode = barcodeDAO.findActiveBarcode(productId);
            if (activeBarcode == null) {
                util.LoggerUtil.logInfo("⚠️ No active barcode found for product ID: " + productId);
            }
            return activeBarcode;
        } catch (Exception e) {
            showError("Error retrieving active barcode: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * ENTERPRISE #4: Set barcode as ACTIVE for product
     * Automatically deactivates all other barcodes for this product
     * Enforces: Only ONE active barcode per product
     */
    public boolean setActiveBarcodeForProduct(Integer barcodeId, Integer productId) {
        try {
            // Validate barcode exists
            Optional<BarcodeEntity> barcodeOpt = barcodeDAO.findById(barcodeId);
            if (!barcodeOpt.isPresent()) {
                showError("Barcode not found!");
                return false;
            }
            
            BarcodeEntity barcode = barcodeOpt.get();
            
            // Validate product exists
            Optional<ProductEntity> productOpt = productDAO.findById(productId);
            if (!productOpt.isPresent()) {
                showError("Product not found!");
                return false;
            }
            
            // Validate barcode belongs to product
            if (!barcode.getProduct().getProductId().equals(productId)) {
                showError("Barcode does not belong to this product!");
                return false;
            }
            
            // Set as active (this will deactivate all others for this product)
            boolean result = barcodeDAO.setBarcodeAsActive(barcodeId, productId);
            
            if (result) {
                JOptionPane.showMessageDialog(null,
                    "✅ Barcode " + barcode.getBarcodeNumber() + " is now ACTIVE\n" +
                    "All other barcodes for this product are INACTIVE",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            }
            
            return result;
        } catch (Exception e) {
            showError("Error setting barcode as active: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * ENTERPRISE #3: Add barcode with ACTIVE status enforcement
     * If product already has an active barcode, this new one will be INACTIVE
     */
    public boolean addBarcodeWithStatusManagement(Integer productId, String barcodeNumber) {
        try {
            // Validate barcode format
            if (!ValidationUtil.isValidBarcode(barcodeNumber)) {
                showError("Invalid barcode format! Use 8-13 digit numbers.");
                return false;
            }
            
            // Check if barcode already exists (ENTERPRISE REQUIREMENT #2: Globally unique)
            if (barcodeDAO.barcodeExists(barcodeNumber)) {
                showError("❌ Barcode already exists in the system! Barcodes must be GLOBALLY UNIQUE.");
                return false;
            }
            
            // Check if product exists
            Optional<ProductEntity> productOpt = productDAO.findById(productId);
            if (!productOpt.isPresent()) {
                showError("Product not found!");
                return false;
            }
            
            // Check if product already has an active barcode
            BarcodeEntity existingActive = barcodeDAO.findActiveBarcode(productId);
            
            // Add the barcode
            boolean added = barcodeDAO.addBarcodeForProduct(productId, barcodeNumber);
            
            if (added) {
                BarcodeEntity newBarcode = barcodeDAO.findByBarcodeNumber(barcodeNumber);
                
                if (existingActive != null) {
                    // Product already has active barcode - make new one inactive
                    newBarcode.setIsActive(false);
                    barcodeDAO.update(newBarcode);
                    
                    JOptionPane.showMessageDialog(null,
                        "✅ Barcode added SUCCESSFULLY (INACTIVE)\n" +
                        "Product already has an active barcode:\n" +
                        existingActive.getBarcodeNumber() + "\n\n" +
                        "To activate this barcode, use 'Set as Active' button.",
                        "Barcode Added",
                        JOptionPane.INFORMATION_MESSAGE);
                } else {
                    // No active barcode yet - make this one active
                    newBarcode.setIsActive(true);
                    barcodeDAO.update(newBarcode);
                    
                    JOptionPane.showMessageDialog(null,
                        "✅ Barcode added and set as ACTIVE\n" +
                        "This is now the primary barcode for scanning.",
                        "Barcode Added",
                        JOptionPane.INFORMATION_MESSAGE);
                }
                
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            showError("Error adding barcode: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * ENTERPRISE #6: Validate barcode before scan
     * Checks: exists, active, product has stock
     */
    public boolean isValidForScan(String barcodeNumber) {
        try {
            if (!ValidationUtil.isNotEmpty(barcodeNumber)) {
                return false;
            }
            return barcodeDAO.isValidForScan(barcodeNumber);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Get complete barcode status for display
     * Shows active/inactive and stock status
     */
    public String getBarcodeStatus(String barcodeNumber) {
        try {
            BarcodeEntity barcode = barcodeDAO.findByBarcodeNumber(barcodeNumber);
            if (barcode == null) {
                return "NOT_FOUND";
            }
            
            StringBuilder status = new StringBuilder();
            status.append("Barcode: ").append(barcode.getBarcodeNumber()).append("\n");
            status.append("Status: ").append(barcode.getStatus().toUpperCase()).append("\n");
            status.append("Active: ").append(barcode.isActivated() ? "✅ YES" : "❌ NO").append("\n");
            
            if (barcode.getProduct() != null) {
                status.append("Product: ").append(barcode.getProduct().getName()).append("\n");
                status.append("Stock: ").append(barcode.getProduct().getStock());
                if (barcode.getProduct().getStock() == 0) {
                    status.append(" ⚠️ OUT OF STOCK");
                }
            }
            
            return status.toString();
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }
    
    /**
     * Get all barcodes for a product with their active status
     */
    public List<BarcodeEntity> getProductBarcodesWithStatus(Integer productId) {
        try {
            List<BarcodeEntity> barcodes = barcodeDAO.findByProduct(productId);
            
            // Log active barcode
            BarcodeEntity activeBarcode = getActiveBarcode(productId);
            if (activeBarcode != null) {
                util.LoggerUtil.logInfo("🔴 ACTIVE BARCODE: " + activeBarcode.getBarcodeNumber());
            }
            
            return barcodes;
        } catch (Exception e) {
            showError("Error retrieving barcode statuses: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Show error message
     */
    private void showError(String message) {
        JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
