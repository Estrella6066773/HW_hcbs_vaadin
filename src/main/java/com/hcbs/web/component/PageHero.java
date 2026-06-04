package com.hcbs.web.component;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;

/**
 * 页面顶部标题区（成员 C · 导航/UI 组件）。
 * <p>
 * 供登录、注册、取消柜台等全屏或主布局页面复用：眉题（eyebrow）+ 主标题 + 说明文案。
 * 样式类 {@code page-hero} 定义在 {@code frontend/themes/hcbs/styles.css}。
 */
public class PageHero extends Div {

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
        getElement().getStyle().set("flex-grow", "0").set("flex-shrink", "0");
    }
}
