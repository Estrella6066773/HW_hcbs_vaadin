package com.hcbs.service;

import com.hcbs.model.Booking;
import com.hcbs.model.BookingStatus;
import com.hcbs.model.Seat;
import com.hcbs.model.Showing;
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
    private UserRepository userRepository;

    @Test
    void cancelsBookingAndAppliesFiftyPercentCharge() {
        Showing showing = showingRepository.findAll().get(0);
        Seat seat = seatRepository.findByScreen(showing.getScreen()).get(0);
        Booking booking = bookingService.createBooking(showing, userRepository.findAll().get(0), List.of(seat));

        Booking cancelled = cancellationService.cancelBooking(booking.getBookingReference());

        assertThat(cancelled.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(cancelled.getCancellationCharge()).isEqualByComparingTo(new BigDecimal("6.00"));
    }
}
