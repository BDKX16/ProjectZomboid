package io.github.bdkx16.regenerativeagriculture.season;

import java.util.Random;

/**
 * Deterministic climate: biome base temperature + seasonal curve + daily weather anomaly + altitude.
 *
 * <p>Everything depends only on (seed, day, biome, height), so a forecast computed today is exactly what
 * happens on that day, and client and server agree as long as they share the seed.
 */
public final class ClimateModel {
    /** Half the difference between the warmest and the coldest day of an average year, in °C. */
    private static final double SEASONAL_AMPLITUDE = 9.0;
    /** Standard deviation of the day-to-day weather anomaly, in °C. */
    private static final double ANOMALY_SD = 3.0;
    /** Height above which air gets colder. */
    private static final int LAPSE_START_Y = 70;
    private static final double LAPSE_PER_BLOCK = 0.06;
    /** Hour of the day with the highest temperature (and lowest humidity). */
    private static final double WARMEST_HOUR = 15.0;
    /** Humidity swing between wet winters and dry summers, in percentage points. */
    private static final double HUMIDITY_SEASONAL = 8.0;
    private static final double HUMIDITY_DAILY_SWING = 12.0;
    private static final double HUMIDITY_ANOMALY_SD = 8.0;

    private final SeasonCalendar calendar;
    private final long seed;

    public ClimateModel(SeasonCalendar calendar, long seed) {
        this.calendar = calendar;
        this.seed = seed;
    }

    public SeasonCalendar calendar() {
        return calendar;
    }

    /**
     * Maps Minecraft's biome temperature (plains 0.8, taiga 0.25, desert 2.0, snowy 0.0) to a yearly mean in °C.
     */
    public static double annualMeanCelsius(float biomeTemperature) {
        if (biomeTemperature <= 1.0f) {
            return 1.0 + 18.0 * biomeTemperature;
        }
        return 19.0 + 6.0 * (biomeTemperature - 1.0);
    }

    /** Seasonal deviation from the yearly mean: warmest in mid-summer, coldest in mid-winter. */
    public double seasonalOffset(long day) {
        int dpy = calendar.daysPerYear();
        double midSummer = (Season.SUMMER.ordinal() + 0.5) * calendar.daysPerSeason();
        double dayOfYear = Math.floorMod(day, dpy) + 0.5;
        return SEASONAL_AMPLITUDE * Math.cos(2 * Math.PI * (dayOfYear - midSummer) / dpy);
    }

    /** Random but reproducible weather anomaly for the day, smoothed with the previous day so fronts last. */
    public double dailyAnomaly(long day) {
        return 0.65 * noise(day) + 0.35 * noise(day - 1);
    }

    private double noise(long day) {
        return new Random(mix(seed ^ mix(day))).nextGaussian() * ANOMALY_SD;
    }

    private static double altitudeOffset(int y) {
        return y > LAPSE_START_Y ? -(y - LAPSE_START_Y) * LAPSE_PER_BLOCK : 0.0;
    }

    /** Half of the difference between night and day; dry, hot biomes swing more. */
    private static double dailyRange(float biomeTemperature) {
        return biomeTemperature >= 1.5f ? 8.0 : 5.0;
    }

    public DayClimate day(float biomeTemperature, int y, long day) {
        double mean = annualMeanCelsius(biomeTemperature) + seasonalOffset(day) + dailyAnomaly(day) + altitudeOffset(y);
        double range = dailyRange(biomeTemperature);
        return new DayClimate(day, mean - range, mean + range);
    }

    /** Temperature at an exact moment, following a daily curve between the day's min and max. */
    public double temperatureAt(float biomeTemperature, int y, long ticks) {
        DayClimate climate = day(biomeTemperature, y, SeasonCalendar.absoluteDay(ticks));
        double hour = SeasonCalendar.ticksIntoDay(ticks) / 1000.0;
        double curve = Math.cos(2 * Math.PI * (hour - WARMEST_HOUR) / 24.0);
        return climate.mean() + (climate.max() - climate.mean()) * curve;
    }

    /**
     * Relative humidity (%) at an exact moment. Wet biomes (high downfall) are humid, deserts dry; winters and
     * nights are more humid than summers and afternoons. Deterministic like temperature.
     *
     * @param downfall      biome downfall (0 desert … 1 jungle/swamp)
     * @param precipitation whether the biome gets rain or snow at all
     */
    public double humidityAt(float downfall, boolean precipitation, long ticks) {
        long day = SeasonCalendar.absoluteDay(ticks);
        double base = precipitation ? 35.0 + 50.0 * downfall : 15.0 + 10.0 * downfall;
        double seasonal = -HUMIDITY_SEASONAL * seasonalOffset(day) / SEASONAL_AMPLITUDE;
        double anomaly = new Random(mix(seed ^ mix(day) ^ 0x4875L)).nextGaussian() * HUMIDITY_ANOMALY_SD;
        double hour = SeasonCalendar.ticksIntoDay(ticks) / 1000.0;
        double daily = -HUMIDITY_DAILY_SWING * Math.cos(2 * Math.PI * (hour - WARMEST_HOUR) / 24.0);
        return Math.clamp(base + seasonal + anomaly + daily, 5.0, 100.0);
    }

    /** SplitMix64 finaliser: turns nearby inputs into unrelated outputs. */
    public static long mix(long z) {
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }

    public record DayClimate(long day, double min, double max) {
        public double mean() {
            return (min + max) / 2;
        }

        public boolean frost() {
            return min < 0;
        }

        public TemperatureBand band() {
            return TemperatureBand.of(mean());
        }
    }
}
