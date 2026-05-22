package com.hcbs.web;

import com.hcbs.config.DemoAccountCatalog;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.UnorderedList;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Route("login")
@PageTitle("Sign in")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final LoginForm login = new LoginForm();

    public LoginView() {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        addClassName("login-view");

        H1 title = new H1("Horizon Cinemas");
        Paragraph passwordNote = new Paragraph(
                "演示账号统一密码：" + DemoAccountCatalog.DEMO_PASSWORD);
        passwordNote.addClassName("login-hint");

        Div demoPanel = buildDemoAccountPanel();

        login.setAction("login");
        login.setForgotPasswordButtonVisible(false);

        Anchor register = new Anchor("register", "没有账号？注册新客户");
        register.addClassName("register-link");

        VerticalLayout card = new VerticalLayout(title, passwordNote, demoPanel, login, register);
        card.addClassName("login-card");
        card.setAlignItems(Alignment.STRETCH);
        card.setWidth("480px");
        add(card);
    }

    private Div buildDemoAccountPanel() {
        Div panel = new Div();
        panel.addClassName("demo-accounts-panel");

        Paragraph intro = new Paragraph("数据库初始化后可使用以下演示账号登录（每类 3 个）：");
        intro.addClassName("login-hint");
        panel.add(intro);

        Map<String, List<DemoAccountCatalog.DemoAccount>> byRoleLabel = DemoAccountCatalog.all().stream()
                .collect(Collectors.groupingBy(
                        DemoAccountCatalog.DemoAccount::roleLabel,
                        LinkedHashMap::new,
                        Collectors.toList()));

        for (Map.Entry<String, List<DemoAccountCatalog.DemoAccount>> entry : byRoleLabel.entrySet()) {
            H3 heading = new H3(entry.getKey());
            heading.addClassName("demo-accounts-heading");

            UnorderedList list = new UnorderedList();
            list.addClassName("demo-accounts-list");
            for (DemoAccountCatalog.DemoAccount account : entry.getValue()) {
                ListItem item = new ListItem(account.username() + " — " + account.fullName());
                list.add(item);
            }
            panel.add(heading, list);
        }

        return panel;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (event.getLocation()
                .getQueryParameters()
                .getParameters()
                .containsKey("error")) {
            login.setError(true);
        }
        if (event.getLocation()
                .getQueryParameters()
                .getParameters()
                .containsKey("registered")) {
            Notification.show("注册成功，请使用用户名和密码登录。");
        }
    }
}
