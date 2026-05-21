package com.hcbs.service.listing;

import com.hcbs.dto.CinemaOption;
import com.hcbs.dto.CityOption;
import com.hcbs.dto.ShowingRow;
import com.hcbs.model.Showing;
import com.hcbs.repository.BookingSeatRepository;
import com.hcbs.repository.CinemaRepository;
import com.hcbs.repository.CityRepository;
import com.hcbs.repository.FilmActorRepository;
import com.hcbs.repository.ShowingRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class FilmListingService {
    private final CityRepository cityRepository;
    private final CinemaRepository cinemaRepository;
    private final ShowingRepository showingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final FilmActorRepository filmActorRepository;

    public FilmListingService(CityRepository cityRepository, CinemaRepository cinemaRepository,
                              ShowingRepository showingRepository, BookingSeatRepository bookingSeatRepository,
                              FilmActorRepository filmActorRepository) {
        this.cityRepository = cityRepository;
        this.cinemaRepository = cinemaRepository;
        this.showingRepository = showingRepository;
        this.bookingSeatRepository = bookingSeatRepository;
        this.filmActorRepository = filmActorRepository;
    }

    public List<CityOption> listCities() {
        return cityRepository.findAll().stream()
                .map(city -> new CityOption(city.getCityId(), city.getName()))
                .toList();
    }

    public List<CinemaOption> listCinemas(Long cityId) {
        if (cityId == null) {
            return cinemaRepository.findAll().stream()
                    .map(cinema -> new CinemaOption(cinema.getCinemaId(), cinema.getName()))
                    .toList();
        }
        return cityRepository.findById(cityId)
                .map(city -> cinemaRepository.findByCity(city).stream()
                        .map(cinema -> new CinemaOption(cinema.getCinemaId(), cinema.getName()))
                        .toList())
                .orElse(List.of());
    }

    public List<ShowingRow> searchShowings(Long cityId, Long cinemaId, LocalDate date, String filmTitle) {
        return showingRepository.searchShowings(cityId, cinemaId, date, filmTitle).stream()
                .map(this::toShowingRow)
                .toList();
    }

    public long countAvailableSeatsAcross(List<ShowingRow> rows) {
        return rows.stream().mapToLong(ShowingRow::availableSeats).sum();
    }

    private ShowingRow toShowingRow(Showing showing) {
        int capacity = showing.getScreen().getCapacity();
        long booked = bookingSeatRepository.countActiveReservationsForShowing(showing);
        return new ShowingRow(
                showing.getShowingId(),
                showing.getFilm().getTitle(),
                showing.getFilm().getDescription(),
                formatActors(showing),
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

    private String formatActors(Showing showing) {
        return filmActorRepository.findByFilm(showing.getFilm()).stream()
                .map(link -> link.getActor().getFullName())
                .reduce((left, right) -> left + ", " + right)
                .orElse("");
    }
}
