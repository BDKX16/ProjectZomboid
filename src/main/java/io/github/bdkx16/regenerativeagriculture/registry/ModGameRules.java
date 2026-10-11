package io.github.bdkx16.regenerativeagriculture.registry;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import io.github.bdkx16.regenerativeagriculture.RegenerativeAgriculture;

public final class ModGameRules {
    /** Length of each season in Minecraft days. */
    public static final GameRule<Integer> SEASON_LENGTH = GameRuleBuilder.forInteger(15)
            .range(1, 120)
            .category(GameRuleCategory.MISC)
            .buildAndRegister(RegenerativeAgriculture.id("season_length"));

    private ModGameRules() {
    }

    public static void init() {
        // Loads the class so the static rules are registered.
    }
}
