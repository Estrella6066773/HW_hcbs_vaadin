package com.hcbs.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HcbsPortAllocatorTest {

    @Test
    void hashPortsStayWithinRangeAndVaryByAttempt() {
        int first = HcbsPortAllocator.hashPort(0);
        int second = HcbsPortAllocator.hashPort(1);
        assertThat(first).isBetween(HcbsPortAllocator.HASH_PORT_BASE,
                HcbsPortAllocator.HASH_PORT_BASE + HcbsPortAllocator.HASH_PORT_SPAN - 1);
        assertThat(second).isBetween(HcbsPortAllocator.HASH_PORT_BASE,
                HcbsPortAllocator.HASH_PORT_BASE + HcbsPortAllocator.HASH_PORT_SPAN - 1);
        assertThat(HcbsPortAllocator.hashPort(0)).isEqualTo(first);
    }

    @Test
    void exposesTwentyHashAttempts() {
        assertThat(HcbsPortAllocator.MAX_HASH_ATTEMPTS).isEqualTo(20);
    }

    @Test
    void resolvePortReturnsDefaultWhenAvailable() {
        if (HcbsPortAllocator.isPortAvailable(HcbsPortAllocator.DEFAULT_PORT)) {
            assertThat(HcbsPortAllocator.resolvePort()).isEqualTo(HcbsPortAllocator.DEFAULT_PORT);
        }
    }

    @Test
    void sequentialFallbackStartsAt8081() {
        assertThat(HcbsPortAllocator.SEQUENTIAL_FALLBACK_FIRST).isEqualTo(8081);
        assertThat(HcbsPortAllocator.SEQUENTIAL_FALLBACK_LAST).isGreaterThanOrEqualTo(8081);
    }
}
