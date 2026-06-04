package com.hcbs.web.shell;

import com.hcbs.model.User;
import com.hcbs.web.admin.AdminDataView;
import com.hcbs.web.auth.AccountCenterView;
import com.hcbs.web.auth.LoginView;
import com.hcbs.web.booking.BookingView;
import com.hcbs.web.cancellation.CancellationView;
import com.hcbs.web.cancellation.MyBookingsView;
import com.hcbs.web.home.FilmRecommendView;
import com.hcbs.security.AuthUiService;
import com.hcbs.security.CurrentUserService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.auth.AnonymousAllowed;

/**
 * 全局导航壳层：顶栏 + 左侧菜单 + 右侧主内容槽位。
 * <p>
 * 带 {@code layout = MainLayout.class} 的页面（首页、订票、我的订单等）会嵌在本布局右侧；
 * 登录/注册页不使用本布局，为全屏独立页。
 * <p>
 * 继承 Vaadin 的 {@link AppLayout}（来自依赖 jar，非本项目源码），使用其 {@code addToNavbar}、
 * {@code addToDrawer} 挂载顶栏与侧栏。样式类名（如 {@code topbar}）在 {@code styles.css} 中定义。
 */
@AnonymousAllowed // 未登录也可进入带此 layout 的公开页（如首页）；各子页可另有权限注解
public class MainLayout extends AppLayout implements AfterNavigationObserver {

    // —— 成员变量：构造时创建或注入，整个 layout 生命周期内复用 ——

    /** 查询当前是否登录、用户角色（驱动菜单与顶栏显示） */
    private final CurrentUserService currentUserService;
    /** 处理登出等认证相关 UI 动作 */
    private final AuthUiService authUiService;
    /** 顶栏品牌区副标题，文字会随登录角色在 refreshChrome 中更新 */
    private final Span subtitle = new Span("Cinema booking portal");
    /** 顶栏右侧按钮区容器；内部按钮在 refreshHeader 中按登录态重建 */
    private final HorizontalLayout headerActions = new HorizontalLayout();
    /** 左侧抽屉菜单容器；菜单项在 refreshDrawer 中按角色重建 */
    private final VerticalLayout drawerContent = new VerticalLayout();

    /**
     * 构造器：只执行一次，搭建固定 UI 骨架（品牌、顶栏结构、侧栏空容器）。
     * 末尾调用 {@link #refreshChrome()} 首次填入可变内容（按钮、菜单、副标题）。
     * 换页面时 layout 对象通常不重建，登录态变化靠 {@link #afterNavigation} 刷新。
     */
    public MainLayout(CurrentUserService currentUserService, AuthUiService authUiService) {
        this.currentUserService = currentUserService;
        this.authUiService = authUiService;
        addClassName("hcbs-shell");

        // —— 品牌区：HC 标记 + 主标题 + 副标题 ——
        Div mark = new Div("HC");
        mark.addClassName("brand-mark");

        H1 title = new H1("Horizon Cinemas");
        subtitle.addClassName("brand-subtitle");

        VerticalLayout brandText = new VerticalLayout(title, subtitle);
        brandText.addClassName("brand-text");
        brandText.setPadding(false);
        brandText.setSpacing(false);

        HorizontalLayout brand = new HorizontalLayout(mark, brandText);
        brand.addClassName("brand-lockup");

        // —— 顶栏右侧：空容器先挂好，具体按钮由 refreshHeader 填充 ——
        headerActions.addClassName("header-actions");
        headerActions.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        headerActions.setSpacing(true);

        // —— 顶栏整行：[汉堡] [品牌] [右侧操作区] ——
        HorizontalLayout header = new HorizontalLayout(new DrawerToggle(), brand, headerActions);
        header.addClassName("topbar");
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.setWidthFull();
        header.expand(brand); // 品牌区占据中间剩余空间

        addToNavbar(header); // 挂到 AppLayout 父类提供的顶栏槽位

        // —— 侧栏：空容器挂到抽屉，菜单项由 refreshDrawer 填充 ——
        drawerContent.setPadding(false);
        drawerContent.setSpacing(false);
        drawerContent.setWidthFull();
        addToDrawer(drawerContent);

        refreshChrome();
    }

    /** 每次路由切换完成后调用，刷新顶栏/侧栏/副标题（构造器只跑一次，无法感知后续登录变化） */
    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        refreshChrome();
    }

    /** 统一刷新壳层可变部分；不负责右侧业务内容（由各 View 的路由切换负责） */
    private void refreshChrome() {
        refreshHeader();
        refreshDrawer();
        subtitle.setText(resolveSubtitle());
    }

    /** 按登录态重建顶栏右侧：未登录仅 Account；已登录显示姓名 + Account + Sign out */
    private void refreshHeader() {
        headerActions.removeAll();
        Button account = new Button("Account", event -> openAccountCenter());
        account.addClassName("primary-action");
        account.addClassName("account-button");

        if (currentUserService.isAuthenticated()) {
            User user = currentUserService.requireCurrentUser();
            Span userBadge = new Span(user.getFullName());
            userBadge.addClassName("user-badge");

            Button signOut = new Button("Sign out", event -> authUiService.signOut());
            signOut.addClassName("secondary-action");
            signOut.addClassName("sign-out-button");

            headerActions.add(userBadge, account, signOut);
        } else {
            headerActions.add(account);
        }
    }

    /**
     * 按角色重建侧栏菜单（答辩重点）。
     * <pre>
     * 未登录     → Home
     * CUSTOMER   → Home + My bookings
     * BOOKING_STAFF → Home + Book tickets + Cancellation
     * ADMIN      → 同上 + Data admin
     * </pre>
     */
    private void refreshDrawer() {
        drawerContent.removeAll();

        Span drawerLabel = new Span("Menu");
        drawerLabel.addClassName("drawer-label");
        drawerContent.add(drawerLabel);
        drawerContent.add(navLink("Home", FilmRecommendView.class, "Browse films and find showtimes"));
        if (currentUserService.isAuthenticated() && currentUserService.requireCurrentUser().getRole().isEmployee()) {
            drawerContent.add(navLink("Book tickets", BookingView.class, "Desk booking for any showing"));
        }

        if (currentUserService.isAuthenticated()) {
            User user = currentUserService.requireCurrentUser();
            if (user.getRole().isCustomer()) {
                drawerContent.add(navLink("My bookings", MyBookingsView.class, "View and cancel your orders"));
            } else {
                drawerContent.add(navLink("Cancellation", CancellationView.class, "Refund desk for any booking"));
                if (user.getRole().canAccessAdminTools()) {
                    drawerContent.add(navLink("Data admin", AdminDataView.class, "Manage films and accounts"));
                }
            }
        }
    }

    /** 根据当前用户角色返回顶栏副标题文案 */
    private String resolveSubtitle() {
        return currentUserService.findCurrentUser()
                .map(user -> user.getRole().isCustomer()
                        ? "Customer self-service"
                        : "Employee control desk")
                .orElse("Cinema booking portal");
    }

    /** Account 按钮：已登录跳转账户中心，未登录跳转登录页 */
    private void openAccountCenter() {
        getUI().ifPresent(ui -> {
            if (currentUserService.isAuthenticated()) {
                ui.navigate(AccountCenterView.class);
            } else {
                ui.navigate(LoginView.class);
            }
        });
    }

    /**
     * 生成侧栏导航卡片并绑定路由。
     * {@code link.setRoute(route)} 将链接与带 {@code @Route} 的 View 类绑定（非 styles.css）；
     * 点击后 Vaadin 跳转对应 URL，右侧主内容区切换为该 View。
     */
    private RouterLink navLink(String title, Class<? extends Component> route, String caption) {
        RouterLink link = new RouterLink();
        link.setRoute(route);
        link.addClassName("nav-card");

        Span name = new Span(title);
        name.addClassName("nav-title");
        Span detail = new Span(caption);
        detail.addClassName("nav-caption");
        link.add(name, detail);
        return link;
    }
}
