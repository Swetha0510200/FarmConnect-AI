package com.farmconnect.repository;

import com.farmconnect.entity.BuyerProfile;
import com.farmconnect.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BuyerProfileRepository extends JpaRepository<BuyerProfile, Long> {
    Optional<BuyerProfile> findByUser(User user);
    Optional<BuyerProfile> findByUserId(Long userId);
    List<BuyerProfile> findByVerificationStatus(boolean status);
    long countByVerificationStatus(boolean status);
}
