package io.github.bdkx16.regenerativeagriculture.soil;

import io.github.bdkx16.regenerativeagriculture.crop.PlantFamily;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SoilRulesTest {
    private static final SoilRules.Neighbourhood ALONE = SoilRules.Neighbourhood.ALONE;

    @Test
    void exhaustedSoilGrowsSlowly() {
        SoilData rich = new SoilData(80, 0, List.of(), 0);
        SoilData poor = new SoilData(0, 0, List.of(), 0);
        assertEquals(1.0f, SoilRules.fertility(rich), 1e-6);
        assertEquals(SoilRules.EXHAUSTED_FERTILITY, SoilRules.fertility(poor), 1e-6);
        assertTrue(SoilRules.fertility(rich.withOrganic(100)) > SoilRules.fertility(rich), "organic matter helps");
    }

    @Test
    void repeatedHarvestsDrainNitrogenAndLegumesRestoreIt() {
        SoilData soil = SoilData.DEFAULT;
        for (int i = 0; i < 4; i++) {
            soil = SoilRules.harvest(soil, PlantFamily.NIGHTSHADES, -12);
        }
        assertTrue(soil.nitrogen() < SoilData.DEFAULT.nitrogen() - 25, "four tomato harvests should drain the soil: " + soil);
        SoilData afterSoy = SoilRules.harvest(soil, PlantFamily.LEGUMES, 20);
        assertTrue(afterSoy.nitrogen() > soil.nitrogen() + 15, "soy fixes nitrogen");
    }

    @Test
    void rotationPenalisesRepeatingFamilies() {
        SoilData once = SoilData.DEFAULT.withHarvested(PlantFamily.BRASSICAS);
        assertEquals(1.0f, SoilRules.rotation(once, PlantFamily.LEGUMES));
        assertEquals(SoilRules.SAME_FAMILY, SoilRules.rotation(once, PlantFamily.BRASSICAS));

        SoilData mono = once.withHarvested(PlantFamily.BRASSICAS).withHarvested(PlantFamily.BRASSICAS);
        assertEquals(SoilRules.MONOCULTURE, SoilRules.rotation(mono, PlantFamily.BRASSICAS));
        assertEquals(SoilData.HISTORY, mono.withHarvested(PlantFamily.GRASSES).history().size(), "history is capped");
    }

    @Test
    void neighboursMatter() {
        assertEquals(1.0f, SoilRules.neighbours(ALONE));
        assertTrue(SoilRules.neighbours(new SoilRules.Neighbourhood(2, true, false)) > 1);
        assertTrue(SoilRules.neighbours(new SoilRules.Neighbourhood(2, false, true)) < 1);
        assertEquals(SoilRules.DIVERSITY_BONUS, SoilRules.neighbours(new SoilRules.Neighbourhood(3, false, false)), 1e-6);
    }

    @Test
    void compostAndMulch() {
        SoilData composted = SoilRules.compost(new SoilData(10, 10, List.of(), 0));
        assertEquals(10 + SoilRules.COMPOST_NITROGEN, composted.nitrogen());
        assertEquals(10 + SoilRules.COMPOST_ORGANIC, composted.organic());

        SoilData mulched = SoilRules.mulch(SoilData.DEFAULT);
        assertEquals(SoilRules.MULCH_HARVESTS, mulched.mulch());
        SoilData harvested = SoilRules.harvest(mulched, PlantFamily.COMPOSITES, -6);
        assertEquals(SoilRules.MULCH_HARVESTS - 1, harvested.mulch());
        assertTrue(harvested.organic() > SoilRules.harvest(SoilData.DEFAULT, PlantFamily.COMPOSITES, -6).organic(),
                "mulch feeds organic matter");
    }

    @Test
    void valuesStayInRange() {
        SoilData soil = new SoilData(5, 2, List.of(), 0);
        for (int i = 0; i < 10; i++) {
            soil = SoilRules.harvest(soil, PlantFamily.CUCURBITS, -14);
        }
        assertEquals(0, soil.organic());
        assertTrue(soil.nitrogen() >= 0);
        assertEquals(100, SoilRules.compost(new SoilData(95, 95, List.of(), 0)).organic());
    }
}
