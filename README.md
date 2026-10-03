# Gym Booking API

A REST API for a gym's class schedule. Staff can schedule classes, members can register, and members can book into classes. The API enforces the booking rules a real gym needs: classes have a capacity, a member can only hold one booking per class, and classes can't be booked or cancelled once they've started.

## Tech Stack

- Java 21
- Spring Boot 3 (Web, Data JPA, Validation)
- H2 in-memory database
- springdoc-openapi (Swagger UI)
- JUnit 5, Mockito, MockMvc
- Maven

## Running Locally

Requirements: JDK 21.

**IntelliJ IDEA:** open the project folder, wait for Maven to import, then run `GymBookingApplication`.

**Command line:**

```bash
mvn spring-boot:run
```

The API starts on `http://localhost:8080` and loads sample classes, members and bookings on startup.

| Tool | URL |
|---|---|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| H2 console | http://localhost:8080/h2-console (JDBC URL `jdbc:h2:mem:gymdb`, user `sa`, empty password) |

A Postman collection with every request is included in `postman/gym-booking-api.postman_collection.json`.

## API

### Classes

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/classes` | Schedule a class |
| GET | `/api/classes` | List classes ordered by start time, with remaining spots |
| GET | `/api/classes/{id}` | Get one class |
| PUT | `/api/classes/{id}` | Update a class |
| DELETE | `/api/classes/{id}` | Delete a class that has no active bookings |
| GET | `/api/classes/{id}/bookings` | List bookings for a class |

### Members

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/members` | Register a member |
| GET | `/api/members` | List members |
| GET | `/api/members/{id}` | Get one member |
| GET | `/api/members/{id}/bookings` | List a member's bookings |

### Bookings

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/bookings` | Book a member into a class |
| GET | `/api/bookings/{id}` | Get one booking |
| PATCH | `/api/bookings/{id}/cancel` | Cancel a booking |

### Example

```http
POST /api/bookings
Content-Type: application/json

{ "memberId": 3, "classId": 2 }
```

```json
{
  "id": 4,
  "memberId": 3,
  "memberName": "Yousef Karim",
  "classId": 2,
  "className": "HIIT Blast",
  "classStartTime": "2026-10-04T18:00:00",
  "status": "ACTIVE",
  "bookedAt": "2026-10-03T17:40:12.481"
}
```

Trying to book a class that is already full:

```json
{
  "timestamp": "2026-10-03T14:41:03.118Z",
  "status": 409,
  "error": "Conflict",
  "message": "Class 'HIIT Blast' is fully booked",
  "path": "/api/bookings"
}
```

## Business Rules

| Rule | Response |
|---|---|
| A class cannot be booked beyond its capacity | `409 Conflict` |
| A member cannot hold two active bookings for the same class | `409 Conflict` |
| Classes that have already started cannot be booked or cancelled | `409 Conflict` |
| A booking cannot be cancelled twice | `409 Conflict` |
| Capacity cannot be reduced below the current number of bookings | `409 Conflict` |
| A class with active bookings cannot be deleted | `409 Conflict` |
| Member emails are unique (case-insensitive) | `409 Conflict` |
| Invalid input (blank names, bad emails, past start times, etc.) | `400 Bad Request` with per-field errors |
| Unknown IDs | `404 Not Found` |

## Design Notes

- **Layered architecture.** Controllers handle HTTP, services hold business logic, repositories handle persistence. Controllers never touch entities directly.
- **DTOs instead of entities in the API.** Requests and responses are Java records, so the database model can change without breaking clients.
- **Preventing overbooking under concurrency.** When booking, the class row is loaded with a pessimistic write lock (`SELECT ... FOR UPDATE`). Two simultaneous requests for the last spot are processed one after the other, so the capacity check can't be bypassed by a race condition.
- **Soft cancellation.** Cancelling marks a booking as `CANCELLED` instead of deleting it, which keeps booking history and frees the spot.
- **Centralized error handling.** A `@RestControllerAdvice` maps exceptions to a consistent JSON error format. All business rule violations extend a common `BusinessRuleException` and map to `409`.
- **Testable time.** Services read the current time from an injected `Clock`, so time-based rules are tested with a fixed clock.
- **No N+1 queries.** Booking lists load members and classes with entity graphs, and class listings fetch booking counts in a single grouped query.

## Tests

```bash
mvn test
```

- `BookingServiceTest`: unit tests for the booking rules using Mockito and a fixed clock.
- `BookingFlowIntegrationTest`: end-to-end tests through the HTTP layer with MockMvc against the H2 database.

## Project Structure

```
src/main/java/com/gymbooking
├── config        Clock, OpenAPI and sample data
├── controller    REST endpoints
├── dto           Request and response records
├── entity        JPA entities
├── exception     Custom exceptions and global error handler
├── repository    Spring Data JPA repositories
└── service       Business logic
```

## Possible Improvements

- Authentication with Spring Security so members can only manage their own bookings
- Waitlist that automatically books the next member when a spot frees up
- PostgreSQL with Flyway migrations for a persistent database
- Pagination and filtering for class listings
- Docker image and CI pipeline
