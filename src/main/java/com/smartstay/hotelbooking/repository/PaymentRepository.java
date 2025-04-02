package com.smartstay.hotelbooking.repository;

import com.smartstay.hotelbooking.model.entity.Booking;
import com.smartstay.hotelbooking.model.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByBooking(Booking booking);

    Optional<Payment> findByTransactionId(String transactionId);

    List<Payment> findByPaymentStatus(Payment.PaymentStatus status);

    @Query("SELECT p FROM Payment p WHERE p.paymentStatus = :status AND p.paymentDate BETWEEN :startDate AND :endDate")
    List<Payment> findByStatusAndDateRange(@Param("status") Payment.PaymentStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}