# Horizon Cinemas Booking System

This is the Exercise 2 implementation for the HCBS coursework. It follows the final ERD in `Course Work/Test/HCBS_ERD.drawio` and focuses on three core functions:

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

Demo users are represented in the database:

- `staff`
- `admin`
- `manager`

No login mechanism is required for the coursework scope; the booking screen uses the sample booking staff user.

## Implemented Rules

- Booking references are unique.
- Bookings are allowed only up to one week in advance.
- A seat cannot be booked twice for the same showing.
- Total cost is calculated from city, time band, and seat area.
- Cancellation is only allowed before the day of the showing.
- Cancellation charge is 50% of the total booking cost.
- Same-day and missed-show cancellations are rejected.

## Useful Commands

Run tests:

```powershell
mvn "-Dmaven.repo.local=.m2/repository" test
```

Package the app:

```powershell
mvn "-Dmaven.repo.local=.m2/repository" package
```
