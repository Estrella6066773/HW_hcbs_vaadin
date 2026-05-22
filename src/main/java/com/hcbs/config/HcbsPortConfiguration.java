package com.hcbs.config;

import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * Binds the HTTP port only after a live port check (see {@link HcbsPortAllocator}).
 */
@Configuration
public class HcbsPortConfiguration {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> hcbsTomcatPortCustomizer(
            ConfigurableEnvironment environment) {
        return factory -> {
            if (HcbsPortAllocator.isPortExplicitlyConfigured()) {
                return;
            }
            int port = HcbsPortAllocator.resolvePort();
            factory.setPort(port);
            HcbsPortEnvironmentPostProcessor.publishPortMetadata(environment, port);
            HcbsPortAllocator.logPortSelection(port);
        };
    }
}
