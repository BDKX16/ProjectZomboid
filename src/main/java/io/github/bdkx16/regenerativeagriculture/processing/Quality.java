package io.github.bdkx16.regenerativeagriculture.processing;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import io.github.bdkx16.regenerativeagriculture.registry.ModComponents;

/**
 * Quality (1 to 3 stars) of what is cured in a curing jar. Longer curing gives better quality; the quality passes
 * on through crafting and makes the effects of what is made with it last longer.
 */
public final class Quality {
    public static final int MIN = 1;
    public static final int MAX = 3;

    private Quality() {
    }

    /** 0 when the stack has no quality. */
    public static int of(ItemStack stack) {
        return stack.getOrDefault(ModComponents.QUALITY, 0);
    }

    /** Effect duration multiplier: ×1, ×1.5, ×2. */
    public static float durationMultiplier(int quality) {
        return quality <= MIN ? 1.0f : 1.0f + 0.5f * (Math.min(quality, MAX) - MIN);
    }

    /**
     * @param thresholds days needed for each quality, e.g. {2, 4, 7}
     * @return 0 if not cured yet, otherwise the quality reached after {@code days}
     */
    public static int afterDays(double days, int[] thresholds) {
        int quality = 0;
        for (int i = 0; i < thresholds.length && days >= thresholds[i]; i++) {
            quality = i + 1;
        }
        return quality;
    }

    public static Component stars(int quality) {
        return Component.literal("★".repeat(quality)).withStyle(ChatFormatting.GOLD)
                .append(Component.literal("★".repeat(MAX - quality)).withStyle(ChatFormatting.DARK_GRAY));
    }
}
