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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 取消服务自动化测试（成员 C）。
 * <p>
 * 用例映射：TC_008/009 → {@link #cancelsBookingAndAppliesFiftyPercentCharge()}；
 * TC_010 → {@link #rejectsSameDayCancellation()}；TC_011 无效单号见 UI/手工。
 * 运行：{@code mvn test -Dtest=CancellationServiceTest}
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:cancellation-service-test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class CancellationServiceTest {

    private static final String STAFF_PHONE = "13800238001";
    private static final String ALICE_PHONE = "13800138001";
    private static final String BOB_PHONE = "13800138002";

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

    /** TC_008 成功取消并释放座位；TC_009 £12 订单手续费 £6 */
    @Test
    @WithMockUser(username = STAFF_PHONE, roles = "BOOKING_STAFF")
    void cancelsBookingAndAppliesFiftyPercentCharge() {
        Showing showing = showingRepository.findAll().get(0);
        Seat seat = seatRepository.findByScreen(showing.getScreen()).get(0);
        var receipt = bookingService.createBooking(showing.getShowingId(), List.of(seat.getSeatId()), ALICE_PHONE);

        BookingSummary cancelled = cancellationService.cancelBooking(receipt.bookingReference());

        assertThat(cancelled.status()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(cancelled.cancellationCharge()).isEqualByComparingTo(new BigDecimal("6.00"));
        assertThat(bookingSeatRepository.existsActiveReservationForShowingAndSeat(showing, seat)).isFalse();
    }

    /** TC_010 放映日当天拒绝取消 */
    @Test
    @WithMockUser(username = STAFF_PHONE, roles = "BOOKING_STAFF")
    void rejectsSameDayCancellation() {
        Showing showing = showingRepository.findAll().get(0);
        showing.setShowDate(LocalDate.now());
        showingRepository.save(showing);
        Seat seat = seatRepository.findByScreen(showing.getScreen()).get(0);
        var receipt = bookingService.createBooking(showing.getShowingId(), List.of(seat.getSeatId()), ALICE_PHONE);

        assertThatThrownBy(() -> cancellationService.cancelBooking(receipt.bookingReference()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at least one day before");
    }

    @Test
    @WithMockUser(username = BOB_PHONE, roles = "CUSTOMER")
    void customerCannotCancelAnotherCustomersBooking() {
        assertThatThrownBy(() -> cancellationService.cancelBooking("HCBS-SEED001"))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @WithMockUser(username = ALICE_PHONE, roles = "CUSTOMER")
    void customerCanCancelOwnSeedBooking() {
        BookingSummary cancelled = cancellationService.cancelBooking("HCBS-SEED001");
        assertThat(cancelled.status()).isEqualTo(BookingStatus.CANCELLED);
    }

    @Test
    @WithMockUser(username = STAFF_PHONE, roles = "BOOKING_STAFF")
    void staffCanCancelGuestBooking() {
        Showing showing = showingRepository.findAll().get(0);
        Seat seat = seatRepository.findByScreen(showing.getScreen()).get(3);
        var receipt = bookingService.createBooking(showing.getShowingId(), List.of(seat.getSeatId()), "13900000001");

        BookingSummary cancelled = cancellationService.cancelBooking(receipt.bookingReference());

        assertThat(cancelled.status()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(cancelled.customerName()).isEqualTo("Guest (13900000001)");
    }

    @Test
    @WithMockUser(username = STAFF_PHONE, roles = "BOOKING_STAFF")
    void listsBookingsByPhoneForRegisteredAndGuestOrders() {
        Showing showing = showingRepository.findAll().get(0);
        Seat seat = seatRepository.findByScreen(showing.getScreen()).get(4);
        bookingService.createBooking(showing.getShowingId(), List.of(seat.getSeatId()), "13900000002");

        assertThat(cancellationService.listBookingsByPhone(ALICE_PHONE))
                .extracting(row -> row.bookingReference())
                .contains("HCBS-SEED001");
        assertThat(cancellationService.listBookingsByPhone("13900000002"))
                .hasSize(1);
        assertThat(cancellationService.searchPhonesWithBookings("138001"))
                .contains(ALICE_PHONE);
    }
}
