package com.smartstay.hotelbooking.config;

import com.smartstay.hotelbooking.model.entity.*;
import com.smartstay.hotelbooking.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

@Component
@Profile("dev") // Only run in development mode
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final HotelRepository hotelRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final ReviewRepository reviewRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DataLoader(UserRepository userRepository, HotelRepository hotelRepository,
            RoomTypeRepository roomTypeRepository, RoomRepository roomRepository,
            BookingRepository bookingRepository, PaymentRepository paymentRepository,
            ReviewRepository reviewRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.hotelRepository = hotelRepository;
        this.roomTypeRepository = roomTypeRepository;
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
        this.reviewRepository = reviewRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Only load data if the database is empty
        if (userRepository.count() > 0) {
            return;
        }

        // Create users
        User adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setPassword(passwordEncoder.encode("admin123"));
        adminUser.setEmail("admin@smartstay.com");
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setPhoneNumber("1234567890");
        adminUser.setAddress("123 Admin St");
        adminUser.setRole(User.UserRole.ADMIN);
        userRepository.save(adminUser);

        User customerUser = new User();
        customerUser.setUsername("customer");
        customerUser.setPassword(passwordEncoder.encode("customer123"));
        customerUser.setEmail("customer@example.com");
        customerUser.setFirstName("John");
        customerUser.setLastName("Doe");
        customerUser.setPhoneNumber("9876543210");
        customerUser.setAddress("456 Customer Ave");
        customerUser.setRole(User.UserRole.CUSTOMER);
        userRepository.save(customerUser);

        // Create hotels
        Hotel hotel1 = new Hotel();
        hotel1.setName("SmartStay Grand");
        hotel1.setDescription("A luxury hotel in the heart of the city with modern amenities and excellent service.");
        hotel1.setAddress("123 Main Street");
        hotel1.setCity("New York");
        hotel1.setCountry("USA");
        hotel1.setPostalCode("10001");
        hotel1.setStarRating(5);
        hotel1.setAmenities("Pool, Spa, Gym, Restaurant, Bar, Wi-Fi, Room Service, Concierge");
        hotel1.setCheckInTime(LocalTime.of(14, 0));
        hotel1.setCheckOutTime(LocalTime.of(11, 0));
        hotel1.setStatus(Hotel.HotelStatus.ACTIVE);
        hotelRepository.save(hotel1);

        Hotel hotel2 = new Hotel();
        hotel2.setName("SmartStay Express");
        hotel2.setDescription(
                "A budget-friendly hotel offering comfortable accommodations for business and leisure travelers.");
        hotel2.setAddress("789 Business Park");
        hotel2.setCity("Chicago");
        hotel2.setCountry("USA");
        hotel2.setPostalCode("60601");
        hotel2.setStarRating(3);
        hotel2.setAmenities("Wi-Fi, Breakfast, Business Center, Parking");
        hotel2.setCheckInTime(LocalTime.of(15, 0));
        hotel2.setCheckOutTime(LocalTime.of(12, 0));
        hotel2.setStatus(Hotel.HotelStatus.ACTIVE);
        hotelRepository.save(hotel2);

        // Create room types
        RoomType deluxeType = new RoomType();
        deluxeType.setName("Deluxe");
        deluxeType.setDescription("Spacious room with king-size bed and city view");
        deluxeType.setBaseCapacity(2);
        deluxeType.setMaxCapacity(3);
        deluxeType.setBasePrice(new BigDecimal("299.99"));
        deluxeType.setAmenities("King bed, 55-inch TV, Mini bar, Coffee machine, Safe");
        roomTypeRepository.save(deluxeType);

        RoomType standardType = new RoomType();
        standardType.setName("Standard");
        standardType.setDescription("Comfortable room with queen-size bed");
        standardType.setBaseCapacity(2);
        standardType.setMaxCapacity(2);
        standardType.setBasePrice(new BigDecimal("199.99"));
        standardType.setAmenities("Queen bed, 42-inch TV, Coffee maker");
        roomTypeRepository.save(standardType);

        RoomType suiteType = new RoomType();
        suiteType.setName("Suite");
        suiteType.setDescription("Luxury suite with separate living room and bedroom");
        suiteType.setBaseCapacity(2);
        suiteType.setMaxCapacity(4);
        suiteType.setBasePrice(new BigDecimal("499.99"));
        suiteType.setAmenities("King bed, Living room, Jacuzzi, 65-inch TV, Mini bar, Coffee machine, Safe, Work desk");
        roomTypeRepository.save(suiteType);

        // Create rooms for hotel1
        Room room101 = new Room();
        room101.setRoomNumber("101");
        room101.setFloor(1);
        room101.setCapacity(2);
        room101.setPricePerNight(new BigDecimal("329.99"));
        room101.setDescription("Deluxe room with city view");
        room101.setStatus(Room.RoomStatus.AVAILABLE);
        room101.setHotel(hotel1);
        room101.setRoomType(deluxeType);
        roomRepository.save(room101);

        Room room102 = new Room();
        room102.setRoomNumber("102");
        room102.setFloor(1);
        room102.setCapacity(2);
        room102.setPricePerNight(new BigDecimal("329.99"));
        room102.setDescription("Deluxe room with city view");
        room102.setStatus(Room.RoomStatus.AVAILABLE);
        room102.setHotel(hotel1);
        room102.setRoomType(deluxeType);
        roomRepository.save(room102);

        Room room201 = new Room();
        room201.setRoomNumber("201");
        room201.setFloor(2);
        room201.setCapacity(4);
        room201.setPricePerNight(new BigDecimal("599.99"));
        room201.setDescription("Luxury suite with panoramic city view");
        room201.setStatus(Room.RoomStatus.AVAILABLE);
        room201.setHotel(hotel1);
        room201.setRoomType(suiteType);
        roomRepository.save(room201);

        // Create rooms for hotel2
        Room room301 = new Room();
        room301.setRoomNumber("301");
        room301.setFloor(3);
        room301.setCapacity(2);
        room301.setPricePerNight(new BigDecimal("169.99"));
        room301.setDescription("Standard room with queen bed");
        room301.setStatus(Room.RoomStatus.AVAILABLE);
        room301.setHotel(hotel2);
        room301.setRoomType(standardType);
        roomRepository.save(room301);

        Room room302 = new Room();
        room302.setRoomNumber("302");
        room302.setFloor(3);
        room302.setCapacity(2);
        room302.setPricePerNight(new BigDecimal("169.99"));
        room302.setDescription("Standard room with queen bed");
        room302.setStatus(Room.RoomStatus.AVAILABLE);
        room302.setHotel(hotel2);
        room302.setRoomType(standardType);
        roomRepository.save(room302);

        // Create a completed booking
        Booking completedBooking = new Booking();
        completedBooking.setBookingReference("BK12345678");
        completedBooking.setCheckInDate(LocalDate.now().minusDays(7));
        completedBooking.setCheckOutDate(LocalDate.now().minusDays(5));
        completedBooking.setAdults(2);
        completedBooking.setChildren(0);
        completedBooking.setTotalPrice(new BigDecimal("659.98"));
        completedBooking.setBookingStatus(Booking.BookingStatus.CHECKED_OUT);
        completedBooking.setSpecialRequests("Late check-out requested");
        completedBooking.setUser(customerUser);
        completedBooking.setRoom(room101);
        bookingRepository.save(completedBooking);

        // Create payment for the completed booking
        Payment payment = new Payment();
        payment.setAmount(new BigDecimal("659.98"));
        payment.setPaymentMethod("Credit Card");
        payment.setTransactionId("TXN987654321");
        payment.setPaymentStatus(Payment.PaymentStatus.COMPLETED);
        payment.setPaymentDate(LocalDateTime.now().minusDays(8));
        payment.setCardLastDigits("4321");
        payment.setBooking(completedBooking);
        paymentRepository.save(payment);

        // Create a review for the completed booking
        Review review = new Review();
        review.setRating(4);
        review.setComment("Great stay! The room was clean and the staff was friendly. Would definitely stay again.");
        review.setDatePosted(LocalDateTime.now().minusDays(4));
        review.setIsApproved(true);
        review.setUser(customerUser);
        review.setHotel(hotel1);
        review.setBooking(completedBooking);
        reviewRepository.save(review);

        // Create an upcoming booking
        Booking upcomingBooking = new Booking();
        upcomingBooking.setBookingReference("BK87654321");
        upcomingBooking.setCheckInDate(LocalDate.now().plusDays(10));
        upcomingBooking.setCheckOutDate(LocalDate.now().plusDays(15));
        upcomingBooking.setAdults(2);
        upcomingBooking.setChildren(1);
        upcomingBooking.setTotalPrice(new BigDecimal("849.95"));
        upcomingBooking.setBookingStatus(Booking.BookingStatus.CONFIRMED);
        upcomingBooking.setSpecialRequests("Extra pillow, High floor preferred");
        upcomingBooking.setUser(customerUser);
        upcomingBooking.setRoom(room302);
        bookingRepository.save(upcomingBooking);

        // Create payment for the upcoming booking
        Payment upcomingPayment = new Payment();
        upcomingPayment.setAmount(new BigDecimal("849.95"));
        upcomingPayment.setPaymentMethod("Credit Card");
        upcomingPayment.setTransactionId("TXN123456789");
        upcomingPayment.setPaymentStatus(Payment.PaymentStatus.COMPLETED);
        upcomingPayment.setPaymentDate(LocalDateTime.now().minusDays(1));
        upcomingPayment.setCardLastDigits("1234");
        upcomingPayment.setBooking(upcomingBooking);
        paymentRepository.save(upcomingPayment);
    }
}