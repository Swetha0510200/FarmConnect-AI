package com.farmconnect.service;

import com.farmconnect.dto.AnalyticsSummaryDto;
import com.farmconnect.entity.*;
import com.farmconnect.repository.*;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnalyticsService {

    private final OrderRepository orderRepository;
    private final CropListingRepository cropListingRepository;
    private final BuyerRequirementRepository buyerRequirementRepository;
    private final UserRepository userRepository;

    public AnalyticsService(OrderRepository orderRepository,
                            CropListingRepository cropListingRepository,
                            BuyerRequirementRepository buyerRequirementRepository,
                            UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.cropListingRepository = cropListingRepository;
        this.buyerRequirementRepository = buyerRequirementRepository;
        this.userRepository = userRepository;
    }

    /**
     * Computes real sales analytics for a specific farmer from actual MySQL records.
     */
    public AnalyticsSummaryDto getFarmerAnalytics(User farmer) {
        AnalyticsSummaryDto dto = new AnalyticsSummaryDto();

        List<CropListing> listings = cropListingRepository.findByFarmerOrderByIdDesc(farmer);
        List<Order> orders = orderRepository.findByFarmerOrderByIdDesc(farmer);

        dto.setTotalListings(listings.size());
        dto.setActiveListings(listings.stream().filter(l -> l.getStatus() == ListingStatus.ACTIVE).count());
        dto.setTotalOrders(orders.size());

        long completedCount = 0;
        double totalSalesRevenue = 0;
        double totalQuantitySold = 0;
        double pendingRevenue = 0;
        Map<String, Double> cropSalesMap = new HashMap<>();
        Map<String, Long> statusMap = new HashMap<>();

        for (Order o : orders) {
            String status = o.getStatus().name();
            statusMap.put(status, statusMap.getOrDefault(status, 0L) + 1);

            if (o.getStatus() == OrderStatus.COMPLETED) {
                completedCount++;
                totalSalesRevenue += o.getTotalAmount();
                totalQuantitySold += o.getQuantity();

                cropSalesMap.put(o.getCropName(), cropSalesMap.getOrDefault(o.getCropName(), 0.0) + o.getTotalAmount());
            } else if (o.getStatus() != OrderStatus.CANCELLED && o.getStatus() != OrderStatus.REJECTED) {
                pendingRevenue += o.getTotalAmount();
            }
        }

        dto.setCompletedOrders(completedCount);
        dto.setTotalSalesRevenue(Math.round(totalSalesRevenue * 100.0) / 100.0);
        dto.setPendingRevenue(Math.round(pendingRevenue * 100.0) / 100.0);
        dto.setTotalQuantitySold(Math.round(totalQuantitySold * 10.0) / 10.0);
        dto.setCropSalesDistribution(cropSalesMap);
        dto.setOrderStatusDistribution(statusMap);

        return dto;
    }

    /**
     * Computes platform-wide real metrics for Administrator dashboard.
     */
    public Map<String, Object> getPlatformStats() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalFarmers", userRepository.countByRole(Role.ROLE_FARMER));
        stats.put("totalBuyers", userRepository.countByRole(Role.ROLE_BUYER));
        stats.put("activeListings", cropListingRepository.countByStatus(ListingStatus.ACTIVE));
        stats.put("openRequirements", buyerRequirementRepository.countByStatus(RequirementStatus.OPEN));
        stats.put("totalOrders", orderRepository.count());
        stats.put("completedOrders", orderRepository.countByStatus(OrderStatus.COMPLETED));

        Double totalRevenue = orderRepository.sumTotalPlatformRevenue();
        stats.put("totalTradeVolume", totalRevenue != null ? Math.round(totalRevenue * 100.0) / 100.0 : 0.0);

        return stats;
    }
}
