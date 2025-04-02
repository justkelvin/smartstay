package com.smartstay.hotelbooking.repository;

import com.smartstay.hotelbooking.model.entity.Booking;
import com.smartstay.hotelbooking.model.entity.Room;
import com.smartstay.hotelbooking.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Page<Booking> findByUser(User user, Pageable pageable);

    List<Booking> findByRoom(Room room);

    Optional<Booking> findByBookingReference(String bookingReference);

    List<Booking> findByBookingStatus(Booking.BookingStatus status);

    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId AND b.bookingStatus = :status")
    List<Booking> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") Booking.BookingStatus status);

    @Query("SELECT b FROM Booking b JOIN b.room r JOIN r.hotel h " +
            "WHERE h.id = :hotelId AND b.checkInDate >= :startDate AND b.checkInDate <= :endDate")
    List<Booking> findByHotelAndDateRange(@Param("hotelId") Long hotelId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Booking b " +
            "WHERE b.room.id = :roomId AND b.bookingStatus NOT IN ('CANCELLED', 'NO_SHOW') " +
            "AND ((b.checkInDate <= :checkOutDate AND b.checkOutDate >= :checkInDate))")
    boolean isRoomBookedInDateRange(@Param("roomId") Long roomId,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate);
}