package org.example.pz.projectzomboid_prd.crop;

import org.example.pz.projectzomboid_prd.season.ClimateModel;
import org.example.pz.projectzomboid_prd.season.Season;
import org.example.pz.projectzomboid_prd.season.SeasonCalendar;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlantingAdvisorTest {
    private static final float PLAINS = 0.8f;

    private final ClimateModel climate = new ClimateModel(new SeasonCalendar(15), 7L);
    private final Map<String, GrowthRequirements> crops = Map.of(
            "lettuce", new GrowthRequirements(List.of(Season.AUTUMN, Season.WINTER, Season.SPRING),
                    new GrowthRequirements.TemperatureRange(2, 10, 20, 26), true, 1),
            "tomato", new GrowthRequirements(List.of(Season.SPRING, Season.SUMMER),
                    new GrowthRequirements.TemperatureRange(10, 18, 28, 35), false, 1));
    private final Function<String, GrowthRequirements> needs = crops::get;

    @Test
    void summerRecommendsTomatoNotLettuce() {
        List<String> now = PlantingAdvisor.plantNow(crops.keySet(), needs, climate, PLAINS, 64, 20);
        assertTrue(now.contains("tomato"), "tomato in summer: " + now);
        assertFalse(now.contains("lettuce"), "lettuce is out of season in summer: " + now);
    }

    @Test
    void winterRecommendsLettuceNotTomato() {
        List<String> now = PlantingAdvisor.plantNow(crops.keySet(), needs, climate, PLAINS, 64, 50);
        assertFalse(now.contains("tomato"));
    }

    @Test
    void lateWinterSuggestsPreparingSpringCrops() {
        // Day 55 is late winter: tomato (spring/summer) is next, lettuce already grows now.
        assertEquals(List.of("tomato"), PlantingAdvisor.nextSeason(crops.keySet(), needs, climate, 55));
    }

    @Test
    void daysUntilNextSeason() {
        SeasonCalendar calendar = climate.calendar();
        assertEquals(15, PlantingAdvisor.daysUntilNextSeason(calendar, 0));
        assertEquals(1, PlantingAdvisor.daysUntilNextSeason(calendar, 14));
    }
}
