package io.github.bdkx16.regenerativeagriculture;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import io.github.bdkx16.regenerativeagriculture.command.SeasonCommand;
import io.github.bdkx16.regenerativeagriculture.crop.CropProfiles;
import io.github.bdkx16.regenerativeagriculture.effect.ModEffects;
import io.github.bdkx16.regenerativeagriculture.food.DietTracker;
import io.github.bdkx16.regenerativeagriculture.guide.Guide;
import io.github.bdkx16.regenerativeagriculture.registry.ModBlockEntities;
import io.github.bdkx16.regenerativeagriculture.registry.ModBlocks;
import io.github.bdkx16.regenerativeagriculture.registry.ModComponents;
import io.github.bdkx16.regenerativeagriculture.registry.ModCrops;
import io.github.bdkx16.regenerativeagriculture.registry.ModFoods;
import io.github.bdkx16.regenerativeagriculture.registry.ModGameRules;
import io.github.bdkx16.regenerativeagriculture.registry.ModItems;
import io.github.bdkx16.regenerativeagriculture.registry.ModSounds;
import io.github.bdkx16.regenerativeagriculture.season.SeasonManager;
import io.github.bdkx16.regenerativeagriculture.soil.SoilManager;
import io.github.bdkx16.regenerativeagriculture.worldgen.ModWorldGen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegenerativeAgriculture implements ModInitializer {
    public static final String MOD_ID = "regenerative_agriculture";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        ModGameRules.init();
        SeasonManager.init();
        SeasonCommand.init();
        CropProfiles.init();
        ModComponents.init();
        ModSounds.init();
        ModBlocks.init();
        ModCrops.init();
        ModItems.init();
        ModBlockEntities.init();
        ModEffects.init();
        ModFoods.init();
        DietTracker.init();
        ModWorldGen.init();
        SoilManager.init();
        Guide.init();
    }
}
