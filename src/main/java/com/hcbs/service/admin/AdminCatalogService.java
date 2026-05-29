package com.hcbs.service.admin;

import com.hcbs.dto.FilmAdminRow;
import com.hcbs.dto.ShowingRow;
import com.hcbs.model.Cinema;
import com.hcbs.model.Film;
import com.hcbs.model.FilmActor;
import com.hcbs.model.Screen;
import com.hcbs.model.Showing;
import com.hcbs.model.TimeBand;
import com.hcbs.model.User;
import com.hcbs.model.UserRole;
import com.hcbs.model.UserStatus;
import com.hcbs.repository.BookingRepository;
import com.hcbs.repository.BookingSeatRepository;
import com.hcbs.repository.CinemaRepository;
import com.hcbs.repository.FilmActorRepository;
import com.hcbs.repository.FilmRepository;
import com.hcbs.repository.ScreenRepository;
import com.hcbs.repository.ShowingRepository;
import com.hcbs.repository.UserRepository;
import com.hcbs.security.CurrentUserService;
import com.hcbs.util.PhoneNumbers;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.time.LocalDate;
import java.time.LocalTime;

@Service
public class AdminCatalogService {

    private final FilmRepository filmRepository;
    private final UserRepository userRepository;
    private final ShowingRepository showingRepository;
    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final CinemaRepository cinemaRepository;
    private final FilmActorRepository filmActorRepository;
    private final ScreenRepository screenRepository;
    private final CurrentUserService currentUserService;

    public AdminCatalogService(FilmRepository filmRepository, UserRepository userRepository,
                               ShowingRepository showingRepository, BookingRepository bookingRepository,
                               BookingSeatRepository bookingSeatRepository, CinemaRepository cinemaRepository,
                               FilmActorRepository filmActorRepository,
                               ScreenRepository screenRepository,
                               CurrentUserService currentUserService) {
        this.filmRepository = filmRepository;
        this.userRepository = userRepository;
        this.showingRepository = showingRepository;
        this.bookingRepository = bookingRepository;
        this.bookingSeatRepository = bookingSeatRepository;
        this.cinemaRepository = cinemaRepository;
        this.filmActorRepository = filmActorRepository;
        this.screenRepository = screenRepository;
        this.currentUserService = currentUserService;
    }

    public List<FilmAdminRow> listFilms() {
        requireEmployee();
        return filmRepository.findAll().stream().map(this::toRow).toList();
    }

    public List<ShowingRow> listShowings() {
        requireEmployee();
        return showingRepository.searchShowings(null, null, null, null).stream()
                .map(this::toShowingRow)
                .toList();
    }

    public List<Screen> listScreens() {
        requireEmployee();
        return screenRepository.findAll();
    }

    public List<Cinema> listCinemas() {
        requireEmployee();
        return cinemaRepository.findAll();
    }

    @Transactional
    public FilmAdminRow saveFilm(FilmAdminRow row) {
        requireEmployee();
        Film film = row.getFilmId() == null
                ? new Film()
                : filmRepository.findById(row.getFilmId())
                        .orElseThrow(() -> new IllegalArgumentException("Film not found: " + row.getFilmId()));
        film.setTitle(row.getTitle());
        film.setGenre(row.getGenre());
        film.setAgeRating(row.getAgeRating());
        film.setRating(row.getRating());
        film.setDurationMinutes(row.getDurationMinutes());
        String posterUrl = row.getPosterUrl();
        if (posterUrl == null || posterUrl.isBlank()) {
            throw new IllegalArgumentException("Poster image URL is required");
        }
        film.setPosterUrl(posterUrl.trim());
        if (film.getDescription() == null) {
            film.setDescription("");
        }
        return toRow(filmRepository.save(film));
    }

    @Transactional
    public ShowingRow addShowing(Long filmId, Long screenId, LocalDate showDate, LocalTime startTime) {
        requireEmployee();
        if (showDate == null || startTime == null) {
            throw new IllegalArgumentException("Date and time are required");
        }
        Film film = filmRepository.findById(filmId)
                .orElseThrow(() -> new IllegalArgumentException("Film not found: " + filmId));
        Screen screen = screenRepository.findById(screenId)
                .orElseThrow(() -> new IllegalArgumentException("Screen not found: " + screenId));
        Showing showing = new Showing(
                film,
                screen,
                showDate,
                startTime,
                startTime.plusMinutes(film.getDurationMinutes()),
                timeBandFor(startTime));
        ensureNoScreenOverlap(null, screen, showDate, showing.getStartTime(), showing.getEndTime());
        return toShowingRow(showingRepository.save(showing));
    }

    @Transactional
    public ShowingRow updateShowing(Long showingId, Long filmId, Long screenId, LocalDate showDate, LocalTime startTime) {
        requireEmployee();
        if (showDate == null || startTime == null) {
            throw new IllegalArgumentException("Date and time are required");
        }
        Showing showing = showingRepository.findById(showingId)
                .orElseThrow(() -> new IllegalArgumentException("Showing not found: " + showingId));
        if (bookingRepository.existsByShowing(showing)) {
            throw new IllegalStateException("This showing has bookings and cannot be changed");
        }
        Film film = filmRepository.findById(filmId)
                .orElseThrow(() -> new IllegalArgumentException("Film not found: " + filmId));
        Screen screen = screenRepository.findById(screenId)
                .orElseThrow(() -> new IllegalArgumentException("Screen not found: " + screenId));
        showing.setFilm(film);
        showing.setScreen(screen);
        showing.setShowDate(showDate);
        showing.setStartTime(startTime);
        showing.setEndTime(startTime.plusMinutes(film.getDurationMinutes()));
        showing.setTimeBand(timeBandFor(startTime));
        ensureNoScreenOverlap(showing.getShowingId(), screen, showDate, showing.getStartTime(), showing.getEndTime());
        return toShowingRow(showingRepository.save(showing));
    }

    @Transactional
    public void deleteShowing(Long showingId) {
        requireEmployee();
        Showing showing = showingRepository.findById(showingId)
                .orElseThrow(() -> new IllegalArgumentException("Showing not found: " + showingId));
        if (bookingRepository.existsByShowing(showing)) {
            throw new IllegalStateException("This showing has bookings and cannot be deleted");
        }
        bookingSeatRepository.deleteAll(bookingSeatRepository.findByShowing(showing));
        showingRepository.delete(showing);
    }

    @Transactional
    public void deleteFilm(Long filmId) {
        requireEmployee();
        Film film = filmRepository.findById(filmId)
                .orElseThrow(() -> new IllegalArgumentException("Film not found: " + filmId));
        List<Showing> showings = showingRepository.findByFilm(film);
        if (showings.stream().anyMatch(bookingRepository::existsByShowing)) {
            throw new IllegalStateException("This film has booked showings and cannot be deleted");
        }
        for (Showing showing : showings) {
            bookingSeatRepository.deleteAll(bookingSeatRepository.findByShowing(showing));
        }
        showingRepository.deleteAll(showings);
        filmActorRepository.deleteAll(filmActorRepository.findByFilm(film));
        filmRepository.delete(film);
    }

    private FilmAdminRow toRow(Film film) {
        return new FilmAdminRow(
                film.getFilmId(),
                film.getTitle(),
                film.getGenre(),
                film.getAgeRating(),
                film.getRating(),
                film.getDurationMinutes(),
                film.getPosterUrl());
    }

    private ShowingRow toShowingRow(Showing showing) {
        long reservedSeats = bookingSeatRepository.countActiveReservationsForShowing(showing);
        return new ShowingRow(
                showing.getShowingId(),
                showing.getFilm().getFilmId(),
                showing.getFilm().getTitle(),
                showing.getFilm().getDescription(),
                formatActors(showing.getFilm()),
                showing.getFilm().getGenre(),
                showing.getFilm().getAgeRating(),
                showing.getScreen().getCinema().getName(),
                showing.getScreen().getScreenId(),
                showing.getScreen().getScreenNumber(),
                showing.getShowDate(),
                showing.getStartTime(),
                showing.getEndTime(),
                showing.getTimeBand(),
                showing.getScreen().getCapacity() - reservedSeats);
    }

    private String formatActors(Film film) {
        return filmActorRepository.findByFilm(film).stream()
                .map(FilmActor::getActor)
                .map(actor -> actor.getFullName())
                .sorted()
                .reduce((left, right) -> left + ", " + right)
                .orElse("");
    }

    private TimeBand timeBandFor(LocalTime time) {
        if (time.isBefore(LocalTime.NOON)) {
            return TimeBand.MORNING;
        }
        if (time.isBefore(LocalTime.of(18, 0))) {
            return TimeBand.AFTERNOON;
        }
        return TimeBand.EVENING;
    }

    private void ensureNoScreenOverlap(Long showingId, Screen screen, LocalDate showDate,
                                       LocalTime startTime, LocalTime endTime) {
        boolean overlaps = showingRepository.findByScreenAndShowDate(screen, showDate).stream()
                .filter(existing -> showingId == null || !existing.getShowingId().equals(showingId))
                .anyMatch(existing -> startTime.isBefore(existing.getEndTime())
                        && endTime.isAfter(existing.getStartTime()));
        if (overlaps) {
            throw new IllegalStateException("This screen already has a showing during that time");
        }
    }

    public List<User> listUsers() {
        requireEmployee();
        return userRepository.findAll();
    }

    public List<User> listUsersByCustomerPhone(String rawPhonePrefix) {
        requireEmployee();
        String phonePrefix = PhoneNumbers.normalize(rawPhonePrefix);
        if (phonePrefix == null) {
            return listUsers();
        }
        return userRepository.findByRoleAndPhoneStartsWithOrderByPhoneAsc(UserRole.CUSTOMER, phonePrefix);
    }

    @Transactional
    public User setUserStatus(Long userId, UserStatus status) {
        requireEmployee();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        user.setStatus(status);
        return userRepository.save(user);
    }

    private void requireEmployee() {
        if (!currentUserService.requireCurrentUser().getRole().isEmployee()) {
            throw new AccessDeniedException("Only employee accounts can manage catalog data");
        }
    }
}
