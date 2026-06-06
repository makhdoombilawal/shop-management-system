package models.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * JPA Entity for Products
 * Represents a product in the supermart inventory system
 * 
 * @author Shop Management System
 */
@Entity
@Table(name = "products", indexes = {
    @Index(name = "idx_product_name", columnList = "name"),
    @Index(name = "idx_product_type", columnList = "product_type")
})
public class ProductEntity implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Integer productId;
    
    @Column(name = "name", nullable = false, length = 200)
    private String name;
    
    @Column(name = "product_type", length = 100)
    private String productType;
    
    // Category relationship - separate management through CategoryService
    // Using category_id for future integration without ORM relationship to avoid Hibernate issues
    @Column(name = "category_id")
    private Integer categoryId;
    
    @Column(name = "remarks", length = 1000)
    private String remarks;
    
    @Column(name = "stock", nullable = false)
    private Integer stock = 0;
    
    @Column(name = "sell_price", nullable = false, precision = 10, scale = 2)
    private Double sellPrice;
    
    @Column(name = "purchase_price", nullable = false, precision = 10, scale = 2)
    private Double purchasePrice;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "status", length = 20)
    private String status = "active"; // active, discontinued
    
    // Constructors
    public ProductEntity() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public ProductEntity(String name, String productType, Integer stock, 
                         Double sellPrice, Double purchasePrice) {
        this();
        this.name = name;
        this.productType = productType;
        this.stock = stock;
        this.sellPrice = sellPrice;
        this.purchasePrice = purchasePrice;
    }
    
    // Lifecycle callbacks
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        this.updatedAt = LocalDateTime.now();
    }
    
    // Business logic methods
    public Double calculateProfit() {
        return (sellPrice != null && purchasePrice != null) 
                ? sellPrice - purchasePrice : 0.0;
    }
    
    public Double calculateProfitMargin() {
        if (purchasePrice == null || purchasePrice == 0) return 0.0;
        return ((sellPrice - purchasePrice) / purchasePrice) * 100;
    }
    
    public boolean isLowStock(int threshold) {
        return stock != null && stock < threshold;
    }
    
    public boolean isOutOfStock() {
        return stock == null || stock <= 0;
    }
    
    // Getters and Setters
    public Integer getProductId() {
        return productId;
    }
    
    public void setProductId(Integer productId) {
        this.productId = productId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getProductType() {
        return productType;
    }
    
    public void setProductType(String productType) {
        this.productType = productType;
    }
    
    public Integer getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }
    
    public String getRemarks() {
        return remarks;
    }
    
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
    
    public Integer getStock() {
        return stock;
    }
    
    public void setStock(Integer stock) {
        this.stock = stock;
    }
    
    public Double getSellPrice() {
        return sellPrice;
    }
    
    public void setSellPrice(Double sellPrice) {
        this.sellPrice = sellPrice;
    }
    
    public Double getPurchasePrice() {
        return purchasePrice;
    }
    
    public void setPurchasePrice(Double purchasePrice) {
        this.purchasePrice = purchasePrice;
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
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    @Override
    public String toString() {
        return "ProductEntity{" +
                "productId=" + productId +
                ", name='" + name + '\'' +
                ", stock=" + stock +
                ", sellPrice=" + sellPrice +
                ", status='" + status + '\'' +
                '}';
    }
}
