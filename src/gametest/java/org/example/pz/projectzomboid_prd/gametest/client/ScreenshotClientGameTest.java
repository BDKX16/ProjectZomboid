package org.example.pz.projectzomboid_prd.gametest.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import org.example.pz.projectzomboid_prd.client.screen.WeatherStationScreen;

/**
 * Opens a real client, creates a world and saves screenshots of the HUD, the weather station screen and a grow
 * room ({@code ./gradlew runClientGameTest}; images end up in {@code build/run/clientGameTest/screenshots}).
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

            weatherStation(context, world);
            growRoom(context, world);
        }
    }

    private static void weatherStation(ClientGameTestContext context, TestSingleplayerContext world) {
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

    /** Builds a closed stone room around the player with crops, hanging grow lights and a vented extractor. */
    private static void growRoom(ClientGameTestContext context, TestSingleplayerContext world) {
        String[] commands = {
                "fill ~-5 ~-1 ~-3 ~5 ~4 ~7 stone hollow",
                "fill ~-3 ~-1 ~4 ~3 ~-1 ~5 farmland",
                "fill ~-3 ~ ~4 ~3 ~ ~5 projectzomboid_prd:tomato_crop[age=7]",
                "setblock ~-2 ~3 ~4 projectzomboid_prd:grow_light",
                "setblock ~2 ~3 ~4 projectzomboid_prd:grow_light",
                "setblock ~ ~3 ~1 projectzomboid_prd:grow_light",
                "setblock ~4 ~3 ~6 projectzomboid_prd:extractor",
                "setblock ~4 ~4 ~6 iron_bars",
                "item replace entity @p weapon.mainhand with projectzomboid_prd:thermohygrometer",
                "tp @p ~ ~ ~ facing ~ ~1.5 ~6"
        };
        for (String command : commands) {
            world.getServer().runCommand("execute as @p at @s run " + command);
        }
        context.waitTicks(60);
        context.takeScreenshot("grow_room_ok");

        // Block the vent: the fan stops and the meter asks for an extractor.
        world.getServer().runCommand("execute as @p at @s run setblock ~4 ~4 ~6 stone");
        context.waitTicks(60);
        context.takeScreenshot("grow_room_no_vent");
    }
}
