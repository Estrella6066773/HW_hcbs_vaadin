package com.hcbs.repository;

import com.hcbs.model.Booking;
import com.hcbs.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByBookingReference(String bookingReference);

    boolean existsByBookingReference(String bookingReference);

    List<Booking> findByCustomerOrderByBookingDateTimeDesc(User customer);
}
