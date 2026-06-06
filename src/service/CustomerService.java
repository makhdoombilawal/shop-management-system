package service;

import dao.CustomerHibernateDAO;
import models.entity.CustomerEntity;
import util.ValidationUtil;

import javax.swing.JOptionPane;
import java.util.List;
import java.util.Optional;

/**
 * Service layer for Customer operations
 * Provides business logic and validation for customer management
 */
public class CustomerService {
    
    private final CustomerHibernateDAO customerDAO;
    
    public CustomerService() {
        this.customerDAO = new CustomerHibernateDAO();
    }
    
    /**
     * Get all active customers
     */
    public List<CustomerEntity> getAllCustomers() {
        return customerDAO.findAll();
    }
    
    /**
     * Get customer by ID
     */
    public Optional<CustomerEntity> getCustomerById(int customerId) {
        return customerDAO.findById(customerId);
    }
    
    /**
     * Add new customer with validation
     */
    public boolean addCustomer(CustomerEntity customer) {
        try {
            // Validate customer data
            if (!validateCustomer(customer)) {
                return false;
            }
            
            // Check if phone number already exists - use the service method that wraps DAO call
            Optional<CustomerEntity> existing = findByPhone(customer.getPhoneNumber());
            if (existing.isPresent()) {
                JOptionPane.showMessageDialog(null,
                    "⚠️ Customer with phone number " + customer.getPhoneNumber() + " already exists!",
                    "Duplicate Customer",
                    JOptionPane.WARNING_MESSAGE);
                return false;
            }
            
            // Set default values
            if (customer.getStatus() == null) {
                customer.setStatus("active");
            }
            if (customer.getTotalPurchases() == null) {
                customer.setTotalPurchases(0.0);
            }
            
            customerDAO.save(customer);
            JOptionPane.showMessageDialog(null,
                "✅ Customer added successfully!",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
            return true;
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                "❌ Error adding customer: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Update existing customer with validation
     */
    public boolean updateCustomer(CustomerEntity customer) {
        try {
            // Validate customer data
            if (!validateCustomer(customer)) {
                return false;
            }
            
            // Check if customer exists
            Optional<CustomerEntity> existing = customerDAO.findById(customer.getCustomerId());
            if (!existing.isPresent()) {
                JOptionPane.showMessageDialog(null,
                    "⚠️ Customer not found!",
                    "Not Found",
                    JOptionPane.WARNING_MESSAGE);
                return false;
            }
            
            customerDAO.update(customer);
            JOptionPane.showMessageDialog(null,
                "✅ Customer updated successfully!",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
            return true;
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                "❌ Error updating customer: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Delete customer (soft delete - sets status to 'inactive')
     */
    public boolean deleteCustomer(int customerId) {
        try {
            Optional<CustomerEntity> customer = customerDAO.findById(customerId);
            if (!customer.isPresent()) {
                JOptionPane.showMessageDialog(null,
                    "⚠️ Customer not found!",
                    "Not Found",
                    JOptionPane.WARNING_MESSAGE);
                return false;
            }
            
            // Soft delete - set status to inactive
            CustomerEntity c = customer.get();
            c.setStatus("inactive");
            customerDAO.update(c);
            
            JOptionPane.showMessageDialog(null,
                "✅ Customer deleted successfully!",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
            return true;
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                "❌ Error deleting customer: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Find customer by phone number
     */
    public Optional<CustomerEntity> findByPhone(String phoneNumber) {
        try {
            List<CustomerEntity> customers = customerDAO.findByPhoneNumber(phoneNumber);
            return customers.isEmpty() ? Optional.empty() : Optional.of(customers.get(0));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    
    /**
     * Find customer by email
     */
    public Optional<CustomerEntity> findByEmail(String email) {
        try {
            CustomerEntity customer = customerDAO.findByEmail(email);
            return Optional.ofNullable(customer);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    
    /**
     * Get top customers by total purchases
     */
    public List<CustomerEntity> getTopCustomers(int limit) {
        try {
            // CustomerDAO doesn't have this method yet, return empty list
            return new java.util.ArrayList<>();
        } catch (Exception e) {
            return new java.util.ArrayList<>();
        }
    }
    
    /**
     * Get total count of active customers
     */
    public Long getTotalCustomersCount() {
        return customerDAO.count();
    }
    
    /**
     * Check if phone number already exists
     */
    public boolean isPhoneNumberExists(String phoneNumber) {
        try {
            Optional<CustomerEntity> existing = findByPhone(phoneNumber);
            return existing.isPresent();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Search customers by name (partial match)
     */
    public List<CustomerEntity> searchByName(String name) {
        try {
            return customerDAO.findByName(name);
        } catch (Exception e) {
            showError("Error searching customers: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Save or update customer with validation
     */
    public CustomerEntity saveCustomer(CustomerEntity customer) {
        try {
            // Validate customer data
            if (!validateCustomer(customer)) {
                return null;
            }
            
            // Set default values if not provided
            if (customer.getStatus() == null || customer.getStatus().isEmpty()) {
                customer.setStatus("active");
            }
            if (customer.getTotalPurchases() == null) {
                customer.setTotalPurchases(0.0);
            }
            
            return customerDAO.save(customer);
            
        } catch (Exception e) {
            showError("Error saving customer: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Validate customer data
     */
    private boolean validateCustomer(CustomerEntity customer) {
        // Name validation
        if (customer.getName() == null || customer.getName().trim().isEmpty()) {
            JOptionPane.showMessageDialog(null,
                "⚠️ Customer name is required!",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        // Name length check
        if (customer.getName().length() > 100) {
            JOptionPane.showMessageDialog(null,
                "⚠️ Customer name must be less than 100 characters!",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        // Phone number validation
        if (customer.getPhoneNumber() == null || customer.getPhoneNumber().trim().isEmpty()) {
            JOptionPane.showMessageDialog(null,
                "⚠️ Phone number is required!",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        // Phone number format check (digits only, 10-15 characters)
        String phone = customer.getPhoneNumber().replaceAll("[\\s-]", "");
        if (!phone.matches("\\d{10,15}")) {
            JOptionPane.showMessageDialog(null,
                "⚠️ Phone number must be 10-15 digits!",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        // Email validation (if provided)
        if (customer.getEmail() != null && !customer.getEmail().trim().isEmpty()) {
            if (!ValidationUtil.isValidEmail(customer.getEmail())) {
                JOptionPane.showMessageDialog(null,
                    "⚠️ Invalid email format!",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Show error message
     */
    private void showError(String message) {
        JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
