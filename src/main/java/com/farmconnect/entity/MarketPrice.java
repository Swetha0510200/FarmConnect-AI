package com.farmconnect.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "market_prices")
public class MarketPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String cropName;

    @Column(nullable = false, length = 150)
    private String marketName;

    @Column(nullable = false, length = 100)
    private String district;

    @Column(nullable = false, length = 100)
    private String state;

    @Column(nullable = false)
    private Double price;

    private Double minPrice;
    private Double maxPrice;

    @Column(nullable = false, length = 20)
    private String unit = "kg";

    @Column(nullable = false)
    private LocalDate date;

    @Column(length = 150)
    private String source = "Agmarknet / Open Government Data";

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public MarketPrice() {}

    public MarketPrice(String cropName, String marketName, String district, String state, Double price, Double minPrice, Double maxPrice, String unit, LocalDate date, String source) {
        this.cropName = cropName;
        this.marketName = marketName;
        this.district = district;
        this.state = state;
        this.price = price;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.unit = unit;
        this.date = date;
        this.source = source;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCropName() { return cropName; }
    public void setCropName(String cropName) { this.cropName = cropName; }
    public String getMarketName() { return marketName; }
    public void setMarketName(String marketName) { this.marketName = marketName; }
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public Double getMinPrice() { return minPrice; }
    public void setMinPrice(Double minPrice) { this.minPrice = minPrice; }
    public Double getMaxPrice() { return maxPrice; }
    public void setMaxPrice(Double maxPrice) { this.maxPrice = maxPrice; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
