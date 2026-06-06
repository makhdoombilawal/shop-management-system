package models.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * JPA Entity for Roles (Role-Based Access Control)
 * Defines roles that can be assigned to users
 * 
 * @author Shop Management System
 */
@Entity
@Table(name = "roles", 
    uniqueConstraints = @UniqueConstraint(columnNames = "name"),
    indexes = {
        @Index(name = "idx_role_name", columnList = "name")
    }
)
public class RoleEntity implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Integer roleId;
    
    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name; // ADMIN, MANAGER, CASHIER, INVENTORY_CLERK, etc.
    
    @Column(name = "description", length = 255)
    private String description;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // Constructors
    public RoleEntity() {
        this.createdAt = LocalDateTime.now();
    }
    
    public RoleEntity(String name, String description) {
        this();
        this.name = name;
        this.description = description;
    }
    
    // Lifecycle callback
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
    
    // Business methods
    public boolean isAdminRole() {
        return "ADMIN".equalsIgnoreCase(this.name);
    }
    
    public boolean isManagerRole() {
        return "MANAGER".equalsIgnoreCase(this.name);
    }
    
    public boolean isCashierRole() {
        return "CASHIER".equalsIgnoreCase(this.name);
    }
    
    // Getters and Setters
    public Integer getRoleId() {
        return roleId;
    }
    
    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    @Override
    public String toString() {
        return "RoleEntity{" +
                "roleId=" + roleId +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RoleEntity)) return false;
        RoleEntity that = (RoleEntity) o;
        return roleId != null && roleId.equals(that.roleId);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
