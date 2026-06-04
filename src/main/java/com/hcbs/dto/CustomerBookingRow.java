package com.hcbs.dto;

import com.hcbs.model.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 客户订单列表行 DTO（成员 C · 取消模块）。
 * <p>
 * 用于在数据表格中展示订单基本信息，用于 {@link com.hcbs.web.cancellation.CancellationView}
 * 和 {@link com.hcbs.web.cancellation.MyBookingsView}。
 *
 * @param bookingReference 订单编号
 * @param filmTitle 影片标题
 * @param showDate 放映日期
 * @param startTime 放映时间
 * @param numberOfTickets 座位数量
 * @param totalCost 订单总价
 * @param status 订单状态
 * @param canCancel 是否可以取消此订单
 */
public record CustomerBookingRow(
        String bookingReference,
        String filmTitle,
        LocalDate showDate,
        LocalTime startTime,
        int numberOfTickets,
        BigDecimal totalCost,
        BookingStatus status,
        boolean canCancel
) {
}
