# HCBS Architecture

Technical layering for the Horizon Cinemas Booking System. For **module ownership (Members A–D)**, see [四人分工.md](四人分工.md) and [CONTRIBUTION_MATRIX.md](CONTRIBUTION_MATRIX.md).

## Layer diagram

```text
com.hcbs.web.*        → Vaadin views (by menu package)
        ↓ dto only
com.hcbs.service.*    → Application services
com.hcbs.security.*   → Authentication helpers (Module C)
        ↓ entities
com.hcbs.repository   → Spring Data JPA
        ↓
com.hcbs.model        → JPA entities and enums

com.hcbs.dto          → Records exposed to the web layer
com.hcbs.config       → Security, seed data, port allocation, media catalogs
com.hcbs.util         → Shared utilities (e.g. PhoneNumbers)
```

## Web packages (Vaadin)

| Package | Route(s) | Module owner |
| --- | --- | --- |
| `web.home` | `/`, `/film/:id` | **A** |
| `web.booking` | `/booking` | **B** |
| `web.auth` | `/login`, `/register`, `/logout`, `/account` | **C** |
| `web.cancellation` | `/cancellation`, `/my-bookings` | **C** |
| `web.shell` | Layout for all menu pages | **C** |
| `web.admin` | `/admin` | **D** |
| `web.component` | Shared UI (e.g. `PageHero`) | **C** maintains |

Views must not import `com.hcbs.repository` or bind JPA entities in the UI (except allowed enums such as `SeatArea` in booking).

## Feature services

| Package | Class | Module owner |
| --- | --- | --- |
| `service.search` | `HcbsSearchService` | **A** |
| `service.listing` | `FilmListingService` | **A** |
| `service.catalog` | `FilmCatalogService`, `PosterResourceService` | **A** |
| `service.booking` | `BookingService` | **B** |
| `service.cancellation` | `CancellationService` | **C** |
| `service.auth` | `RegistrationService` | **C** |
| `service.admin` | `AdminCatalogService` | **D** |

## Security (Module C)

| Class | Role |
| --- | --- |
| `SecurityConfig` | Spring Security + Vaadin login view |
| `HcbsUserDetailsService` | Load user by phone |
| `CurrentUserService` | Current user and role checks |
| `AuthUiService` | Sign-out from UI |

## Platform / config (Module D)

| Class | Role |
| --- | --- |
| `DataLoader` / `HcbsTestDataSeeder` | Demo database on startup |
| `DemoAccountCatalog` | Demo phones (password `demo`) — used by C login UI |
| `HcbsPortAllocator` (+ related) | Automatic free port selection |
| `StartupAccessLogger` | Log Web UI and H2 console URLs |
| `HcbsDatabaseLockFailureAnalyzer` | Dev-only H2 lock guidance |

Media catalogs used by Module A: `FilmPosterCatalog`, `HcbsMediaCatalog`.

## Persistence

- `model/*` — entities, enums, relationships (split by module; see [四人分工.md](四人分工.md))
- `repository/*` — including `ShowingRepository.searchShowings` and `BookingSeatRepository.existsActiveReservationForShowingAndSeat`

## Dependency rules

1. `web` → `service.*`, `security.*` (read-only helpers), and `dto` only.
2. `service` → `repository` and `model`; return `dto` to the web layer.
3. `repository` → `model` only.
4. Cross-module changes: PR + review by the **module owner** (see [CONTRIBUTION_MATRIX.md](CONTRIBUTION_MATRIX.md) §3).

## Code review

When merging, the owner of the affected **module** reviews PRs. Do not split review by “frontend vs backend”.
