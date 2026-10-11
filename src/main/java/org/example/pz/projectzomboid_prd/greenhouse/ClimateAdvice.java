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
    public static final double HOT = 32;

    private ClimateAdvice() {
    }

    /** Advice, most important first; a single {@code ok} when nothing needs fixing. */
    public static List<Advice> advise(MicroClimate reading) {
        List<Advice> advice = new ArrayList<>();
        Shelter shelter = reading.shelter();
        Shelter.Ventilation ventilation = shelter.ventilation();
        switch (shelter.type()) {
            case GROW_ROOM_UNVENTILATED -> advice.add(Advice.of("grow_room_needs_extractor"));
            case GREENHOUSE_OVERHEATED -> advice.add(Advice.of("greenhouse_overheated"));
            case GREENHOUSE -> {
                if (ventilation.extractors() == 0) {
                    advice.add(Advice.of(reading.season() == Season.SUMMER && !reading.snowyBiome()
                            ? "greenhouse_needs_extractor_summer" : "greenhouse_extractor_recommended"));
                }
            }
            default -> {
                if (reading.undergroundWithoutLight()) {
                    advice.add(Advice.of("needs_grow_light"));
                }
            }
        }
        if (ventilation.extractors() > 0 && !shelter.ventilated()) {
            advice.add(Advice.of("not_enough_extractors", ventilation.plants(), ventilation.extractors(), ventilation.needed()));
        }
        if (reading.temperature() < 0) {
            advice.add(Advice.of("frost"));
        } else if (reading.temperature() > HOT) {
            advice.add(Advice.of("too_hot"));
        }
        if (Mold.atRisk(shelter, reading.humidity())) {
            advice.add(Advice.of("mold_risk"));
        } else if (reading.humidity() < DRY) {
            advice.add(Advice.of("dry"));
        }
        if (advice.isEmpty()) {
            advice.add(Advice.of("ok"));
        }
        return advice;
    }

    /** One piece of advice: a translation key under {@code advice.projectzomboid_prd.} and its arguments. */
    public record Advice(String key, List<Object> args) {
        public static Advice of(String key, Object... args) {
            return new Advice(key, List.of(args));
        }

        public boolean ok() {
            return key.equals("ok");
        }

        public Component text() {
            return Component.translatable("advice." + Projectzomboid_prd.MOD_ID + "." + key, args.toArray());
        }
    }
}
