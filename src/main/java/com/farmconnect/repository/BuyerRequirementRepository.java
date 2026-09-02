package com.farmconnect.repository;

import com.farmconnect.entity.BuyerRequirement;
import com.farmconnect.entity.RequirementStatus;
import com.farmconnect.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BuyerRequirementRepository extends JpaRepository<BuyerRequirement, Long> {
    List<BuyerRequirement> findByBuyerOrderByIdDesc(User buyer);
    List<BuyerRequirement> findByBuyerAndStatusOrderByIdDesc(User buyer, RequirementStatus status);
    List<BuyerRequirement> findByStatusOrderByIdDesc(RequirementStatus status);
    long countByBuyer(User buyer);
    long countByBuyerAndStatus(User buyer, RequirementStatus status);
    long countByStatus(RequirementStatus status);

    @Query("SELECT DISTINCT r.cropName FROM BuyerRequirement r WHERE r.status = 'OPEN'")
    List<String> findDistinctOpenCropNames();

    @Query("SELECT r FROM BuyerRequirement r WHERE r.status = 'OPEN' " +
           "AND LOWER(r.cropName) LIKE LOWER(CONCAT('%', :cropName, '%'))")
    List<BuyerRequirement> findOpenByCropName(@Param("cropName") String cropName);
}
