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
 * Spring Security 与 Vaadin 集成配置（成员 C · 安全模块）。
 * <p>
 * 继承 {@link VaadinWebSecurity}：未标注 {@code @AnonymousAllowed} 的视图需要登录；
 * 登录页指向 {@link LoginView}；拒绝访问时先跳转到 {@code /} 再由 Vaadin 转发到登录页。
 * 静态图片 {@code /images/**} 允许匿名访问（用于影片海报等资源）。
 * <p>
 * 与成员 B、D 的边界：本类不定义业务角色规则，角色由 View 上的 {@code @RolesAllowed} 与
 * {@link com.hcbs.security.HcbsUserDetailsService} 注入的 {@code ROLE_*} 配合使用。
 */
@EnableWebSecurity
@Configuration
public class SecurityConfig extends VaadinWebSecurity {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/images/**").permitAll()); // 海报等静态资源，无需会话
        // loginView=LoginView；defaultSuccessUrl="/" 供未带 redirect 参数时的默认跳转
        // super.configure 启用 Vaadin 对 @Route、@AnonymousAllowed 和 @RolesAllowed 注解的检查
        setLoginView(http, LoginView.class, "/");
        super.configure(http);
    }

    /** 用于注册和种子数据写库时使用；与 HcbsUserDetailsService 返回的 passwordHash 匹配 */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
