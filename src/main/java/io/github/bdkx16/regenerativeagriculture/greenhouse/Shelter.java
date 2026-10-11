package io.github.bdkx16.regenerativeagriculture.greenhouse;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import io.github.bdkx16.regenerativeagriculture.RegenerativeAgriculture;

import java.util.Locale;

/**
 * Where a crop is growing, which decides the climate it feels.
 *
 * @param heated      a heat source (campfire, furnace, magma…) is close to a greenhouse crop
 * @param ventilation working extractors and plants around the crop
 */
public record Shelter(Type type, boolean heated, Ventilation ventilation) {
    public static final Shelter OPEN = new Shelter(Type.OPEN, false, Ventilation.NONE);

    /** Greenhouse warming over the outside temperature, in °C. */
    public static final double GREENHOUSE_WARMING = 8.0;
    /** Extra warming from a heat source inside a greenhouse. */
    public static final double HEATER_WARMING = 6.0;
    /** A fully ventilated greenhouse never gets hotter than this. Without an extractor there is no cap. */
    public static final double GREENHOUSE_MAX = 26.0;
    /** Growth multiplier for an unventilated greenhouse in summer outside snowy biomes. */
    public static final float POOR_VENTILATION_FACTOR = 0.5f;
    /** Biomes at or below this Minecraft temperature are snowy: their summers do not overheat greenhouses. */
    public static final float SNOWY_BIOME_TEMPERATURE = 0.15f;
    /** Controlled temperature of an underground grow room. */
    public static final double GROW_ROOM_TEMPERATURE = 20.0;
    /** Plants whose evaporation one extractor can remove. */
    public static final int PLANTS_PER_EXTRACTOR = 20;

    public static Shelter of(Type type) {
        return new Shelter(type, false, Ventilation.NONE);
    }

    /** 0 (no extractor) to 1 (enough extractors for all the plants). */
    public float ventilationRatio() {
        return ventilation.ratio();
    }

    public boolean ventilated() {
        return ventilation.ratio() >= 1;
    }

    /** Temperature the plant feels, given the outdoor one. */
    public double adjust(double outside) {
        return switch (type) {
            case OPEN, GROW_ROOM_UNVENTILATED -> outside;
            case GREENHOUSE -> {
                double inside = outside + GREENHOUSE_WARMING + (heated ? HEATER_WARMING : 0);
                yield lerp(ventilationRatio(), inside, Math.min(inside, GREENHOUSE_MAX));
            }
            // In hot, dry biomes the glass traps heat and cannot be cooled: hotter than outside.
            case GREENHOUSE_OVERHEATED -> outside + GREENHOUSE_WARMING;
            case GROW_ROOM -> GROW_ROOM_TEMPERATURE;
        };
    }

    /**
     * Relative humidity (%) inside, given the outdoor one. Plants evaporate water: closed spaces with too few
     * extractors for their plants get muggy.
     */
    public double adjustHumidity(double outside) {
        return switch (type) {
            case OPEN -> outside;
            case GREENHOUSE -> {
                double vented = ventilation.extractors() > 0 ? Math.clamp(outside + 5, 45, 75) : outside + 5;
                yield lerp(ventilation.humidityLoad(), vented, Math.max(vented, Math.min(outside + 25, 98)));
            }
            case GREENHOUSE_OVERHEATED -> Math.max(outside - 5, 5);
            case GROW_ROOM -> lerp(ventilation.humidityLoad(), 60, 95);
            case GROW_ROOM_UNVENTILATED -> 95;
        };
    }

    /** Whether the plant can ignore its seasons here (only temperature still matters). */
    public boolean ignoresSeasons() {
        return type == Type.GREENHOUSE || type == Type.GROW_ROOM;
    }

    /** Greenhouses and grow rooms: closed spaces where humidity can build up. */
    public boolean enclosed() {
        return type != Type.OPEN;
    }

    public MutableComponent displayName() {
        String prefix = "shelter." + RegenerativeAgriculture.MOD_ID + ".";
        MutableComponent name = Component.translatable(prefix + type.name().toLowerCase(Locale.ROOT));
        if ((type == Type.GREENHOUSE || type == Type.GROW_ROOM) && ventilation.extractors() > 0) {
            name.append(Component.translatable(prefix + "extractors", ventilation.extractors(), ventilation.needed()));
        }
        if (heated) {
            name.append(Component.translatable(prefix + "heated"));
        }
        return name;
    }

    private static double lerp(float t, double from, double to) {
        return from + (to - from) * t;
    }

    /** Working extractors (with a vent shaft) and plants found around a crop. */
    public record Ventilation(int extractors, int plants) {
        public static final Ventilation NONE = new Ventilation(0, 0);

        /** Extractors needed for these plants (at least one). */
        public int needed() {
            return Math.max(1, Math.ceilDiv(plants, PLANTS_PER_EXTRACTOR));
        }

        public float ratio() {
            return Math.min(1f, (float) extractors / needed());
        }

        /**
         * How much the plants' evaporation exceeds what the extractors remove, 0-1: the share of plants not covered
         * by an extractor, scaled down when there are only a few plants in total.
         */
        public float humidityLoad() {
            if (plants == 0) {
                return 0;
            }
            int uncovered = Math.max(0, plants - extractors * PLANTS_PER_EXTRACTOR);
            return (float) uncovered / plants * Math.min(1f, (float) plants / PLANTS_PER_EXTRACTOR);
        }
    }

    public enum Type {
        /** Outdoors, or a structure that does not qualify. */
        OPEN,
        /** Glass roof and closed walls. */
        GREENHOUSE,
        /** A greenhouse in a desert or badlands: overheats. */
        GREENHOUSE_OVERHEATED,
        /** Underground, lit by grow lights, with at least one vented extractor. */
        GROW_ROOM,
        /** Underground with grow lights but no working extractor: air goes stale, nothing grows. */
        GROW_ROOM_UNVENTILATED
    }
}
