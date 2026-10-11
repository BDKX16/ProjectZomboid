package io.github.bdkx16.regenerativeagriculture.season;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClimateModelTest {
    private static final float PLAINS = 0.8f;
    private final ClimateModel climate = new ClimateModel(new SeasonCalendar(15), 12345L);

    @Test
    void forecastIsDeterministic() {
        ClimateModel other = new ClimateModel(new SeasonCalendar(15), 12345L);
        for (long day = 0; day < 120; day++) {
            assertEquals(climate.day(PLAINS, 64, day), other.day(PLAINS, 64, day));
        }
    }

    @Test
    void summerIsWarmerThanWinter() {
        double summer = 0;
        double winter = 0;
        for (int i = 0; i < 15; i++) {
            summer += climate.day(PLAINS, 64, 15 + i).mean();
            winter += climate.day(PLAINS, 64, 45 + i).mean();
        }
        assertTrue(summer / 15 - winter / 15 > 12, "summer should be clearly warmer than winter");
    }

    @Test
    void mountainsAreColder() {
        assertTrue(climate.day(PLAINS, 160, 10).mean() < climate.day(PLAINS, 64, 10).mean() - 4);
    }

    @Test
    void afternoonIsWarmerThanNight() {
        long day = 20;
        long afternoon = SeasonCalendar.startOfDay(day) + 15_000;
        long night = SeasonCalendar.startOfDay(day) + 3_000;
        assertTrue(climate.temperatureAt(PLAINS, 64, afternoon) > climate.temperatureAt(PLAINS, 64, night));
    }

    @Test
    void biomeMeansAreSensible() {
        assertEquals(15.4, ClimateModel.annualMeanCelsius(0.8f), 0.01);
        assertEquals(25.0, ClimateModel.annualMeanCelsius(2.0f), 0.01);
        assertTrue(ClimateModel.annualMeanCelsius(0.0f) < 2);
    }

    @Test
    void humidityFollowsBiomeAndTimeOfDay() {
        long day = 20;
        long dawn = SeasonCalendar.startOfDay(day) + 5000;
        long afternoon = SeasonCalendar.startOfDay(day) + 15000;
        double desert = climate.humidityAt(0.0f, false, afternoon);
        double jungle = climate.humidityAt(0.9f, true, afternoon);
        assertTrue(desert < 35, "desert afternoon should be dry: " + desert);
        assertTrue(jungle > desert + 30, "jungle much more humid than desert");
        assertTrue(climate.humidityAt(0.4f, true, dawn) > climate.humidityAt(0.4f, true, afternoon), "dawn more humid");
        assertEquals(climate.humidityAt(0.4f, true, dawn), new ClimateModel(new SeasonCalendar(15), 12345L).humidityAt(0.4f, true, dawn));
    }
}
