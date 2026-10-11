package org.example.pz.projectzomboid_prd.worldgen;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.SimpleBlockFeature;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.BlockPredicateFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.OffsetPlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.RarityFilter;
import org.example.pz.projectzomboid_prd.Projectzomboid_prd;
import org.example.pz.projectzomboid_prd.registry.ModCrops;

import java.util.function.Predicate;

/**
 * Wild crop patches. The features themselves are generated as JSON by datagen ({@link #bootstrapFeatures},
 * {@link #bootstrapPlacedFeatures}); at runtime {@link #init} adds them to the right biomes.
 */
public final class ModWorldGen {
    /** On average one patch every this many chunks in a matching biome. */
    private static final int RARITY = 24;
    private static final int TRIES_PER_PATCH = 12;

    private ModWorldGen() {
    }

    public static void init() {
        addWildPlant(ModCrops.LETTUCE, BiomeSelectors.tag(BiomeTags.IS_FOREST));
        addWildPlant(ModCrops.TOMATO, BiomeSelectors.tag(BiomeTags.IS_SAVANNA).or(BiomeSelectors.tag(BiomeTags.IS_JUNGLE)));
        addWildPlant(ModCrops.ARUGULA, BiomeSelectors.includeByKey(Biomes.PLAINS, Biomes.MEADOW));
        addWildPlant(ModCrops.BROCCOLI, BiomeSelectors.tag(BiomeTags.IS_TAIGA));
        addWildPlant(ModCrops.SOYBEAN, BiomeSelectors.includeByKey(Biomes.PLAINS).or(BiomeSelectors.tag(BiomeTags.IS_SAVANNA)));
        addWildPlant(ModCrops.SUNFLOWER, BiomeSelectors.includeByKey(Biomes.SUNFLOWER_PLAINS, Biomes.PLAINS));
        addWildPlant(ModCrops.BASIL, BiomeSelectors.tag(BiomeTags.IS_JUNGLE));
        addWildPlant(ModCrops.SQUASH, BiomeSelectors.tag(BiomeTags.IS_JUNGLE).or(BiomeSelectors.tag(BiomeTags.IS_FOREST)));
    }

    private static void addWildPlant(ModCrops.Crop crop, Predicate<BiomeSelectionContext> biomes) {
        BiomeModifications.addFeature(biomes, GenerationStep.Decoration.VEGETAL_DECORATION, placedKey(crop));
    }

    public static ResourceKey<Feature> featureKey(ModCrops.Crop crop) {
        return ResourceKey.create(Registries.FEATURE, Projectzomboid_prd.id("wild_" + crop.name()));
    }

    public static ResourceKey<PlacedFeature> placedKey(ModCrops.Crop crop) {
        return ResourceKey.create(Registries.PLACED_FEATURE, Projectzomboid_prd.id("patch_wild_" + crop.name()));
    }

    public static void bootstrapFeatures(BootstrapContext<Feature> context) {
        for (ModCrops.Crop crop : ModCrops.ALL) {
            context.register(featureKey(crop), new SimpleBlockFeature(BlockStateProvider.of(crop.wildPlant())));
        }
    }

    public static void bootstrapPlacedFeatures(BootstrapContext<PlacedFeature> context) {
        HolderGetter<Feature> features = context.lookup(Registries.FEATURE);
        for (ModCrops.Crop crop : ModCrops.ALL) {
            PlacementUtils.register(context, placedKey(crop), features.getOrThrow(featureKey(crop)),
                    RarityFilter.onAverageOnceEvery(RARITY),
                    InSquarePlacement.spread(),
                    PlacementUtils.HEIGHTMAP,
                    BiomeFilter.biome(),
                    CountPlacement.of(TRIES_PER_PATCH),
                    OffsetPlacement.ofTriangle(4, 2),
                    BlockPredicateFilter.forPredicate(BlockPredicate.allOf(
                            BlockPredicate.ONLY_IN_AIR_PREDICATE,
                            BlockPredicate.wouldSurvive(crop.wildPlant()))));
        }
    }
}
