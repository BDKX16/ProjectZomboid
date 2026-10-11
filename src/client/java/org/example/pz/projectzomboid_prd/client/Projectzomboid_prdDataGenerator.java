package org.example.pz.projectzomboid_prd.client;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import org.example.pz.projectzomboid_prd.client.datagen.ModBlockLootProvider;
import org.example.pz.projectzomboid_prd.client.datagen.ModModelProvider;
import org.example.pz.projectzomboid_prd.client.datagen.ModRecipeProvider;
import org.example.pz.projectzomboid_prd.client.datagen.ModWorldGenProvider;
import org.example.pz.projectzomboid_prd.worldgen.ModWorldGen;

public class Projectzomboid_prdDataGenerator implements DataGeneratorEntrypoint {

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider(ModModelProvider::new);
        pack.addProvider(ModBlockLootProvider::new);
        pack.addProvider(ModRecipeProvider::new);
        pack.addProvider(ModWorldGenProvider::new);
    }

    @Override
    public void buildRegistry(RegistrySetBuilder registryBuilder) {
        registryBuilder.add(Registries.FEATURE, ModWorldGen::bootstrapFeatures);
        registryBuilder.add(Registries.PLACED_FEATURE, ModWorldGen::bootstrapPlacedFeatures);
    }
}
