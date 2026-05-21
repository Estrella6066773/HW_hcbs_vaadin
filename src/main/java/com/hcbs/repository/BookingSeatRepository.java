package com.hcbs.repository;

import com.hcbs.model.Booking;
import com.hcbs.model.BookingSeat;
import com.hcbs.model.Seat;
import com.hcbs.model.Showing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookingSeatRepository extends JpaRepository<BookingSeat, Long> {

    @Query("""
            SELECT CASE WHEN COUNT(bs) > 0 THEN true ELSE false END
            FROM BookingSeat bs
            JOIN bs.booking b
            WHERE bs.showing = :showing AND bs.seat = :seat AND b.status = com.hcbs.model.BookingStatus.CONFIRMED
            """)
    boolean existsActiveReservationForShowingAndSeat(@Param("showing") Showing showing, @Param("seat") Seat seat);

    @Query("""
            SELECT COUNT(bs)
            FROM BookingSeat bs
            JOIN bs.booking b
            WHERE bs.showing = :showing AND b.status = com.hcbs.model.BookingStatus.CONFIRMED
            """)
    long countActiveReservationsForShowing(@Param("showing") Showing showing);

    List<BookingSeat> findByShowing(Showing showing);

    List<BookingSeat> findByBooking(Booking booking);
}
