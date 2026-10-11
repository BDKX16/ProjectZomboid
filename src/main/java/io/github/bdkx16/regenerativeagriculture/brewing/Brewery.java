package io.github.bdkx16.regenerativeagriculture.brewing;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import io.github.bdkx16.regenerativeagriculture.block.entity.BrewKettleBlockEntity;
import io.github.bdkx16.regenerativeagriculture.block.entity.FermenterBlockEntity;
import io.github.bdkx16.regenerativeagriculture.block.entity.MashTunBlockEntity;
import io.github.bdkx16.regenerativeagriculture.registry.ModTags;

import java.util.Optional;

/**
 * A microbrewery is a mash tun, a brew kettle and a fermenter standing side by side: the kettle in the middle,
 * touching the other two. Only a complete one works; wort flows mash tun → kettle → fermenter on its own.
 */
public final class Brewery {

    private Brewery() {
    }

    /** The kettle at {@code pos} or right next to it. */
    public static Optional<BrewKettleBlockEntity> kettleAt(Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof BrewKettleBlockEntity kettle) {
            return Optional.of(kettle);
        }
        return neighbour(level, pos, BrewKettleBlockEntity.class);
    }

    public static <T extends BlockEntity> Optional<T> neighbour(Level level, BlockPos pos, Class<T> type) {
        for (Direction side : Direction.Plane.HORIZONTAL) {
            BlockEntity entity = level.getBlockEntity(pos.relative(side));
            if (type.isInstance(entity)) {
                return Optional.of(type.cast(entity));
            }
        }
        return Optional.empty();
    }

    /** Whether the machine at {@code pos} belongs to a complete microbrewery. */
    public static boolean complete(Level level, BlockPos pos) {
        return kettleAt(level, pos).map(kettle -> {
            BlockPos at = kettle.getBlockPos();
            return neighbour(level, at, MashTunBlockEntity.class).isPresent()
                    && neighbour(level, at, FermenterBlockEntity.class).isPresent();
        }).orElse(false);
    }

    /** A lit fire right under the kettle: campfire, fire, lava, magma or a lit furnace. */
    public static boolean heated(Level level, BlockPos kettle) {
        BlockState below = level.getBlockState(kettle.below());
        if (below.is(net.minecraft.tags.BlockTags.FIRE)) {
            return true;
        }
        if (!below.is(ModTags.HEAT_SOURCES)) {
            return false;
        }
        return !below.hasProperty(BlockStateProperties.LIT) || below.getValue(BlockStateProperties.LIT);
    }
}
