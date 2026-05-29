package com.hcbs.service.catalog;

import com.hcbs.config.FilmPosterCatalog;
import com.hcbs.dto.FilmCardDto;
import com.hcbs.dto.FilmDetailDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:film-catalog-test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class FilmCatalogServiceTest {

    @Autowired
    private FilmCatalogService filmCatalogService;

    @Autowired
    private PosterResourceService posterResourceService;

    @Test
    void listsRecommendedFilmsWithPosters() {
        assertThat(filmCatalogService.listRecommendedFilms())
                .hasSize(12)
                .allSatisfy(card -> {
                    assertThat(card.posterUrl()).startsWith(FilmPosterCatalog.BASE);
                    assertThat(card.posterUrl()).doesNotEndWith(".svg");
                    assertThat(card.title()).isNotBlank();
                });
        assertThat(posterResourceService.isAvailable(FilmPosterCatalog.SOLITUDE)).isFalse();
        assertThat(posterResourceService.isAvailable(FilmPosterCatalog.SPIRITED_AWAY)).isTrue();
    }

    @Test
    void loadsFilmDetailWithUpcomingShowings() {
        FilmCardDto first = filmCatalogService.listRecommendedFilms().get(0);
        FilmDetailDto detail = filmCatalogService.getFilmDetail(first.filmId());

        assertThat(detail.title()).isEqualTo(first.title());
        assertThat(detail.description()).isNotBlank();
        assertThat(detail.actors()).isNotBlank();
        assertThat(detail.upcomingShowings()).isNotEmpty();
    }

    @Test
    void searchFilmsMatchesTitleGenreOrDescription() {
        assertThat(filmCatalogService.searchFilms("Spirited")).hasSize(1)
                .first()
                .extracting(FilmCardDto::title)
                .isEqualTo("Spirited Away");

        assertThat(filmCatalogService.searchFilms("sci-fi")).isNotEmpty();
        assertThat(filmCatalogService.searchFilms("")).hasSize(12);
        assertThat(filmCatalogService.searchFilms("   ")).hasSize(12);
        assertThat(filmCatalogService.searchFilms("thriller")).hasSize(2);
        assertThat(filmCatalogService.searchFilms("no-such-film-xyz")).isEmpty();
    }

    @Test
    void rejectsUnknownFilm() {
        assertThatThrownBy(() -> filmCatalogService.getFilmDetail(99999L))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
