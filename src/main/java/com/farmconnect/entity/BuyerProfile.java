package com.farmconnect.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "buyer_profiles")
public class BuyerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private String businessName;

    @Column(nullable = false)
    private String businessType;

    private String address;

    @Column(nullable = false)
    private String district;

    @Column(nullable = false)
    private String state;

    private Double latitude;
    private Double longitude;

    @Column(nullable = false)
    private boolean verificationStatus = false;

    public BuyerProfile() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }
    public String getBusinessType() { return businessType; }
    public void setBusinessType(String businessType) { this.businessType = businessType; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public boolean isVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(boolean verificationStatus) { this.verificationStatus = verificationStatus; }

    public String getFullLocation() {
        StringBuilder sb = new StringBuilder();
        if (address != null && !address.isBlank()) sb.append(address).append(", ");
        if (district != null && !district.isBlank()) sb.append(district).append(", ");
        if (state != null && !state.isBlank()) sb.append(state);
        return sb.toString();
    }
}
