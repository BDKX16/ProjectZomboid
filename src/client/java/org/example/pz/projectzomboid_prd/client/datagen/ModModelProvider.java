package org.example.pz.projectzomboid_prd.client.datagen;

import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.blockstates.MultiPartGenerator;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.resources.Identifier;
import org.example.pz.projectzomboid_prd.Projectzomboid_prd;
import org.example.pz.projectzomboid_prd.block.ExtractorBlock;
import net.minecraft.world.level.block.ComposterBlock;
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
        blockModelGenerators.createCrossBlock(ModBlocks.MOLDY_CROP, BlockModelGenerators.PlantType.NOT_TINTED);

        // Compost bin: own base model (models/block/compost_bin.json) + vanilla composter contents per fill level
        MultiPartGenerator compostBin = MultiPartGenerator.multiPart(ModBlocks.COMPOST_BIN)
                .with(BlockModelGenerators.plainVariant(Projectzomboid_prd.id("block/compost_bin")));
        for (int level = 1; level < ComposterBlock.READY; level++) {
            compostBin.with(BlockModelGenerators.condition().term(ComposterBlock.LEVEL, level),
                    BlockModelGenerators.plainVariant(Identifier.withDefaultNamespace("block/composter_contents" + level)));
        }
        compostBin.with(BlockModelGenerators.condition().term(ComposterBlock.LEVEL, ComposterBlock.READY),
                BlockModelGenerators.plainVariant(Identifier.withDefaultNamespace("block/composter_contents_ready")));
        blockModelGenerators.blockStateOutput.accept(compostBin);

        // Hand-made hanging model in resources: models/block/grow_light.json
        blockModelGenerators.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(ModBlocks.GROW_LIGHT,
                BlockModelGenerators.plainVariant(Projectzomboid_prd.id("block/grow_light"))));

        // extractor_top is the solar panel, extractor_side the fan (animated when active), extractor_bottom a grille
        Identifier extractorIdle = TexturedModel.CUBE_BOTTOM_TOP.create(ModBlocks.EXTRACTOR, blockModelGenerators.modelOutput);
        Identifier extractorActive = TexturedModel.CUBE_BOTTOM_TOP
                .updateTexture(textures -> textures.put(TextureSlot.SIDE, TextureMapping.getBlockTexture(ModBlocks.EXTRACTOR, "_side_active")))
                .createWithSuffix(ModBlocks.EXTRACTOR, "_active", blockModelGenerators.modelOutput);
        blockModelGenerators.blockStateOutput.accept(MultiVariantGenerator.dispatch(ModBlocks.EXTRACTOR)
                .with(BlockModelGenerators.createBooleanModelDispatch(ExtractorBlock.ACTIVE,
                        BlockModelGenerators.plainVariant(extractorActive), BlockModelGenerators.plainVariant(extractorIdle))));
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
        itemModelGenerators.generateFlatItem(ModItems.THERMOHYGROMETER, ModelTemplates.FLAT_ITEM);
        itemModelGenerators.generateFlatItem(ModItems.SOIL_TEST_KIT, ModelTemplates.FLAT_ITEM);
        itemModelGenerators.generateFlatItem(ModItems.COMPOST, ModelTemplates.FLAT_ITEM);
        itemModelGenerators.generateFlatItem(ModItems.STRAW_MULCH, ModelTemplates.FLAT_ITEM);
        // Seed models are created automatically as the crop blocks' items.
        for (ModCrops.Crop crop : ModCrops.ALL) {
            itemModelGenerators.generateFlatItem(crop.produce(), ModelTemplates.FLAT_ITEM);
        }
    }
}
