package com.hcbs.repository;

import com.hcbs.model.Cinema;
import com.hcbs.model.Film;
import com.hcbs.model.Screen;
import com.hcbs.model.Showing;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ShowingRepository extends JpaRepository<Showing, Long> {
    List<Showing> findByShowDate(LocalDate date);

    List<Showing> findByFilm(Film film);

    List<Showing> findByScreenCinema(Cinema cinema);

    List<Showing> findByFilmAndShowDate(Film film, LocalDate date);

    List<Showing> findByScreenAndShowDate(Screen screen, LocalDate date);

    @Query("""
            SELECT s FROM Showing s
            JOIN s.screen sc
            JOIN sc.cinema c
            JOIN c.city ct
            WHERE (:cityId IS NULL OR ct.cityId = :cityId)
              AND (:cinemaId IS NULL OR c.cinemaId = :cinemaId)
              AND (:date IS NULL OR s.showDate = :date)
              AND (:filmTitle IS NULL OR :filmTitle = '' OR LOWER(s.film.title) LIKE LOWER(CONCAT('%', :filmTitle, '%')))
            ORDER BY s.showDate ASC, s.startTime ASC
            """)
    List<Showing> searchShowings(
            @Param("cityId") Long cityId,
            @Param("cinemaId") Long cinemaId,
            @Param("date") LocalDate date,
            @Param("filmTitle") String filmTitle);
}
