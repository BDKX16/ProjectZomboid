package io.github.bdkx16.regenerativeagriculture.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.BonemealSource;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import io.github.bdkx16.regenerativeagriculture.crop.CropClimate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Makes every crop with a crop profile (vanilla or ours) obey seasons and temperature. */
@Mixin(CropBlock.class)
public abstract class CropBlockMixin {

    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    private void regenerative_agriculture$seasonalGrowth(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, CallbackInfo ci) {
        if (CropClimate.onRandomTick((CropBlock) (Object) this, state, level, pos, random)) {
            ci.cancel();
        }
    }

    @Inject(method = "performBonemeal", at = @At("HEAD"), cancellable = true)
    private void regenerative_agriculture$seasonalBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state, BonemealSource source, CallbackInfo ci) {
        if (CropClimate.blocksBonemeal((CropBlock) (Object) this, level, pos)) {
            ci.cancel();
        }
    }
}
