package io.github.bdkx16.regenerativeagriculture.mixin;

import net.minecraft.world.entity.LivingEntity;
import io.github.bdkx16.regenerativeagriculture.effect.ModEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** "Warmth" (squash soup) keeps powder snow from freezing the entity. */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Inject(method = "canFreeze", at = @At("HEAD"), cancellable = true)
    private void regenerative_agriculture$warmth(CallbackInfoReturnable<Boolean> cir) {
        if (((LivingEntity) (Object) this).hasEffect(ModEffects.WARMTH)) {
            cir.setReturnValue(false);
        }
    }
}
