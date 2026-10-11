package org.example.pz.projectzomboid_prd.mixin;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import org.example.pz.projectzomboid_prd.processing.Quality;
import org.example.pz.projectzomboid_prd.registry.ModComponents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Something crafted from cured ingredients keeps their quality (the lowest one, if they differ). */
@Mixin(ShapelessRecipe.class)
public abstract class ShapelessRecipeMixin {

    @Inject(method = "assemble(Lnet/minecraft/world/item/crafting/CraftingInput;)Lnet/minecraft/world/item/ItemStack;", at = @At("RETURN"))
    private void projectzomboid_prd$keepQuality(CraftingInput input, CallbackInfoReturnable<ItemStack> cir) {
        int quality = 0;
        for (ItemStack ingredient : input.items()) {
            int q = Quality.of(ingredient);
            if (q > 0) {
                quality = quality == 0 ? q : Math.min(quality, q);
            }
        }
        ItemStack result = cir.getReturnValue();
        if (quality > 0 && !result.isEmpty()) {
            result.set(ModComponents.QUALITY, quality);
        }
    }
}
