# Booking Service

Booking Service is a Spring Boot microservice for the Event Ticketing and Venue Management System. It owns the booking/reservation part of the domain: creating ticket bookings, storing booking records, listing customer bookings, cancelling reservations, and initiating payment for a booking.

This service is designed as an independent microservice with its own PostgreSQL database, Dockerfile, Docker Compose setup, OpenAPI documentation, and automated tests.

## Responsibilities

The Booking Service is responsible for:

- Creating a booking for one or more ticket types
- Reserving ticket quantities through the Ticketing Service dependency
- Persisting booking and booking item data in its own database
- Returning booking details by booking id
- Returning all bookings for a customer
- Cancelling a booking and releasing reserved tickets
- Initiating payment through the Payment Service dependency

The service does not own event details, ticket inventory, user accounts, or payment processing. Those belong to other bounded contexts/microservices.

## Architecture

The service follows a layered Spring Boot architecture with a simple Domain-Driven Design structure.

```text
Controller layer
    BookingController
        |
Service layer
    BookingService
        |
Domain model
    Booking, BookingItem, Money, BookingStatus
        |
Repository layer
    BookingRepository
        |
Database
    PostgreSQL booking_db
```

### DDD Mapping

| DDD Concept | Implementation |
| --- | --- |
| Aggregate Root | `Booking` |
| Entity | `BookingItem` |
| Value Object | `Money` |
| Repository | `BookingRepository` |
| Domain Status | `BookingStatus` |
| Application Service | `BookingService` |

A `Booking` owns its `BookingItem` objects. The total amount and reservation expiry are calculated inside the booking domain model.

## Technology Stack

- Java 17
- Spring Boot 3.5.x
- Spring Web
- Spring Data JPA
- Jakarta Validation
- PostgreSQL
- Springdoc OpenAPI / Swagger UI
- Docker and Docker Compose
- JUnit 5, Mockito, Spring MockMvc

## API Endpoints

| Method | Endpoint | Description |
| --- | --- | --- |
| `POST` | `/bookings` | Create a new booking |
| `GET` | `/bookings/{id}` | Get booking details by booking id |
| `DELETE` | `/bookings/{id}` | Cancel a booking |
| `POST` | `/bookings/{id}/pay` | Initiate payment for a booking |
| `GET` | `/users/{userId}/bookings` | Get all bookings for a customer |

## Run With Docker Compose

From the project root:

```bash
cd /Users/parvesshikder/Desktop/booking-service
docker compose up -d --build
```

This starts:

- `booking-service` on port `8081`
- `booking-db` PostgreSQL database on host port `5433`

Check running containers:

```bash
docker compose ps
```

Stop the service:

```bash
docker compose down
```

Stop and remove the database volume:

```bash
docker compose down -v
```

## Health Check

```bash
curl http://localhost:8081/actuator/health
```

Expected response:

```json
{"status":"UP"}
```

## Swagger / OpenAPI

Swagger UI is available at:

```text
http://localhost:8081/swagger-ui.html
```

Raw OpenAPI JSON is available at:

```text
http://localhost:8081/v3/api-docs
```

## Security / JWT Authentication

Booking endpoints are protected with Spring Security JWT bearer-token authentication.

Public endpoints:

- `GET /actuator/health`
- Swagger UI and OpenAPI docs

Protected endpoints:

- `POST /bookings`
- `GET /bookings/{id}`
- `DELETE /bookings/{id}`
- `POST /bookings/{id}/pay`
- `GET /users/{userId}/bookings`

Clients must send a JWT in the `Authorization` header:

```text
Authorization: Bearer <jwt-token>
```

This service is configured as a resource server. That means it validates JWTs issued by an auth/user service; it does not manage user registration or passwords itself.

Local development JWT configuration is controlled by environment variables:

```yaml
JWT_ISSUER: booking-service
JWT_SECRET: booking-service-development-secret-key-32
```


## Example Requests

### Create Booking

```bash
curl -X POST http://localhost:8081/bookings \
  -H "Authorization: Bearer <jwt-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1,
    "eventId": 10,
    "currency": "EUR",
    "items": [
      {
        "ticketTypeId": 5,
        "quantity": 2,
        "unitPrice": 25.00
      }
    ]
  }'
```

Example response:

```json
{
  "bookingId": 1,
  "customerId": 1,
  "eventId": 10,
  "status": "PENDING",
  "totalAmount": 50.00,
  "currency": "EUR",
  "items": [
    {
      "bookingItemId": 1,
      "ticketTypeId": 5,
      "quantity": 2,
      "unitPrice": 25.00,
      "currency": "EUR"
    }
  ]
}
```

### Get Booking By Id

```bash
curl http://localhost:8081/bookings/1 \
  -H "Authorization: Bearer <jwt-token>"
```

### Get Customer Bookings

```bash
curl http://localhost:8081/users/1/bookings \
  -H "Authorization: Bearer <jwt-token>"
```

### Initiate Payment

```bash
curl -X POST http://localhost:8081/bookings/1/pay \
  -H "Authorization: Bearer <jwt-token>"
```

### Cancel Booking

```bash
curl -X DELETE http://localhost:8081/bookings/1 \
  -H "Authorization: Bearer <jwt-token>"
```

## Validation and Error Handling

The service validates incoming requests using Jakarta Validation.

Examples of validation rules:

- `customerId` is required
- `eventId` is required
- `currency` must not be blank
- `items` must not be empty
- `ticketTypeId` is required
- `quantity` must be at least `1`
- `unitPrice` must be at least `0.00`

Common error responses:

| Status | Meaning |
| --- | --- |
| `400 Bad Request` | Invalid request or invalid booking state |
| `404 Not Found` | Booking was not found |
| `503 Service Unavailable` | Dependent service is unavailable |

## Service Dependencies

Booking Service communicates with two external microservices:

| Dependency | Purpose | Default URL |
| --- | --- | --- |
| Ticketing Service | Reserve and release ticket quantities | `http://localhost:8083` |
| Payment Service | Create/initiate payment | `http://localhost:8082` |

For Checkpoint 1, these dependencies can run in mock mode. Mock mode allows Booking Service to be demonstrated independently before the other services are fully integrated.

Configuration is controlled in `application.yaml`:

```yaml
services:
  ticketing:
    base-url: ${TICKETING_SERVICE_URL:http://localhost:8083}
    mock-enabled: ${TICKETING_SERVICE_MOCK_ENABLED:true}
  payment:
    base-url: ${PAYMENT_SERVICE_URL:http://localhost:8082}
    mock-enabled: ${PAYMENT_SERVICE_MOCK_ENABLED:true}
```

When real services are available, set mock mode to `false` and provide the real service URLs.

## Database

This service owns its own PostgreSQL database.

Default Docker Compose database configuration:

| Property | Value |
| --- | --- |
| Database | `booking_db` |
| Username | `booking_user` |
| Password | `booking_pass` |
| Container port | `5432` |
| Host port | `5433` |

The database is separate from other microservices, which follows the microservice principle of database ownership per service.

## Running Tests

Run tests with Docker, without installing Maven locally:

```bash
docker run --rm \
  -v /Users/parvesshikder/Desktop/booking-service:/workspace \
  -w /workspace \
  maven:3.9.9-eclipse-temurin-17 \
  mvn -B test
```

The current test suite includes:

- A `@WebMvcTest` controller test for successful booking creation
- A controller validation/error test
- A service test verifying booking creation, persistence interaction, and ticket reservation dependency call


Useful commands:

```bash
docker compose up -d --build
curl http://localhost:8081/actuator/health
curl http://localhost:8081/v3/api-docs
docker run --rm -v /Users/parvesshikder/Desktop/booking-service:/workspace -w /workspace maven:3.9.9-eclipse-temurin-17 mvn -B test
```

