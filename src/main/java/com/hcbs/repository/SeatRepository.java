package com.hcbs.repository;

import com.hcbs.model.Screen;
import com.hcbs.model.Seat;
import com.hcbs.model.SeatArea;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByScreen(Screen screen);

    List<Seat> findByScreenAndSeatArea(Screen screen, SeatArea seatArea);
}
