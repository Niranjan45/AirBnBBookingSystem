# Airbnb-Like Property Booking Backend

A full-featured REST API backend built with **Spring Boot 3** that simulates a property rental platform — allowing hosts to list properties and guests to search, book, and review them.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Framework | Spring Boot 3.2 |
| ORM | Spring Data JPA / Hibernate |
| Database | MySQL 8 (PostgreSQL compatible) |
| Security | Spring Security + JWT |
| Docs | Swagger / OpenAPI 3 |
| Build | Maven |
| Java | Java 17 |

---

## Project Structure

```
src/main/java/com/airbnb/booking/
├── AirbnbBookingApplication.java
├── config/
│   ├── SecurityConfig.java          # JWT security filter chain
│   └── SwaggerConfig.java           # OpenAPI / Swagger setup
├── controller/
│   ├── AuthController.java          # Register & Login
│   ├── PropertyController.java      # CRUD + search + availability
│   ├── BookingController.java       # Booking lifecycle
│   └── ReviewController.java        # Property reviews
├── dto/
│   ├── request/                     # Input DTOs with validation
│   └── response/                    # Output DTOs
├── entity/
│   ├── User.java
│   ├── Property.java
│   ├── PropertyAvailability.java
│   ├── Booking.java
│   └── Review.java
├── enums/
│   ├── Role.java                    # HOST, GUEST
│   └── BookingStatus.java           # REQUESTED, CONFIRMED, CANCELLED, COMPLETED
├── exception/
│   ├── GlobalExceptionHandler.java  # Centralized error handling
│   └── *.java                       # Custom exception classes
├── repository/                      # Spring Data JPA repositories
├── security/
│   ├── JwtUtil.java                 # Token generation & validation
│   ├── JwtAuthFilter.java           # Request filter
│   └── UserDetailsServiceImpl.java
└── service/
    ├── *.java                       # Service interfaces
    └── impl/                        # Service implementations
```

---

## Database Schema (ERD Summary)

```
users           →  properties      (host_id FK)
users           →  bookings        (guest_id FK)
properties      →  bookings        (property_id FK)
properties      →  property_availability (property_id FK)
properties      →  reviews         (property_id FK)
users           →  reviews         (guest_id FK)
```

---

## Setup Instructions

### Prerequisites
- Java 17+
- Maven 3.8+
- MySQL 8.0+

### 1. Clone the repository
```bash
git clone https://github.com/your-repo/airbnb-booking.git
cd airbnb-booking
```

### 2. Configure the database
Create the MySQL database:
```sql
CREATE DATABASE airbnb_db;
```

Edit `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/airbnb_db?createDatabaseIfNotExist=true
spring.datasource.username=YOUR_DB_USER
spring.datasource.password=YOUR_DB_PASSWORD
```

> **PostgreSQL?** Swap the dependency in `pom.xml` and update the dialect to `PostgreSQLDialect`.

### 3. Build & Run
```bash
mvn clean install
mvn spring-boot:run
```

The server starts at: `http://localhost:8080`

---

## API Documentation

Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

OpenAPI JSON: [http://localhost:8080/api-docs](http://localhost:8080/api-docs)

---

## API Endpoints Reference

### Auth
| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| POST | `/api/auth/register` | Public | Register as HOST or GUEST |
| POST | `/api/auth/login` | Public | Login and get JWT token |

### Properties
| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| POST | `/api/properties` | HOST | Create a property listing |
| PUT | `/api/properties/{id}` | HOST | Update property details |
| DELETE | `/api/properties/{id}` | HOST | Delete a property |
| GET | `/api/properties` | Public | Search properties (location, dates, price) |
| GET | `/api/properties/{id}` | Public | Get property details |
| GET | `/api/properties/popular` | Public | Get popular properties |
| GET | `/api/properties/my-listings` | HOST | Get host's own properties |
| POST | `/api/properties/{id}/availability` | HOST | Set availability dates |
| GET | `/api/properties/{id}/availability` | Public | Get availability for a property |

### Bookings
| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| POST | `/api/bookings` | GUEST | Create a booking |
| PUT | `/api/bookings/{id}/cancel` | AUTH | Cancel a booking |
| GET | `/api/bookings/user/{userId}` | AUTH | Get bookings by user ID |
| GET | `/api/bookings/my-bookings` | AUTH | Get your bookings |
| GET | `/api/bookings/host` | HOST | Get all bookings for host's properties |
| GET | `/api/bookings/property/{propertyId}` | AUTH | Get bookings for a property |
| GET | `/api/bookings/property/{propertyId}/stats` | AUTH | Booking statistics |

### Reviews
| Method | Endpoint | Access | Description |
|--------|----------|--------|-------------|
| POST | `/api/reviews` | GUEST | Add review (requires completed stay) |
| GET | `/api/reviews/property/{propertyId}` | Public | Get all reviews for a property |

---

## Authentication

All protected endpoints require a `Bearer` JWT token in the `Authorization` header:
```
Authorization: Bearer <your_jwt_token>
```

Get a token by calling `POST /api/auth/login`.

---

## Business Rules

- **No double booking**: The system checks for overlapping active bookings before confirming. A database-level query validates that no `CONFIRMED` or `REQUESTED` booking exists for the same property and overlapping date range.
- **Review eligibility**: A guest can only review a property after their booking status is `COMPLETED`.
- **Price calculation**: `total_price = price_per_night × number_of_nights`
- **Role-based access**: Hosts manage properties; Guests create bookings. Both can cancel bookings.
- **Hosts cannot self-book**: A host is blocked from booking their own property.

---

## Running Tests

```bash
mvn test
```

---

## Sample Postman Workflow

1. **Register** a HOST → `POST /api/auth/register` `{ "role": "HOST", ... }`
2. **Register** a GUEST → `POST /api/auth/register` `{ "role": "GUEST", ... }`
3. **Login** as HOST → `POST /api/auth/login` → copy JWT
4. **Create property** → `POST /api/properties` (with HOST JWT)
5. **Set availability** → `POST /api/properties/{id}/availability`
6. **Login** as GUEST → `POST /api/auth/login` → copy JWT
7. **Search properties** → `GET /api/properties?location=Hyderabad&startDate=2025-10-01&endDate=2025-10-05`
8. **Book property** → `POST /api/bookings` (with GUEST JWT)
9. **View bookings** → `GET /api/bookings/user/{guestId}`
10. **Cancel booking** → `PUT /api/bookings/{id}/cancel`
