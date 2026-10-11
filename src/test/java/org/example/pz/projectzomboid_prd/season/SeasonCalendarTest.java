package org.example.pz.projectzomboid_prd.season;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SeasonCalendarTest {
    private final SeasonCalendar calendar = new SeasonCalendar(15);

    @Test
    void firstDayIsFirstOfSeptemberInSpring() {
        SeasonCalendar.Date date = calendar.dateOfDay(0);
        assertEquals(Season.SPRING, date.season());
        assertEquals(1, date.dayOfSeason());
        assertEquals(8, date.month());
        assertEquals(1, date.dayOfMonth());
        assertEquals(1, date.year());
    }

    @Test
    void seasonsFollowSouthernHemisphere() {
        assertEquals(Season.SUMMER, calendar.dateOfDay(15).season());
        assertEquals(11, calendar.dateOfDay(15).month()); // December
        assertEquals(Season.AUTUMN, calendar.dateOfDay(30).season());
        assertEquals(2, calendar.dateOfDay(30).month()); // March
        assertEquals(Season.WINTER, calendar.dateOfDay(45).season());
        assertEquals(5, calendar.dateOfDay(45).month()); // June
        assertEquals(15, calendar.dateOfDay(59).dayOfSeason());
    }

    @Test
    void yearWrapsAround() {
        SeasonCalendar.Date date = calendar.dateOfDay(60);
        assertEquals(Season.SPRING, date.season());
        assertEquals(2, date.year());
        assertEquals(1, date.dayOfYear() + 1);
    }

    @Test
    void monthsHaveFiveDaysWithFifteenDaySeasons() {
        assertEquals(5, calendar.dateOfDay(4).dayOfMonth());
        assertEquals(1, calendar.dateOfDay(5).dayOfMonth());
        assertEquals(9, calendar.dateOfDay(5).month()); // October
    }

    @Test
    void daysChangeAtMidnight() {
        // Tick 0 is 06:00 of day 0; tick 18000 is 00:00 of day 1.
        assertEquals(0, SeasonCalendar.absoluteDay(17999));
        assertEquals(1, SeasonCalendar.absoluteDay(18000));
        assertEquals(6, SeasonCalendar.hour(0));
        assertEquals(30, SeasonCalendar.minute(500));
        assertEquals(18000, SeasonCalendar.startOfDay(1));
    }

    @Test
    void nextStartOfSeason() {
        assertEquals(15, calendar.nextStartOf(Season.SUMMER, 3));
        assertEquals(60, calendar.nextStartOf(Season.SPRING, 1));
        assertEquals(0, calendar.nextStartOf(Season.SPRING, 0));
    }
}
