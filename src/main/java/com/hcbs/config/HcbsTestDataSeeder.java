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
 */
@Component
public class HcbsTestDataSeeder {

    /** Fixed reference for cancellation UI/manual tests (show date = today + 1). */
    public static final String SEED_BOOKING_REFERENCE = "HCBS-SEED001";

    /**
     * Day offset for the anchor showing — first row in {@code ShowingRepository.findAll()} order.
     * Used by {@code BookingServiceTest} (London evening lower hall, £12 per seat).
     */
    public static final int ANCHOR_SHOWING_DAY_OFFSET = 3;

    private static final int[] FLAGSHIP_SCREEN_CAPACITIES = {50, 80, 100, 120};
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

    public HcbsTestDataSeeder(CityRepository cityRepository, CinemaRepository cinemaRepository,
                              ScreenRepository screenRepository, SeatRepository seatRepository,
                              FilmRepository filmRepository, ActorRepository actorRepository,
                              FilmActorRepository filmActorRepository, ShowingRepository showingRepository,
                              PriceRuleRepository priceRuleRepository, UserRepository userRepository,
                              BookingRepository bookingRepository, BookingSeatRepository bookingSeatRepository) {
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
    }

    @Transactional
    public void seedAll() {
        SeedContext ctx = new SeedContext();
        ctx.cities = seedCities();
        ctx.cinemas = seedCinemas(ctx.cities);
        ctx.screens = seedScreensAndSeats(ctx.cinemas);
        ctx.films = seedFilmsAndActors();
        seedPriceRules(ctx.cities);
        User staff = seedUsers();
        ctx.showings = seedShowings(ctx);
        seedSampleBookings(staff, ctx);
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
     * Flagship: 4 screens (50–120 seats). Secondary: 2×50 seats. Case: ≥2 cinemas/city, ≤6 screens/cinema.
     */
    private Map<String, Screen> seedScreensAndSeats(Map<String, Cinema> cinemas) {
        Map<String, Screen> screens = new LinkedHashMap<>();
        for (String cinemaKey : FLAGSHIP_CINEMA_KEYS) {
            Cinema cinema = cinemas.get(cinemaKey);
            for (int i = 0; i < FLAGSHIP_SCREEN_CAPACITIES.length; i++) {
                int capacity = FLAGSHIP_SCREEN_CAPACITIES[i];
                Screen screen = screenRepository.save(new Screen(cinema, i + 1, capacity));
                screens.put(screenKey(cinemaKey, i + 1), screen);
                createSeats(screen, capacity);
            }
        }
        for (String cinemaKey : SECONDARY_CINEMA_KEYS) {
            Cinema cinema = cinemas.get(cinemaKey);
            for (int screenNumber = 1; screenNumber <= 2; screenNumber++) {
                Screen screen = screenRepository.save(new Screen(cinema, screenNumber, 50));
                screens.put(screenKey(cinemaKey, screenNumber), screen);
                createSeats(screen, 50);
            }
        }
        return screens;
    }

    private void createSeats(Screen screen, int capacity) {
        int lowerCount = capacity / 2;
        int upperCount = capacity - lowerCount;
        for (int i = 1; i <= lowerCount; i++) {
            seatRepository.save(new Seat(screen, "L" + i, SeatArea.LOWER_HALL));
        }
        for (int i = 1; i <= upperCount; i++) {
            seatRepository.save(new Seat(screen, "U" + i, SeatArea.UPPER_GALLERY));
        }
    }

    private Map<String, Film> seedFilmsAndActors() {
        Map<String, Film> films = new LinkedHashMap<>();
        films.put("Skyline", filmRepository.save(new Film(
                "Skyline Run",
                "A fast-paced city thriller following a courier racing across London before dawn.",
                "Action", "12A", 4.4, 118,
                "/images/posters/skyline-run.svg")));
        films.put("Orbit", filmRepository.save(new Film(
                "Orbit Garden",
                "A science fiction story set around a lost orbital station and its last crew.",
                "Sci-Fi", "PG", 4.6, 132,
                "/images/posters/orbit-garden.svg")));
        films.put("Harbour", filmRepository.save(new Film(
                "Harbour Lights",
                "A warm drama about family reconciliation in a seaside town.",
                "Drama", "PG", 4.1, 105,
                "/images/posters/harbour-lights.svg")));
        films.put("Coral", filmRepository.save(new Film(
                "Coral Bay",
                "A family adventure on the Welsh coast with treasure hunts and summer storms.",
                "Family", "U", 4.0, 95,
                "/images/posters/coral-bay.svg")));
        films.put("Archive", filmRepository.save(new Film(
                "Archive Echo",
                "A documentary on restored cinema heritage and touring projectionists.",
                "Documentary", "PG", 4.3, 88,
                "/images/posters/archive-echo.svg")));

        Actor maya = actorRepository.save(new Actor("Maya Stone", "Lead actor"));
        Actor leo = actorRepository.save(new Actor("Leo Grant", "Supporting actor"));
        Actor nina = actorRepository.save(new Actor("Nina Clark", "Lead actor"));
        Actor sam = actorRepository.save(new Actor("Sam Reed", "Lead actor"));

        filmActorRepository.save(new FilmActor(films.get("Skyline"), maya));
        filmActorRepository.save(new FilmActor(films.get("Skyline"), leo));
        filmActorRepository.save(new FilmActor(films.get("Orbit"), nina));
        filmActorRepository.save(new FilmActor(films.get("Orbit"), maya));
        filmActorRepository.save(new FilmActor(films.get("Harbour"), leo));
        filmActorRepository.save(new FilmActor(films.get("Harbour"), sam));
        filmActorRepository.save(new FilmActor(films.get("Coral"), nina));
        filmActorRepository.save(new FilmActor(films.get("Archive"), sam));
        return films;
    }

    /** Lower-hall prices from the case study; upper gallery = lower + £2 per city and band. */
    private void seedPriceRules(Map<String, City> cities) {
        seedCityPrices(cities.get("London"), "10.00", "11.00", "12.00");
        seedCityPrices(cities.get("Birmingham"), "5.00", "6.00", "7.00");
        seedCityPrices(cities.get("Bristol"), "6.00", "7.00", "8.00");
        seedCityPrices(cities.get("Cardiff"), "5.00", "6.00", "7.00");
    }

    private void seedCityPrices(City city, String morning, String afternoon, String evening) {
        priceRuleRepository.save(new PriceRule(city, TimeBand.MORNING, SeatArea.LOWER_HALL, new BigDecimal(morning)));
        priceRuleRepository.save(new PriceRule(city, TimeBand.AFTERNOON, SeatArea.LOWER_HALL, new BigDecimal(afternoon)));
        priceRuleRepository.save(new PriceRule(city, TimeBand.EVENING, SeatArea.LOWER_HALL, new BigDecimal(evening)));
        priceRuleRepository.save(new PriceRule(city, TimeBand.MORNING, SeatArea.UPPER_GALLERY, new BigDecimal(morning).add(new BigDecimal("2.00"))));
        priceRuleRepository.save(new PriceRule(city, TimeBand.AFTERNOON, SeatArea.UPPER_GALLERY, new BigDecimal(afternoon).add(new BigDecimal("2.00"))));
        priceRuleRepository.save(new PriceRule(city, TimeBand.EVENING, SeatArea.UPPER_GALLERY, new BigDecimal(evening).add(new BigDecimal("2.00"))));
    }

    private User seedUsers() {
        userRepository.save(new User("staff", "demo", "Booking Staff", UserRole.BOOKING_STAFF));
        userRepository.save(new User("admin", "demo", "Admin User", UserRole.ADMIN));
        userRepository.save(new User("manager", "demo", "Manager User", UserRole.MANAGER));
        return userRepository.findFirstByRole(UserRole.BOOKING_STAFF).orElseThrow();
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
        List<ShowingSpec> specs = new ArrayList<>();

        // --- Anchor (must stay first): London Central Screen 1, evening, day+3, £12 lower hall ---
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

    private void seedSampleBookings(User staff, SeedContext ctx) {
        Showing cancellationDemo = ctx.showings.stream()
                .filter(s -> s.getShowDate().equals(LocalDate.now().plusDays(1)))
                .filter(s -> s.getFilm().getTitle().equals("Harbour Lights"))
                .filter(s -> s.getScreen().getCinema().getName().contains("London Central"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Seed cancellation showing missing"));

        Seat lowerSeat = seatRepository.findByScreenAndSeatArea(cancellationDemo.getScreen(), SeatArea.LOWER_HALL).get(0);
        BigDecimal ticketPrice = lookupLowerHallPrice(cancellationDemo);

        Booking booking = new Booking();
        booking.setBookingReference(SEED_BOOKING_REFERENCE);
        booking.setShowing(cancellationDemo);
        booking.setUser(staff);
        booking.setBookingDateTime(LocalDateTime.now().minusHours(2));
        booking.setNumberOfTickets(1);
        booking.setTotalCost(ticketPrice);
        booking.setStatus(BookingStatus.CONFIRMED);
        Booking saved = bookingRepository.save(booking);
        bookingSeatRepository.save(new BookingSeat(saved, lowerSeat, cancellationDemo, ticketPrice));
    }

    private BigDecimal lookupLowerHallPrice(Showing showing) {
        PriceRule rule = priceRuleRepository.findByCityAndTimeBandAndSeatArea(
                        showing.getScreen().getCinema().getCity(),
                        showing.getTimeBand(),
                        SeatArea.LOWER_HALL)
                .orElseThrow(() -> new IllegalStateException("No lower-hall price for seed booking"));
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
