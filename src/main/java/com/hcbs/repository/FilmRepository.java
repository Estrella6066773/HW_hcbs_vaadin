package com.hcbs.repository;

import com.hcbs.model.Film;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FilmRepository extends JpaRepository<Film, Long> {
    List<Film> findByTitleContainingIgnoreCase(String title);
}
