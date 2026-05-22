package com.hcbs.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HcbsPortAllocatorTest {

    @Test
    void hashPortStaysWithinConfiguredRange() {
        int port = HcbsPortAllocator.hashPort();
        assertThat(port).isBetween(HcbsPortAllocator.HASH_PORT_BASE,
                HcbsPortAllocator.HASH_PORT_BASE + HcbsPortAllocator.HASH_PORT_SPAN - 1);
    }

    @Test
    void hashPortIsDeterministicForSameWorkingDirectory() {
        assertThat(HcbsPortAllocator.hashPort()).isEqualTo(HcbsPortAllocator.hashPort());
    }

    @Test
    void resolvePortReturnsDefaultWhenAvailable() {
        if (HcbsPortAllocator.isPortAvailable(HcbsPortAllocator.DEFAULT_PORT)) {
            assertThat(HcbsPortAllocator.resolvePort()).isEqualTo(HcbsPortAllocator.DEFAULT_PORT);
        }
    }
}
