package io.github.bdkx16.regenerativeagriculture.brewing;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BrewingRulesTest {

    @Test
    void theHydrometerFollowsTheFermentation() {
        assertEquals(BrewingRules.ORIGINAL_GRAVITY, BrewingRules.gravity(0), 1e-9);
        assertEquals(BrewingRules.FINAL_GRAVITY, BrewingRules.gravity(BrewingRules.FERMENT_TICKS), 1e-9);
        assertEquals(0, BrewingRules.alcohol(0), 1e-9);
        assertEquals(5.25, BrewingRules.alcohol(BrewingRules.FERMENT_TICKS), 1e-6);
        // Most of the sugar goes in the first half
        assertTrue(BrewingRules.attenuation(BrewingRules.FERMENT_TICKS / 2) > 0.8);
        assertEquals(1.0, BrewingRules.attenuation(BrewingRules.FERMENT_TICKS * 3L), 1e-9);
    }

    @Test
    void temperatureDecidesTheQuality() {
        assertEquals(0, BrewingRules.offIdeal(19));
        assertEquals(4, BrewingRules.offIdeal(12));
        assertEquals(8, BrewingRules.offIdeal(30));
        assertEquals(3, BrewingRules.quality(0.5));
        assertEquals(2, BrewingRules.quality(3));
        assertEquals(1, BrewingRules.quality(8));
    }
}
