package com.farmconnect.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "farmer_profiles")
public class FarmerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    private String farmName;
    private Double farmSize;
    @Column(nullable = false)
    private String village;
    @Column(nullable = false)
    private String district;
    @Column(nullable = false)
    private String state;
    private Double latitude;
    private Double longitude;
    private String farmingType;

    public FarmerProfile() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getFarmName() { return farmName; }
    public void setFarmName(String farmName) { this.farmName = farmName; }
    public Double getFarmSize() { return farmSize; }
    public void setFarmSize(Double farmSize) { this.farmSize = farmSize; }
    public String getVillage() { return village; }
    public void setVillage(String village) { this.village = village; }
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public String getFarmingType() { return farmingType; }
    public void setFarmingType(String farmingType) { this.farmingType = farmingType; }

    public String getFullLocation() {
        StringBuilder sb = new StringBuilder();
        if (village != null && !village.isBlank()) sb.append(village).append(", ");
        if (district != null && !district.isBlank()) sb.append(district).append(", ");
        if (state != null && !state.isBlank()) sb.append(state);
        return sb.toString();
    }
}
