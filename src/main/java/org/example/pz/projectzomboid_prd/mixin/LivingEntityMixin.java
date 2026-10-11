package org.example.pz.projectzomboid_prd.mixin;

import net.minecraft.world.entity.LivingEntity;
import org.example.pz.projectzomboid_prd.effect.ModEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** "Warmth" (squash soup) keeps powder snow from freezing the entity. */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Inject(method = "canFreeze", at = @At("HEAD"), cancellable = true)
    private void projectzomboid_prd$warmth(CallbackInfoReturnable<Boolean> cir) {
        if (((LivingEntity) (Object) this).hasEffect(ModEffects.WARMTH)) {
            cir.setReturnValue(false);
        }
    }
}
