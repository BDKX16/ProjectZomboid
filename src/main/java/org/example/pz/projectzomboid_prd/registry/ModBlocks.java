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
import org.example.pz.projectzomboid_prd.Projectzomboid_prd;
import org.example.pz.projectzomboid_prd.block.WeatherStationBlock;
import org.example.pz.projectzomboid_prd.item.DescribedBlockItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/** Farming equipment blocks (everything that is not a crop). */
public final class ModBlocks {
    private static final List<Block> MUTABLE_ALL = new ArrayList<>();
    public static final List<Block> ALL = Collections.unmodifiableList(MUTABLE_ALL);

    /** Lights an underground grow room. */
    public static final Block GROW_LIGHT = register("grow_light", Block::new, BlockBehaviour.Properties.of()
            .mapColor(MapColor.COLOR_PINK)
            .strength(0.3f)
            .sound(SoundType.GLASS)
            .lightLevel(state -> 15));

    /**
     * Ventilates a greenhouse or grow room; needs an open shaft to the surface above it. Its top is a solar panel,
     * which is why it regulates itself without power.
     */
    public static final Block EXTRACTOR = register("extractor", Block::new, BlockBehaviour.Properties.of()
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

    private ModBlocks() {
    }

    public static void init() {
        // Loads the class so the static blocks are registered.
    }

    private static Block register(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties properties) {
        ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, Projectzomboid_prd.id(name));
        Block block = Registry.register(BuiltInRegistries.BLOCK, blockKey, factory.apply(properties.setId(blockKey)));
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, Projectzomboid_prd.id(name));
        Registry.register(BuiltInRegistries.ITEM, itemKey,
                new DescribedBlockItem(block, new Item.Properties().useBlockDescriptionPrefix().setId(itemKey)));
        MUTABLE_ALL.add(block);
        return block;
    }
}
