package com.farmconnect.dto;

public class DemandInsightDto {
    private String cropName;
    private Double totalDemandQuantity; // from active buyer requirements
    private Double totalSupplyQuantity;  // from active crop listings
    private String unit;
    private Double averageBuyerMinPrice;
    private Double averageBuyerMaxPrice;
    private Double averageFarmerPrice;
    private String demandLevel; // High Demand, Moderate Demand, Balanced, Supply Exceeds Demand
    private String explanation;

    public DemandInsightDto() {}

    public String getCropName() { return cropName; }
    public void setCropName(String cropName) { this.cropName = cropName; }
    public Double getTotalDemandQuantity() { return totalDemandQuantity; }
    public void setTotalDemandQuantity(Double totalDemandQuantity) { this.totalDemandQuantity = totalDemandQuantity; }
    public Double getTotalSupplyQuantity() { return totalSupplyQuantity; }
    public void setTotalSupplyQuantity(Double totalSupplyQuantity) { this.totalSupplyQuantity = totalSupplyQuantity; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public Double getAverageBuyerMinPrice() { return averageBuyerMinPrice; }
    public void setAverageBuyerMinPrice(Double averageBuyerMinPrice) { this.averageBuyerMinPrice = averageBuyerMinPrice; }
    public Double getAverageBuyerMaxPrice() { return averageBuyerMaxPrice; }
    public void setAverageBuyerMaxPrice(Double averageBuyerMaxPrice) { this.averageBuyerMaxPrice = averageBuyerMaxPrice; }
    public Double getAverageFarmerPrice() { return averageFarmerPrice; }
    public void setAverageFarmerPrice(Double averageFarmerPrice) { this.averageFarmerPrice = averageFarmerPrice; }
    public String getDemandLevel() { return demandLevel; }
    public void setDemandLevel(String demandLevel) { this.demandLevel = demandLevel; }
    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
}
