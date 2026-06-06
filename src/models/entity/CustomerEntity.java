package models.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * JPA Entity for Customers
 * Represents customer information in the supermart system
 * 
 * @author Shop Management System
 */
@Entity
@Table(name = "customers", indexes = {
    @Index(name = "idx_customer_name", columnList = "name"),
    @Index(name = "idx_customer_phone", columnList = "phone_number")
})
public class CustomerEntity implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id")
    private Integer customerId;
    
    @Column(name = "name", nullable = false, length = 200)
    private String name;
    
    @Column(name = "address", length = 2000)
    private String address;
    
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;
    
    @Column(name = "email", length = 100)
    private String email;
    
    @Column(name = "remarks", length = 2000)
    private String remarks;
    
    @Column(name = "date_created", nullable = false, updatable = false)
    private LocalDateTime dateCreated;
    
    @Column(name = "last_purchase_date")
    private LocalDateTime lastPurchaseDate;
    
    @Column(name = "total_purchases", precision = 10, scale = 2)
    private Double totalPurchases = 0.0;
    
    @Column(name = "status", length = 20)
    private String status = "active"; // active, inactive
    
    // Constructors
    public CustomerEntity() {
        this.dateCreated = LocalDateTime.now();
    }
    
    public CustomerEntity(String name, String phoneNumber, String email) {
        this();
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
    }
    
    // Lifecycle callback
    @PrePersist
    protected void onCreate() {
        if (this.dateCreated == null) {
            this.dateCreated = LocalDateTime.now();
        }
    }
    
    // Business methods
    public void updateLastPurchase(Double amount) {
        this.lastPurchaseDate = LocalDateTime.now();
        this.totalPurchases = (this.totalPurchases != null ? this.totalPurchases : 0.0) + amount;
    }
    
    public boolean isActive() {
        return "active".equalsIgnoreCase(this.status);
    }
    
    // Getters and Setters
    public Integer getCustomerId() {
        return customerId;
    }
    
    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getRemarks() {
        return remarks;
    }
    
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
    
    public LocalDateTime getDateCreated() {
        return dateCreated;
    }
    
    public void setDateCreated(LocalDateTime dateCreated) {
        this.dateCreated = dateCreated;
    }
    
    public LocalDateTime getLastPurchaseDate() {
        return lastPurchaseDate;
    }
    
    public void setLastPurchaseDate(LocalDateTime lastPurchaseDate) {
        this.lastPurchaseDate = lastPurchaseDate;
    }
    
    public Double getTotalPurchases() {
        return totalPurchases;
    }
    
    public void setTotalPurchases(Double totalPurchases) {
        this.totalPurchases = totalPurchases;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    @Override
    public String toString() {
        return "CustomerEntity{" +
                "customerId=" + customerId +
                ", name='" + name + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
