# SmartStay Hotel Booking API 🏨✨

![Build Status](https://img.shields.io/badge/build-passing-brightgreen)
![License](https://img.shields.io/badge/license-MIT-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.12-brightgreen)
![Java](https://img.shields.io/badge/Java-17-blue)

Welcome to the backend API for **SmartStay**, a comprehensive hotel booking system built with Spring Boot. This API provides functionalities for users to search, book, review hotels, and for administrators to manage the platform's data.

---

## 🌟 Key Features

*   ✅ **Authentication & Authorization:** Secure user registration and login using JWT (JSON Web Tokens). Role-based access control (CUSTOMER, ADMIN).
*   ✅ **Hotel Management:** CRUD operations for hotels, including details like amenities, star rating, check-in/out times.
*   ✅ **Room Management:** Manage rooms within hotels, linking them to specific types and managing availability status.
*   ✅ **Room Type Definitions:** Define different room types (Standard, Deluxe, Suite, etc.) with base pricing and capacity.
*   ✅ **Availability Search:** Find available rooms across hotels based on dates, location, capacity, and room type.
*   ✅ **Booking System:** Create, view, and cancel bookings. Admin management of booking statuses.
*   ✅ **Payment Processing (Simulated):** Handle payment information linked to bookings, including simulated processing and refunds.
*   ✅ **Review System:** Allow users to submit reviews for completed stays. Admin approval and response capabilities.
*   ✅ **User Profile Management:** Users can view and update their profile information.
*   ✅ **Data Seeding:** Includes a `DataLoader` (active in `dev` profile) to populate the database with sample data for development.

---

## 💻 Technology Stack

*   **Framework:** Spring Boot 3.2.12
*   **Language:** Java 17 ☕
*   **Data Persistence:** Spring Data JPA / Hibernate
*   **Database:**
    *   H2 (In-Memory for Dev/Test) 💾
    *   PostgreSQL (Intended for Production) 🐘
*   **Security:** Spring Security, JWT (jjwt library) 🔒
*   **Build Tool:** Maven  M
*   **API:** RESTful principles
*   **Utilities:** Lombok

---

## 🚀 Getting Started

Follow these instructions to get the project up and running on your local machine for development and testing purposes.

### Prerequisites

*   **Java Development Kit (JDK):** Version 17 or later.
*   **Maven:** Build tool (usually comes with IDEs or install separately).
*   **Git:** Version control system.
*   **(Optional) Database Client:** Like DBeaver, pgAdmin, etc., if you want to inspect the database.

### Installation & Setup

1.  **Clone the repository:**
    ```bash
    git clone <your-repository-url>
    cd hotel-booking
    ```

2.  **Build the project:**
    This will download dependencies and compile the code.
    ```bash
    mvn clean install
    ```

---

## ▶️ Running the Application

The application uses Spring Profiles to manage configurations. The `dev` profile is configured by default in `application.properties` to enable the `DataLoader` and use the H2 in-memory database.

1.  **Run using Maven:**
    ```bash
    # This command explicitly activates the 'dev' profile
    mvn spring-boot:run -Dspring-boot.run.profiles=dev
    ```
    *(Alternatively, if `dev` is set as default in `application.properties`, `mvn spring-boot:run` might suffice)*

2.  **Accessing the Application:**
    *   The API will be available at `http://localhost:8080` (or the port configured in `application.properties`).
    *   The H2 database console (when using the `dev` or `test` profile) is available at `http://localhost:8080/h2-console`.
        *   **JDBC URL:** `jdbc:h2:mem:smartstaydb` (for dev) or `jdbc:h2:mem:testdb` (for test)
        *   **Username:** `sa`
        *   **Password:** (leave blank)

---

## 🗺️ API Endpoints Overview

The API follows RESTful conventions. The base path for most endpoints is `/api`.

*   `/api/auth/` - Authentication (Login, Register)
*   `/api/hotels/` - Hotel information and search
*   `/api/rooms/` - Room details and availability search
*   `/api/bookings/` - Booking creation and management
*   `/api/payments/` - Payment processing and information
*   `/api/reviews/` - Review submission and retrieval
*   `/api/users/` - User profile management

*Note: For detailed API documentation, consider integrating Swagger/OpenAPI in the future.*

---

## ⚙️ Configuration

Key configuration settings are located in `src/main/resources/application.properties`.

*   **Server Port:** `server.port` (default: 8080)
*   **Database:** `spring.datasource.*` properties configure the database connection. H2 is used by default for `dev` and `test` profiles. PostgreSQL settings are commented out for reference.
    *   ⚠️ **Important:** `spring.jpa.hibernate.ddl-auto=create-drop` is used for H2. **DO NOT** use this in production. Use `validate` or `none` and manage schema changes with migration tools like Flyway or Liquibase for production databases.
*   **JWT:**
    *   `app.jwt.secret`: The secret key for signing JWTs. **This should be externalized and kept secure in production!**
    *   `app.jwt.expiration-ms`: Token validity duration (default: 24 hours).
*   **Profiles:** `spring.profiles.active` determines the active profile(s). `dev` enables the `DataLoader`.

---

## 🔒 Security

*   Authentication is handled via JWT. Users obtain a token upon successful login (`/api/auth/login`).
*   The JWT must be included in the `Authorization` header for subsequent requests to protected endpoints (e.g., `Authorization: Bearer <your_jwt_token>`).
*   Endpoints are secured based on roles (CUSTOMER, ADMIN) using Spring Security annotations (`@PreAuthorize`) and configuration in `SecurityConfig.java`.
*   Passwords are securely hashed using `BCryptPasswordEncoder`.
*   CORS is configured in `CorsConfig.java` to allow requests from typical frontend development ports (localhost:3000, 8081, 4200). Adjust as needed.

---

## 🧪 Testing

The project includes unit and integration tests.

*   **Run all tests:**
    ```bash
    mvn test
    ```
*   Tests use the `test` profile (`src/test/resources/application-test.properties`), which configures a separate H2 in-memory database (`jdbc:h2:mem:testdb`).
*   **Current Coverage:** Basic tests for `UserService` and `AuthController` are provided. **More tests are needed** to cover services, controllers, and business logic thoroughly.

---

## 🤝 Contributing

Contributions are welcome! Please follow standard Git workflow:

1.  Fork the repository.
2.  Create a new branch (`git checkout -b feature/your-feature-name`).
3.  Make your changes.
4.  Write tests for your changes.
5.  Ensure all tests pass (`mvn test`).
6.  Commit your changes (`git commit -m 'Add some feature'`).
7.  Push to the branch (`git push origin feature/your-feature-name`).
8.  Open a Pull Request.

---

## 📜 License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details.

---

*Happy Coding!* 😊
