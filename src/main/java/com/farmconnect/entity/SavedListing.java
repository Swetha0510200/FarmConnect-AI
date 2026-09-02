package com.farmconnect.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "saved_listings")
public class SavedListing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crop_listing_id", nullable = false)
    private CropListing cropListing;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public SavedListing() {}

    public SavedListing(User user, CropListing cropListing) {
        this.user = user;
        this.cropListing = cropListing;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public CropListing getCropListing() { return cropListing; }
    public void setCropListing(CropListing cropListing) { this.cropListing = cropListing; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
