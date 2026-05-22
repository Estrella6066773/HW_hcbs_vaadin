package com.hcbs.service.catalog;

import com.hcbs.dto.FilmCardDto;
import com.hcbs.dto.FilmCatalogFilter;
import com.hcbs.dto.FilmDetailDto;
import com.hcbs.dto.ShowingRow;
import com.hcbs.model.Film;
import com.hcbs.model.Showing;
import com.hcbs.repository.BookingSeatRepository;
import com.hcbs.repository.FilmActorRepository;
import com.hcbs.repository.FilmRepository;
import com.hcbs.repository.ShowingRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class FilmCatalogService {
    private static final String DEFAULT_POSTER = "/images/posters/default.svg";

    private final FilmRepository filmRepository;
    private final ShowingRepository showingRepository;
    private final FilmActorRepository filmActorRepository;
    private final BookingSeatRepository bookingSeatRepository;

    public FilmCatalogService(FilmRepository filmRepository, ShowingRepository showingRepository,
                              FilmActorRepository filmActorRepository,
                              BookingSeatRepository bookingSeatRepository) {
        this.filmRepository = filmRepository;
        this.showingRepository = showingRepository;
        this.filmActorRepository = filmActorRepository;
        this.bookingSeatRepository = bookingSeatRepository;
    }

    public List<FilmCardDto> listRecommendedFilms() {
        return filmRepository.findAll().stream()
                .sorted(Comparator.comparing(Film::getRating).reversed())
                .map(this::toCard)
                .toList();
    }

    public List<FilmCardDto> search(FilmCatalogFilter filter) {
        if (filter.isEmpty()) {
            return listRecommendedFilms();
        }
        String lower = filter.keyword().toLowerCase(Locale.ROOT);
        return filmRepository.findAll().stream()
                .filter(film -> matchesSearch(film, lower))
                .sorted(Comparator.comparing(Film::getRating).reversed())
                .map(this::toCard)
                .toList();
    }

    public List<FilmCardDto> searchFilms(String query) {
        return search(FilmCatalogFilter.of(query));
    }

    public List<FilmCardDto> listFilmCards(List<Long> filmIds) {
        if (filmIds == null || filmIds.isEmpty()) {
            return List.of();
        }
        Map<Long, Film> byId = new LinkedHashMap<>();
        filmRepository.findAllById(filmIds).forEach(film -> byId.put(film.getFilmId(), film));
        return filmIds.stream()
                .map(byId::get)
                .filter(film -> film != null)
                .sorted(Comparator.comparing(Film::getRating).reversed())
                .map(this::toCard)
                .toList();
    }

    private boolean matchesSearch(Film film, String lowerQuery) {
        return containsIgnoreCase(film.getTitle(), lowerQuery)
                || containsIgnoreCase(film.getGenre(), lowerQuery)
                || containsIgnoreCase(film.getDescription(), lowerQuery);
    }

    private boolean containsIgnoreCase(String value, String lowerQuery) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(lowerQuery);
    }

    public FilmDetailDto getFilmDetail(Long filmId) {
        Film film = filmRepository.findById(filmId)
                .orElseThrow(() -> new IllegalArgumentException("Film not found: " + filmId));
        LocalDate today = LocalDate.now();
        List<ShowingRow> upcoming = showingRepository.findByFilm(film).stream()
                .filter(showing -> !showing.getShowDate().isBefore(today))
                .sorted(Comparator.comparing(Showing::getShowDate).thenComparing(Showing::getStartTime))
                .map(this::toShowingRow)
                .toList();
        return new FilmDetailDto(
                film.getFilmId(),
                film.getTitle(),
                resolvePosterUrl(film),
                film.getDescription(),
                film.getGenre(),
                film.getAgeRating(),
                film.getRating(),
                film.getDurationMinutes(),
                formatActors(film),
                upcoming);
    }

    private FilmCardDto toCard(Film film) {
        return new FilmCardDto(
                film.getFilmId(),
                film.getTitle(),
                resolvePosterUrl(film),
                film.getGenre(),
                film.getAgeRating(),
                film.getRating());
    }

    private String resolvePosterUrl(Film film) {
        if (film.getPosterUrl() == null || film.getPosterUrl().isBlank()) {
            return DEFAULT_POSTER;
        }
        return film.getPosterUrl();
    }

    private ShowingRow toShowingRow(Showing showing) {
        int capacity = showing.getScreen().getCapacity();
        long booked = bookingSeatRepository.countActiveReservationsForShowing(showing);
        return new ShowingRow(
                showing.getShowingId(),
                showing.getFilm().getFilmId(),
                showing.getFilm().getTitle(),
                showing.getFilm().getDescription(),
                formatActors(showing.getFilm()),
                showing.getFilm().getGenre(),
                showing.getFilm().getAgeRating(),
                showing.getScreen().getCinema().getName(),
                showing.getScreen().getScreenNumber(),
                showing.getShowDate(),
                showing.getStartTime(),
                showing.getEndTime(),
                showing.getTimeBand(),
                capacity - booked);
    }

    private String formatActors(Film film) {
        return filmActorRepository.findByFilm(film).stream()
                .map(link -> link.getActor().getFullName())
                .reduce((left, right) -> left + ", " + right)
                .orElse("");
    }
}
