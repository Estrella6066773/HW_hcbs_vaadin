# HCBS Architecture

This project uses a layered, feature-oriented layout so four students can work in parallel with clear ownership and low coupling between the web tier and persistence.

## Layer diagram

```text
com.hcbs.web          → Vaadin views (Member C)
        ↓ dto only
com.hcbs.service.*    → Application services (Member B)
        ↓ entities
com.hcbs.repository   → Spring Data JPA (Member A)
        ↓
com.hcbs.model        → JPA entities (Member A)

com.hcbs.dto          → UI-facing records (Member B defines, all layers read)
com.hcbs.config       → Bootstrap / demo data (Member B + Member A review)
```

## Feature modules (Member B)

| Package | Responsibility |
| --- | --- |
| `service.listing` | `FilmListingService` — search showings, city/cinema options, `ShowingRow` |
| `service.booking` | `BookingService` — bookable showings, seats, receipts |
| `service.cancellation` | `CancellationService` — lookup, cancel, 50% charge, release seats |

## Web module (Member C)

| Class | Depends on |
| --- | --- |
| `FilmListingView` | `FilmListingService` only |
| `BookingView` | `BookingService` only |
| `CancellationView` | `CancellationService` only |
| `MainLayout` | Route links only |

Views must not import `com.hcbs.repository` or bind JPA entities directly.

## Persistence module (Member A)

- `model/*` — entities and enums
- `repository/*` — queries including `ShowingRepository.searchShowings` and active-seat checks on `BookingSeatRepository`

## Merge / review rules

1. Changes under `model` or `repository` → reviewed by Member A.
2. Changes under `service.*` or `dto` → reviewed by Member B.
3. Changes under `web` or `frontend` → reviewed by Member C.
4. Changes under `src/test` → reviewed by Member D.

See [CONTRIBUTION_MATRIX.md](CONTRIBUTION_MATRIX.md) for the full team split.
