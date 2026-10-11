package org.example.pz.projectzomboid_prd.mixin;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.level.Level;
import org.example.pz.projectzomboid_prd.food.DietTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Tells the diet tracker about every food a player finishes eating. */
@Mixin(FoodProperties.class)
public abstract class FoodPropertiesMixin {

    @Inject(method = "onConsume", at = @At("TAIL"))
    private void projectzomboid_prd$trackDiet(Level level, LivingEntity entity, ItemStack stack, Consumable consumable, CallbackInfo ci) {
        if (entity instanceof ServerPlayer player) {
            DietTracker.onEat(player, stack);
        }
    }
}
