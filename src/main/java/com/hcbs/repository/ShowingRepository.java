package com.hcbs.repository;

import com.hcbs.model.Cinema;
import com.hcbs.model.Film;
import com.hcbs.model.Showing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ShowingRepository extends JpaRepository<Showing, Long> {
    List<Showing> findByShowDate(LocalDate date);

    List<Showing> findByFilm(Film film);

    List<Showing> findByScreenCinema(Cinema cinema);

    List<Showing> findByFilmAndShowDate(Film film, LocalDate date);
}
