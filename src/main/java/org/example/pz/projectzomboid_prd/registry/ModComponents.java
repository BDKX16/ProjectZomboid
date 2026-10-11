package org.example.pz.projectzomboid_prd.registry;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import org.example.pz.projectzomboid_prd.Projectzomboid_prd;
import org.example.pz.projectzomboid_prd.processing.Quality;
import org.example.pz.projectzomboid_prd.processing.QualityEffectsConsumeEffect;

/** Item data components and consume effect types added by the mod. */
public final class ModComponents {
    /** Stars (1-3) of a cured product and of what is crafted from it. */
    public static final DataComponentType<Integer> QUALITY = Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE,
            Projectzomboid_prd.id("quality"), DataComponentType.<Integer>builder()
                    .persistent(Codec.intRange(Quality.MIN, Quality.MAX))
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
                    .build());

    public static final ConsumeEffect.Type<QualityEffectsConsumeEffect> QUALITY_EFFECTS = Registry.register(
            BuiltInRegistries.CONSUME_EFFECT_TYPE, Projectzomboid_prd.id("quality_effects"),
            new ConsumeEffect.Type<>(QualityEffectsConsumeEffect.CODEC, QualityEffectsConsumeEffect.STREAM_CODEC));

    private ModComponents() {
    }

    public static void init() {
        // Loads the class so the static components are registered.
    }
}
