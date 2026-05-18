package com.hcbs.repository;

import com.hcbs.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByBookingReference(String bookingReference);

    boolean existsByBookingReference(String bookingReference);
}
