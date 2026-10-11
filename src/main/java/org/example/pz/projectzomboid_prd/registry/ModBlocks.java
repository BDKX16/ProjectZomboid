package org.example.pz.projectzomboid_prd.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import org.example.pz.projectzomboid_prd.Projectzomboid_prd;
import org.example.pz.projectzomboid_prd.block.CompostBinBlock;
import org.example.pz.projectzomboid_prd.block.ExtractorBlock;
import org.example.pz.projectzomboid_prd.block.GrowLightBlock;
import org.example.pz.projectzomboid_prd.block.MoldyCropBlock;
import org.example.pz.projectzomboid_prd.block.WeatherStationBlock;
import org.example.pz.projectzomboid_prd.item.DescribedBlockItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/** Farming equipment blocks (everything that is not a crop). {@link #ALL} lists the ones with an item. */
public final class ModBlocks {
    private static final List<Block> MUTABLE_ALL = new ArrayList<>();
    public static final List<Block> ALL = Collections.unmodifiableList(MUTABLE_ALL);

    /** Lights an underground grow room; hangs from the ceiling. */
    public static final Block GROW_LIGHT = register("grow_light", GrowLightBlock::new, BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_PINK)
            .strength(0.3f)
            .sound(SoundType.LANTERN)
            .noOcclusion()
            .lightLevel(state -> 15));

    /**
     * Ventilates a greenhouse or grow room; needs an open shaft to the surface above it. Its top is a solar panel,
     * which is why it regulates itself without power.
     */
    public static final Block EXTRACTOR = register("extractor", ExtractorBlock::new, BlockBehaviour.Properties.of()
            .mapColor(MapColor.METAL)
            .strength(3.0f)
            .requiresCorrectToolForDrops()
            .sound(SoundType.METAL));

    /** Shows date, temperature, a 7-day forecast and what to plant. */
    public static final Block WEATHER_STATION = register("weather_station", WeatherStationBlock::new, BlockBehaviour.Properties.of()
            .mapColor(MapColor.METAL)
            .strength(2.0f)
            .requiresCorrectToolForDrops()
            .sound(SoundType.COPPER));

    /** Compost bin: like a composter, but yields compost for the soil. */
    public static final Block COMPOST_BIN = register("compost_bin", CompostBinBlock::new, BlockBehaviour.Properties.of()
            .mapColor(MapColor.WOOD)
            .strength(0.6f)
            .sound(SoundType.WOOD));

    /** A crop killed by mold in a muggy greenhouse or grow room; has no item. */
    public static final Block MOLDY_CROP = registerBlock("moldy_crop", MoldyCropBlock::new, BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_LIGHT_GRAY)
            .noCollision()
            .instabreak()
            .randomTicks()
            .noLootTable()
            .sound(SoundType.WET_GRASS)
            .pushReaction(PushReaction.POPPED));

    private ModBlocks() {
    }

    public static void init() {
        // Loads the class so the static blocks are registered.
    }

    private static Block registerBlock(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties properties) {
        ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, Projectzomboid_prd.id(name));
        return Registry.register(BuiltInRegistries.BLOCK, key, factory.apply(properties.setId(key)));
    }

    /** Registers the block with an item and lists it in {@link #ALL}. */
    private static Block register(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties properties) {
        Block block = registerBlock(name, factory, properties);
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, Projectzomboid_prd.id(name));
        Registry.register(BuiltInRegistries.ITEM, itemKey,
                new DescribedBlockItem(block, new Item.Properties().useBlockDescriptionPrefix().setId(itemKey)));
        MUTABLE_ALL.add(block);
        return block;
    }
}
