package com.farmconnect.dto;

public class UserProfileDto {
    private String name;
    private String email;
    private String mobile;
    private String farmOrBusinessName;
    private String farmSizeOrBusinessType;
    private String addressOrVillage;
    private String district;
    private String state;
    private String farmingType;

    public UserProfileDto() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }
    public String getFarmOrBusinessName() { return farmOrBusinessName; }
    public void setFarmOrBusinessName(String farmOrBusinessName) { this.farmOrBusinessName = farmOrBusinessName; }
    public String getFarmSizeOrBusinessType() { return farmSizeOrBusinessType; }
    public void setFarmSizeOrBusinessType(String farmSizeOrBusinessType) { this.farmSizeOrBusinessType = farmSizeOrBusinessType; }
    public String getAddressOrVillage() { return addressOrVillage; }
    public void setAddressOrVillage(String addressOrVillage) { this.addressOrVillage = addressOrVillage; }
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    public String getState() { return state; }
    public void setState(String state) { this.state = state; }
    public String getFarmingType() { return farmingType; }
    public void setFarmingType(String farmingType) { this.farmingType = farmingType; }
}
