package io.github.bdkx16.regenerativeagriculture.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * "Relaxed" (cannabis): slower movement, a slow trickle of healing and the munchies (hunger drains faster).
 * The client also tints the screen while it lasts.
 */
public class RelaxedEffect extends ModEffect {
    /** Heals half a heart this often. */
    private static final int HEAL_INTERVAL = 80;
    /** Extra exhaustion per tick: about one hunger point every ten seconds. */
    private static final float MUNCHIES = 0.02f;

    public RelaxedEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
        if (entity instanceof Player player) {
            player.causeFoodExhaustion(MUNCHIES);
        }
        if (entity.tickCount % HEAL_INTERVAL == 0 && entity.getHealth() < entity.getMaxHealth()) {
            entity.heal(1.0f);
        }
        return true;
    }
}
