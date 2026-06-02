# Horizon Cinemas Booking System (HCBS)

**Module:** IN3338 Object-Oriented Development  
**Institution:** UWE Bristol  
**Academic year:** 2025–2026  

Horizon Cinemas Booking System is a web application for cinema staff to browse film showings, sell tickets, and process cancellations. It implements the three core functions required by the HCBS case study.

**Languages:** English (this file) · [简体中文](.Docs/README_CN.md)

> Group membership and **module-based** division of labour: [.Docs/四人分工.md](.Docs/四人分工.md) and [.Docs/CONTRIBUTION_MATRIX.md](.Docs/CONTRIBUTION_MATRIX.md). This readme describes **what the software does** and **how to run it** only.

---

## Table of contents

- [Features](#features)
- [User flows](#user-flows)
- [Technology stack](#technology-stack)
- [Project structure](#project-structure)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [How to run](#how-to-run)
- [How to test](#how-to-test)
- [Business rules](#business-rules)
- [Demo data](#demo-data)
- [Related documents](#related-documents)

---

## Features

| Function | Route | Description |
| --- | --- | --- |
| **Home** | `/` | Poster browse by default; additive search with metric cards and a full showing grid. |
| **Film detail** | `/film/{id}` | Synopsis, cast, rating, and upcoming showtimes for one film. |
| **Booking** | `/booking` | Pick a showing within the allowed booking window, choose seat area (lower hall or upper gallery), select one or more free seats, and confirm. A receipt is printed with a unique booking reference. |
| **Cancellation** | `/cancellation` | Staff: find any booking by reference and cancel when policy allows. |
| **My bookings** | `/my-bookings` | Customers: view and cancel their own bookings. |
| **Sign in** | `/login` | Phone number and password; demo accounts grouped by role. |
| **Register** | `/register` | Customer self-registration (username, email, password). |
| **Data admin** | `/admin` | Administrators: manage films, showings, and accounts (`ADMIN` only). |

**Not implemented** (out of coursework scope): payment processing, email verification, password recovery.

---

## User flows

### Film listing

1. Open the home page.
2. Optionally set city, cinema, date, and/or film title filter.
3. Click **Search** to refresh the grid.
4. Review sessions and remaining capacity before moving to booking.

### Booking

1. Open **Booking** from the menu.
2. Select a showing from the dropdown (only dates within the next seven days are listed).
3. Choose **Seat area** (lower hall or upper gallery).
4. Select one or more available seats.
5. Click **Confirm booking** and read the receipt (reference, film, date/time, screen, seats, total, booking timestamp).

### Cancellation

1. Open **Cancellation** from the menu.
2. Enter the booking reference and click **Find booking**.
3. Check whether cancellation is allowed and the 50% charge.
4. Click **Cancel booking** if eligible; the detail panel updates with the new status and charge.

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
│   ├── model/              # JPA entities
│   ├── repository/         # Spring Data repositories
│   ├── dto/                # Data passed to the UI layer
│   ├── service/
│   │   ├── search/         # Module A — home search
│   │   ├── listing/        # Module A — showings list
│   │   ├── catalog/        # Module A — films & posters
│   │   ├── booking/        # Module B
│   │   ├── cancellation/   # Module C
│   │   ├── auth/           # Module C
│   │   └── admin/          # Module D
│   ├── security/           # Module C
│   ├── web/                # Vaadin views (home, booking, auth, …)
│   └── config/             # Seed data, security, port (mostly Module D)
├── src/test/java/          # Automated tests (per module owner)
├── frontend/themes/hcbs/   # Application theme
├── README.md               # Functional overview (this file)
└── .Docs/                  # All other documentation
    ├── 四人分工.md         # Module ownership + file links (primary)
    ├── README_CN.md
    ├── ARCHITECTURE.md
    ├── CONTRIBUTION_MATRIX.md
    ├── TEST_CASES.md
    ├── TEST_DATABASE.md
    └── req/                # Case study & coursework briefs
```

---

## Architecture

The application follows a layered layout:

```text
web  →  service.*  →  repository  →  model
         ↑
        dto (UI-facing records)
```

- **Web** calls application services only and binds DTOs—not JPA entities or repositories.
- **Services** enforce business rules (pricing, booking window, cancellation policy).
- **Repositories** handle persistence queries (e.g. filtered showings, active seat reservations).

Details: [.Docs/ARCHITECTURE.md](.Docs/ARCHITECTURE.md).

---

## Prerequisites

- **JDK 21** (`java -version`)
- **Maven 3.9+** (`mvn -version`)
- A modern browser (default URL `http://localhost:8080`; see **Access URLs** below if the port changes)

On Windows, if the project lives under OneDrive, close any running dev server before `mvn test` to avoid file locks under `frontend/generated` and `./data/hcbs.mv.db`.

**If startup fails with H2 lock / `90020` / `The file is locked`:** this is a **development** build. Stop other runs, delete `./data/hcbs.lock.db` and `./data/hcbs.mv.db`, then restart—**do not change code** to work around stale local data. See [.Docs/DEV_TROUBLESHOOTING.md](.Docs/DEV_TROUBLESHOOTING.md).

**Git:** Local runtime data is not committed — see `.gitignore` (`data/`, H2 `*.mv.db`, `target/`, `.m2/`, `frontend/generated/`). After `spring-boot:run`, do not add `./data/` to commits.

---

## How to run

From the project root:

```powershell
mvn "-Dmaven.repo.local=.m2/repository" spring-boot:run
```

When startup completes, the console prints a banner with the actual URLs, for example:

```text
============================================================
  HCBS is ready
  Web UI:     http://localhost:8080/
  H2 console: http://localhost:8080/h2-console
  Port:       8080
============================================================
```

**Port selection:** The app prefers **8080**. If that port is in use, it tries up to **20** hash-derived ports in **8081–8280** (seed + working directory + attempt index). If all 20 are busy, the process **exits**. Override with `-Dserver.port=9090` or `SERVER_PORT=9090`.

**Navigation:** Home · Booking · Cancellation (drawer menu).

After changing Vaadin dependencies or the theme:

```powershell
mvn "-Dmaven.repo.local=.m2/repository" vaadin:prepare-frontend
```

---

## How to test

Run all automated tests:

```powershell
mvn "-Dmaven.repo.local=.m2/repository" clean test
```

| Test class | Module | Covers |
| --- | --- | --- |
| `HcbsSearchServiceTest` | A | Home search & filters |
| `FilmListingServiceTest` | A | Listing rows, description |
| `FilmCatalogServiceTest` | A | Film catalog / posters |
| `BookingServiceTest` | B | Booking, pricing, duplicate seat, 7-day limit |
| `RegistrationServiceTest` | C | Customer registration |
| `CancellationServiceTest` | C | Cancel fee, seat release, same-day rejection |
| `AdminCatalogServiceTest` | D | Admin catalog logic |
| `DataLoaderTest` | D | Seed data (cities, cinemas, seats) |
| `HcbsPortAllocatorTest` | D | Port selection |
| `UiThemeTest` | D | Custom `hcbs` theme |

Manual scenarios: [.Docs/TEST_CASES.md](.Docs/TEST_CASES.md) (TC_001–TC_011).

Package the application:

```powershell
mvn "-Dmaven.repo.local=.m2/repository" package
```

---

## Business rules

| Rule | Behaviour |
| --- | --- |
| Unique booking reference | Generated until unique (`HCBS-` prefix) |
| Booking window | From today up to 7 days before show date |
| Past showings | Cannot be booked |
| Seat availability | A seat cannot be sold twice for the same showing while booking is `CONFIRMED` |
| Ticket price | Based on city, time band (morning / afternoon / evening), and seat area |
| Upper gallery | Priced £2 above lower hall for the same city and band |
| Cancellation timing | Allowed only if today is **before** the show date |
| Cancellation fee | 50% of total booking cost |
| Same-day cancellation | Rejected |
| After cancellation | Booking seats are released; seats can be booked again |

---

## Demo data

On first startup, `HcbsTestDataSeeder` loads a full **test database** aligned with the case study (see [.Docs/TEST_DATABASE.md](.Docs/TEST_DATABASE.md)):

| Data | Content |
| --- | --- |
| Cities | London, Birmingham, Bristol, Cardiff (≥2 cinemas each) |
| Screens | Flagship 4 screens (50–120 seats); secondary 2×50; lower/upper split per screen |
| Films & actors | 5 films with cast, posters, and searchable descriptions |
| Showings | 18 dated sessions across four cities (booking/cancellation edge cases) |
| Price rules | Case-study lower-hall prices; upper gallery +£2 |
| Users | 9 demo accounts (3 per role: customer, booking staff, administrator); password `demo`; listed on `/login` by phone; new customers via `/register` |

Customers book for themselves; staff desk bookings require selecting a customer phone. Self-service and staff flows use signed-in users (`CUSTOMER` / `BOOKING_STAFF` / `ADMIN`).

**Pre-seeded booking:** reference `HCBS-SEED001` (for cancellation demos on the Cancellation page).

**Reset data:** stop the app, delete `./data/hcbs.lock.db` and `./data/hcbs.mv.db`, then start again to re-run the seeder. After seat-format or seed changes, always reset data instead of patching code—see [.Docs/DEV_TROUBLESHOOTING.md](.Docs/DEV_TROUBLESHOOTING.md).

---

## Related documents

| Document | Purpose |
| --- | --- |
| [.Docs/README.md](.Docs/README.md) | Index of all project documentation |
| [.Docs/四人分工.md](.Docs/四人分工.md) | **Primary:** module ownership A–D + clickable source paths |
| [.Docs/README_CN.md](.Docs/README_CN.md) | Chinese version of this readme |
| [.Docs/ARCHITECTURE.md](.Docs/ARCHITECTURE.md) | Technical layering and dependencies |
| [.Docs/TEST_CASES.md](.Docs/TEST_CASES.md) | Manual test case table |
| [.Docs/TEST_DATABASE.md](.Docs/TEST_DATABASE.md) | Test database design, seed scenarios, reset steps |
| [.Docs/DEV_TROUBLESHOOTING.md](.Docs/DEV_TROUBLESHOOTING.md) | Dev-only: delete `./data/` on format/lock errors—do not change code |
| [.Docs/CONTRIBUTION_MATRIX.md](.Docs/CONTRIBUTION_MATRIX.md) | Names, contribution %, signatures, presentation |
| [.Docs/roles/](.Docs/roles/) | Per-member module guides |
| [.Docs/req/](.Docs/req/) | Case study and coursework brief |

---

## Academic integrity

Coursework submission for IN3338. Cite any external resources used in your report. Do not copy from other groups.
