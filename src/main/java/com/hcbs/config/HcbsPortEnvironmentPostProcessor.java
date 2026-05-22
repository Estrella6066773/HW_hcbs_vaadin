package com.hcbs.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * First pass: probe ports before Spring creates the web server.
 * {@link HcbsPortConfiguration} repeats the probe immediately before Tomcat binds.
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class HcbsPortEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (HcbsPortAllocator.isPortExplicitlyConfigured()) {
            return;
        }

        Integer fixedPort = environment.getProperty("server.port", Integer.class);
        if (fixedPort != null && fixedPort != HcbsPortAllocator.DEFAULT_PORT) {
            // e.g. server.port=9090 in a profile — honour without scanning
            return;
        }

        int resolved = HcbsPortAllocator.resolvePort();
        publishPortMetadata(environment, resolved);
        HcbsPortAllocator.logPortSelection(resolved);
    }

    static void publishPortMetadata(ConfigurableEnvironment environment, int resolved) {
        HcbsPortEnvironmentPostProcessorSupport.publish(environment, resolved);
    }
}
