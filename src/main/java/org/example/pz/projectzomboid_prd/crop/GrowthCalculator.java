package org.example.pz.projectzomboid_prd.crop;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.example.pz.projectzomboid_prd.Projectzomboid_prd;
import org.example.pz.projectzomboid_prd.greenhouse.Shelter;
import org.example.pz.projectzomboid_prd.season.ClimateModel;
import org.example.pz.projectzomboid_prd.season.Season;
import org.example.pz.projectzomboid_prd.season.SeasonCalendar;

import java.util.Locale;

/** Decides how well a crop grows right now. Pure logic: no world access. */
public final class GrowthCalculator {
    /** Degrees above the maximum at which plants start to wilt. */
    private static final double HEAT_DAMAGE_MARGIN = 4.0;

    private GrowthCalculator() {
    }

    /** Outdoors. */
    public static Growth evaluate(GrowthRequirements needs, ClimateModel climate, float biomeTemperature, int y, long ticks) {
        return evaluate(needs, climate, biomeTemperature, y, ticks, Shelter.OPEN);
    }

    /**
     * @param biomeTemperature Minecraft biome base temperature at the crop
     * @param y                height of the crop
     * @param ticks            overworld clock time
     * @param shelter          greenhouse / grow room the crop is in, which changes the temperature it feels
     */
    public static Growth evaluate(GrowthRequirements needs, ClimateModel climate, float biomeTemperature, int y, long ticks, Shelter shelter) {
        if (shelter.type() == Shelter.Type.GROW_ROOM_UNVENTILATED) {
            return new Growth(0, Stress.NO_VENTILATION);
        }

        double now = shelter.adjust(climate.temperatureAt(biomeTemperature, y, ticks));
        if (now < 0 && !needs.frostTolerant()) {
            return new Growth(0, Stress.FROST);
        }
        if (now > needs.temperature().max() + HEAT_DAMAGE_MARGIN) {
            return new Growth(0, Stress.HEAT);
        }

        SeasonCalendar.Date date = climate.calendar().dateAt(ticks);
        if (!shelter.ignoresSeasons() && !needs.growsIn(date.season())) {
            return new Growth(0, Stress.OUT_OF_SEASON);
        }

        double dayMean = shelter.adjust(climate.day(biomeTemperature, y, date.day()).mean());
        float temperatureFactor = needs.temperature().factor(dayMean);
        if (temperatureFactor <= 0) {
            return new Growth(0, dayMean < needs.temperature().min() ? Stress.TOO_COLD : Stress.TOO_HOT);
        }
        float factor = temperatureFactor * needs.growthRate();

        // A closed greenhouse in summer gets stuffy unless it has enough extractors (snowy biomes stay cool enough).
        if (shelter.type() == Shelter.Type.GREENHOUSE && !shelter.ventilated()
                && date.season() == Season.SUMMER && biomeTemperature > Shelter.SNOWY_BIOME_TEMPERATURE) {
            float penalty = Shelter.POOR_VENTILATION_FACTOR + (1 - Shelter.POOR_VENTILATION_FACTOR) * shelter.ventilationRatio();
            return new Growth(factor * penalty, Stress.POOR_VENTILATION);
        }
        return new Growth(factor, Stress.NONE);
    }

    /** @param factor 0-1 chance that a growth tick goes ahead */
    public record Growth(float factor, Stress stress) {
        public boolean damaging() {
            return stress.damaging;
        }
    }

    public enum Stress {
        NONE(false),
        OUT_OF_SEASON(false),
        TOO_COLD(false),
        TOO_HOT(false),
        /** Greenhouse without enough extractors in summer: down to half speed. */
        POOR_VENTILATION(false),
        /** Grow room without a working extractor: nothing grows. */
        NO_VENTILATION(false),
        /** Night below 0 °C on a frost-sensitive plant. */
        FROST(true),
        /** Well above the plant's maximum temperature. */
        HEAT(true);

        private final boolean damaging;

        Stress(boolean damaging) {
            this.damaging = damaging;
        }

        public MutableComponent displayName() {
            return Component.translatable("stress." + Projectzomboid_prd.MOD_ID + "." + name().toLowerCase(Locale.ROOT));
        }
    }
}
