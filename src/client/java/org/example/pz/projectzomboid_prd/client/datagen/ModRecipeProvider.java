package org.example.pz.projectzomboid_prd.client.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import org.example.pz.projectzomboid_prd.registry.ModBlocks;
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
                shaped(RecipeCategory.DECORATIONS, ModBlocks.GROW_LIGHT)
                        .pattern("IRI")
                        .pattern("GLG")
                        .define('I', Items.IRON_INGOT)
                        .define('R', Items.REDSTONE)
                        .define('G', Items.GLASS_PANE)
                        .define('L', Items.GLOWSTONE)
                        .unlockedBy(getHasName(Items.GLOWSTONE), has(Items.GLOWSTONE))
                        .save(output);
                shaped(RecipeCategory.DECORATIONS, ModBlocks.EXTRACTOR)
                        .pattern("IBI")
                        .pattern("RCR")
                        .pattern("IBI")
                        .define('I', Items.IRON_INGOT)
                        .define('B', Items.IRON_BARS)
                        .define('R', Items.REDSTONE)
                        .define('C', Items.COPPER_INGOT)
                        .unlockedBy(getHasName(Items.IRON_INGOT), has(Items.IRON_INGOT))
                        .save(output);

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
