package io.github.bdkx16.regenerativeagriculture.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import io.github.bdkx16.regenerativeagriculture.RegenerativeAgriculture;

public final class ModTags {
    /** Blocks that count as a greenhouse roof. */
    public static final TagKey<Block> GREENHOUSE_GLASS = block("greenhouse_glass");
    /** Blocks that warm a greenhouse when nearby. */
    public static final TagKey<Block> HEAT_SOURCES = block("heat_sources");
    /** Blocks air can pass through in an extractor's vent shaft (besides air). */
    public static final TagKey<Block> VENT_PASSABLE = block("vent_passable");
    /** Biomes where greenhouses overheat instead of helping. */
    public static final TagKey<Biome> GREENHOUSE_OVERHEATS = TagKey.create(Registries.BIOME, RegenerativeAgriculture.id("greenhouse_overheats"));

    private ModTags() {
    }

    private static TagKey<Block> block(String name) {
        return TagKey.create(Registries.BLOCK, RegenerativeAgriculture.id(name));
    }
}
