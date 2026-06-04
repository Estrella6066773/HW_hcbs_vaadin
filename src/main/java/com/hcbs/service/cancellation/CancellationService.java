package com.hcbs.service.cancellation;

import com.hcbs.dto.BookingSummary;
import com.hcbs.dto.CustomerBookingRow;
import com.hcbs.model.Booking;
import com.hcbs.model.BookingStatus;
import com.hcbs.model.User;
import com.hcbs.repository.BookingRepository;
import com.hcbs.repository.BookingSeatRepository;
import com.hcbs.security.CurrentUserService;
import com.hcbs.service.booking.BookingService;
import com.hcbs.util.PhoneNumbers;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 订单取消业务（成员 C · 取消模块）。
 * <p>
 * 规则摘要：
 * <ul>
 *   <li>仅 {@link BookingStatus#CONFIRMED} 且 {@code today.isBefore(showDate)} 可取消（TC_010 当日拒绝）</li>
 *   <li>手续费 = 总价 × 0.5（TC_009）</li>
 *   <li>取消后删除 {@code BookingSeat} 释放座位（TC_008）</li>
 * </ul>
 * 客户只能操作自己的订单；员工可按手机号检索任意客户/访客订单。
 * 只读依赖成员 B 的 {@link BookingRepository}，不修改订票逻辑。
 */
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

    public List<String> searchPhonesWithBookings(String rawQuery) {
        requireEmployeeActor();
        String prefix = PhoneNumbers.normalize(rawQuery);
        if (prefix == null || prefix.length() < 3) {
            return List.of();
        }
        Set<String> phones = new LinkedHashSet<>();
        phones.addAll(bookingRepository.findDistinctCustomerPhonesWithBookings(prefix));
        phones.addAll(bookingRepository.findDistinctGuestPhonesWithBookings(prefix));
        return new ArrayList<>(phones);
    }

    public List<CustomerBookingRow> listBookingsByPhone(String rawPhone) {
        requireEmployeeActor();
        String phone = requireValidPhone(rawPhone);
        return bookingRepository.findByCustomerPhoneOrGuestPhoneOrderByBookingDateTimeDesc(phone).stream()
                .map(this::toCustomerRow)
                .toList();
    }

    public BookingSummary findBookingSummary(String bookingReference) {
        Booking booking = findBookingByReference(bookingReference);
        assertCanAccess(booking);
        return toSummary(booking);
    }

    /** 是否允许取消：已确认且放映日严格晚于今天 */
    public boolean canCancel(Booking booking) {
        return booking.getStatus() == BookingStatus.CONFIRMED
                && LocalDate.now().isBefore(booking.getShowing().getShowDate());
    }

    /** 取消手续费：总价 50%，保留两位小数 */
    public BigDecimal calculateCancellationCharge(Booking booking) {
        return booking.getTotalCost()
                .multiply(new BigDecimal("0.50"))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /** 执行取消：更新状态、记录手续费与时间，并删除座位占用（TC_008/009/010） */
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
                && (booking.getCustomer() == null
                || !booking.getCustomer().getUserId().equals(actor.getUserId()))) {
            throw new AccessDeniedException("You can only manage your own bookings");
        }
    }

    private Booking findBookingByReference(String bookingReference) {
        return bookingRepository.findByBookingReference(bookingReference)
                .orElseThrow(() -> new IllegalArgumentException("Booking reference not found"));
    }

    private static String requireValidPhone(String rawPhone) {
        if (rawPhone == null || rawPhone.isBlank()) {
            throw new IllegalArgumentException("Enter a phone number");
        }
        String phone = PhoneNumbers.normalize(rawPhone);
        if (!PhoneNumbers.isValid(phone)) {
            throw new IllegalArgumentException("Phone number format is invalid");
        }
        return phone;
    }

    private void requireEmployeeActor() {
        User actor = currentUserService.requireCurrentUser();
        if (!actor.getRole().isEmployee()) {
            throw new AccessDeniedException("Only employee accounts can search bookings by phone");
        }
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
                BookingService.customerLabel(booking.getCustomer(), booking.getGuestPhone()),
                booking.getCreatedBy().getFullName());
    }
}
