package com.hcbs.service.booking;

import com.hcbs.dto.BookingReceipt;
import com.hcbs.model.Seat;
import com.hcbs.model.Showing;
import com.hcbs.repository.BookingRepository;
import com.hcbs.repository.BookingSeatRepository;
import com.hcbs.repository.SeatRepository;
import com.hcbs.repository.ShowingRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:booking-service-test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class BookingServiceTest {

    private static final String STAFF_PHONE = "13800238001";
    private static final String ALICE_PHONE = "13800138001";

    @Autowired
    private BookingService bookingService;

    @Autowired
    private ShowingRepository showingRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private BookingSeatRepository bookingSeatRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Test
    @WithMockUser(username = STAFF_PHONE, roles = "BOOKING_STAFF")
    void createsBookingWithReceiptValuesAndReservesSeats() {
        Showing showing = showingRepository.findAll().get(0);
        List<Seat> seats = seatRepository.findByScreenAndSeatArea(showing.getScreen(), com.hcbs.model.SeatArea.STANDARD)
                .stream()
                .limit(2)
                .toList();
        List<Long> seatIds = seats.stream().map(Seat::getSeatId).toList();

        BookingReceipt receipt = bookingService.createBooking(showing.getShowingId(), seatIds, ALICE_PHONE);

        assertThat(receipt.bookingReference()).startsWith("HCBS-");
        assertThat(receipt.numberOfTickets()).isEqualTo(2);
        assertThat(receipt.totalCost()).isEqualByComparingTo(new BigDecimal("24.00"));
        assertThat(receipt.customerName()).isEqualTo("Alice Chen");
        assertThat(bookingSeatRepository.existsActiveReservationForShowingAndSeat(showing, seats.get(0))).isTrue();
    }

    @Test
    @WithMockUser(username = STAFF_PHONE, roles = "BOOKING_STAFF")
    void rejectsDuplicateSeatForSameShowing() {
        Showing showing = showingRepository.findAll().get(0);
        Seat seat = seatRepository.findByScreenAndSeatArea(showing.getScreen(), com.hcbs.model.SeatArea.STANDARD).get(0);
        bookingService.createBooking(showing.getShowingId(), List.of(seat.getSeatId()), ALICE_PHONE);

        assertThatThrownBy(() -> bookingService.createBooking(showing.getShowingId(), List.of(seat.getSeatId()), ALICE_PHONE))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already booked");
    }

    @Test
    @WithMockUser(username = STAFF_PHONE, roles = "BOOKING_STAFF")
    void rejectsBookingMoreThanSevenDaysInAdvance() {
        Showing showing = showingRepository.findAll().get(0);
        showing.setShowDate(LocalDate.now().plusDays(10));
        showingRepository.save(showing);
        Seat seat = seatRepository.findByScreenAndSeatArea(showing.getScreen(), com.hcbs.model.SeatArea.STANDARD).get(0);

        assertThatThrownBy(() -> bookingService.createBooking(showing.getShowingId(), List.of(seat.getSeatId()), ALICE_PHONE))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("one week");
    }

    @Test
    @WithMockUser(username = ALICE_PHONE, roles = "CUSTOMER")
    void customerBooksForSelfWithoutCustomerPicker() {
        Showing showing = showingRepository.findAll().get(0);
        Seat seat = seatRepository.findByScreenAndSeatArea(showing.getScreen(), com.hcbs.model.SeatArea.STANDARD).get(1);

        BookingReceipt receipt = bookingService.createBooking(showing.getShowingId(), List.of(seat.getSeatId()), null);

        assertThat(receipt.customerName()).isEqualTo("Alice Chen");
        assertThat(receipt.bookedByName()).isEqualTo("Alice Chen");
    }

    @Test
    @WithMockUser(username = STAFF_PHONE, roles = "BOOKING_STAFF")
    void createsGuestBookingWhenPhoneHasNoAccount() {
        Showing showing = showingRepository.findAll().get(0);
        Seat seat = seatRepository.findByScreenAndSeatArea(showing.getScreen(), com.hcbs.model.SeatArea.STANDARD).get(2);
        String guestPhone = "13900000000";

        BookingReceipt receipt = bookingService.createBooking(showing.getShowingId(), List.of(seat.getSeatId()), guestPhone);

        assertThat(receipt.customerName()).isEqualTo("Guest (13900000000)");
        assertThat(bookingRepository.findByBookingReference(receipt.bookingReference())).isPresent()
                .get()
                .satisfies(booking -> {
                    assertThat(booking.getCustomer()).isNull();
                    assertThat(booking.getGuestPhone()).isEqualTo(guestPhone);
                });
    }

    @Test
    @WithMockUser(username = STAFF_PHONE, roles = "BOOKING_STAFF")
    void searchesCustomerPhonesByPrefix() {
        assertThat(bookingService.searchCustomerPhones("138001"))
                .anyMatch(option -> option.phone().equals(ALICE_PHONE));
        assertThat(bookingService.searchCustomerPhones("12")).isEmpty();
    }
}
