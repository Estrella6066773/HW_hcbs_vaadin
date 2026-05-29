package com.hcbs.config;

import com.hcbs.model.Actor;
import com.hcbs.model.Booking;
import com.hcbs.model.BookingSeat;
import com.hcbs.model.BookingStatus;
import com.hcbs.model.Cinema;
import com.hcbs.model.City;
import com.hcbs.model.Film;
import com.hcbs.model.FilmActor;
import com.hcbs.model.PriceRule;
import com.hcbs.model.Screen;
import com.hcbs.model.Seat;
import com.hcbs.model.SeatArea;
import com.hcbs.model.Showing;
import com.hcbs.model.TimeBand;
import com.hcbs.model.User;
import com.hcbs.model.UserRole;
import com.hcbs.repository.ActorRepository;
import com.hcbs.repository.BookingRepository;
import com.hcbs.repository.BookingSeatRepository;
import com.hcbs.repository.CinemaRepository;
import com.hcbs.repository.CityRepository;
import com.hcbs.repository.FilmActorRepository;
import com.hcbs.repository.FilmRepository;
import com.hcbs.repository.PriceRuleRepository;
import com.hcbs.repository.ScreenRepository;
import com.hcbs.repository.SeatRepository;
import com.hcbs.repository.ShowingRepository;
import com.hcbs.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Seeds an H2 database aligned with the HCBS case study and automated/manual test scenarios.
 * See .Docs/TEST_DATABASE.md for the scenario catalogue.
 * <p>
 * Dev note (non-production): after changing seat layout or seed shape, delete ./data/hcbs.*
 * and restart — do not alter business code to keep an old local file DB. See .Docs/DEV_TROUBLESHOOTING.md.
 */
@Component
public class HcbsTestDataSeeder {

    /** Fixed reference for cancellation UI/manual tests (show date = today + 1). */
    public static final String SEED_BOOKING_REFERENCE = "HCBS-SEED001";

    /** Second demo booking for customer bob (show date = today + 5). */
    public static final String SEED_BOOKING_REFERENCE_002 = "HCBS-SEED002";

    /**
     * Day offset for the anchor showing — first row in {@code ShowingRepository.findAll()} order.
     * Used by {@code BookingServiceTest} (London evening standard seats, £12 per seat).
     */
    public static final int ANCHOR_SHOWING_DAY_OFFSET = 3;

    /** Every screen uses a 10×10 grid (3 + aisle + 4 + aisle + 3). */
    public static final int SCREEN_CAPACITY = SeatGridFormat.SEATS_PER_SCREEN;

    private static final int[] FLAGSHIP_SCREEN_NUMBERS = {1, 2, 3, 4};
    private static final List<String> FLAGSHIP_CINEMA_KEYS = List.of(
            "London-Central", "Birmingham-Bullring", "Bristol-Harbour", "Cardiff-Bay");
    private static final List<String> SECONDARY_CINEMA_KEYS = List.of(
            "London-East", "Birmingham-NewStreet", "Bristol-Clifton", "Cardiff-Central");

    private final CityRepository cityRepository;
    private final CinemaRepository cinemaRepository;
    private final ScreenRepository screenRepository;
    private final SeatRepository seatRepository;
    private final FilmRepository filmRepository;
    private final ActorRepository actorRepository;
    private final FilmActorRepository filmActorRepository;
    private final ShowingRepository showingRepository;
    private final PriceRuleRepository priceRuleRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final PasswordEncoder passwordEncoder;

    public HcbsTestDataSeeder(CityRepository cityRepository, CinemaRepository cinemaRepository,
                              ScreenRepository screenRepository, SeatRepository seatRepository,
                              FilmRepository filmRepository, ActorRepository actorRepository,
                              FilmActorRepository filmActorRepository, ShowingRepository showingRepository,
                              PriceRuleRepository priceRuleRepository, UserRepository userRepository,
                              BookingRepository bookingRepository, BookingSeatRepository bookingSeatRepository,
                              PasswordEncoder passwordEncoder) {
        this.cityRepository = cityRepository;
        this.cinemaRepository = cinemaRepository;
        this.screenRepository = screenRepository;
        this.seatRepository = seatRepository;
        this.filmRepository = filmRepository;
        this.actorRepository = actorRepository;
        this.filmActorRepository = filmActorRepository;
        this.showingRepository = showingRepository;
        this.priceRuleRepository = priceRuleRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.bookingSeatRepository = bookingSeatRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void seedAll() {
        SeedContext ctx = new SeedContext();
        ctx.cities = seedCities();
        ctx.cinemas = seedCinemas(ctx.cities);
        ctx.screens = seedScreensAndSeats(ctx.cinemas);
        ctx.films = seedFilmsAndActors();
        seedPriceRules(ctx.cities);
        SeedUsers users = seedUsers();
        ctx.showings = seedShowings(ctx);
        seedSampleBookings(users, ctx);
    }

    private Map<String, City> seedCities() {
        Map<String, City> cities = new LinkedHashMap<>();
        cities.put("London", cityRepository.save(new City("London")));
        cities.put("Birmingham", cityRepository.save(new City("Birmingham")));
        cities.put("Bristol", cityRepository.save(new City("Bristol")));
        cities.put("Cardiff", cityRepository.save(new City("Cardiff")));
        return cities;
    }

    private Map<String, Cinema> seedCinemas(Map<String, City> cities) {
        Map<String, Cinema> cinemas = new LinkedHashMap<>();
        cinemas.put("London-Central", cinemaRepository.save(new Cinema(cities.get("London"), "HC London Central", "Westminster")));
        cinemas.put("London-East", cinemaRepository.save(new Cinema(cities.get("London"), "HC London East", "Stratford")));
        cinemas.put("Birmingham-Bullring", cinemaRepository.save(new Cinema(cities.get("Birmingham"), "HC Birmingham Bullring", "Bullring")));
        cinemas.put("Birmingham-NewStreet", cinemaRepository.save(new Cinema(cities.get("Birmingham"), "HC Birmingham New Street", "New Street")));
        cinemas.put("Bristol-Harbour", cinemaRepository.save(new Cinema(cities.get("Bristol"), "HC Bristol Harbour", "Harbourside")));
        cinemas.put("Bristol-Clifton", cinemaRepository.save(new Cinema(cities.get("Bristol"), "HC Bristol Clifton", "Clifton")));
        cinemas.put("Cardiff-Bay", cinemaRepository.save(new Cinema(cities.get("Cardiff"), "HC Cardiff Bay", "Cardiff Bay")));
        cinemas.put("Cardiff-Central", cinemaRepository.save(new Cinema(cities.get("Cardiff"), "HC Cardiff Central", "Central")));
        return cinemas;
    }

    /**
     * Flagship: 4 screens × 100 seats. Secondary: 2×100 seats. Case: ≥2 cinemas/city, ≤6 screens/cinema.
     */
    private Map<String, Screen> seedScreensAndSeats(Map<String, Cinema> cinemas) {
        Map<String, Screen> screens = new LinkedHashMap<>();
        for (String cinemaKey : FLAGSHIP_CINEMA_KEYS) {
            Cinema cinema = cinemas.get(cinemaKey);
            for (int screenNumber : FLAGSHIP_SCREEN_NUMBERS) {
                Screen screen = screenRepository.save(new Screen(cinema, screenNumber, SCREEN_CAPACITY));
                screens.put(screenKey(cinemaKey, screenNumber), screen);
                createSeats(screen);
            }
        }
        for (String cinemaKey : SECONDARY_CINEMA_KEYS) {
            Cinema cinema = cinemas.get(cinemaKey);
            for (int screenNumber = 1; screenNumber <= 2; screenNumber++) {
                Screen screen = screenRepository.save(new Screen(cinema, screenNumber, SCREEN_CAPACITY));
                screens.put(screenKey(cinemaKey, screenNumber), screen);
                createSeats(screen);
            }
        }
        return screens;
    }

    private void createSeats(Screen screen) {
        for (int row = 1; row <= SeatGridFormat.ROWS; row++) {
            for (int col = 1; col <= SeatGridFormat.COLS; col++) {
                seatRepository.save(new Seat(screen, SeatGridFormat.seatNumber(row, col), SeatArea.STANDARD));
            }
        }
    }

    private Map<String, Film> seedFilmsAndActors() {
        Map<String, Film> films = new LinkedHashMap<>();
        films.put("Skyline", saveFilm(new HcbsMediaCatalog.FilmSeed(
                "Skyline", "Spirited Away",
                "A fast-paced city thriller following a courier racing across London before dawn.",
                "Action", "12A", 4.4, 118, "/images/posters/home/spirited-away.png")));
        films.put("Orbit", saveFilm(new HcbsMediaCatalog.FilmSeed(
                "Orbit", "The Legend of 1900",
                "A science fiction story set around a lost orbital station and its last crew.",
                "Sci-Fi", "PG", 4.6, 132, "/images/posters/home/legend-of-1900.png")));
        films.put("Harbour", saveFilm(new HcbsMediaCatalog.FilmSeed(
                "Harbour", "The Wasted Times",
                "A warm drama about family reconciliation in a seaside town.",
                "Drama", "PG", 4.1, 105, "/images/posters/home/wasted-times.png")));
        films.put("Coral", saveFilm(new HcbsMediaCatalog.FilmSeed(
                "Coral", "Harry Potter and the Order of the Phoenix",
                "A family adventure on the Welsh coast with treasure hunts and summer storms.",
                "Family", "U", 4.0, 95, "/images/posters/home/harry-potter-phoenix.png")));
        films.put("Archive", saveFilm(new HcbsMediaCatalog.FilmSeed(
                "Archive", "Solitude",
                "A documentary on restored cinema heritage and touring projectionists.",
                "Documentary", "PG", 4.3, 88, "/images/posters/home/solitude.png")));
        for (HcbsMediaCatalog.FilmSeed film : HcbsMediaCatalog.extendedFilms()) {
            films.put(film.key(), saveFilm(film));
        }

        Map<String, Actor> actorsByName = new LinkedHashMap<>();
        actorsByName.put("Maya Stone", actorRepository.save(new Actor("Maya Stone", "Lead actor")));
        actorsByName.put("Leo Grant", actorRepository.save(new Actor("Leo Grant", "Supporting actor")));
        actorsByName.put("Nina Clark", actorRepository.save(new Actor("Nina Clark", "Lead actor")));
        actorsByName.put("Sam Reed", actorRepository.save(new Actor("Sam Reed", "Lead actor")));
        for (HcbsMediaCatalog.ActorSeed actor : HcbsMediaCatalog.extendedActors()) {
            actorsByName.put(actor.fullName(), actorRepository.save(new Actor(actor.fullName(), actor.details())));
        }

        linkCast(films, actorsByName, "Skyline", "Maya Stone", "Leo Grant");
        linkCast(films, actorsByName, "Orbit", "Nina Clark", "Maya Stone");
        linkCast(films, actorsByName, "Harbour", "Leo Grant", "Sam Reed");
        linkCast(films, actorsByName, "Coral", "Nina Clark");
        linkCast(films, actorsByName, "Archive", "Sam Reed");
        for (HcbsMediaCatalog.CastSeed cast : HcbsMediaCatalog.extendedCast()) {
            Film film = films.get(cast.filmKey());
            Actor actor = actorsByName.get(cast.actorFullName());
            if (film != null && actor != null) {
                filmActorRepository.save(new FilmActor(film, actor));
            }
        }
        return films;
    }

    private Film saveFilm(HcbsMediaCatalog.FilmSeed seed) {
        return filmRepository.save(new Film(
                seed.title(),
                seed.description(),
                seed.genre(),
                seed.ageRating(),
                seed.rating(),
                seed.durationMinutes(),
                seed.posterUrl()));
    }

    private void linkCast(Map<String, Film> films, Map<String, Actor> actors, String filmKey, String... actorNames) {
        Film film = films.get(filmKey);
        if (film == null) {
            return;
        }
        for (String name : actorNames) {
            Actor actor = actors.get(name);
            if (actor != null) {
                filmActorRepository.save(new FilmActor(film, actor));
            }
        }
    }

    /** Standard-seat prices from the case study (former lower-hall rates). */
    private void seedPriceRules(Map<String, City> cities) {
        seedCityPrices(cities.get("London"), "10.00", "11.00", "12.00");
        seedCityPrices(cities.get("Birmingham"), "5.00", "6.00", "7.00");
        seedCityPrices(cities.get("Bristol"), "6.00", "7.00", "8.00");
        seedCityPrices(cities.get("Cardiff"), "5.00", "6.00", "7.00");
    }

    private void seedCityPrices(City city, String morning, String afternoon, String evening) {
        priceRuleRepository.save(new PriceRule(city, TimeBand.MORNING, SeatArea.STANDARD, new BigDecimal(morning)));
        priceRuleRepository.save(new PriceRule(city, TimeBand.AFTERNOON, SeatArea.STANDARD, new BigDecimal(afternoon)));
        priceRuleRepository.save(new PriceRule(city, TimeBand.EVENING, SeatArea.STANDARD, new BigDecimal(evening)));
    }

    private SeedUsers seedUsers() {
        String encoded = passwordEncoder.encode(DemoAccountCatalog.DEMO_PASSWORD);
        for (DemoAccountCatalog.DemoAccount account : DemoAccountCatalog.all()) {
            User user = new User(
                    account.username(),
                    account.email(),
                    encoded,
                    account.fullName(),
                    account.role());
            user.setPhone(account.phone());
            userRepository.save(user);
        }
        User staff = userRepository.findByUsername("staff").orElseThrow();
        User alice = userRepository.findByUsername("alice").orElseThrow();
        User bob = userRepository.findByUsername("bob").orElseThrow();
        return new SeedUsers(staff, alice, bob);
    }

    /**
     * Inserts showings in a fixed order so {@code findAll()} row 0 remains the booking-test anchor.
     */
    private List<Showing> seedShowings(SeedContext ctx) {
        LocalDate today = LocalDate.now();
        List<Showing> created = new ArrayList<>();
        for (ShowingSpec spec : buildShowingSpecs(today)) {
            created.add(persistShowing(ctx, spec));
        }
        return created;
    }

    private List<ShowingSpec> buildShowingSpecs(LocalDate today) {
        List<ShowingSpec> specs = new ArrayList<>(buildCoreShowingSpecs(today));
        for (HcbsMediaCatalog.ShowingSeed extended : HcbsMediaCatalog.extendedShowings(today)) {
            specs.add(new ShowingSpec(
                    extended.filmKey(),
                    extended.cinemaKey(),
                    extended.screenNumber(),
                    extended.showDate(),
                    extended.start(),
                    extended.end(),
                    extended.timeBand(),
                    extended.scenarioNote()));
        }
        return specs;
    }

    /** Case-study anchor, policy edges, and automated-test fixtures — order must not change. */
    private List<ShowingSpec> buildCoreShowingSpecs(LocalDate today) {
        List<ShowingSpec> specs = new ArrayList<>();

        // --- Anchor (must stay first): London Central Screen 1, evening, day+3, £12 standard seat ---
        specs.add(new ShowingSpec("Skyline", "London-Central", 1, today.plusDays(ANCHOR_SHOWING_DAY_OFFSET),
                LocalTime.of(18, 30), LocalTime.of(20, 30), TimeBand.EVENING, "ANCHOR"));

        specs.add(new ShowingSpec("Orbit", "London-Central", 2, today.plusDays(3),
                LocalTime.of(10, 0), LocalTime.of(12, 15), TimeBand.MORNING, "London morning"));
        specs.add(new ShowingSpec("Skyline", "London-East", 1, today.plusDays(3),
                LocalTime.of(14, 30), LocalTime.of(16, 30), TimeBand.AFTERNOON, "London secondary"));
        specs.add(new ShowingSpec("Archive", "London-Central", 3, today.plusDays(3),
                LocalTime.of(9, 30), LocalTime.of(11, 0), TimeBand.MORNING, "London documentary"));

        specs.add(new ShowingSpec("Harbour", "Birmingham-Bullring", 1, today.plusDays(4),
                LocalTime.of(14, 0), LocalTime.of(15, 45), TimeBand.AFTERNOON, "Birmingham flagship"));
        specs.add(new ShowingSpec("Coral", "Birmingham-NewStreet", 1, today.plusDays(3),
                LocalTime.of(10, 15), LocalTime.of(11, 50), TimeBand.MORNING, "Birmingham secondary"));
        specs.add(new ShowingSpec("Skyline", "Birmingham-Bullring", 2, today.plusDays(2),
                LocalTime.of(17, 30), LocalTime.of(19, 30), TimeBand.EVENING, "Birmingham day+2"));

        specs.add(new ShowingSpec("Coral", "Bristol-Harbour", 1, today.plusDays(5),
                LocalTime.of(19, 0), LocalTime.of(20, 40), TimeBand.EVENING, "Bristol flagship"));
        specs.add(new ShowingSpec("Orbit", "Bristol-Clifton", 1, today.plusDays(3),
                LocalTime.of(11, 30), LocalTime.of(13, 45), TimeBand.MORNING, "Bristol secondary"));
        specs.add(new ShowingSpec("Harbour", "Bristol-Harbour", 2, today.plusDays(3),
                LocalTime.of(15, 0), LocalTime.of(16, 50), TimeBand.AFTERNOON, "Bristol filter"));

        specs.add(new ShowingSpec("Archive", "Cardiff-Bay", 1, today.plusDays(2),
                LocalTime.of(11, 0), LocalTime.of(12, 30), TimeBand.MORNING, "Cardiff day+2"));
        specs.add(new ShowingSpec("Orbit", "Cardiff-Bay", 2, today.plusDays(4),
                LocalTime.of(20, 15), LocalTime.of(22, 30), TimeBand.EVENING, "Cardiff flagship"));
        specs.add(new ShowingSpec("Skyline", "Cardiff-Central", 1, today.plusDays(6),
                LocalTime.of(18, 0), LocalTime.of(20, 0), TimeBand.EVENING, "Cardiff secondary"));

        // Booking window edges
        specs.add(new ShowingSpec("Skyline", "London-Central", 1, today.plusDays(7),
                LocalTime.of(20, 0), LocalTime.of(22, 0), TimeBand.EVENING, "Max advance (allowed)"));
        specs.add(new ShowingSpec("Orbit", "London-Central", 2, today.plusDays(8),
                LocalTime.of(18, 0), LocalTime.of(20, 0), TimeBand.EVENING, "Beyond 7 days (reject)"));

        // Cancellation policy
        specs.add(new ShowingSpec("Harbour", "London-Central", 2, today.plusDays(1),
                LocalTime.of(17, 0), LocalTime.of(19, 0), TimeBand.EVENING, "Seed booking / cancel OK"));
        specs.add(new ShowingSpec("Coral", "Birmingham-Bullring", 1, today,
                LocalTime.of(19, 30), LocalTime.of(21, 0), TimeBand.EVENING, "Same-day cancel (reject)"));

        // Past showing — cannot book
        specs.add(new ShowingSpec("Archive", "Cardiff-Bay", 1, today.minusDays(1),
                LocalTime.of(14, 0), LocalTime.of(15, 30), TimeBand.AFTERNOON, "Past showing"));

        return specs;
    }

    private Showing persistShowing(SeedContext ctx, ShowingSpec spec) {
        Screen screen = requireScreen(ctx.screens, spec.cinemaKey(), spec.screenNumber());
        Film film = ctx.films.get(spec.filmKey());
        return showingRepository.save(new Showing(
                film, screen, spec.showDate(), spec.start(), spec.end(), spec.timeBand()));
    }

    private void seedSampleBookings(SeedUsers users, SeedContext ctx) {
        Showing cancellationDemo = ctx.showings.stream()
                .filter(s -> s.getShowDate().equals(LocalDate.now().plusDays(1)))
                .filter(s -> s.getFilm().getTitle().equals("The Wasted Times"))
                .filter(s -> s.getScreen().getCinema().getName().contains("London Central"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Seed cancellation showing missing"));

        Seat demoSeat = requireSeat(cancellationDemo.getScreen(), 5, 5);
        BigDecimal ticketPrice = lookupStandardPrice(cancellationDemo);

        Booking booking = new Booking();
        booking.setBookingReference(SEED_BOOKING_REFERENCE);
        booking.setShowing(cancellationDemo);
        booking.setCreatedBy(users.staff());
        booking.setCustomer(users.demoCustomer());
        booking.setBookingDateTime(LocalDateTime.now().minusHours(2));
        booking.setNumberOfTickets(1);
        booking.setTotalCost(ticketPrice);
        booking.setStatus(BookingStatus.CONFIRMED);
        Booking saved = bookingRepository.save(booking);
        bookingSeatRepository.save(new BookingSeat(saved, demoSeat, cancellationDemo, ticketPrice));

        seedSecondSampleBooking(users, ctx);
    }

    private void seedSecondSampleBooking(SeedUsers users, SeedContext ctx) {
        Showing bobShowing = ctx.showings.stream()
                .filter(s -> s.getShowDate().equals(LocalDate.now().plusDays(5)))
                .filter(s -> s.getFilm().getTitle().equals("The Legend of 1900"))
                .filter(s -> s.getScreen().getCinema().getName().contains("Cardiff Bay"))
                .findFirst()
                .orElseGet(() -> ctx.showings.stream()
                        .filter(s -> s.getFilm().getTitle().equals("The Legend of 1900"))
                        .filter(s -> !s.getShowDate().isBefore(LocalDate.now()))
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("Seed booking #2 showing missing")));

        Seat seat1 = requireSeat(bobShowing.getScreen(), 3, 2);
        Seat seat2 = requireSeat(bobShowing.getScreen(), 3, 3);
        BigDecimal price = lookupStandardPrice(bobShowing);
        BigDecimal total = price.multiply(new BigDecimal("2"));

        Booking booking = new Booking();
        booking.setBookingReference(SEED_BOOKING_REFERENCE_002);
        booking.setShowing(bobShowing);
        booking.setCreatedBy(users.bob());
        booking.setCustomer(users.bob());
        booking.setBookingDateTime(LocalDateTime.now().minusDays(1));
        booking.setNumberOfTickets(2);
        booking.setTotalCost(total);
        booking.setStatus(BookingStatus.CONFIRMED);
        Booking saved = bookingRepository.save(booking);
        bookingSeatRepository.save(new BookingSeat(saved, seat1, bobShowing, price));
        bookingSeatRepository.save(new BookingSeat(saved, seat2, bobShowing, price));
    }

    private Seat requireSeat(Screen screen, int row, int column) {
        String seatNumber = SeatGridFormat.seatNumber(row, column);
        return seatRepository.findByScreenAndSeatArea(screen, SeatArea.STANDARD).stream()
                .filter(seat -> seatNumber.equals(seat.getSeatNumber()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Seat not seeded: " + seatNumber));
    }

    private BigDecimal lookupStandardPrice(Showing showing) {
        PriceRule rule = priceRuleRepository.findByCityAndTimeBandAndSeatArea(
                        showing.getScreen().getCinema().getCity(),
                        showing.getTimeBand(),
                        SeatArea.STANDARD)
                .orElseThrow(() -> new IllegalStateException("No standard price for seed booking"));
        return rule.getPrice();
    }

    private static Screen requireScreen(Map<String, Screen> screens, String cinemaKey, int screenNumber) {
        Screen screen = screens.get(screenKey(cinemaKey, screenNumber));
        if (screen == null) {
            throw new IllegalStateException("Screen not seeded: " + screenKey(cinemaKey, screenNumber));
        }
        return screen;
    }

    private static String screenKey(String cinemaKey, int screenNumber) {
        return cinemaKey + "#" + screenNumber;
    }

    private static final class SeedContext {
        Map<String, City> cities;
        Map<String, Cinema> cinemas;
        Map<String, Screen> screens;
        Map<String, Film> films;
        List<Showing> showings;
    }

    private record SeedUsers(User staff, User demoCustomer, User bob) {
    }

    private record ShowingSpec(
            String filmKey,
            String cinemaKey,
            int screenNumber,
            LocalDate showDate,
            LocalTime start,
            LocalTime end,
            TimeBand timeBand,
            String scenarioNote) {
    }
}
