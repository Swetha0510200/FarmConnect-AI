package com.farmconnect.service;

import com.farmconnect.dto.PaymentUpdateDto;
import com.farmconnect.entity.*;
import com.farmconnect.exception.ResourceNotFoundException;
import com.farmconnect.repository.OrderRepository;
import com.farmconnect.repository.PaymentRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class PaymentService {

    private final PaymentRecordRepository paymentRecordRepository;
    private final OrderRepository orderRepository;
    private final NotificationService notificationService;

    public PaymentService(PaymentRecordRepository paymentRecordRepository,
                          OrderRepository orderRepository,
                          NotificationService notificationService) {
        this.paymentRecordRepository = paymentRecordRepository;
        this.orderRepository = orderRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public PaymentRecord updatePaymentDetails(Long orderId, PaymentUpdateDto dto, User actor) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        PaymentRecord record = paymentRecordRepository.findByOrder(order)
                .orElseGet(() -> {
                    PaymentRecord p = new PaymentRecord();
                    p.setOrder(order);
                    return p;
                });

        if (dto.getPaymentMethod() != null) {
            record.setPaymentMethod(dto.getPaymentMethod());
        }
        if (dto.getPaymentStatus() != null) {
            record.setPaymentStatus(dto.getPaymentStatus());
            if (dto.getPaymentStatus() == PaymentStatus.RECEIVED && record.getReceivedDate() == null) {
                record.setReceivedDate(LocalDateTime.now());
            }
        }
        if (dto.getNotes() != null) {
            record.setNotes(dto.getNotes());
        }

        PaymentRecord saved = paymentRecordRepository.save(record);

        notificationService.createNotification(
                order.getBuyer(),
                "Payment Record Updated",
                "Payment status for Order " + order.getOrderNumber() + " is now: " + saved.getPaymentStatus() + " via " + saved.getPaymentMethod(),
                "PAYMENT",
                "/buyer/orders/" + order.getId()
        );

        return saved;
    }

    @Transactional
    public void markPaymentReceived(Long orderId, User farmer) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!order.getFarmer().getId().equals(farmer.getId())) {
            throw new IllegalArgumentException("Only the farmer can mark payment as received.");
        }

        PaymentRecord record = paymentRecordRepository.findByOrder(order)
                .orElseGet(() -> {
                    PaymentRecord p = new PaymentRecord();
                    p.setOrder(order);
                    return p;
                });

        record.setPaymentStatus(PaymentStatus.RECEIVED);
        record.setReceivedDate(LocalDateTime.now());
        paymentRecordRepository.save(record);

        notificationService.createNotification(
                order.getBuyer(),
                "Payment Confirmed by Farmer",
                "The farmer has marked payment as RECEIVED for Order " + order.getOrderNumber() + ".",
                "PAYMENT",
                "/buyer/orders/" + order.getId()
        );
    }
}
