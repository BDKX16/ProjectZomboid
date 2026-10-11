package org.example.pz.projectzomboid_prd.processing;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DryingRulesTest {

    @Test
    void dryWarmAirIsFastestAndRainStopsDrying() {
        assertTrue(DryingRules.rate(25, 30, false) > DryingRules.rate(25, 60, false));
        assertTrue(DryingRules.rate(25, 50, false) > DryingRules.rate(2, 50, false), "cold air dries slowly");
        assertEquals(0, DryingRules.rate(25, 30, true), "rain");
        assertEquals(0, DryingRules.rate(25, 95, false), "saturated air");
    }

    @Test
    void aBatchDriesInItsDaysAtNormalSpeed() {
        // 50 % humidity and 20 °C is exactly normal speed
        assertEquals(1.0f, DryingRules.rate(20, 50, false), 1e-6);
        DryingRules.Batch batch = DryingRules.Batch.FRESH.advance(2, 20, 50, false, DryingRules.DAY);
        assertEquals(0.5f, batch.progress(), 1e-4);
        batch = batch.advance(2, 20, 50, false, DryingRules.DAY);
        assertTrue(batch.done());
        assertFalse(batch.spoiled());
    }

    @Test
    void dampAirSpoilsTheBatchButDryAirLetsItRecover() {
        DryingRules.Batch batch = DryingRules.Batch.FRESH.advance(3, 20, 88, false, DryingRules.MOLD_TICKS / 2);
        batch = batch.advance(3, 20, 40, false, DryingRules.MOLD_TICKS / 2);
        batch = batch.advance(3, 20, 88, false, DryingRules.MOLD_TICKS / 2);
        assertFalse(batch.spoiled(), "a dry spell in between resets the dampness");
        batch = batch.advance(3, 20, 88, false, DryingRules.MOLD_TICKS);
        assertTrue(batch.spoiled());
    }
}
