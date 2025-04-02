package com.smartstay.hotelbooking.repository;

import com.smartstay.hotelbooking.model.entity.Hotel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HotelRepository extends JpaRepository<Hotel, Long> {
    Page<Hotel> findByCity(String city, Pageable pageable);

    Page<Hotel> findByCountry(String country, Pageable pageable);

    Page<Hotel> findByStarRating(Integer starRating, Pageable pageable);

    @Query("SELECT h FROM Hotel h WHERE h.city = :city AND h.starRating >= :minRating")
    Page<Hotel> findByCityAndMinimumRating(@Param("city") String city, @Param("minRating") int minRating,
            Pageable pageable);

    @Query("SELECT h FROM Hotel h WHERE h.status = 'ACTIVE'")
    List<Hotel> findAllActiveHotels();
}