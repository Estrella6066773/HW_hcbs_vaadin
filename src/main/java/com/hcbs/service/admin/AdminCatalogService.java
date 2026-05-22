package com.hcbs.service.admin;

import com.hcbs.dto.FilmAdminRow;
import com.hcbs.model.Film;
import com.hcbs.model.User;
import com.hcbs.model.UserRole;
import com.hcbs.model.UserStatus;
import com.hcbs.repository.FilmRepository;
import com.hcbs.repository.UserRepository;
import com.hcbs.security.CurrentUserService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminCatalogService {

    private final FilmRepository filmRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    public AdminCatalogService(FilmRepository filmRepository, UserRepository userRepository,
                               CurrentUserService currentUserService) {
        this.filmRepository = filmRepository;
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
    }

    public List<FilmAdminRow> listFilms() {
        requireEmployee();
        return filmRepository.findAll().stream().map(this::toRow).toList();
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
        film.setPosterUrl(row.getPosterUrl());
        if (film.getDescription() == null) {
            film.setDescription("");
        }
        return toRow(filmRepository.save(film));
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

    public List<User> listUsers() {
        requireEmployee();
        return userRepository.findAll();
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
