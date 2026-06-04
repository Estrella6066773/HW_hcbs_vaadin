package com.hcbs.config;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import org.springframework.context.annotation.Configuration;

import java.util.Locale;

/**
 * Vaadin 全站英文区域设置。
 * <p>
 * 在浏览器打开任意页面时，Vaadin 会为该标签页创建一个 {@link com.vaadin.flow.component.UI} 实例；
 * 此处于 UI 初始化时调用 {@code setLocale(Locale.ENGLISH)}，使组件默认格式化（数字、日期等）
 * 与 {@link com.hcbs.util.EnglishWeekdays} 中 DatePicker 的星期缩写一致。
 * <p>
 * 与 Spring {@link java.util.Locale} 或 JVM 默认语言无关；仅影响 Vaadin 前端组件。
 */
@Configuration
public class VaadinLocaleConfiguration implements VaadinServiceInitListener {

    /**
     * Vaadin 服务启动时注册监听器（每个应用进程执行一次）。
     */
    @Override
    public void serviceInit(ServiceInitEvent event) {
        // 每个新 UI（通常对应一个浏览器标签页）创建时设置区域
        event.getSource().addUIInitListener(uiEvent ->
                uiEvent.getUI().setLocale(Locale.ENGLISH));
    }
}
