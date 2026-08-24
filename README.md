# SmartPark — Intelligent Parking Management System

A REST API built for the SmartPark technical assessment. It lets clients register
parking lots and vehicles, check vehicles in/out, view live occupancy, and see
who is currently parked where. Vehicles that overstay a 15-minute limit are
automatically removed by a background scheduler.

---

## 1. Tech Stack

| Concern              | Choice                                            |
|-----------------------|---------------------------------------------------|
| Language              | Java 8                                             |
| Framework             | Spring Boot 2.7.18 (Web, Data JPA, Security, Validation) |
| Database              | H2 (in-memory), preloaded via `data.sql`          |
| Auth                  | JWT (jjwt 0.11.5), static username/password       |
| Build tool            | Maven                                              |
| Tests                 | JUnit 5 + Mockito + Spring MockMvc                 |



---

## 2. Project Structure

```
Hitachi-Assignment/
├── pom.xml
├── README.md
├── postman/
│   └── SmartPark.postman_collection.json
└── src
    ├── main
    │   ├── java/com/smartpark
    │   │   ├── SmartParkApplication.java
    │   │   ├── config        (JWT + Spring Security wiring)
    │   │   ├── controller    (REST endpoints)
    │   │   ├── dto           (request/response payloads)
    │   │   ├── entity        (JPA entities)
    │   │   ├── exception     (custom exceptions + global handler)
    │   │   ├── repository    (Spring Data JPA repositories)
    │   │   └── service       (business logic + scheduler)
    │   └── resources
    │       ├── application.yml
    │       └── data.sql      (preloaded lots & vehicles)
    └── test
        ├── java/com/smartpark
        │   ├── controller/SmartParkApiIntegrationTest.java
        │   └── service/{ParkingServiceTest,ParkingLotServiceTest}.java
        └── resources/application-test.yml
```

---

## 3. Assumptions & Design Decisions

Per the assignment's "you are allowed to make any assumptions" note, the
following decisions were made explicit:

- **Lot ID and License Plate are the primary keys** (natural keys) for
  `ParkingLot` and `Vehicle` respectively, since the spec calls them "unique
  identifiers" — no separate surrogate ID is introduced for these two entities.
- **Static credentials**: username `admin`, password `Admin@123` (configurable
  in `application.yml` under `smartpark.auth.*`). `POST /api/auth/login`
  exchanges these for a JWT valid for 1 hour.
- **All endpoints except `/api/auth/login`** (and the H2 console) require
  `Authorization: Bearer <token>`.
- **Billing**: cost = `costPerMinute × minutes parked`, minutes are rounded
  **up** to the nearest whole minute with a 1-minute minimum charge, then the
  result is rounded to 2 decimal places.
- **One active session per vehicle**: a vehicle cannot be checked into a
  second lot while it already has an active session elsewhere (`409 Conflict`
  if attempted).
- **Concurrency safety**: check-in/out uses a pessimistic DB row lock
  (`SELECT ... FOR UPDATE` via `@Lock(PESSIMISTIC_WRITE)`) on the parking lot
  row so concurrent requests can't both squeeze into the last free space or
  corrupt the occupied-space counter.
- **Auto-removal**: a `@Scheduled` job (`AutoCheckoutScheduler`) runs every 30
  seconds (configurable) and force-checks-out any vehicle whose session has
  been `ACTIVE` for more than 15 minutes (configurable), computing its cost
  exactly as a normal checkout would and freeing the space. Such sessions are
  marked `AUTO_REMOVED` (vs. `COMPLETED` for a manual checkout) so the
  distinction is preserved in the data.
- **Preloaded data**: 4 parking lots and 4 vehicles are inserted at startup
  via `data.sql` (see section 6) so the API can be exercised immediately
  without first registering anything.
- **In-memory H2**, retained for the lifetime of the JVM
  (`DB_CLOSE_DELAY=-1`) so data persists across requests within a single run,
  as required ("any data required by the application to run must be
  preloaded").

---

## 4. Build

Requires **JDK 8+** and **Maven 3.6+** on your machine, with normal internet
access to Maven Central (to download Spring Boot, H2, jjwt, etc.).

```bash
mvn clean install
```

This compiles the project, runs the unit/integration tests, and packages a
runnable JAR at `target/smartpark.jar`.

To build without running tests:

```bash
mvn clean package -DskipTests
```

---

## 5. Run

```bash
mvn spring-boot:run
```

or, after building:

```bash
java -jar target/smartpark.jar
```

The service starts on **http://localhost:8080**.

### H2 Console (optional, for inspecting the in-memory DB)

Visit `http://localhost:8080/h2-console` with:
- JDBC URL: `jdbc:h2:mem:smartparkdb`
- User: `sa`
- Password: *(blank)*

---

## 6. Preloaded Data

| Lot ID   | Location            | Capacity | Cost/Minute |
|----------|----------------------|----------|-------------|
| LOT-001  | Downtown Plaza       | 50       | 0.50        |
| LOT-002  | Airport Terminal 1   | 100      | 0.75        |
| LOT-003  | City Mall            | 30       | 0.40        |
| LOT-004  | Tiny Test Lot        | 1        | 1.00 (handy for testing the "full lot" rule) |

| License Plate | Type        | Owner Name  |
|----------------|-------------|-------------|
| ABC-123        | CAR         | John Doe    |
| XYZ-789        | MOTORCYCLE  | Jane Smith  |
| TRK-456        | TRUCK       | Robert Brown|
| MNO-321        | CAR         | Alice Green |

---

## 7. Test

```bash
mvn test
```

Included tests:
- **`ParkingServiceTest`** — unit tests (Mockito) for check-in/check-out
  rules: successful check-in, full-lot rejection, double-parking rejection,
  unregistered-vehicle rejection, cost/minute calculation, and rounding.
- **`ParkingLotServiceTest`** — unit tests for lot registration, duplicate
  rejection, and occupancy reporting.
- **`SmartParkApiIntegrationTest`** — full HTTP-layer integration test
  (via `MockMvc`, security filters included) covering: unauthenticated
  access is rejected, bad login is rejected, and the full happy path
  (login → register lot → register vehicle → check-in → lot-full rejection
  → occupancy → parked-vehicle listing → check-out with cost → space freed).

---

## 8. Authentication

```
POST /api/auth/login
Content-Type: application/json

{ "username": "admin", "password": "Admin@123" }
```

Response:

```json
{ "token": "<jwt>", "tokenType": "Bearer", "expiresInMs": 3600000 }
```

Use the token on every other call:

```
Authorization: Bearer <jwt>
```

---

## 9. API Summary

| Method | Path                                  | Auth | Description                                  |
|--------|-----------------------------------------|------|-----------------------------------------------|
| POST   | `/api/auth/login`                       | No   | Authenticate, get JWT                         |
| POST   | `/api/parking-lots`                     | Yes  | Register a parking lot                        |
| GET    | `/api/parking-lots`                     | Yes  | List all parking lots                         |
| GET    | `/api/parking-lots/{lotId}/occupancy`   | Yes  | Current occupancy & availability of a lot     |
| GET    | `/api/parking-lots/{lotId}/vehicles`    | Yes  | Vehicles currently parked in a lot            |
| POST   | `/api/vehicles`                         | Yes  | Register a vehicle                            |
| GET    | `/api/vehicles`                         | Yes  | List all registered vehicles                  |
| GET    | `/api/vehicles/{licensePlate}`          | Yes  | Fetch a single vehicle                        |
| POST   | `/api/parking/check-in`                 | Yes  | Check a vehicle into a lot                    |
| POST   | `/api/parking/check-out`                | Yes  | Check a vehicle out; returns cost             |

Full request/response examples and curl commands: see
import
[`postman/SmartPark.postman_collection.json`](./postman/SmartPark.postman_collection.json)
into Postman (the collection auto-captures the JWT from the login response
into a collection variable, so every subsequent request is pre-authenticated).

---

## 10. Error Format

All errors return a consistent JSON body:

```json
{
  "timestamp": "2026-08-24T10:15:30",
  "status": 409,
  "error": "Conflict",
  "message": "Parking lot LOT-004 is full (capacity 1)",
  "path": "/api/parking/check-in"
}
```

Validation errors additionally include a `details` array with per-field
messages.

---

## 11. Out of Scope

Per the assignment brief, no design documents are included. This README
covers build/run/test instructions and functional/assumption notes only.
