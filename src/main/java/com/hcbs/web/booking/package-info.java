/**
 * Book tickets 板块 — 选座订票。
 *
 * <ul>
 *   <li>{@link com.hcbs.web.booking.BookingView} — {@code /booking}</li>
 *   <li>{@link com.hcbs.web.booking.component.SeatMapPicker} — 10×10 座位图（Vaadin Button 网格）</li>
 * </ul>
 *
 * 交互：未登录 → 转发 {@code LoginView?redirect=booking}；选场次 → 加载座位图 → 点 Confirm
 * → {@code BookingService.createBooking} → 收据写入 TextArea。
 *
 * 触发事件：{@code beforeEnter}（URL showingId）、ComboBox 换场次、座位 Button 点击、Confirm 按钮。
 */
package com.hcbs.web.booking;
