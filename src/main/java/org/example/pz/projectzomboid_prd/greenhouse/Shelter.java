package org.example.pz.projectzomboid_prd.greenhouse;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.example.pz.projectzomboid_prd.Projectzomboid_prd;

import java.util.Locale;

/**
 * Where a crop is growing, which decides the climate it feels.
 *
 * @param heated     a heat source (campfire, furnace, magma…) is close to a greenhouse crop
 * @param ventilated an extractor with an open vent shaft serves the greenhouse or grow room
 */
public record Shelter(Type type, boolean heated, boolean ventilated) {
    public static final Shelter OPEN = new Shelter(Type.OPEN, false, false);

    /** Greenhouse warming over the outside temperature, in °C. */
    public static final double GREENHOUSE_WARMING = 8.0;
    /** Extra warming from a heat source inside a greenhouse. */
    public static final double HEATER_WARMING = 6.0;
    /** A ventilated greenhouse never gets hotter than this. Without an extractor there is no cap. */
    public static final double GREENHOUSE_MAX = 26.0;
    /** Growth multiplier for an unventilated greenhouse in summer outside snowy biomes. */
    public static final float POOR_VENTILATION_FACTOR = 0.5f;
    /** Biomes at or below this Minecraft temperature are snowy: their summers do not overheat greenhouses. */
    public static final float SNOWY_BIOME_TEMPERATURE = 0.15f;
    /** Controlled temperature of an underground grow room. */
    public static final double GROW_ROOM_TEMPERATURE = 20.0;

    public static Shelter of(Type type) {
        return new Shelter(type, false, false);
    }

    /** Temperature the plant feels, given the outdoor one. */
    public double adjust(double outside) {
        return switch (type) {
            case OPEN, GROW_ROOM_UNVENTILATED -> outside;
            case GREENHOUSE -> {
                double inside = outside + GREENHOUSE_WARMING + (heated ? HEATER_WARMING : 0);
                yield ventilated ? Math.min(inside, GREENHOUSE_MAX) : inside;
            }
            // In hot, dry biomes the glass traps heat and cannot be cooled: hotter than outside.
            case GREENHOUSE_OVERHEATED -> outside + GREENHOUSE_WARMING;
            case GROW_ROOM -> GROW_ROOM_TEMPERATURE;
        };
    }

    /** Whether the plant can ignore its seasons here (only temperature still matters). */
    public boolean ignoresSeasons() {
        return type == Type.GREENHOUSE || type == Type.GROW_ROOM;
    }

    public MutableComponent displayName() {
        MutableComponent name = Component.translatable("shelter." + Projectzomboid_prd.MOD_ID + "." + type.name().toLowerCase(Locale.ROOT));
        if (type == Type.GREENHOUSE && ventilated) {
            name.append(Component.translatable("shelter." + Projectzomboid_prd.MOD_ID + ".ventilated"));
        }
        if (heated) {
            name.append(Component.translatable("shelter." + Projectzomboid_prd.MOD_ID + ".heated"));
        }
        return name;
    }

    public enum Type {
        /** Outdoors, or a structure that does not qualify. */
        OPEN,
        /** Glass roof and closed walls. */
        GREENHOUSE,
        /** A greenhouse in a desert or badlands: overheats. */
        GREENHOUSE_OVERHEATED,
        /** Underground, lit by grow lights, with a vented extractor. */
        GROW_ROOM,
        /** Underground with grow lights but no working extractor: air goes stale, nothing grows. */
        GROW_ROOM_UNVENTILATED
    }
}
