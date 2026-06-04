package com.hcbs.web.auth;

import com.hcbs.config.DemoAccountCatalog;
import com.hcbs.web.home.component.BackToHomeAction;
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
import com.hcbs.util.PhoneNumbers;
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import jakarta.servlet.ServletException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 登录页（成员 C · 账户模块）。
 * <p>
 * 路由 {@code /login}，全屏独立页，不使用 {@link com.hcbs.web.shell.MainLayout}。
 * 未登录用户访问受保护页（如订票）时，Spring Security 会转发到此页；
 * 支持 {@code ?redirect=/booking} 登录成功后回到原目标。
 * <p>
 * 核心流程：提交表单 → {@link jakarta.servlet.http.HttpServletRequest#login(String, String)}
 * 建立会话 → {@code changeSessionId()} 防固定会话 → 跳转 redirect 或首页。
 * 演示账号列表来自 {@link com.hcbs.config.DemoAccountCatalog}。
 */
@Route("login")
@PageTitle("Sign in")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final TextField phone = new TextField("Phone number");
    private final PasswordField password = new PasswordField("Password");
    private final Span loginError = new Span("Incorrect phone number or password");

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

        phone.setRequiredIndicatorVisible(true);
        phone.setWidthFull();
        phone.getElement().setAttribute("autocomplete", "tel");

        password.setRequiredIndicatorVisible(true);
        password.setWidthFull();
        password.setRevealButtonVisible(true);
        password.getElement().setAttribute("autocomplete", "current-password");

        loginError.addClassName("auth-login-error");
        loginError.setVisible(false);

        Paragraph passwordNote = new Paragraph(
                "Demo account password: " + DemoAccountCatalog.DEMO_PASSWORD);
        passwordNote.addClassName("auth-hint");
        passwordNote.addClassName("auth-password-note");

        Div demoPanel = buildDemoAccountPanel();

        Button submit = new Button("Sign in", event -> submitLogin());
        submit.addClassName("primary-action");

        Div credentialsPanel = new Div(phone, password, submit);
        credentialsPanel.addClassName("auth-credentials-panel");

        Anchor register = new Anchor("register", "No account? Register as a new customer");
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

    /** 校验输入后调用容器级 login；失败显示错误，成功按 redirect 参数导航 */
    private void submitLogin() {
        loginError.setVisible(false);
        String user = phone.getValue();
        String pass = password.getValue();
        if (user == null || user.isBlank() || pass == null || pass.isEmpty()) {
            loginError.setText("Please enter your phone number and password");
            loginError.setVisible(true);
            return;
        }

        VaadinServletRequest request = (VaadinServletRequest) VaadinServletRequest.getCurrent();
        if (request == null) {
            loginError.setText("Unable to submit sign-in request. Please refresh the page and try again.");
            loginError.setVisible(true);
            return;
        }

        try {
            request.getHttpServletRequest().login(PhoneNumbers.normalize(user.trim()), pass);
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
            loginError.setText("Incorrect phone number or password");
            loginError.setVisible(true);
        }
    }

    /** 按角色分组展示种子库演示手机号（答辩演示三种角色登录） */
    private Div buildDemoAccountPanel() {
        Div panel = new Div();
        panel.addClassName("demo-accounts-panel");

        Paragraph intro = new Paragraph(
                "After database initialization, sign in with the demo phone numbers below (3 per role, password: demo):");
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
                ListItem item = new ListItem(account.phone());
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

    /** 解析 {@code redirect}、{@code error}、{@code registered} 查询参数 */
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
            Notification.show("Registration successful. Please sign in with your phone number and password.");
        }
    }
}
