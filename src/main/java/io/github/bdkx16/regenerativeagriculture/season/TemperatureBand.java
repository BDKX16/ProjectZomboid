package io.github.bdkx16.regenerativeagriculture.season;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import io.github.bdkx16.regenerativeagriculture.RegenerativeAgriculture;

import java.util.Locale;

/** Coarse temperature ranges used by crops and shown to the player. */
public enum TemperatureBand {
    FROST(Double.NEGATIVE_INFINITY, 0x9AD7FF),
    COLD(0, 0x7FB2E5),
    MILD(10, 0x7BD35A),
    WARM(24, 0xF5C542),
    HOT(32, 0xE5533D);

    private final double minCelsius;
    private final int color;

    TemperatureBand(double minCelsius, int color) {
        this.minCelsius = minCelsius;
        this.color = color;
    }

    public int color() {
        return color;
    }

    public static TemperatureBand of(double celsius) {
        TemperatureBand result = FROST;
        for (TemperatureBand band : values()) {
            if (celsius >= band.minCelsius) {
                result = band;
            }
        }
        return result;
    }

    public MutableComponent displayName() {
        return Component.translatable("temperature." + RegenerativeAgriculture.MOD_ID + "." + name().toLowerCase(Locale.ROOT))
                .withColor(color);
    }
}
