package com.hcbs.web;

import com.vaadin.flow.theme.Theme;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UiThemeTest {

    @Test
    void applicationUsesCustomCinemaTheme() {
        Theme theme = AppShell.class.getAnnotation(Theme.class);

        assertThat(theme).isNotNull();
        assertThat(theme.value()).isEqualTo("hcbs");
    }
}
