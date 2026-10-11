package io.github.bdkx16.regenerativeagriculture.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockState;
import io.github.bdkx16.regenerativeagriculture.soil.SoilManager;
import org.jspecify.annotations.Nullable;

/**
 * Farmland covered with straw mulch: the visible side of {@code SoilData.mulch() > 0}. {@link SoilManager} swaps
 * plain farmland for this block (and back) whenever the soil data changes. Trampled or dried out, it turns to dirt
 * like farmland and the mulch is lost.
 */
public class MulchedFarmlandBlock extends FarmlandBlock {

    public MulchedFarmlandBlock(Properties properties) {
        super(Blocks.DIRT, properties);
    }

    @Override
    public void turnToBaseBlock(@Nullable Entity entity, BlockState state, Level level, BlockPos pos) {
        if (!level.isClientSide()) {
            SoilManager.update(level, pos, soil -> soil.withMulch(0));
        }
        super.turnToBaseBlock(entity, state, level, pos);
    }
}
