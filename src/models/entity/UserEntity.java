package models.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * JPA Entity for Users
 * Manages user authentication and authorization
 * 
 * @author Shop Management System
 */
@Entity
@Table(name = "users", 
    uniqueConstraints = @UniqueConstraint(columnNames = "username"),
    indexes = {
        @Index(name = "idx_username", columnList = "username"),
        @Index(name = "idx_role", columnList = "role")
    }
)
public class UserEntity implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;
    
    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;
    
    @Column(name = "password", nullable = false, length = 255)
    private String password; // Should be hashed
    
    @Column(name = "full_name", length = 200)
    private String fullName;
    
    @Column(name = "email", length = 100)
    private String email;
    
    @Column(name = "role", nullable = false, length = 20)
    private String role = "CASHIER"; // ADMIN, MANAGER, CASHIER (legacy field)
    
    // TEMPORARILY COMMENTED OUT - Hibernate mapping issue to be resolved
    // @ManyToOne(fetch = FetchType.EAGER)
    // @JoinColumn(name = "role_id", referencedColumnName = "role_id")
    // private RoleEntity roleEntity;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "last_login")
    private LocalDateTime lastLogin;
    
    // Constructors
    public UserEntity() {
        this.createdAt = LocalDateTime.now();
    }
    
    public UserEntity(String username, String password, String fullName, String role) {
        this();
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.role = role;
    }
    
    // Lifecycle callback
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
    
    // Business methods
    public boolean hasRole(String... roles) {
        for (String role : roles) {
            if (this.role.equalsIgnoreCase(role)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(this.role);
    }
    
    public boolean isManager() {
        return "MANAGER".equalsIgnoreCase(this.role);
    }
    
    public void updateLastLogin() {
        this.lastLogin = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Integer getUserId() {
        return userId;
    }
    
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    // TEMPORARILY COMMENTED OUT - Hibernate mapping issue
    // public RoleEntity getRoleEntity() {
    //     return roleEntity;
    // }
    // 
    // public void setRoleEntity(RoleEntity roleEntity) {
    //     this.roleEntity = roleEntity;
    //     // Sync legacy role field with roleEntity
    //     if (roleEntity != null) {
    //         this.role = roleEntity.getName();
    //     }
    // }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getLastLogin() {
        return lastLogin;
    }
    
    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }
    
    @Override
    public String toString() {
        return "UserEntity{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", role='" + role + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}
