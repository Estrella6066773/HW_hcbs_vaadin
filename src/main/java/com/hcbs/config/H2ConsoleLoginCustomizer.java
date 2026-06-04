package com.hcbs.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Pre-fills H2 Console login with this project's database URL (not H2's generic {@code ~/test}).
 */
@Configuration
@ConditionalOnProperty(name = "spring.h2.console.enabled", havingValue = "true")
public class H2ConsoleLoginCustomizer {

    public static final String LOGIN_JDBC_URL = "jdbc:h2:file:./data/hcbs";

    @Bean
    static BeanPostProcessor h2ConsoleInitParameterCustomizer() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                if ("h2Console".equals(beanName) && bean instanceof ServletRegistrationBean<?> registration) {
                    registration.addInitParameter("url", LOGIN_JDBC_URL);
                    registration.addInitParameter("user", "sa");
                }
                return bean;
            }
        };
    }
}
