package com.hcbs.web.cancellation;

import com.hcbs.dto.CustomerBookingRow;
import com.hcbs.model.BookingStatus;
import com.hcbs.service.cancellation.CancellationService;
import com.hcbs.web.shell.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

/**
 * 客户自助「我的订单」页面（成员 C · 取消模块）。
 * <p>
 * 路由为 {@code /my-bookings}，仅允许 {@code CUSTOMER} 角色访问（与侧边栏菜单可见性一致）。
 * 订单列表来自 {@link CancellationService#listAccessibleBookings()}，Service 层会自动按当前登录客户过滤订单。
 * 仅能取消本人名下、状态为 CONFIRMED 且满足「放映日期严格晚于今天」条件的订单（对应 TC_008–010 测试用例）。
 */
@Route(value = "my-bookings", layout = MainLayout.class)
@PageTitle("My bookings")
@RolesAllowed("CUSTOMER")
public class MyBookingsView extends VerticalLayout {

    /** 取消服务，处理订单查询和取消操作 */
    private final CancellationService cancellationService;
    /** 订单数据表格，展示客户的所有订单 */
    private final Grid<CustomerBookingRow> grid = new Grid<>(CustomerBookingRow.class, false);

    /**
     * 构造函数：初始化「我的订单」页面。
     * @param cancellationService 取消服务
     */
    public MyBookingsView(CancellationService cancellationService) {
        this.cancellationService = cancellationService;
        setSizeFull();
        setPadding(false);
        setMargin(false);
        addClassName("page-view");

        // 配置订单数据表格
        grid.addColumn(CustomerBookingRow::bookingReference).setHeader("Reference").setFlexGrow(1);
        grid.addColumn(CustomerBookingRow::filmTitle).setHeader("Film").setFlexGrow(2);
        grid.addColumn(CustomerBookingRow::showDate).setHeader("Date");
        grid.addColumn(CustomerBookingRow::startTime).setHeader("Time");
        grid.addColumn(CustomerBookingRow::numberOfTickets).setHeader("Tickets");
        grid.addColumn(CustomerBookingRow::totalCost).setHeader("Total");
        grid.addColumn(row -> row.status().name()).setHeader("Status");
        grid.addComponentColumn(row -> {
            Button cancel = new Button("Cancel", event -> cancel(row.bookingReference()));
            boolean cancellable = row.status() == BookingStatus.CONFIRMED && row.canCancel();
            cancel.setEnabled(cancellable);
            cancel.addClassName(cancellable ? "danger-action" : "inactive-action");
            return cancel;
        }).setHeader("Action");
        grid.setItems(cancellationService.listAccessibleBookings()); // Service 层内按当前 CUSTOMER 自动过滤订单
        grid.setWidthFull();

        // 构建页面布局
        Div panel = new Div(sectionTitle("Your orders", "Only bookings on your account are listed. Cancel before show day."), grid);
        panel.addClassName("surface-panel");

        add(pageHero("My bookings", "View and cancel orders you placed."), panel);
    }

    /**
     * 取消指定订单。
     * @param reference 订单编号
     */
    private void cancel(String reference) {
        try {
            cancellationService.cancelBooking(reference); // Service 层会通过 assertCanAccess 保证仅可取消本人订单
            grid.setItems(cancellationService.listAccessibleBookings()); // 刷新订单列表
            Notification.show("Booking cancelled: " + reference);
        } catch (RuntimeException ex) {
            Notification.show(ex.getMessage());
        }
    }

    /**
     * 构建页头组件。
     * @param heading 标题文本
     * @param copy 说明文本
     * @return 页头组件
     */
    private Div pageHero(String heading, String copy) {
        Span badge = new Span("Self-service");
        badge.addClassName("eyebrow");
        H2 title = new H2(heading);
        Paragraph description = new Paragraph(copy);
        Div hero = new Div(badge, title, description);
        hero.addClassName("page-hero");
        return hero;
    }

    /**
     * 构建章节标题组件。
     * @param title 标题文本
     * @param caption 说明文本
     * @return 章节标题组件
     */
    private Div sectionTitle(String title, String caption) {
        H2 heading = new H2(title);
        Paragraph detail = new Paragraph(caption);
        Div wrapper = new Div(heading, detail);
        wrapper.addClassName("section-title");
        return wrapper;
    }
}
