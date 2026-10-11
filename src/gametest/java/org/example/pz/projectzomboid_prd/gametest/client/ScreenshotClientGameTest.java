package org.example.pz.projectzomboid_prd.gametest.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import org.example.pz.projectzomboid_prd.client.screen.WeatherStationScreen;

/**
 * Opens a real client, creates a world and saves screenshots of the HUD and the weather station screen
 * ({@code ./gradlew runClientGameTest}; images end up in {@code build/run/clientGameTest/screenshots}).
 * Visual check only: it fails just if something crashes.
 */
public class ScreenshotClientGameTest implements FabricClientGameTest {

    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            world.getServer().runCommand("time set 6000");
            world.getServer().runCommand("season set winter");
            context.waitTicks(100);
            context.takeScreenshot("hud_winter");

            BlockPos pos = context.computeOnClient(client -> client.player.blockPosition());
            world.getServer().runCommand("setblock %d %d %d projectzomboid_prd:weather_station"
                    .formatted(pos.getX() + 2, pos.getY(), pos.getZ()));
            context.waitTicks(5);
            context.setScreen(() -> new WeatherStationScreen(pos));
            context.waitTicks(5);
            context.takeScreenshot("weather_station_winter");

            context.setScreen(() -> null);
            world.getServer().runCommand("season set summer");
            context.waitTicks(20);
            context.setScreen(() -> new WeatherStationScreen(pos));
            context.waitTicks(5);
            context.takeScreenshot("weather_station_summer");
            context.setScreen(() -> null);
        }
    }
}
