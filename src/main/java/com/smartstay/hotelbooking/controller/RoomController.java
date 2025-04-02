package com.smartstay.hotelbooking.controller;

import com.smartstay.hotelbooking.model.entity.Hotel;
import com.smartstay.hotelbooking.model.entity.Room;
import com.smartstay.hotelbooking.model.entity.RoomType;
import com.smartstay.hotelbooking.repository.BookingRepository;
import com.smartstay.hotelbooking.service.HotelService;
import com.smartstay.hotelbooking.service.RoomService;
import com.smartstay.hotelbooking.service.RoomTypeService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;
    private final HotelService hotelService;
    private final RoomTypeService roomTypeService;
    private final BookingRepository bookingRepository;

    @Autowired
    public RoomController(RoomService roomService,
            HotelService hotelService,
            RoomTypeService roomTypeService,
            BookingRepository bookingRepository) {
        this.roomService = roomService;
        this.hotelService = hotelService;
        this.roomTypeService = roomTypeService;
        this.bookingRepository = bookingRepository;
    }

    @GetMapping("/available")
    public ResponseEntity<?> getAvailableRooms(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate,
            @RequestParam(required = false) String city,
            @RequestParam(defaultValue = "1") int adults,
            @RequestParam(defaultValue = "0") int children,
            @RequestParam(required = false) String roomType) {

        // Basic validation
        if (checkInDate.isBefore(LocalDate.now())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Check-in date cannot be in the past"));
        }
        if (checkOutDate.isBefore(checkInDate)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Check-out date must be after check-in date"));
        }

        List<Map<String, Object>> availableRoomsResponse = new ArrayList<>();

        // Filter hotels by city if provided
        List<Hotel> hotels;
        if (city != null && !city.isEmpty()) {
            hotels = hotelService.findByCity(city, null).getContent();
        } else {
            hotels = hotelService.findAllActiveHotels();
        }

        // For each hotel, find available rooms
        for (Hotel hotel : hotels) {
            List<Room> availableRooms = roomService.findAvailableRooms(
                    hotel.getId(), checkInDate, checkOutDate);

            // Filter by capacity
            int totalGuests = adults + children;
            availableRooms = availableRooms.stream()
                    .filter(room -> room.getCapacity() >= totalGuests)
                    .collect(Collectors.toList());

            // Filter by room type if provided
            if (roomType != null && !roomType.isEmpty()) {
                availableRooms = availableRooms.stream()
                        .filter(room -> room.getRoomType().getName().equalsIgnoreCase(roomType))
                        .collect(Collectors.toList());
            }

            // Add filtered rooms to response
            for (Room room : availableRooms) {
                Map<String, Object> roomMap = new HashMap<>();
                roomMap.put("roomId", room.getId());
                roomMap.put("hotelId", hotel.getId());
                roomMap.put("hotelName", hotel.getName());
                roomMap.put("hotelCity", hotel.getCity());
                roomMap.put("roomNumber", room.getRoomNumber());
                roomMap.put("roomType", room.getRoomType().getName());
                roomMap.put("pricePerNight", room.getPricePerNight());

                // Calculate total price for the stay
                long nights = checkInDate.until(checkOutDate).getDays();
                BigDecimal totalPrice = room.getPricePerNight().multiply(BigDecimal.valueOf(nights));
                roomMap.put("totalPrice", totalPrice);

                roomMap.put("capacity", room.getCapacity());
                roomMap.put("description", room.getDescription());

                // Parse amenities
                String amenitiesString = room.getRoomType().getAmenities();
                List<String> amenitiesList = new ArrayList<>();
                if (amenitiesString != null && !amenitiesString.isEmpty()) {
                    for (String amenity : amenitiesString.split(",")) {
                        amenitiesList.add(amenity.trim());
                    }
                }
                roomMap.put("amenities", amenitiesList);

                availableRoomsResponse.add(roomMap);
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("content", availableRoomsResponse);
        response.put("pageable", Map.of(
                "pageNumber", 0,
                "pageSize", availableRoomsResponse.size(),
                "totalPages", 1,
                "totalElements", availableRoomsResponse.size()));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getRoomById(@PathVariable Long id) {
        Room room = roomService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Room not found with id: " + id));

        Map<String, Object> response = new HashMap<>();
        response.put("roomId", room.getId());
        response.put("roomNumber", room.getRoomNumber());
        response.put("floor", room.getFloor());
        response.put("capacity", room.getCapacity());
        response.put("pricePerNight", room.getPricePerNight());
        response.put("description", room.getDescription());
        response.put("status", room.getStatus().name());

        Map<String, Object> hotelMap = new HashMap<>();
        hotelMap.put("hotelId", room.getHotel().getId());
        hotelMap.put("name", room.getHotel().getName());
        hotelMap.put("address", room.getHotel().getAddress());
        hotelMap.put("city", room.getHotel().getCity());
        hotelMap.put("country", room.getHotel().getCountry());
        hotelMap.put("starRating", room.getHotel().getStarRating());
        response.put("hotel", hotelMap);

        Map<String, Object> roomTypeMap = new HashMap<>();
        roomTypeMap.put("roomTypeId", room.getRoomType().getId());
        roomTypeMap.put("name", room.getRoomType().getName());
        roomTypeMap.put("description", room.getRoomType().getDescription());
        roomTypeMap.put("baseCapacity", room.getRoomType().getBaseCapacity());
        roomTypeMap.put("maxCapacity", room.getRoomType().getMaxCapacity());

        // Parse amenities
        String amenitiesString = room.getRoomType().getAmenities();
        List<String> amenitiesList = new ArrayList<>();
        if (amenitiesString != null && !amenitiesString.isEmpty()) {
            for (String amenity : amenitiesString.split(",")) {
                amenitiesList.add(amenity.trim());
            }
        }
        roomTypeMap.put("amenities", amenitiesList);

        response.put("roomType", roomTypeMap);

        // Generate availability calendar for next 30 days
        List<Map<String, Object>> availabilityCalendar = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = 0; i < 30; i++) {
            LocalDate date = today.plusDays(i);
            boolean available = !bookingRepository.isRoomBookedInDateRange(
                    room.getId(), date, date.plusDays(1));

            Map<String, Object> dateAvailability = new HashMap<>();
            dateAvailability.put("date", date);
            dateAvailability.put("available", available);
            dateAvailability.put("price", available ? room.getPricePerNight() : null);

            availabilityCalendar.add(dateAvailability);
        }

        response.put("availabilityCalendar", availabilityCalendar);

        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createRoom(@RequestBody Map<String, Object> roomDetails) {
        try {
            Long hotelId = Long.parseLong(roomDetails.get("hotelId").toString());
            Long roomTypeId = Long.parseLong(roomDetails.get("roomTypeId").toString());

            Hotel hotel = hotelService.findById(hotelId)
                    .orElseThrow(() -> new EntityNotFoundException("Hotel not found with id: " + hotelId));

            RoomType roomType = roomTypeService.findById(roomTypeId)
                    .orElseThrow(() -> new EntityNotFoundException("RoomType not found with id: " + roomTypeId));

            Room room = new Room();
            room.setRoomNumber(roomDetails.get("roomNumber").toString());
            room.setFloor(Integer.parseInt(roomDetails.get("floor").toString()));
            room.setCapacity(Integer.parseInt(roomDetails.get("capacity").toString()));
            room.setPricePerNight(new BigDecimal(roomDetails.get("pricePerNight").toString()));
            room.setDescription((String) roomDetails.get("description"));
            room.setStatus(Room.RoomStatus.valueOf(roomDetails.get("status").toString()));
            room.setHotel(hotel);
            room.setRoomType(roomType);

            Room createdRoom = roomService.createRoom(room);

            return new ResponseEntity<>(createdRoom, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid input format: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateRoom(@PathVariable Long id, @RequestBody Map<String, Object> roomDetails) {
        try {
            Room room = roomService.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Room not found with id: " + id));

            if (roomDetails.containsKey("roomNumber")) {
                room.setRoomNumber(roomDetails.get("roomNumber").toString());
            }

            if (roomDetails.containsKey("floor")) {
                room.setFloor(Integer.parseInt(roomDetails.get("floor").toString()));
            }

            if (roomDetails.containsKey("capacity")) {
                room.setCapacity(Integer.parseInt(roomDetails.get("capacity").toString()));
            }

            if (roomDetails.containsKey("pricePerNight")) {
                room.setPricePerNight(new BigDecimal(roomDetails.get("pricePerNight").toString()));
            }

            if (roomDetails.containsKey("description")) {
                room.setDescription((String) roomDetails.get("description"));
            }

            if (roomDetails.containsKey("status")) {
                room.setStatus(Room.RoomStatus.valueOf(roomDetails.get("status").toString()));
            }

            if (roomDetails.containsKey("hotelId")) {
                Long hotelId = Long.parseLong(roomDetails.get("hotelId").toString());
                Hotel hotel = hotelService.findById(hotelId)
                        .orElseThrow(() -> new EntityNotFoundException("Hotel not found with id: " + hotelId));
                room.setHotel(hotel);
            }

            if (roomDetails.containsKey("roomTypeId")) {
                Long roomTypeId = Long.parseLong(roomDetails.get("roomTypeId").toString());
                RoomType roomType = roomTypeService.findById(roomTypeId)
                        .orElseThrow(() -> new EntityNotFoundException("RoomType not found with id: " + roomTypeId));
                room.setRoomType(roomType);
            }

            Room updatedRoom = roomService.updateRoom(room);

            return ResponseEntity.ok(updatedRoom);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid input format: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteRoom(@PathVariable Long id) {
        roomService.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Room not found with id: " + id));

        roomService.deleteRoom(id);

        return ResponseEntity.ok(Map.of("message", "Room deleted successfully"));
    }
}