# HCBS Architecture

Technical layering for the Horizon Cinemas Booking System. For **group roles, contribution, and presentation**, see [CONTRIBUTION_MATRIX.md](CONTRIBUTION_MATRIX.md).

## Layer diagram

```text
com.hcbs.web          → Vaadin views and layout
        ↓ dto only
com.hcbs.service.*    → Application services (listing, booking, cancellation)
        ↓ entities
com.hcbs.repository   → Spring Data JPA
        ↓
com.hcbs.model        → JPA entities and enums

com.hcbs.dto          → Records exposed to the web layer
com.hcbs.config       → DataLoader (demo seed data on startup)
```

## Feature services

| Package | Responsibility |
| --- | --- |
| `service.listing` | `FilmListingService` — filter showings, city/cinema options, `ShowingRow` |
| `service.booking` | `BookingService` — bookable showings, seat list, receipts, booking rules |
| `service.cancellation` | `CancellationService` — lookup, cancel, 50% charge, release seats |

## Web layer

| Class | Depends on |
| --- | --- |
| `FilmListingView` | `FilmListingService` |
| `BookingView` | `BookingService` |
| `CancellationView` | `CancellationService` |
| `MainLayout` | Route navigation only |

Views must not import `com.hcbs.repository` or bind JPA entities in the UI.

## Persistence layer

- `model/*` — entities, enums, relationships
- `repository/*` — including `ShowingRepository.searchShowings` and active-reservation checks on `BookingSeatRepository`

## Dependency rules

1. `web` → `service.*` and `dto` only (`SeatArea` enum in booking UI is the allowed exception).
2. `service` → `repository` and `model`; return `dto` to the web layer.
3. `repository` → `model` only.

## Code review (by package)

When merging changes, the owner for each package reviews PRs touching that area. Owner mapping (Members A–D) is listed in [CONTRIBUTION_MATRIX.md](CONTRIBUTION_MATRIX.md) §2.
