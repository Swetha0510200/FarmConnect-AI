package com.farmconnect.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

public class CropListingDto {

    private Long id;

    @NotBlank(message = "Crop name is required")
    private String cropName;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Double quantity;

    @NotBlank(message = "Unit is required")
    private String unit = "kg";

    @NotNull(message = "Expected price is required")
    @Min(value = 1, message = "Expected price must be greater than 0")
    private Double expectedPrice;

    private String qualityGrade = "Grade A";

    @NotNull(message = "Available from date is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate availableFrom;

    @NotNull(message = "Available until date is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate availableUntil;

    @NotBlank(message = "Location is required")
    private String location;

    private String district;
    private String state;
    private String description;

    public CropListingDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCropName() { return cropName; }
    public void setCropName(String cropName) { this.cropName = cropName; }
    public Double getQuantity() { return quantity; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public Double getExpectedPrice() { return expectedPrice; }
    public void setExpectedPrice(Double expectedPrice) { this.expectedPrice = expectedPrice; }
    public String getQualityGrade() { return qualityGrade; }
    public void setQualityGrade(String qualityGrade) { this.qualityGrade = qualityGrade; }
    public LocalDate getAvailableFrom() { return availableFrom; }
    public void setAvailableFrom(LocalDate availableFrom) { this.availableFrom = availableFrom; }
    public LocalDate getAvailableUntil() { return availableUntil; }
    public void setAvailableUntil(LocalDate availableUntil) { this.availableUntil = availableUntil; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
