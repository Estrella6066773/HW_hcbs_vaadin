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
 * 规则摘要（答辩 / TC_008–011）：
 * <ul>
 *   <li>仅 {@link BookingStatus#CONFIRMED} 且 {@code today.isBefore(showDate)} 可取消（TC_010 当日禁止取消）</li>
 *   <li>手续费 = 总价 × 0.5（TC_009）</li>
 *   <li>取消后删除 {@code BookingSeat} 释放座位（TC_008）</li>
 * </ul>
 * 客户只能操作自己的订单；员工可按手机号检索任意客户或访客订单。
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

    /**
     * 客户：返回本人订单；员工若误调用此方法会得到全部订单（员工应使用 {@link #listBookingsByPhone}）。
     */
    public List<CustomerBookingRow> listAccessibleBookings() {
        User actor = currentUserService.requireCurrentUser();
        List<Booking> bookings = actor.getRole().isCustomer()
                ? bookingRepository.findByCustomerOrderByBookingDateTimeDesc(actor)
                : bookingRepository.findAll();
        return bookings.stream().map(this::toCustomerRow).toList();
    }

    /** 员工柜台：下拉框数据源，输入至少 3 位数字进行前缀匹配 */
    public List<String> searchPhonesWithBookings(String rawQuery) {
        requireEmployeeActor();
        String prefix = PhoneNumbers.normalize(rawQuery);
        if (prefix == null || prefix.length() < 3) {
            return List.of();
        }
        Set<String> phones = new LinkedHashSet<>();
        phones.addAll(bookingRepository.findDistinctCustomerPhonesWithBookings(prefix));
        phones.addAll(bookingRepository.findDistinctGuestPhonesWithBookings(prefix)); // 访客订票无用户记录
        return new ArrayList<>(phones);
    }

    /** 员工柜台：查询选定手机号下的全部订单（包括注册客户和访客手机号） */
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

    /**
     * 判断是否允许取消：订单已确认且放映日期严格晚于今天（当天不可取消，TC_010）。
     */
    public boolean canCancel(Booking booking) {
        return booking.getStatus() == BookingStatus.CONFIRMED
                && LocalDate.now().isBefore(booking.getShowing().getShowDate());
    }

    /** 取消手续费：总价的 50%，保留两位小数（TC_009） */
    public BigDecimal calculateCancellationCharge(Booking booking) {
        return booking.getTotalCost()
                .multiply(new BigDecimal("0.50"))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 执行取消操作：更新状态、记录手续费与时间，并删除座位占用记录（TC_008）。
     */
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
        bookingSeatRepository.deleteAll(bookingSeatRepository.findByBooking(booking)); // 释放座位，B 模块可再次销售
        return toSummary(bookingRepository.save(booking));
    }

    /** 客户只能取消 customer_id 为自己的订单；员工无此限制（仍须登录） */
    private void assertCanAccess(Booking booking) {
        User actor = currentUserService.requireCurrentUser();
        if (actor.getRole().isCustomer()
                && (booking.getCustomer() == null
                || !booking.getCustomer().getUserId().equals(actor.getUserId()))) {
            throw new AccessDeniedException("You can only manage your own bookings");
        }
    }

    /**
     * 根据订单编号查找订单。
     * @param bookingReference 订单编号
     * @return 找到的订单
     * @throws IllegalArgumentException 如果未找到对应订单
     */
    private Booking findBookingByReference(String bookingReference) {
        return bookingRepository.findByBookingReference(bookingReference)
                .orElseThrow(() -> new IllegalArgumentException("Booking reference not found"));
    }

    /**
     * 验证并规范化手机号。
     * @param rawPhone 原始手机号
     * @return 规范化后的手机号
     * @throws IllegalArgumentException 如果手机号无效
     */
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

    /**
     * 确保当前用户是员工角色。
     * @throws AccessDeniedException 如果当前用户不是员工
     */
    private void requireEmployeeActor() {
        User actor = currentUserService.requireCurrentUser();
        if (!actor.getRole().isEmployee()) {
            throw new AccessDeniedException("Only employee accounts can search bookings by phone");
        }
    }

    /**
     * 将订单实体转换为客户订单行 DTO。
     * @param booking 订单实体
     * @return 客户订单行 DTO
     */
    private CustomerBookingRow toCustomerRow(Booking booking) {
        return new CustomerBookingRow(
                booking.getBookingReference(),
                booking.getShowing().getFilm().getTitle(),
                booking.getShowing().getShowDate(),
                booking.getShowing().getStartTime(),
                booking.getNumberOfTickets(),
                booking.getTotalCost(),
                booking.getStatus(),
                canCancel(booking) // 驱动 Grid 上 Cancel 按钮的启用状态
        );
    }

    /**
     * 将订单实体转换为订单摘要 DTO。
     * @param booking 订单实体
     * @return 订单摘要 DTO
     */
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
