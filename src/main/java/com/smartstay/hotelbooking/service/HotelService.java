package com.smartstay.hotelbooking.service;

import com.smartstay.hotelbooking.model.entity.Hotel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface HotelService {
    Hotel createHotel(Hotel hotel);

    Optional<Hotel> findById(Long id);

    Page<Hotel> findAll(Pageable pageable);

    Page<Hotel> findByCity(String city, Pageable pageable);

    Page<Hotel> findByCountry(String country, Pageable pageable);

    Page<Hotel> findByStarRating(Integer starRating, Pageable pageable);

    Page<Hotel> findByCityAndMinimumRating(String city, int minRating, Pageable pageable);

    List<Hotel> findAllActiveHotels();

    Hotel updateHotel(Hotel hotel);

    void deleteHotel(Long id);
}