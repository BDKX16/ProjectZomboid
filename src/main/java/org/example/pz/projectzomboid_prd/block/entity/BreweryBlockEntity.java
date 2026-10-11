package org.example.pz.projectzomboid_prd.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.example.pz.projectzomboid_prd.block.BreweryBlock;
import org.example.pz.projectzomboid_prd.brewing.BrewStage;

/** One machine of the microbrewery. Its {@link BreweryBlock#STAGE} shows what it is doing. */
public abstract class BreweryBlockEntity extends BlockEntity {

    protected BreweryBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /** Runs every {@link BreweryBlock#TICK_INTERVAL} ticks on the server. */
    public abstract void serverTick(ServerLevel level);

    /** Right click holding {@code stack}; PASS if this machine has nothing to do with it. */
    public abstract InteractionResult use(ItemStack stack, Player player);

    /** One line about what the machine is doing, shown on an empty-hand click. */
    public abstract Component status();

    /** What the block shows now. */
    protected abstract BrewStage stage();

    /** Saves and updates the block's look. */
    protected void changed() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            BlockState state = getBlockState();
            if (state.hasProperty(BreweryBlock.STAGE) && state.getValue(BreweryBlock.STAGE) != stage()) {
                level.setBlock(worldPosition, state.setValue(BreweryBlock.STAGE, stage()), 3);
            }
        }
    }

    protected void sound(SoundEvent sound) {
        if (level != null) {
            level.playSound(null, worldPosition, sound, SoundSource.BLOCKS, 1.0f, 1.0f);
        }
    }

    /** Uses up items, except in creative. */
    protected static void consume(ItemStack stack, Player player, int count) {
        if (!player.getAbilities().instabuild) {
            stack.shrink(count);
        }
    }
}
