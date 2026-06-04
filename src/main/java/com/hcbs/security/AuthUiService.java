package com.hcbs.security;

import com.vaadin.flow.spring.security.AuthenticationContext;
import org.springframework.stereotype.Service;

/**
 * Vaadin 侧认证 UI 动作封装服务（成员 C · 安全模块）。
 * <p>
 * 集中处理登出操作，避免各 View 直接依赖 Spring Security API。
 * 调用方包括：{@link com.hcbs.web.shell.MainLayout} 顶栏、
 * {@link com.hcbs.web.auth.LogoutView}、{@link com.hcbs.web.auth.AccountCenterView}。
 */
@Service
public class AuthUiService {

    private final AuthenticationContext authenticationContext;

    public AuthUiService(AuthenticationContext authenticationContext) {
        this.authenticationContext = authenticationContext;
    }

    /**
     * 登出当前 Vaadin UI 关联的会话，并导航到配置的登出成功页面（通常为公开首页）。
     */
    public void signOut() {
        authenticationContext.logout();
    }
}
