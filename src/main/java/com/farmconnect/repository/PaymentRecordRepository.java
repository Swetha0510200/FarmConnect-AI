package com.farmconnect.repository;

import com.farmconnect.entity.Order;
import com.farmconnect.entity.PaymentRecord;
import com.farmconnect.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRecordRepository extends JpaRepository<PaymentRecord, Long> {
    Optional<PaymentRecord> findByOrder(Order order);
    Optional<PaymentRecord> findByOrderId(Long orderId);
    List<PaymentRecord> findByPaymentStatus(PaymentStatus status);
}
