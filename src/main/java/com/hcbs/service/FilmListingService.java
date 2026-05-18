package com.hcbs.service;

import com.hcbs.model.Cinema;
import com.hcbs.model.City;
import com.hcbs.model.Film;
import com.hcbs.model.Showing;
import com.hcbs.repository.BookingSeatRepository;
import com.hcbs.repository.CinemaRepository;
import com.hcbs.repository.CityRepository;
import com.hcbs.repository.FilmActorRepository;
import com.hcbs.repository.ShowingRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
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

    public List<City> getAllCities() {
        return cityRepository.findAll();
    }

    public List<Cinema> getCinemasByCity(City city) {
        if (city == null) {
            return cinemaRepository.findAll();
        }
        return cinemaRepository.findByCity(city);
    }

    public List<Showing> searchShowings(City city, Cinema cinema, LocalDate date, String filmTitle) {
        return showingRepository.findAll().stream()
                .filter(showing -> city == null || showing.getScreen().getCinema().getCity().equals(city))
                .filter(showing -> cinema == null || showing.getScreen().getCinema().equals(cinema))
                .filter(showing -> date == null || showing.getShowDate().equals(date))
                .filter(showing -> filmTitle == null || filmTitle.isBlank()
                        || showing.getFilm().getTitle().toLowerCase().contains(filmTitle.toLowerCase()))
                .sorted(Comparator.comparing(Showing::getShowDate).thenComparing(Showing::getStartTime))
                .toList();
    }

    public long countAvailableSeats(Showing showing) {
        int capacity = showing.getScreen().getCapacity();
        long bookedSeats = bookingSeatRepository.findByShowing(showing).size();
        return capacity - bookedSeats;
    }

    public String getFilmActors(Film film) {
        return filmActorRepository.findByFilm(film).stream()
                .map(link -> link.getActor().getFullName())
                .reduce((left, right) -> left + ", " + right)
                .orElse("");
    }
}
