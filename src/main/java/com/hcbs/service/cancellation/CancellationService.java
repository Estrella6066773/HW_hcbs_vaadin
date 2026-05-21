package com.hcbs.service.cancellation;

import com.hcbs.dto.BookingSummary;
import com.hcbs.model.Booking;
import com.hcbs.model.BookingStatus;
import com.hcbs.repository.BookingRepository;
import com.hcbs.repository.BookingSeatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class CancellationService {
    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;

    public CancellationService(BookingRepository bookingRepository, BookingSeatRepository bookingSeatRepository) {
        this.bookingRepository = bookingRepository;
        this.bookingSeatRepository = bookingSeatRepository;
    }

    public BookingSummary findBookingSummary(String bookingReference) {
        return toSummary(findBookingByReference(bookingReference));
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
    public BookingSummary cancelBooking(String bookingReference) {
        Booking booking = findBookingByReference(bookingReference);
        if (!canCancel(booking)) {
            throw new IllegalStateException("Cancellation is only allowed at least one day before the showing");
        }
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancellationDateTime(LocalDateTime.now());
        booking.setCancellationCharge(calculateCancellationCharge(booking));
        bookingSeatRepository.deleteAll(bookingSeatRepository.findByBooking(booking));
        return toSummary(bookingRepository.save(booking));
    }

    private Booking findBookingByReference(String bookingReference) {
        return bookingRepository.findByBookingReference(bookingReference)
                .orElseThrow(() -> new IllegalArgumentException("Booking reference not found"));
    }

    private BookingSummary toSummary(Booking booking) {
        return new BookingSummary(
                booking.getBookingReference(),
                booking.getShowing().getFilm().getTitle(),
                booking.getShowing().getShowDate(),
                booking.getShowing().getStartTime(),
                booking.getTotalCost(),
                booking.getStatus(),
                canCancel(booking),
                booking.getCancellationCharge());
    }
}
