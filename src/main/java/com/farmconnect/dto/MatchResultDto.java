package com.farmconnect.dto;

import com.farmconnect.entity.BuyerRequirement;
import com.farmconnect.entity.CropListing;
import java.util.ArrayList;
import java.util.List;

public class MatchResultDto {
    private CropListing cropListing;
    private BuyerRequirement buyerRequirement;
    private int suitabilityScore; // 0 - 100
    private List<String> reasons = new ArrayList<>();
    private Double distanceKm;
    private String locationSummary;

    public MatchResultDto() {}

    public CropListing getCropListing() { return cropListing; }
    public void setCropListing(CropListing cropListing) { this.cropListing = cropListing; }
    public BuyerRequirement getBuyerRequirement() { return buyerRequirement; }
    public void setBuyerRequirement(BuyerRequirement buyerRequirement) { this.buyerRequirement = buyerRequirement; }
    public int getSuitabilityScore() { return suitabilityScore; }
    public void setSuitabilityScore(int suitabilityScore) { this.suitabilityScore = suitabilityScore; }
    public List<String> getReasons() { return reasons; }
    public void setReasons(List<String> reasons) { this.reasons = reasons; }
    public Double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(Double distanceKm) { this.distanceKm = distanceKm; }
    public String getLocationSummary() { return locationSummary; }
    public void setLocationSummary(String locationSummary) { this.locationSummary = locationSummary; }
}
