package com.hcbs.service.cancellation;

import com.hcbs.dto.BookingSummary;
import com.hcbs.dto.CustomerBookingRow;
import com.hcbs.model.Booking;
import com.hcbs.model.BookingStatus;
import com.hcbs.model.User;
import com.hcbs.repository.BookingRepository;
import com.hcbs.repository.BookingSeatRepository;
import com.hcbs.security.CurrentUserService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CancellationService {
    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final CurrentUserService currentUserService;

    public CancellationService(BookingRepository bookingRepository, BookingSeatRepository bookingSeatRepository,
                               CurrentUserService currentUserService) {
        this.bookingRepository = bookingRepository;
        this.bookingSeatRepository = bookingSeatRepository;
        this.currentUserService = currentUserService;
    }

    public List<CustomerBookingRow> listAccessibleBookings() {
        User actor = currentUserService.requireCurrentUser();
        List<Booking> bookings = actor.getRole().isCustomer()
                ? bookingRepository.findByCustomerOrderByBookingDateTimeDesc(actor)
                : bookingRepository.findAll();
        return bookings.stream().map(this::toCustomerRow).toList();
    }

    public BookingSummary findBookingSummary(String bookingReference) {
        Booking booking = findBookingByReference(bookingReference);
        assertCanAccess(booking);
        return toSummary(booking);
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
        assertCanAccess(booking);
        if (!canCancel(booking)) {
            throw new IllegalStateException("Cancellation is only allowed at least one day before the showing");
        }
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancellationDateTime(LocalDateTime.now());
        booking.setCancellationCharge(calculateCancellationCharge(booking));
        bookingSeatRepository.deleteAll(bookingSeatRepository.findByBooking(booking));
        return toSummary(bookingRepository.save(booking));
    }

    private void assertCanAccess(Booking booking) {
        User actor = currentUserService.requireCurrentUser();
        if (actor.getRole().isCustomer()
                && !booking.getCustomer().getUserId().equals(actor.getUserId())) {
            throw new AccessDeniedException("You can only manage your own bookings");
        }
    }

    private Booking findBookingByReference(String bookingReference) {
        return bookingRepository.findByBookingReference(bookingReference)
                .orElseThrow(() -> new IllegalArgumentException("Booking reference not found"));
    }

    private CustomerBookingRow toCustomerRow(Booking booking) {
        return new CustomerBookingRow(
                booking.getBookingReference(),
                booking.getShowing().getFilm().getTitle(),
                booking.getShowing().getShowDate(),
                booking.getShowing().getStartTime(),
                booking.getNumberOfTickets(),
                booking.getTotalCost(),
                booking.getStatus(),
                canCancel(booking));
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
                booking.getCancellationCharge(),
                booking.getCustomer().getFullName(),
                booking.getCreatedBy().getFullName());
    }
}
