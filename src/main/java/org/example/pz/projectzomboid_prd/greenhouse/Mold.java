package org.example.pz.projectzomboid_prd.greenhouse;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import org.example.pz.projectzomboid_prd.registry.ModBlocks;
import org.example.pz.projectzomboid_prd.season.SeasonManager;

/**
 * Fungal disease in closed, muggy spaces: crops in a greenhouse or grow room above {@value #HUMIDITY}% humidity can
 * turn into a moldy crop, which keeps infecting its neighbours while the air stays that humid.
 */
public final class Mold {
    public static final double HUMIDITY = 85;
    /** Chance per random tick that a crop at risk gets infected. */
    private static final float INFECT_CHANCE = 0.05f;
    /** Chance per random tick that a moldy crop infects one neighbour at risk. */
    private static final float SPREAD_CHANCE = 0.3f;

    private Mold() {
    }

    public static boolean atRisk(Shelter shelter, double humidity) {
        return shelter.enclosed() && humidity > HUMIDITY;
    }

    /** @return true if the crop at {@code pos} got infected (and was replaced) */
    public static boolean tryInfect(ServerLevel level, BlockPos pos, Shelter shelter, RandomSource random) {
        if (random.nextFloat() >= INFECT_CHANCE || !atRisk(shelter, humidity(level, pos, shelter))) {
            return false;
        }
        level.setBlock(pos, ModBlocks.MOLDY_CROP.defaultBlockState(), Block.UPDATE_ALL);
        return true;
    }

    /** Called on the moldy crop's random tick. */
    public static void spread(ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextFloat() >= SPREAD_CHANCE) {
            return;
        }
        BlockPos target = pos.relative(Direction.Plane.HORIZONTAL.getRandomDirection(random));
        if (level.getBlockState(target).getBlock() instanceof CropBlock) {
            Shelter shelter = ShelterDetector.detect(level, target);
            if (atRisk(shelter, humidity(level, target, shelter))) {
                level.setBlock(target, ModBlocks.MOLDY_CROP.defaultBlockState(), Block.UPDATE_ALL);
            }
        }
    }

    private static double humidity(ServerLevel level, BlockPos pos, Shelter shelter) {
        return MicroClimate.humidity(level, pos, SeasonManager.climate(level.getServer()), SeasonManager.ticks(level.getServer()), shelter);
    }
}
