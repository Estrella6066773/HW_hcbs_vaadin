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
 * 全局导航壳层：包含顶栏、左侧抽屉菜单和右侧主内容槽位。
 * <p>
 * <b>在 Vaadin 路由中的位置</b>：业务 View 通过 {@code @Route(..., layout = MainLayout.class)}
 * 声明嵌入本布局右侧。例如首页 {@link FilmRecommendView}、订票页 {@link BookingView} 等。
 * 登录页 {@link LoginView}、注册页等不使用本布局，为全屏独立页面。
 * <p>
 * <b>与 {@link AppShell} 的分工</b>：
 * <ul>
 *   <li>{@link AppShell} — 全站主题 CSS（{@code frontend/themes/hcbs/styles.css}）、HTML 壳层</li>
 *   <li>{@link MainLayout} — 应用内导航与顶栏品牌（本类）</li>
 *   <li>各业务 View — 仅负责右侧主内容区的业务 UI</li>
 * </ul>
 * <p>
 * <b>为何实现 {@link AfterNavigationObserver}</b>：
 * Spring 构造本类时只会执行一次构造函数，此时用户可能尚未登录。
 * 登录、登出或切换账号后，侧边栏菜单与顶栏按钮必须随之变化，但布局实例通常不会重建。
 * 因此在每次路由导航结束后调用 {@link #afterNavigation} → {@link #refreshChrome()}，
 * 根据 {@link CurrentUserService} 的当前状态重新绘制可变部分。
 * <p>
 * <b>安全</b>：类上的 {@link AnonymousAllowed} 表示未登录也可加载带此布局的公开路由
 * （如首页浏览影片）。具体页面仍可在各自 View 上使用 {@code @RolesAllowed} 等限制访问；
 * 侧边栏仅做导航展示，不能替代服务端权限校验。
 * <p>
 * <b>样式</b>：继承自 Vaadin {@link AppLayout}（来自依赖，非本项目源码）。
 * 通过 {@code addClassName} 添加的类名（如 {@code topbar}、{@code nav-card}）在
 * {@code frontend/themes/hcbs/styles.css} 中定义。
 *
 * @see #refreshDrawer() 按角色生成侧边栏（答辩常考点）
 * @see com.hcbs.model.UserRole 角色与菜单可见性的对应关系
 */
// 允许未登录用户加载带本 layout 的公开路由（如首页）；各 View 仍可用 @RolesAllowed 限制
@AnonymousAllowed
public class MainLayout extends AppLayout implements AfterNavigationObserver {

    // —— 依赖与可变 UI 容器（仅构造一次，内容在 refresh* 方法中反复重建）——

    /**
     * 读取 Spring Security 会话中的当前用户与登录状态。
     * 所有菜单和顶栏的分支判断均以此为准，切勿在布局内缓存 User 对象，以免与登出状态不同步。
     */
    private final CurrentUserService currentUserService;

    /** 封装登出等需触发服务端会话变更的 UI 操作（避免布局直接依赖 Security API）。 */
    private final AuthUiService authUiService;

    /**
     * 顶栏品牌区副标题（{@code brand-subtitle}）。
     * 初始文案在构造器中设置样式；具体文字在 {@link #resolveSubtitle()} 中按角色更新。
     */
    private final Span subtitle = new Span("Cinema booking portal");

    /**
     * 顶栏右侧操作区容器（{@code header-actions}）。
     * 子组件在 {@link #refreshHeader()} 中调用 {@code removeAll()} 后按登录状态重新添加，
     * 避免重复叠加按钮。
     */
    private final HorizontalLayout headerActions = new HorizontalLayout();

    /**
     * 左侧抽屉内容区（{@code addToDrawer} 挂载点）。
     * 菜单项在 {@link #refreshDrawer()} 中整批重建，确保角色变化后链接集合正确。
     */
    private final VerticalLayout drawerContent = new VerticalLayout();

    /**
     * 搭建固定 UI 骨架：品牌标识、顶栏行、空操作区和抽屉容器。
     * <p>
     * 不在此方法内写死「Book tickets」等链接，以免构造时角色尚未就绪；
     * 可变部分统一交给 {@link #refreshChrome()} 处理（在构造末尾和每次导航后各调用一次）。
     *
     * @param currentUserService 由 Spring 注入，与 HTTP 会话绑定
     * @param authUiService      由 Spring 注入，处理登出等操作
     */
    public MainLayout(CurrentUserService currentUserService, AuthUiService authUiService) {
        this.currentUserService = currentUserService;
        this.authUiService = authUiService;
        addClassName("hcbs-shell");

        // —— 品牌区：HC 标记 + 主标题 + 副标题（副标题文案稍后由 refreshChrome 更新）——
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

        // —— 顶栏右侧：先挂空容器，按钮由 refreshHeader 按登录态填充 ——
        headerActions.addClassName("header-actions");
        headerActions.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        headerActions.setSpacing(true);

        // —— 顶栏整行：[汉堡开关] [品牌] [右侧操作区] ——
        // DrawerToggle 控制 AppLayout 左侧抽屉显隐（小屏常用）
        HorizontalLayout header = new HorizontalLayout(new DrawerToggle(), brand, headerActions);
        header.addClassName("topbar");
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.setWidthFull();
        header.expand(brand); // 品牌区占据中间弹性空间，操作区靠右

        addToNavbar(header); // AppLayout 顶栏槽位，右侧 outlet 仍由各 @Route View 填充

        // —— 侧栏：空 VerticalLayout 挂入抽屉，菜单由 refreshDrawer 填充 ——
        drawerContent.setPadding(false);
        drawerContent.setSpacing(false);
        drawerContent.setWidthFull();
        addToDrawer(drawerContent);

        // 构造结束时用户可能尚未登录，必须按当前会话刷一次菜单/顶栏
        refreshChrome();
    }

    /**
     * Vaadin 路由完成后的回调（包括首次进入带 layout 的页面、登录后跳转等）。
     * <p>
     * 仅刷新壳层 chrome，不干预右侧 {@code RouterOutlet} 中的业务 View；
     * 业务页面自己的 {@code BeforeEnterObserver} 仍负责页面级权限控制。
     */
    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        // 登录、登出或跳转后 layout 实例不重建，在此同步侧边栏与顶栏
        refreshChrome();
    }

    /**
     * 统一刷新顶栏按钮、侧边栏链接和品牌副标题。
     * 三个子步骤相互独立，执行顺序无严格要求。
     */
    private void refreshChrome() {
        refreshHeader();
        refreshDrawer();
        subtitle.setText(resolveSubtitle()); // 顶栏副标题随角色或登录状态变化
    }

    /**
     * 按登录状态重建顶栏右侧控件。
     * <ul>
     *   <li>未登录 — 仅「Account」按钮（点击进入 {@link LoginView}）</li>
     *   <li>已登录 — 姓名徽章 + Account（进入 {@link AccountCenterView}）+ Sign out 按钮</li>
     * </ul>
     */
    private void refreshHeader() {
        headerActions.removeAll(); // 防止多次 refresh 叠加重复按钮
        Button account = new Button("Account", event -> openAccountCenter());
        account.addClassName("primary-action");
        account.addClassName("account-button");

        if (currentUserService.isAuthenticated()) {
            User user = currentUserService.requireCurrentUser();
            Span userBadge = new Span(user.getFullName()); // 顶栏展示用户全名，而非手机号
            userBadge.addClassName("user-badge");

            // signOut 通过 AuthUiService 处理，内部会使会话失效并导航到登录页
            Button signOut = new Button("Sign out", event -> authUiService.signOut());
            signOut.addClassName("secondary-action");
            signOut.addClassName("sign-out-button");

            headerActions.add(userBadge, account, signOut);
        } else {
            // 访客：Account 按钮点击后进入 LoginView（详见 openAccountCenter）
            headerActions.add(account);
        }
    }

    /**
     * 按角色重建侧边栏导航（功能分工答辩重点：账户与导航由 shell 统一管控）。
     * <p>
     * 规则摘要（与案例三类用户一致，无单独「经理」角色）：
     * <table>
     *   <caption>侧边栏可见项</caption>
     *   <tr><th>状态 / 角色</th><th>菜单项</th></tr>
     *   <tr><td>未登录</td><td>Home</td></tr>
     *   <tr><td>{@link com.hcbs.model.UserRole#CUSTOMER}</td><td>Home、My bookings</td></tr>
     *   <tr><td>{@link com.hcbs.model.UserRole#BOOKING_STAFF}</td><td>Home、Book tickets、Cancellation</td></tr>
     *   <tr><td>{@link com.hcbs.model.UserRole#ADMIN}</td><td>同上 + Data admin</td></tr>
     * </table>
     * <p>
     * 「Home」对所有人可见，便于未登录用户浏览排片；员工菜单依赖
     * {@link com.hcbs.model.UserRole#isEmployee()}；管理员工具依赖
     * {@link com.hcbs.model.UserRole#canAccessAdminTools()}。
     */
    private void refreshDrawer() {
        drawerContent.removeAll(); // 角色变化时整批替换，避免旧链接残留

        Span drawerLabel = new Span("Menu");
        drawerLabel.addClassName("drawer-label");
        drawerContent.add(drawerLabel);

        // 公开入口：影片推荐 / 排片浏览
        drawerContent.add(navLink("Home", FilmRecommendView.class, "Browse films and find showtimes"));

        // BOOKING_STAFF / ADMIN：柜台代客订票（CUSTOMER 在首页自助选座，不出现此入口）
        if (currentUserService.isAuthenticated() && currentUserService.requireCurrentUser().getRole().isEmployee()) {
            drawerContent.add(navLink("Book tickets", BookingView.class, "Desk booking for any showing"));
        }

        if (currentUserService.isAuthenticated()) {
            User user = currentUserService.requireCurrentUser();
            if (user.getRole().isCustomer()) {
                // CUSTOMER：仅自己的订单列表与取消
                drawerContent.add(navLink("My bookings", MyBookingsView.class, "View and cancel your orders"));
            } else {
                // BOOKING_STAFF：全站退票柜台
                drawerContent.add(navLink("Cancellation", CancellationView.class, "Refund desk for any booking"));
                // ADMIN：影片、账号和排片数据维护（canAccessAdminTools 为 true）
                if (user.getRole().canAccessAdminTools()) {
                    drawerContent.add(navLink("Data admin", AdminDataView.class, "Manage films and accounts"));
                }
            }
        }
        // 未登录：仅上方 Home 项，无 My bookings / Book tickets 等选项
    }

    /**
     * 顶栏副标题：向用户暗示当前门户模式（访客 / 客户自助 / 员工工作台）。
     */
    private String resolveSubtitle() {
        return currentUserService.findCurrentUser()
                .map(user -> user.getRole().isCustomer()
                        ? "Customer self-service"   // CUSTOMER
                        : "Employee control desk")  // BOOKING_STAFF / ADMIN
                .orElse("Cinema booking portal");     // 未登录访客
    }

    /**
     * Account 按钮路由逻辑：已登录进入账户中心，未登录进入登录页（登录成功后可再回到账户中心）。
     */
    private void openAccountCenter() {
        getUI().ifPresent(ui -> {
            if (currentUserService.isAuthenticated()) {
                ui.navigate(AccountCenterView.class); // 已登录：资料与改密
            } else {
                ui.navigate(LoginView.class);         // 未登录：先登录再进账户中心
            }
        });
    }

    /**
     * 构造侧边栏导航卡片（包含标题、说明和 {@link RouterLink}）。
     * <p>
     * {@link RouterLink#setRoute(Class)} 绑定到目标 View 的 {@code @Route} 路径；
     * 点击后 Vaadin 切换右侧 outlet 中的组件，URL 同步更新，无需手动编写 href。
     * 样式类 {@code nav-card}、{@code nav-title} 和 {@code nav-caption} 在主题 CSS 中定义。
     *
     * @param title   菜单主标题（英文，与界面语言一致）
     * @param route   目标 View 类，须带有 {@code @Route} 且通常设置 {@code layout = MainLayout.class}
     * @param caption 菜单辅助说明，帮助答辩演示时口述各入口的用途
     */
    private RouterLink navLink(String title, Class<? extends Component> route, String caption) {
        RouterLink link = new RouterLink();
        link.setRoute(route); // 读取目标 View 的 @Route 路径，客户端路由无整页刷新
        link.addClassName("nav-card");

        Span name = new Span(title);
        name.addClassName("nav-title");
        Span detail = new Span(caption);
        detail.addClassName("nav-caption");
        link.add(name, detail);
        return link;
    }
}
