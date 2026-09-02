package com.farmconnect.repository;

import com.farmconnect.entity.MarketPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MarketPriceRepository extends JpaRepository<MarketPrice, Long> {
    List<MarketPrice> findByCropNameIgnoreCaseOrderByDateDesc(String cropName);
    List<MarketPrice> findByDistrictIgnoreCaseOrderByDateDesc(String district);
    List<MarketPrice> findAllByOrderByDateDescCreatedAtDesc();

    @Query("SELECT m FROM MarketPrice m WHERE " +
           "(:cropName IS NULL OR LOWER(m.cropName) LIKE LOWER(CONCAT('%', :cropName, '%'))) AND " +
           "(:district IS NULL OR LOWER(m.district) LIKE LOWER(CONCAT('%', :district, '%'))) AND " +
           "(:state IS NULL OR LOWER(m.state) LIKE LOWER(CONCAT('%', :state, '%'))) " +
           "ORDER BY m.date DESC, m.id DESC")
    List<MarketPrice> searchMarketPrices(
            @Param("cropName") String cropName,
            @Param("district") String district,
            @Param("state") String state
    );

    Optional<MarketPrice> findFirstByCropNameIgnoreCaseOrderByDateDesc(String cropName);
}
