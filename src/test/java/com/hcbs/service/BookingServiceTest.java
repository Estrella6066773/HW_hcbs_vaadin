package com.hcbs.service;

import com.hcbs.model.Booking;
import com.hcbs.model.Seat;
import com.hcbs.model.Showing;
import com.hcbs.repository.BookingSeatRepository;
import com.hcbs.repository.SeatRepository;
import com.hcbs.repository.ShowingRepository;
import com.hcbs.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:booking-service-test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class BookingServiceTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private ShowingRepository showingRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingSeatRepository bookingSeatRepository;

    @Test
    void createsBookingWithReceiptValuesAndReservesSeats() {
        Showing showing = showingRepository.findAll().get(0);
        List<Seat> seats = seatRepository.findByScreenAndSeatArea(showing.getScreen(), com.hcbs.model.SeatArea.LOWER_HALL)
                .stream()
                .limit(2)
                .toList();

        Booking booking = bookingService.createBooking(showing, userRepository.findAll().get(0), seats);

        assertThat(booking.getBookingReference()).startsWith("HCBS-");
        assertThat(booking.getNumberOfTickets()).isEqualTo(2);
        assertThat(booking.getTotalCost()).isEqualByComparingTo(new BigDecimal("24.00"));
        assertThat(bookingSeatRepository.existsByShowingAndSeat(showing, seats.get(0))).isTrue();
    }

    @Test
    void rejectsDuplicateSeatForSameShowing() {
        Showing showing = showingRepository.findAll().get(0);
        Seat seat = seatRepository.findByScreenAndSeatArea(showing.getScreen(), com.hcbs.model.SeatArea.LOWER_HALL).get(0);
        bookingService.createBooking(showing, userRepository.findAll().get(0), List.of(seat));

        assertThatThrownBy(() -> bookingService.createBooking(showing, userRepository.findAll().get(0), List.of(seat)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already booked");
    }
}
