package com.smartstay.hotelbooking.controller;

import com.smartstay.hotelbooking.model.entity.Booking;
import com.smartstay.hotelbooking.model.entity.Room;
import com.smartstay.hotelbooking.model.entity.User;
import com.smartstay.hotelbooking.service.BookingService;
import com.smartstay.hotelbooking.service.RoomService;
import com.smartstay.hotelbooking.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final UserService userService;
    private final RoomService roomService;

    @Autowired
    public BookingController(BookingService bookingService, UserService userService, RoomService roomService) {
        this.bookingService = bookingService;
        this.userService = userService;
        this.roomService = roomService;
    }

    @GetMapping
    public ResponseEntity<?> getCurrentUserBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "checkInDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String status) {

        // Get current user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        User currentUser = userService.findByUsername(currentUsername)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        // Get user's bookings
        Page<Booking> bookings = bookingService.findByUser(currentUser, pageable);

        // Filter by status if provided
        if (status != null && !status.isEmpty()) {
            try {
                Booking.BookingStatus bookingStatus = Booking.BookingStatus.valueOf(status.toUpperCase());
                List<Booking> filteredBookings = bookingService.findByUserIdAndStatus(currentUser.getId(),
                        bookingStatus);

                return ResponseEntity.ok(mapBookingsToResponse(filteredBookings));
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid booking status: " + status));
            }
        }

        return ResponseEntity.ok(mapBookingsToResponse(bookings.getContent()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getBookingById(@PathVariable Long id) {
        Booking booking = bookingService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with id: " + id));

        // Check if the current user is authorized to view this booking
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        // Allow admin role or the user who made the booking
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !booking.getUser().getUsername().equals(currentUsername)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "You are not authorized to view this booking"));
        }

        return ResponseEntity.ok(mapBookingToResponse(booking));
    }

    @PostMapping
    public ResponseEntity<?> createBooking(@RequestBody Map<String, Object> bookingRequest, Principal principal) {
        try {
            // Get current user
            User currentUser = userService.findByUsername(principal.getName())
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));

            // Parse booking details
            Long roomId = Long.parseLong(bookingRequest.get("roomId").toString());
            LocalDate checkInDate = LocalDate.parse(bookingRequest.get("checkInDate").toString());
            LocalDate checkOutDate = LocalDate.parse(bookingRequest.get("checkOutDate").toString());
            int adults = Integer.parseInt(bookingRequest.get("adults").toString());
            int children = bookingRequest.containsKey("children")
                    ? Integer.parseInt(bookingRequest.get("children").toString())
                    : 0;
            String specialRequests = bookingRequest.containsKey("specialRequests")
                    ? bookingRequest.get("specialRequests").toString()
                    : "";

            // Create booking
            Booking booking = bookingService.processBookingRequest(
                    currentUser.getId(), roomId, checkInDate, checkOutDate, adults, children, specialRequests);

            return new ResponseEntity<>(mapBookingToResponse(booking), HttpStatus.CREATED);
        } catch (EntityNotFoundException | IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelBooking(@PathVariable Long id, Principal principal) {
        try {
            Booking booking = bookingService.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Booking not found with id: " + id));

            // Check if the current user is authorized to cancel this booking
            User currentUser = userService.findByUsername(principal.getName())
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));

            boolean isAdmin = SecurityContextHolder.getContext().getAuthentication()
                    .getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            if (!isAdmin && !booking.getUser().getId().equals(currentUser.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "You are not authorized to cancel this booking"));
            }

            // Cancel the booking
            Booking cancelledBooking = bookingService.cancelBooking(id);

            return ResponseEntity.ok(mapBookingToResponse(cancelledBooking));
        } catch (EntityNotFoundException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/reference/{reference}")
    public ResponseEntity<?> getBookingByReference(@PathVariable String reference) {
        Booking booking = bookingService.findByBookingReference(reference)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with reference: " + reference));

        // Security check
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !booking.getUser().getUsername().equals(currentUsername)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "You are not authorized to view this booking"));
        }

        return ResponseEntity.ok(mapBookingToResponse(booking));
    }

    @GetMapping("/hotel/{hotelId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getBookingsByHotel(
            @PathVariable Long hotelId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<Booking> bookings = bookingService.findByHotelAndDateRange(hotelId, startDate, endDate);

        return ResponseEntity.ok(mapBookingsToResponse(bookings));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateBookingStatus(@PathVariable Long id, @RequestBody Map<String, String> statusUpdate) {
        if (!statusUpdate.containsKey("status")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Status field is required"));
        }

        String statusStr = statusUpdate.get("status");
        try {
            Booking.BookingStatus status = Booking.BookingStatus.valueOf(statusStr.toUpperCase());
            Booking updatedBooking = bookingService.updateBookingStatus(id, status);

            return ResponseEntity.ok(mapBookingToResponse(updatedBooking));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid booking status: " + statusStr));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Helper methods
    private List<Map<String, Object>> mapBookingsToResponse(List<Booking> bookings) {
        List<Map<String, Object>> response = new ArrayList<>();
        for (Booking booking : bookings) {
            response.add(mapBookingToResponse(booking));
        }
        return response;
    }

    private Map<String, Object> mapBookingToResponse(Booking booking) {
        Map<String, Object> bookingMap = new HashMap<>();
        bookingMap.put("id", booking.getId());
        bookingMap.put("bookingReference", booking.getBookingReference());
        bookingMap.put("checkInDate", booking.getCheckInDate());
        bookingMap.put("checkOutDate", booking.getCheckOutDate());
        bookingMap.put("adults", booking.getAdults());
        bookingMap.put("children", booking.getChildren());
        bookingMap.put("totalPrice", booking.getTotalPrice());
        bookingMap.put("status", booking.getBookingStatus().name());
        bookingMap.put("specialRequests", booking.getSpecialRequests());
        bookingMap.put("createdAt", booking.getCreatedAt());

        // Include room details
        Room room = booking.getRoom();
        Map<String, Object> roomMap = new HashMap<>();
        roomMap.put("id", room.getId());
        roomMap.put("roomNumber", room.getRoomNumber());
        roomMap.put("roomType", room.getRoomType().getName());
        roomMap.put("pricePerNight", room.getPricePerNight());

        // Include hotel details
        Map<String, Object> hotelMap = new HashMap<>();
        hotelMap.put("id", room.getHotel().getId());
        hotelMap.put("name", room.getHotel().getName());
        hotelMap.put("address", room.getHotel().getAddress());
        hotelMap.put("city", room.getHotel().getCity());
        hotelMap.put("country", room.getHotel().getCountry());

        roomMap.put("hotel", hotelMap);
        bookingMap.put("room", roomMap);

        // Include payment details
        if (booking.getPayment() != null) {
            Map<String, Object> paymentMap = new HashMap<>();
            paymentMap.put("id", booking.getPayment().getId());
            paymentMap.put("amount", booking.getPayment().getAmount());
            paymentMap.put("paymentMethod", booking.getPayment().getPaymentMethod());
            paymentMap.put("status", booking.getPayment().getPaymentStatus().name());
            paymentMap.put("paymentDate", booking.getPayment().getPaymentDate());

            bookingMap.put("payment", paymentMap);
        }

        // Include review if available
        if (booking.getReview() != null) {
            Map<String, Object> reviewMap = new HashMap<>();
            reviewMap.put("id", booking.getReview().getId());
            reviewMap.put("rating", booking.getReview().getRating());
            reviewMap.put("comment", booking.getReview().getComment());
            reviewMap.put("datePosted", booking.getReview().getDatePosted());

            bookingMap.put("review", reviewMap);
        }

        return bookingMap;
    }
}