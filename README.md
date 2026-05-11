# Booking Service

The Booking Service manages ticket reservations for the event ticketing system.

It is responsible for creating bookings, storing booking items, cancelling reservations, starting payment, and confirming a booking after payment is completed.

## What It Does

- Creates a booking for a customer and event.
- Stores selected ticket types, quantity, price, and total amount.
- Keeps booking status such as `PENDING`, `CONFIRMED`, and `CANCELLED`.
- Reserves or releases tickets through the ticketing dependency.
- Calls the Payment Service when a booking needs to be paid.
- Exposes health and Swagger endpoints for development.

For now, the ticketing dependency can run in mock mode, so this service can be tested without building the ticketing service.

## Main Endpoints

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `POST` | `/bookings` | Create a booking |
| `GET` | `/bookings/{id}` | Get one booking |
| `DELETE` | `/bookings/{id}` | Cancel a booking |
| `POST` | `/bookings/{id}/pay` | Start payment for a booking |
| `POST` | `/bookings/{id}/confirm` | Confirm a booking after payment |
| `GET` | `/users/{userId}/bookings` | List bookings for one user |
| `GET` | `/actuator/health` | Check if service is running |

## Run With Docker Compose

From this folder:

```bash
docker compose up --build
```

The service runs on:

```text
http://localhost:8081
```

PostgreSQL runs on host port:

```text
5433
```

## Run With The Full System

From the infra folder:

```bash
cd ../infra
docker compose up --build
```

This is the recommended way because it also starts the API Gateway, Payment Service, frontend, and databases.

## Swagger

Open:

```text
http://localhost:8081/swagger-ui.html
```

## Health Check

```bash
curl http://localhost:8081/actuator/health
```

Expected:

```json
{"status":"UP"}
```

## Example Booking Request

```bash
curl -X POST http://localhost:8081/bookings \
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

## Important Environment Variables

| Variable | Default | Meaning |
| --- | --- | --- |
| `SERVER_PORT` | `8081` | Service port |
| `SPRING_DATASOURCE_URL` | local booking DB | PostgreSQL connection |
| `SPRING_DATASOURCE_USERNAME` | `booking_user` | DB user |
| `SPRING_DATASOURCE_PASSWORD` | `booking_pass` | DB password |
| `PAYMENT_SERVICE_URL` | `http://localhost:8082` | Payment Service URL |
| `PAYMENT_SERVICE_MOCK_ENABLED` | `true` | Use fake payment response |
| `TICKETING_SERVICE_MOCK_ENABLED` | `true` | Skip real ticketing calls |

## Tests

```bash
./mvnw test
```
