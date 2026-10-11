package org.example.pz.projectzomboid_prd.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import org.example.pz.projectzomboid_prd.guide.Guide;
import org.example.pz.projectzomboid_prd.guide.RouteNodes;

public class RouteGameTest {

    /** Every node of the manual's route is a loaded advancement, linked to its parent. */
    @GameTest
    public void everyNodeIsAnAdvancement(GameTestHelper helper) {
        for (RouteNodes.Node node : RouteNodes.ALL) {
            AdvancementHolder holder = helper.getLevel().getServer().getAdvancements().get(node.advancement());
            helper.assertTrue(holder != null, "missing advancement for route node " + node.id());
            if (!node.parents().isEmpty()) {
                String parent = RouteNodes.byId(node.parents().getFirst()).orElseThrow().advancement().toString();
                helper.assertTrue(holder.value().parent().map(Object::toString).orElse("").equals(parent),
                        node.id() + " should hang from " + parent);
            }
        }
        helper.succeed();
    }

    /** Harvesting under glass or in a grow room completes the matching nodes. */
    @GameTest
    public void shelterHarvestTriggers(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        Guide.GREENHOUSE_HARVEST.trigger(player);
        Guide.GROW_ROOM_HARVEST.trigger(player);
        for (String id : new String[]{"greenhouse", "grow_room"}) {
            AdvancementHolder holder = helper.getLevel().getServer().getAdvancements()
                    .get(RouteNodes.byId(id).orElseThrow().advancement());
            helper.assertTrue(player.getAdvancements().getOrStartProgress(holder).isDone(), id + " should be done");
        }
        helper.succeed();
    }
}
