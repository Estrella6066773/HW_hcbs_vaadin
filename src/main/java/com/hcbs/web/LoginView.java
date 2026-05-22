package com.hcbs.web;

import com.hcbs.config.DemoAccountCatalog;
import com.hcbs.web.component.BackToHomeAction;
import com.hcbs.web.component.PageHero;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.html.UnorderedList;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import jakarta.servlet.ServletException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Route("login")
@PageTitle("Sign in")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final TextField username = new TextField("Username");
    private final PasswordField password = new PasswordField("Password");
    private final Span loginError = new Span("Invalid username or password");

    public LoginView() {
        setWidthFull();
        setPadding(false);
        setMargin(false);
        addClassName("page-view");
        addClassName("auth-page");

        Div actions = new Div(new BackToHomeAction());
        actions.addClassName("film-detail-actions");

        PageHero hero = new PageHero(
                "Sign in",
                "Horizon Cinemas",
                "Sign in to book tickets, view orders, or use staff tools."
        );

        username.setRequiredIndicatorVisible(true);
        username.setWidthFull();
        username.getElement().setAttribute("autocomplete", "username");

        password.setRequiredIndicatorVisible(true);
        password.setWidthFull();
        password.setRevealButtonVisible(true);
        password.getElement().setAttribute("autocomplete", "current-password");

        loginError.addClassName("auth-login-error");
        loginError.setVisible(false);

        Paragraph passwordNote = new Paragraph(
                "演示账号统一密码：" + DemoAccountCatalog.DEMO_PASSWORD);
        passwordNote.addClassName("auth-hint");
        passwordNote.addClassName("auth-password-note");

        Div demoPanel = buildDemoAccountPanel();

        Button submit = new Button("Sign in", event -> submitLogin());
        submit.addClassName("primary-action");

        Div credentialsPanel = new Div(username, password, submit);
        credentialsPanel.addClassName("auth-credentials-panel");

        Anchor register = new Anchor("register", "没有账号？注册新客户");
        register.addClassName("auth-link");

        VerticalLayout page = new VerticalLayout(
                actions,
                hero,
                loginError,
                credentialsPanel,
                register,
                passwordNote,
                demoPanel);
        page.addClassName("auth-page-content");
        page.setPadding(false);
        page.setSpacing(true);
        page.setWidthFull();

        add(page);
    }

    private void submitLogin() {
        loginError.setVisible(false);
        String user = username.getValue();
        String pass = password.getValue();
        if (user == null || user.isBlank() || pass == null || pass.isEmpty()) {
            loginError.setText("请输入用户名和密码");
            loginError.setVisible(true);
            return;
        }

        VaadinServletRequest request = (VaadinServletRequest) VaadinServletRequest.getCurrent();
        if (request == null) {
            loginError.setText("无法提交登录请求，请刷新页面后重试");
            loginError.setVisible(true);
            return;
        }

        try {
            request.getHttpServletRequest().login(user.trim(), pass);
            request.getHttpServletRequest().changeSessionId();
            getUI().ifPresent(ui -> {
                String redirect = resolveRedirectTarget();
                if (redirect != null && !redirect.isBlank()) {
                    ui.navigate(redirect.startsWith("/") ? redirect.substring(1) : redirect);
                } else {
                    ui.navigate("");
                }
            });
        } catch (ServletException ex) {
            loginError.setText("Invalid username or password");
            loginError.setVisible(true);
        }
    }

    private Div buildDemoAccountPanel() {
        Div panel = new Div();
        panel.addClassName("demo-accounts-panel");

        Paragraph intro = new Paragraph("数据库初始化后可使用以下演示账号登录（每类 3 个）：");
        intro.addClassName("auth-hint");
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
                ListItem item = new ListItem(account.username());
                list.add(item);
            }
            panel.add(heading, list);
        }

        return panel;
    }

    private String pendingRedirect;

    private String resolveRedirectTarget() {
        return pendingRedirect;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        pendingRedirect = event.getLocation()
                .getQueryParameters()
                .getParameters()
                .getOrDefault("redirect", List.of())
                .stream()
                .findFirst()
                .orElse(null);
        if (event.getLocation()
                .getQueryParameters()
                .getParameters()
                .containsKey("error")) {
            loginError.setVisible(true);
        }
        if (event.getLocation()
                .getQueryParameters()
                .getParameters()
                .containsKey("registered")) {
            Notification.show("注册成功，请使用用户名和密码登录。");
        }
    }
}
