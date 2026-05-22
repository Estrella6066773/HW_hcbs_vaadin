package com.hcbs.config;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Resolves the HTTP port: prefers 8080, then up to 20 hash-derived candidates; exits if none are free.
 */
public final class HcbsPortAllocator {

    public static final int DEFAULT_PORT = 8080;
    public static final int HASH_PORT_BASE = 8081;
    public static final int HASH_PORT_SPAN = 200;
    public static final int MAX_HASH_ATTEMPTS = 20;

    private static final String HASH_SEED = "hcbs-vaadin";

    private HcbsPortAllocator() {
    }

    public static int resolvePort() {
        if (isPortAvailable(DEFAULT_PORT)) {
            return DEFAULT_PORT;
        }
        for (int attempt = 0; attempt < MAX_HASH_ATTEMPTS; attempt++) {
            int candidate = hashPort(attempt);
            if (isPortAvailable(candidate)) {
                return candidate;
            }
        }
        System.err.println(
                "HCBS: Port " + DEFAULT_PORT + " is in use. Tried " + MAX_HASH_ATTEMPTS
                        + " hash-based ports in range " + HASH_PORT_BASE + "-"
                        + (HASH_PORT_BASE + HASH_PORT_SPAN - 1) + "; none available. Exiting.");
        System.exit(1);
        return -1;
    }

    /**
     * Deterministic port from seed, working directory, and attempt index (0 .. {@link #MAX_HASH_ATTEMPTS} - 1).
     */
    public static int hashPort(int attempt) {
        String seed = HASH_SEED + ":" + System.getProperty("user.dir", "") + ":" + attempt;
        int hash = seed.hashCode();
        return HASH_PORT_BASE + Math.floorMod(hash, HASH_PORT_SPAN);
    }

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
