package org.example.pz.projectzomboid_prd.crop;

import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import org.example.pz.projectzomboid_prd.Projectzomboid_prd;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

/** The crop profile registry, synced to clients so tooltips and the manual can use it. */
public final class CropProfiles {
    public static final ResourceKey<Registry<CropProfile>> REGISTRY_KEY =
            ResourceKey.createRegistryKey(Projectzomboid_prd.id("crop_profile"));

    /** Block → profile index per loaded registry (one per world/connection). */
    private static final Map<Registry<CropProfile>, Map<Block, CropProfile>> BY_BLOCK =
            Collections.synchronizedMap(new WeakHashMap<>());

    private CropProfiles() {
    }

    public static void init() {
        DynamicRegistries.registerSynced(REGISTRY_KEY, CropProfile.CODEC);
    }

    public static Optional<CropProfile> get(RegistryAccess registries, Block block) {
        return registries.lookup(REGISTRY_KEY)
                .map(registry -> BY_BLOCK.computeIfAbsent(registry, CropProfiles::index).get(block));
    }

    private static Map<Block, CropProfile> index(Registry<CropProfile> registry) {
        Map<Block, CropProfile> map = new IdentityHashMap<>();
        for (CropProfile profile : registry) {
            map.put(profile.block(), profile);
        }
        return map;
    }
}
