package org.example.pz.projectzomboid_prd.greenhouse;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import org.example.pz.projectzomboid_prd.registry.ModBlocks;
import org.example.pz.projectzomboid_prd.registry.ModTags;

/**
 * Looks at the blocks around a crop to decide whether it is outdoors, in a greenhouse or in an underground grow room.
 *
 * <p>Greenhouse: the first block above the crop (within {@value #ROOF_RANGE}) is glass, and in all four directions
 * a wall is found within {@value #WALL_RANGE} blocks. It is ventilated if an extractor with a vent shaft is nearby.
 *
 * <p>Grow room: no sky above (not a glass roof), a grow light within 2 blocks sideways and 1-5 above, and an
 * extractor within {@value #EXTRACTOR_RANGE} blocks whose shaft of air (or vent-passable blocks) reaches the surface.
 */
public final class ShelterDetector {
    private static final int ROOF_RANGE = 12;
    private static final int WALL_RANGE = 8;
    private static final int HEATER_RANGE = 4;
    private static final int LIGHT_RANGE_XZ = 2;
    private static final int LIGHT_RANGE_UP = 5;
    private static final int EXTRACTOR_RANGE = 8;
    private static final int EXTRACTOR_BELOW = 2;
    private static final int EXTRACTOR_ABOVE = 5;
    private static final int MAX_SHAFT = 96;

    private ShelterDetector() {
    }

    public static Shelter detect(Level level, BlockPos crop) {
        boolean glassRoof = hasGlassRoof(level, crop);
        if (!glassRoof && isUnderground(level, crop) && hasGrowLight(level, crop)) {
            return isVentilated(level, crop)
                    ? new Shelter(Shelter.Type.GROW_ROOM, false, true)
                    : Shelter.of(Shelter.Type.GROW_ROOM_UNVENTILATED);
        }
        if (glassRoof && hasWalls(level, crop)) {
            if (level.getBiome(crop).is(ModTags.GREENHOUSE_OVERHEATS)) {
                return Shelter.of(Shelter.Type.GREENHOUSE_OVERHEATED);
            }
            return new Shelter(Shelter.Type.GREENHOUSE, hasHeater(level, crop), isVentilated(level, crop));
        }
        return Shelter.OPEN;
    }

    private static boolean hasGlassRoof(Level level, BlockPos crop) {
        BlockPos.MutableBlockPos pos = crop.mutable();
        for (int i = 0; i < ROOF_RANGE; i++) {
            BlockState state = level.getBlockState(pos.move(Direction.UP));
            if (!state.isAir() && !state.is(ModBlocks.GROW_LIGHT)) {
                return state.is(ModTags.GREENHOUSE_GLASS);
            }
        }
        return false;
    }

    private static boolean hasWalls(Level level, BlockPos crop) {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (!hasWall(level, crop, direction) && !hasWall(level, crop.above(), direction)) {
                return false;
            }
        }
        return true;
    }

    private static boolean hasWall(Level level, BlockPos from, Direction direction) {
        BlockPos.MutableBlockPos pos = from.mutable();
        for (int i = 0; i < WALL_RANGE; i++) {
            BlockState state = level.getBlockState(pos.move(direction));
            if (!state.isAir() && !state.canBeReplaced()) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasHeater(Level level, BlockPos crop) {
        for (BlockPos pos : BlockPos.betweenClosed(crop.offset(-HEATER_RANGE, -1, -HEATER_RANGE), crop.offset(HEATER_RANGE, 2, HEATER_RANGE))) {
            if (level.getBlockState(pos).is(ModTags.HEAT_SOURCES)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isUnderground(Level level, BlockPos crop) {
        return crop.getY() + 1 < level.getHeight(Heightmap.Types.WORLD_SURFACE, crop.getX(), crop.getZ());
    }

    public static boolean hasGrowLight(Level level, BlockPos crop) {
        for (BlockPos pos : BlockPos.betweenClosed(crop.offset(-LIGHT_RANGE_XZ, 1, -LIGHT_RANGE_XZ), crop.offset(LIGHT_RANGE_XZ, LIGHT_RANGE_UP, LIGHT_RANGE_XZ))) {
            if (level.getBlockState(pos).is(ModBlocks.GROW_LIGHT)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isVentilated(Level level, BlockPos crop) {
        for (BlockPos pos : BlockPos.betweenClosed(crop.offset(-EXTRACTOR_RANGE, -EXTRACTOR_BELOW, -EXTRACTOR_RANGE), crop.offset(EXTRACTOR_RANGE, EXTRACTOR_ABOVE, EXTRACTOR_RANGE))) {
            if (level.getBlockState(pos).is(ModBlocks.EXTRACTOR) && hasVentShaft(level, pos)) {
                return true;
            }
        }
        return false;
    }

    /** The column above the extractor must be open (air or vent-passable blocks) all the way to the surface. */
    public static boolean hasVentShaft(Level level, BlockPos extractor) {
        int surface = level.getHeight(Heightmap.Types.WORLD_SURFACE, extractor.getX(), extractor.getZ());
        BlockPos.MutableBlockPos pos = extractor.mutable();
        for (int i = 0; i < MAX_SHAFT; i++) {
            pos.move(Direction.UP);
            if (pos.getY() >= surface) {
                return true;
            }
            BlockState state = level.getBlockState(pos);
            if (!state.isAir() && !state.is(ModTags.VENT_PASSABLE)) {
                return false;
            }
        }
        return false;
    }
}
