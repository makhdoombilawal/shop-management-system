package models.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * JPA Entity for Categories (Product Categories)
 * Organizes products into logical groups for easier management
 * 
 * @author Shop Management System
 */
@Entity
@Table(name = "categories", 
    uniqueConstraints = @UniqueConstraint(columnNames = "name"),
    indexes = {
        @Index(name = "idx_category_name", columnList = "name"),
        @Index(name = "idx_parent", columnList = "parent_category_id")
    }
)
public class CategoryEntity implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Integer categoryId;
    
    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;
    
    @Column(name = "description", length = 2000)
    private String description;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_category_id", referencedColumnName = "category_id")
    private CategoryEntity parentCategory;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public CategoryEntity() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public CategoryEntity(String name, String description) {
        this();
        this.name = name;
        this.description = description;
    }
    
    public CategoryEntity(String name, String description, CategoryEntity parentCategory) {
        this(name, description);
        this.parentCategory = parentCategory;
    }
    
    // Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        this.updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Business methods
    public boolean hasParent() {
        return this.parentCategory != null;
    }
    
    public boolean isTopLevel() {
        return this.parentCategory == null;
    }
    
    public String getFullPath() {
        if (hasParent()) {
            return parentCategory.getFullPath() + " > " + name;
        }
        return name;
    }
    
    // Getters and Setters
    public Integer getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
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
    
    public CategoryEntity getParentCategory() {
        return parentCategory;
    }
    
    public void setParentCategory(CategoryEntity parentCategory) {
        this.parentCategory = parentCategory;
    }
    
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
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    @Override
    public String toString() {
        return "CategoryEntity{" +
                "categoryId=" + categoryId +
                ", name='" + name + '\'' +
                ", isActive=" + isActive +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CategoryEntity)) return false;
        CategoryEntity that = (CategoryEntity) o;
        return categoryId != null && categoryId.equals(that.categoryId);
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
