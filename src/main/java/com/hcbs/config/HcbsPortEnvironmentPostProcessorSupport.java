package com.hcbs.config;

import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

final class HcbsPortEnvironmentPostProcessorSupport {

    private static final String PROPERTY_SOURCE = "hcbsPortSelection";

    private HcbsPortEnvironmentPostProcessorSupport() {
    }

    static void publish(ConfigurableEnvironment environment, int resolved) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("server.port", resolved);
        if (resolved == HcbsPortAllocator.DEFAULT_PORT) {
            properties.put("hcbs.server.port.strategy", "default");
        } else {
            properties.put("hcbs.server.port.strategy", "fallback");
            properties.put("hcbs.server.port.requested", HcbsPortAllocator.DEFAULT_PORT);
            if (resolved >= HcbsPortAllocator.SEQUENTIAL_FALLBACK_FIRST
                    && resolved <= HcbsPortAllocator.SEQUENTIAL_FALLBACK_LAST) {
                properties.put("hcbs.server.port.fallback", "sequential");
            } else {
                properties.put("hcbs.server.port.fallback", "hash");
            }
        }
        environment.getPropertySources().addFirst(new MapPropertySource(PROPERTY_SOURCE, properties));
    }
}
