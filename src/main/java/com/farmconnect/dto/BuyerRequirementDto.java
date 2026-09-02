package com.farmconnect.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

public class BuyerRequirementDto {

    private Long id;

    @NotBlank(message = "Crop name is required")
    private String cropName;

    @NotNull(message = "Required quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Double requiredQuantity;

    @NotBlank(message = "Unit is required")
    private String unit = "kg";

    @NotNull(message = "Minimum price is required")
    @Min(value = 1, message = "Minimum price must be greater than 0")
    private Double minimumPrice;

    @NotNull(message = "Maximum price is required")
    @Min(value = 1, message = "Maximum price must be greater than 0")
    private Double maximumPrice;

    @NotNull(message = "Required from date is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate requiredFrom;

    @NotNull(message = "Required until date is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate requiredUntil;

    @NotBlank(message = "Location is required")
    private String location;

    private String district;
    private String state;
    private String qualityRequirement = "Any";
    private String description;

    public BuyerRequirementDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public String getQualityRequirement() { return qualityRequirement; }
    public void setQualityRequirement(String qualityRequirement) { this.qualityRequirement = qualityRequirement; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
