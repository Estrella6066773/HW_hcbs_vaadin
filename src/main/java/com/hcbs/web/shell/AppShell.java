package com.hcbs.web.shell;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.AppShellSettings;
import com.vaadin.flow.theme.Theme;

import java.util.Locale;

/**
 * 应用级壳层配置（全站生效，不是某个具体页面）。
 * <p>
 * Vaadin 启动时会自动发现实现了 {@link AppShellConfigurator} 的类。
 * 此处通过 {@link Theme} 把全站样式绑定到 {@code frontend/themes/hcbs/styles.css}。
 * <p>
 * 与 {@link MainLayout} 的分工：
 * <ul>
 *   <li>AppShell — 主题 CSS、全局外观（颜色、字体等）</li>
 *   <li>MainLayout — 顶栏 + 侧栏导航壳层</li>
 *   <li>各业务 View — 右侧主内容区展示什么</li>
 * </ul>
 */
@Theme("hcbs") // 主题名 = 文件夹 frontend/themes/hcbs/，样式在 styles.css
public class AppShell implements AppShellConfigurator {

    @Override
    public void configurePage(AppShellSettings settings) {
        settings.setLocale(Locale.ENGLISH);
    }
}
