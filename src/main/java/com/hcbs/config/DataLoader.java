package com.hcbs.config;

import com.hcbs.model.Actor;
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
import com.hcbs.repository.CinemaRepository;
import com.hcbs.repository.CityRepository;
import com.hcbs.repository.FilmActorRepository;
import com.hcbs.repository.FilmRepository;
import com.hcbs.repository.PriceRuleRepository;
import com.hcbs.repository.ScreenRepository;
import com.hcbs.repository.SeatRepository;
import com.hcbs.repository.ShowingRepository;
import com.hcbs.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Component
public class DataLoader implements CommandLineRunner {
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

    public DataLoader(CityRepository cityRepository, CinemaRepository cinemaRepository,
                      ScreenRepository screenRepository, SeatRepository seatRepository,
                      FilmRepository filmRepository, ActorRepository actorRepository,
                      FilmActorRepository filmActorRepository, ShowingRepository showingRepository,
                      PriceRuleRepository priceRuleRepository, UserRepository userRepository) {
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
    }

    @Override
    public void run(String... args) {
        if (cityRepository.count() > 0) {
            return;
        }

        City london = cityRepository.save(new City("London"));
        City birmingham = cityRepository.save(new City("Birmingham"));
        City bristol = cityRepository.save(new City("Bristol"));
        City cardiff = cityRepository.save(new City("Cardiff"));

        Cinema londonCentral = cinemaRepository.save(new Cinema(london, "HC London Central", "Westminster"));
        Cinema londonEast = cinemaRepository.save(new Cinema(london, "HC London East", "Stratford"));
        Cinema birminghamBullring = cinemaRepository.save(new Cinema(birmingham, "HC Birmingham Bullring", "Bullring"));
        Cinema birminghamNewStreet = cinemaRepository.save(new Cinema(birmingham, "HC Birmingham New Street", "New Street"));
        Cinema bristolHarbour = cinemaRepository.save(new Cinema(bristol, "HC Bristol Harbour", "Harbourside"));
        Cinema bristolClifton = cinemaRepository.save(new Cinema(bristol, "HC Bristol Clifton", "Clifton"));
        Cinema cardiffBay = cinemaRepository.save(new Cinema(cardiff, "HC Cardiff Bay", "Cardiff Bay"));
        Cinema cardiffCentral = cinemaRepository.save(new Cinema(cardiff, "HC Cardiff Central", "Central"));

        List<Cinema> cinemas = List.of(
                londonCentral, londonEast,
                birminghamBullring, birminghamNewStreet,
                bristolHarbour, bristolClifton,
                cardiffBay, cardiffCentral);
        for (Cinema cinema : cinemas) {
            Screen screenOne = screenRepository.save(new Screen(cinema, 1, 50));
            Screen screenTwo = screenRepository.save(new Screen(cinema, 2, 50));
            createSeats(screenOne);
            createSeats(screenTwo);
        }

        Film skyline = filmRepository.save(new Film("Skyline Run", "A fast-paced city thriller.", "Action", "12A", 4.4, 118));
        Film orbit = filmRepository.save(new Film("Orbit Garden", "A science fiction story set around a lost station.", "Sci-Fi", "PG", 4.6, 132));
        Film harbour = filmRepository.save(new Film("Harbour Lights", "A warm drama about family and second chances.", "Drama", "PG", 4.1, 105));

        Actor maya = actorRepository.save(new Actor("Maya Stone", "Lead actor"));
        Actor leo = actorRepository.save(new Actor("Leo Grant", "Supporting actor"));
        Actor nina = actorRepository.save(new Actor("Nina Clark", "Lead actor"));
        filmActorRepository.save(new FilmActor(skyline, maya));
        filmActorRepository.save(new FilmActor(skyline, leo));
        filmActorRepository.save(new FilmActor(orbit, nina));
        filmActorRepository.save(new FilmActor(orbit, maya));
        filmActorRepository.save(new FilmActor(harbour, leo));

        priceRules(london, "10.00", "11.00", "12.00");
        priceRules(birmingham, "5.00", "6.00", "7.00");
        priceRules(bristol, "6.00", "7.00", "8.00");
        priceRules(cardiff, "5.00", "6.00", "7.00");

        List<Screen> screens = screenRepository.findAll();
        LocalDate demoDate = LocalDate.now().plusDays(3);
        showingRepository.save(new Showing(skyline, screens.get(0), demoDate, LocalTime.of(18, 30), LocalTime.of(20, 30), TimeBand.EVENING));
        showingRepository.save(new Showing(orbit, screens.get(1), demoDate, LocalTime.of(10, 0), LocalTime.of(12, 15), TimeBand.MORNING));
        showingRepository.save(new Showing(harbour, screens.get(2), demoDate.plusDays(1), LocalTime.of(14, 0), LocalTime.of(15, 45), TimeBand.AFTERNOON));
        showingRepository.save(new Showing(skyline, screens.get(4), demoDate.plusDays(2), LocalTime.of(19, 0), LocalTime.of(21, 0), TimeBand.EVENING));

        userRepository.save(new User("staff", "demo", "Booking Staff", UserRole.BOOKING_STAFF));
        userRepository.save(new User("admin", "demo", "Admin User", UserRole.ADMIN));
        userRepository.save(new User("manager", "demo", "Manager User", UserRole.MANAGER));
    }

    private void createSeats(Screen screen) {
        for (int i = 1; i <= 25; i++) {
            seatRepository.save(new Seat(screen, "L" + i, SeatArea.LOWER_HALL));
        }
        for (int i = 1; i <= 25; i++) {
            seatRepository.save(new Seat(screen, "U" + i, SeatArea.UPPER_GALLERY));
        }
    }

    private void priceRules(City city, String morning, String afternoon, String evening) {
        priceRuleRepository.save(new PriceRule(city, TimeBand.MORNING, SeatArea.LOWER_HALL, new BigDecimal(morning)));
        priceRuleRepository.save(new PriceRule(city, TimeBand.AFTERNOON, SeatArea.LOWER_HALL, new BigDecimal(afternoon)));
        priceRuleRepository.save(new PriceRule(city, TimeBand.EVENING, SeatArea.LOWER_HALL, new BigDecimal(evening)));
        priceRuleRepository.save(new PriceRule(city, TimeBand.MORNING, SeatArea.UPPER_GALLERY, new BigDecimal(morning).add(new BigDecimal("2.00"))));
        priceRuleRepository.save(new PriceRule(city, TimeBand.AFTERNOON, SeatArea.UPPER_GALLERY, new BigDecimal(afternoon).add(new BigDecimal("2.00"))));
        priceRuleRepository.save(new PriceRule(city, TimeBand.EVENING, SeatArea.UPPER_GALLERY, new BigDecimal(evening).add(new BigDecimal("2.00"))));
    }
}
