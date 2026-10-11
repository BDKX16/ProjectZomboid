package org.example.pz.projectzomboid_prd.crop;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;

import java.util.List;
import java.util.Optional;

/**
 * Data-driven description of a crop, loaded from
 * {@code data/<namespace>/projectzomboid_prd/crop_profile/<name>.json}.
 *
 * @param regrowAge   if present, pruning shears harvest the mature plant without breaking it and set it back to this
 *                    age (tomato, basil, lettuce, ...); absent for crops harvested once (soy, sunflower)
 * @param nitrogen    soil nitrogen change per harvest: negative when consumed, positive for legumes that fix it
 * @param companions  crop blocks that help this one when growing nearby
 * @param antagonists crop blocks that hinder this one when growing nearby
 */
public record CropProfile(Block block, PlantFamily family, GrowthRequirements requirements, Optional<Integer> regrowAge,
                          int nitrogen, List<Identifier> companions, List<Identifier> antagonists) {
    public static final int DEFAULT_NITROGEN = -10;

    public static final Codec<CropProfile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BuiltInRegistries.BLOCK.byNameCodec().fieldOf("block").forGetter(CropProfile::block),
            PlantFamily.CODEC.fieldOf("family").forGetter(CropProfile::family),
            GrowthRequirements.MAP_CODEC.forGetter(CropProfile::requirements),
            Codec.intRange(0, CropBlock.MAX_AGE - 1).optionalFieldOf("regrow_age").forGetter(CropProfile::regrowAge),
            Codec.intRange(-50, 50).optionalFieldOf("nitrogen", DEFAULT_NITROGEN).forGetter(CropProfile::nitrogen),
            Identifier.CODEC.listOf().optionalFieldOf("companions", List.of()).forGetter(CropProfile::companions),
            Identifier.CODEC.listOf().optionalFieldOf("antagonists", List.of()).forGetter(CropProfile::antagonists)
    ).apply(instance, CropProfile::new));

    public boolean isCompanion(Block other) {
        return companions.contains(BuiltInRegistries.BLOCK.getKey(other));
    }

    public boolean isAntagonist(Block other) {
        return antagonists.contains(BuiltInRegistries.BLOCK.getKey(other));
    }
}
