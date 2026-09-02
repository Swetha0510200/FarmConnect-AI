package com.farmconnect.dto;

import com.farmconnect.entity.DeliveryResponsibility;
import com.farmconnect.entity.DeliveryStatus;

public class DeliveryUpdateDto {
    private DeliveryResponsibility responsibility;
    private String pickupLocation;
    private String deliveryLocation;
    private Double distance;
    private DeliveryStatus status;
    private String notes;

    public DeliveryUpdateDto() {}

    public DeliveryResponsibility getResponsibility() { return responsibility; }
    public void setResponsibility(DeliveryResponsibility responsibility) { this.responsibility = responsibility; }
    public String getPickupLocation() { return pickupLocation; }
    public void setPickupLocation(String pickupLocation) { this.pickupLocation = pickupLocation; }
    public String getDeliveryLocation() { return deliveryLocation; }
    public void setDeliveryLocation(String deliveryLocation) { this.deliveryLocation = deliveryLocation; }
    public Double getDistance() { return distance; }
    public void setDistance(Double distance) { this.distance = distance; }
    public DeliveryStatus getStatus() { return status; }
    public void setStatus(DeliveryStatus status) { this.status = status; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
