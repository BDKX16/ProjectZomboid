package org.example.pz.projectzomboid_prd.client.hud;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.example.pz.projectzomboid_prd.Projectzomboid_prd;
import org.example.pz.projectzomboid_prd.client.season.ClientSeasonState;
import org.example.pz.projectzomboid_prd.greenhouse.ClimateAdvice;
import org.example.pz.projectzomboid_prd.greenhouse.MicroClimate;
import org.example.pz.projectzomboid_prd.greenhouse.Mold;
import org.example.pz.projectzomboid_prd.registry.ModItems;
import org.example.pz.projectzomboid_prd.season.ClimateModel;
import org.example.pz.projectzomboid_prd.season.SeasonText;
import org.example.pz.projectzomboid_prd.season.TemperatureBand;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Phone-like readout in the bottom-right corner while the player holds a thermo-hygrometer. */
public final class ThermohygrometerHud {
    private static final String KEY = "hud." + Projectzomboid_prd.MOD_ID + ".thermohygrometer.";
    private static final int WIDTH = 132;
    private static final int PADDING = 6;
    private static final int MARGIN = 6;
    /** Keeps the device above the hotbar, health and food bars. */
    private static final int BOTTOM_CLEARANCE = 44;
    private static final int UPDATE_INTERVAL = 10;
    private static final int CASE = 0xFF202226;
    private static final int SCREEN = 0xFF0F1D24;
    private static final int MUTED = 0xFF8FA3AE;
    private static final int TEXT = 0xFFFFFFFF;
    private static final int OK = 0xFF7BD35A;
    private static final int WARNING = 0xFFF0A040;

    private static @Nullable MicroClimate reading;
    private static int timer;

    private ThermohygrometerHud() {
    }

    public static void init() {
        ClientTickEvents.END_CLIENT_TICK.register(ThermohygrometerHud::tick);
        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, Projectzomboid_prd.id("thermohygrometer"), ThermohygrometerHud::render);
    }

    private static void tick(Minecraft mc) {
        ClimateModel climate = ClientSeasonState.climate();
        if (climate == null || mc.level == null || mc.player == null
                || !(mc.player.getMainHandItem().is(ModItems.THERMOHYGROMETER) || mc.player.getOffhandItem().is(ModItems.THERMOHYGROMETER))) {
            reading = null;
            timer = 0;
            return;
        }
        if (reading == null || ++timer >= UPDATE_INTERVAL) {
            timer = 0;
            reading = MicroClimate.measure(mc.level, mc.player.blockPosition(), climate, mc.level.getOverworldClockTime());
        }
    }

    private static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        MicroClimate r = reading;
        Minecraft mc = Minecraft.getInstance();
        if (r == null || mc.getDebugOverlay().showDebugScreen()) {
            return;
        }
        int innerWidth = WIDTH - 2 * PADDING;
        List<ClimateAdvice.Advice> adviceList = ClimateAdvice.advise(r);
        List<FormattedCharSequence> place = mc.font.split(r.shelter().displayName(), innerWidth);
        List<List<FormattedCharSequence>> advice = new ArrayList<>();
        for (ClimateAdvice.Advice item : adviceList) {
            advice.add(mc.font.split(Component.literal("• ").append(item.text()), innerWidth));
        }

        int line = mc.font.lineHeight + 1;
        int height = PADDING * 2 + line + 20 + place.size() * line + 4;
        for (List<FormattedCharSequence> lines : advice) {
            height += lines.size() * line;
        }
        int x = graphics.guiWidth() - WIDTH - MARGIN;
        int y = Math.max(MARGIN, graphics.guiHeight() - height - BOTTOM_CLEARANCE);

        // Device case and screen
        graphics.fill(x - 2, y - 2, x + WIDTH + 2, y + height + 2, CASE);
        graphics.fill(x, y, x + WIDTH, y + height, SCREEN);
        int cx = x + PADDING;
        int cy = y + PADDING;
        graphics.text(mc.font, Component.translatable(KEY + "title"), cx, cy, MUTED, false);
        cy += line + 2;

        TemperatureBand band = TemperatureBand.of(r.temperature());
        graphics.pose().pushMatrix();
        graphics.pose().translate(cx, cy);
        graphics.pose().scale(1.5f, 1.5f);
        graphics.text(mc.font, SeasonText.celsius(r.temperature()), 0, 0, 0xFF000000 | band.color(), true);
        graphics.pose().popMatrix();
        String humidity = String.format(Locale.ROOT, "%d%% HR", Math.round(r.humidity()));
        graphics.text(mc.font, Component.literal(humidity), cx + 64, cy + 3, humidityColor(r.humidity()), true);
        cy += 18;

        for (FormattedCharSequence text : place) {
            graphics.text(mc.font, text, cx, cy, TEXT, false);
            cy += line;
        }
        cy += 4;
        for (int i = 0; i < advice.size(); i++) {
            int color = adviceList.get(i).ok() ? OK : WARNING;
            for (FormattedCharSequence text : advice.get(i)) {
                graphics.text(mc.font, text, cx, cy, color, false);
                cy += line;
            }
        }
    }

    private static int humidityColor(double humidity) {
        if (humidity > Mold.HUMIDITY) {
            return 0xFFE5533D;
        }
        if (humidity > ClimateAdvice.HUMID || humidity < ClimateAdvice.DRY) {
            return WARNING;
        }
        return 0xFF7FC8F0;
    }
}
