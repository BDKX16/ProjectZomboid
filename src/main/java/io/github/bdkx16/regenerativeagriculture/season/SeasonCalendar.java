package io.github.bdkx16.regenerativeagriculture.season;

/**
 * Pure calendar maths: turns the world clock into seasons, months and hours.
 *
 * <p>Southern hemisphere calendar. Day 0 of each year is 1 September (start of spring), so
 * spring = Sep-Nov, summer = Dec-Feb, autumn = Mar-May, winter = Jun-Aug.
 * Days roll over at midnight (Minecraft tick 0 is 06:00).
 */
public final class SeasonCalendar {
    public static final int TICKS_PER_DAY = 24000;
    /** Tick offset so that day boundaries fall on 00:00 instead of 06:00. */
    private static final int MIDNIGHT_OFFSET = 6000;
    private static final int MONTHS = 12;
    /** 0-based index of the first month of the year (September). */
    private static final int FIRST_MONTH = 8;

    private final int daysPerSeason;

    public SeasonCalendar(int daysPerSeason) {
        if (daysPerSeason < 1) {
            throw new IllegalArgumentException("daysPerSeason must be >= 1");
        }
        this.daysPerSeason = daysPerSeason;
    }

    public int daysPerSeason() {
        return daysPerSeason;
    }

    public int daysPerYear() {
        return daysPerSeason * 4;
    }

    public static long absoluteDay(long ticks) {
        return Math.floorDiv(ticks + MIDNIGHT_OFFSET, TICKS_PER_DAY);
    }

    /** Ticks since 00:00 of the current day. */
    public static int ticksIntoDay(long ticks) {
        return (int) Math.floorMod(ticks + MIDNIGHT_OFFSET, TICKS_PER_DAY);
    }

    public static int hour(long ticks) {
        return ticksIntoDay(ticks) / 1000;
    }

    public static int minute(long ticks) {
        return ticksIntoDay(ticks) % 1000 * 60 / 1000;
    }

    /** First tick (00:00) of the given absolute day. */
    public static long startOfDay(long day) {
        return day * TICKS_PER_DAY - MIDNIGHT_OFFSET;
    }

    public Date dateAt(long ticks) {
        return dateOfDay(absoluteDay(ticks));
    }

    public Date dateOfDay(long day) {
        int dayOfYear = (int) Math.floorMod(day, daysPerYear());
        int year = (int) Math.floorDiv(day, daysPerYear()) + 1;
        Season season = Season.byIndex(dayOfYear / daysPerSeason);
        int dayOfSeason = dayOfYear % daysPerSeason + 1;
        int monthOfYear = dayOfYear * MONTHS / daysPerYear();
        int month = (FIRST_MONTH + monthOfYear) % MONTHS;
        int dayOfMonth = dayOfYear - ceilDiv(monthOfYear * daysPerYear(), MONTHS) + 1;
        return new Date(day, year, dayOfYear, season, dayOfSeason, month, dayOfMonth);
    }

    /** The next day (today included) on which the given season starts. */
    public long nextStartOf(Season season, long fromDay) {
        long yearStart = fromDay - Math.floorMod(fromDay, daysPerYear());
        long start = yearStart + (long) season.ordinal() * daysPerSeason;
        return start < fromDay ? start + daysPerYear() : start;
    }

    private static int ceilDiv(int a, int b) {
        return -Math.floorDiv(-a, b);
    }

    /**
     * @param month      0-based calendar month (0 = January)
     * @param dayOfMonth 1-based
     */
    public record Date(long day, int year, int dayOfYear, Season season, int dayOfSeason, int month, int dayOfMonth) {
    }
}
