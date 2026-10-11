package org.example.pz.projectzomboid_prd.greenhouse;

import net.minecraft.network.chat.Component;
import org.example.pz.projectzomboid_prd.Projectzomboid_prd;
import org.example.pz.projectzomboid_prd.season.Season;

import java.util.ArrayList;
import java.util.List;

/** Turns a {@link MicroClimate} reading into advice for setting up a greenhouse or grow room. Pure logic. */
public final class ClimateAdvice {
    public static final double DRY = 30;
    public static final double HUMID = 75;
    public static final double MUGGY = 85;
    public static final double HOT = 32;

    private ClimateAdvice() {
    }

    /** Advice keys, most important first; {@code ok} when nothing needs fixing. */
    public static List<String> advise(MicroClimate reading) {
        List<String> advice = new ArrayList<>();
        Shelter shelter = reading.shelter();
        switch (shelter.type()) {
            case GROW_ROOM_UNVENTILATED -> advice.add("grow_room_needs_extractor");
            case GREENHOUSE_OVERHEATED -> advice.add("greenhouse_overheated");
            case GREENHOUSE -> {
                if (!shelter.ventilated()) {
                    advice.add(reading.season() == Season.SUMMER && !reading.snowyBiome()
                            ? "greenhouse_needs_extractor_summer" : "greenhouse_extractor_recommended");
                }
            }
            default -> {
                if (reading.undergroundWithoutLight()) {
                    advice.add("needs_grow_light");
                }
            }
        }
        if (reading.temperature() < 0) {
            advice.add("frost");
        } else if (reading.temperature() > HOT) {
            advice.add("too_hot");
        }
        if (reading.humidity() > MUGGY) {
            advice.add("muggy");
        } else if (reading.humidity() < DRY) {
            advice.add("dry");
        }
        if (advice.isEmpty()) {
            advice.add("ok");
        }
        return advice;
    }

    public static Component text(String key) {
        return Component.translatable("advice." + Projectzomboid_prd.MOD_ID + "." + key);
    }
}
