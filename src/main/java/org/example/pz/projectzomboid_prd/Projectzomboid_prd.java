package org.example.pz.projectzomboid_prd;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.example.pz.projectzomboid_prd.command.SeasonCommand;
import org.example.pz.projectzomboid_prd.crop.CropProfiles;
import org.example.pz.projectzomboid_prd.effect.ModEffects;
import org.example.pz.projectzomboid_prd.food.DietTracker;
import org.example.pz.projectzomboid_prd.registry.ModBlockEntities;
import org.example.pz.projectzomboid_prd.registry.ModBlocks;
import org.example.pz.projectzomboid_prd.registry.ModComponents;
import org.example.pz.projectzomboid_prd.registry.ModCrops;
import org.example.pz.projectzomboid_prd.registry.ModFoods;
import org.example.pz.projectzomboid_prd.registry.ModGameRules;
import org.example.pz.projectzomboid_prd.registry.ModItems;
import org.example.pz.projectzomboid_prd.season.SeasonManager;
import org.example.pz.projectzomboid_prd.soil.SoilManager;
import org.example.pz.projectzomboid_prd.worldgen.ModWorldGen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Projectzomboid_prd implements ModInitializer {
    public static final String MOD_ID = "projectzomboid_prd";
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
        ModBlocks.init();
        ModCrops.init();
        ModItems.init();
        ModBlockEntities.init();
        ModEffects.init();
        ModFoods.init();
        DietTracker.init();
        ModWorldGen.init();
        SoilManager.init();
    }
}
