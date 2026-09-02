package com.farmconnect.repository;

import com.farmconnect.entity.CropListing;
import com.farmconnect.entity.ListingStatus;
import com.farmconnect.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CropListingRepository extends JpaRepository<CropListing, Long> {
    List<CropListing> findByFarmerOrderByIdDesc(User farmer);
    List<CropListing> findByFarmerAndStatusOrderByIdDesc(User farmer, ListingStatus status);
    List<CropListing> findByStatusOrderByIdDesc(ListingStatus status);
    long countByFarmer(User farmer);
    long countByFarmerAndStatus(User farmer, ListingStatus status);
    long countByStatus(ListingStatus status);

    @Query("SELECT DISTINCT c.cropName FROM CropListing c WHERE c.status = 'ACTIVE'")
    List<String> findDistinctActiveCropNames();

    @Query("SELECT c FROM CropListing c WHERE c.status = 'ACTIVE' " +
           "AND (:cropName IS NULL OR LOWER(c.cropName) LIKE LOWER(CONCAT('%', :cropName, '%'))) " +
           "AND (:district IS NULL OR LOWER(c.district) LIKE LOWER(CONCAT('%', :district, '%')) OR LOWER(c.location) LIKE LOWER(CONCAT('%', :district, '%'))) " +
           "AND (:minPrice IS NULL OR c.expectedPrice >= :minPrice) " +
           "AND (:maxPrice IS NULL OR c.expectedPrice <= :maxPrice) " +
           "AND (:minQuantity IS NULL OR c.quantity >= :minQuantity) " +
           "AND (:qualityGrade IS NULL OR :qualityGrade = '' OR c.qualityGrade = :qualityGrade) " +
           "ORDER BY c.createdAt DESC")
    List<CropListing> searchActiveListings(
            @Param("cropName") String cropName,
            @Param("district") String district,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("minQuantity") Double minQuantity,
            @Param("qualityGrade") String qualityGrade
    );
}
