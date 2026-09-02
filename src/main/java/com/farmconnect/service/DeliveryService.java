package com.farmconnect.service;

import com.farmconnect.dto.DeliveryUpdateDto;
import com.farmconnect.entity.*;
import com.farmconnect.exception.ResourceNotFoundException;
import com.farmconnect.repository.DeliveryArrangementRepository;
import com.farmconnect.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeliveryService {

    private final DeliveryArrangementRepository deliveryRepository;
    private final OrderRepository orderRepository;
    private final LocationService locationService;
    private final NotificationService notificationService;

    public DeliveryService(DeliveryArrangementRepository deliveryRepository,
                           OrderRepository orderRepository,
                           LocationService locationService,
                           NotificationService notificationService) {
        this.deliveryRepository = deliveryRepository;
        this.orderRepository = orderRepository;
        this.locationService = locationService;
        this.notificationService = notificationService;
    }

    @Transactional
    public DeliveryArrangement updateDelivery(Long orderId, DeliveryUpdateDto dto, User actor) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        DeliveryArrangement delivery = deliveryRepository.findByOrder(order)
                .orElseGet(() -> {
                    DeliveryArrangement d = new DeliveryArrangement();
                    d.setOrder(order);
                    return d;
                });

        if (dto.getResponsibility() != null) {
            delivery.setResponsibility(dto.getResponsibility());
        }
        if (dto.getPickupLocation() != null && !dto.getPickupLocation().isBlank()) {
            delivery.setPickupLocation(dto.getPickupLocation());
        }
        if (dto.getDeliveryLocation() != null && !dto.getDeliveryLocation().isBlank()) {
            delivery.setDeliveryLocation(dto.getDeliveryLocation());
        }
        if (dto.getStatus() != null) {
            delivery.setStatus(dto.getStatus());
        }
        if (dto.getNotes() != null) {
            delivery.setNotes(dto.getNotes());
        }

        // Recalculate distance
        double distance = locationService.calculateDistanceBetween(
                delivery.getPickupLocation(),
                delivery.getDeliveryLocation(),
                null, null, null, null
        );
        delivery.setDistance(distance);

        DeliveryArrangement saved = deliveryRepository.save(delivery);

        // Notify other party
        User recipient = actor.getId().equals(order.getFarmer().getId()) ? order.getBuyer() : order.getFarmer();
        String link = recipient.getRole() == Role.ROLE_FARMER ? "/farmer/orders/" + order.getId() : "/buyer/orders/" + order.getId();
        notificationService.createNotification(
                recipient,
                "Transportation Update",
                "Delivery coordination for Order " + order.getOrderNumber() + " updated: Status is " + saved.getStatus().name().replace("_", " "),
                "DELIVERY",
                link
        );

        return saved;
    }
}
