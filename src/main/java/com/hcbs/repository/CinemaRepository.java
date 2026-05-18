package com.hcbs.repository;

import com.hcbs.model.Cinema;
import com.hcbs.model.City;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CinemaRepository extends JpaRepository<Cinema, Long> {
    List<Cinema> findByCity(City city);
}
