package io.github.bdkx16.regenerativeagriculture.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import io.github.bdkx16.regenerativeagriculture.greenhouse.Mold;

/** A crop killed by mold. Yields nothing and infects neighbouring crops while the air stays muggy. */
public class MoldyCropBlock extends VegetationBlock {
    private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 10, 14);

    public MoldyCropBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        Mold.spread(level, pos, random);
    }
}
