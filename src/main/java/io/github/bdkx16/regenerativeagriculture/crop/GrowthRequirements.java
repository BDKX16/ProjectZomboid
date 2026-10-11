package io.github.bdkx16.regenerativeagriculture.crop;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.bdkx16.regenerativeagriculture.season.Season;

import java.util.List;

/**
 * What a plant needs to grow, independent of any block (so it can be tested without Minecraft bootstrapped).
 *
 * @param seasons       seasons in which the plant grows outdoors
 * @param temperature   daily mean temperature range in °C
 * @param frostTolerant whether it survives nights below 0 °C
 * @param growthRate    0-1 multiplier on vanilla growth speed (slow crops < 1)
 */
public record GrowthRequirements(List<Season> seasons, TemperatureRange temperature, boolean frostTolerant, float growthRate) {
    public static final MapCodec<GrowthRequirements> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Season.CODEC.listOf().fieldOf("seasons").forGetter(GrowthRequirements::seasons),
            TemperatureRange.CODEC.fieldOf("temperature").forGetter(GrowthRequirements::temperature),
            Codec.BOOL.optionalFieldOf("frost_tolerant", false).forGetter(GrowthRequirements::frostTolerant),
            Codec.floatRange(0, 1).optionalFieldOf("growth_rate", 1.0f).forGetter(GrowthRequirements::growthRate)
    ).apply(instance, GrowthRequirements::new));

    public boolean growsIn(Season season) {
        return seasons.contains(season);
    }

    /**
     * Daily mean temperature limits in °C. Growth is full between the optimal bounds, falls linearly to zero
     * towards min/max and stops outside them.
     */
    public record TemperatureRange(float min, float optimalMin, float optimalMax, float max) {
        public static final Codec<TemperatureRange> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.FLOAT.fieldOf("min").forGetter(TemperatureRange::min),
                Codec.FLOAT.fieldOf("optimal_min").forGetter(TemperatureRange::optimalMin),
                Codec.FLOAT.fieldOf("optimal_max").forGetter(TemperatureRange::optimalMax),
                Codec.FLOAT.fieldOf("max").forGetter(TemperatureRange::max)
        ).apply(instance, TemperatureRange::new));

        public float factor(double celsius) {
            if (celsius <= min || celsius >= max) {
                return 0;
            }
            if (celsius < optimalMin) {
                return (float) ((celsius - min) / (optimalMin - min));
            }
            if (celsius > optimalMax) {
                return (float) ((max - celsius) / (max - optimalMax));
            }
            return 1;
        }
    }
}
