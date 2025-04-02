package com.smartstay.hotelbooking.controller;

import com.smartstay.hotelbooking.model.entity.Booking;
import com.smartstay.hotelbooking.model.entity.Payment;
import com.smartstay.hotelbooking.model.entity.User;
import com.smartstay.hotelbooking.service.BookingService;
import com.smartstay.hotelbooking.service.PaymentService;
import com.smartstay.hotelbooking.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final BookingService bookingService;
    private final UserService userService;

    @Autowired
    public PaymentController(PaymentService paymentService, BookingService bookingService, UserService userService) {
        this.paymentService = paymentService;
        this.bookingService = bookingService;
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPaymentById(@PathVariable Long id) {
        Payment payment = paymentService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found with id: " + id));

        // Security check
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        // Only allow admin or the booking owner to view the payment details
        if (!isAdmin && !payment.getBooking().getUser().getUsername().equals(currentUsername)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "You are not authorized to view this payment"));
        }

        return ResponseEntity.ok(mapPaymentToResponse(payment));
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<?> getPaymentByBookingId(@PathVariable Long bookingId) {
        Booking booking = bookingService.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with id: " + bookingId));

        // Security check
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        // Only allow admin or the booking owner to view the payment details
        if (!isAdmin && !booking.getUser().getUsername().equals(currentUsername)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "You are not authorized to view this payment"));
        }

        Payment payment = paymentService.findByBooking(booking)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found for booking id: " + bookingId));

        return ResponseEntity.ok(mapPaymentToResponse(payment));
    }

    @PostMapping("/process")
    public ResponseEntity<?> processPayment(@RequestBody Map<String, Object> paymentRequest) {
        try {
            // Extract booking ID
            Long bookingId = Long.parseLong(paymentRequest.get("bookingId").toString());

            // Security check - Only allow payment owner or admin
            Booking booking = bookingService.findById(bookingId)
                    .orElseThrow(() -> new EntityNotFoundException("Booking not found with id: " + bookingId));

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication.getName();
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            if (!isAdmin && !booking.getUser().getUsername().equals(currentUsername)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "You are not authorized to process payment for this booking"));
            }

            // Extract payment details
            String paymentMethod = paymentRequest.get("paymentMethod").toString();
            String cardNumber = paymentRequest.containsKey("cardNumber") ? paymentRequest.get("cardNumber").toString()
                    : null;
            String cardHolderName = paymentRequest.containsKey("cardHolderName")
                    ? paymentRequest.get("cardHolderName").toString()
                    : null;
            String expiryMonth = paymentRequest.containsKey("expiryMonth")
                    ? paymentRequest.get("expiryMonth").toString()
                    : null;
            String expiryYear = paymentRequest.containsKey("expiryYear") ? paymentRequest.get("expiryYear").toString()
                    : null;
            String cvv = paymentRequest.containsKey("cvv") ? paymentRequest.get("cvv").toString() : null;

            // Process payment
            Payment payment = paymentService.processPayment(
                    bookingId, paymentMethod, cardNumber, cardHolderName, expiryMonth, expiryYear, cvv);

            return ResponseEntity.ok(mapPaymentToResponse(payment));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid booking ID format"));
        } catch (EntityNotFoundException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/refund")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> refundPayment(@PathVariable Long id) {
        try {
            Payment payment = paymentService.refundPayment(id);
            return ResponseEntity.ok(mapPaymentToResponse(payment));
        } catch (EntityNotFoundException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/admin/report")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getPaymentReport(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        // Default to searching for completed payments in the last 30 days
        Payment.PaymentStatus paymentStatus = status != null ? Payment.PaymentStatus.valueOf(status.toUpperCase())
                : Payment.PaymentStatus.COMPLETED;

        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        LocalDateTime start = startDate != null ? LocalDateTime.parse(startDate, formatter)
                : LocalDateTime.now().minusDays(30);

        LocalDateTime end = endDate != null ? LocalDateTime.parse(endDate, formatter) : LocalDateTime.now();

        List<Payment> payments = paymentService.findByStatusAndDateRange(paymentStatus, start, end);

        // Transform to response format
        List<Map<String, Object>> paymentList = payments.stream()
                .map(this::mapPaymentToResponse)
                .collect(Collectors.toList());

        // Calculate totals
        double totalAmount = payments.stream()
                .mapToDouble(p -> p.getAmount().doubleValue())
                .sum();

        Map<String, Object> response = new HashMap<>();
        response.put("startDate", start);
        response.put("endDate", end);
        response.put("status", paymentStatus.name());
        response.put("totalPayments", payments.size());
        response.put("totalAmount", totalAmount);
        response.put("payments", paymentList);

        return ResponseEntity.ok(response);
    }

    // Helper method
    private Map<String, Object> mapPaymentToResponse(Payment payment) {
        Map<String, Object> paymentMap = new HashMap<>();
        paymentMap.put("id", payment.getId());
        paymentMap.put("amount", payment.getAmount());
        paymentMap.put("paymentMethod", payment.getPaymentMethod());
        paymentMap.put("transactionId", payment.getTransactionId());
        paymentMap.put("status", payment.getPaymentStatus().name());
        paymentMap.put("paymentDate", payment.getPaymentDate());
        paymentMap.put("cardLastDigits", payment.getCardLastDigits());
        paymentMap.put("createdAt", payment.getCreatedAt());

        // Include booking details
        Booking booking = payment.getBooking();
        Map<String, Object> bookingMap = new HashMap<>();
        bookingMap.put("id", booking.getId());
        bookingMap.put("bookingReference", booking.getBookingReference());
        bookingMap.put("status", booking.getBookingStatus().name());
        bookingMap.put("checkInDate", booking.getCheckInDate());
        bookingMap.put("checkOutDate", booking.getCheckOutDate());

        // Include user details
        User user = booking.getUser();
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", user.getId());
        userMap.put("fullName", user.getFirstName() + " " + user.getLastName());
        userMap.put("email", user.getEmail());

        bookingMap.put("user", userMap);
        paymentMap.put("booking", bookingMap);

        return paymentMap;
    }
}