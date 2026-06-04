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
 * 路由为 {@code /login}，全屏独立页面，不使用 {@link com.hcbs.web.shell.MainLayout}。
 * 当未登录用户访问受保护页面（如订票页面）时，Spring Security 会转发到此页面；
 * 支持通过 {@code ?redirect=/booking} 参数在登录成功后跳转到原目标页面。
 * <p>
 * 核心流程：提交表单 → 调用 {@link jakarta.servlet.http.HttpServletRequest#login(String, String)}
 * 建立会话 → 执行 {@code changeSessionId()} 防止会话固定攻击 → 跳转到指定 redirect 页面或首页。
 * 测试区块（用于答辩）：展示演示账号提示与列表，数据来自 {@link com.hcbs.config.DemoAccountCatalog}。
 *
 * @see com.hcbs.security.HcbsUserDetailsService#loadUserByUsername 负责校验密码与角色
 * @see com.hcbs.config.SecurityConfig#setLoginView 设置未登录访问受保护页面时的入口
 */
@Route("login") // 注册 URL /login；不绑定 MainLayout，避免未登录时也加载侧栏
@PageTitle("Sign in") // 浏览器标签页标题
@AnonymousAllowed // 允许匿名访问：否则未登录用户无法打开登录页本身
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    /** 登录名字段：业务上使用手机号，而非用户名；提交前会进行规范化处理 */
    private final TextField phone = new TextField("Phone number");
    /** 密码字段：明文密码仅通过 HTTPS 请求提交给 Servlet 容器 */
    private final PasswordField password = new PasswordField("Password");
    /** 错误提示条：默认隐藏，在校验失败或 URL 带 ?error 参数时显示 */
    private final Span loginError = new Span("Incorrect phone number or password");

    public LoginView() {
        // --- 页面容器样式：占满宽度、去掉默认边距，应用 auth 主题 ---
        setWidthFull();
        setPadding(false);
        setMargin(false);
        addClassName("page-view");
        addClassName("auth-page");

        // 「回首页」链接：协作组件（由成员 A 维护），跳转到公开首页 FilmRecommendView
        Div actions = new Div(new BackToHomeAction());
        actions.addClassName("film-detail-actions");

        // 页头：包含标题、品牌和一句说明，与注册页、取消页共用 PageHero 组件
        PageHero hero = new PageHero(
                "Sign in",
                "Horizon Cinemas",
                "Sign in to book tickets, view orders, or use staff tools.");

        // 手机号：必填、全宽、autocomplete=tel 方便浏览器或手机自动填充
        phone.setRequiredIndicatorVisible(true);
        phone.setWidthFull();
        phone.getElement().setAttribute("autocomplete", "tel");

        // 密码：必填、支持切换明文显示、autocomplete=current-password
        password.setRequiredIndicatorVisible(true);
        password.setWidthFull();
        password.setRevealButtonVisible(true);
        password.getElement().setAttribute("autocomplete", "current-password");

        // 错误条：初始隐藏，在 submitLogin 或 beforeEnter(?error) 时显示
        loginError.addClassName("auth-login-error");
        loginError.setVisible(false);

        // 提交按钮：点击时触发 submitLogin()，不使用 HTML form action
        Button submit = new Button("Sign in", event -> submitLogin());
        submit.addClassName("primary-action");

        // 凭证区：将手机号、密码和登录按钮打包在一个面板中
        Div credentialsPanel = new Div(phone, password, submit);
        credentialsPanel.addClassName("auth-credentials-panel");

        // 注册入口：使用 Vaadin Anchor 相对路径 register 指向 RegisterView
        Anchor register = new Anchor("register", "No account? Register as a new customer");
        register.addClassName("auth-link");

        // 组装整页：从上到下依次为回首页链接、页头、错误提示、表单、注册链接、测试演示区
        VerticalLayout page = new VerticalLayout(
                actions,
                hero,
                loginError,
                credentialsPanel,
                register,
                buildTestAccountSection()); // 测试用：答辩时使用的演示账号，见 region Test
        page.addClassName("auth-page-content");
        page.setPadding(false);
        page.setSpacing(true);
        page.setWidthFull();

        add(page);
    }

    /**
     * 登录提交处理：读取表单数据 → 调用 Servlet 标准 login 方法 → 更换会话 ID → 跳转到目标页面。
     * <p>
     * 不使用自定义 REST API；密码校验由 {@link com.hcbs.security.HcbsUserDetailsService} 完成。
     */
    private void submitLogin() {
        // 每次提交先清掉上次的错误提示
        loginError.setVisible(false);

        // 从 Vaadin 组件取值（尚未发 HTTP 请求）
        String user = phone.getValue();
        String pass = password.getValue();

        // 前端校验：空手机号或空密码直接提示，不调用 login()
        if (user == null || user.isBlank() || pass == null || pass.isEmpty()) {
            loginError.setText("Please enter your phone number and password");
            loginError.setVisible(true);
            return;
        }

        // 拿到当前 HTTP 请求；Vaadin 单页应用在 Servlet 容器里跑，需要这层包装
        VaadinServletRequest request = (VaadinServletRequest) VaadinServletRequest.getCurrent();
        if (request == null) {
            loginError.setText("Unable to submit sign-in request. Please refresh the page and try again.");
            loginError.setVisible(true);
            return;
        }

        try {
            // 调用容器 login：用户名 = 规范化手机号（与 User.phone、HcbsUserDetailsService 一致）
            // 成功 → Spring Security 建立 Authentication 并写入 HttpSession
            // 失败 → 抛 ServletException（密码错或用户不存在，不区分原因）
            request.getHttpServletRequest().login(PhoneNumbers.normalize(user.trim()), pass);

            // 登录成功后立刻换 sessionId，降低「会话固定」风险（旧 sessionId 作废）
            request.getHttpServletRequest().changeSessionId();

            // 客户端路由跳转（不整页刷新）
            getUI().ifPresent(ui -> {
                // pendingRedirect 在 beforeEnter 里从 ?redirect= 解析
                String redirect = resolveRedirectTarget();
                if (redirect != null && !redirect.isBlank()) {
                    // Vaadin navigate 不要前导 /，如 /booking → booking
                    ui.navigate(redirect.startsWith("/") ? redirect.substring(1) : redirect);
                } else {
                    // 无 redirect 时去根路由 "" → FilmRecommendView
                    ui.navigate("");
                }
            });
        } catch (ServletException ex) {
            // HcbsUserDetailsService 验密失败或用户不存在时容器抛出
            loginError.setText("Incorrect phone number or password");
            loginError.setVisible(true);
        }
    }

    // region Test — 演示账号（由 HcbsTestDataSeeder 生成种子数据，用于答辩，非生产环境登录流程）

    /**
     * 测试用：组装登录页底部的「演示账号」完整 UI 组件。
     * <p>
     * 该组件不参与实际登录逻辑，仅用于帮助答辩时快速查看各角色的演示账号和密码。
     */
    private Div buildTestAccountSection() {
        // 外层容器，CSS 类 test-account-section 便于整体隐藏或单独设置样式
        Div section = new Div();
        section.addClassName("test-account-section");

        // 固定提示信息：所有种子账号使用同一密码（DemoAccountCatalog.DEMO_PASSWORD = "demo"）
        Paragraph passwordNote = new Paragraph(
                "Demo account password: " + DemoAccountCatalog.DEMO_PASSWORD);
        passwordNote.addClassName("auth-hint");
        passwordNote.addClassName("auth-password-note");
        passwordNote.addClassName("test-account-password-note");

        // 密码提示 + 按角色分组的手机号列表
        section.add(passwordNote, buildTestAccountPanel());
        return section;
    }

    /**
     * 测试用：从 {@link DemoAccountCatalog} 读取 9 个种子账号，按角色分组并渲染为列表。
     * <p>
     * 数据在空数据库启动时由 {@link com.hcbs.config.HcbsTestDataSeeder} 写入；
     * 登录时用户需要手动将列表中的手机号填入上方的登录表单。
     */
    private Div buildTestAccountPanel() {
        Div panel = new Div();
        panel.addClassName("demo-accounts-panel");
        panel.addClassName("test-accounts-panel");

        // 说明段落：告知演示者每类角色有 3 个账号，密码为 demo
        Paragraph intro = new Paragraph(
                "After database initialization, sign in with the demo phone numbers below (3 per role, password: demo):");
        intro.addClassName("auth-hint");
        intro.addClassName("test-account-intro");
        panel.add(intro);

        // 将 DemoAccountCatalog.all() 返回的 9 条记录按 roleLabel 分组
        // 使用 LinkedHashMap 保证分组顺序：Customer → Booking staff → Administrator
        Map<String, List<DemoAccountCatalog.DemoAccount>> byRoleLabel = DemoAccountCatalog.all().stream()
                .collect(Collectors.groupingBy(
                        DemoAccountCatalog.DemoAccount::roleLabel,
                        LinkedHashMap::new,
                        Collectors.toList()));

        // 每个角色为一组：包含 H3 标题 + 无序列表（仅展示手机号，不自动填充表单）
        for (Map.Entry<String, List<DemoAccountCatalog.DemoAccount>> entry : byRoleLabel.entrySet()) {
            H3 heading = new H3(entry.getKey()); // 例如 "Customer"、"Booking staff"
            heading.addClassName("demo-accounts-heading");
            heading.addClassName("test-accounts-heading");

            UnorderedList list = new UnorderedList();
            list.addClassName("demo-accounts-list");
            list.addClassName("test-accounts-list");

            for (DemoAccountCatalog.DemoAccount account : entry.getValue()) {
                // 每条记录仅显示手机号；对应的 UserRole 将决定登录后侧边栏的菜单（MainLayout.refreshDrawer）
                ListItem item = new ListItem(account.phone());
                list.add(item);
            }
            panel.add(heading, list);
        }

        return panel;
    }

    // endregion Test

    /** 暂存 URL 中的 redirect 目标；由 beforeEnter 写入，submitLogin 成功后读取 */
    private String pendingRedirect;

    /** 供 submitLogin 读取 pendingRedirect，避免直接访问私有字段 */
    private String resolveRedirectTarget() {
        return pendingRedirect;
    }

    /**
     * 路由进入前的回调方法：解析查询参数，不渲染新 UI（Notification 除外）。
     * <p>
     * 执行时机在构造函数之后、页面展示之前。
     */
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // ?redirect=/booking — 受保护页被 Security 拦下时带回；登录成功后 submitLogin 会 navigate 回去
        pendingRedirect = event.getLocation()
                .getQueryParameters()
                .getParameters()
                .getOrDefault("redirect", List.of())
                .stream()
                .findFirst()
                .orElse(null);

        // ?error — 例如表单登录失败后 Security 重定向到 login?error，显示默认错误文案
        if (event.getLocation()
                .getQueryParameters()
                .getParameters()
                .containsKey("error")) {
            loginError.setVisible(true);
        }

        // ?registered — RegisterView 注册成功后跳转过来，弹一条成功通知
        if (event.getLocation()
                .getQueryParameters()
                .getParameters()
                .containsKey("registered")) {
            Notification.show("Registration successful. Please sign in with your phone number and password.");
        }
    }
}
