package com.hcbs.service.cancellation;

import com.hcbs.dto.BookingSummary;
import com.hcbs.model.BookingStatus;
import com.hcbs.model.Seat;
import com.hcbs.model.Showing;
import com.hcbs.repository.BookingSeatRepository;
import com.hcbs.repository.SeatRepository;
import com.hcbs.repository.ShowingRepository;
import com.hcbs.service.booking.BookingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:cancellation-service-test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class CancellationServiceTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private CancellationService cancellationService;

    @Autowired
    private ShowingRepository showingRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private BookingSeatRepository bookingSeatRepository;

    @Test
    void cancelsBookingAndAppliesFiftyPercentCharge() {
        Showing showing = showingRepository.findAll().get(0);
        Seat seat = seatRepository.findByScreen(showing.getScreen()).get(0);
        var receipt = bookingService.createBooking(showing.getShowingId(), List.of(seat.getSeatId()));

        BookingSummary cancelled = cancellationService.cancelBooking(receipt.bookingReference());

        assertThat(cancelled.status()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(cancelled.cancellationCharge()).isEqualByComparingTo(new BigDecimal("6.00"));
        assertThat(bookingSeatRepository.existsActiveReservationForShowingAndSeat(showing, seat)).isFalse();
    }

    @Test
    void rejectsSameDayCancellation() {
        Showing showing = showingRepository.findAll().get(0);
        showing.setShowDate(LocalDate.now());
        showingRepository.save(showing);
        Seat seat = seatRepository.findByScreen(showing.getScreen()).get(0);
        var receipt = bookingService.createBooking(showing.getShowingId(), List.of(seat.getSeatId()));

        assertThatThrownBy(() -> cancellationService.cancelBooking(receipt.bookingReference()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at least one day before");
    }
}
