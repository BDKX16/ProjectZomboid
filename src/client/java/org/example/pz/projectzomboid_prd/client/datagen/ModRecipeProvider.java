package org.example.pz.projectzomboid_prd.client.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SmokingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import org.example.pz.projectzomboid_prd.registry.ModBlocks;
import org.example.pz.projectzomboid_prd.registry.ModCrops;
import org.example.pz.projectzomboid_prd.registry.ModFoods;
import org.example.pz.projectzomboid_prd.registry.ModItems;

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
                equipment();
                tools();
                soil();
                meals();

                // Produce -> seeds, so a harvest can always be replanted.
                for (ModCrops.Crop crop : ModCrops.ALL) {
                    shapeless(RecipeCategory.MISC, crop.seeds(), crop.seedsPerCraft())
                            .requires(crop.produce())
                            .unlockedBy(getHasName(crop.produce()), has(crop.produce()))
                            .save(output);
                }
            }

            private void equipment() {
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
                shaped(RecipeCategory.DECORATIONS, ModBlocks.WEATHER_STATION)
                        .pattern("CKC")
                        .pattern("GRG")
                        .pattern("III")
                        .define('C', Items.COPPER_INGOT)
                        .define('K', Items.CLOCK)
                        .define('G', Items.GLASS_PANE)
                        .define('R', Items.REDSTONE)
                        .define('I', Items.IRON_INGOT)
                        .unlockedBy(getHasName(Items.CLOCK), has(Items.CLOCK))
                        .save(output);
                shaped(RecipeCategory.MISC, ModBlocks.COMPOST_BIN)
                        .pattern("P P")
                        .pattern("PDP")
                        .pattern("PPP")
                        .define('P', ItemTags.PLANKS)
                        .define('D', Items.DIRT)
                        .unlockedBy(getHasName(Items.DIRT), has(Items.DIRT))
                        .save(output);
            }

            private void tools() {
                shaped(RecipeCategory.TOOLS, ModItems.PRUNING_SHEARS)
                        .pattern("I I")
                        .pattern(" I ")
                        .pattern("S S")
                        .define('I', Items.IRON_INGOT)
                        .define('S', Items.STICK)
                        .unlockedBy(getHasName(Items.IRON_INGOT), has(Items.IRON_INGOT))
                        .save(output);
                shaped(RecipeCategory.TOOLS, ModItems.THERMOHYGROMETER)
                        .pattern("CGC")
                        .pattern("CRC")
                        .pattern("CKC")
                        .define('C', Items.COPPER_INGOT)
                        .define('G', Items.GLASS_PANE)
                        .define('R', Items.REDSTONE)
                        .define('K', Items.COMPASS)
                        .unlockedBy(getHasName(Items.COPPER_INGOT), has(Items.COPPER_INGOT))
                        .save(output);
                shapeless(RecipeCategory.TOOLS, ModItems.SOIL_TEST_KIT)
                        .requires(Items.GLASS_BOTTLE)
                        .requires(Items.PAPER)
                        .requires(Items.REDSTONE)
                        .unlockedBy(getHasName(Items.GLASS_BOTTLE), has(Items.GLASS_BOTTLE))
                        .save(output);
            }

            private void meals() {
                Item lettuce = ModCrops.LETTUCE.produce();
                Item tomato = ModCrops.TOMATO.produce();
                Item basil = ModCrops.BASIL.produce();
                Item sunflower = ModCrops.SUNFLOWER.produce();
                shapeless(RecipeCategory.FOOD, ModFoods.HAMBURGER.item())
                        .requires(Items.BREAD)
                        .requires(Items.COOKED_BEEF)
                        .requires(lettuce)
                        .requires(tomato)
                        .unlockedBy(getHasName(tomato), has(tomato))
                        .save(output);
                shapeless(RecipeCategory.FOOD, ModFoods.GREEN_SALAD.item())
                        .requires(Items.BOWL)
                        .requires(lettuce)
                        .requires(ModCrops.ARUGULA.produce())
                        .requires(tomato)
                        .unlockedBy(getHasName(lettuce), has(lettuce))
                        .save(output);
                shapeless(RecipeCategory.FOOD, ModFoods.STEAMED_BROCCOLI.item())
                        .requires(Items.BOWL)
                        .requires(ModCrops.BROCCOLI.produce(), 2)
                        .requires(basil)
                        .unlockedBy(getHasName(ModCrops.BROCCOLI.produce()), has(ModCrops.BROCCOLI.produce()))
                        .save(output);
                shapeless(RecipeCategory.FOOD, ModFoods.SOY_STEW.item())
                        .requires(Items.BOWL)
                        .requires(ModCrops.SOYBEAN.produce(), 2)
                        .requires(Items.CARROT)
                        .requires(Items.POTATO)
                        .unlockedBy(getHasName(ModCrops.SOYBEAN.produce()), has(ModCrops.SOYBEAN.produce()))
                        .save(output);
                shapeless(RecipeCategory.FOOD, ModFoods.SQUASH_SOUP.item())
                        .requires(Items.BOWL)
                        .requires(ModCrops.SQUASH.produce())
                        .requires(Items.POTATO)
                        .unlockedBy(getHasName(ModCrops.SQUASH.produce()), has(ModCrops.SQUASH.produce()))
                        .save(output);
                shapeless(RecipeCategory.FOOD, ModFoods.PESTO_PASTA.item())
                        .requires(Items.BOWL)
                        .requires(Items.WHEAT, 2)
                        .requires(basil, 2)
                        .requires(sunflower)
                        .unlockedBy(getHasName(basil), has(basil))
                        .save(output);
                shapeless(RecipeCategory.FOOD, ModFoods.TOMATO_JUICE.item())
                        .requires(Items.GLASS_BOTTLE)
                        .requires(tomato, 2)
                        .unlockedBy(getHasName(tomato), has(tomato))
                        .save(output);
                // Sunflower heads roast like any other food: furnace, smoker or campfire.
                Item roasted = ModFoods.ROASTED_SUNFLOWER_SEEDS.item();
                SimpleCookingRecipeBuilder.smelting(Ingredient.of(sunflower), RecipeCategory.FOOD, CookingBookCategory.FOOD, roasted, 0.35f, 200)
                        .unlockedBy(getHasName(sunflower), has(sunflower))
                        .save(output);
                simpleCookingRecipe("smoking", SmokingRecipe::new, 100, sunflower, roasted, 0.35f);
                simpleCookingRecipe("campfire_cooking", CampfireCookingRecipe::new, 600, sunflower, roasted, 0.35f);
            }

            private void soil() {
                shapeless(RecipeCategory.MISC, ModItems.STRAW_MULCH, 2)
                        .requires(Items.WHEAT, 3)
                        .unlockedBy(getHasName(Items.WHEAT), has(Items.WHEAT))
                        .save(output);
            }
        };
    }

    @Override
    public String getName() {
        return "Recipes";
    }
}
