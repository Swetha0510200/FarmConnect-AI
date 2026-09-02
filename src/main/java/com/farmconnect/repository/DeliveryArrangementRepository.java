package com.farmconnect.repository;

import com.farmconnect.entity.DeliveryArrangement;
import com.farmconnect.entity.DeliveryStatus;
import com.farmconnect.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryArrangementRepository extends JpaRepository<DeliveryArrangement, Long> {
    Optional<DeliveryArrangement> findByOrder(Order order);
    Optional<DeliveryArrangement> findByOrderId(Long orderId);
    List<DeliveryArrangement> findByStatus(DeliveryStatus status);
}
