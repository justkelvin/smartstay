package com.smartstay.hotelbooking.service;

import com.smartstay.hotelbooking.model.entity.Booking;
import com.smartstay.hotelbooking.model.entity.Payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentService {
    Payment createPayment(Payment payment);

    Optional<Payment> findById(Long id);

    Optional<Payment> findByBooking(Booking booking);

    Optional<Payment> findByTransactionId(String transactionId);

    List<Payment> findByPaymentStatus(Payment.PaymentStatus status);

    List<Payment> findByStatusAndDateRange(Payment.PaymentStatus status, LocalDateTime startDate,
            LocalDateTime endDate);

    Payment updatePayment(Payment payment);

    Payment processPayment(Long bookingId, String paymentMethod, String cardNumber,
            String cardHolderName, String expiryMonth, String expiryYear, String cvv);

    Payment refundPayment(Long paymentId);

    void deletePayment(Long id);
}