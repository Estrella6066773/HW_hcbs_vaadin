/**
 * Home 板块 — 浏览影片、筛选场次。
 *
 * <ul>
 *   <li>{@link com.hcbs.web.home.FilmRecommendView} — {@code /} 海报墙 + 筛选</li>
 *   <li>{@link com.hcbs.web.home.FilmDetailView} — {@code /film/:id} 详情 + 场次表</li>
 *   <li>{@link com.hcbs.web.home.ShowingFilterQuery} — URL 查询参数与筛选条件互转</li>
 * </ul>
 *
 * 交互：筛选 → {@code navigate(FilmRecommendView, queryParams)} → 点击海报/轮播 → {@code FilmDetailView}
 * → 点击 Book → {@code navigate(BookingView, showingId)}。
 *
 * 依赖 Service：{@code HcbsSearchService}、{@code PosterResourceService}。
 */
package com.hcbs.web.home;
