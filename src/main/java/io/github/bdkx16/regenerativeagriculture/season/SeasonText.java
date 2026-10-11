package io.github.bdkx16.regenerativeagriculture.season;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import io.github.bdkx16.regenerativeagriculture.RegenerativeAgriculture;

import java.util.Locale;

/** Shared text formatting for dates, times and temperatures (used by commands, HUD and screens). */
public final class SeasonText {
    private static final String PREFIX = RegenerativeAgriculture.MOD_ID + ".";

    private SeasonText() {
    }

    public static MutableComponent month(int month) {
        return Component.translatable("month." + PREFIX + month);
    }

    /** "3 de septiembre, año 1" */
    public static MutableComponent date(SeasonCalendar.Date date) {
        return Component.translatable("date." + PREFIX + "long", date.dayOfMonth(), month(date.month()), date.year());
    }

    /** "3 sep" */
    public static MutableComponent shortDate(SeasonCalendar.Date date) {
        return Component.translatable("date." + PREFIX + "short", date.dayOfMonth(),
                Component.translatable("month." + PREFIX + "short." + date.month()));
    }

    public static String time(long ticks) {
        return String.format(Locale.ROOT, "%02d:%02d", SeasonCalendar.hour(ticks), SeasonCalendar.minute(ticks));
    }

    public static MutableComponent celsius(double celsius) {
        // Rounded first so that -0.4 shows as "0°C", not "-0°C".
        return Component.literal(String.format(Locale.ROOT, "%d°C", Math.round(celsius)));
    }
}
