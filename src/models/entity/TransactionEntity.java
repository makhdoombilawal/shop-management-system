package models.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * JPA Entity for Transactions
 * Represents sales and purchase transactions in the supermart
 * 
 * @author Shop Management System
 */
@Entity
@Table(name = "transactions", indexes = {
    @Index(name = "idx_transaction_date", columnList = "transaction_date"),
    @Index(name = "idx_transaction_type", columnList = "transaction_type"),
    @Index(name = "idx_customer_id", columnList = "customer_id"),
    @Index(name = "idx_product_id", columnList = "product_id"),
    @Index(name = "idx_supplier_id", columnList = "supplier_id")
})
public class TransactionEntity implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Integer transactionId;
    
    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;
    
    @Column(name = "transaction_type", nullable = false, length = 20)
    private String transactionType; // SALE, PURCHASE, RETURN
    
    @ManyToOne(fetch = FetchType.EAGER, targetEntity = CustomerEntity.class)
    @JoinColumn(name = "customer_id")
    private CustomerEntity customer;
    
    @ManyToOne(fetch = FetchType.EAGER, targetEntity = SupplierEntity.class)
    @JoinColumn(name = "supplier_id")
    private SupplierEntity supplier;
    
    @ManyToOne(fetch = FetchType.EAGER, targetEntity = ProductEntity.class)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;
    
    @Column(name = "quantity", nullable = false)
    private Integer quantity;
    
    @Column(name = "sell_price", precision = 10, scale = 2)
    private Double sellPrice;
    
    @Column(name = "purchase_price", precision = 10, scale = 2)
    private Double purchasePrice;
    
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private Double totalAmount;
    
    @Column(name = "payment_type", length = 20)
    private String paymentType; // CASH, CARD, MOBILE
    
    @Column(name = "remarks", length = 2000)
    private String remarks;
    
    @Column(name = "stock_after_transaction")
    private Integer stockAfterTransaction;
    
    // Transient fields for display purposes
    @Transient
    private String customerName;
    
    @Transient
    private String productName;
    
    // Constructors
    public TransactionEntity() {
        this.transactionDate = LocalDateTime.now();
    }
    
    public TransactionEntity(String transactionType, ProductEntity product, 
                             Integer quantity, Double price) {
        this();
        this.transactionType = transactionType;
        this.product = product;
        this.quantity = quantity;
        
        if ("SALE".equalsIgnoreCase(transactionType)) {
            this.sellPrice = price;
            this.totalAmount = quantity * price;
        } else {
            this.purchasePrice = price;
            this.totalAmount = quantity * price;
        }
    }
    
    // Lifecycle callbacks
    @PrePersist
    @PreUpdate
    protected void calculateTotal() {
        if (quantity != null && sellPrice != null && "SALE".equalsIgnoreCase(transactionType)) {
            this.totalAmount = quantity * sellPrice;
        } else if (quantity != null && purchasePrice != null) {
            this.totalAmount = quantity * purchasePrice;
        }
    }
    
    // Business methods
    public Double calculateProfit() {
        if ("SALE".equalsIgnoreCase(transactionType) && 
            sellPrice != null && purchasePrice != null) {
            return (sellPrice - purchasePrice) * quantity;
        }
        return 0.0;
    }
    
    public boolean isSale() {
        return "SALE".equalsIgnoreCase(transactionType);
    }
    
    public boolean isPurchase() {
        return "PURCHASE".equalsIgnoreCase(transactionType);
    }
    
    // Getters and Setters
    public Integer getTransactionId() {
        return transactionId;
    }
    
    public void setTransactionId(Integer transactionId) {
        this.transactionId = transactionId;
    }
    
    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }
    
    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }
    
    public String getTransactionType() {
        return transactionType;
    }
    
    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }
    
    public CustomerEntity getCustomer() {
        return customer;
    }
    
    public SupplierEntity getSupplier() {
        return supplier;
    }
    
    public void setSupplier(SupplierEntity supplier) {
        this.supplier = supplier;
    }
    
    public void setCustomer(CustomerEntity customer) {
        this.customer = customer;
    }
    
    public ProductEntity getProduct() {
        return product;
    }
    
    public void setProduct(ProductEntity product) {
        this.product = product;
    }
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
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
    
    public Double getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public String getPaymentType() {
        return paymentType;
    }
    
    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }
    
    public String getRemarks() {
        return remarks;
    }
    
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
    
    public Integer getStockAfterTransaction() {
        return stockAfterTransaction;
    }
    
    public void setStockAfterTransaction(Integer stockAfterTransaction) {
        this.stockAfterTransaction = stockAfterTransaction;
    }
    
    public String getCustomerName() {
        return customerName != null ? customerName : 
               (customer != null ? customer.getName() : "Walk-in");
    }
    
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
    
    public String getProductName() {
        return productName != null ? productName : 
               (product != null ? product.getName() : "");
    }
    
    public void setProductName(String productName) {
        this.productName = productName;
    }
    
    @Override
    public String toString() {
        return "TransactionEntity{" +
                "transactionId=" + transactionId +
                ", type='" + transactionType + '\'' +
                ", quantity=" + quantity +
                ", totalAmount=" + totalAmount +
                '}';
    }
}
