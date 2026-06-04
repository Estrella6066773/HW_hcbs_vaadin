package com.hcbs.config;

import com.hcbs.web.auth.LoginView;
import com.vaadin.flow.spring.security.VaadinWebSecurity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Spring Security 与 Vaadin 集成（成员 C · 安全模块）。
 * <p>
 * 继承 {@link VaadinWebSecurity}：未标注 {@code @AnonymousAllowed} 的视图需登录；
 * 登录页指向 {@link com.hcbs.web.auth.LoginView}，拒绝访问时重定向到 {@code /} 再由 Vaadin 转发登录。
 * 静态图片 {@code /images/**} 允许匿名访问。
 */
@EnableWebSecurity
@Configuration
public class SecurityConfig extends VaadinWebSecurity {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/images/**").permitAll());
        // 仅受保护视图（订票、员工工具等）要求登录；公开首页等带 @AnonymousAllowed 的页面可匿名访问
        setLoginView(http, LoginView.class, "/");
        super.configure(http);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
