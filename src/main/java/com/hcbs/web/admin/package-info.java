/**
 * Data admin 板块 — 员工管理影片场次与用户账户。
 *
 * <ul>
 *   <li>{@link com.hcbs.web.admin.AdminDataView} — {@code /admin}（仅 ADMIN 角色）</li>
 * </ul>
 *
 * 交互：选影院/日期 → 周排片表 → 增删改场次 Dialog；用户 Grid → Enable/Disable。
 *
 * 依赖 Service：{@code AdminCatalogService}。
 */
package com.hcbs.web.admin;
