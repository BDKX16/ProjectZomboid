package org.example.pz.projectzomboid_prd.brewing;

import org.example.pz.projectzomboid_prd.processing.DryingRules;

/**
 * Numbers of the microbrewery: how long each step takes, how the fermentation is read with a hydrometer and how
 * the fermentation temperature decides the beer's quality. Pure logic.
 */
public final class BrewingRules {
    /** Malt needed for one batch in the mash tun. */
    public static final int MALT_PER_BATCH = 4;
    /** Dried hops needed in the kettle. */
    public static final int HOPS_PER_BATCH = 2;
    /** Bottles of beer from one batch. */
    public static final int BOTTLES_PER_BATCH = 8;
    /** Mashing time: one in-game hour. */
    public static final int MASH_TICKS = 1000;
    /** Boiling time on the fire. */
    public static final int BOIL_TICKS = 1000;
    /** Fermentation: seven Minecraft days. */
    public static final int FERMENT_TICKS = 7 * DryingRules.DAY;

    /** Original gravity of the wort and final gravity of the finished beer. */
    public static final double ORIGINAL_GRAVITY = 1.050;
    public static final double FINAL_GRAVITY = 1.010;
    /** How fast the yeast eats the sugar: most of it goes in the first days. */
    private static final double FERMENT_RATE = 4.0;

    /** Ale yeast is happiest between these temperatures (°C). */
    public static final double IDEAL_MIN = 16;
    public static final double IDEAL_MAX = 22;
    /** Further than this from the ideal range, the beer gets off-flavours. */
    private static final double TOLERANCE = 5;

    private BrewingRules() {
    }

    /** Share of the sugar already fermented, 0 to 1, after {@code ticks} of fermentation. */
    public static double attenuation(long ticks) {
        double t = Math.clamp(ticks / (double) FERMENT_TICKS, 0.0, 1.0);
        return (1 - Math.exp(-FERMENT_RATE * t)) / (1 - Math.exp(-FERMENT_RATE));
    }

    /** What the hydrometer reads. */
    public static double gravity(long ticks) {
        return ORIGINAL_GRAVITY - (ORIGINAL_GRAVITY - FINAL_GRAVITY) * attenuation(ticks);
    }

    /** Alcohol by volume (%) from the usual homebrewer formula. */
    public static double alcohol(long ticks) {
        return (ORIGINAL_GRAVITY - gravity(ticks)) * 131.25;
    }

    /** How far {@code temperature} is from the ideal range (0 inside it). */
    public static double offIdeal(double temperature) {
        if (temperature < IDEAL_MIN) {
            return IDEAL_MIN - temperature;
        }
        return Math.max(0, temperature - IDEAL_MAX);
    }

    /**
     * Quality from the average distance to the ideal range during the fermentation: inside it three stars, within
     * a few degrees two, otherwise one.
     */
    public static int quality(double averageOffIdeal) {
        if (averageOffIdeal < 1) {
            return 3;
        }
        return averageOffIdeal <= TOLERANCE ? 2 : 1;
    }
}
