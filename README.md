# Horizon Cinemas Booking System

Exercise 2 implementation for the HCBS coursework. The codebase is organised for a four-person team: persistence, application services, web UI, and testing.

## Architecture

See [ARCHITECTURE.md](ARCHITECTURE.md) for package layout, dependency rules, and review ownership.

## Core functions

- Film Listing
- Booking
- Cancellation

## Technology

- Java 21
- Spring Boot
- Vaadin
- Spring Data JPA
- H2 database

## How to Run

From this folder:

```powershell
mvn "-Dmaven.repo.local=.m2/repository" spring-boot:run
```

Then open:

```text
http://localhost:8080
```

## Demo Data

The app loads sample cities, cinemas, screens, seats, films, actors, showings, users, and price rules when it starts.

Demo users in the database: `staff`, `admin`, `manager`. No login UI; booking uses the `BOOKING_STAFF` user via `BookingService`.

## Implemented Rules

- Booking references are unique.
- Bookings are allowed only up to one week in advance.
- A seat cannot be booked twice for the same showing (active bookings only).
- Total cost uses city, time band, and seat area.
- Cancellation is only allowed before the day of the showing.
- Cancellation charge is 50% of the total booking cost.
- Cancelled bookings release seats for re-booking.

## Useful Commands

Refresh Vaadin frontend (after dependency or theme changes):

```powershell
mvn "-Dmaven.repo.local=.m2/repository" vaadin:prepare-frontend
```

Run tests:

```powershell
mvn "-Dmaven.repo.local=.m2/repository" test
```

Package the app:

```powershell
mvn "-Dmaven.repo.local=.m2/repository" package
```
