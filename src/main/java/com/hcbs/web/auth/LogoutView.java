package com.hcbs.web.auth;

import com.hcbs.security.AuthUiService;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

/**
 * 显式登出路由（成员 C · 账户模块）。
 * <p>
 * 访问 {@code /logout} 时在进入页面前调用 {@link AuthUiService#signOut()}，
 * 供书签或外链使用；顶栏 Sign out 按钮也可直接调用同一服务而不经过本页。
 * <p>
 * 本类几乎不包含 UI：在 {@link #beforeEnter} 内登出后由 AuthenticationContext 导航离开。
 */
@Route("logout")
@PageTitle("Sign out")
@AnonymousAllowed // 已登录用户才能有效登出，但路由本身不拦匿名
public class LogoutView extends Div implements BeforeEnterObserver {

    private final AuthUiService authUiService;

    public LogoutView(AuthUiService authUiService) {
        this.authUiService = authUiService;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        // 清除 SecurityContext + Vaadin 会话，并回到公开页（与 MainLayout 顶栏 Sign out 相同）
        authUiService.signOut();
    }
}
