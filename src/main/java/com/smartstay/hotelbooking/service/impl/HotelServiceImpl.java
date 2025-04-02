package com.smartstay.hotelbooking.service.impl;

import com.smartstay.hotelbooking.model.entity.Hotel;
import com.smartstay.hotelbooking.repository.HotelRepository;
import com.smartstay.hotelbooking.service.HotelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class HotelServiceImpl implements HotelService {

    private final HotelRepository hotelRepository;

    @Autowired
    public HotelServiceImpl(HotelRepository hotelRepository) {
        this.hotelRepository = hotelRepository;
    }

    @Override
    public Hotel createHotel(Hotel hotel) {
        return hotelRepository.save(hotel);
    }

    @Override
    public Optional<Hotel> findById(Long id) {
        return hotelRepository.findById(id);
    }

    @Override
    public Page<Hotel> findAll(Pageable pageable) {
        return hotelRepository.findAll(pageable);
    }

    @Override
    public Page<Hotel> findByCity(String city, Pageable pageable) {
        return hotelRepository.findByCity(city, pageable);
    }

    @Override
    public Page<Hotel> findByCountry(String country, Pageable pageable) {
        return hotelRepository.findByCountry(country, pageable);
    }

    @Override
    public Page<Hotel> findByStarRating(Integer starRating, Pageable pageable) {
        return hotelRepository.findByStarRating(starRating, pageable);
    }

    @Override
    public Page<Hotel> findByCityAndMinimumRating(String city, int minRating, Pageable pageable) {
        return hotelRepository.findByCityAndMinimumRating(city, minRating, pageable);
    }

    @Override
    public List<Hotel> findAllActiveHotels() {
        return hotelRepository.findAllActiveHotels();
    }

    @Override
    public Hotel updateHotel(Hotel hotel) {
        return hotelRepository.save(hotel);
    }

    @Override
    public void deleteHotel(Long id) {
        hotelRepository.deleteById(id);
    }
}