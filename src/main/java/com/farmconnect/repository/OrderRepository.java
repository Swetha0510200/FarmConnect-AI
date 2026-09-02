package com.farmconnect.repository;

import com.farmconnect.entity.Order;
import com.farmconnect.entity.OrderStatus;
import com.farmconnect.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNumber(String orderNumber);
    List<Order> findByFarmerOrderByIdDesc(User farmer);
    List<Order> findByBuyerOrderByIdDesc(User buyer);
    List<Order> findByStatusOrderByIdDesc(OrderStatus status);
    List<Order> findAllByOrderByIdDesc();
    long countByFarmer(User farmer);
    long countByFarmerAndStatus(User farmer, OrderStatus status);
    long countByBuyer(User buyer);
    long countByStatus(OrderStatus status);

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.farmer = :farmer AND o.status = 'COMPLETED'")
    Double sumCompletedRevenueByFarmer(@Param("farmer") User farmer);

    @Query("SELECT SUM(o.quantity) FROM Order o WHERE o.farmer = :farmer AND o.status = 'COMPLETED'")
    Double sumCompletedQuantityByFarmer(@Param("farmer") User farmer);

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status = 'COMPLETED'")
    Double sumTotalPlatformRevenue();
}
