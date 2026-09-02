package com.farmconnect.repository;

import com.farmconnect.entity.CropListing;
import com.farmconnect.entity.SavedListing;
import com.farmconnect.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavedListingRepository extends JpaRepository<SavedListing, Long> {
    List<SavedListing> findByUserOrderByCreatedAtDesc(User user);
    Optional<SavedListing> findByUserAndCropListing(User user, CropListing cropListing);
    boolean existsByUserAndCropListing(User user, CropListing cropListing);
    void deleteByUserAndCropListing(User user, CropListing cropListing);
}
