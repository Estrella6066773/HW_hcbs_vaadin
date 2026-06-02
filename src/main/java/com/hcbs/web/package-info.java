/**
 * Vaadin Web 层，按侧栏菜单板块分包。
 *
 * <pre>
 * shell/        全局壳层（导航栏 MainLayout、主题 AppShell）
 * home/         Home — 浏览影片与场次
 * booking/      Book tickets — 订票
 * cancellation/ Cancellation — 取消 / 我的订单
 * admin/        Data admin — 数据管理
 * auth/         登录 / 注册 / 账户（无侧栏）
 * component/    跨板块共享组件（PageHero）
 * </pre>
 *
 * 用户路径：home → film 详情 → booking → cancellation
 */
package com.hcbs.web;
