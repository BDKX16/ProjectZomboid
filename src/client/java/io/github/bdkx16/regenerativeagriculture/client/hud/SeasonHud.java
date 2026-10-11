package io.github.bdkx16.regenerativeagriculture.client.hud;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import io.github.bdkx16.regenerativeagriculture.RegenerativeAgriculture;
import io.github.bdkx16.regenerativeagriculture.client.season.ClientSeasonState;
import io.github.bdkx16.regenerativeagriculture.season.ClimateModel;
import io.github.bdkx16.regenerativeagriculture.season.SeasonCalendar;
import io.github.bdkx16.regenerativeagriculture.season.SeasonText;
import io.github.bdkx16.regenerativeagriculture.season.TemperatureBand;

/** Small top-left overlay: season, date, time and the temperature where the player stands. */
public final class SeasonHud {
    private static final String KEY = "hud." + RegenerativeAgriculture.MOD_ID + ".";
    private static final int MARGIN = 4;
    private static final int TEXT_COLOR = 0xFFFFFFFF;

    private SeasonHud() {
    }

    public static void init() {
        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, RegenerativeAgriculture.id("season"), SeasonHud::render);
    }

    private static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        ClimateModel climate = ClientSeasonState.climate();
        if (climate == null || mc.level == null || mc.player == null || mc.getDebugOverlay().showDebugScreen()) {
            return;
        }

        long ticks = mc.level.getOverworldClockTime();
        SeasonCalendar.Date date = climate.calendar().dateAt(ticks);
        BlockPos pos = mc.player.blockPosition();
        float biomeTemperature = mc.level.getBiome(pos).value().getBaseTemperature();
        double temperature = climate.temperatureAt(biomeTemperature, pos.getY(), ticks);
        TemperatureBand band = TemperatureBand.of(temperature);

        Component line1 = Component.translatable(KEY + "season",
                date.season().displayName(), date.dayOfSeason(), climate.calendar().daysPerSeason());
        Component line2 = Component.translatable(KEY + "date", SeasonText.date(date), SeasonText.time(ticks));
        Component line3 = SeasonText.celsius(temperature).withColor(band.color())
                .append(Component.literal(" · ")).append(band.displayName());

        int lineHeight = mc.font.lineHeight + 2;
        graphics.text(mc.font, line1, MARGIN, MARGIN, TEXT_COLOR, true);
        graphics.text(mc.font, line2, MARGIN, MARGIN + lineHeight, TEXT_COLOR, true);
        graphics.text(mc.font, line3, MARGIN, MARGIN + 2 * lineHeight, TEXT_COLOR, true);
    }
}
