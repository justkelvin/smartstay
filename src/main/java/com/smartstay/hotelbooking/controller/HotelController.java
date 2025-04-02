package com.smartstay.hotelbooking.controller;

import com.smartstay.hotelbooking.model.entity.Hotel;
import com.smartstay.hotelbooking.model.entity.Review;
import com.smartstay.hotelbooking.model.entity.Room;
import com.smartstay.hotelbooking.service.HotelService;
import com.smartstay.hotelbooking.service.ReviewService;
import com.smartstay.hotelbooking.service.RoomService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/hotels")
public class HotelController {

    private final HotelService hotelService;
    private final RoomService roomService;
    private final ReviewService reviewService;

    @Autowired
    public HotelController(HotelService hotelService, RoomService roomService, ReviewService reviewService) {
        this.hotelService = hotelService;
        this.roomService = roomService;
        this.reviewService = reviewService;
    }

    @GetMapping
    public ResponseEntity<?> getAllHotels(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) Integer starRating,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<Hotel> hotels;

        if (city != null && !city.isEmpty()) {
            if (starRating != null) {
                hotels = hotelService.findByCityAndMinimumRating(city, starRating, pageable);
            } else {
                hotels = hotelService.findByCity(city, pageable);
            }
        } else if (country != null && !country.isEmpty()) {
            hotels = hotelService.findByCountry(country, pageable);
        } else if (starRating != null) {
            hotels = hotelService.findByStarRating(starRating, pageable);
        } else {
            hotels = hotelService.findAll(pageable);
        }

        return ResponseEntity.ok(hotels);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getHotelById(@PathVariable Long id) {
        Hotel hotel = hotelService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Hotel not found with id: " + id));

        // Get additional information for response
        Double averageRating = reviewService.findAverageRatingByHotelId(id);
        Map<Integer, Long> ratingDistribution = reviewService.findRatingDistributionByHotelId(id);

        Map<String, Object> response = new HashMap<>();
        response.put("hotelId", hotel.getId());
        response.put("name", hotel.getName());
        response.put("description", hotel.getDescription());
        response.put("address", hotel.getAddress());
        response.put("city", hotel.getCity());
        response.put("country", hotel.getCountry());
        response.put("postalCode", hotel.getPostalCode());
        response.put("starRating", hotel.getStarRating());
        response.put("amenities", hotel.getAmenities());
        response.put("checkInTime", hotel.getCheckInTime());
        response.put("checkOutTime", hotel.getCheckOutTime());
        response.put("status", hotel.getStatus().name());
        response.put("totalRooms", hotel.getRooms().size());
        response.put("availableRooms", hotel.getRooms().stream()
                .filter(room -> room.getStatus() == Room.RoomStatus.AVAILABLE)
                .count());
        response.put("rating", averageRating);
        response.put("ratingDistribution", ratingDistribution);
        response.put("reviewCount", ratingDistribution.values().stream().mapToLong(Long::longValue).sum());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/rooms")
    public ResponseEntity<?> getHotelRooms(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Integer capacity) {

        Hotel hotel = hotelService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Hotel not found with id: " + id));

        Pageable pageable = PageRequest.of(page, size);
        Page<Room> rooms = roomService.findByHotel(hotel, pageable);

        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/{id}/reviews")
    public ResponseEntity<?> getHotelReviews(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "datePosted") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Hotel hotel = hotelService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Hotel not found with id: " + id));

        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<Review> reviews = reviewService.findByHotel(hotel, pageable);
        Double averageRating = reviewService.findAverageRatingByHotelId(id);
        Map<Integer, Long> ratingDistribution = reviewService.findRatingDistributionByHotelId(id);

        Map<String, Object> response = new HashMap<>();
        response.put("content", reviews.getContent().stream().map(review -> {
            Map<String, Object> reviewMap = new HashMap<>();
            reviewMap.put("reviewId", review.getId());
            reviewMap.put("user", Map.of(
                    "firstName", review.getUser().getFirstName(),
                    "lastName", review.getUser().getLastName()));
            reviewMap.put("rating", review.getRating());
            reviewMap.put("comment", review.getComment());
            reviewMap.put("datePosted", review.getDatePosted());
            reviewMap.put("response", review.getResponse());
            return reviewMap;
        }).collect(Collectors.toList()));

        response.put("averageRating", averageRating);
        response.put("reviewCount", reviews.getTotalElements());
        response.put("ratingDistribution", ratingDistribution);

        Map<String, Object> pageInfo = new HashMap<>();
        pageInfo.put("pageNumber", reviews.getNumber());
        pageInfo.put("pageSize", reviews.getSize());
        pageInfo.put("totalPages", reviews.getTotalPages());
        pageInfo.put("totalElements", reviews.getTotalElements());

        response.put("pageable", pageInfo);

        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createHotel(@RequestBody Hotel hotel) {
        Hotel createdHotel = hotelService.createHotel(hotel);
        return new ResponseEntity<>(createdHotel, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateHotel(@PathVariable Long id, @RequestBody Hotel hotelDetails) {
        Hotel hotel = hotelService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Hotel not found with id: " + id));

        hotel.setName(hotelDetails.getName());
        hotel.setDescription(hotelDetails.getDescription());
        hotel.setAddress(hotelDetails.getAddress());
        hotel.setCity(hotelDetails.getCity());
        hotel.setCountry(hotelDetails.getCountry());
        hotel.setPostalCode(hotelDetails.getPostalCode());
        hotel.setStarRating(hotelDetails.getStarRating());
        hotel.setAmenities(hotelDetails.getAmenities());
        hotel.setCheckInTime(hotelDetails.getCheckInTime());
        hotel.setCheckOutTime(hotelDetails.getCheckOutTime());
        hotel.setStatus(hotelDetails.getStatus());

        Hotel updatedHotel = hotelService.updateHotel(hotel);
        return ResponseEntity.ok(updatedHotel);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteHotel(@PathVariable Long id) {
        Hotel hotel = hotelService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Hotel not found with id: " + id));

        hotelService.deleteHotel(id);

        return ResponseEntity.ok(Map.of("message", "Hotel deleted successfully"));
    }
}