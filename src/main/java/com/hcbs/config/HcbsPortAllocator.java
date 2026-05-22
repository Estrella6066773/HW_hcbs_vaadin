package com.hcbs.config;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Picks a free TCP port before the embedded web server starts.
 */
public final class HcbsPortAllocator {

    public static final int DEFAULT_PORT = 8080;
    public static final int SEQUENTIAL_FALLBACK_FIRST = 8081;
    public static final int SEQUENTIAL_FALLBACK_LAST = 8180;
    public static final int HASH_PORT_BASE = 8181;
    public static final int HASH_PORT_SPAN = 100;
    public static final int MAX_HASH_ATTEMPTS = 20;

    private static final String HASH_SEED = "hcbs-vaadin";

    private HcbsPortAllocator() {
    }

    /**
     * Returns the first port that passes a live bind test: 8080, then 8081…8180, then hash range.
     */
    public static int resolvePort() {
        if (isPortAvailable(DEFAULT_PORT)) {
            return DEFAULT_PORT;
        }
        for (int port = SEQUENTIAL_FALLBACK_FIRST; port <= SEQUENTIAL_FALLBACK_LAST; port++) {
            if (isPortAvailable(port)) {
                return port;
            }
        }
        for (int attempt = 0; attempt < MAX_HASH_ATTEMPTS; attempt++) {
            int candidate = hashPort(attempt);
            if (isPortAvailable(candidate)) {
                return candidate;
            }
        }
        System.err.println(
                "[HCBS] No free HTTP port. Tried " + DEFAULT_PORT + ", "
                        + SEQUENTIAL_FALLBACK_FIRST + "-" + SEQUENTIAL_FALLBACK_LAST
                        + ", and " + MAX_HASH_ATTEMPTS + " hash ports ("
                        + HASH_PORT_BASE + "-" + (HASH_PORT_BASE + HASH_PORT_SPAN - 1) + ").");
        System.err.println("[HCBS] Stop the process on 8080, or run: mvn spring-boot:run -Dserver.port=9090");
        System.exit(1);
        return -1;
    }

    public static void logPortSelection(int port) {
        if (port == DEFAULT_PORT) {
            System.out.println("[HCBS] HTTP port " + port + " is available — starting on default port.");
        } else {
            System.out.println("[HCBS] Port " + DEFAULT_PORT + " is in use — starting on fallback port " + port + ".");
        }
    }

    public static int hashPort(int attempt) {
        String seed = HASH_SEED + ":" + System.getProperty("user.dir", "") + ":" + attempt;
        int hash = seed.hashCode();
        return HASH_PORT_BASE + Math.floorMod(hash, HASH_PORT_SPAN);
    }

    public static boolean isPortExplicitlyConfigured() {
        if (System.getProperty("server.port") != null) {
            return true;
        }
        String envPort = System.getenv("SERVER_PORT");
        return envPort != null && !envPort.isBlank();
    }

    /**
     * Tries to bind a temporary socket — the same check the OS uses when Tomcat starts.
     */
    public static boolean isPortAvailable(int port) {
        if (port < 1 || port > 65535) {
            return false;
        }
        try (ServerSocket socket = new ServerSocket(port)) {
            socket.setReuseAddress(true);
            return true;
        } catch (IOException ex) {
            return false;
        }
    }
}
