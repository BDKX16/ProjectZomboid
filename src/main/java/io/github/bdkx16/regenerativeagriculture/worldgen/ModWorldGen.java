package io.github.bdkx16.regenerativeagriculture.worldgen;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Util;
import net.minecraft.world.level.biome.Biome;
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
import io.github.bdkx16.regenerativeagriculture.RegenerativeAgriculture;
import io.github.bdkx16.regenerativeagriculture.registry.ModCrops;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

    private static final Habitat FORESTS = Habitat.tag(BiomeTags.IS_FOREST, "forests");
    private static final Habitat SAVANNAS = Habitat.tag(BiomeTags.IS_SAVANNA, "savannas");
    private static final Habitat JUNGLES = Habitat.tag(BiomeTags.IS_JUNGLE, "jungles");
    private static final Habitat TAIGAS = Habitat.tag(BiomeTags.IS_TAIGA, "taigas");
    private static final Habitat BADLANDS = Habitat.tag(BiomeTags.IS_BADLANDS, "badlands");
    private static final Habitat PLAINS = Habitat.biome(Biomes.PLAINS);
    private static final Habitat SUNFLOWER_PLAINS = Habitat.biome(Biomes.SUNFLOWER_PLAINS);
    private static final Habitat MEADOW = Habitat.biome(Biomes.MEADOW);
    private static final Habitat WINDSWEPT_HILLS = Habitat.biome(Biomes.WINDSWEPT_HILLS);
    private static final Habitat CHERRY_GROVE = Habitat.biome(Biomes.CHERRY_GROVE);
    private static final Habitat FOREST = Habitat.biome(Biomes.FOREST);
    private static final Habitat BIRCH_FOREST = Habitat.biome(Biomes.BIRCH_FOREST);
    private static final Habitat DARK_FOREST = Habitat.biome(Biomes.DARK_FOREST);
    private static final Habitat FLOWER_FOREST = Habitat.biome(Biomes.FLOWER_FOREST);

    private static final Map<ModCrops.Crop, List<Habitat>> HABITATS = new LinkedHashMap<>();

    public static void init() {
        addWildPlant(ModCrops.LETTUCE, FORESTS);
        addWildPlant(ModCrops.TOMATO, SAVANNAS, JUNGLES);
        addWildPlant(ModCrops.ARUGULA, PLAINS, MEADOW);
        addWildPlant(ModCrops.BROCCOLI, TAIGAS);
        addWildPlant(ModCrops.SOYBEAN, PLAINS, SAVANNAS);
        addWildPlant(ModCrops.SUNFLOWER, SUNFLOWER_PLAINS, PLAINS);
        addWildPlant(ModCrops.BASIL, JUNGLES);
        addWildPlant(ModCrops.SQUASH, JUNGLES, FORESTS);
        addWildPlant(ModCrops.ONION, PLAINS, MEADOW);
        addWildPlant(ModCrops.GARLIC, MEADOW, WINDSWEPT_HILLS, CHERRY_GROVE);
        addWildPlant(ModCrops.CANNABIS, SAVANNAS, MEADOW);
        addWildPlant(ModCrops.TOBACCO, JUNGLES, SAVANNAS);
        addWildPlant(ModCrops.COFFEE, JUNGLES);
        addWildPlant(ModCrops.CHILI, SAVANNAS, BADLANDS);
        addWildPlant(ModCrops.HOP, FOREST, BIRCH_FOREST, DARK_FOREST);
        addWildPlant(ModCrops.YERBA_MATE, JUNGLES, FOREST);
        addWildPlant(ModCrops.CORN, PLAINS, SUNFLOWER_PLAINS, SAVANNAS);
        addWildPlant(ModCrops.BEAN, PLAINS, FOREST, FLOWER_FOREST);
    }

    private static void addWildPlant(ModCrops.Crop crop, Habitat... habitats) {
        HABITATS.put(crop, List.of(habitats));
        Predicate<BiomeSelectionContext> biomes = habitats[0].biomes();
        for (int i = 1; i < habitats.length; i++) {
            biomes = biomes.or(habitats[i].biomes());
        }
        BiomeModifications.addFeature(biomes, GenerationStep.Decoration.VEGETAL_DECORATION, placedKey(crop));
    }

    /** Where the wild plant of {@code crop} grows, for the manual; empty if it has no wild patches. */
    public static List<Habitat> habitats(ModCrops.Crop crop) {
        return HABITATS.getOrDefault(crop, List.of());
    }

    /** A biome or group of biomes wild plants grow in, with the name the manual shows for it. */
    public record Habitat(Predicate<BiomeSelectionContext> biomes, Component name) {
        static Habitat tag(TagKey<Biome> tag, String name) {
            return new Habitat(BiomeSelectors.tag(tag), Component.translatable("habitat." + RegenerativeAgriculture.MOD_ID + "." + name));
        }

        static Habitat biome(ResourceKey<Biome> biome) {
            return new Habitat(BiomeSelectors.includeByKey(biome), Component.translatable(Util.makeDescriptionId("biome", biome.identifier())));
        }
    }

    public static ResourceKey<Feature> featureKey(ModCrops.Crop crop) {
        return ResourceKey.create(Registries.FEATURE, RegenerativeAgriculture.id("wild_" + crop.name()));
    }

    public static ResourceKey<PlacedFeature> placedKey(ModCrops.Crop crop) {
        return ResourceKey.create(Registries.PLACED_FEATURE, RegenerativeAgriculture.id("patch_wild_" + crop.name()));
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
