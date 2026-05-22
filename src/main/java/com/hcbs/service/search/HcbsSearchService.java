package com.hcbs.service.search;

import com.hcbs.dto.CinemaOption;
import com.hcbs.dto.CityOption;
import com.hcbs.dto.FilmCardDto;
import com.hcbs.dto.FilmCatalogFilter;
import com.hcbs.dto.FilmDetailDto;
import com.hcbs.dto.ShowingListingFilter;
import com.hcbs.dto.ShowingListingResult;
import com.hcbs.dto.ShowingRow;
import com.hcbs.service.catalog.FilmCatalogService;
import com.hcbs.service.listing.FilmListingService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Unified search entry point for catalog and showing listing filters.
 */
@Service
public class HcbsSearchService {

    private final FilmCatalogService filmCatalogService;
    private final FilmListingService filmListingService;

    public HcbsSearchService(FilmCatalogService filmCatalogService, FilmListingService filmListingService) {
        this.filmCatalogService = filmCatalogService;
        this.filmListingService = filmListingService;
    }

    public List<FilmCardDto> searchFilms(FilmCatalogFilter filter) {
        return filmCatalogService.search(filter);
    }

    public ShowingListingResult searchShowings(ShowingListingFilter filter) {
        List<ShowingRow> showings = filmListingService.search(filter);
        long seats = filmListingService.countAvailableSeatsAcross(showings);
        return new ShowingListingResult(showings, seats);
    }

    public ShowingListingResult searchHomeShowings(ShowingListingFilter filter) {
        List<ShowingRow> showings = filmListingService.searchUpcoming(filter);
        long seats = filmListingService.countAvailableSeatsAcross(showings);
        return new ShowingListingResult(showings, seats);
    }

    /**
     * Films that have at least one upcoming showing matching the filter (browse by movie, not by session row).
     */
    public List<FilmCardDto> searchFilmsByShowings(ShowingListingFilter filter) {
        List<Long> filmIds = filmListingService.searchUpcoming(filter).stream()
                .map(ShowingRow::filmId)
                .distinct()
                .toList();
        return filmCatalogService.listFilmCards(filmIds);
    }

    public FilmDetailDto getFilmDetail(Long filmId, ShowingListingFilter filter) {
        FilmDetailDto detail = filmCatalogService.getFilmDetail(filmId);
        if (filter == null || filter.isEmpty()) {
            return detail;
        }
        var allowedIds = filmListingService.searchUpcoming(filter).stream()
                .filter(row -> row.filmId().equals(filmId))
                .map(ShowingRow::showingId)
                .collect(java.util.stream.Collectors.toSet());
        List<ShowingRow> filtered = detail.upcomingShowings().stream()
                .filter(row -> allowedIds.contains(row.showingId()))
                .toList();
        return new FilmDetailDto(
                detail.filmId(),
                detail.title(),
                detail.posterUrl(),
                detail.description(),
                detail.genre(),
                detail.ageRating(),
                detail.rating(),
                detail.durationMinutes(),
                detail.actors(),
                filtered);
    }

    public List<CityOption> listCities() {
        return filmListingService.listCities();
    }

    public List<CinemaOption> listCinemas(Long cityId) {
        return filmListingService.listCinemas(cityId);
    }
}
