package io.github.bdkx16.regenerativeagriculture.registry;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Unit;
import io.github.bdkx16.regenerativeagriculture.item.MateItem;
import io.github.bdkx16.regenerativeagriculture.item.ThermosItem;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import io.github.bdkx16.regenerativeagriculture.RegenerativeAgriculture;
import io.github.bdkx16.regenerativeagriculture.processing.Quality;
import io.github.bdkx16.regenerativeagriculture.processing.QualityEffectsConsumeEffect;

/** Item data components and consume effect types added by the mod. */
public final class ModComponents {
    /** Stars (1-3) of a cured product and of what is crafted from it. */
    public static final DataComponentType<Integer> QUALITY = Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE,
            RegenerativeAgriculture.id("quality"), DataComponentType.<Integer>builder()
                    .persistent(Codec.intRange(Quality.MIN, Quality.MAX))
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
                    .build());

    /** Servings left of the yerba loaded in a mate. */
    public static final DataComponentType<Integer> MATE_YERBA = Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE,
            RegenerativeAgriculture.id("mate_yerba"), DataComponentType.<Integer>builder()
                    .persistent(Codec.intRange(1, MateItem.SERVINGS_PER_LOAD))
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
                    .build());
    /** The mate has hot water in it, ready to drink. */
    public static final DataComponentType<Unit> MATE_SERVED = Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE,
            RegenerativeAgriculture.id("mate_served"), DataComponentType.<Unit>builder()
                    .persistent(Unit.CODEC)
                    .networkSynchronized(StreamCodec.unit(Unit.INSTANCE))
                    .build());
    /** Hot water servings left in a thermos. */
    public static final DataComponentType<Integer> THERMOS_SERVINGS = Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE,
            RegenerativeAgriculture.id("thermos_servings"), DataComponentType.<Integer>builder()
                    .persistent(Codec.intRange(0, ThermosItem.SERVINGS))
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
                    .build());

    public static final ConsumeEffect.Type<QualityEffectsConsumeEffect> QUALITY_EFFECTS = Registry.register(
            BuiltInRegistries.CONSUME_EFFECT_TYPE, RegenerativeAgriculture.id("quality_effects"),
            new ConsumeEffect.Type<>(QualityEffectsConsumeEffect.CODEC, QualityEffectsConsumeEffect.STREAM_CODEC));

    private ModComponents() {
    }

    public static void init() {
        // Loads the class so the static components are registered.
    }
}
