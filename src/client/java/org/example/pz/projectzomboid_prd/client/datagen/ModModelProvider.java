package org.example.pz.projectzomboid_prd.client.datagen;

import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.world.level.block.CropBlock;
import org.example.pz.projectzomboid_prd.registry.ModBlocks;
import org.example.pz.projectzomboid_prd.registry.ModCrops;
import org.example.pz.projectzomboid_prd.registry.ModItems;

public class ModModelProvider extends FabricModelProvider {

    public ModModelProvider(FabricPackOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockModelGenerators blockModelGenerators) {
        blockModelGenerators.createTrivialCube(ModBlocks.GROW_LIGHT);
        // extractor_top is the solar panel, extractor_side the fan, extractor_bottom a grille
        blockModelGenerators.createTrivialBlock(ModBlocks.EXTRACTOR, TexturedModel.CUBE_BOTTOM_TOP);
        // weather_station_top: anemometer and rain gauge, weather_station_side: display panel
        blockModelGenerators.createTrivialBlock(ModBlocks.WEATHER_STATION, TexturedModel.CUBE_BOTTOM_TOP);
        for (ModCrops.Crop crop : ModCrops.ALL) {
            // 8 ages share 4 textures: <name>_crop_stage0..3
            blockModelGenerators.createCropBlock(crop.block(), CropBlock.AGE, 0, 0, 1, 1, 2, 2, 2, 3);
            blockModelGenerators.createCrossBlock(crop.wildPlant(), BlockModelGenerators.PlantType.NOT_TINTED);
        }
    }

    @Override
    public void generateItemModels(ItemModelGenerators itemModelGenerators) {
        itemModelGenerators.generateFlatItem(ModItems.PRUNING_SHEARS, ModelTemplates.FLAT_HANDHELD_ITEM);
        // Seed models are created automatically as the crop blocks' items.
        for (ModCrops.Crop crop : ModCrops.ALL) {
            itemModelGenerators.generateFlatItem(crop.produce(), ModelTemplates.FLAT_ITEM);
        }
    }
}
