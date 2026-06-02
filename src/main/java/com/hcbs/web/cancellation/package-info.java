/**
 * Cancellation 板块 — 取消订单。
 *
 * <ul>
 *   <li>{@link com.hcbs.web.cancellation.CancellationView} — {@code /cancellation}（员工柜台）</li>
 *   <li>{@link com.hcbs.web.cancellation.MyBookingsView} — {@code /my-bookings}（客户自助）</li>
 * </ul>
 *
 * 交互：输入手机号 → {@code CancellationService.listBookingsByPhone} → Grid 展示
 * → Cancel 按钮 → {@code cancelBooking} → Notification 反馈。
 *
 * 依赖 Service：{@code CancellationService}（业务规则在 Service，View 只展示）。
 */
package com.hcbs.web.cancellation;
