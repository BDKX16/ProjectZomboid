package org.example.pz.projectzomboid_prd.registry;

import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Compostable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import org.example.pz.projectzomboid_prd.Projectzomboid_prd;
import org.example.pz.projectzomboid_prd.block.SeasonalCropBlock;
import org.example.pz.projectzomboid_prd.block.WildPlantBlock;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/** The mod's crops: for each one a crop block, its seeds, the harvested produce and a wild plant. */
public final class ModCrops {
    private static final List<Crop> MUTABLE_ALL = new ArrayList<>();
    public static final List<Crop> ALL = Collections.unmodifiableList(MUTABLE_ALL);

    public static final Crop LETTUCE = register("lettuce", "lettuce", food(2, 0.3f), 2);
    public static final Crop TOMATO = register("tomato", "tomato", food(3, 0.4f), 3);
    public static final Crop ARUGULA = register("arugula", "arugula", food(1, 0.3f), 2);
    public static final Crop BROCCOLI = register("broccoli", "broccoli", food(3, 0.6f), 2);
    public static final Crop SOYBEAN = register("soybean", "soybeans", food(2, 0.5f), 2);
    public static final Crop SUNFLOWER = register("sunflower", "sunflower_head", null, 4);
    public static final Crop BASIL = register("basil", "basil", food(1, 0.2f), 2);
    public static final Crop SQUASH = register("squash", "squash", food(4, 0.6f), 3);

    public static final ResourceKey<CreativeModeTab> TAB = ResourceKey.create(Registries.CREATIVE_MODE_TAB, Projectzomboid_prd.id("crops"));

    private ModCrops() {
    }

    public static void init() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, TAB, FabricCreativeModeTab.builder()
                .title(Component.translatable("itemGroup." + Projectzomboid_prd.MOD_ID + ".crops"))
                .icon(() -> new ItemStack(TOMATO.produce()))
                .displayItems((parameters, output) -> {
                    for (Block block : ModBlocks.ALL) {
                        output.accept(block);
                    }
                    output.accept(ModItems.PRUNING_SHEARS);
                    output.accept(ModItems.THERMOHYGROMETER);
                    output.accept(ModItems.SOIL_TEST_KIT);
                    output.accept(ModItems.COMPOST);
                    output.accept(ModItems.STRAW_MULCH);
                    for (Crop crop : ALL) {
                        output.accept(crop.seeds());
                        output.accept(crop.produce());
                    }
                    for (ModFoods.Meal meal : ModFoods.ALL) {
                        output.accept(meal.item());
                    }
                })
                .build());
    }

    /**
     * @param name          base id: blocks are {@code <name>_crop} and {@code wild_<name>}, seeds {@code <name>_seeds}
     * @param produceName   id of the harvested item
     * @param food          null if the produce is not edible raw
     * @param seedsPerCraft seeds obtained by crafting one produce
     */
    private static Crop register(String name, String produceName, @Nullable FoodProperties food, int seedsPerCraft) {
        SeasonalCropBlock block = registerBlock(name + "_crop", SeasonalCropBlock::new, BlockBehaviour.Properties.of()
                .mapColor(MapColor.PLANT)
                .noCollision()
                .randomTicks()
                .instabreak()
                .sound(SoundType.CROP)
                .pushReaction(PushReaction.POPPED));
        Item seeds = registerItem(name + "_seeds", properties -> new BlockItem(block, properties),
                new Item.Properties().component(DataComponents.COMPOSTABLE, compostable("low")));

        Item.Properties produceProperties = new Item.Properties().component(DataComponents.COMPOSTABLE, compostable("medium"));
        if (food != null) {
            produceProperties.food(food);
        }
        Item produce = registerItem(produceName, Item::new, produceProperties);
        block.setItems(seeds, produce);

        WildPlantBlock wild = registerBlock("wild_" + name, WildPlantBlock::new, BlockBehaviour.Properties.of()
                .mapColor(MapColor.PLANT)
                .noCollision()
                .instabreak()
                .sound(SoundType.GRASS)
                .offsetType(BlockBehaviour.OffsetType.XZ)
                .pushReaction(PushReaction.POPPED));

        Crop crop = new Crop(name, block, seeds, produce, wild, seedsPerCraft);
        MUTABLE_ALL.add(crop);
        return crop;
    }

    /** One of vanilla's compost chance providers ({@code data/minecraft/context_int_provider/compostable/}). */
    private static Compostable compostable(String chance) {
        return new Compostable(ResourceKey.create(Registries.CONTEXT_INT_PROVIDER, Identifier.withDefaultNamespace("compostable/" + chance)));
    }

    private static FoodProperties food(int nutrition, float saturation) {
        return new FoodProperties.Builder().nutrition(nutrition).saturationModifier(saturation).build();
    }

    private static <B extends Block> B registerBlock(String name, Function<BlockBehaviour.Properties, B> factory, BlockBehaviour.Properties properties) {
        ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, Projectzomboid_prd.id(name));
        return Registry.register(BuiltInRegistries.BLOCK, key, factory.apply(properties.setId(key)));
    }

    static Item registerItem(String name, Function<Item.Properties, Item> factory, Item.Properties properties) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Projectzomboid_prd.id(name));
        return Registry.register(BuiltInRegistries.ITEM, key, factory.apply(properties.setId(key)));
    }

    public record Crop(String name, SeasonalCropBlock block, Item seeds, Item produce, WildPlantBlock wildPlant, int seedsPerCraft) {
    }
}
