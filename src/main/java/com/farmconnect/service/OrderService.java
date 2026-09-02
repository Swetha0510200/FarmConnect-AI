package com.farmconnect.service;

import com.farmconnect.dto.OrderPlacementDto;
import com.farmconnect.entity.*;
import com.farmconnect.exception.ResourceNotFoundException;
import com.farmconnect.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CropListingRepository cropListingRepository;
    private final PaymentRecordRepository paymentRecordRepository;
    private final DeliveryArrangementRepository deliveryArrangementRepository;
    private final NotificationService notificationService;
    private final LocationService locationService;

    public OrderService(OrderRepository orderRepository,
                        CropListingRepository cropListingRepository,
                        PaymentRecordRepository paymentRecordRepository,
                        DeliveryArrangementRepository deliveryArrangementRepository,
                        NotificationService notificationService,
                        LocationService locationService) {
        this.orderRepository = orderRepository;
        this.cropListingRepository = cropListingRepository;
        this.paymentRecordRepository = paymentRecordRepository;
        this.deliveryArrangementRepository = deliveryArrangementRepository;
        this.notificationService = notificationService;
        this.locationService = locationService;
    }

    @Transactional
    public Order placeOrder(User buyer, OrderPlacementDto dto) {
        CropListing listing = cropListingRepository.findById(dto.getCropListingId())
                .orElseThrow(() -> new ResourceNotFoundException("Crop listing not found"));

        if (listing.getStatus() != ListingStatus.ACTIVE) {
            throw new IllegalStateException("This crop listing is currently not active for orders.");
        }

        if (dto.getQuantity() > listing.getQuantity()) {
            throw new IllegalArgumentException("Requested quantity exceeds available quantity (" + listing.getQuantity() + " " + listing.getUnit() + ").");
        }

        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setBuyer(buyer);
        order.setFarmer(listing.getFarmer());
        order.setCropListing(listing);
        order.setCropName(listing.getCropName());
        order.setQuantity(dto.getQuantity());
        order.setUnit(listing.getUnit());
        order.setAgreedPrice(dto.getAgreedPrice());
        order.setTotalAmount(Math.round(dto.getQuantity() * dto.getAgreedPrice() * 100.0) / 100.0);
        order.setStatus(OrderStatus.PLACED);
        order.setNotes(dto.getNotes());

        Order savedOrder = orderRepository.save(order);

        // Initialize default Payment Record
        PaymentRecord paymentRecord = new PaymentRecord();
        paymentRecord.setOrder(savedOrder);
        paymentRecord.setPaymentMethod(PaymentMethod.CASH);
        paymentRecord.setPaymentStatus(PaymentStatus.PENDING);
        paymentRecordRepository.save(paymentRecord);

        // Initialize default Delivery Arrangement
        DeliveryArrangement delivery = new DeliveryArrangement();
        delivery.setOrder(savedOrder);
        delivery.setResponsibility(DeliveryResponsibility.BUYER);
        delivery.setPickupLocation(listing.getLocation());
        delivery.setDeliveryLocation(buyer.getBuyerProfile() != null ? buyer.getBuyerProfile().getFullLocation() : "Buyer Address");
        delivery.setStatus(DeliveryStatus.NOT_ARRANGED);

        double dist = locationService.calculateDistanceBetween(
                listing.getLocation(),
                delivery.getDeliveryLocation(),
                listing.getLatitude(),
                listing.getLongitude(),
                buyer.getBuyerProfile() != null ? buyer.getBuyerProfile().getLatitude() : null,
                buyer.getBuyerProfile() != null ? buyer.getBuyerProfile().getLongitude() : null
        );
        delivery.setDistance(dist);
        deliveryArrangementRepository.save(delivery);

        // Notifications
        notificationService.createNotification(
                listing.getFarmer(),
                "New Order Received!",
                buyer.getName() + " placed an order for " + (int)(double)dto.getQuantity() + " " + listing.getUnit() + " of " + listing.getCropName() + " (Total: ?" + savedOrder.getTotalAmount() + ").",
                "ORDER",
                "/farmer/orders/" + savedOrder.getId()
        );

        notificationService.createNotification(
                buyer,
                "Order Placed Successfully",
                "Your order " + savedOrder.getOrderNumber() + " has been sent to the farmer.",
                "ORDER",
                "/buyer/orders/" + savedOrder.getId()
        );

        return savedOrder;
    }

    private String generateOrderNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int rand = 1000 + new Random().nextInt(9000);
        return "FC-" + timestamp + "-" + rand;
    }

    @Transactional
    public void updateOrderStatus(Long orderId, OrderStatus newStatus, User actor) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID " + orderId));

        order.setStatus(newStatus);
        orderRepository.save(order);

        // If order completed, adjust or complete the crop listing quantity
        if (newStatus == OrderStatus.COMPLETED) {
            CropListing listing = order.getCropListing();
            if (listing != null) {
                double remaining = listing.getQuantity() - order.getQuantity();
                if (remaining <= 0) {
                    listing.setQuantity(0.0);
                    listing.setStatus(ListingStatus.COMPLETED);
                } else {
                    listing.setQuantity(remaining);
                }
                cropListingRepository.save(listing);
            }
        }

        // Notify both parties
        String msg = "Order " + order.getOrderNumber() + " status updated to: " + newStatus.name().replace("_", " ");
        notificationService.createNotification(order.getFarmer(), "Order Update", msg, "ORDER", "/farmer/orders/" + order.getId());
        notificationService.createNotification(order.getBuyer(), "Order Update", msg, "ORDER", "/buyer/orders/" + order.getId());
    }

    public List<Order> getFarmerOrders(User farmer) {
        return orderRepository.findByFarmerOrderByIdDesc(farmer);
    }

    public List<Order> getBuyerOrders(User buyer) {
        return orderRepository.findByBuyerOrderByIdDesc(buyer);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByIdDesc();
    }

    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }

    public long countFarmerOrders(User farmer) {
        return orderRepository.countByFarmer(farmer);
    }

    public long countBuyerOrders(User buyer) {
        return orderRepository.countByBuyer(buyer);
    }

    public long countAllOrders() {
        return orderRepository.count();
    }
}
