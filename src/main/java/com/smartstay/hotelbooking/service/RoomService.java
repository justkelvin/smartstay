package com.smartstay.hotelbooking.service;

import com.smartstay.hotelbooking.model.entity.Hotel;
import com.smartstay.hotelbooking.model.entity.Room;
import com.smartstay.hotelbooking.model.entity.RoomType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RoomService {
    Room createRoom(Room room);

    Optional<Room> findById(Long id);

    List<Room> findAll();

    List<Room> findByHotel(Hotel hotel);

    Page<Room> findByHotel(Hotel hotel, Pageable pageable);

    List<Room> findByRoomType(RoomType roomType);

    List<Room> findByStatus(Room.RoomStatus status);

    Optional<Room> findByHotelAndRoomNumber(Hotel hotel, String roomNumber);

    List<Room> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice);

    List<Room> findByHotelIdAndMinCapacity(Long hotelId, Integer capacity);

    List<Room> findAvailableRooms(Long hotelId, LocalDate checkInDate, LocalDate checkOutDate);

    Room updateRoom(Room room);

    Room updateRoomStatus(Long roomId, Room.RoomStatus status);

    void deleteRoom(Long id);
}