package com.hcbs.service.booking;

import com.hcbs.dto.BookingReceipt;
import com.hcbs.dto.BookingShowingContext;
import com.hcbs.dto.SeatMapSeat;
import com.hcbs.dto.SeatOption;
import com.hcbs.dto.ShowingOption;
import com.hcbs.dto.ShowingTimeSlot;
import com.hcbs.dto.UserOption;
import com.hcbs.model.Booking;
import com.hcbs.model.BookingSeat;
import com.hcbs.model.BookingStatus;
import com.hcbs.model.PriceRule;
import com.hcbs.model.Seat;
import com.hcbs.model.SeatArea;
import com.hcbs.model.Showing;
import com.hcbs.model.User;
import com.hcbs.model.UserRole;
import com.hcbs.repository.BookingRepository;
import com.hcbs.repository.BookingSeatRepository;
import com.hcbs.repository.PriceRuleRepository;
import com.hcbs.repository.SeatRepository;
import com.hcbs.repository.ShowingRepository;
import com.hcbs.repository.UserRepository;
import com.hcbs.security.CurrentUserService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class BookingService {
    private final ShowingRepository showingRepository;
    private final SeatRepository seatRepository;
    private final PriceRuleRepository priceRuleRepository;
    private final BookingRepository bookingRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    public BookingService(ShowingRepository showingRepository, SeatRepository seatRepository,
                          PriceRuleRepository priceRuleRepository, BookingRepository bookingRepository,
                          BookingSeatRepository bookingSeatRepository, UserRepository userRepository,
                          CurrentUserService currentUserService) {
        this.showingRepository = showingRepository;
        this.seatRepository = seatRepository;
        this.priceRuleRepository = priceRuleRepository;
        this.bookingRepository = bookingRepository;
        this.bookingSeatRepository = bookingSeatRepository;
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
    }

    public Optional<ShowingOption> findBookableShowing(Long showingId) {
        if (showingId == null) {
            return Optional.empty();
        }
        return listBookableShowings().stream()
                .filter(option -> showingId.equals(option.showingId()))
                .findFirst();
    }

    public List<ShowingOption> listBookableShowings() {
        LocalDate today = LocalDate.now();
        LocalDate latest = today.plusDays(7);
        return showingRepository.findAll().stream()
                .filter(showing -> !showing.getShowDate().isBefore(today))
                .filter(showing -> !showing.getShowDate().isAfter(latest))
                .map(showing -> new ShowingOption(
                        showing.getShowingId(),
                        showing.getFilm().getTitle() + " | "
                                + showing.getScreen().getCinema().getName()
                                + " | " + showing.getShowDate() + " " + showing.getStartTime()))
                .toList();
    }

    public List<UserOption> listCustomersForDesk() {
        requireEmployeeActor();
        return userRepository.findByRoleOrderByFullNameAsc(UserRole.CUSTOMER).stream()
                .map(user -> new UserOption(user.getUserId(), user.getUsername(), user.getFullName()))
                .toList();
    }

    public List<SeatOption> listAvailableSeats(Long showingId) {
        return listAvailableSeats(showingId, SeatArea.STANDARD);
    }

    public List<SeatOption> listAvailableSeats(Long showingId, SeatArea seatArea) {
        Showing showing = requireShowing(showingId);
        return seatRepository.findByScreenAndSeatArea(showing.getScreen(), seatArea).stream()
                .filter(seat -> !bookingSeatRepository.existsActiveReservationForShowingAndSeat(showing, seat))
                .map(seat -> new SeatOption(
                        seat.getSeatId(),
                        seat.getSeatNumber(),
                        seat.getSeatArea(),
                        calculateTicketPrice(showing, seat)))
                .toList();
    }

    public Optional<BookingShowingContext> findBookingContext(Long showingId) {
        if (showingId == null) {
            return Optional.empty();
        }
        return findBookableShowing(showingId).flatMap(option -> showingRepository.findById(option.showingId())
                .filter(this::isBookableShowing)
                .map(showing -> new BookingShowingContext(
                        showing.getShowingId(),
                        showing.getFilm().getTitle(),
                        showing.getScreen().getCinema().getName(),
                        "Screen " + showing.getScreen().getScreenNumber(),
                        showing.getShowDate(),
                        showing.getStartTime(),
                        listSameDayShowtimes(showing))));
    }

    public List<SeatMapSeat> listSeatMap(Long showingId) {
        Showing showing = requireShowing(showingId);
        if (!isBookableShowing(showing)) {
            throw new IllegalStateException("Showing is not available for booking");
        }
        return seatRepository.findByScreenAndSeatArea(showing.getScreen(), SeatArea.STANDARD).stream()
                .sorted((a, b) -> com.hcbs.config.SeatGridFormat.compareSeatNumbers(
                        a.getSeatNumber(), b.getSeatNumber()))
                .map(seat -> new SeatMapSeat(
                        seat.getSeatId(),
                        seat.getSeatNumber(),
                        seat.getSeatArea(),
                        calculateTicketPrice(showing, seat),
                        !bookingSeatRepository.existsActiveReservationForShowingAndSeat(showing, seat)))
                .toList();
    }

    private List<ShowingTimeSlot> listSameDayShowtimes(Showing anchor) {
        Long cinemaId = anchor.getScreen().getCinema().getCinemaId();
        return showingRepository.findByFilmAndShowDate(anchor.getFilm(), anchor.getShowDate()).stream()
                .filter(s -> s.getScreen().getCinema().getCinemaId().equals(cinemaId))
                .filter(this::isBookableShowing)
                .sorted((a, b) -> a.getStartTime().compareTo(b.getStartTime()))
                .map(s -> new ShowingTimeSlot(s.getShowingId(), s.getStartTime()))
                .toList();
    }

    private boolean isBookableShowing(Showing showing) {
        LocalDate today = LocalDate.now();
        LocalDate latest = today.plusDays(7);
        return !showing.getShowDate().isBefore(today) && !showing.getShowDate().isAfter(latest);
    }

    @Transactional
    public BookingReceipt createBooking(Long showingId, List<Long> seatIds, Long customerUserId) {
        User actor = currentUserService.requireCurrentUser();
        User customer = resolveCustomer(actor, customerUserId);
        Showing showing = requireShowing(showingId);
        List<Seat> seats = seatIds.stream()
                .map(seatId -> seatRepository.findById(seatId)
                        .orElseThrow(() -> new IllegalArgumentException("Seat not found: " + seatId)))
                .toList();

        validateBookingDate(showing);
        validateSeatsAvailable(showing, seats);

        Booking booking = new Booking();
        booking.setBookingReference(generateBookingReference());
        booking.setShowing(showing);
        booking.setCreatedBy(actor);
        booking.setCustomer(customer);
        booking.setBookingDateTime(LocalDateTime.now());
        booking.setNumberOfTickets(seats.size());
        booking.setTotalCost(calculateTotalCost(showing, seats));
        booking.setStatus(BookingStatus.CONFIRMED);
        Booking saved = bookingRepository.save(booking);

        for (Seat seat : seats) {
            bookingSeatRepository.save(new BookingSeat(saved, seat, showing, calculateTicketPrice(showing, seat)));
        }

        return toReceipt(saved, seats, customer);
    }

    private User resolveCustomer(User actor, Long customerUserId) {
        if (actor.getRole().isCustomer()) {
            if (customerUserId != null && !customerUserId.equals(actor.getUserId())) {
                throw new AccessDeniedException("Customers can only book for their own account");
            }
            return actor;
        }
        requireEmployeeActor(actor);
        if (customerUserId == null) {
            throw new IllegalArgumentException("Select a customer when booking on behalf of someone");
        }
        User customer = userRepository.findById(customerUserId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + customerUserId));
        if (!customer.getRole().isCustomer()) {
            throw new IllegalArgumentException("Bookings must be assigned to a customer account");
        }
        if (customer.getStatus() != com.hcbs.model.UserStatus.ACTIVE) {
            throw new IllegalStateException("Customer account is not active");
        }
        return customer;
    }

    private void requireEmployeeActor() {
        requireEmployeeActor(currentUserService.requireCurrentUser());
    }

    private static void requireEmployeeActor(User actor) {
        if (!actor.getRole().isEmployee()) {
            throw new AccessDeniedException("Only employee accounts can book on behalf of customers");
        }
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
            if (bookingSeatRepository.existsActiveReservationForShowingAndSeat(showing, seat)) {
                throw new IllegalStateException("Seat " + seat.getSeatNumber() + " is already booked for this showing");
            }
        }
    }

    private Showing requireShowing(Long showingId) {
        return showingRepository.findById(showingId)
                .orElseThrow(() -> new IllegalArgumentException("Showing not found: " + showingId));
    }

    private BookingReceipt toReceipt(Booking booking, List<Seat> seats, User customer) {
        String seatNumbers = seats.stream()
                .map(Seat::getSeatNumber)
                .reduce((left, right) -> left + ", " + right)
                .orElse("");
        return new BookingReceipt(
                booking.getBookingReference(),
                booking.getShowing().getFilm().getTitle(),
                booking.getShowing().getShowDate(),
                booking.getShowing().getStartTime(),
                booking.getShowing().getScreen().getScreenNumber(),
                booking.getNumberOfTickets(),
                seatNumbers,
                booking.getTotalCost(),
                booking.getBookingDateTime(),
                customer.getFullName(),
                booking.getCreatedBy().getFullName());
    }
}
