package org.example.pz.projectzomboid_prd.gametest.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.example.pz.projectzomboid_prd.client.screen.WeatherStationScreen;
import org.example.pz.projectzomboid_prd.crop.CropProfile;
import org.example.pz.projectzomboid_prd.crop.CropProfiles;
import org.example.pz.projectzomboid_prd.registry.ModCrops;
import org.example.pz.projectzomboid_prd.registry.ModFoods;
import org.example.pz.projectzomboid_prd.registry.ModItems;
import org.example.pz.projectzomboid_prd.soil.SoilManager;

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
            soil(context, world);
            food(context, world);
            processing(context, world);
        }
    }

    /** The meals in the hotbar and the mod's effects in the inventory screen. */
    private static void food(ClientGameTestContext context, TestSingleplayerContext world) {
        world.getServer().runCommand("clear @p");
        int slot = 0;
        for (ModFoods.Meal meal : ModFoods.ALL) {
            world.getServer().runCommand("item replace entity @p container.%d with %s".formatted(slot++,
                    BuiltInRegistries.ITEM.getKey(meal.item())));
        }
        for (String effect : new String[]{"vitality", "immunity", "warmth", "nourished"}) {
            world.getServer().runCommand("effect give @p projectzomboid_prd:" + effect + " 120");
        }
        context.waitTicks(20);
        context.takeScreenshot("food_hud");
        context.setScreen(() -> new InventoryScreen(Minecraft.getInstance().player));
        context.waitTicks(5);
        context.takeScreenshot("food_inventory");
        context.setScreen(() -> null);
    }

    /**
     * The ten new crops ripe on farmland (half of it mulched), their wild plants behind, drying racks in every state
     * and curing jars; then the "Relaxed" screen tint.
     */
    private static void processing(ClientGameTestContext context, TestSingleplayerContext world) {
        String[] crops = {"onion", "garlic", "cannabis", "tobacco", "coffee", "chili", "hop", "yerba_mate", "corn", "bean"};
        world.getServer().runCommand("execute as @p at @s run tp @p ~ ~ ~-30");
        world.getServer().runCommand("execute as @p at @s run fill ~-6 ~-1 ~1 ~6 ~3 ~8 air");
        world.getServer().runCommand("execute as @p at @s run fill ~-6 ~-1 ~1 ~6 ~-1 ~8 grass_block");
        for (int i = 0; i < crops.length; i++) {
            int x = i - 5;
            String soil = i % 2 == 0 ? "projectzomboid_prd:mulched_farmland[moisture=7]" : "farmland[moisture=7]";
            run(world, "setblock ~%d ~-1 ~3 %s".formatted(x, soil));
            run(world, "setblock ~%d ~ ~3 projectzomboid_prd:%s_crop[age=7]".formatted(x, crops[i]));
            run(world, "setblock ~%d ~ ~5 projectzomboid_prd:wild_%s".formatted(x, crops[i]));
        }
        String[] contents = {"empty", "fresh", "dry", "spoiled"};
        for (int i = 0; i < contents.length; i++) {
            run(world, "setblock ~%d ~ ~7 projectzomboid_prd:drying_rack[contents=%s,facing=north]".formatted(-5 + i * 2, contents[i]));
        }
        run(world, "setblock ~3 ~ ~7 projectzomboid_prd:curing_jar");
        run(world, "setblock ~4 ~ ~7 projectzomboid_prd:curing_jar[filled=true]");
        run(world, "tp @p ~ ~4 ~-1 facing ~ ~ ~5");
        context.waitTicks(220); // let the chat from the soil analysis fade
        context.takeScreenshot("new_crops");

        world.getServer().runCommand("effect give @p projectzomboid_prd:relaxed 60");
        context.waitTicks(30);
        context.takeScreenshot("relaxed");
        world.getServer().runCommand("effect clear @p");
    }

    private static void run(TestSingleplayerContext world, String command) {
        world.getServer().runCommand("execute as @p at @s run " + command);
    }

    /** A tomato between basil (companion) and potatoes (antagonist), harvested twice, analysed with the soil kit. */
    private static void soil(ClientGameTestContext context, TestSingleplayerContext world) {
        String[] commands = {
                "tp @p ~20 ~ ~",
                "fill ~-3 ~-1 ~2 ~3 ~-1 ~2 farmland",
                "setblock ~-1 ~ ~2 projectzomboid_prd:basil_crop[age=7]",
                "setblock ~ ~ ~2 projectzomboid_prd:tomato_crop[age=7]",
                "setblock ~1 ~ ~2 potatoes[age=7]",
                "setblock ~3 ~ ~4 projectzomboid_prd:compost_bin[level=5]",
                "item replace entity @p weapon.mainhand with projectzomboid_prd:soil_test_kit",
                "tp @p ~ ~ ~ facing ~ ~-0.5 ~2"
        };
        for (String command : commands) {
            world.getServer().runCommand("execute as @p at @s run " + command);
        }
        context.waitTicks(20);
        world.getServer().runOnServer(server -> {
            ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
            BlockPos tomato = player.blockPosition().offset(0, 0, 2);
            CropProfile profile = CropProfiles.get(server.registryAccess(), ModCrops.TOMATO.block()).orElseThrow();
            SoilManager.onHarvest(player.level(), tomato, profile);
            SoilManager.onHarvest(player.level(), tomato, profile);
            ModItems.SOIL_TEST_KIT.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                    new BlockHitResult(Vec3.atCenterOf(tomato), Direction.UP, tomato, false)));
        });
        context.waitTicks(10);
        context.takeScreenshot("soil_analysis");
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

        // Speed up random ticks so the stale air has time to grow mold on the tomatoes.
        world.getServer().runCommand("gamerule minecraft:random_tick_speed 200");
        context.waitTicks(100);
        world.getServer().runCommand("gamerule minecraft:random_tick_speed 3");
        context.takeScreenshot("grow_room_mold");
    }
}
