package org.example.pz.projectzomboid_prd.greenhouse;

import org.example.pz.projectzomboid_prd.season.Season;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClimateAdviceTest {
    private static MicroClimate reading(Shelter shelter, Season season, double temperature, double humidity) {
        return new MicroClimate(temperature, humidity, shelter, season, false, false);
    }

    @Test
    void goodGrowRoomIsOk() {
        Shelter room = new Shelter(Shelter.Type.GROW_ROOM, false, true);
        assertEquals(List.of("ok"), ClimateAdvice.advise(reading(room, Season.WINTER, 20, room.adjustHumidity(40))));
    }

    @Test
    void unventilatedGrowRoomIsMuggyAndNeedsExtractor() {
        Shelter room = Shelter.of(Shelter.Type.GROW_ROOM_UNVENTILATED);
        List<String> advice = ClimateAdvice.advise(reading(room, Season.WINTER, 15, room.adjustHumidity(40)));
        assertEquals("grow_room_needs_extractor", advice.getFirst());
        assertTrue(advice.contains("muggy"));
    }

    @Test
    void summerGreenhouseWithoutExtractor() {
        Shelter closed = new Shelter(Shelter.Type.GREENHOUSE, false, false);
        assertEquals("greenhouse_needs_extractor_summer", ClimateAdvice.advise(reading(closed, Season.SUMMER, 28, 70)).getFirst());
        assertEquals("greenhouse_extractor_recommended", ClimateAdvice.advise(reading(closed, Season.WINTER, 15, 70)).getFirst());
        MicroClimate snowySummer = new MicroClimate(15, 70, closed, Season.SUMMER, true, false);
        assertEquals("greenhouse_extractor_recommended", ClimateAdvice.advise(snowySummer).getFirst());
    }

    @Test
    void darkCaveNeedsGrowLight() {
        MicroClimate cave = new MicroClimate(12, 60, Shelter.OPEN, Season.SPRING, false, true);
        assertEquals("needs_grow_light", ClimateAdvice.advise(cave).getFirst());
    }

    @Test
    void frostAndDryAir() {
        List<String> advice = ClimateAdvice.advise(reading(Shelter.OPEN, Season.WINTER, -3, 20));
        assertTrue(advice.contains("frost") && advice.contains("dry"));
    }
}
