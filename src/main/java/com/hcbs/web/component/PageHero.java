package com.hcbs.web.component;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;

/**
 * 页面顶部标题区组件（成员 C · 导航/UI 组件）。
 * <p>
 * 供登录、注册、取消柜台等页面复用：包含眉题（eyebrow）+ 主标题 + 说明文案。
 * 样式类 {@code page-hero} 定义在 {@code frontend/themes/hcbs/styles.css}。
 * <p>
 * 与 {@link com.hcbs.web.shell.MainLayout} 无关：可在无 layout 的全屏页或 layout 内的内容区使用。
 */
public class PageHero extends Div {

    /**
     * 创建页面标题区组件。
     * @param eyebrow 小标签，如 "Sign in"、"Cancellation"
     * @param heading 主标题（H2）
     * @param copy    副说明段落
     */
    public PageHero(String eyebrow, String heading, String copy) {
        Span badge = new Span(eyebrow);
        badge.addClassName("eyebrow");

        H2 title = new H2(heading);
        title.addClassName("page-hero-heading");

        Paragraph description = new Paragraph(copy);
        description.addClassName("page-hero-copy");

        add(badge, title, description);
        addClassName("page-hero");
        setWidthFull();
        // 避免在 flex 父布局中被拉伸占满剩余高度
        getElement().getStyle().set("flex-grow", "0").set("flex-shrink", "0");
    }
}
