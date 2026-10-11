package org.example.pz.projectzomboid_prd.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** Holds one batch (a single stack of up to {@link #CAPACITY} items) that is processed over time. */
public abstract class ProcessingBlockEntity extends BlockEntity {
    public static final int CAPACITY = 16;
    protected ItemStack contents = ItemStack.EMPTY;

    protected ProcessingBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public ItemStack contents() {
        return contents;
    }

    /** Whether {@code stack} can start a batch here. */
    protected abstract boolean accepts(ItemStack stack);

    /** Whether more of the same item can still join the current batch. */
    protected abstract boolean canTopUp();

    /** Called when a new batch starts. */
    protected abstract void startBatch();

    /** The block state that shows the current contents. */
    protected abstract BlockState visualState(BlockState state);

    /** Moves as many items as fit from {@code stack} into the batch. */
    public boolean insert(ItemStack stack) {
        if (contents.isEmpty()) {
            if (!accepts(stack)) {
                return false;
            }
            contents = stack.split(CAPACITY);
            startBatch();
        } else if (ItemStack.isSameItemSameComponents(contents, stack) && canTopUp() && contents.getCount() < CAPACITY) {
            int moved = Math.min(stack.getCount(), CAPACITY - contents.getCount());
            contents.grow(moved);
            stack.shrink(moved);
        } else {
            return false;
        }
        changed();
        return true;
    }

    /** Empties the block and returns what was inside, as it is now. */
    public ItemStack takeOut() {
        ItemStack taken = contents;
        contents = ItemStack.EMPTY;
        changed();
        return taken;
    }

    /** Saves and updates the block's look. */
    protected void changed() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            BlockState updated = visualState(getBlockState());
            if (updated != getBlockState()) {
                level.setBlock(worldPosition, updated, 3);
            }
        }
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level != null && !contents.isEmpty()) {
            Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, contents);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        contents = input.read("contents", ItemStack.CODEC).orElse(ItemStack.EMPTY);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (!contents.isEmpty()) {
            output.store("contents", ItemStack.CODEC, contents);
        }
    }
}
