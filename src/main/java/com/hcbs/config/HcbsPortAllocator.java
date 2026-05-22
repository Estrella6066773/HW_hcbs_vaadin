package com.hcbs.config;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Resolves the HTTP port: prefers 8080, then a deterministic hash-based port if 8080 is taken.
 */
public final class HcbsPortAllocator {

    public static final int DEFAULT_PORT = 8080;
    public static final int HASH_PORT_BASE = 8081;
    public static final int HASH_PORT_SPAN = 200;

    private static final String HASH_SEED = "hcbs-vaadin";

    private HcbsPortAllocator() {
    }

    public static int resolvePort() {
        if (isPortAvailable(DEFAULT_PORT)) {
            return DEFAULT_PORT;
        }
        int start = hashPort();
        for (int offset = 0; offset < HASH_PORT_SPAN; offset++) {
            int candidate = HASH_PORT_BASE + Math.floorMod(start - HASH_PORT_BASE + offset, HASH_PORT_SPAN);
            if (isPortAvailable(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException(
                "Port " + DEFAULT_PORT + " is in use and no free port found in range "
                        + HASH_PORT_BASE + "-" + (HASH_PORT_BASE + HASH_PORT_SPAN - 1));
    }

    public static int hashPort() {
        String seed = HASH_SEED + ":" + System.getProperty("user.dir", "");
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
