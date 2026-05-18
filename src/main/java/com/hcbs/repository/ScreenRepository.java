package com.hcbs.repository;

import com.hcbs.model.Cinema;
import com.hcbs.model.Screen;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScreenRepository extends JpaRepository<Screen, Long> {
    List<Screen> findByCinema(Cinema cinema);
}
