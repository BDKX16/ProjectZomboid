package org.example.pz.projectzomboid_prd.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.example.pz.projectzomboid_prd.soil.SoilData;
import org.example.pz.projectzomboid_prd.soil.SoilManager;
import org.jspecify.annotations.Nullable;

import java.util.function.UnaryOperator;

/**
 * Something spread on farmland (compost, mulch). Works on the farmland itself or on a crop growing on it, and is
 * used up.
 */
public class SoilAmendmentItem extends DescribedItem {
    private final UnaryOperator<SoilData> effect;
    private final SoundEvent sound;

    public SoilAmendmentItem(Properties properties, UnaryOperator<SoilData> effect, SoundEvent sound) {
        super(properties);
        this.effect = effect;
        this.sound = sound;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos soil = farmland(level, context.getClickedPos());
        if (soil == null) {
            return InteractionResult.PASS;
        }
        if (level instanceof ServerLevel serverLevel) {
            SoilManager.update(level, soil, effect);
            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, soil.getX() + 0.5, soil.getY() + 1.05, soil.getZ() + 0.5,
                    8, 0.3, 0.05, 0.3, 0.0);
            level.playSound(null, soil, sound, SoundSource.BLOCKS, 1.0f, 1.0f);
            if (context.getPlayer() != null) {
                context.getItemInHand().consume(1, context.getPlayer());
            }
        }
        return InteractionResult.SUCCESS;
    }

    /** The farmland at {@code pos}, or under the crop at {@code pos}; null if neither. */
    public static @Nullable BlockPos farmland(Level level, BlockPos pos) {
        if (isFarmland(level.getBlockState(pos))) {
            return pos;
        }
        return isFarmland(level.getBlockState(pos.below())) ? pos.below() : null;
    }

    private static boolean isFarmland(BlockState state) {
        return state.getBlock() instanceof FarmlandBlock;
    }
}
