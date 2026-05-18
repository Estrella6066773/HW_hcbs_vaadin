package com.hcbs.repository;

import com.hcbs.model.Film;
import com.hcbs.model.FilmActor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FilmActorRepository extends JpaRepository<FilmActor, Long> {
    List<FilmActor> findByFilm(Film film);
}
