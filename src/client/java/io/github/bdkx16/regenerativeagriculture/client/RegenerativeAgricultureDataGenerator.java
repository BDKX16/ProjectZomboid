package io.github.bdkx16.regenerativeagriculture.client;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import io.github.bdkx16.regenerativeagriculture.client.datagen.ModAdvancementProvider;
import io.github.bdkx16.regenerativeagriculture.client.datagen.ModBlockLootProvider;
import io.github.bdkx16.regenerativeagriculture.client.datagen.ModModelProvider;
import io.github.bdkx16.regenerativeagriculture.client.datagen.ModRecipeProvider;
import io.github.bdkx16.regenerativeagriculture.client.datagen.ModWorldGenProvider;
import io.github.bdkx16.regenerativeagriculture.worldgen.ModWorldGen;

public class RegenerativeAgricultureDataGenerator implements DataGeneratorEntrypoint {

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider(ModModelProvider::new);
        pack.addProvider(ModBlockLootProvider::new);
        pack.addProvider(ModRecipeProvider::new);
        pack.addProvider(ModAdvancementProvider::new);
        pack.addProvider(ModWorldGenProvider::new);
    }

    @Override
    public void buildRegistry(RegistrySetBuilder registryBuilder) {
        registryBuilder.add(Registries.FEATURE, ModWorldGen::bootstrapFeatures);
        registryBuilder.add(Registries.PLACED_FEATURE, ModWorldGen::bootstrapPlacedFeatures);
    }
}
