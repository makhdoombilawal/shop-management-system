package models.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * JPA Entity for Stock Audit (Automatic Stock Change Tracking)
 * Records every stock change for inventory audit trail
 * 
 * @author Shop Management System
 */
@Entity
@Table(name = "stock_audit", indexes = {
    @Index(name = "idx_product", columnList = "product_id"),
    @Index(name = "idx_change_date", columnList = "change_date"),
    @Index(name = "idx_transaction", columnList = "transaction_id")
})
public class StockAuditEntity implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "audit_id")
    private Integer auditId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;
    
    @Column(name = "change_date", nullable = false)
    private LocalDateTime changeDate;
    
    @Column(name = "stock_before", nullable = false)
    private Integer stockBefore;
    
    @Column(name = "stock_after", nullable = false)
    private Integer stockAfter;
    
    @Column(name = "change_amount", nullable = false)
    private Integer changeAmount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "change_source", nullable = false, length = 20)
    private ChangeSource changeSource;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id")
    private TransactionEntity transaction;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by")
    private UserEntity changedBy;
    
    @Column(name = "remarks", length = 2000)
    private String remarks;
    
    // Enum for change source
    public enum ChangeSource {
        SALE, PURCHASE, RETURN, ADJUSTMENT, DAMAGE, SYSTEM
    }
    
    // Constructors
    public StockAuditEntity() {
        this.changeDate = LocalDateTime.now();
    }
    
    public StockAuditEntity(ProductEntity product, Integer stockBefore, Integer stockAfter, 
                           ChangeSource changeSource) {
        this();
        this.product = product;
        this.stockBefore = stockBefore;
        this.stockAfter = stockAfter;
        this.changeAmount = stockAfter - stockBefore;
        this.changeSource = changeSource;
    }
    
    // Lifecycle callback
    @PrePersist
    protected void onCreate() {
        if (this.changeDate == null) {
            this.changeDate = LocalDateTime.now();
        }
        // Auto-calculate change amount if not set
        if (this.changeAmount == null && this.stockBefore != null && this.stockAfter != null) {
            this.changeAmount = this.stockAfter - this.stockBefore;
        }
    }
    
    // Business methods
    public boolean isIncrease() {
        return changeAmount != null && changeAmount > 0;
    }
    
    public boolean isDecrease() {
        return changeAmount != null && changeAmount < 0;
    }
    
    public Integer getAbsoluteChange() {
        return changeAmount != null ? Math.abs(changeAmount) : 0;
    }
    
    // Getters and Setters
    public Integer getAuditId() {
        return auditId;
    }
    
    public void setAuditId(Integer auditId) {
        this.auditId = auditId;
    }
    
    public ProductEntity getProduct() {
        return product;
    }
    
    public void setProduct(ProductEntity product) {
        this.product = product;
    }
    
    public LocalDateTime getChangeDate() {
        return changeDate;
    }
    
    public void setChangeDate(LocalDateTime changeDate) {
        this.changeDate = changeDate;
    }
    
    public Integer getStockBefore() {
        return stockBefore;
    }
    
    public void setStockBefore(Integer stockBefore) {
        this.stockBefore = stockBefore;
    }
    
    public Integer getStockAfter() {
        return stockAfter;
    }
    
    public void setStockAfter(Integer stockAfter) {
        this.stockAfter = stockAfter;
    }
    
    public Integer getChangeAmount() {
        return changeAmount;
    }
    
    public void setChangeAmount(Integer changeAmount) {
        this.changeAmount = changeAmount;
    }
    
    public ChangeSource getChangeSource() {
        return changeSource;
    }
    
    public void setChangeSource(ChangeSource changeSource) {
        this.changeSource = changeSource;
    }
    
    public TransactionEntity getTransaction() {
        return transaction;
    }
    
    public void setTransaction(TransactionEntity transaction) {
        this.transaction = transaction;
    }
    
    public UserEntity getChangedBy() {
        return changedBy;
    }
    
    public void setChangedBy(UserEntity changedBy) {
        this.changedBy = changedBy;
    }
    
    public String getRemarks() {
        return remarks;
    }
    
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
    
    @Override
    public String toString() {
        return "StockAuditEntity{" +
                "auditId=" + auditId +
                ", stockBefore=" + stockBefore +
                ", stockAfter=" + stockAfter +
                ", changeAmount=" + changeAmount +
                ", changeSource=" + changeSource +
                ", changeDate=" + changeDate +
                '}';
    }
}
