package com.hcbs.web.auth;

import com.hcbs.model.User;
import com.hcbs.security.AuthUiService;
import com.hcbs.security.CurrentUserService;
import com.hcbs.web.admin.AdminDataView;
import com.hcbs.web.booking.BookingView;
import com.hcbs.web.cancellation.CancellationView;
import com.hcbs.web.cancellation.MyBookingsView;
import com.hcbs.web.shell.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

/**
 * 账户中心（成员 C · 账户模块）。
 * <p>
 * 路由为 {@code /account}，嵌在 {@link MainLayout} 右侧。
 * 顶栏 Account 按钮：未登录时跳转到 {@link LoginView}，已登录时跳转到本页。
 * 已登录时按角色展示快捷入口（与 {@link MainLayout#refreshDrawer()} 菜单互补，但不替代权限控制）。
 */
@Route(value = "account", layout = MainLayout.class)
@PageTitle("Account")
@AnonymousAllowed // 未登录显示 guestPanel，不抛 403
public class AccountCenterView extends VerticalLayout {

    public AccountCenterView(CurrentUserService currentUserService, AuthUiService authUiService) {
        setSizeFull();
        setPadding(false);
        setMargin(false);
        addClassName("page-view");

        if (!currentUserService.isAuthenticated()) {
            add(guestPanel());
            return; // 不调用 requireCurrentUser，避免 AccessDeniedException
        }

        User user = currentUserService.requireCurrentUser();
        add(signedInPanel(user, authUiService));
    }

    /**
     * 未登录时的面板：引导用户注册或登录（与侧边栏仅显示 Home 时的 Account 顶栏行为一致）。
     * @return 访客面板组件
     */
    private Div guestPanel() {
        H2 title = new H2("Account");
        Paragraph copy = new Paragraph("Sign in to book tickets, view orders, and manage your account.");
        Button signIn = new Button("Sign in", event ->
                getUI().ifPresent(ui -> ui.navigate(LoginView.class)));
        signIn.addClassName("primary-action");
        Button register = new Button("Register", event ->
                getUI().ifPresent(ui -> ui.navigate(RegisterView.class)));
        register.addClassName("secondary-action");

        Div panel = new Div(title, copy, signIn, register);
        panel.addClassName("surface-panel");
        panel.addClassName("account-panel");
        return panel;
    }

    /**
     * 已登录时的面板：展示用户资料与角色相关快捷导航。
     * 注意：各目标 View 仍有 {@code @RolesAllowed} 权限控制，此处按钮仅为便利入口。
     * @param user 当前登录用户
     * @param authUiService 认证 UI 服务
     * @return 已登录用户面板组件
     */
    private Div signedInPanel(User user, AuthUiService authUiService) {
        H2 title = new H2("Account");
        Span profile = new Span(user.getFullName() + " · " + user.getEmail());
        profile.addClassName("account-profile");

        Div links = new Div();
        links.addClassName("account-links");

        if (user.getRole().isCustomer()) {
            links.add(linkButton("My bookings", MyBookingsView.class));   // CUSTOMER 的取消入口
            links.add(linkButton("Book tickets", BookingView.class));     // 自助订票（B 模块）
        } else {
            links.add(linkButton("Booking desk", BookingView.class));       // BOOKING_STAFF / ADMIN
            links.add(linkButton("Cancellation desk", CancellationView.class));
            if (user.getRole().canAccessAdminTools()) {
                links.add(linkButton("Data admin", AdminDataView.class)); // 仅 ADMIN（D 主责数据管理）
            }
        }

        Button signOut = new Button("Sign out", event -> authUiService.signOut());
        signOut.addClassName("secondary-action");
        signOut.addClassName("sign-out-button");

        Div panel = new Div(title, profile, links, signOut);
        panel.addClassName("surface-panel");
        panel.addClassName("account-panel");
        return panel;
    }

    /**
     * 创建一个导航按钮，点击后跳转到指定路由。
     * @param label 按钮标签
     * @param route 目标路由类
     * @return 导航按钮组件
     */
    private Button linkButton(String label, Class<? extends com.vaadin.flow.component.Component> route) {
        Button button = new Button(label, event -> getUI().ifPresent(ui -> ui.navigate(route)));
        button.addClassName("secondary-action");
        return button;
    }
}
