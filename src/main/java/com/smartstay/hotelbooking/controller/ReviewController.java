package com.smartstay.hotelbooking.controller;

import com.smartstay.hotelbooking.model.entity.Booking;
import com.smartstay.hotelbooking.model.entity.Hotel;
import com.smartstay.hotelbooking.model.entity.Review;
import com.smartstay.hotelbooking.model.entity.User;
import com.smartstay.hotelbooking.service.BookingService;
import com.smartstay.hotelbooking.service.HotelService;
import com.smartstay.hotelbooking.service.ReviewService;
import com.smartstay.hotelbooking.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    private final BookingService bookingService;
    private final UserService userService;
    private final HotelService hotelService;

    @Autowired
    public ReviewController(ReviewService reviewService, BookingService bookingService,
            UserService userService, HotelService hotelService) {
        this.reviewService = reviewService;
        this.bookingService = bookingService;
        this.userService = userService;
        this.hotelService = hotelService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getReviewById(@PathVariable Long id) {
        Review review = reviewService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Review not found with id: " + id));

        return ResponseEntity.ok(mapReviewToResponse(review));
    }

    @GetMapping("/user")
    public ResponseEntity<?> getCurrentUserReviews() {
        // Get current user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        User currentUser = userService.findByUsername(currentUsername)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        List<Review> reviews = reviewService.findByUser(currentUser);

        // Transform to response format
        List<Map<String, Object>> reviewList = reviews.stream()
                .map(this::mapReviewToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(reviewList);
    }

    @PostMapping("/booking/{bookingId}")
    public ResponseEntity<?> submitReview(
            @PathVariable Long bookingId,
            @RequestBody Map<String, Object> reviewRequest,
            Principal principal) {

        try {
            // Validate request
            if (!reviewRequest.containsKey("rating") || !reviewRequest.containsKey("comment")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Rating and comment are required"));
            }

            Integer rating = Integer.parseInt(reviewRequest.get("rating").toString());
            String comment = reviewRequest.get("comment").toString();

            if (rating < 1 || rating > 5) {
                return ResponseEntity.badRequest().body(Map.of("error", "Rating must be between 1 and 5"));
            }

            // Verify current user owns the booking
            Booking booking = bookingService.findById(bookingId)
                    .orElseThrow(() -> new EntityNotFoundException("Booking not found with id: " + bookingId));

            User currentUser = userService.findByUsername(principal.getName())
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));

            if (!booking.getUser().getId().equals(currentUser.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "You can only submit reviews for your own bookings"));
            }

            // Submit review
            Review review = reviewService.submitReview(bookingId, rating, comment);

            return ResponseEntity.ok(mapReviewToResponse(review));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid rating format"));
        } catch (EntityNotFoundException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateReview(
            @PathVariable Long id,
            @RequestBody Map<String, Object> reviewUpdate,
            Principal principal) {

        try {
            Review review = reviewService.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Review not found with id: " + id));

            // Security check - only owner can update review
            User currentUser = userService.findByUsername(principal.getName())
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));

            if (!review.getUser().getId().equals(currentUser.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "You can only update your own reviews"));
            }

            // Update review
            if (reviewUpdate.containsKey("rating")) {
                Integer rating = Integer.parseInt(reviewUpdate.get("rating").toString());
                if (rating < 1 || rating > 5) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Rating must be between 1 and 5"));
                }
                review.setRating(rating);
            }

            if (reviewUpdate.containsKey("comment")) {
                review.setComment(reviewUpdate.get("comment").toString());
            }

            // Save updated review
            Review updatedReview = reviewService.updateReview(review);

            return ResponseEntity.ok(mapReviewToResponse(updatedReview));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid rating format"));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteReview(@PathVariable Long id) {
        reviewService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Review not found with id: " + id));

        reviewService.deleteReview(id);

        return ResponseEntity.ok(Map.of("message", "Review deleted successfully"));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> approveReview(@PathVariable Long id) {
        try {
            Review review = reviewService.approveReview(id);
            return ResponseEntity.ok(mapReviewToResponse(review));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/respond")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> addManagementResponse(
            @PathVariable Long id,
            @RequestBody Map<String, String> response) {

        if (!response.containsKey("response")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Response field is required"));
        }

        try {
            Review review = reviewService.addManagementResponse(id, response.get("response"));
            return ResponseEntity.ok(mapReviewToResponse(review));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getPendingReviews() {
        List<Review> pendingReviews = reviewService.findByIsApproved(false);

        // Transform to response format
        List<Map<String, Object>> reviewList = pendingReviews.stream()
                .map(this::mapReviewToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(reviewList);
    }

    // Helper method
    private Map<String, Object> mapReviewToResponse(Review review) {
        Map<String, Object> reviewMap = new HashMap<>();
        reviewMap.put("id", review.getId());
        reviewMap.put("rating", review.getRating());
        reviewMap.put("comment", review.getComment());
        reviewMap.put("datePosted", review.getDatePosted());
        reviewMap.put("isApproved", review.getIsApproved());
        reviewMap.put("response", review.getResponse());

        // Include user details
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", review.getUser().getId());
        userMap.put("username", review.getUser().getUsername());
        userMap.put("firstName", review.getUser().getFirstName());
        userMap.put("lastName", review.getUser().getLastName());

        reviewMap.put("user", userMap);

        // Include hotel details
        Map<String, Object> hotelMap = new HashMap<>();
        hotelMap.put("id", review.getHotel().getId());
        hotelMap.put("name", review.getHotel().getName());
        hotelMap.put("city", review.getHotel().getCity());
        hotelMap.put("country", review.getHotel().getCountry());
        hotelMap.put("starRating", review.getHotel().getStarRating());

        reviewMap.put("hotel", hotelMap);

        // Include booking reference
        reviewMap.put("bookingReference", review.getBooking().getBookingReference());

        return reviewMap;
    }
}