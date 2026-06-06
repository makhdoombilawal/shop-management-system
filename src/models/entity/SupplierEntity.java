package models.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * JPA Entity for Suppliers
 * Manages supplier information for inventory purchasing
 * 
 * @author Shop Management System - Enterprise Edition
 */
@Entity
@Table(name = "suppliers", indexes = {
    @Index(name = "idx_supplier_name", columnList = "company_name"),
    @Index(name = "idx_supplier_status", columnList = "status")
})
public class SupplierEntity implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "supplier_id")
    private Integer supplierId;
    
    @Column(name = "company_name", nullable = false, length = 200)
    private String companyName;
    
    @Column(name = "contact_person", length = 100)
    private String contactPerson;
    
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;
    
    @Column(name = "email", length = 100)
    private String email;
    
    @Column(name = "address", length = 2000)
    private String address;
    
    @Column(name = "city", length = 100)
    private String city;
    
    @Column(name = "country", length = 100)
    private String country;
    
    @Column(name = "tax_number", length = 50)
    private String taxNumber;
    
    @Column(name = "payment_terms", length = 100)
    private String paymentTerms; // e.g., "Net 30", "COD", "Net 60"
    
    @Column(name = "credit_limit", precision = 12, scale = 2)
    private Double creditLimit;
    
    @Column(name = "current_balance", precision = 12, scale = 2)
    private Double currentBalance = 0.0;
    
    @Column(name = "status", length = 20)
    private String status = "active"; // active, inactive, blocked
    
    @Column(name = "remarks", length = 2000)
    private String remarks;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "last_purchase_date")
    private LocalDateTime lastPurchaseDate;
    
    // Constructors
    public SupplierEntity() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public SupplierEntity(String companyName, String contactPerson, String phoneNumber) {
        this();
        this.companyName = companyName;
        this.contactPerson = contactPerson;
        this.phoneNumber = phoneNumber;
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
    public boolean isActive() {
        return "active".equalsIgnoreCase(this.status);
    }
    
    public void updateLastPurchase() {
        this.lastPurchaseDate = LocalDateTime.now();
    }
    
    public void addToBalance(Double amount) {
        if (this.currentBalance == null) {
            this.currentBalance = 0.0;
        }
        this.currentBalance += amount;
    }
    
    public void subtractFromBalance(Double amount) {
        if (this.currentBalance == null) {
            this.currentBalance = 0.0;
        }
        this.currentBalance -= amount;
    }
    
    public boolean hasReachedCreditLimit() {
        if (this.creditLimit == null || this.currentBalance == null) {
            return false;
        }
        return this.currentBalance >= this.creditLimit;
    }
    
    public String getDisplayName() {
        return companyName + (contactPerson != null ? " (" + contactPerson + ")" : "");
    }
    
    // Getters and Setters
    public Integer getSupplierId() {
        return supplierId;
    }
    
    public void setSupplierId(Integer supplierId) {
        this.supplierId = supplierId;
    }
    
    public String getCompanyName() {
        return companyName;
    }
    
    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
    
    public String getContactPerson() {
        return contactPerson;
    }
    
    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
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
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getCity() {
        return city;
    }
    
    public void setCity(String city) {
        this.city = city;
    }
    
    public String getCountry() {
        return country;
    }
    
    public void setCountry(String country) {
        this.country = country;
    }
    
    public String getTaxNumber() {
        return taxNumber;
    }
    
    public void setTaxNumber(String taxNumber) {
        this.taxNumber = taxNumber;
    }
    
    public String getPaymentTerms() {
        return paymentTerms;
    }
    
    public void setPaymentTerms(String paymentTerms) {
        this.paymentTerms = paymentTerms;
    }
    
    public Double getCreditLimit() {
        return creditLimit;
    }
    
    public void setCreditLimit(Double creditLimit) {
        this.creditLimit = creditLimit;
    }
    
    public Double getCurrentBalance() {
        return currentBalance;
    }
    
    public void setCurrentBalance(Double currentBalance) {
        this.currentBalance = currentBalance;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getRemarks() {
        return remarks;
    }
    
    public void setRemarks(String remarks) {
        this.remarks = remarks;
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
    
    public LocalDateTime getLastPurchaseDate() {
        return lastPurchaseDate;
    }
    
    public void setLastPurchaseDate(LocalDateTime lastPurchaseDate) {
        this.lastPurchaseDate = lastPurchaseDate;
    }
    
    @Override
    public String toString() {
        return "SupplierEntity{" +
                "supplierId=" + supplierId +
                ", companyName='" + companyName + '\'' +
                ", contactPerson='" + contactPerson + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
