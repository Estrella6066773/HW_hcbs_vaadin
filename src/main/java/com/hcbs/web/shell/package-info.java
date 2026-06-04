/**
 * 全局壳层与导航（成员 D：登录 / 注册 / 角色 / 导航 + 管理端入口）。
 *
 * <h2>包内类</h2>
 * <ul>
 *   <li>{@link com.hcbs.web.shell.MainLayout} — 顶栏品牌、Account/登出、按角色生成的侧栏
 *       （答辩常问：「管理导航栏的文件」即此类）</li>
 *   <li>{@link com.hcbs.web.shell.AppShell} — Vaadin 应用壳：注册主题 {@code hcbs}</li>
 * </ul>
 *
 * <h2>路由与布局</h2>
 * 带 {@code layout = MainLayout.class} 的 View 渲染在右侧主内容区；
 * {@link com.hcbs.web.auth.LoginView} 等认证页不使用 MainLayout，为全屏页。
 * 导航完成后 {@link com.hcbs.web.shell.MainLayout#afterNavigation} 刷新菜单，
 * 以便登录态变化后侧栏即时更新。
 *
 * <h2>角色与菜单（三类用户）</h2>
 * 未登录仅 Home；客户见 My bookings；订票员见 Book tickets + Cancellation；
 * 管理员额外见 Data admin。详见 {@link com.hcbs.web.shell.MainLayout} 类注释中的表格。
 *
 * <h2>相关配置（包外）</h2>
 * <ul>
 *   <li>{@link com.hcbs.config.VaadinLocaleConfiguration} — UI 会话英文区域</li>
 *   <li>{@link com.hcbs.util.EnglishWeekdays} — DatePicker 与排片表头英文星期</li>
 *   <li>{@code frontend/themes/hcbs/styles.css} — 壳层与导航视觉样式</li>
 * </ul>
 */
package com.hcbs.web.shell;
