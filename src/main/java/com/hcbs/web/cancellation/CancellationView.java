package com.hcbs.web.cancellation;

import com.hcbs.dto.CustomerBookingRow;
import com.hcbs.model.BookingStatus;
import com.hcbs.security.CurrentUserService;
import com.hcbs.service.cancellation.CancellationService;
import com.hcbs.util.PhoneNumbers;
import com.hcbs.web.component.PageHero;
import com.hcbs.web.shell.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

/**
 * 员工取消柜台（成员 C · 取消模块）。
 * <p>
 * 路由为 {@code /cancellation}，仅允许 {@code BOOKING_STAFF} 和 {@code ADMIN} 角色访问（与侧边栏菜单可见性一致）。
 * 功能流程：按客户手机号搜索订单 → 在数据表格中展示订单列表 → 点击取消按钮调用 {@link CancellationService#cancelBooking} 执行取消。
 * <p>
 * 界面结构：
 * <ul>
 *   <li>左侧：搜索和订单列表区，包含手机号下拉框和订单数据表格</li>
 *   <li>右侧：取消规则说明面板，展示业务规则和当前登录员工姓名</li>
 * </ul>
 * <p>
 * 答辩演示场景：员工登录后取消测试订单 {@code HCBS-SEED001}（对应 TC_008–010 测试用例）。
 * 业务规则完全在 Service 层实现；本页面右侧规则面板仅作展示说明，不代表最终校验依据。
 */
@Route(value = "cancellation", layout = MainLayout.class)
@PageTitle("Cancellation")
@RolesAllowed({"BOOKING_STAFF", "ADMIN"}) // 客户应使用 MyBookingsView，无权访问此页面
public class CancellationView extends VerticalLayout {

    /** 取消服务，处理业务逻辑 */
    private final CancellationService cancellationService;
    /** 当前用户服务，获取登录状态 */
    private final CurrentUserService currentUserService;

    /** 客户手机号选择框，用于搜索订单 */
    private final ComboBox<String> customerPhone = new ComboBox<>("Customer phone");
    /** 订单数据表格，展示选中手机号的所有订单 */
    private final Grid<CustomerBookingRow> bookingsGrid = new Grid<>(CustomerBookingRow.class, false);

    /** 当前选中的手机号，用于在取消成功后刷新同手机号的订单列表 */
    private String activePhone;

    /**
     * 构造函数：初始化取消柜台页面。
     * @param cancellationService 取消服务
     * @param currentUserService 当前用户服务
     */
    public CancellationView(CancellationService cancellationService, CurrentUserService currentUserService) {
        this.cancellationService = cancellationService;
        this.currentUserService = currentUserService;
        setSizeFull();
        setPadding(false);
        setMargin(false);
        addClassName("page-view");

        // 配置手机号选择框：支持前缀搜索和手动输入
        customerPhone.setWidthFull();
        customerPhone.setClearButtonVisible(true);
        customerPhone.setAllowCustomValue(true); // 允许手动输入未出现在下拉列表中的手机号
        customerPhone.setItems(query -> {
            String filter = query.getFilter().orElse("");
            // 仅员工可调用此接口；返回曾订票的客户或访客手机号的前缀匹配结果
            return cancellationService.searchPhonesWithBookings(filter).stream()
                    .skip(query.getOffset())
                    .limit(query.getLimit());
        });
        customerPhone.addValueChangeListener(event -> {
            if (event.getValue() != null) {
                activePhone = event.getValue();
                loadBookings(event.getValue());
            } else {
                activePhone = null;
                bookingsGrid.setItems();
            }
        });
        customerPhone.addCustomValueSetListener(event -> {
            activePhone = PhoneNumbers.normalize(event.getDetail());
            loadBookings(event.getDetail());
        });

        // 配置订单数据表格：展示订单信息和操作按钮
        bookingsGrid.addColumn(CustomerBookingRow::bookingReference).setHeader("Reference").setFlexGrow(1);
        bookingsGrid.addColumn(CustomerBookingRow::filmTitle).setHeader("Film").setFlexGrow(2);
        bookingsGrid.addColumn(CustomerBookingRow::showDate).setHeader("Date");
        bookingsGrid.addColumn(CustomerBookingRow::startTime).setHeader("Time");
        bookingsGrid.addColumn(CustomerBookingRow::numberOfTickets).setHeader("Tickets");
        bookingsGrid.addColumn(CustomerBookingRow::totalCost).setHeader("Total");
        bookingsGrid.addColumn(row -> row.status().name()).setHeader("Status");
        bookingsGrid.addComponentColumn(row -> {
            Button cancel = new Button("Cancel", event -> cancelBooking(row.bookingReference()));
            // canCancel 由 Service 层计算：订单状态必须为 CONFIRMED 且放映日期严格晚于今天（TC_010 当日禁用按钮）
            boolean cancellable = row.status() == BookingStatus.CONFIRMED && row.canCancel();
            cancel.setEnabled(cancellable);
            cancel.addClassName(cancellable ? "danger-action" : "inactive-action");
            return cancel;
        }).setHeader("Action");
        bookingsGrid.setWidthFull();
        bookingsGrid.setAllRowsVisible(true);

        // 构建左侧搜索区
        Div lookupPanel = new Div(
                sectionTitle("Refund desk", "Search by phone to list bookings, then cancel the selected order."),
                customerPhone,
                bookingsGrid);
        lookupPanel.addClassName("surface-panel");

        // 构建右侧规则区
        Div rulePanel = new Div(
                sectionTitle("Cancellation rules", "Policy checks apply before seats are released."),
                ruleLine("Before showing day", "Allowed"),
                ruleLine("Same day", "Rejected"),
                ruleLine("Cancellation charge", "50% of total booking cost"),
                ruleLine("Signed in as", currentUserService.requireCurrentUser().getFullName()));
        rulePanel.addClassName("surface-panel");
        rulePanel.addClassName("rule-panel");

        // 组装页面布局
        HorizontalLayout workspace = new HorizontalLayout(lookupPanel, rulePanel);
        workspace.addClassName("booking-workspace");
        workspace.setWidthFull();

        add(new PageHero("Refund control", "Cancellation", "Find orders by phone and cancel eligible bookings."), workspace);
    }

    /**
     * 根据手机号加载订单列表。
     * @param rawPhone 原始手机号
     */
    private void loadBookings(String rawPhone) {
        try {
            String phone = PhoneNumbers.normalize(rawPhone);
            if (phone == null || phone.length() < 3) {
                bookingsGrid.setItems();
                return;
            }
            bookingsGrid.setItems(cancellationService.listBookingsByPhone(phone));
        } catch (RuntimeException ex) {
            bookingsGrid.setItems();
            Notification.show(ex.getMessage());
        }
    }

    /**
     * 取消指定订单。
     * @param reference 订单编号
     */
    private void cancelBooking(String reference) {
        try {
            cancellationService.cancelBooking(reference); // TC_008/009：释放座位 + 50% 手续费
            if (activePhone != null && !activePhone.isBlank()) {
                loadBookings(activePhone); // 刷新当前手机号的订单列表
            } else {
                bookingsGrid.setItems();
            }
            Notification.show("Booking cancelled: " + reference);
        } catch (RuntimeException ex) {
            Notification.show(ex.getMessage()); // 例如当日取消会抛出 IllegalStateException
        }
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

    /**
     * 构建规则说明行组件。
     * @param label 规则标签
     * @param value 规则值
     * @return 规则说明行组件
     */
    private Div ruleLine(String label, String value) {
        Span labelSpan = new Span(label);
        labelSpan.addClassName("metric-label");
        Span valueSpan = new Span(value);
        valueSpan.addClassName("policy-value");
        Div line = new Div(labelSpan, valueSpan);
        line.addClassName("policy-line");
        return line;
    }
}
