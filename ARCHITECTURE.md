# HCBS Architecture

This project uses a layered, feature-oriented layout so four students can work in parallel with clear ownership and low coupling between the web tier and persistence.

## Layer diagram

```text
com.hcbs.web          → Vaadin views (Member 3)
        ↓ dto only
com.hcbs.service.*    → Application services (Member 2)
        ↓ entities
com.hcbs.repository   → Spring Data JPA (Member 1)
        ↓
com.hcbs.model        → JPA entities (Member 1)

com.hcbs.dto          → UI-facing records (Member 2 defines, all layers read)
com.hcbs.config       → Bootstrap / demo data (Member 2 + Member 1 review)
```

## Feature modules (Member 2)

| Package | Responsibility |
| --- | --- |
| `service.listing` | `FilmListingService` — search showings, city/cinema options, `ShowingRow` |
| `service.booking` | `BookingService` — bookable showings, seats, receipts |
| `service.cancellation` | `CancellationService` — lookup, cancel, 50% charge, release seats |

## Web module (Member 3)

| Class | Depends on |
| --- | --- |
| `FilmListingView` | `FilmListingService` only |
| `BookingView` | `BookingService` only |
| `CancellationView` | `CancellationService` only |
| `MainLayout` | Route links only |

Views must not import `com.hcbs.repository` or bind JPA entities directly.

## Persistence module (Member 1)

- `model/*` — entities and enums
- `repository/*` — queries including `ShowingRepository.searchShowings` and active-seat checks on `BookingSeatRepository`

## Merge / review rules

1. Changes under `model` or `repository` → reviewed by Member 1.
2. Changes under `service.*` or `dto` → reviewed by Member 2.
3. Changes under `web` or `frontend` → reviewed by Member 3.
4. Changes under `src/test` → reviewed by Member 4.

See `.Docs/CONTRIBUTION_MATRIX.md` for the full team split.
