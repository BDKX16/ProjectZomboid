package org.example.pz.projectzomboid_prd.crop;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;

/**
 * Data-driven description of a crop, loaded from
 * {@code data/<namespace>/projectzomboid_prd/crop_profile/<name>.json}.
 */
public record CropProfile(Block block, PlantFamily family, GrowthRequirements requirements) {
    public static final Codec<CropProfile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BuiltInRegistries.BLOCK.byNameCodec().fieldOf("block").forGetter(CropProfile::block),
            PlantFamily.CODEC.fieldOf("family").forGetter(CropProfile::family),
            GrowthRequirements.MAP_CODEC.forGetter(CropProfile::requirements)
    ).apply(instance, CropProfile::new));
}
