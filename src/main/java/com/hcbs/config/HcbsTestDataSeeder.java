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
 * Seeds an H2 database that matches the HCBS case study and supports manual/automated test scenarios.
 * See TEST_DATABASE.md for scenario mapping.
 */
@Component
public class HcbsTestDataSeeder {

    /** Fixed reference for cancellation UI/manual tests (show date = today + 1). */
    public static final String SEED_BOOKING_REFERENCE = "HCBS-SEED001";

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
        Map<String, City> cities = seedCities();
        Map<String, Cinema> cinemas = seedCinemas(cities);
        seedScreensAndSeats(cinemas);
        Map<String, Film> films = seedFilmsAndActors();
        seedPriceRules(cities);
        User staff = seedUsers();
        List<Showing> showings = seedShowings(cinemas, films);
        seedSampleBooking(staff, showings);
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
     * Flagship sites: up to 4 screens, capacities 50–120. Secondary sites: 2×50 (case: ≥2 cinemas per city, ≤6 screens).
     */
    private void seedScreensAndSeats(Map<String, Cinema> cinemas) {
        int[] flagshipCapacities = {50, 80, 100, 120};
        for (String key : List.of("London-Central", "Birmingham-Bullring", "Bristol-Harbour", "Cardiff-Bay")) {
            Cinema cinema = cinemas.get(key);
            for (int i = 0; i < flagshipCapacities.length; i++) {
                Screen screen = screenRepository.save(new Screen(cinema, i + 1, flagshipCapacities[i]));
                createSeats(screen, flagshipCapacities[i]);
            }
        }
        for (String key : List.of("London-East", "Birmingham-NewStreet", "Bristol-Clifton", "Cardiff-Central")) {
            Cinema cinema = cinemas.get(key);
            for (int screenNumber = 1; screenNumber <= 2; screenNumber++) {
                Screen screen = screenRepository.save(new Screen(cinema, screenNumber, 50));
                createSeats(screen, 50);
            }
        }
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
                "Skyline Run", "A fast-paced city thriller.", "Action", "12A", 4.4, 118)));
        films.put("Orbit", filmRepository.save(new Film(
                "Orbit Garden", "A science fiction story set around a lost station.", "Sci-Fi", "PG", 4.6, 132)));
        films.put("Harbour", filmRepository.save(new Film(
                "Harbour Lights", "A warm drama about family and second chances.", "Drama", "PG", 4.1, 105)));
        films.put("Coral", filmRepository.save(new Film(
                "Coral Bay", "A family adventure on the coast.", "Family", "U", 4.0, 95)));
        films.put("Archive", filmRepository.save(new Film(
                "Archive Echo", "A documentary on restored cinema heritage.", "Documentary", "PG", 4.3, 88)));

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

    /** Lower-hall prices from the case study; upper gallery = lower + £2. */
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
     * Inserts showings so automated tests keep a stable first row: London Central, evening, day+3.
     * Additional rows cover booking-window edges and cancellation policy checks.
     */
    private List<Showing> seedShowings(Map<String, Cinema> cinemas, Map<String, Film> films) {
        List<Showing> created = new ArrayList<>();
        LocalDate today = LocalDate.now();

        Screen londonCentralScreen1 = screenRepository.findAll().stream()
                .filter(s -> s.getCinema().getCinemaId().equals(cinemas.get("London-Central").getCinemaId()))
                .filter(s -> s.getScreenNumber() == 1)
                .findFirst()
                .orElseThrow();

        // First persisted showing — used by BookingServiceTest (London evening £12 lower hall)
        created.add(showingRepository.save(new Showing(
                films.get("Skyline"), londonCentralScreen1,
                today.plusDays(3), LocalTime.of(18, 30), LocalTime.of(20, 30), TimeBand.EVENING)));

        Screen londonCentralScreen2 = screenRepository.findAll().stream()
                .filter(s -> s.getCinema().getCinemaId().equals(cinemas.get("London-Central").getCinemaId()))
                .filter(s -> s.getScreenNumber() == 2)
                .findFirst()
                .orElseThrow();
        created.add(showingRepository.save(new Showing(
                films.get("Orbit"), londonCentralScreen2,
                today.plusDays(3), LocalTime.of(10, 0), LocalTime.of(12, 15), TimeBand.MORNING)));

        Screen birminghamScreen1 = firstScreen(cinemas.get("Birmingham-Bullring"));
        created.add(showingRepository.save(new Showing(
                films.get("Harbour"), birminghamScreen1,
                today.plusDays(4), LocalTime.of(14, 0), LocalTime.of(15, 45), TimeBand.AFTERNOON)));

        Screen bristolScreen1 = firstScreen(cinemas.get("Bristol-Harbour"));
        created.add(showingRepository.save(new Showing(
                films.get("Coral"), bristolScreen1,
                today.plusDays(5), LocalTime.of(19, 0), LocalTime.of(20, 40), TimeBand.EVENING)));

        Screen cardiffScreen1 = firstScreen(cinemas.get("Cardiff-Bay"));
        created.add(showingRepository.save(new Showing(
                films.get("Archive"), cardiffScreen1,
                today.plusDays(2), LocalTime.of(11, 0), LocalTime.of(12, 30), TimeBand.MORNING)));

        // Booking window: last allowed day (today + 7)
        created.add(showingRepository.save(new Showing(
                films.get("Skyline"), londonCentralScreen1,
                today.plusDays(7), LocalTime.of(20, 0), LocalTime.of(22, 0), TimeBand.EVENING)));

        // Booking window: too far ahead (today + 8) — manual / TC_007 style checks
        created.add(showingRepository.save(new Showing(
                films.get("Orbit"), londonCentralScreen2,
                today.plusDays(8), LocalTime.of(18, 0), LocalTime.of(20, 0), TimeBand.EVENING)));

        // Cancellation: tomorrow (allowed if cancelled today)
        created.add(showingRepository.save(new Showing(
                films.get("Harbour"), londonCentralScreen2,
                today.plusDays(1), LocalTime.of(17, 0), LocalTime.of(19, 0), TimeBand.EVENING)));

        // Cancellation: same day (must reject)
        created.add(showingRepository.save(new Showing(
                films.get("Coral"), birminghamScreen1,
                today, LocalTime.of(19, 30), LocalTime.of(21, 0), TimeBand.EVENING)));

        // Past showing — cannot book
        created.add(showingRepository.save(new Showing(
                films.get("Archive"), cardiffScreen1,
                today.minusDays(1), LocalTime.of(14, 0), LocalTime.of(15, 30), TimeBand.AFTERNOON)));

        // Extra listings for filter demos (city / title)
        created.add(showingRepository.save(new Showing(
                films.get("Skyline"), firstScreen(cinemas.get("London-East")),
                today.plusDays(3), LocalTime.of(14, 30), LocalTime.of(16, 30), TimeBand.AFTERNOON)));

        return created;
    }

    private Screen firstScreen(Cinema cinema) {
        return screenRepository.findAll().stream()
                .filter(s -> s.getCinema().getCinemaId().equals(cinema.getCinemaId()))
                .filter(s -> s.getScreenNumber() == 1)
                .findFirst()
                .orElseThrow();
    }

    /** One confirmed booking on tomorrow's London showing for cancellation demos. */
    private void seedSampleBooking(User staff, List<Showing> showings) {
        Showing tomorrowLondon = showings.stream()
                .filter(s -> s.getShowDate().equals(LocalDate.now().plusDays(1)))
                .filter(s -> s.getScreen().getCinema().getName().contains("London Central"))
                .findFirst()
                .orElseThrow();

        Seat seat = seatRepository.findByScreenAndSeatArea(tomorrowLondon.getScreen(), SeatArea.LOWER_HALL).get(0);
        BigDecimal ticketPrice = new BigDecimal("12.00");

        Booking booking = new Booking();
        booking.setBookingReference(SEED_BOOKING_REFERENCE);
        booking.setShowing(tomorrowLondon);
        booking.setUser(staff);
        booking.setBookingDateTime(LocalDateTime.now().minusHours(2));
        booking.setNumberOfTickets(1);
        booking.setTotalCost(ticketPrice);
        booking.setStatus(BookingStatus.CONFIRMED);
        Booking saved = bookingRepository.save(booking);
        bookingSeatRepository.save(new BookingSeat(saved, seat, tomorrowLondon, ticketPrice));
    }
}
