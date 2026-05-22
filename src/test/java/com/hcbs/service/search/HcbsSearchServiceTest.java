package com.hcbs.service.search;

import com.hcbs.dto.FilmCatalogFilter;
import com.hcbs.dto.ShowingListingFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:hcbs-search-test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class HcbsSearchServiceTest {

    @Autowired
    private HcbsSearchService searchService;

    @Test
    void searchesFilmCatalogByFilter() {
        assertThat(searchService.searchFilms(FilmCatalogFilter.of("Skyline"))).hasSize(1);
        assertThat(searchService.searchFilms(FilmCatalogFilter.of(""))).hasSize(5);
    }

    @Test
    void searchesShowingsByFilter() {
        var result = searchService.searchShowings(
                ShowingListingFilter.of(null, null, LocalDate.now().plusDays(3), null));

        assertThat(result.showings()).isNotEmpty();
        assertThat(result.availableSeats()).isGreaterThan(0);
    }
}
