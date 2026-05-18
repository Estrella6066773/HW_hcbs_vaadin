package com.hcbs.repository;

import com.hcbs.model.Booking;
import com.hcbs.model.BookingSeat;
import com.hcbs.model.Seat;
import com.hcbs.model.Showing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingSeatRepository extends JpaRepository<BookingSeat, Long> {
    boolean existsByShowingAndSeat(Showing showing, Seat seat);

    List<BookingSeat> findByShowing(Showing showing);

    List<BookingSeat> findByBooking(Booking booking);
}
