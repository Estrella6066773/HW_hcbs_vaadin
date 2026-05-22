package com.hcbs.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Prints application access URLs when the server is ready.
 * URLs are logged on their own lines so IDEs/terminals support Ctrl+Click.
 */
@Component
public class StartupAccessLogger implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger log = LoggerFactory.getLogger(StartupAccessLogger.class);

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        Environment env = event.getApplicationContext().getEnvironment();
        int port = env.getProperty("server.port", Integer.class, HcbsPortAllocator.DEFAULT_PORT);
        String webUiUrl = "http://localhost:" + port + "/";
        String h2ConsoleUrl = "http://localhost:" + port + "/h2-console";
        String strategy = env.getProperty("hcbs.server.port.strategy", "default");

        log.info("");
        log.info("============================================================");
        log.info("  HCBS is ready");
        log.info("  Web UI (Ctrl+Click):");
        log.info("  {}", webUiUrl);
        log.info("  H2 console (Ctrl+Click):");
        log.info("  {}", h2ConsoleUrl);
        log.info("  H2 JDBC: jdbc:h2:file:./data/hcbs");
        log.info("  H2 user: sa   password: (empty)");
        if ("hash-fallback".equals(strategy)) {
            int requested = env.getProperty("hcbs.server.port.requested", Integer.class, HcbsPortAllocator.DEFAULT_PORT);
            int hashAttempt = env.getProperty("hcbs.server.port.hash-attempt", Integer.class, -1);
            int hashCandidate = env.getProperty("hcbs.server.port.hash-candidate", Integer.class, -1);
            log.info("  Port: {} ({} was in use; hash attempt {}/{}, candidate {})",
                    port, requested, hashAttempt + 1, HcbsPortAllocator.MAX_HASH_ATTEMPTS, hashCandidate);
        } else {
            log.info("  Port: {}", port);
        }
        log.info("============================================================");
        log.info("");
    }
}
