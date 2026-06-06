package service;

import dao.SupplierHibernateDAO;
import models.entity.SupplierEntity;
import util.ValidationUtil;
import util.LoggerUtil;

import javax.swing.JOptionPane;
import java.util.List;
import java.util.Optional;

/**
 * Service layer for Supplier business logic
 * Handles validation and business rules for supplier management
 * 
 * @author Shop Management System - Enterprise Edition
 */
public class SupplierService {
    
    private final SupplierHibernateDAO supplierDAO;
    
    public SupplierService() {
        this.supplierDAO = new SupplierHibernateDAO();
    }
    
    /**
     * Add new supplier with validation
     */
    public SupplierEntity addSupplier(String companyName, String contactPerson, 
                                      String phoneNumber, String email, String address,
                                      String paymentTerms, Double creditLimit, String remarks) {
        try {
            // Validate inputs
            if (!ValidationUtil.isNotEmpty(companyName)) {
                showError("Company name is required!");
                return null;
            }
            
            // Check for duplicate
            if (supplierDAO.supplierExists(companyName)) {
                showError("Supplier with company name '" + companyName + "' already exists!");
                return null;
            }
            
            // Validate email if provided
            if (ValidationUtil.isNotEmpty(email) && !ValidationUtil.isValidEmail(email)) {
                showError("Invalid email format!");
                return null;
            }
            
            // Validate credit limit if provided
            if (creditLimit != null && creditLimit < 0) {
                showError("Credit limit cannot be negative!");
                return null;
            }
            
            // Create supplier entity
            SupplierEntity supplier = new SupplierEntity();
            supplier.setCompanyName(companyName);
            supplier.setContactPerson(contactPerson);
            supplier.setPhoneNumber(phoneNumber);
            supplier.setEmail(email);
            supplier.setAddress(address);
            supplier.setPaymentTerms(paymentTerms);
            supplier.setCreditLimit(creditLimit);
            supplier.setRemarks(remarks);
            supplier.setStatus("active");
            supplier.setCurrentBalance(0.0);
            
            // Save to database
            SupplierEntity savedSupplier = supplierDAO.save(supplier);
            
            LoggerUtil.logInfo(SupplierService.class, 
                String.format("New supplier added: %s (ID: %d)", 
                    companyName, savedSupplier.getSupplierId()));
            
            return savedSupplier;
            
        } catch (Exception e) {
            LoggerUtil.logError(SupplierService.class, "Error adding supplier", e);
            showError("Error adding supplier: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Update existing supplier
     */
    public boolean updateSupplier(SupplierEntity supplier) {
        try {
            // Validate inputs
            if (!ValidationUtil.isNotEmpty(supplier.getCompanyName())) {
                showError("Company name is required!");
                return false;
            }
            
            // Validate email if provided
            if (ValidationUtil.isNotEmpty(supplier.getEmail()) && 
                !ValidationUtil.isValidEmail(supplier.getEmail())) {
                showError("Invalid email format!");
                return false;
            }
            
            // Update supplier
            supplierDAO.update(supplier);
            
            LoggerUtil.logInfo(SupplierService.class, 
                String.format("Supplier updated: %s (ID: %d)", 
                    supplier.getCompanyName(), supplier.getSupplierId()));
            
            return true;
            
        } catch (Exception e) {
            LoggerUtil.logError(SupplierService.class, "Error updating supplier", e);
            showError("Error updating supplier: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Delete supplier (soft delete by marking inactive)
     */
    public boolean deleteSupplier(Integer supplierId) {
        try {
            Optional<SupplierEntity> supplierOpt = supplierDAO.findById(supplierId);
            if (!supplierOpt.isPresent()) {
                showError("Supplier not found!");
                return false;
            }
            
            SupplierEntity supplier = supplierOpt.get();
            
            // Check if supplier has outstanding balance
            if (supplier.getCurrentBalance() != null && supplier.getCurrentBalance() > 0) {
                boolean confirm = showConfirm(
                    "Supplier has outstanding balance of $" + 
                    String.format("%.2f", supplier.getCurrentBalance()) + 
                    ". Are you sure you want to deactivate this supplier?");
                
                if (!confirm) {
                    return false;
                }
            }
            
            // Soft delete - mark as inactive
            supplier.setStatus("inactive");
            supplierDAO.update(supplier);
            
            LoggerUtil.logInfo(SupplierService.class, 
                String.format("Supplier deactivated: %s (ID: %d)", 
                    supplier.getCompanyName(), supplierId));
            
            return true;
            
        } catch (Exception e) {
            LoggerUtil.logError(SupplierService.class, "Error deleting supplier", e);
            showError("Error deleting supplier: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get all suppliers
     */
    public List<SupplierEntity> getAllSuppliers() {
        try {
            return supplierDAO.findAll();
        } catch (Exception e) {
            LoggerUtil.logError(SupplierService.class, "Error loading suppliers", e);
            showError("Error loading suppliers: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Get active suppliers only
     */
    public List<SupplierEntity> getActiveSuppliers() {
        try {
            return supplierDAO.findActiveSuppliers();
        } catch (Exception e) {
            LoggerUtil.logError(SupplierService.class, "Error loading active suppliers", e);
            showError("Error loading suppliers: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Get supplier by ID
     */
    public Optional<SupplierEntity> getSupplierById(Integer supplierId) {
        try {
            return supplierDAO.findById(supplierId);
        } catch (Exception e) {
            LoggerUtil.logError(SupplierService.class, "Error finding supplier", e);
            showError("Error finding supplier: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Search suppliers by company name
     */
    public List<SupplierEntity> searchSuppliers(String companyName) {
        try {
            return supplierDAO.findByCompanyName(companyName);
        } catch (Exception e) {
            LoggerUtil.logError(SupplierService.class, "Error searching suppliers", e);
            showError("Error searching suppliers: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Get suppliers over credit limit
     */
    public List<SupplierEntity> getSuppliersOverCreditLimit() {
        try {
            return supplierDAO.findOverCreditLimit();
        } catch (Exception e) {
            LoggerUtil.logError(SupplierService.class, "Error checking credit limits", e);
            throw e;
        }
    }
    
    /**
     * Update supplier's last purchase date
     */
    public boolean updateLastPurchaseDate(Integer supplierId) {
        try {
            return supplierDAO.updateLastPurchaseDate(supplierId);
        } catch (Exception e) {
            LoggerUtil.logError(SupplierService.class, "Error updating purchase date", e);
            return false;
        }
    }
    
    /**
     * Add to supplier balance (new purchase)
     */
    public boolean addToSupplierBalance(Integer supplierId, Double amount) {
        try {
            return supplierDAO.updateBalance(supplierId, amount, true);
        } catch (Exception e) {
            LoggerUtil.logError(SupplierService.class, "Error updating supplier balance", e);
            return false;
        }
    }
    
    /**
     * Subtract from supplier balance (payment made)
     */
    public boolean subtractFromSupplierBalance(Integer supplierId, Double amount) {
        try {
            return supplierDAO.updateBalance(supplierId, amount, false);
        } catch (Exception e) {
            LoggerUtil.logError(SupplierService.class, "Error updating supplier balance", e);
            return false;
        }
    }
    
    /**
     * Get count of active suppliers
     */
    public long getActiveSupplierCount() {
        try {
            return supplierDAO.countActiveSuppliers();
        } catch (Exception e) {
            LoggerUtil.logError(SupplierService.class, "Error counting suppliers", e);
            return 0;
        }
    }
    
    // Helper methods
    private void showError(String message) {
        JOptionPane.showMessageDialog(null, message, "Supplier Error", JOptionPane.ERROR_MESSAGE);
    }
    
    private boolean showConfirm(String message) {
        int result = JOptionPane.showConfirmDialog(null, message, "Confirm Action", JOptionPane.YES_NO_OPTION);
        return result == JOptionPane.YES_OPTION;
    }
}
