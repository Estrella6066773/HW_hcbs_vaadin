# Horizon Cinemas Booking System (HCBS)

**Module:** IN3338 Object-Oriented Development  
**Institution:** UWE Bristol  
**Academic year:** 2025–2026  

A group coursework implementation of the *Horizon Cinemas Booking System* case study. The application supports **film listing**, **ticket booking**, and **booking cancellation** through a Vaadin web interface backed by Spring Boot and JPA.

**Languages:** English (this file) · [简体中文](README_CN.md)

---

## Table of contents

- [Features](#features)
- [Technology stack](#technology-stack)
- [Project structure](#project-structure)
- [Team organisation](#team-organisation)
- [Presentation (individual)](#presentation-individual)
- [Prerequisites](#prerequisites)
- [How to run](#how-to-run)
- [How to test](#how-to-test)
- [Business rules](#business-rules)
- [Demo data](#demo-data)
- [Submission notes](#submission-notes)
- [Further reading](#further-reading)

---

## Features

| Function | Route | Description |
| --- | --- | --- |
| **Film listing** | `/` | Filter showings by city, cinema, date, and film title; view description, cast, genre, age rating, times, and available seats. |
| **Booking** | `/booking` | Select a showing and seats (lower hall / upper gallery); receive a receipt with a unique reference. |
| **Cancellation** | `/cancellation` | Look up a booking by reference; cancel before show day with a 50% charge. |

Out of scope (by coursework design): Admin/Manager screens, login and role-based access control, payment processing.

---

## Technology stack

| Layer | Technology |
| --- | --- |
| Language | Java 21 |
| Framework | Spring Boot 3.2 |
| UI | Vaadin 24 |
| Persistence | Spring Data JPA, H2 (in-memory) |
| Build | Maven |
| Tests | JUnit 5, Spring Boot Test, AssertJ |

---

## Project structure

```text
hcbs-vaadin/
├── src/main/java/com/hcbs/
│   ├── model/              # JPA entities (Member A)
│   ├── repository/         # Data access (Member A)
│   ├── dto/                # UI-facing records (Member B)
│   ├── service/
│   │   ├── listing/        # FilmListingService
│   │   ├── booking/        # BookingService
│   │   └── cancellation/   # CancellationService
│   ├── web/                # Vaadin views (Member C)
│   └── config/             # DataLoader seed data
├── src/test/java/          # Unit & integration tests (Member D)
├── frontend/themes/hcbs/   # Application theme (Member C)
├── TEST_CASES.md           # Manual test case table
├── ARCHITECTURE.md         # Layering and dependency rules
└── CONTRIBUTION_MATRIX.md  # Group contribution breakdown (Members A–D)
```

**Dependency rule:** `web` → `service` + `dto` only; `service` → `repository` + `model`; views must not call repositories or bind JPA entities directly.

---

## Team organisation

Work is split across four members to match the modular packages:

| Member | Role | Primary packages |
| --- | --- | --- |
| **A** | Persistence & data design | `model/`, `repository/`, Exercise 1 ERD |
| **B** | Application services & DTOs | `dto/`, `service/*`, `config/DataLoader` |
| **C** | Web UI & theme | `web/`, `frontend/themes/hcbs/` |
| **D** | Testing & delivery | `src/test/`, `TEST_CASES.md`, submission pack |

See [CONTRIBUTION_MATRIX.md](CONTRIBUTION_MATRIX.md) for percentages and deliverables. See [ARCHITECTURE.md](ARCHITECTURE.md) for review rules before merging.

---

## Presentation (individual)

The demo session (20% of the module) uses **individual presentations**: **each member explains their own part** of the system. There is no single group spokesperson for the whole project.

| Member | Typical focus (3–5 min each) |
| --- | --- |
| **A** | ERD, schema, entities, repository queries |
| **B** | Services, business rules, DTOs, seed data |
| **C** | Vaadin views, navigation, theme, live UI walkthrough |
| **D** | Test strategy, `TEST_CASES.md`, automated tests, scope limits |

All members must attend. Tutors may ask questions on **your** module only. Speaking notes are in [CONTRIBUTION_MATRIX.md](CONTRIBUTION_MATRIX.md) §5.

---

## Prerequisites

- **JDK 21** (`java -version`)
- **Maven 3.9+** (`mvn -version`)
- A modern browser for `http://localhost:8080`

On Windows, if the project lives under OneDrive, close any running dev server before `mvn test` to avoid file locks under `frontend/generated`.

---

## How to run

From the project root:

```powershell
mvn "-Dmaven.repo.local=.m2/repository" spring-boot:run
```

Open:

```text
http://localhost:8080
```

**Navigation:** Film Listing (home) · Booking · Cancellation (drawer menu).

After changing Vaadin dependencies or the theme, refresh the frontend once:

```powershell
mvn "-Dmaven.repo.local=.m2/repository" vaadin:prepare-frontend
```

---

## How to test

Run all automated tests:

```powershell
mvn "-Dmaven.repo.local=.m2/repository" clean test
```

| Test class | Covers |
| --- | --- |
| `BookingServiceTest` | Booking, pricing, duplicate seat, 7-day limit |
| `CancellationServiceTest` | Cancel fee, seat release, same-day rejection |
| `FilmListingServiceTest` | Search rows, description field |
| `DataLoaderTest` | Seed data (cities, cinemas, seats) |
| `UiThemeTest` | Custom `hcbs` theme |

Manual scenarios are documented in [TEST_CASES.md](TEST_CASES.md) (TC_001–TC_011).

Package the application:

```powershell
mvn "-Dmaven.repo.local=.m2/repository" package
```

---

## Business rules

| Rule | Implementation |
| --- | --- |
| Unique booking reference | Generated in `BookingService` until unique |
| Book up to 7 days ahead | `validateBookingDate` |
| No double booking of the same seat | Active `CONFIRMED` reservations only |
| Ticket price | City × time band × seat area (`PriceRule`) |
| Cancel at least one day before show | `CancellationService.canCancel` |
| Cancellation fee | 50% of total booking cost |
| No same-day cancellation | Rejected with clear message |
| Seats after cancellation | `BookingSeat` rows removed; seats available again |

---

## Demo data

On first startup, `DataLoader` seeds:

- **Cities:** London, Birmingham, Bristol, Cardiff (≥2 cinemas each)
- **Screens:** 2 per cinema, 50 seats each (25 lower hall + 25 upper gallery)
- **Films & actors:** Sample titles with cast links
- **Showings:** Spread across the next few days
- **Price rules:** Lower-hall table from the case study; upper gallery +£2
- **Users:** `staff` (booking), `admin`, `manager` — no login UI; booking uses `BOOKING_STAFF`

---

## Submission notes

Blackboard typically requires (check the latest brief):

1. **Exercise 1:** ERD, logical schema, short design explanation (PDF).
2. **Exercise 2:** Full source in `Group_No.zip` plus this README (how to run).
3. **Exercise 3:** Test evidence — `TEST_CASES.md`, test classes, and optional screenshots.
4. **Contribution matrix:** [CONTRIBUTION_MATRIX.md](CONTRIBUTION_MATRIX.md); all members present; **each presents their own section** (not one unified talk).

Zip layout suggestion:

```text
Group_No.zip
├── src/
├── pom.xml
├── README.md
├── README_CN.md
├── ARCHITECTURE.md
├── CONTRIBUTION_MATRIX.md
├── TEST_CASES.md
└── (optional) docs/ERD.pdf
```

---

## Further reading

- [ARCHITECTURE.md](ARCHITECTURE.md) — layers, modules, code review ownership  
- [README_CN.md](README_CN.md) — Chinese readme  
- [CONTRIBUTION_MATRIX.md](CONTRIBUTION_MATRIX.md) — Members A–D contribution breakdown  
- [TEST_CASES.md](TEST_CASES.md) — coursework test case table  
- Case study & brief: `.Docs/req/`

---

## Licence & academic integrity

Coursework submission for IN3338. All group members are responsible for the submitted work. Do not copy from other groups; cite any external resources used in your report.
