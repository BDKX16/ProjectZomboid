package org.example.pz.projectzomboid_prd.crop;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.example.pz.projectzomboid_prd.season.SeasonManager;

import java.util.Optional;

/** Applies seasons and climate to crops in a world. Called from {@code CropBlockMixin}. */
public final class CropClimate {
    /** Chance per random tick that frost or heat sets a stressed crop back one stage. */
    private static final float DAMAGE_CHANCE = 0.25f;

    private CropClimate() {
    }

    public static Optional<GrowthCalculator.Growth> growth(ServerLevel level, BlockPos pos, Block block) {
        return CropProfiles.get(level.registryAccess(), block).map(profile -> GrowthCalculator.evaluate(
                profile.requirements(),
                SeasonManager.climate(level.getServer()),
                level.getBiome(pos).value().getBaseTemperature(),
                pos.getY(),
                SeasonManager.ticks(level.getServer())));
    }

    /**
     * Runs before vanilla growth.
     *
     * @return true if vanilla growth should be skipped this tick
     */
    public static boolean onRandomTick(CropBlock crop, BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        Optional<GrowthCalculator.Growth> growth = growth(level, pos, crop);
        if (growth.isEmpty()) {
            return false;
        }
        if (growth.get().damaging()) {
            if (random.nextFloat() < DAMAGE_CHANCE) {
                damage(crop, state, level, pos);
            }
            return true;
        }
        return random.nextFloat() >= growth.get().factor();
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
