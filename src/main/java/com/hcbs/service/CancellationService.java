package com.hcbs.service;

import com.hcbs.model.Booking;
import com.hcbs.model.BookingStatus;
import com.hcbs.repository.BookingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class CancellationService {
    private final BookingRepository bookingRepository;

    public CancellationService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    public Booking findBookingByReference(String bookingReference) {
        return bookingRepository.findByBookingReference(bookingReference)
                .orElseThrow(() -> new IllegalArgumentException("Booking reference not found"));
    }

    public boolean canCancel(Booking booking) {
        return booking.getStatus() == BookingStatus.CONFIRMED
                && LocalDate.now().isBefore(booking.getShowing().getShowDate());
    }

    public BigDecimal calculateCancellationCharge(Booking booking) {
        return booking.getTotalCost()
                .multiply(new BigDecimal("0.50"))
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Transactional
    public Booking cancelBooking(String bookingReference) {
        Booking booking = findBookingByReference(bookingReference);
        if (!canCancel(booking)) {
            throw new IllegalStateException("Cancellation is only allowed at least one day before the showing");
        }
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancellationDateTime(LocalDateTime.now());
        booking.setCancellationCharge(calculateCancellationCharge(booking));
        return bookingRepository.save(booking);
    }
}
