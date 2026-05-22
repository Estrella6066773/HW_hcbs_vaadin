package com.hcbs.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

/**
 * Picks {@value HcbsPortAllocator#DEFAULT_PORT} when free; otherwise a hash-based fallback port.
 * Skips when {@code server.port} is set on the command line or via {@code SERVER_PORT}.
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class HcbsPortEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String PROPERTY_SOURCE = "hcbsPortSelection";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (isPortExplicitlyConfigured()) {
            return;
        }

        int requested = environment.getProperty("server.port", Integer.class, HcbsPortAllocator.DEFAULT_PORT);
        if (requested != HcbsPortAllocator.DEFAULT_PORT) {
            return;
        }

        int resolved = HcbsPortAllocator.resolvePort();
        Map<String, Object> properties = new HashMap<>();
        properties.put("server.port", resolved);
        if (resolved == HcbsPortAllocator.DEFAULT_PORT) {
            properties.put("hcbs.server.port.strategy", "default");
        } else {
            properties.put("hcbs.server.port.strategy", "hash-fallback");
            properties.put("hcbs.server.port.requested", HcbsPortAllocator.DEFAULT_PORT);
            properties.put("hcbs.server.port.hash-candidate", HcbsPortAllocator.hashPort());
        }
        environment.getPropertySources().addFirst(new MapPropertySource(PROPERTY_SOURCE, properties));
    }

    private boolean isPortExplicitlyConfigured() {
        if (System.getProperty("server.port") != null) {
            return true;
        }
        String envPort = System.getenv("SERVER_PORT");
        return envPort != null && !envPort.isBlank();
    }
}
