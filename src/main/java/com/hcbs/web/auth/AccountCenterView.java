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
 * 路由 {@code /account}，嵌在 {@link com.hcbs.web.shell.MainLayout} 右侧。
 * 顶栏 Account 按钮：未登录跳转 {@link LoginView}，已登录跳转本页。
 * 已登录时按角色展示快捷入口（客户：我的订单；员工：柜台 + 可选管理）。
 */
@Route(value = "account", layout = MainLayout.class)
@PageTitle("Account")
@AnonymousAllowed
public class AccountCenterView extends VerticalLayout {

    public AccountCenterView(CurrentUserService currentUserService, AuthUiService authUiService) {
        setSizeFull();
        setPadding(false);
        setMargin(false);
        addClassName("page-view");

        if (!currentUserService.isAuthenticated()) {
            add(guestPanel());
            return;
        }

        User user = currentUserService.requireCurrentUser();
        add(signedInPanel(user, authUiService));
    }

    /** 未登录：引导注册或登录 */
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

    /** 已登录：展示资料与角色相关快捷导航 */
    private Div signedInPanel(User user, AuthUiService authUiService) {
        H2 title = new H2("Account");
        Span profile = new Span(user.getFullName() + " · " + user.getEmail());
        profile.addClassName("account-profile");

        Div links = new Div();
        links.addClassName("account-links");

        if (user.getRole().isCustomer()) {
            links.add(linkButton("My bookings", MyBookingsView.class));
            links.add(linkButton("Book tickets", BookingView.class));
        } else {
            links.add(linkButton("Booking desk", BookingView.class));
            links.add(linkButton("Cancellation desk", CancellationView.class));
            if (user.getRole().canAccessAdminTools()) {
                links.add(linkButton("Data admin", AdminDataView.class));
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

    private Button linkButton(String label, Class<? extends com.vaadin.flow.component.Component> route) {
        Button button = new Button(label, event -> getUI().ifPresent(ui -> ui.navigate(route)));
        button.addClassName("secondary-action");
        return button;
    }
}
