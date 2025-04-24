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
import java.util.*;

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
        List<User> users = createUsers();

        // Get reference to admin and customer users
        User adminUser = users.get(0);
        User customerUser = users.get(1);

        // Create room types
        Map<String, RoomType> roomTypes = createRoomTypes();

        // Create hotels
        List<Hotel> hotels = createHotels();

        // Create rooms for each hotel
        Map<Hotel, List<Room>> hotelRooms = createRooms(hotels, roomTypes);

        // Create bookings, payments, and reviews
        createBookingsPaymentsAndReviews(users, hotels, hotelRooms);

        System.out.println("Sample data loaded successfully!");
    }

    private List<User> createUsers() {
        List<User> users = new ArrayList<>();

        // Admin user
        User adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setPassword(passwordEncoder.encode("admin123"));
        adminUser.setEmail("admin@smartstay.com");
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setPhoneNumber("1234567890");
        adminUser.setAddress("123 Admin St");
        adminUser.setRole(User.UserRole.ADMIN);
        users.add(userRepository.save(adminUser));

        // Regular customer
        User customerUser = new User();
        customerUser.setUsername("customer");
        customerUser.setPassword(passwordEncoder.encode("customer123"));
        customerUser.setEmail("customer@example.com");
        customerUser.setFirstName("John");
        customerUser.setLastName("Doe");
        customerUser.setPhoneNumber("9876543210");
        customerUser.setAddress("456 Customer Ave");
        customerUser.setRole(User.UserRole.CUSTOMER);
        users.add(userRepository.save(customerUser));

        // Additional customers
        String[] firstNames = { "Alice", "Bob", "Charlie", "David", "Emma", "Frank", "Grace", "Henry" };
        String[] lastNames = { "Smith", "Johnson", "Williams", "Jones", "Brown", "Davis", "Miller", "Wilson" };

        for (int i = 0; i < 8; i++) {
            User user = new User();
            user.setUsername("user" + (i + 1));
            user.setPassword(passwordEncoder.encode("password" + (i + 1)));
            user.setEmail("user" + (i + 1) + "@example.com");
            user.setFirstName(firstNames[i]);
            user.setLastName(lastNames[i]);
            user.setPhoneNumber("555" + String.format("%07d", (i + 1)));
            user.setAddress((i + 100) + " Main St");
            user.setRole(User.UserRole.CUSTOMER);
            users.add(userRepository.save(user));
        }

        return users;
    }

    private Map<String, RoomType> createRoomTypes() {
        Map<String, RoomType> roomTypes = new HashMap<>();

        // Standard room type
        RoomType standardType = new RoomType();
        standardType.setName("Standard");
        standardType.setDescription("Comfortable room with queen-size bed");
        standardType.setBaseCapacity(2);
        standardType.setMaxCapacity(2);
        standardType.setBasePrice(new BigDecimal("199.99"));
        standardType.setAmenities("Queen bed, 42-inch TV, Coffee maker, Free Wi-Fi");
        roomTypes.put("standard", roomTypeRepository.save(standardType));

        // Deluxe room type
        RoomType deluxeType = new RoomType();
        deluxeType.setName("Deluxe");
        deluxeType.setDescription("Spacious room with king-size bed and city view");
        deluxeType.setBaseCapacity(2);
        deluxeType.setMaxCapacity(3);
        deluxeType.setBasePrice(new BigDecimal("299.99"));
        deluxeType.setAmenities("King bed, 55-inch TV, Mini bar, Coffee machine, Safe, Work desk, Free Wi-Fi");
        roomTypes.put("deluxe", roomTypeRepository.save(deluxeType));

        // Suite room type
        RoomType suiteType = new RoomType();
        suiteType.setName("Suite");
        suiteType.setDescription("Luxury suite with separate living room and bedroom");
        suiteType.setBaseCapacity(2);
        suiteType.setMaxCapacity(4);
        suiteType.setBasePrice(new BigDecimal("499.99"));
        suiteType.setAmenities(
                "King bed, Living room, Jacuzzi, 65-inch TV, Mini bar, Coffee machine, Safe, Work desk, Free Wi-Fi, Bathrobes");
        roomTypes.put("suite", roomTypeRepository.save(suiteType));

        // Family room type
        RoomType familyType = new RoomType();
        familyType.setName("Family");
        familyType.setDescription("Spacious room ideal for families with children");
        familyType.setBaseCapacity(3);
        familyType.setMaxCapacity(5);
        familyType.setBasePrice(new BigDecimal("349.99"));
        familyType.setAmenities("King bed, Bunk beds, 50-inch TV, Mini fridge, Coffee maker, Safe, Free Wi-Fi");
        roomTypes.put("family", roomTypeRepository.save(familyType));

        // Executive room type
        RoomType executiveType = new RoomType();
        executiveType.setName("Executive");
        executiveType.setDescription("Premium room with executive lounge access");
        executiveType.setBaseCapacity(2);
        executiveType.setMaxCapacity(2);
        executiveType.setBasePrice(new BigDecimal("399.99"));
        executiveType.setAmenities(
                "King bed, 60-inch TV, Mini bar, Premium coffee machine, Safe, Work desk, Free Wi-Fi, Lounge access");
        roomTypes.put("executive", roomTypeRepository.save(executiveType));

        return roomTypes;
    }

    private List<Hotel> createHotels() {
        List<Hotel> hotels = new ArrayList<>();

        // Hotel 1 - New York
        Hotel hotel1 = new Hotel();
        hotel1.setName("SmartStay Grand New York");
        hotel1.setDescription(
                "A luxury hotel in the heart of Manhattan with stunning views of the city skyline. Features gourmet dining, a rooftop pool, and world-class spa services.");
        hotel1.setAddress("123 Broadway");
        hotel1.setCity("New York");
        hotel1.setCountry("USA");
        hotel1.setPostalCode("10001");
        hotel1.setStarRating(5);
        hotel1.setAmenities(
                "Pool, Spa, Gym, Restaurant, Bar, Wi-Fi, Room Service, Concierge, Business Center, Valet Parking");
        hotel1.setCheckInTime(LocalTime.of(15, 0));
        hotel1.setCheckOutTime(LocalTime.of(11, 0));
        hotel1.setStatus(Hotel.HotelStatus.ACTIVE);
        hotels.add(hotelRepository.save(hotel1));

        // Hotel 2 - Chicago
        Hotel hotel2 = new Hotel();
        hotel2.setName("SmartStay Express Chicago");
        hotel2.setDescription(
                "A budget-friendly hotel in downtown Chicago offering comfortable accommodations for business and leisure travelers.");
        hotel2.setAddress("789 Business Park");
        hotel2.setCity("Chicago");
        hotel2.setCountry("USA");
        hotel2.setPostalCode("60601");
        hotel2.setStarRating(3);
        hotel2.setAmenities("Wi-Fi, Breakfast, Business Center, Parking");
        hotel2.setCheckInTime(LocalTime.of(15, 0));
        hotel2.setCheckOutTime(LocalTime.of(12, 0));
        hotel2.setStatus(Hotel.HotelStatus.ACTIVE);
        hotels.add(hotelRepository.save(hotel2));

        // Hotel 3 - Los Angeles
        Hotel hotel3 = new Hotel();
        hotel3.setName("SmartStay Resort LA");
        hotel3.setDescription(
                "A luxurious beachfront resort in Los Angeles offering the perfect blend of relaxation and entertainment with stunning ocean views.");
        hotel3.setAddress("456 Ocean Drive");
        hotel3.setCity("Los Angeles");
        hotel3.setCountry("USA");
        hotel3.setPostalCode("90001");
        hotel3.setStarRating(4);
        hotel3.setAmenities("Beach Access, Pool, Spa, Gym, Restaurant, Bar, Wi-Fi, Room Service");
        hotel3.setCheckInTime(LocalTime.of(16, 0));
        hotel3.setCheckOutTime(LocalTime.of(10, 0));
        hotel3.setStatus(Hotel.HotelStatus.ACTIVE);
        hotels.add(hotelRepository.save(hotel3));

        // Hotel 4 - Miami
        Hotel hotel4 = new Hotel();
        hotel4.setName("SmartStay Beach Miami");
        hotel4.setDescription(
                "Experience the vibrant Miami Beach lifestyle at our trendy beachfront hotel with art deco design and lively atmosphere.");
        hotel4.setAddress("789 Collins Avenue");
        hotel4.setCity("Miami");
        hotel4.setCountry("USA");
        hotel4.setPostalCode("33139");
        hotel4.setStarRating(4);
        hotel4.setAmenities("Beach Access, Pool, Gym, Restaurant, Nightclub, Wi-Fi, Room Service");
        hotel4.setCheckInTime(LocalTime.of(15, 0));
        hotel4.setCheckOutTime(LocalTime.of(11, 0));
        hotel4.setStatus(Hotel.HotelStatus.ACTIVE);
        hotels.add(hotelRepository.save(hotel4));

        // Hotel 5 - San Francisco
        Hotel hotel5 = new Hotel();
        hotel5.setName("SmartStay Bay View");
        hotel5.setDescription(
                "Boutique hotel in the heart of San Francisco with stunning views of the Golden Gate Bridge and personalized service.");
        hotel5.setAddress("555 Bay Street");
        hotel5.setCity("San Francisco");
        hotel5.setCountry("USA");
        hotel5.setPostalCode("94133");
        hotel5.setStarRating(4);
        hotel5.setAmenities("Fine Dining, Wine Bar, Business Center, Free Bicycles, Wi-Fi, Concierge");
        hotel5.setCheckInTime(LocalTime.of(14, 0));
        hotel5.setCheckOutTime(LocalTime.of(12, 0));
        hotel5.setStatus(Hotel.HotelStatus.ACTIVE);
        hotels.add(hotelRepository.save(hotel5));

        // Hotel 6 - Las Vegas
        Hotel hotel6 = new Hotel();
        hotel6.setName("SmartStay Casino Resort");
        hotel6.setDescription(
                "Experience the ultimate Las Vegas entertainment with our casino resort featuring world-class shows, dining, and luxury accommodations.");
        hotel6.setAddress("777 The Strip");
        hotel6.setCity("Las Vegas");
        hotel6.setCountry("USA");
        hotel6.setPostalCode("89109");
        hotel6.setStarRating(5);
        hotel6.setAmenities(
                "Casino, Multiple Pools, Spa, Gym, Multiple Restaurants, Nightclub, Shows, Wi-Fi, Room Service");
        hotel6.setCheckInTime(LocalTime.of(16, 0));
        hotel6.setCheckOutTime(LocalTime.of(11, 0));
        hotel6.setStatus(Hotel.HotelStatus.ACTIVE);
        hotels.add(hotelRepository.save(hotel6));

        // Hotel 7 - Under maintenance
        Hotel hotel7 = new Hotel();
        hotel7.setName("SmartStay Downtown");
        hotel7.setDescription(
                "Currently undergoing renovations to provide even better accommodations and amenities for our guests.");
        hotel7.setAddress("123 Main Street");
        hotel7.setCity("Boston");
        hotel7.setCountry("USA");
        hotel7.setPostalCode("02108");
        hotel7.setStarRating(4);
        hotel7.setAmenities("Pool, Gym, Restaurant, Bar, Wi-Fi, Business Center");
        hotel7.setCheckInTime(LocalTime.of(15, 0));
        hotel7.setCheckOutTime(LocalTime.of(11, 0));
        hotel7.setStatus(Hotel.HotelStatus.MAINTENANCE);
        hotels.add(hotelRepository.save(hotel7));

        return hotels;
    }

    private Map<Hotel, List<Room>> createRooms(List<Hotel> hotels, Map<String, RoomType> roomTypes) {
        Map<Hotel, List<Room>> hotelRooms = new HashMap<>();
        Random random = new Random();

        for (Hotel hotel : hotels) {
            List<Room> rooms = new ArrayList<>();

            // Skip creating rooms for hotels under maintenance
            if (hotel.getStatus() == Hotel.HotelStatus.MAINTENANCE) {
                hotelRooms.put(hotel, rooms);
                continue;
            }

            // Number of floors depends on hotel star rating
            int floors = hotel.getStarRating() - 1;
            int roomsPerFloor = random.nextInt(3) + 3; // 3-5 rooms per floor

            for (int floor = 1; floor <= floors; floor++) {
                for (int roomNum = 1; roomNum <= roomsPerFloor; roomNum++) {
                    // Determine room type based on floor and room number
                    RoomType roomType;
                    String roomTypeKey;

                    if (floor == floors && roomNum == 1) { // Top floor, first room is always a suite
                        roomTypeKey = "suite";
                    } else if (floor > (floors / 2) && roomNum <= 2) { // Upper floors have more deluxe rooms
                        roomTypeKey = "deluxe";
                    } else if (floor == 1 && roomNum <= 2) { // First floor has family rooms
                        roomTypeKey = "family";
                    } else if (floor == floors && roomNum > roomsPerFloor - 2) { // Top floor, last rooms are executive
                        roomTypeKey = "executive";
                    } else {
                        roomTypeKey = "standard";
                    }

                    roomType = roomTypes.get(roomTypeKey);

                    // Create room
                    Room room = new Room();
                    room.setRoomNumber(String.format("%d%02d", floor, roomNum)); // e.g. 101, 102, etc.
                    room.setFloor(floor);
                    room.setCapacity(roomType.getBaseCapacity());

                    // Vary the price slightly from the base price
                    BigDecimal priceVariance = new BigDecimal(random.nextInt(50) - 25); // -25 to +25
                    room.setPricePerNight(roomType.getBasePrice().add(priceVariance));

                    // Description based on room type
                    String viewType = (roomNum % 2 == 0) ? "city" : "garden";
                    room.setDescription(roomType.getName() + " room with " + viewType + " view");

                    // Most rooms are available, but some are occupied or under maintenance
                    int statusRandom = random.nextInt(10);
                    if (statusRandom < 7) {
                        room.setStatus(Room.RoomStatus.AVAILABLE);
                    } else if (statusRandom < 9) {
                        room.setStatus(Room.RoomStatus.OCCUPIED);
                    } else {
                        room.setStatus(Room.RoomStatus.MAINTENANCE);
                    }

                    room.setHotel(hotel);
                    room.setRoomType(roomType);
                    rooms.add(roomRepository.save(room));
                }
            }

            hotelRooms.put(hotel, rooms);
        }

        return hotelRooms;
    }

    private void createBookingsPaymentsAndReviews(List<User> users, List<Hotel> hotels,
            Map<Hotel, List<Room>> hotelRooms) {
        Random random = new Random();

        // Generate random bookings
        for (int i = 0; i < 50; i++) {
            // Skip if i exceeds users size
            if (i % users.size() == 0 && i != 0) {
                continue;
            }

            // Pick a user (skip admin)
            User user = users.get((i % (users.size() - 1)) + 1);

            // Pick a hotel (skip maintenance hotels)
            Hotel hotel = null;
            List<Room> availableRooms = new ArrayList<>();
            while (availableRooms.isEmpty()) {
                int hotelIndex = random.nextInt(hotels.size());
                hotel = hotels.get(hotelIndex);

                if (hotel.getStatus() == Hotel.HotelStatus.ACTIVE) {
                    List<Room> hotelRoomList = hotelRooms.get(hotel);
                    for (Room room : hotelRoomList) {
                        if (room.getStatus() == Room.RoomStatus.AVAILABLE) {
                            availableRooms.add(room);
                        }
                    }
                }
            }

            // Pick a room
            Room room = availableRooms.get(random.nextInt(availableRooms.size()));

            // Determine booking timeframe
            LocalDate now = LocalDate.now();

            // 70% past bookings, 30% future bookings
            boolean isPastBooking = random.nextInt(10) < 7;

            LocalDate checkInDate;
            LocalDate checkOutDate;
            Booking.BookingStatus bookingStatus;

            if (isPastBooking) {
                // Past booking (1-90 days ago)
                int daysAgo = random.nextInt(90) + 1;
                checkInDate = now.minusDays(daysAgo);

                // Stay duration 1-7 days
                int stayDuration = random.nextInt(7) + 1;
                checkOutDate = checkInDate.plusDays(stayDuration);

                // All past bookings are either checked out or cancelled
                bookingStatus = (random.nextInt(10) < 8)
                        ? Booking.BookingStatus.CHECKED_OUT
                        : Booking.BookingStatus.CANCELLED;
            } else {
                // Future booking (1-90 days ahead)
                int daysAhead = random.nextInt(90) + 1;
                checkInDate = now.plusDays(daysAhead);

                // Stay duration 1-10 days
                int stayDuration = random.nextInt(10) + 1;
                checkOutDate = checkInDate.plusDays(stayDuration);

                // Future bookings are confirmed
                bookingStatus = Booking.BookingStatus.CONFIRMED;
            }

            // Create booking
            Booking booking = new Booking();
            booking.setBookingReference("BK" + String.format("%08d", i + 10000));
            booking.setCheckInDate(checkInDate);
            booking.setCheckOutDate(checkOutDate);
            booking.setAdults(random.nextInt(room.getCapacity()) + 1);
            booking.setChildren(random.nextInt(3));

            // Calculate total price (room price * number of days)
            long days = java.time.temporal.ChronoUnit.DAYS.between(checkInDate, checkOutDate);
            BigDecimal totalPrice = room.getPricePerNight().multiply(BigDecimal.valueOf(days));
            booking.setTotalPrice(totalPrice);

            booking.setBookingStatus(bookingStatus);

            // Add special requests for some bookings
            if (random.nextInt(5) == 0) {
                String[] requests = { "Late check-out", "Early check-in", "Extra pillows", "High floor",
                        "Away from elevator", "Connecting rooms" };
                booking.setSpecialRequests(requests[random.nextInt(requests.length)]);
            }

            booking.setUser(user);
            booking.setRoom(room);
            booking = bookingRepository.save(booking);

            // Create payment for most bookings
            if (random.nextInt(10) < 9) {
                Payment payment = new Payment();
                payment.setAmount(totalPrice);

                String[] paymentMethods = { "Credit Card", "Debit Card", "PayPal" };
                payment.setPaymentMethod(paymentMethods[random.nextInt(paymentMethods.length)]);

                payment.setTransactionId("TXN" + String.format("%09d", i + 100000));

                // Payment status depends on booking status
                if (bookingStatus == Booking.BookingStatus.CANCELLED) {
                    payment.setPaymentStatus(Payment.PaymentStatus.REFUNDED);
                } else {
                    payment.setPaymentStatus(Payment.PaymentStatus.COMPLETED);
                }

                // Payment date is before check-in
                payment.setPaymentDate(LocalDateTime.of(checkInDate.minusDays(random.nextInt(14) + 1),
                        LocalTime.of(random.nextInt(24), random.nextInt(60))));

                // Add card details for credit/debit payments
                if (!payment.getPaymentMethod().equals("PayPal")) {
                    payment.setCardLastDigits(String.format("%04d", random.nextInt(10000)));
                }

                payment.setBooking(booking);
                paymentRepository.save(payment);
            }

            // Add reviews for some completed bookings
            if (bookingStatus == Booking.BookingStatus.CHECKED_OUT && random.nextInt(10) < 7) {
                Review review = new Review();

                // Rating between 3-5 for most reviews, occasionally lower
                int rating = (random.nextInt(10) < 8) ? (random.nextInt(3) + 3) : (random.nextInt(2) + 1);
                review.setRating(rating);

                // Review comments based on rating
                String comment;
                if (rating >= 4) {
                    String[] goodComments = {
                            "Excellent stay! The room was clean and the staff was very friendly. Would definitely stay again.",
                            "Great location and amenities. The bed was very comfortable and the view was amazing.",
                            "Wonderful experience. The restaurant had delicious food and the spa services were top notch.",
                            "Exceeded expectations. Very professional staff and the room service was prompt.",
                            "Loved our stay here. The hotel is beautiful and the location is perfect for sightseeing."
                    };
                    comment = goodComments[random.nextInt(goodComments.length)];
                } else if (rating == 3) {
                    String[] averageComments = {
                            "Decent hotel but nothing special. Room was clean but a bit small.",
                            "Staff was friendly but slow at check-in. Location is good though.",
                            "Average experience. Breakfast was good but the room could use updating.",
                            "It was okay for the price. Convenient location but noisy at night.",
                            "Mixed feelings about our stay. Great location but some maintenance issues in the room."
                    };
                    comment = averageComments[random.nextInt(averageComments.length)];
                } else {
                    String[] badComments = {
                            "Disappointed with our stay. The room had cleanliness issues and the staff wasn't helpful.",
                            "Not worth the money. Small room with outdated furniture and noisy neighbors.",
                            "Would not recommend. The bathroom had plumbing problems and it wasn't fixed during our stay.",
                            "Below expectations. Slow service and the amenities mentioned online were not available.",
                            "Very unsatisfied. The air conditioning didn't work properly and it was too hot in the room."
                    };
                    comment = badComments[random.nextInt(badComments.length)];
                }
                review.setComment(comment);

                // Date posted is after check-out
                review.setDatePosted(LocalDateTime.of(checkOutDate.plusDays(random.nextInt(7) + 1),
                        LocalTime.of(random.nextInt(24), random.nextInt(60))));

                // Some reviews are approved, some pending
                review.setIsApproved(random.nextInt(10) < 7);

                // Add management response to some approved reviews
                if (review.getIsApproved() && random.nextInt(10) < 6) {
                    if (rating >= 4) {
                        String[] goodResponses = {
                                "Thank you for your wonderful review! We're delighted you enjoyed your stay and hope to welcome you back soon.",
                                "We appreciate your kind words and are so glad you had a great experience with us.",
                                "Thank you for sharing your positive feedback. It was a pleasure hosting you!"
                        };
                        review.setResponse(goodResponses[random.nextInt(goodResponses.length)]);
                    } else {
                        String[] improvementResponses = {
                                "Thank you for your feedback. We apologize for not meeting your expectations and will address the issues you mentioned.",
                                "We're sorry to hear about your experience. We've shared your comments with our team to improve our service.",
                                "Thank you for bringing these concerns to our attention. We would love to make it right during your next stay."
                        };
                        review.setResponse(improvementResponses[random.nextInt(improvementResponses.length)]);
                    }
                }

                review.setUser(user);
                review.setHotel(hotel);
                review.setBooking(booking);
                reviewRepository.save(review);
            }
        }
    }
}