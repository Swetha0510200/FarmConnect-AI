package com.farmconnect.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class OrderPlacementDto {

    @NotNull(message = "Crop listing ID is required")
    private Long cropListingId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Double quantity;

    @NotNull(message = "Agreed price is required")
    @Min(value = 1, message = "Price must be greater than 0")
    private Double agreedPrice;

    private String notes;

    public OrderPlacementDto() {}

    public Long getCropListingId() { return cropListingId; }
    public void setCropListingId(Long cropListingId) { this.cropListingId = cropListingId; }
    public Double getQuantity() { return quantity; }
    public void setQuantity(Double quantity) { this.quantity = quantity; }
    public Double getAgreedPrice() { return agreedPrice; }
    public void setAgreedPrice(Double agreedPrice) { this.agreedPrice = agreedPrice; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
