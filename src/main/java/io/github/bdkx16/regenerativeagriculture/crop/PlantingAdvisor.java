package io.github.bdkx16.regenerativeagriculture.crop;

import io.github.bdkx16.regenerativeagriculture.season.ClimateModel;
import io.github.bdkx16.regenerativeagriculture.season.Season;
import io.github.bdkx16.regenerativeagriculture.season.SeasonCalendar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/** Suggests what to sow outdoors at a place, from the crop profiles and the forecast. Pure logic. */
public final class PlantingAdvisor {
    /** Days of forecast considered when recommending a crop. */
    public static final int FORECAST_DAYS = 7;
    /** Minimum average temperature suitability over the forecast for a crop to be recommended. */
    private static final float MIN_AVERAGE_FACTOR = 0.25f;

    private PlantingAdvisor() {
    }

    /**
     * Crops worth sowing today: in season, warm/cool enough on average over the next days and, if frost-sensitive,
     * with no frost in the forecast.
     */
    public static <T> List<T> plantNow(Collection<T> crops, Function<T, GrowthRequirements> needsOf,
                                       ClimateModel climate, float biomeTemperature, int y, long today) {
        Season season = climate.calendar().dateOfDay(today).season();
        List<T> result = new ArrayList<>();
        for (T crop : crops) {
            GrowthRequirements needs = needsOf.apply(crop);
            if (needs.growsIn(season) && suitable(needs, climate, biomeTemperature, y, today)) {
                result.add(crop);
            }
        }
        return result;
    }

    /** Crops that are not in season now but will be next season, to prepare seeds for. */
    public static <T> List<T> nextSeason(Collection<T> crops, Function<T, GrowthRequirements> needsOf,
                                         ClimateModel climate, long today) {
        Season season = climate.calendar().dateOfDay(today).season();
        Season next = Season.byIndex(season.ordinal() + 1);
        List<T> result = new ArrayList<>();
        for (T crop : crops) {
            GrowthRequirements needs = needsOf.apply(crop);
            if (needs.growsIn(next) && !needs.growsIn(season)) {
                result.add(crop);
            }
        }
        return result;
    }

    /** Days until the next season starts (1 on the last day of a season). */
    public static int daysUntilNextSeason(SeasonCalendar calendar, long today) {
        return calendar.daysPerSeason() - calendar.dateOfDay(today).dayOfSeason() + 1;
    }

    private static boolean suitable(GrowthRequirements needs, ClimateModel climate, float biomeTemperature, int y, long today) {
        float total = 0;
        for (int i = 0; i < FORECAST_DAYS; i++) {
            ClimateModel.DayClimate day = climate.day(biomeTemperature, y, today + i);
            if (day.frost() && !needs.frostTolerant()) {
                return false;
            }
            total += needs.temperature().factor(day.mean());
        }
        return total / FORECAST_DAYS >= MIN_AVERAGE_FACTOR;
    }
}
