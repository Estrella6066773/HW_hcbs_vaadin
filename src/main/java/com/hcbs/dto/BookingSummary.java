package com.hcbs.dto;

import com.hcbs.model.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 订单详情摘要 DTO（成员 C · 取消模块）。
 * <p>
 * 用于展示订单的完整信息，包括客户名称、操作人信息等。
 *
 * @param bookingReference 订单编号
 * @param filmTitle 影片标题
 * @param showDate 放映日期
 * @param startTime 放映时间
 * @param totalCost 订单总价
 * @param status 订单状态
 * @param canCancel 是否可以取消此订单
 * @param cancellationCharge 取消手续费（仅在已取消时可能有值）
 * @param customerName 客户名称（注册客户姓名或访客手机号）
 * @param bookedByName 下单人姓名
 */
public record BookingSummary(
        String bookingReference,
        String filmTitle,
        LocalDate showDate,
        LocalTime startTime,
        BigDecimal totalCost,
        BookingStatus status,
        boolean canCancel,
        BigDecimal cancellationCharge,
        String customerName,
        String bookedByName
) {
    /**
     * 将订单摘要转换为格式化的文本。
     * @return 格式化的订单详情文本
     */
    public String toDetailText() {
        return """
                Reference: %s
                Customer: %s
                Booked by: %s
                Film: %s
                Date: %s
                Time: %s
                Total cost: £%s
                Status: %s
                Can cancel now: %s
                Cancellation charge: £%s
                """.formatted(
                bookingReference,
                customerName,
                bookedByName,
                filmTitle,
                showDate,
                startTime,
                totalCost,
                status,
                canCancel ? "Yes" : "No",
                cancellationCharge == null ? "0.00" : cancellationCharge);
    }
}
