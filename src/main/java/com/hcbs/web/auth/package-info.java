/**
 * 认证板块（成员 C · 模块 3）— 登录、注册、账户。
 * <p>
 * 全屏页（无 {@link com.hcbs.web.shell.MainLayout} 侧栏）：
 * <ul>
 *   <li>{@link com.hcbs.web.auth.LoginView} — {@code /login}，{@code HttpServletRequest.login}</li>
 *   <li>{@link com.hcbs.web.auth.RegisterView} — {@code /register}，仅客户注册</li>
 *   <li>{@link com.hcbs.web.auth.LogoutView} — {@code /logout}，显式登出</li>
 * </ul>
 * 带主布局：{@link com.hcbs.web.auth.AccountCenterView} — {@code /account}。
 * 业务与安全：{@link com.hcbs.service.auth.RegistrationService}、{@link com.hcbs.config.SecurityConfig}、
 * {@link com.hcbs.security.HcbsUserDetailsService}、{@link com.hcbs.security.CurrentUserService}。
 */
package com.hcbs.web.auth;
