package com.hcbs.web;

import com.hcbs.model.User;
import com.hcbs.security.AuthUiService;
import com.hcbs.security.CurrentUserService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

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

    private Div guestPanel() {
        H2 title = new H2("个人中心");
        Paragraph copy = new Paragraph("登录后可订票、查看订单并管理账户。");
        Button signIn = new Button("登录", event ->
                getUI().ifPresent(ui -> ui.navigate(LoginView.class)));
        signIn.addClassName("primary-action");
        Button register = new Button("注册新账户", event ->
                getUI().ifPresent(ui -> ui.navigate(RegisterView.class)));
        register.addClassName("secondary-action");

        Div panel = new Div(title, copy, signIn, register);
        panel.addClassName("surface-panel");
        panel.addClassName("account-panel");
        return panel;
    }

    private Div signedInPanel(User user, AuthUiService authUiService) {
        H2 title = new H2("个人中心");
        Span profile = new Span(user.getFullName() + " · " + user.getEmail());
        profile.addClassName("account-profile");

        Div links = new Div();
        links.addClassName("account-links");

        if (user.getRole().isCustomer()) {
            links.add(linkButton("我的订单", MyBookingsView.class));
            links.add(linkButton("订票", BookingView.class));
        } else {
            links.add(linkButton("订票柜台", BookingView.class));
            links.add(linkButton("退票柜台", CancellationView.class));
            links.add(linkButton("数据管理", AdminDataView.class));
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
