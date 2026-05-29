package com.hcbs.config;

import com.hcbs.model.SeatArea;
import com.hcbs.model.Showing;
import com.hcbs.model.TimeBand;
import com.hcbs.repository.BookingRepository;
import com.hcbs.repository.FilmRepository;
import com.hcbs.repository.CinemaRepository;
import com.hcbs.repository.CityRepository;
import com.hcbs.repository.PriceRuleRepository;
import com.hcbs.repository.ScreenRepository;
import com.hcbs.repository.SeatRepository;
import com.hcbs.repository.ShowingRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:dataloader-test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class DataLoaderTest {

    @Autowired
    private CityRepository cityRepository;

    @Autowired
    private CinemaRepository cinemaRepository;

    @Autowired
    private ScreenRepository screenRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private PriceRuleRepository priceRuleRepository;

    @Autowired
    private ShowingRepository showingRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private FilmRepository filmRepository;

    @Test
    void createsAtLeastTwoCinemasForEveryCity() {
        cityRepository.findAll().forEach(city ->
                assertThat(cinemaRepository.findByCity(city))
                        .as(city.getName() + " cinemas")
                        .hasSizeGreaterThanOrEqualTo(2));
    }

    @Test
    void createsFlagshipScreensWithHundredSeatCapacity() {
        long flagshipScreens = screenRepository.findAll().stream()
                .filter(screen -> screen.getCinema().getName().contains("Central")
                        || screen.getCinema().getName().contains("Bullring")
                        || screen.getCinema().getName().contains("Harbour")
                        || screen.getCinema().getName().contains("Cardiff Bay"))
                .count();
        assertThat(flagshipScreens).isGreaterThanOrEqualTo(16);
        assertThat(screenRepository.findAll().stream().mapToInt(s -> s.getCapacity()).max().orElse(0))
                .isEqualTo(HcbsTestDataSeeder.SCREEN_CAPACITY);
    }

    @Test
    void createsTenByTenStandardSeatsPerScreen() {
        screenRepository.findAll().forEach(screen -> {
            assertThat(seatRepository.findByScreen(screen)).hasSize(HcbsTestDataSeeder.SCREEN_CAPACITY);
            assertThat(seatRepository.findByScreenAndSeatArea(screen, SeatArea.STANDARD))
                    .hasSize(HcbsTestDataSeeder.SCREEN_CAPACITY);
            assertThat(seatRepository.findByScreen(screen).getFirst().getSeatNumber()).isEqualTo("R01C01");
        });
    }

    @Test
    void createsTwelvePriceRulesAcrossFourCities() {
        assertThat(priceRuleRepository.count()).isEqualTo(12);
    }

    @Test
    void createsShowingsForBookingAndCancellationScenarios() {
        assertThat(showingRepository.count()).isGreaterThanOrEqualTo(18);
    }

    @Test
    void anchorShowingSupportsAutomatedBookingTests() {
        Showing anchor = showingRepository.findAll().getFirst();
        assertThat(anchor.getFilm().getTitle()).isEqualTo("Spirited Away");
        assertThat(anchor.getScreen().getCinema().getName()).contains("London Central");
        assertThat(anchor.getScreen().getScreenNumber()).isEqualTo(1);
        assertThat(anchor.getShowDate()).isEqualTo(LocalDate.now().plusDays(HcbsTestDataSeeder.ANCHOR_SHOWING_DAY_OFFSET));
        assertThat(anchor.getTimeBand()).isEqualTo(TimeBand.EVENING);
    }

    @Test
    void createsSeedBookingForCancellationDemo() {
        assertThat(bookingRepository.findByBookingReference(HcbsTestDataSeeder.SEED_BOOKING_REFERENCE)).isPresent();
    }

    @Test
    void persistsPosterImageUrlOnEveryFilm() {
        assertThat(filmRepository.findAll()).isNotEmpty();
        filmRepository.findAll().forEach(film -> {
            assertThat(film.getPosterUrl())
                    .as(film.getTitle())
                    .isNotBlank()
                    .startsWith("/images/posters/")
                    .endsWith(".jpg");
        });
    }
}
