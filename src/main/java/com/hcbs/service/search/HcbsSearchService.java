package com.hcbs.service.search;

import com.hcbs.dto.CinemaOption;
import com.hcbs.dto.CityOption;
import com.hcbs.dto.FilmCardDto;
import com.hcbs.dto.FilmCatalogFilter;
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

    public List<CityOption> listCities() {
        return filmListingService.listCities();
    }

    public List<CinemaOption> listCinemas(Long cityId) {
        return filmListingService.listCinemas(cityId);
    }
}
