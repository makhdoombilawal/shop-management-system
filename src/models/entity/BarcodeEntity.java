package models.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * JPA Entity for Barcodes
 * Manages barcode information for products in the supermart
 * 
 * @author Shop Management System
 */
@Entity
@Table(name = "barcode", 
    uniqueConstraints = @UniqueConstraint(columnNames = "barcode_number"),
    indexes = {
        @Index(name = "idx_barcode_status", columnList = "status"),
        @Index(name = "idx_product_id", columnList = "product_id")
    }
)
public class BarcodeEntity implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "barcode_id")
    private Integer barcodeId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;
    
    @Column(name = "barcode_number", nullable = false, unique = true, length = 50)
    private String barcodeNumber;
    
    @Column(name = "status", nullable = false, length = 20)
    private String status = "available"; // available, sold, damaged
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "sold_at")
    private LocalDateTime soldAt;
    
    @Column(name = "remarks", length = 1000)
    private String remarks;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true; // ENTERPRISE REQUIREMENT: Only ONE active barcode per product
    
    // Constructors
    public BarcodeEntity() {
        this.createdAt = LocalDateTime.now();
    }
    
    public BarcodeEntity(ProductEntity product, String barcodeNumber) {
        this();
        this.product = product;
        this.barcodeNumber = barcodeNumber;
        this.status = determineInitialStatus(product);
    }
    
    // Lifecycle callback
    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
    
    // Business methods
    private String determineInitialStatus(ProductEntity product) {
        return (product != null && product.getStock() > 0) ? "available" : "sold";
    }
    
    public boolean isAvailable() {
        return "available".equalsIgnoreCase(this.status);
    }
    
    public void markAsSold() {
        this.status = "sold";
        this.soldAt = LocalDateTime.now();
    }
    
    public void markAsDamaged(String reason) {
        this.status = "damaged";
        this.remarks = reason;
    }
    
    public void makeAvailable() {
        if (this.product != null && this.product.getStock() > 0) {
            this.status = "available";
            this.soldAt = null;
        }
    }
    
    // Getters and Setters
    public Integer getBarcodeId() {
        return barcodeId;
    }
    
    public void setBarcodeId(Integer barcodeId) {
        this.barcodeId = barcodeId;
    }
    
    public ProductEntity getProduct() {
        return product;
    }
    
    public void setProduct(ProductEntity product) {
        this.product = product;
    }
    
    public String getBarcodeNumber() {
        return barcodeNumber;
    }
    
    public void setBarcodeNumber(String barcodeNumber) {
        this.barcodeNumber = barcodeNumber;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getSoldAt() {
        return soldAt;
    }
    
    public void setSoldAt(LocalDateTime soldAt) {
        this.soldAt = soldAt;
    }
    
    public String getRemarks() {
        return remarks;
    }
    
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    public boolean isActivated() {
        return isActive != null && isActive;
    }
    
    @Override
    public String toString() {
        return "BarcodeEntity{" +
                "barcodeId=" + barcodeId +
                ", barcodeNumber='" + barcodeNumber + '\'' +
                ", status='" + status + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}
