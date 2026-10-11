package org.example.pz.projectzomboid_prd.effect;

import net.fabricmc.fabric.api.entity.event.v1.effect.ServerMobEffectEvents;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.example.pz.projectzomboid_prd.Projectzomboid_prd;

import java.util.List;

/** Effects given by the mod's meals. */
public final class ModEffects {
    /** Fresh salad: +10 % movement speed (vanilla Speed I is +20 %). */
    public static final Holder<MobEffect> VITALITY = register("vitality", new ModEffect(MobEffectCategory.BENEFICIAL, 0x7BC043)
            .addAttributeModifier(Attributes.MOVEMENT_SPEED, Projectzomboid_prd.id("effect.vitality"), 0.10,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    /** Broccoli: poison, hunger and nausea cannot be applied while it lasts. */
    public static final Holder<MobEffect> IMMUNITY = register("immunity", new ModEffect(MobEffectCategory.BENEFICIAL, 0x2E8B57));
    /** Squash soup: the player does not freeze in powder snow. */
    public static final Holder<MobEffect> WARMTH = register("warmth", new ModEffect(MobEffectCategory.BENEFICIAL, 0xE8862A));
    /** Varied diet: +2 hearts of maximum health. */
    public static final Holder<MobEffect> NOURISHED = register("nourished", new ModEffect(MobEffectCategory.BENEFICIAL, 0xD94F70)
            .addAttributeModifier(Attributes.MAX_HEALTH, Projectzomboid_prd.id("effect.nourished"), 4.0,
                    AttributeModifier.Operation.ADD_VALUE));

    /** What {@link #IMMUNITY} blocks. */
    public static final List<Holder<MobEffect>> IMMUNE_TO = List.of(MobEffects.POISON, MobEffects.HUNGER, MobEffects.NAUSEA);

    private ModEffects() {
    }

    public static void init() {
        ServerMobEffectEvents.ALLOW_ADD.register((instance, entity, context) ->
                !(entity.hasEffect(IMMUNITY) && IMMUNE_TO.stream().anyMatch(instance::is)));
    }

    private static Holder<MobEffect> register(String name, MobEffect effect) {
        return Registry.registerForHolder(BuiltInRegistries.MOB_EFFECT, Projectzomboid_prd.id(name), effect);
    }
}
