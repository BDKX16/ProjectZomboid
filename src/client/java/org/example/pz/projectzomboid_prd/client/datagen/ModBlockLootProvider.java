package org.example.pz.projectzomboid_prd.client.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootSubProvider;
import net.minecraft.advancements.predicates.StatePropertiesPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.storage.loot.predicates.MatchBlock;
import org.example.pz.projectzomboid_prd.registry.ModBlocks;
import org.example.pz.projectzomboid_prd.registry.ModCrops;

import java.util.concurrent.CompletableFuture;

public class ModBlockLootProvider extends FabricBlockLootSubProvider {

    public ModBlockLootProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    public void generate() {
        for (Block block : ModBlocks.ALL) {
            dropSelf(block);
        }
        for (ModCrops.Crop crop : ModCrops.ALL) {
            // Mature: produce + seeds (with Fortune bonus); otherwise just the seed back.
            add(crop.block(), createCropDrops(crop.block(), crop.produce(), crop.seeds(),
                    MatchBlock.blockMatches(blocks, crop.block(),
                            StatePropertiesPredicate.Builder.properties().hasProperty(CropBlock.AGE, CropBlock.MAX_AGE))));
            // Wild plants are the natural source of seeds.
            dropOther(crop.wildPlant(), crop.seeds());
        }
    }
}
