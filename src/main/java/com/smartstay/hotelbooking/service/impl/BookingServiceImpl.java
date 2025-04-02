package com.smartstay.hotelbooking.service.impl;

import com.smartstay.hotelbooking.model.entity.Booking;
import com.smartstay.hotelbooking.model.entity.Payment;
import com.smartstay.hotelbooking.model.entity.Room;
import com.smartstay.hotelbooking.model.entity.User;
import com.smartstay.hotelbooking.repository.BookingRepository;
import com.smartstay.hotelbooking.repository.RoomRepository;
import com.smartstay.hotelbooking.repository.UserRepository;
import com.smartstay.hotelbooking.service.BookingService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final Random random = new Random();

    @Autowired
    public BookingServiceImpl(BookingRepository bookingRepository,
            UserRepository userRepository,
            RoomRepository roomRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
    }

    @Override
    @Transactional
    public Booking createBooking(Booking booking) {
        // Generate a unique booking reference
        booking.setBookingReference(generateBookingReference());

        // Initialize payment record
        Payment payment = new Payment();
        payment.setAmount(booking.getTotalPrice());
        payment.setPaymentStatus(Payment.PaymentStatus.PENDING);
        payment.setBooking(booking);
        booking.setPayment(payment);

        return bookingRepository.save(booking);
    }

    @Override
    public Optional<Booking> findById(Long id) {
        return bookingRepository.findById(id);
    }

    @Override
    public Optional<Booking> findByBookingReference(String bookingReference) {
        return bookingRepository.findByBookingReference(bookingReference);
    }

    @Override
    public Page<Booking> findByUser(User user, Pageable pageable) {
        return bookingRepository.findByUser(user, pageable);
    }

    @Override
    public List<Booking> findByRoom(Room room) {
        return bookingRepository.findByRoom(room);
    }

    @Override
    public List<Booking> findByBookingStatus(Booking.BookingStatus status) {
        return bookingRepository.findByBookingStatus(status);
    }

    @Override
    public List<Booking> findByUserIdAndStatus(Long userId, Booking.BookingStatus status) {
        return bookingRepository.findByUserIdAndStatus(userId, status);
    }

    @Override
    public List<Booking> findByHotelAndDateRange(Long hotelId, LocalDate startDate, LocalDate endDate) {
        return bookingRepository.findByHotelAndDateRange(hotelId, startDate, endDate);
    }

    @Override
    public boolean isRoomBookedInDateRange(Long roomId, LocalDate checkInDate, LocalDate checkOutDate) {
        return bookingRepository.isRoomBookedInDateRange(roomId, checkInDate, checkOutDate);
    }

    @Override
    public Booking updateBooking(Booking booking) {
        return bookingRepository.save(booking);
    }

    @Override
    @Transactional
    public Booking updateBookingStatus(Long bookingId, Booking.BookingStatus status) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with id: " + bookingId));
        booking.setBookingStatus(status);
        return bookingRepository.save(booking);
    }

    @Override
    @Transactional
    public Booking cancelBooking(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with id: " + bookingId));

        // Check if booking can be cancelled
        if (booking.getBookingStatus() == Booking.BookingStatus.CHECKED_IN ||
                booking.getBookingStatus() == Booking.BookingStatus.CHECKED_OUT) {
            throw new IllegalStateException("Cannot cancel a booking that is already checked-in or checked-out");
        }

        booking.setBookingStatus(Booking.BookingStatus.CANCELLED);

        // Handle payment status
        if (booking.getPayment() != null &&
                booking.getPayment().getPaymentStatus() == Payment.PaymentStatus.COMPLETED) {
            // Set payment status to REFUNDED
            booking.getPayment().setPaymentStatus(Payment.PaymentStatus.REFUNDED);
        }

        return bookingRepository.save(booking);
    }

    @Override
    public void deleteBooking(Long id) {
        bookingRepository.deleteById(id);
    }

    @Override
    public String generateBookingReference() {
        // Generate a unique booking reference with format "BK" + 8 random digits
        StringBuilder sb = new StringBuilder("BK");
        for (int i = 0; i < 8; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    @Override
    @Transactional
    public Booking processBookingRequest(Long userId, Long roomId, LocalDate checkInDate,
            LocalDate checkOutDate, int adults, int children,
            String specialRequests) {
        // Check if room is available
        if (isRoomBookedInDateRange(roomId, checkInDate, checkOutDate)) {
            throw new IllegalStateException("Room is not available for the selected dates");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("Room not found with id: " + roomId));

        // Check if room capacity is sufficient
        int totalGuests = adults + children;
        if (totalGuests > room.getCapacity()) {
            throw new IllegalArgumentException("Room capacity is not sufficient for the number of guests");
        }

        // Calculate total price
        long nights = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        BigDecimal totalPrice = room.getPricePerNight().multiply(new BigDecimal(nights));

        // Create booking
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setRoom(room);
        booking.setCheckInDate(checkInDate);
        booking.setCheckOutDate(checkOutDate);
        booking.setAdults(adults);
        booking.setChildren(children);
        booking.setTotalPrice(totalPrice);
        booking.setSpecialRequests(specialRequests);
        booking.setBookingStatus(Booking.BookingStatus.CONFIRMED);

        return createBooking(booking);
    }
}