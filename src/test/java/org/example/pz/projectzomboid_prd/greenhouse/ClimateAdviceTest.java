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

    private static List<String> keys(MicroClimate reading) {
        return ClimateAdvice.advise(reading).stream().map(ClimateAdvice.Advice::key).toList();
    }

    @Test
    void goodGrowRoomIsOk() {
        Shelter room = new Shelter(Shelter.Type.GROW_ROOM, false, new Shelter.Ventilation(1, 10));
        assertEquals(List.of("ok"), keys(reading(room, Season.WINTER, 20, room.adjustHumidity(40))));
    }

    @Test
    void unventilatedGrowRoomIsMuggyAndNeedsExtractor() {
        Shelter room = Shelter.of(Shelter.Type.GROW_ROOM_UNVENTILATED);
        List<String> advice = keys(reading(room, Season.WINTER, 15, room.adjustHumidity(40)));
        assertEquals("grow_room_needs_extractor", advice.getFirst());
        assertTrue(advice.contains("mold_risk"));
    }

    @Test
    void summerGreenhouseWithoutExtractor() {
        Shelter closed = new Shelter(Shelter.Type.GREENHOUSE, false, Shelter.Ventilation.NONE);
        assertEquals("greenhouse_needs_extractor_summer", keys(reading(closed, Season.SUMMER, 28, 70)).getFirst());
        assertEquals("greenhouse_extractor_recommended", keys(reading(closed, Season.WINTER, 15, 70)).getFirst());
        MicroClimate snowySummer = new MicroClimate(15, 70, closed, Season.SUMMER, true, false);
        assertEquals("greenhouse_extractor_recommended", keys(snowySummer).getFirst());
    }

    @Test
    void darkCaveNeedsGrowLight() {
        MicroClimate cave = new MicroClimate(12, 60, Shelter.OPEN, Season.SPRING, false, true);
        assertEquals("needs_grow_light", keys(cave).getFirst());
    }

    @Test
    void frostAndDryAir() {
        List<String> advice = keys(reading(Shelter.OPEN, Season.WINTER, -3, 20));
        assertTrue(advice.contains("frost") && advice.contains("dry"));
    }

    @Test
    void bigGreenhouseNeedsMoreExtractors() {
        Shelter big = new Shelter(Shelter.Type.GREENHOUSE, false, new Shelter.Ventilation(1, 50));
        assertEquals(3, big.ventilation().needed());
        assertEquals(1f / 3, big.ventilationRatio(), 1e-6);
        List<ClimateAdvice.Advice> advice = ClimateAdvice.advise(reading(big, Season.SPRING, 20, big.adjustHumidity(60)));
        assertEquals(ClimateAdvice.Advice.of("not_enough_extractors", 50, 1, 3), advice.getFirst());
    }

    @Test
    void humidityScalesWithVentilation() {
        Shelter none = new Shelter(Shelter.Type.GREENHOUSE, false, new Shelter.Ventilation(0, 40));
        Shelter half = new Shelter(Shelter.Type.GREENHOUSE, false, new Shelter.Ventilation(1, 40));
        Shelter full = new Shelter(Shelter.Type.GREENHOUSE, false, new Shelter.Ventilation(2, 40));
        double outside = 65;
        assertTrue(none.adjustHumidity(outside) > half.adjustHumidity(outside));
        assertTrue(half.adjustHumidity(outside) > full.adjustHumidity(outside));
        assertTrue(Mold.atRisk(none, none.adjustHumidity(outside)), "closed greenhouse full of plants grows mold");
        assertTrue(!Mold.atRisk(full, full.adjustHumidity(outside)), "enough extractors keep mold away");
        assertTrue(!Mold.atRisk(Shelter.OPEN, 99), "no mold outdoors");

        Shelter small = new Shelter(Shelter.Type.GREENHOUSE, false, new Shelter.Ventilation(0, 3));
        assertTrue(!Mold.atRisk(small, small.adjustHumidity(outside)), "3 plants barely add humidity");
    }
}
