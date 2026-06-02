/**
 * 认证板块 — 登录、注册、账户（无 MainLayout 侧栏）。
 *
 * <ul>
 *   <li>{@link com.hcbs.web.auth.LoginView} — {@code /login}，Spring Security 表单登录</li>
 *   <li>{@link com.hcbs.web.auth.RegisterView} — {@code /register}，客户注册</li>
 *   <li>{@link com.hcbs.web.auth.AccountCenterView} — {@code /account}，账户入口</li>
 * </ul>
 *
 * 交互（Login）：点 Sign in → {@code HttpServletRequest.login()} → 跳转 redirect 或首页。
 * BookingView 未登录时 {@code forwardTo(LoginView)} 是跨板块联调示例。
 */
package com.hcbs.web.auth;
