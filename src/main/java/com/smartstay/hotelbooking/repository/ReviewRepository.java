package com.smartstay.hotelbooking.repository;

import com.smartstay.hotelbooking.model.entity.Booking;
import com.smartstay.hotelbooking.model.entity.Hotel;
import com.smartstay.hotelbooking.model.entity.Review;
import com.smartstay.hotelbooking.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Page<Review> findByHotel(Hotel hotel, Pageable pageable);

    List<Review> findByUser(User user);

    Optional<Review> findByBooking(Booking booking);

    List<Review> findByIsApproved(boolean isApproved);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.hotel.id = :hotelId")
    Double findAverageRatingByHotelId(@Param("hotelId") Long hotelId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.hotel.id = :hotelId AND r.rating = :rating")
    Long countByHotelIdAndRating(@Param("hotelId") Long hotelId, @Param("rating") Integer rating);
}