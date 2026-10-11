package org.example.pz.projectzomboid_prd.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.example.pz.projectzomboid_prd.registry.ModItems;

/**
 * Compost bin: filled like a vanilla composter (any compostable item, including this mod's produce and seeds),
 * but when ready it yields compost for the soil instead of bone meal.
 */
public class CompostBinBlock extends ComposterBlock {

    public CompostBinBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                          InteractionHand hand, BlockHitResult hit) {
        if (state.getValue(LEVEL) == READY) {
            return harvest(state, level, pos);
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (state.getValue(LEVEL) == READY) {
            return harvest(state, level, pos);
        }
        return super.useWithoutItem(state, level, pos, player, hit);
    }

    /** Hoppers can fill the bin but not take vanilla bone meal out of it. */
    @Override
    public WorldlyContainer getContainer(BlockState state, LevelAccessor level, BlockPos pos) {
        return state.getValue(LEVEL) == READY ? new Sealed() : super.getContainer(state, level, pos);
    }

    private static InteractionResult harvest(BlockState state, Level level, BlockPos pos) {
        if (!level.isClientSide()) {
            Block.popResourceFromFace(level, pos, Direction.UP, new ItemStack(ModItems.COMPOST, 2));
            level.setBlock(pos, state.setValue(LEVEL, 0), Block.UPDATE_ALL);
            level.playSound(null, pos, SoundEvents.COMPOSTER_EMPTY, SoundSource.BLOCKS, 1.0f, 1.0f);
        }
        return InteractionResult.SUCCESS;
    }

    private static final class Sealed extends SimpleContainer implements WorldlyContainer {
        Sealed() {
            super(0);
        }

        @Override
        public int[] getSlotsForFace(Direction direction) {
            return new int[0];
        }

        @Override
        public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction direction) {
            return false;
        }

        @Override
        public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
            return false;
        }
    }
}
