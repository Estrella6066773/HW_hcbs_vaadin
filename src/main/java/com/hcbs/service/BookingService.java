package com.hcbs.service;

import com.hcbs.model.Booking;
import com.hcbs.model.BookingSeat;
import com.hcbs.model.BookingStatus;
import com.hcbs.model.PriceRule;
import com.hcbs.model.Seat;
import com.hcbs.model.SeatArea;
import com.hcbs.model.Showing;
import com.hcbs.model.User;
import com.hcbs.repository.BookingRepository;
import com.hcbs.repository.BookingSeatRepository;
import com.hcbs.repository.PriceRuleRepository;
import com.hcbs.repository.SeatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class BookingService {
    private final SeatRepository seatRepository;
    private final PriceRuleRepository priceRuleRepository;
    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;

    public BookingService(SeatRepository seatRepository, PriceRuleRepository priceRuleRepository,
                          BookingRepository bookingRepository, BookingSeatRepository bookingSeatRepository) {
        this.seatRepository = seatRepository;
        this.priceRuleRepository = priceRuleRepository;
        this.bookingRepository = bookingRepository;
        this.bookingSeatRepository = bookingSeatRepository;
    }

    public List<Seat> getAvailableSeats(Showing showing, SeatArea seatArea) {
        return seatRepository.findByScreenAndSeatArea(showing.getScreen(), seatArea).stream()
                .filter(seat -> !bookingSeatRepository.existsByShowingAndSeat(showing, seat))
                .toList();
    }

    public BigDecimal calculateTicketPrice(Showing showing, Seat seat) {
        PriceRule rule = priceRuleRepository
                .findByCityAndTimeBandAndSeatArea(
                        showing.getScreen().getCinema().getCity(),
                        showing.getTimeBand(),
                        seat.getSeatArea())
                .orElseThrow(() -> new IllegalStateException("No price rule found for selected showing and seat"));
        return rule.getPrice();
    }

    public BigDecimal calculateTotalCost(Showing showing, List<Seat> seats) {
        return seats.stream()
                .map(seat -> calculateTicketPrice(showing, seat))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional
    public Booking createBooking(Showing showing, User user, List<Seat> seats) {
        validateBookingDate(showing);
        validateSeatsAvailable(showing, seats);

        Booking booking = new Booking();
        booking.setBookingReference(generateBookingReference());
        booking.setShowing(showing);
        booking.setUser(user);
        booking.setBookingDateTime(LocalDateTime.now());
        booking.setNumberOfTickets(seats.size());
        booking.setTotalCost(calculateTotalCost(showing, seats));
        booking.setStatus(BookingStatus.CONFIRMED);
        Booking saved = bookingRepository.save(booking);

        for (Seat seat : seats) {
            bookingSeatRepository.save(new BookingSeat(saved, seat, showing, calculateTicketPrice(showing, seat)));
        }

        return saved;
    }

    public String generateBookingReference() {
        String reference;
        do {
            reference = "HCBS-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (bookingRepository.existsByBookingReference(reference));
        return reference;
    }

    public void validateBookingDate(Showing showing) {
        LocalDate today = LocalDate.now();
        if (showing.getShowDate().isBefore(today)) {
            throw new IllegalStateException("Cannot book a missed showing");
        }
        if (showing.getShowDate().isAfter(today.plusDays(7))) {
            throw new IllegalStateException("Bookings are only allowed up to one week in advance");
        }
    }

    public void validateSeatsAvailable(Showing showing, List<Seat> seats) {
        if (seats == null || seats.isEmpty()) {
            throw new IllegalArgumentException("At least one seat must be selected");
        }
        for (Seat seat : seats) {
            if (!seat.getScreen().getScreenId().equals(showing.getScreen().getScreenId())) {
                throw new IllegalStateException("Selected seat does not belong to the showing screen");
            }
            if (bookingSeatRepository.existsByShowingAndSeat(showing, seat)) {
                throw new IllegalStateException("Seat " + seat.getSeatNumber() + " is already booked for this showing");
            }
        }
    }
}
