package com.hcbs.service.listing;

import com.hcbs.dto.ShowingRow;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:listing-service-test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class FilmListingServiceTest {

    @Autowired
    private FilmListingService filmListingService;

    @Test
    void searchReturnsShowingRowsWithDescription() {
        List<ShowingRow> rows = filmListingService.searchShowings(null, null, LocalDate.now().plusDays(3), null);

        assertThat(rows).isNotEmpty();
        assertThat(rows.get(0).description()).isNotBlank();
        assertThat(rows.get(0).availableSeats()).isGreaterThan(0);
    }
}
