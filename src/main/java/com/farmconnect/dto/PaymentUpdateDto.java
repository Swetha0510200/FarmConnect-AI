package com.farmconnect.dto;

import com.farmconnect.entity.PaymentMethod;
import com.farmconnect.entity.PaymentStatus;

public class PaymentUpdateDto {
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private String notes;

    public PaymentUpdateDto() {}

    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
