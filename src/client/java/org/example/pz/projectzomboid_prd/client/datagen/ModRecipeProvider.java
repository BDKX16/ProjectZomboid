package org.example.pz.projectzomboid_prd.client.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.item.crafting.Recipe;
import org.example.pz.projectzomboid_prd.registry.ModCrops;

import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends FabricRecipeProvider {

    public ModRecipeProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, BootstrapContext<Recipe<?>> recipes,
                                                  BootstrapContext<Advancement> advancements) {
        return new RecipeProvider(recipes, advancements) {
            @Override
            public void buildRecipes() {
                // Produce → seeds, so a harvest can always be replanted.
                for (ModCrops.Crop crop : ModCrops.ALL) {
                    shapeless(RecipeCategory.MISC, crop.seeds(), crop.seedsPerCraft())
                            .requires(crop.produce())
                            .unlockedBy(getHasName(crop.produce()), has(crop.produce()))
                            .save(output);
                }
            }
        };
    }

    @Override
    public String getName() {
        return "Recipes";
    }
}
