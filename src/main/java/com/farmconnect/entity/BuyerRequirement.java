package com.farmconnect.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "buyer_requirements")
public class BuyerRequirement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private User buyer;

    @Column(nullable = false, length = 100)
    private String cropName;

    @Column(nullable = false)
    private Double requiredQuantity;

    @Column(nullable = false, length = 20)
    private String unit = "kg";

    @Column(nullable = false)
    private Double minimumPrice;

    @Column(nullable = false)
    private Double maximumPrice;

    @Column(nullable = false)
    private LocalDate requiredFrom;

    @Column(nullable = false)
    private LocalDate requiredUntil;

    @Column(nullable = false)
    private String location;

    private String district;
    private String state;
    private Double latitude;
    private Double longitude;

    @Column(length = 50)
    private String qualityRequirement;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RequirementStatus status = RequirementStatus.OPEN;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public BuyerRequirement() {}

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getBuyer() { return buyer; }
    public void setBuyer(User buyer) { this.buyer = buyer; }
    public String getCropName() { return cropName; }
    public void setCropName(String cropName) { this.cropName = cropName; }
    public Double getRequiredQuantity() { return requiredQuantity; }
    public void setRequiredQuantity(Double requiredQuantity) { this.requiredQuantity = requiredQuantity; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public Double getMinimumPrice() { return minimumPrice; }
    public void setMinimumPrice(Double minimumPrice) { this.minimumPrice = minimumPrice; }
    public Double getMaximumPrice() { return maximumPrice; }
    public void setMaximumPrice(Double maximumPrice) { this.maximumPrice = maximumPrice; }
    public LocalDate getRequiredFrom() { return requiredFrom; }
    public void setRequiredFrom(LocalDate requiredFrom) { this.requiredFrom = requiredFrom; }
    public LocalDate getRequiredUntil() { return requiredUntil; }
    public void setRequiredUntil(LocalDate requiredUntil) { this.requiredUntil = requiredUntil; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public String getQualityRequirement() { return qualityRequirement; }
    public void setQualityRequirement(String qualityRequirement) { this.qualityRequirement = qualityRequirement; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public RequirementStatus getStatus() { return status; }
    public void setStatus(RequirementStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
