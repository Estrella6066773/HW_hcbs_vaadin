package com.hcbs.config;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import org.springframework.context.annotation.Configuration;

import java.util.Locale;

/**
 * 每个 UI 会话使用英文区域，与 {@link com.hcbs.util.EnglishWeekdays} 一致。
 */
@Configuration
public class VaadinLocaleConfiguration implements VaadinServiceInitListener {

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.getSource().addUIInitListener(uiEvent -> uiEvent.getUI().setLocale(Locale.ENGLISH));
    }
}
