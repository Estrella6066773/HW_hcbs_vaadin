package com.hcbs.config;

import com.hcbs.model.SeatArea;
import com.hcbs.repository.BookingRepository;
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

    @Test
    void createsAtLeastTwoCinemasForEveryCity() {
        cityRepository.findAll().forEach(city ->
                assertThat(cinemaRepository.findByCity(city))
                        .as(city.getName() + " cinemas")
                        .hasSizeGreaterThanOrEqualTo(2));
    }

    @Test
    void createsFlagshipScreensWithCapacityBetweenFiftyAndOneTwenty() {
        long flagshipScreens = screenRepository.findAll().stream()
                .filter(screen -> screen.getCinema().getName().contains("Central")
                        || screen.getCinema().getName().contains("Bullring")
                        || screen.getCinema().getName().contains("Harbour")
                        || screen.getCinema().getName().contains("Cardiff Bay"))
                .count();
        assertThat(flagshipScreens).isGreaterThanOrEqualTo(16);
        assertThat(screenRepository.findAll().stream().mapToInt(s -> s.getCapacity()).max().orElse(0))
                .isEqualTo(120);
    }

    @Test
    void createsFiftySeatsSplitAcrossLowerHallAndUpperGallery() {
        screenRepository.findAll().stream()
                .filter(screen -> screen.getCapacity() == 50)
                .forEach(screen -> {
                    assertThat(seatRepository.findByScreen(screen)).hasSize(50);
                    assertThat(seatRepository.findByScreenAndSeatArea(screen, SeatArea.LOWER_HALL)).hasSize(25);
                    assertThat(seatRepository.findByScreenAndSeatArea(screen, SeatArea.UPPER_GALLERY)).hasSize(25);
                });
    }

    @Test
    void createsTwentyFourPriceRulesAcrossFourCities() {
        assertThat(priceRuleRepository.count()).isEqualTo(24);
    }

    @Test
    void createsShowingsForBookingAndCancellationScenarios() {
        assertThat(showingRepository.count()).isGreaterThanOrEqualTo(10);
    }

    @Test
    void createsSeedBookingForCancellationDemo() {
        assertThat(bookingRepository.findByBookingReference(HcbsTestDataSeeder.SEED_BOOKING_REFERENCE)).isPresent();
    }
}
