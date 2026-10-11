package org.example.pz.projectzomboid_prd.crop;

import org.example.pz.projectzomboid_prd.greenhouse.Shelter;
import org.example.pz.projectzomboid_prd.season.ClimateModel;
import org.example.pz.projectzomboid_prd.season.Season;
import org.example.pz.projectzomboid_prd.season.SeasonCalendar;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GrowthCalculatorTest {
    private static final float PLAINS = 0.8f;
    private static final float SNOWY = 0.0f;
    private static final float DESERT = 2.0f;
    private static final int NOON = 6000;

    private final SeasonCalendar calendar = new SeasonCalendar(15);
    private final ClimateModel climate = new ClimateModel(calendar, 42L);

    private final GrowthRequirements wheat = new GrowthRequirements(
            List.of(Season.AUTUMN, Season.WINTER, Season.SPRING),
            new GrowthRequirements.TemperatureRange(0, 10, 22, 30), true, 1.0f);
    private final GrowthRequirements potato = new GrowthRequirements(
            List.of(Season.SPRING, Season.SUMMER),
            new GrowthRequirements.TemperatureRange(6, 15, 24, 32), false, 1.0f);

    private long noonOf(long day) {
        return SeasonCalendar.startOfDay(day) + NOON;
    }

    @Test
    void temperatureFactorRamps() {
        GrowthRequirements.TemperatureRange range = new GrowthRequirements.TemperatureRange(0, 10, 20, 30);
        assertEquals(0, range.factor(-1));
        assertEquals(0.5f, range.factor(5), 1e-6);
        assertEquals(1, range.factor(15));
        assertEquals(0.5f, range.factor(25), 1e-6);
        assertEquals(0, range.factor(31));
    }

    @Test
    void outOfSeasonDoesNotGrow() {
        long summerDay = 20; // day 20 is summer with 15-day seasons
        GrowthCalculator.Growth growth = GrowthCalculator.evaluate(wheat, climate, PLAINS, 64, noonOf(summerDay));
        assertEquals(0, growth.factor());
        assertEquals(GrowthCalculator.Stress.OUT_OF_SEASON, growth.stress());
    }

    @Test
    void springOnPlainsGrowsPotatoesMostDays() {
        int growing = 0;
        for (long day = 0; day < 15; day++) {
            if (GrowthCalculator.evaluate(potato, climate, PLAINS, 64, noonOf(day)).factor() > 0) {
                growing++;
            }
        }
        assertTrue(growing >= 10, "potatoes should grow on most spring days on plains, got " + growing);
    }

    @Test
    void frostDamagesSensitiveCropsOnly() {
        long winterNight = SeasonCalendar.startOfDay(52) + 4000; // 04:00 in mid-winter
        GrowthCalculator.Growth potatoGrowth = GrowthCalculator.evaluate(potato, climate, SNOWY, 64, winterNight);
        assertEquals(GrowthCalculator.Stress.FROST, potatoGrowth.stress());
        assertTrue(potatoGrowth.damaging());

        GrowthCalculator.Growth wheatGrowth = GrowthCalculator.evaluate(wheat, climate, SNOWY, 64, winterNight);
        assertTrue(!wheatGrowth.damaging(), "wheat tolerates frost");
    }

    @Test
    void desertSummerAfternoonIsTooHotForWheat() {
        long afternoon = SeasonCalendar.startOfDay(22) + 15000; // 15:00 mid-summer
        GrowthCalculator.Growth growth = GrowthCalculator.evaluate(wheat, climate, DESERT, 64, afternoon);
        assertEquals(GrowthCalculator.Stress.HEAT, growth.stress());
    }

    private final GrowthRequirements tomato = new GrowthRequirements(
            List.of(Season.SPRING, Season.SUMMER),
            new GrowthRequirements.TemperatureRange(10, 18, 28, 35), false, 1.0f);

    private static final Shelter GREENHOUSE = new Shelter(Shelter.Type.GREENHOUSE, false, true);
    private static final Shelter GREENHOUSE_NO_EXTRACTOR = new Shelter(Shelter.Type.GREENHOUSE, false, false);
    private static final Shelter HEATED_GREENHOUSE = new Shelter(Shelter.Type.GREENHOUSE, true, true);
    private static final Shelter GROW_ROOM = new Shelter(Shelter.Type.GROW_ROOM, false, true);

    @Test
    void greenhouseLetsCropsGrowOutOfSeason() {
        long summerNoon = noonOf(20);
        assertEquals(GrowthCalculator.Stress.OUT_OF_SEASON,
                GrowthCalculator.evaluate(wheat, climate, PLAINS, 64, summerNoon).stress());
        assertTrue(GrowthCalculator.evaluate(wheat, climate, PLAINS, 64, summerNoon, GREENHOUSE).factor() > 0);
    }

    @Test
    void heatedGreenhouseProtectsFromFrost() {
        long winterNight = SeasonCalendar.startOfDay(52) + 4000;
        assertTrue(GrowthCalculator.evaluate(potato, climate, SNOWY, 64, winterNight).damaging());
        assertTrue(!GrowthCalculator.evaluate(potato, climate, SNOWY, 64, winterNight, HEATED_GREENHOUSE).damaging());
    }

    @Test
    void desertGreenhouseOverheats() {
        long afternoon = SeasonCalendar.startOfDay(22) + 15000;
        GrowthCalculator.Growth growth = GrowthCalculator.evaluate(tomato, climate, DESERT, 64, afternoon,
                Shelter.of(Shelter.Type.GREENHOUSE_OVERHEATED));
        assertEquals(GrowthCalculator.Stress.HEAT, growth.stress());
    }

    @Test
    void ventilatedGrowRoomWorksAllYearEvenInTheDesert() {
        for (long day = 0; day < 60; day += 5) {
            GrowthCalculator.Growth growth = GrowthCalculator.evaluate(potato, climate, DESERT, 40, noonOf(day), GROW_ROOM);
            assertEquals(1.0f, growth.factor(), 1e-6, "day " + day);
        }
    }

    @Test
    void growRoomWithoutExtractorDoesNotGrow() {
        GrowthCalculator.Growth growth = GrowthCalculator.evaluate(potato, climate, PLAINS, 40, noonOf(5),
                Shelter.of(Shelter.Type.GROW_ROOM_UNVENTILATED));
        assertEquals(0, growth.factor());
        assertEquals(GrowthCalculator.Stress.NO_VENTILATION, growth.stress());
    }

    @Test
    void summerGreenhouseNeedsExtractorExceptInSnowyBiomes() {
        int poorlyVentilated = 0;
        for (long day = 15; day < 30; day++) {
            long morning = SeasonCalendar.startOfDay(day) + 6000;
            GrowthCalculator.Growth closed = GrowthCalculator.evaluate(tomato, climate, PLAINS, 64, morning, GREENHOUSE_NO_EXTRACTOR);
            GrowthCalculator.Growth vented = GrowthCalculator.evaluate(tomato, climate, PLAINS, 64, morning, GREENHOUSE);
            assertTrue(closed.factor() < vented.factor(), "day " + day + ": closed " + closed + " vs vented " + vented);
            if (closed.stress() == GrowthCalculator.Stress.POOR_VENTILATION) {
                poorlyVentilated++;
            }

            GrowthCalculator.Growth snowy = GrowthCalculator.evaluate(tomato, climate, SNOWY, 64, morning, GREENHOUSE_NO_EXTRACTOR);
            assertTrue(snowy.stress() != GrowthCalculator.Stress.POOR_VENTILATION, "snowy biomes do not need an extractor");
        }
        assertTrue(poorlyVentilated > 0);
    }
}
