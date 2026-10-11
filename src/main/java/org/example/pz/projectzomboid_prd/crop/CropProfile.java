package org.example.pz.projectzomboid_prd.crop;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;

import java.util.Optional;

/**
 * Data-driven description of a crop, loaded from
 * {@code data/<namespace>/projectzomboid_prd/crop_profile/<name>.json}.
 *
 * @param regrowAge if present, pruning shears harvest the mature plant without breaking it and set it back to this
 *                  age (tomato, basil, lettuce, ...); absent for crops harvested once (soy, sunflower)
 */
public record CropProfile(Block block, PlantFamily family, GrowthRequirements requirements, Optional<Integer> regrowAge) {
    public static final Codec<CropProfile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BuiltInRegistries.BLOCK.byNameCodec().fieldOf("block").forGetter(CropProfile::block),
            PlantFamily.CODEC.fieldOf("family").forGetter(CropProfile::family),
            GrowthRequirements.MAP_CODEC.forGetter(CropProfile::requirements),
            Codec.intRange(0, CropBlock.MAX_AGE - 1).optionalFieldOf("regrow_age").forGetter(CropProfile::regrowAge)
    ).apply(instance, CropProfile::new));
}
