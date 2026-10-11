package io.github.bdkx16.regenerativeagriculture.gametest.client;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import io.github.bdkx16.regenerativeagriculture.client.screen.manual.ManualScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import io.github.bdkx16.regenerativeagriculture.client.screen.WeatherStationScreen;
import io.github.bdkx16.regenerativeagriculture.crop.CropProfile;
import io.github.bdkx16.regenerativeagriculture.crop.CropProfiles;
import io.github.bdkx16.regenerativeagriculture.registry.ModCrops;
import io.github.bdkx16.regenerativeagriculture.registry.ModBlocks;
import io.github.bdkx16.regenerativeagriculture.block.entity.CuringJarBlockEntity;
import io.github.bdkx16.regenerativeagriculture.block.entity.DryingRackBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.Heightmap;
import io.github.bdkx16.regenerativeagriculture.registry.ModFoods;
import io.github.bdkx16.regenerativeagriculture.registry.ModItems;
import io.github.bdkx16.regenerativeagriculture.soil.SoilManager;

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
            brewery(context, world);
            manual(context, world);
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
            world.getServer().runCommand("effect give @p regenerative_agriculture:" + effect + " 120");
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
            String soil = i % 2 == 0 ? "regenerative_agriculture:mulched_farmland[moisture=7]" : "farmland[moisture=7]";
            run(world, "setblock ~%d ~-1 ~3 %s".formatted(x, soil));
            run(world, "setblock ~%d ~ ~3 regenerative_agriculture:%s_crop[age=7]".formatted(x, crops[i]));
            run(world, "setblock ~%d ~ ~5 regenerative_agriculture:wild_%s".formatted(x, crops[i]));
        }
        String[] contents = {"empty", "fresh", "dry", "spoiled"};
        for (int i = 0; i < contents.length; i++) {
            run(world, "setblock ~%d ~ ~7 regenerative_agriculture:drying_rack[contents=%s,facing=north]".formatted(-5 + i * 2, contents[i]));
        }
        run(world, "setblock ~3 ~ ~7 regenerative_agriculture:curing_jar");
        run(world, "setblock ~4 ~ ~7 regenerative_agriculture:curing_jar[filled=true]");
        run(world, "tp @p ~ ~4 ~-1 facing ~ ~ ~5");
        context.waitTicks(220); // let the chat from the soil analysis fade
        context.takeScreenshot("new_crops");

        // Racks and jars with real contents, seen up close
        world.getServer().runOnServer(server -> {
            ServerPlayer player = server.getPlayerList().getPlayers().getFirst();
            BlockPos start = player.blockPosition().offset(20, 0, 0);
            BlockPos base = player.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, start);
            // fresh tobacco, the same dried, cannabis gone moldy, a few chilies
            ItemStack[] racks = {new ItemStack(ModCrops.TOBACCO.produce(), 16), new ItemStack(ModCrops.TOBACCO.produce(), 16),
                    new ItemStack(ModCrops.CANNABIS.produce(), 12), new ItemStack(ModCrops.CHILI.produce(), 4)};
            for (int i = 0; i < racks.length; i++) {
                BlockPos pos = base.offset(-5 + i * 2, 0, 6);
                player.level().setBlockAndUpdate(pos, ModBlocks.DRYING_RACK.defaultBlockState());
                if (player.level().getBlockEntity(pos) instanceof DryingRackBlockEntity rack) {
                    rack.insert(racks[i]);
                    if (i == 1) {
                        rack.update(20, 40, false, 3 * 24000);
                    } else if (i == 2) {
                        rack.update(20, 95, false, 24000);
                    }
                }
            }
            for (int i = 0; i < 2; i++) {
                BlockPos pos = base.offset(3 + i, 0, 6);
                player.level().setBlockAndUpdate(pos, ModBlocks.CURING_JAR.defaultBlockState());
                if (player.level().getBlockEntity(pos) instanceof CuringJarBlockEntity jar) {
                    jar.insert(new ItemStack(i == 0 ? ModItems.DRIED_CANNABIS : ModItems.DRIED_YERBA, 8));
                }
            }
            server.getCommands().performPrefixedCommand(server.createCommandSourceStack(),
                    "tp @p " + base.getX() + ".5 " + (base.getY() + 1) + " " + (base.getZ() + 3) + ".5 0 25");
        });
        context.waitTicks(20);
        context.takeScreenshot("drying_racks");

        world.getServer().runCommand("effect give @p regenerative_agriculture:relaxed 60");
        context.waitTicks(30);
        context.takeScreenshot("relaxed");
        world.getServer().runCommand("effect clear @p");
    }

    /**
     * Two microbreweries (one idle, one working on a campfire) seen from the front, then the mate and the thermos
     * in the player's hands.
     */
    private static void brewery(ClientGameTestContext context, TestSingleplayerContext world) {
        world.getServer().runCommand("effect clear @p");
        world.getServer().runCommand("clear @p");
        run(world, "tp @p ~ ~ ~-20");
        run(world, "fill ~-5 ~-1 ~1 ~5 ~4 ~6 air");
        run(world, "fill ~-5 ~-1 ~1 ~5 ~-1 ~6 stone_bricks");
        String[] working = {"mash_tun[stage=working,facing=south]", "brew_kettle[stage=working,facing=south]",
                "fermenter[stage=working,facing=south]"};
        String[] idle = {"mash_tun[stage=filled,facing=south]", "brew_kettle[stage=done,facing=south]",
                "fermenter[stage=done,facing=south]"};
        for (int i = 0; i < 3; i++) {
            run(world, "setblock ~%d ~ ~3 regenerative_agriculture:%s".formatted(-1 - i, working[i]));
            run(world, "setblock ~%d ~ ~3 regenerative_agriculture:%s".formatted(4 - i, idle[i]));
        }
        run(world, "setblock ~-2 ~-1 ~3 campfire");
        run(world, "tp @p ~0.5 ~1 ~ facing ~0.5 ~ ~3");
        context.waitTicks(40);
        context.takeScreenshot("brewery");

        run(world, "tp @p ~ ~ ~ facing ~ ~-1 ~3");
        world.getServer().runCommand("item replace entity @p weapon.mainhand with regenerative_agriculture:mate[regenerative_agriculture:mate_yerba=8,regenerative_agriculture:mate_served={}]");
        world.getServer().runCommand("item replace entity @p weapon.offhand with regenerative_agriculture:thermos_hot");
        world.getServer().runCommand("item replace entity @p hotbar.1 with regenerative_agriculture:hydrometer");
        world.getServer().runCommand("item replace entity @p hotbar.2 with regenerative_agriculture:thermos");
        world.getServer().runCommand("item replace entity @p hotbar.3 with regenerative_agriculture:thermos_cold");
        world.getServer().runCommand("item replace entity @p hotbar.4 with regenerative_agriculture:mate");
        world.getServer().runCommand("item replace entity @p hotbar.5 with regenerative_agriculture:mate[regenerative_agriculture:mate_yerba=10]");
        world.getServer().runCommand("item replace entity @p hotbar.6 with regenerative_agriculture:mash_tun");
        world.getServer().runCommand("item replace entity @p hotbar.7 with regenerative_agriculture:brew_kettle");
        world.getServer().runCommand("item replace entity @p hotbar.8 with regenerative_agriculture:fermenter");
        context.waitTicks(20);
        context.takeScreenshot("mate_in_hands");
    }

    /** The manual with part of the route done, and its other chapters. */
    private static void manual(ClientGameTestContext context, TestSingleplayerContext world) {
        world.getServer().runCommand("advancement grant @p until regenerative_agriculture:route/dried");
        world.getServer().runCommand("advancement grant @p only regenerative_agriculture:route/weather_station");
        context.waitTicks(20);
        context.runOnClient(client -> client.gui.toastManager().clear());
        String[][] shots = {{"ROUTE", "manual_route"}, {"SEASONS", "manual_seasons"}, {"KITCHEN", "manual_kitchen"},
                {"SOIL", "manual_soil"}, {"BREWING", "manual_brewing"}};
        for (String[] shot : shots) {
            context.setScreen(() -> new ManualScreen().openTab(shot[0]));
            context.waitTicks(5);
            context.takeScreenshot(shot[1]);
        }
        context.setScreen(() -> new ManualScreen().selectCrop(ModCrops.TOMATO.block()));
        context.waitTicks(5);
        context.takeScreenshot("manual_crop_sheet");
        context.setScreen(() -> null);
    }

    private static void run(TestSingleplayerContext world, String command) {
        world.getServer().runCommand("execute as @p at @s run " + command);
    }

    /** A tomato between basil (companion) and potatoes (antagonist), harvested twice, analysed with the soil kit. */
    private static void soil(ClientGameTestContext context, TestSingleplayerContext world) {
        String[] commands = {
                "tp @p ~20 ~ ~",
                "fill ~-3 ~-1 ~2 ~3 ~-1 ~2 farmland",
                "setblock ~-1 ~ ~2 regenerative_agriculture:basil_crop[age=7]",
                "setblock ~ ~ ~2 regenerative_agriculture:tomato_crop[age=7]",
                "setblock ~1 ~ ~2 potatoes[age=7]",
                "setblock ~3 ~ ~4 regenerative_agriculture:compost_bin[level=5]",
                "item replace entity @p weapon.mainhand with regenerative_agriculture:soil_test_kit",
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
        world.getServer().runCommand("setblock %d %d %d regenerative_agriculture:weather_station"
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
                "fill ~-3 ~ ~4 ~3 ~ ~5 regenerative_agriculture:tomato_crop[age=7]",
                "setblock ~-2 ~3 ~4 regenerative_agriculture:grow_light",
                "setblock ~2 ~3 ~4 regenerative_agriculture:grow_light",
                "setblock ~ ~3 ~1 regenerative_agriculture:grow_light",
                "setblock ~4 ~3 ~6 regenerative_agriculture:extractor",
                "setblock ~4 ~4 ~6 iron_bars",
                "item replace entity @p weapon.mainhand with regenerative_agriculture:thermohygrometer",
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
