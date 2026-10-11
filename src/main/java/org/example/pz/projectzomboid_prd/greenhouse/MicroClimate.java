package org.example.pz.projectzomboid_prd.greenhouse;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import org.example.pz.projectzomboid_prd.season.ClimateModel;
import org.example.pz.projectzomboid_prd.season.Season;

/**
 * What a thermo-hygrometer reads at a position: temperature and humidity felt there, the kind of place, and the
 * facts the advice needs. Works on client and server levels.
 *
 * @param undergroundWithoutLight under a roof, below the surface, with no grow light nearby
 */
public record MicroClimate(double temperature, double humidity, Shelter shelter, Season season,
                           boolean snowyBiome, boolean undergroundWithoutLight) {

    public static MicroClimate measure(Level level, BlockPos pos, ClimateModel climate, long ticks) {
        Biome biome = level.getBiome(pos).value();
        float biomeTemperature = biome.getBaseTemperature();
        Shelter shelter = ShelterDetector.detect(level, pos);

        double temperature = shelter.adjust(climate.temperatureAt(biomeTemperature, pos.getY(), ticks));
        double humidity = humidity(level, pos, climate, ticks, shelter);
        boolean dark = shelter.type() == Shelter.Type.OPEN
                && ShelterDetector.isUnderground(level, pos) && !ShelterDetector.hasGrowLight(level, pos);
        return new MicroClimate(temperature, humidity, shelter, climate.calendar().dateAt(ticks).season(),
                biomeTemperature <= Shelter.SNOWY_BIOME_TEMPERATURE, dark);
    }

    /** Relative humidity (%) felt at {@code pos} inside the given shelter. */
    public static double humidity(Level level, BlockPos pos, ClimateModel climate, long ticks, Shelter shelter) {
        Biome biome = level.getBiome(pos).value();
        float downfall = biome.climateSettings.downfall(); // made accessible in projectzomboid_prd.classtweaker
        return shelter.adjustHumidity(climate.humidityAt(downfall, biome.hasPrecipitation(), ticks));
    }
}
