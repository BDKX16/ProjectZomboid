package io.github.bdkx16.regenerativeagriculture.client.datagen;

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
import io.github.bdkx16.regenerativeagriculture.registry.ModBlocks;
import io.github.bdkx16.regenerativeagriculture.registry.ModCrops;
import io.github.bdkx16.regenerativeagriculture.registry.ModFoods;
import io.github.bdkx16.regenerativeagriculture.registry.ModItems;

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
                processing();

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
                roast(sunflower, ModFoods.ROASTED_SUNFLOWER_SEEDS.item());
            }

            /** Furnace, smoker and campfire. */
            private void roast(Item input, Item result) {
                SimpleCookingRecipeBuilder.smelting(Ingredient.of(input), RecipeCategory.FOOD, CookingBookCategory.FOOD, result, 0.35f, 200)
                        .unlockedBy(getHasName(input), has(input))
                        .save(output);
                simpleCookingRecipe("smoking", SmokingRecipe::new, 100, input, result, 0.35f);
                simpleCookingRecipe("campfire_cooking", CampfireCookingRecipe::new, 600, input, result, 0.35f);
            }

            /** Mash tun, brew kettle, fermenter, yeast and the hydrometer. */
            private void brewery() {
                shaped(RecipeCategory.DECORATIONS, ModBlocks.MASH_TUN)
                        .pattern("I I")
                        .pattern("IBI")
                        .pattern("III")
                        .define('I', Items.IRON_INGOT)
                        .define('B', Items.BARREL)
                        .unlockedBy(getHasName(ModItems.MALT), has(ModItems.MALT))
                        .save(output);
                shaped(RecipeCategory.DECORATIONS, ModBlocks.BREW_KETTLE)
                        .pattern("C C")
                        .pattern("C C")
                        .pattern("CCC")
                        .define('C', Items.COPPER_INGOT)
                        .unlockedBy(getHasName(ModItems.MALT), has(ModItems.MALT))
                        .save(output);
                shaped(RecipeCategory.DECORATIONS, ModBlocks.FERMENTER)
                        .pattern("IBI")
                        .pattern("I I")
                        .pattern(" I ")
                        .define('I', Items.IRON_INGOT)
                        .define('B', Items.GLASS_BOTTLE)
                        .unlockedBy(getHasName(ModItems.MALT), has(ModItems.MALT))
                        .save(output);
                shapeless(RecipeCategory.MISC, ModItems.YEAST, 2)
                        .requires(Items.BREAD)
                        .requires(Items.SUGAR)
                        .unlockedBy(getHasName(Items.SUGAR), has(Items.SUGAR))
                        .save(output);
                shapeless(RecipeCategory.TOOLS, ModItems.HYDROMETER)
                        .requires(Items.GLASS_BOTTLE)
                        .requires(Items.GLASS_PANE)
                        .requires(Items.IRON_NUGGET)
                        .unlockedBy(getHasName(ModItems.MALT), has(ModItems.MALT))
                        .save(output);
            }

            /** Mate from a cured gourd, and the thermos (filled with a water bucket, heated in a furnace). */
            private void mate() {
                shapeless(RecipeCategory.TOOLS, ModItems.MATE)
                        .requires(ModItems.CURED_GOURD)
                        .requires(Items.IRON_NUGGET)
                        .unlockedBy(getHasName(ModItems.CURED_GOURD), has(ModItems.CURED_GOURD))
                        .save(output);
                shaped(RecipeCategory.TOOLS, ModItems.THERMOS)
                        .pattern("N")
                        .pattern("I")
                        .pattern("I")
                        .define('N', Items.IRON_NUGGET)
                        .define('I', Items.IRON_INGOT)
                        .unlockedBy(getHasName(Items.IRON_INGOT), has(Items.IRON_INGOT))
                        .save(output);
                shapeless(RecipeCategory.TOOLS, ModItems.THERMOS_COLD)
                        .requires(ModItems.THERMOS)
                        .requires(Items.WATER_BUCKET)
                        .unlockedBy(getHasName(ModItems.THERMOS), has(ModItems.THERMOS))
                        .save(output);
                roast(ModItems.THERMOS_COLD, ModItems.THERMOS_HOT);
            }

            /** Drying rack, curing jar and what is made from dried and cured harvest. */
            private void processing() {
                shaped(RecipeCategory.DECORATIONS, ModBlocks.DRYING_RACK)
                        .pattern("SSS")
                        .pattern("T T")
                        .pattern("S S")
                        .define('S', Items.STICK)
                        .define('T', Items.STRING)
                        .unlockedBy(getHasName(Items.STRING), has(Items.STRING))
                        .save(output);
                shaped(RecipeCategory.DECORATIONS, ModBlocks.CURING_JAR)
                        .pattern(" W ")
                        .pattern("G G")
                        .pattern("GGG")
                        .define('W', ItemTags.WOODEN_SLABS)
                        .define('G', Items.GLASS)
                        .unlockedBy(getHasName(Items.GLASS), has(Items.GLASS))
                        .save(output);

                shapeless(RecipeCategory.FOOD, ModFoods.COFFEE.item())
                        .requires(Items.GLASS_BOTTLE)
                        .requires(ModItems.ROASTED_COFFEE, 2)
                        .requires(Items.WATER_BUCKET)
                        .unlockedBy(getHasName(ModItems.ROASTED_COFFEE), has(ModItems.ROASTED_COFFEE))
                        .save(output);
                shapeless(RecipeCategory.FOOD, ModItems.GROUND_CHILI, 2)
                        .requires(ModItems.DRIED_CHILI)
                        .unlockedBy(getHasName(ModItems.DRIED_CHILI), has(ModItems.DRIED_CHILI))
                        .save(output);
                shapeless(RecipeCategory.FOOD, ModItems.CHIMICHURRI)
                        .requires(Items.GLASS_BOTTLE)
                        .requires(ModItems.CURED_GARLIC)
                        .requires(ModItems.GROUND_CHILI)
                        .requires(ModCrops.BASIL.produce())
                        .unlockedBy(getHasName(ModItems.CURED_GARLIC), has(ModItems.CURED_GARLIC))
                        .save(output);
                shapeless(RecipeCategory.FOOD, ModFoods.ASADO.item())
                        .requires(Items.COOKED_BEEF, 2)
                        .requires(ModItems.CHIMICHURRI)
                        .unlockedBy(getHasName(ModItems.CHIMICHURRI), has(ModItems.CHIMICHURRI))
                        .save(output);
                shapeless(RecipeCategory.FOOD, ModFoods.LOCRO.item())
                        .requires(Items.BOWL)
                        .requires(ModItems.DRIED_CORN)
                        .requires(ModCrops.BEAN.produce())
                        .requires(ModCrops.SQUASH.produce())
                        .requires(ModItems.GROUND_CHILI)
                        .requires(ModItems.CURED_ONION)
                        .unlockedBy(getHasName(ModItems.DRIED_CORN), has(ModItems.DRIED_CORN))
                        .save(output);
                shapeless(RecipeCategory.FOOD, ModFoods.JOINT.item())
                        .requires(ModItems.CURED_CANNABIS)
                        .requires(Items.PAPER)
                        .unlockedBy(getHasName(ModItems.CURED_CANNABIS), has(ModItems.CURED_CANNABIS))
                        .save(output);
                shapeless(RecipeCategory.FOOD, ModFoods.BROWNIE.item(), 2)
                        .requires(ModItems.CURED_CANNABIS)
                        .requires(Items.COCOA_BEANS)
                        .requires(Items.WHEAT)
                        .requires(Items.EGG)
                        .requires(Items.SUGAR)
                        .unlockedBy(getHasName(ModItems.CURED_CANNABIS), has(ModItems.CURED_CANNABIS))
                        .save(output);
                shapeless(RecipeCategory.FOOD, ModFoods.CIGAR.item())
                        .requires(ModItems.CURED_TOBACCO, 2)
                        .unlockedBy(getHasName(ModItems.CURED_TOBACCO), has(ModItems.CURED_TOBACCO))
                        .save(output);
                roast(ModItems.DRIED_COFFEE, ModItems.ROASTED_COFFEE);
                brewery();
                mate();
                shapeless(RecipeCategory.MISC, ModItems.MANUAL)
                        .requires(Items.BOOK)
                        .requires(Items.WHEAT_SEEDS)
                        .unlockedBy(getHasName(Items.BOOK), has(Items.BOOK))
                        .save(output);
                roast(ModCrops.CORN.produce(), ModFoods.ROASTED_CORN.item());
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
