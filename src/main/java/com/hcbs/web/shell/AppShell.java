package com.hcbs.web.shell;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.Theme;

/**
 * 应用级壳层配置（全站生效，不是某个具体页面）。
 * <p>
 * Vaadin 启动时会自动发现实现了 {@link AppShellConfigurator} 的类（全项目只能有一个）。
 * 此处通过 {@link Theme} 把全站样式绑定到 {@code frontend/themes/hcbs/styles.css}。
 * <p>
 * 与 {@link MainLayout} 的分工：
 * <ul>
 *   <li>AppShell — 主题 CSS、HTML 引导页（index.html 模板）、PWA 等全站级设置</li>
 *   <li>MainLayout — 顶栏 + 侧栏导航壳层（登录后应用内框架）</li>
 *   <li>各业务 View — 仅负责右侧主内容区的业务 UI</li>
 * </ul>
 * <p>
 * <b>注意</b>：不要在此类使用已废弃或错误的 {@code AppShellSettings#setLocale}；
 * 区域设置见 {@link com.hcbs.config.VaadinLocaleConfiguration} 与
 * {@link com.hcbs.util.EnglishWeekdays}；HTML {@code lang} 见 {@code frontend/index.html}。
 */
// 主题文件夹名 = frontend/themes/hcbs/，入口样式为 styles.css
@Theme("hcbs")
public class AppShell implements AppShellConfigurator {
    // 无 configurePage 覆盖：当前仅需 @Theme，避免误用不存在的 page.AppShellSettings API
}
