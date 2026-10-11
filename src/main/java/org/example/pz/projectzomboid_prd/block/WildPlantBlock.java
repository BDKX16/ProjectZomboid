package org.example.pz.projectzomboid_prd.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Wild version of a crop. Only appears through world generation (it has no item, so players cannot place it)
 * and is the natural source of seeds. Grows on grass and dirt like other vegetation.
 */
public class WildPlantBlock extends VegetationBlock {
    private static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 13, 14);

    public WildPlantBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}
