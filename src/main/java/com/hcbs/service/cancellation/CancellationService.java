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
 * 订单取消业务服务（成员 C · 取消模块）。
 * <p>
 * 核心业务规则（答辩 / TC_008–011）：
 * <ul>
 *   <li>取消条件：仅 {@link BookingStatus#CONFIRMED} 状态且放映日期严格晚于今天（TC_010 当日禁止取消）</li>
 *   <li>取消手续费：订单总价的 50%（TC_009）</li>
 *   <li>座位释放：取消成功后删除对应的 {@code BookingSeat} 记录，释放座位可再次销售（TC_008）</li>
 * </ul>
 * <p>
 * 权限控制：
 * <ul>
 *   <li>客户：只能操作自己的订单</li>
 *   <li>员工：可按手机号检索任意客户或访客的订单</li>
 * </ul>
 * <p>
 * 与其他模块协作：只读依赖成员 B 的 {@link BookingRepository}，不修改订票核心逻辑。
 */
@Service
public class CancellationService {
    /** 订单仓储，用于查询和更新订单 */
    private final BookingRepository bookingRepository;
    /** 订单座位仓储，用于删除座位占用记录 */
    private final BookingSeatRepository bookingSeatRepository;
    /** 当前用户服务，用于获取登录状态和用户信息 */
    private final CurrentUserService currentUserService;

    /**
     * 构造函数。
     * @param bookingRepository 订单仓储
     * @param bookingSeatRepository 订单座位仓储
     * @param currentUserService 当前用户服务
     */
    public CancellationService(BookingRepository bookingRepository, BookingSeatRepository bookingSeatRepository,
                               CurrentUserService currentUserService) {
        this.bookingRepository = bookingRepository;
        this.bookingSeatRepository = bookingSeatRepository;
        this.currentUserService = currentUserService;
    }

    /**
     * 获取当前用户可访问的订单列表。
     * <p>
     * - 客户：仅返回本人的订单，按创建时间倒序排列
     * - 员工：返回全部订单（建议员工使用 {@link #listBookingsByPhone} 按手机号查询）
     * @return 订单列表
     */
    public List<CustomerBookingRow> listAccessibleBookings() {
        User actor = currentUserService.requireCurrentUser();
        List<Booking> bookings = actor.getRole().isCustomer()
                ? bookingRepository.findByCustomerOrderByBookingDateTimeDesc(actor)
                : bookingRepository.findAll();
        return bookings.stream().map(this::toCustomerRow).toList();
    }

    /**
     * 员工柜台：根据前缀搜索曾订票的手机号，用于下拉框数据源。
     * <p>
     * 输入至少 3 位数字进行前缀匹配，同时返回注册客户和访客的手机号，去重后返回。
     * @param rawQuery 原始查询字符串
     * @return 匹配的手机号列表
     * @throws AccessDeniedException 如果当前用户不是员工
     */
    public List<String> searchPhonesWithBookings(String rawQuery) {
        requireEmployeeActor();
        String prefix = PhoneNumbers.normalize(rawQuery);
        if (prefix == null || prefix.length() < 3) {
            return List.of();
        }
        Set<String> phones = new LinkedHashSet<>();
        phones.addAll(bookingRepository.findDistinctCustomerPhonesWithBookings(prefix));
        phones.addAll(bookingRepository.findDistinctGuestPhonesWithBookings(prefix)); // 访客订票没有关联的用户记录
        return new ArrayList<>(phones);
    }

    /**
     * 员工柜台：查询指定手机号下的全部订单。
     * <p>
     * 包括注册客户通过该手机号下单的订单，以及访客使用该手机号下单的订单。
     * @param rawPhone 原始手机号
     * @return 订单列表
     * @throws AccessDeniedException 如果当前用户不是员工
     * @throws IllegalArgumentException 如果手机号无效
     */
    public List<CustomerBookingRow> listBookingsByPhone(String rawPhone) {
        requireEmployeeActor();
        String phone = requireValidPhone(rawPhone);
        return bookingRepository.findByCustomerPhoneOrGuestPhoneOrderByBookingDateTimeDesc(phone).stream()
                .map(this::toCustomerRow)
                .toList();
    }

    /**
     * 获取订单详情摘要。
     * @param bookingReference 订单编号
     * @return 订单摘要
     * @throws AccessDeniedException 如果无权访问此订单
     * @throws IllegalArgumentException 如果订单不存在
     */
    public BookingSummary findBookingSummary(String bookingReference) {
        Booking booking = findBookingByReference(bookingReference);
        assertCanAccess(booking);
        return toSummary(booking);
    }

    /**
     * 判断订单是否可取消。
     * <p>
     * 可取消条件：订单状态为 CONFIRMED 且放映日期严格晚于今天（当日不可取消，TC_010）。
     * @param booking 订单实体
     * @return 是否可取消
     */
    public boolean canCancel(Booking booking) {
        return booking.getStatus() == BookingStatus.CONFIRMED
                && LocalDate.now().isBefore(booking.getShowing().getShowDate());
    }

    /**
     * 计算订单取消手续费。
     * <p>
     * 手续费为订单总价的 50%，保留两位小数（TC_009）。
     * @param booking 订单实体
     * @return 手续费金额
     */
    public BigDecimal calculateCancellationCharge(Booking booking) {
        return booking.getTotalCost()
                .multiply(new BigDecimal("0.50"))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 执行订单取消操作（事务方法）。
     * <p>
     * 取消流程：
     * <ol>
     *   <li>验证订单存在且当前用户有权访问</li>
     *   <li>验证订单可取消</li>
     *   <li>将订单状态更新为 CANCELLED</li>
     *   <li>记录取消时间</li>
     *   <li>计算并记录手续费</li>
     *   <li>删除座位占用记录，释放座位（TC_008）</li>
     * </ol>
     * @param bookingReference 订单编号
     * @return 取消后的订单摘要
     * @throws AccessDeniedException 如果无权访问此订单
     * @throws IllegalArgumentException 如果订单不存在
     * @throws IllegalStateException 如果订单不可取消
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

    /**
     * 权限校验：确保当前用户可以访问指定订单。
     * <p>
     * - 客户：只能访问 customer_id 为自己的订单
     * - 员工：无此限制（但仍需登录）
     * @param booking 订单实体
     * @throws AccessDeniedException 如果无权访问此订单
     */
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
     * 将订单实体转换为客户订单行 DTO（用于列表展示）。
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
                canCancel(booking) // 驱动数据表格上取消按钮的启用状态
        );
    }

    /**
     * 将订单实体转换为订单摘要 DTO（用于详情展示）。
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
