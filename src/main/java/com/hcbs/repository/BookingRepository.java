package com.hcbs.repository;

import com.hcbs.model.Booking;
import com.hcbs.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByBookingReference(String bookingReference);

    boolean existsByBookingReference(String bookingReference);

    List<Booking> findByCustomerOrderByBookingDateTimeDesc(User customer);

    @Query("""
            SELECT b FROM Booking b
            LEFT JOIN b.customer c
            WHERE c.phone = :phone OR b.guestPhone = :phone
            ORDER BY b.bookingDateTime DESC
            """)
    List<Booking> findByCustomerPhoneOrGuestPhoneOrderByBookingDateTimeDesc(@Param("phone") String phone);

    @Query("""
            SELECT DISTINCT c.phone FROM Booking b
            JOIN b.customer c
            WHERE c.phone LIKE CONCAT(:prefix, '%')
            """)
    List<String> findDistinctCustomerPhonesWithBookings(@Param("prefix") String prefix);

    @Query("""
            SELECT DISTINCT b.guestPhone FROM Booking b
            WHERE b.guestPhone IS NOT NULL AND b.guestPhone LIKE CONCAT(:prefix, '%')
            """)
    List<String> findDistinctGuestPhonesWithBookings(@Param("prefix") String prefix);
}
