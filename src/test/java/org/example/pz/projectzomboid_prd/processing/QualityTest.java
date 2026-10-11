package org.example.pz.projectzomboid_prd.processing;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class QualityTest {
    private static final int[] DAYS = {2, 4, 7};

    @Test
    void longerCuringGivesMoreStars() {
        assertEquals(0, Quality.afterDays(1.9, DAYS));
        assertEquals(1, Quality.afterDays(2, DAYS));
        assertEquals(2, Quality.afterDays(6.5, DAYS));
        assertEquals(3, Quality.afterDays(30, DAYS));
    }

    @Test
    void betterQualityLastsLonger() {
        assertEquals(1.0f, Quality.durationMultiplier(0));
        assertEquals(1.0f, Quality.durationMultiplier(1));
        assertEquals(1.5f, Quality.durationMultiplier(2));
        assertEquals(2.0f, Quality.durationMultiplier(3));
    }
}
