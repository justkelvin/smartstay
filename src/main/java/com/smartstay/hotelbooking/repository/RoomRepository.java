package com.smartstay.hotelbooking.repository;

import com.smartstay.hotelbooking.model.entity.Hotel;
import com.smartstay.hotelbooking.model.entity.Room;
import com.smartstay.hotelbooking.model.entity.RoomType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByHotel(Hotel hotel);

    Page<Room> findByHotel(Hotel hotel, Pageable pageable);

    List<Room> findByRoomType(RoomType roomType);

    List<Room> findByStatus(Room.RoomStatus status);

    Optional<Room> findByHotelAndRoomNumber(Hotel hotel, String roomNumber);

    @Query("SELECT r FROM Room r WHERE r.pricePerNight BETWEEN :minPrice AND :maxPrice")
    List<Room> findByPriceRange(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);

    @Query("SELECT r FROM Room r WHERE r.hotel.id = :hotelId AND r.capacity >= :capacity")
    List<Room> findByHotelIdAndMinCapacity(@Param("hotelId") Long hotelId, @Param("capacity") Integer capacity);

    @Query("SELECT r FROM Room r WHERE r.hotel.id = :hotelId AND r.id NOT IN " +
            "(SELECT b.room.id FROM Booking b WHERE b.bookingStatus NOT IN ('CANCELLED', 'NO_SHOW') " +
            "AND ((b.checkInDate <= :checkOutDate AND b.checkOutDate >= :checkInDate)))")
    List<Room> findAvailableRooms(@Param("hotelId") Long hotelId,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate);
}