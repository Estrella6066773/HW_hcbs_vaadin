/**
 * 全局壳层。
 *
 * <ul>
 *   <li>{@link com.hcbs.web.shell.MainLayout} — 顶栏 + 侧栏导航（答辩常问：管理导航栏的文件）</li>
 *   <li>{@link com.hcbs.web.shell.AppShell} — 注册 Vaadin 主题 {@code hcbs}</li>
 * </ul>
 *
 * 交互：{@code afterNavigation} 刷新侧栏；按角色显示不同菜单项（员工见 Book/Cancellation/Admin，客户见 My bookings）。
 */
package com.hcbs.web.shell;
