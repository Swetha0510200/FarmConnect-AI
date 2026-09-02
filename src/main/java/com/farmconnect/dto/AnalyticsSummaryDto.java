package com.farmconnect.dto;

import java.util.HashMap;
import java.util.Map;

public class AnalyticsSummaryDto {
    private long totalListings;
    private long activeListings;
    private long totalOrders;
    private long completedOrders;
    private double totalQuantitySold;
    private double totalSalesRevenue;
    private double pendingRevenue;
    private Map<String, Double> cropSalesDistribution = new HashMap<>();
    private Map<String, Long> orderStatusDistribution = new HashMap<>();

    public AnalyticsSummaryDto() {}

    public long getTotalListings() { return totalListings; }
    public void setTotalListings(long totalListings) { this.totalListings = totalListings; }
    public long getActiveListings() { return activeListings; }
    public void setActiveListings(long activeListings) { this.activeListings = activeListings; }
    public long getTotalOrders() { return totalOrders; }
    public void setTotalOrders(long totalOrders) { this.totalOrders = totalOrders; }
    public long getCompletedOrders() { return completedOrders; }
    public void setCompletedOrders(long completedOrders) { this.completedOrders = completedOrders; }
    public double getTotalQuantitySold() { return totalQuantitySold; }
    public void setTotalQuantitySold(double totalQuantitySold) { this.totalQuantitySold = totalQuantitySold; }
    public double getTotalSalesRevenue() { return totalSalesRevenue; }
    public void setTotalSalesRevenue(double totalSalesRevenue) { this.totalSalesRevenue = totalSalesRevenue; }
    public double getPendingRevenue() { return pendingRevenue; }
    public void setPendingRevenue(double pendingRevenue) { this.pendingRevenue = pendingRevenue; }
    public Map<String, Double> getCropSalesDistribution() { return cropSalesDistribution; }
    public void setCropSalesDistribution(Map<String, Double> cropSalesDistribution) { this.cropSalesDistribution = cropSalesDistribution; }
    public Map<String, Long> getOrderStatusDistribution() { return orderStatusDistribution; }
    public void setOrderStatusDistribution(Map<String, Long> orderStatusDistribution) { this.orderStatusDistribution = orderStatusDistribution; }
}
