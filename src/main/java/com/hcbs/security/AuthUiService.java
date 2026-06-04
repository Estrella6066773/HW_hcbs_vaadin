package com.hcbs.security;

import com.vaadin.flow.spring.security.AuthenticationContext;
import org.springframework.stereotype.Service;

/**
 * Vaadin 侧认证 UI 动作封装（成员 C · 安全模块）。
 * <p>
 * 集中处理登出，供 {@link com.hcbs.web.shell.MainLayout} 顶栏、
 * {@link com.hcbs.web.auth.LogoutView}、{@link com.hcbs.web.auth.AccountCenterView} 调用。
 */
@Service
public class AuthUiService {

    private final AuthenticationContext authenticationContext;

    public AuthUiService(AuthenticationContext authenticationContext) {
        this.authenticationContext = authenticationContext;
    }

    /** 登出当前用户并回到公开首页 */
    public void signOut() {
        authenticationContext.logout();
    }
}
