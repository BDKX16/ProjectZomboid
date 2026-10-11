package io.github.bdkx16.regenerativeagriculture.processing;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.minecraft.world.level.Level;
import io.github.bdkx16.regenerativeagriculture.registry.ModComponents;

import java.util.List;

/** Like vanilla's apply_effects, but durations grow with the consumed item's {@link Quality}. */
public record QualityEffectsConsumeEffect(List<MobEffectInstance> effects) implements ConsumeEffect {
    public static final MapCodec<QualityEffectsConsumeEffect> CODEC = MobEffectInstance.CODEC.listOf()
            .fieldOf("effects").xmap(QualityEffectsConsumeEffect::new, QualityEffectsConsumeEffect::effects);
    public static final StreamCodec<RegistryFriendlyByteBuf, QualityEffectsConsumeEffect> STREAM_CODEC =
            MobEffectInstance.STREAM_CODEC.apply(ByteBufCodecs.list())
                    .map(QualityEffectsConsumeEffect::new, QualityEffectsConsumeEffect::effects);

    public QualityEffectsConsumeEffect {
        effects = List.copyOf(effects);
    }

    @Override
    public Type<QualityEffectsConsumeEffect> getType() {
        return ModComponents.QUALITY_EFFECTS;
    }

    @Override
    public boolean apply(Level level, ItemStack stack, LivingEntity entity) {
        float multiplier = Quality.durationMultiplier(Quality.of(stack));
        boolean applied = false;
        for (MobEffectInstance effect : effects) {
            applied |= entity.addEffect(effect.withScaledDuration(multiplier));
        }
        return applied;
    }
}
