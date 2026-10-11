package io.github.bdkx16.regenerativeagriculture.client.hud;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.effect.MobEffectInstance;
import io.github.bdkx16.regenerativeagriculture.RegenerativeAgriculture;
import io.github.bdkx16.regenerativeagriculture.effect.ModEffects;

/** While "Relaxed" lasts, the screen gets a slow, soft glow that drifts between green and violet. */
public final class RelaxedHud {
    private static final Identifier HAZE = RegenerativeAgriculture.id("textures/misc/relaxed_haze.png");
    /** Strongest tint (additive, 0-1). */
    private static final float MAX_STRENGTH = 0.6f;
    /** The tint fades out during the last seconds of the effect. */
    private static final int FADE_TICKS = 100;

    private RelaxedHud() {
    }

    public static void init() {
        HudElementRegistry.attachElementBefore(VanillaHudElements.CROSSHAIR, RegenerativeAgriculture.id("relaxed"), RelaxedHud::render);
    }

    private static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        MobEffectInstance effect = mc.player.getEffect(ModEffects.RELAXED);
        if (effect == null) {
            return;
        }
        float time = (mc.player.tickCount + deltaTracker.getGameTimeDeltaPartialTick(false)) / 40f;
        float fade = effect.isInfiniteDuration() ? 1 : Math.min(1, effect.getDuration() / (float) FADE_TICKS);
        float strength = MAX_STRENGTH * fade * (0.75f + 0.25f * (float) Math.sin(time * 1.3f));
        float mix = 0.5f + 0.5f * (float) Math.sin(time);
        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        // Additive like vanilla's nausea overlay, with a soft haze that glows at the edges
        graphics.blit(RenderPipelines.GUI_NAUSEA_OVERLAY, HAZE, 0, 0, 0, 0, width, height, width, height,
                ARGB.colorFromFloat(1.0f, strength * (0.35f + 0.45f * mix), strength * (0.8f - 0.4f * mix), strength * (0.35f + 0.6f * mix)));
    }
}
