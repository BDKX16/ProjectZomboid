package io.github.bdkx16.regenerativeagriculture.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** {@link MobEffect}'s constructor is protected; the mod's effects only need attribute modifiers or a marker. */
public class ModEffect extends MobEffect {
    public ModEffect(MobEffectCategory category, int color) {
        super(category, color);
    }
}
