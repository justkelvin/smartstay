package com.smartstay.hotelbooking.service;

import com.smartstay.hotelbooking.model.entity.Booking;
import com.smartstay.hotelbooking.model.entity.Room;
import com.smartstay.hotelbooking.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BookingService {
    Booking createBooking(Booking booking);

    Optional<Booking> findById(Long id);

    Optional<Booking> findByBookingReference(String bookingReference);

    Page<Booking> findByUser(User user, Pageable pageable);

    List<Booking> findByRoom(Room room);

    List<Booking> findByBookingStatus(Booking.BookingStatus status);

    List<Booking> findByUserIdAndStatus(Long userId, Booking.BookingStatus status);

    List<Booking> findByHotelAndDateRange(Long hotelId, LocalDate startDate, LocalDate endDate);

    boolean isRoomBookedInDateRange(Long roomId, LocalDate checkInDate, LocalDate checkOutDate);

    Booking updateBooking(Booking booking);

    Booking updateBookingStatus(Long bookingId, Booking.BookingStatus status);

    Booking cancelBooking(Long bookingId);

    void deleteBooking(Long id);

    // Additional business logic methods
    String generateBookingReference();

    Booking processBookingRequest(Long userId, Long roomId, LocalDate checkInDate,
            LocalDate checkOutDate, int adults, int children,
            String specialRequests);
}