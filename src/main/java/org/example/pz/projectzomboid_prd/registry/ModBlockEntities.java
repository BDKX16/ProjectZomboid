package org.example.pz.projectzomboid_prd.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.example.pz.projectzomboid_prd.Projectzomboid_prd;
import org.example.pz.projectzomboid_prd.block.entity.CuringJarBlockEntity;
import org.example.pz.projectzomboid_prd.block.entity.DryingRackBlockEntity;

import java.util.Set;

public final class ModBlockEntities {
    public static final BlockEntityType<DryingRackBlockEntity> DRYING_RACK = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
            Projectzomboid_prd.id("drying_rack"), new BlockEntityType<>(DryingRackBlockEntity::new, Set.of(ModBlocks.DRYING_RACK)));
    public static final BlockEntityType<CuringJarBlockEntity> CURING_JAR = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,
            Projectzomboid_prd.id("curing_jar"), new BlockEntityType<>(CuringJarBlockEntity::new, Set.of(ModBlocks.CURING_JAR)));

    private ModBlockEntities() {
    }

    public static void init() {
        // Loads the class so the static types are registered.
    }
}
