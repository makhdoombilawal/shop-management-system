package models;

import models.entity.UserEntity;

/**
 * Enhanced Session management with Role-Based Access Control (RBAC)
 * Manages user authentication and authorization
 */
public class Session {

    private static boolean loggedIn = false;
    private static String username;
    private static Integer userId;
    private static String role; // ADMIN, MANAGER, CASHIER
    private static String fullName;
    private static UserEntity currentUser;

    /**
     * Login user with full details
     * @param user UserEntity of logged in user
     */
    public static void login(UserEntity user) {
        if (user != null) {
            loggedIn = true;
            username = user.getUsername();
            userId = user.getUserId();
            role = user.getRole();
            fullName = user.getFullName();
            currentUser = user;
        }
    }

    /**
     * Legacy login method (for compatibility)
     * @param user Username
     * @deprecated Use login(UserEntity) instead
     */
    @Deprecated
    public static void login(String user) {
        loggedIn = true;
        username = user;
        role = "ADMIN"; // Default role for legacy logins (hardcoded credentials)
    }

    /**
     * Logout current user
     */
    public static void logout() {
        loggedIn = false;
        username = null;
        userId = null;
        role = null;
        fullName = null;
        currentUser = null;
    }

    /**
     * Check if user is logged in
     * @return true if logged in
     */
    public static boolean isLoggedIn() {
        return loggedIn;
    }

    /**
     * Get current username
     * @return Username of logged in user
     */
    public static String getUsername() {
        return username;
    }

    /**
     * Get current user ID
     * @return User ID of logged in user
     */
    public static Integer getUserId() {
        return userId;
    }

    /**
     * Get current user role
     * @return Role of logged in user (ADMIN, MANAGER, CASHIER)
     */
    public static String getRole() {
        return role != null ? role : "CASHIER";
    }

    /**
     * Get current user full name
     * @return Full name of logged in user
     */
    public static String getFullName() {
        return fullName;
    }

    /**
     * Get current user entity
     * @return UserEntity of logged in user
     */
    public static UserEntity getCurrentUser() {
        return currentUser;
    }

    /**
     * Check if current user has SUPER_ADMIN role (hardcoded Bilawal account)
     * @return true if user is SUPER_ADMIN
     */
    public static boolean isSuperAdmin() {
        return "SUPER_ADMIN".equalsIgnoreCase(role);
    }

    /**
     * Check if current user has ADMIN role
     * @return true if user is ADMIN
     */
    public static boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role) || isSuperAdmin();
    }

    /**
     * Check if current user has MANAGER role
     * @return true if user is MANAGER
     */
    public static boolean isManager() {
        return "MANAGER".equalsIgnoreCase(role);
    }

    /**
     * Check if current user has CASHIER role
     * @return true if user is CASHIER
     */
    public static boolean isCashier() {
        return "CASHIER".equalsIgnoreCase(role);
    }

    /**
     * Check if current user has admin or manager role
     * @return true if user is ADMIN, SUPER_ADMIN, or MANAGER
     */
    public static boolean isAdminOrManager() {
        return isAdmin() || isManager() || isSuperAdmin();
    }

    /**
     * Check if current user has any of the specified roles
     * @param roles Roles to check
     * @return true if user has any of the specified roles
     */
    public static boolean hasRole(String... roles) {
        if (role == null) return false;
        for (String r : roles) {
            if (role.equalsIgnoreCase(r)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if current user has permission for an action
     * @param action Action to check (e.g., "DELETE_PRODUCT", "VIEW_REPORTS")
     * @return true if user has permission
     */
    public static boolean hasPermission(String action) {
        if (!loggedIn || role == null) return false;

        switch (role.toUpperCase()) {
            case "SUPER_ADMIN":
                return true; // Super Admin has unrestricted access

            case "ADMIN":
                return true; // Admin has all permissions

            case "MANAGER":
                // Managers can do everything except user management
                return !action.startsWith("MANAGE_USERS");

            case "CASHIER":
                // Cashiers have limited permissions
                String[] cashierPermissions = {
                    "ADD_TRANSACTION",
                    "VIEW_PRODUCTS",
                    "VIEW_CUSTOMERS",
                    "ADD_CUSTOMER"
                };
                for (String perm : cashierPermissions) {
                    if (perm.equals(action)) {
                        return true;
                    }
                }
                return false;

            default:
                return false;
        }
    }
}
