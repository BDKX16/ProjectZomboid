package org.example.pz.projectzomboid_prd.crop;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.example.pz.projectzomboid_prd.greenhouse.Mold;
import org.example.pz.projectzomboid_prd.greenhouse.Shelter;
import org.example.pz.projectzomboid_prd.greenhouse.ShelterDetector;
import org.example.pz.projectzomboid_prd.season.SeasonManager;
import org.example.pz.projectzomboid_prd.soil.SoilData;
import org.example.pz.projectzomboid_prd.soil.SoilManager;
import org.example.pz.projectzomboid_prd.soil.SoilRules;

import java.util.Optional;

/** Applies seasons, climate, mold and soil to crops in a world. Called from {@code CropBlockMixin}. */
public final class CropClimate {
    /** Chance per random tick that frost or heat sets a stressed crop back one stage. */
    private static final float DAMAGE_CHANCE = 0.25f;

    private CropClimate() {
    }

    public static Optional<GrowthCalculator.Growth> growth(ServerLevel level, BlockPos pos, Block block) {
        return CropProfiles.get(level.registryAccess(), block)
                .map(profile -> evaluate(level, pos, profile, ShelterDetector.detect(level, pos)));
    }

    private static GrowthCalculator.Growth evaluate(ServerLevel level, BlockPos pos, CropProfile profile, Shelter shelter) {
        return GrowthCalculator.evaluate(
                profile.requirements(),
                SeasonManager.climate(level.getServer()),
                level.getBiome(pos).value().getBaseTemperature(),
                pos.getY(),
                SeasonManager.ticks(level.getServer()),
                shelter);
    }

    /**
     * Runs before vanilla growth.
     *
     * @return true if vanilla growth should be skipped this tick
     */
    public static boolean onRandomTick(CropBlock crop, BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        Optional<CropProfile> profile = CropProfiles.get(level.registryAccess(), crop);
        if (profile.isEmpty()) {
            return false;
        }
        Shelter shelter = ShelterDetector.detect(level, pos);
        if (Mold.tryInfect(level, pos, shelter, random)) {
            return true;
        }
        GrowthCalculator.Growth growth = evaluate(level, pos, profile.get(), shelter);
        SoilData soil = SoilManager.get(level, pos.below());
        if (growth.damaging()) {
            if (!protectedByMulch(level, pos, shelter, growth, soil) && random.nextFloat() < DAMAGE_CHANCE) {
                damage(crop, state, level, pos);
            }
            return true;
        }
        float factor = growth.factor() * soilMultiplier(level, pos, profile.get(), soil);
        return random.nextFloat() >= factor;
    }

    /** Growth multiplier from the soil below and the crops around. */
    public static float soilMultiplier(ServerLevel level, BlockPos pos, CropProfile profile, SoilData soil) {
        return SoilRules.multiplier(soil, profile.family(), SoilManager.neighbourhood(level, pos, profile));
    }

    /** Mulch keeps the soil warm enough to shrug off a light frost. */
    private static boolean protectedByMulch(ServerLevel level, BlockPos pos, Shelter shelter, GrowthCalculator.Growth growth, SoilData soil) {
        if (growth.stress() != GrowthCalculator.Stress.FROST || soil.mulch() <= 0) {
            return false;
        }
        double now = shelter.adjust(SeasonManager.climate(level.getServer()).temperatureAt(
                level.getBiome(pos).value().getBaseTemperature(), pos.getY(), SeasonManager.ticks(level.getServer())));
        return now >= SoilRules.MULCH_FROST_PROTECTION;
    }

    /** @return true if bone meal should have no effect */
    public static boolean blocksBonemeal(CropBlock crop, ServerLevel level, BlockPos pos) {
        return growth(level, pos, crop).map(growth -> growth.factor() <= 0).orElse(false);
    }

    private static void damage(CropBlock crop, BlockState state, ServerLevel level, BlockPos pos) {
        int age = crop.getAge(state);
        if (age > 0) {
            level.setBlock(pos, crop.getStateForAge(age - 1), Block.UPDATE_CLIENTS);
        } else {
            level.removeBlock(pos, false);
        }
    }
}
