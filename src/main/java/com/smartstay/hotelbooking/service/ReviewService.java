package com.smartstay.hotelbooking.service;

import com.smartstay.hotelbooking.model.entity.Booking;
import com.smartstay.hotelbooking.model.entity.Hotel;
import com.smartstay.hotelbooking.model.entity.Review;
import com.smartstay.hotelbooking.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ReviewService {
    Review createReview(Review review);

    Optional<Review> findById(Long id);

    Page<Review> findByHotel(Hotel hotel, Pageable pageable);

    List<Review> findByUser(User user);

    Optional<Review> findByBooking(Booking booking);

    List<Review> findByIsApproved(boolean isApproved);

    Double findAverageRatingByHotelId(Long hotelId);

    Map<Integer, Long> findRatingDistributionByHotelId(Long hotelId);

    Review updateReview(Review review);

    Review approveReview(Long reviewId);

    Review addManagementResponse(Long reviewId, String response);

    Review submitReview(Long bookingId, Integer rating, String comment);

    void deleteReview(Long id);
}