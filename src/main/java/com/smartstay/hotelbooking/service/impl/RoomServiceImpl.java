package com.smartstay.hotelbooking.service.impl;

import com.smartstay.hotelbooking.model.entity.Hotel;
import com.smartstay.hotelbooking.model.entity.Room;
import com.smartstay.hotelbooking.model.entity.RoomType;
import com.smartstay.hotelbooking.repository.RoomRepository;
import com.smartstay.hotelbooking.service.RoomService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;

    @Autowired
    public RoomServiceImpl(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @Override
    public Room createRoom(Room room) {
        return roomRepository.save(room);
    }

    @Override
    public Optional<Room> findById(Long id) {
        return roomRepository.findById(id);
    }

    @Override
    public List<Room> findAll() {
        return roomRepository.findAll();
    }

    @Override
    public List<Room> findByHotel(Hotel hotel) {
        return roomRepository.findByHotel(hotel);
    }

    @Override
    public Page<Room> findByHotel(Hotel hotel, Pageable pageable) {
        return roomRepository.findByHotel(hotel, pageable);
    }

    @Override
    public List<Room> findByRoomType(RoomType roomType) {
        return roomRepository.findByRoomType(roomType);
    }

    @Override
    public List<Room> findByStatus(Room.RoomStatus status) {
        return roomRepository.findByStatus(status);
    }

    @Override
    public Optional<Room> findByHotelAndRoomNumber(Hotel hotel, String roomNumber) {
        return roomRepository.findByHotelAndRoomNumber(hotel, roomNumber);
    }

    @Override
    public List<Room> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return roomRepository.findByPriceRange(minPrice, maxPrice);
    }

    @Override
    public List<Room> findByHotelIdAndMinCapacity(Long hotelId, Integer capacity) {
        return roomRepository.findByHotelIdAndMinCapacity(hotelId, capacity);
    }

    @Override
    public List<Room> findAvailableRooms(Long hotelId, LocalDate checkInDate, LocalDate checkOutDate) {
        return roomRepository.findAvailableRooms(hotelId, checkInDate, checkOutDate);
    }

    @Override
    public Room updateRoom(Room room) {
        return roomRepository.save(room);
    }

    @Override
    public Room updateRoomStatus(Long roomId, Room.RoomStatus status) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("Room not found with id: " + roomId));
        room.setStatus(status);
        return roomRepository.save(room);
    }

    @Override
    public void deleteRoom(Long id) {
        roomRepository.deleteById(id);
    }
}