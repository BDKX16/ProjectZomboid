package org.example.pz.projectzomboid_prd.client.datagen;

import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.blockstates.MultiPartGenerator;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import org.example.pz.projectzomboid_prd.block.BreweryBlock;
import org.example.pz.projectzomboid_prd.registry.ModComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Blocks;
import org.example.pz.projectzomboid_prd.block.CuringJarBlock;
import org.example.pz.projectzomboid_prd.block.DryingRackBlock;
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
import org.example.pz.projectzomboid_prd.registry.ModFoods;
import org.example.pz.projectzomboid_prd.registry.ModItems;

public class ModModelProvider extends FabricModelProvider {

    public ModModelProvider(FabricPackOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockModelGenerators blockModelGenerators) {
        blockModelGenerators.createCrossBlock(ModBlocks.MOLDY_CROP, BlockModelGenerators.PlantType.NOT_TINTED);
        // Straw on top (mulched_farmland and mulched_farmland_moist), dirt sides like vanilla farmland
        blockModelGenerators.createFarmland(ModBlocks.MULCHED_FARMLAND, TextureMapping.getBlockTexture(Blocks.DIRT),
                TextureMapping.getBlockTexture(Blocks.DIRT), ModelTemplates.CUBE_BOTTOM_TOP_INDENTED);

        // Hand-made models in resources: drying_rack_<contents>.json, curing_jar(_filled).json
        // The hanging harvest is drawn by ProcessingRenderer with the real item, so every state uses the bare frame
        blockModelGenerators.blockStateOutput.accept(MultiVariantGenerator.dispatch(ModBlocks.DRYING_RACK)
                .with(PropertyDispatch.initial(DryingRackBlock.CONTENTS).generate(contents ->
                        BlockModelGenerators.plainVariant(Projectzomboid_prd.id("block/drying_rack_empty"))))
                .with(BlockModelGenerators.ROTATION_HORIZONTAL_FACING));
        blockModelGenerators.registerSimpleItemModel(ModBlocks.DRYING_RACK, Projectzomboid_prd.id("block/drying_rack_dry"));
        blockModelGenerators.blockStateOutput.accept(MultiVariantGenerator.dispatch(ModBlocks.CURING_JAR)
                .with(BlockModelGenerators.createBooleanModelDispatch(CuringJarBlock.FILLED,
                        BlockModelGenerators.plainVariant(Projectzomboid_prd.id("block/curing_jar")),
                        BlockModelGenerators.plainVariant(Projectzomboid_prd.id("block/curing_jar")))));
        blockModelGenerators.registerSimpleItemModel(ModBlocks.CURING_JAR, Projectzomboid_prd.id("block/curing_jar_filled"));

        // Microbrewery: hand-made models <name>_<stage>.json, rotated to face the player
        for (Block machine : new Block[]{ModBlocks.MASH_TUN, ModBlocks.BREW_KETTLE, ModBlocks.FERMENTER}) {
            String name = BuiltInRegistries.BLOCK.getKey(machine).getPath();
            blockModelGenerators.blockStateOutput.accept(MultiVariantGenerator.dispatch(machine)
                    .with(PropertyDispatch.initial(BreweryBlock.STAGE).generate(stage ->
                            BlockModelGenerators.plainVariant(Projectzomboid_prd.id("block/" + name + "_" + stage.getSerializedName()))))
                    .with(BlockModelGenerators.ROTATION_HORIZONTAL_FACING));
            blockModelGenerators.registerSimpleItemModel(machine, Projectzomboid_prd.id("block/" + name + "_done"));
        }

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
        itemModelGenerators.generateFlatItem(ModItems.MANUAL, ModelTemplates.FLAT_ITEM);
        for (Item item : ModItems.PRODUCTS) {
            itemModelGenerators.generateFlatItem(item, ModelTemplates.FLAT_ITEM);
        }
        // 3D item models in resources/models/item; the mate shows its yerba and the hot water
        Identifier mate = Projectzomboid_prd.id("item/mate");
        itemModelGenerators.itemModelOutput.accept(ModItems.MATE, ItemModelUtils.conditional(
                ItemModelUtils.hasComponent(ModComponents.MATE_SERVED), ItemModelUtils.plainModel(mate.withSuffix("_served")),
                ItemModelUtils.conditional(ItemModelUtils.hasComponent(ModComponents.MATE_YERBA),
                        ItemModelUtils.plainModel(mate.withSuffix("_loaded")), ItemModelUtils.plainModel(mate))));
        for (Item item : new Item[]{ModItems.HYDROMETER, ModItems.THERMOS, ModItems.THERMOS_COLD, ModItems.THERMOS_HOT}) {
            itemModelGenerators.itemModelOutput.accept(item, ItemModelUtils.plainModel(
                    Projectzomboid_prd.id("item/" + BuiltInRegistries.ITEM.getKey(item).getPath())));
        }
        for (ModFoods.Meal meal : ModFoods.ALL) {
            itemModelGenerators.generateFlatItem(meal.item(), ModelTemplates.FLAT_ITEM);
        }
    }
}
