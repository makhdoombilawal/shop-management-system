package service;

import dao.RoleHibernateDAO;
import models.entity.RoleEntity;

import java.util.List;
import java.util.Optional;

/**
 * Service layer for Role management
 * Provides business logic for user role operations
 * 
 * @author Shop Management System
 */
public class RoleService {
    
    private final RoleHibernateDAO roleDAO;
    
    public RoleService() {
        this.roleDAO = new RoleHibernateDAO();
    }
    
    /**
     * Get all roles
     */
    public List<RoleEntity> getAllRoles() {
        try {
            return roleDAO.findAll();
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve roles: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get role by ID
     */
    public Optional<RoleEntity> getRoleById(Integer roleId) {
        try {
            if (roleId == null || roleId <= 0) {
                throw new IllegalArgumentException("Invalid role ID");
            }
            return roleDAO.findById(roleId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to find role: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get role by name
     */
    public Optional<RoleEntity> getRoleByName(String roleName) {
        try {
            if (roleName == null || roleName.trim().isEmpty()) {
                throw new IllegalArgumentException("Role name cannot be empty");
            }
            return roleDAO.findByName(roleName.trim());
        } catch (Exception e) {
            throw new RuntimeException("Failed to find role: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get all role names (for dropdowns)
     */
    public List<String> getAllRoleNames() {
        try {
            return roleDAO.findAllRoleNames();
        } catch (Exception e) {
            throw new RuntimeException("Failed to retrieve role names: " + e.getMessage(), e);
        }
    }
    
    /**
     * Create a new role
     */
    public void createRole(RoleEntity role) {
        try {
            // Validate role data
            validateRole(role);
            
            // Check if role name already exists
            if (roleDAO.roleExists(role.getName())) {
                throw new IllegalArgumentException("Role with name '" + role.getName() + "' already exists");
            }
            
            roleDAO.save(role);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create role: " + e.getMessage(), e);
        }
    }
    
    /**
     * Update an existing role
     */
    public void updateRole(RoleEntity role) {
        try {
            // Validate role data
            validateRole(role);
            
            if (role.getRoleId() == null || role.getRoleId() <= 0) {
                throw new IllegalArgumentException("Invalid role ID for update");
            }
            
            // Check if role exists
            Optional<RoleEntity> existing = roleDAO.findById(role.getRoleId());
            if (!existing.isPresent()) {
                throw new IllegalArgumentException("Role not found");
            }
            
            // Check if new name already exists (excluding current role)
            Optional<RoleEntity> roleWithSameName = roleDAO.findByName(role.getName());
            if (roleWithSameName.isPresent() && !roleWithSameName.get().getRoleId().equals(role.getRoleId())) {
                throw new IllegalArgumentException("Role with name '" + role.getName() + "' already exists");
            }
            
            roleDAO.update(role);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update role: " + e.getMessage(), e);
        }
    }
    
    /**
     * Delete a role
     */
    public void deleteRole(Integer roleId) {
        try {
            if (roleId == null || roleId <= 0) {
                throw new IllegalArgumentException("Invalid role ID");
            }
            
            Optional<RoleEntity> role = roleDAO.findById(roleId);
            if (!role.isPresent()) {
                throw new IllegalArgumentException("Role not found");
            }
            
            // Prevent deletion of system roles (ADMIN, MANAGER, CASHIER)
            String roleName = role.get().getName();
            if ("ADMIN".equalsIgnoreCase(roleName) || 
                "MANAGER".equalsIgnoreCase(roleName) || 
                "CASHIER".equalsIgnoreCase(roleName)) {
                throw new IllegalArgumentException("Cannot delete system role: " + roleName);
            }
            
            roleDAO.deleteById(roleId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete role: " + e.getMessage(), e);
        }
    }
    
    /**
     * Search roles by name
     */
    public List<RoleEntity> searchRoles(String keyword) {
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                return getAllRoles();
            }
            return roleDAO.searchByName(keyword.trim());
        } catch (Exception e) {
            throw new RuntimeException("Failed to search roles: " + e.getMessage(), e);
        }
    }
    
    /**
     * Check if a role exists
     */
    public boolean roleExists(String roleName) {
        try {
            if (roleName == null || roleName.trim().isEmpty()) {
                return false;
            }
            return roleDAO.roleExists(roleName.trim());
        } catch (Exception e) {
            throw new RuntimeException("Failed to check role existence: " + e.getMessage(), e);
        }
    }
    
    /**
     * Initialize default roles if they don't exist
     */
    public void initializeDefaultRoles() {
        try {
            roleDAO.createDefaultRoles();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize default roles: " + e.getMessage(), e);
        }
    }
    
    /**
     * Validate role entity
     */
    private void validateRole(RoleEntity role) {
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }
        
        if (role.getName() == null || role.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Role name is required");
        }
        
        if (role.getName().length() < 2 || role.getName().length() > 50) {
            throw new IllegalArgumentException("Role name must be between 2 and 50 characters");
        }
        
        // Validate role name format (letters, numbers, underscores only)
        if (!role.getName().matches("^[A-Za-z0-9_]+$")) {
            throw new IllegalArgumentException("Role name can only contain letters, numbers, and underscores");
        }
        
        // Description is optional but has length limit if provided
        if (role.getDescription() != null && role.getDescription().length() > 255) {
            throw new IllegalArgumentException("Role description cannot exceed 255 characters");
        }
    }
}
