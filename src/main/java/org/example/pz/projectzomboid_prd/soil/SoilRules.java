package org.example.pz.projectzomboid_prd.soil;

import org.example.pz.projectzomboid_prd.crop.PlantFamily;

/** How soil and neighbours change growth, and how harvests, compost and mulch change the soil. Pure logic. */
public final class SoilRules {
    /** Nitrogen at or above which fertility is full. */
    public static final int FULL_NITROGEN = 50;
    /** Growth on exhausted soil (0 nitrogen). */
    public static final float EXHAUSTED_FERTILITY = 0.4f;
    public static final float MAX_ORGANIC_BONUS = 0.15f;
    public static final float SAME_FAMILY = 0.7f;
    public static final float MONOCULTURE = 0.5f;
    public static final float COMPANION_BONUS = 1.15f;
    public static final float ANTAGONIST_PENALTY = 0.75f;
    public static final float DIVERSITY_BONUS = 1.10f;
    /** Distinct crop species around (including itself) for the diversity bonus. */
    public static final int DIVERSE_SPECIES = 3;

    public static final int COMPOST_ORGANIC = 25;
    public static final int COMPOST_NITROGEN = 15;
    public static final int MULCH_HARVESTS = 4;
    public static final int MULCH_ORGANIC = 3;
    /** Organic matter lost to decomposition each harvest. */
    public static final int ORGANIC_DECAY = 2;
    /** Each harvest, organic matter releases this fraction of itself as nitrogen. */
    public static final int ORGANIC_TO_NITROGEN_DIVISOR = 20;
    /** Frost down to this temperature does not hurt crops on mulched soil. */
    public static final double MULCH_FROST_PROTECTION = -5.0;

    private SoilRules() {
    }

    /** Fertility from nitrogen and organic matter. */
    public static float fertility(SoilData soil) {
        float nitrogen = EXHAUSTED_FERTILITY + (1 - EXHAUSTED_FERTILITY) * Math.min(1f, soil.nitrogen() / (float) FULL_NITROGEN);
        return nitrogen * (1 + MAX_ORGANIC_BONUS * soil.organic() / 100f);
    }

    /** Penalty for growing the same family as the last harvest(s) on this soil. */
    public static float rotation(SoilData soil, PlantFamily family) {
        if (soil.history().isEmpty() || soil.history().getFirst() != family) {
            return 1;
        }
        boolean monoculture = soil.history().size() >= SoilData.HISTORY
                && soil.history().stream().allMatch(previous -> previous == family);
        return monoculture ? MONOCULTURE : SAME_FAMILY;
    }

    public static float neighbours(Neighbourhood neighbourhood) {
        float factor = 1;
        if (neighbourhood.companion()) {
            factor *= COMPANION_BONUS;
        }
        if (neighbourhood.antagonist()) {
            factor *= ANTAGONIST_PENALTY;
        }
        if (neighbourhood.species() >= DIVERSE_SPECIES) {
            factor *= DIVERSITY_BONUS;
        }
        return factor;
    }

    /** Combined multiplier on growth from soil and neighbours. */
    public static float multiplier(SoilData soil, PlantFamily family, Neighbourhood neighbourhood) {
        return fertility(soil) * rotation(soil, family) * neighbours(neighbourhood);
    }

    /**
     * Soil after harvesting a crop.
     *
     * @param nitrogenChange negative for most crops (consumed), positive for legumes (fixed)
     */
    public static SoilData harvest(SoilData soil, PlantFamily family, int nitrogenChange) {
        int released = soil.organic() / ORGANIC_TO_NITROGEN_DIVISOR;
        int organic = soil.organic() - ORGANIC_DECAY + (soil.mulch() > 0 ? MULCH_ORGANIC : 0);
        return new SoilData(soil.nitrogen() + nitrogenChange + released, organic, soil.history(), soil.mulch() - 1)
                .withHarvested(family);
    }

    public static SoilData compost(SoilData soil) {
        return soil.withOrganic(soil.organic() + COMPOST_ORGANIC).withNitrogen(soil.nitrogen() + COMPOST_NITROGEN);
    }

    public static SoilData mulch(SoilData soil) {
        return soil.withMulch(MULCH_HARVESTS);
    }

    /**
     * Crops of other species within reach of a crop.
     *
     * @param species    distinct crop species, the crop itself included
     * @param companion  a companion plant of this crop is near
     * @param antagonist an antagonist plant of this crop is near
     */
    public record Neighbourhood(int species, boolean companion, boolean antagonist) {
        public static final Neighbourhood ALONE = new Neighbourhood(1, false, false);
    }
}
