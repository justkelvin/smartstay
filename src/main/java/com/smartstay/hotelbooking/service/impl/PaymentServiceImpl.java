package com.smartstay.hotelbooking.service.impl;

import com.smartstay.hotelbooking.model.entity.Booking;
import com.smartstay.hotelbooking.model.entity.Payment;
import com.smartstay.hotelbooking.repository.BookingRepository;
import com.smartstay.hotelbooking.repository.PaymentRepository;
import com.smartstay.hotelbooking.service.PaymentService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final Random random = new Random();

    @Autowired
    public PaymentServiceImpl(PaymentRepository paymentRepository, BookingRepository bookingRepository) {
        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    public Payment createPayment(Payment payment) {
        return paymentRepository.save(payment);
    }

    @Override
    public Optional<Payment> findById(Long id) {
        return paymentRepository.findById(id);
    }

    @Override
    public Optional<Payment> findByBooking(Booking booking) {
        return paymentRepository.findByBooking(booking);
    }

    @Override
    public Optional<Payment> findByTransactionId(String transactionId) {
        return paymentRepository.findByTransactionId(transactionId);
    }

    @Override
    public List<Payment> findByPaymentStatus(Payment.PaymentStatus status) {
        return paymentRepository.findByPaymentStatus(status);
    }

    @Override
    public List<Payment> findByStatusAndDateRange(Payment.PaymentStatus status, LocalDateTime startDate,
            LocalDateTime endDate) {
        return paymentRepository.findByStatusAndDateRange(status, startDate, endDate);
    }

    @Override
    public Payment updatePayment(Payment payment) {
        return paymentRepository.save(payment);
    }

    @Override
    @Transactional
    public Payment processPayment(Long bookingId, String paymentMethod, String cardNumber,
            String cardHolderName, String expiryMonth, String expiryYear, String cvv) {
        // Find the booking
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with id: " + bookingId));

        // Get the payment associated with the booking or create a new one if not exists
        Payment payment = paymentRepository.findByBooking(booking)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found for booking id: " + bookingId));

        // In a real application, you would integrate with a payment gateway here
        // For this example, we'll simulate a successful payment

        // Update payment details
        payment.setPaymentMethod(paymentMethod);
        payment.setPaymentStatus(Payment.PaymentStatus.COMPLETED);
        payment.setPaymentDate(LocalDateTime.now());

        // Generate a transaction ID
        payment.setTransactionId("TXN" + System.currentTimeMillis() + random.nextInt(1000));

        // Store last 4 digits of the card for reference
        if (cardNumber != null && cardNumber.length() >= 4) {
            payment.setCardLastDigits(cardNumber.substring(cardNumber.length() - 4));
        }

        return paymentRepository.save(payment);
    }

    @Override
    @Transactional
    public Payment refundPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found with id: " + paymentId));

        // Check if payment is completed
        if (payment.getPaymentStatus() != Payment.PaymentStatus.COMPLETED) {
            throw new IllegalStateException("Cannot refund a payment that is not completed");
        }

        // In a real application, you would integrate with a payment gateway to process
        // the refund
        // For this example, we'll just update the status

        payment.setPaymentStatus(Payment.PaymentStatus.REFUNDED);

        // Update the booking status if needed
        Booking booking = payment.getBooking();
        if (booking != null && booking.getBookingStatus() != Booking.BookingStatus.CANCELLED) {
            booking.setBookingStatus(Booking.BookingStatus.CANCELLED);
            bookingRepository.save(booking);
        }

        return paymentRepository.save(payment);
    }

    @Override
    public void deletePayment(Long id) {
        paymentRepository.deleteById(id);
    }
}