package com.smartstay.hotelbooking.service.impl;

import com.smartstay.hotelbooking.model.entity.Booking;
import com.smartstay.hotelbooking.model.entity.Hotel;
import com.smartstay.hotelbooking.model.entity.Review;
import com.smartstay.hotelbooking.model.entity.User;
import com.smartstay.hotelbooking.repository.BookingRepository;
import com.smartstay.hotelbooking.repository.ReviewRepository;
import com.smartstay.hotelbooking.service.ReviewService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;

    @Autowired
    public ReviewServiceImpl(ReviewRepository reviewRepository, BookingRepository bookingRepository) {
        this.reviewRepository = reviewRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    public Review createReview(Review review) {
        review.setDatePosted(LocalDateTime.now());
        review.setIsApproved(true); // Default to approved; can be changed based on business rules
        return reviewRepository.save(review);
    }

    @Override
    public Optional<Review> findById(Long id) {
        return reviewRepository.findById(id);
    }

    @Override
    public Page<Review> findByHotel(Hotel hotel, Pageable pageable) {
        return reviewRepository.findByHotel(hotel, pageable);
    }

    @Override
    public List<Review> findByUser(User user) {
        return reviewRepository.findByUser(user);
    }

    @Override
    public Optional<Review> findByBooking(Booking booking) {
        return reviewRepository.findByBooking(booking);
    }

    @Override
    public List<Review> findByIsApproved(boolean isApproved) {
        return reviewRepository.findByIsApproved(isApproved);
    }

    @Override
    public Double findAverageRatingByHotelId(Long hotelId) {
        return reviewRepository.findAverageRatingByHotelId(hotelId);
    }

    @Override
    public Map<Integer, Long> findRatingDistributionByHotelId(Long hotelId) {
        Map<Integer, Long> distribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            distribution.put(i, reviewRepository.countByHotelIdAndRating(hotelId, i));
        }
        return distribution;
    }

    @Override
    public Review updateReview(Review review) {
        return reviewRepository.save(review);
    }

    @Override
    @Transactional
    public Review approveReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("Review not found with id: " + reviewId));
        review.setIsApproved(true);
        return reviewRepository.save(review);
    }

    @Override
    @Transactional
    public Review addManagementResponse(Long reviewId, String response) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("Review not found with id: " + reviewId));
        review.setResponse(response);
        return reviewRepository.save(review);
    }

    @Override
    @Transactional
    public Review submitReview(Long bookingId, Integer rating, String comment) {
        // Check if the booking exists
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with id: " + bookingId));

        // Validate that the booking is completed
        if (booking.getBookingStatus() != Booking.BookingStatus.CHECKED_OUT) {
            throw new IllegalStateException("Cannot review a booking that has not been checked out");
        }

        // Check if a review for this booking already exists
        Optional<Review> existingReview = reviewRepository.findByBooking(booking);
        if (existingReview.isPresent()) {
            throw new IllegalStateException("A review for this booking already exists");
        }

        // Create a new review
        Review review = new Review();
        review.setBooking(booking);
        review.setUser(booking.getUser());
        review.setHotel(booking.getRoom().getHotel());
        review.setRating(rating);
        review.setComment(comment);
        review.setDatePosted(LocalDateTime.now());
        review.setIsApproved(true);

        return reviewRepository.save(review);
    }

    @Override
    public void deleteReview(Long id) {
        reviewRepository.deleteById(id);
    }
}