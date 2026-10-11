package org.example.pz.projectzomboid_prd.guide;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.advancements.triggers.PlayerTrigger;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.CropBlock;
import org.example.pz.projectzomboid_prd.Projectzomboid_prd;
import org.example.pz.projectzomboid_prd.greenhouse.Shelter;
import org.example.pz.projectzomboid_prd.greenhouse.ShelterDetector;
import org.example.pz.projectzomboid_prd.network.OpenManualPayload;
import org.example.pz.projectzomboid_prd.registry.ModItems;

/**
 * The manual: the payload that opens it, a copy for every new player, and the advancement triggers the route
 * needs that vanilla does not have (harvesting under glass or in a grow room).
 */
public final class Guide {
    /** Fired when a player harvests a ripe crop in a greenhouse. */
    public static final PlayerTrigger GREENHOUSE_HARVEST = trigger("greenhouse_harvest");
    /** Fired when a player harvests a ripe crop in an underground grow room. */
    public static final PlayerTrigger GROW_ROOM_HARVEST = trigger("grow_room_harvest");

    private static final AttachmentType<Boolean> RECEIVED_MANUAL = AttachmentRegistry.create(Projectzomboid_prd.id("received_manual"),
            builder -> builder.persistent(Codec.BOOL).copyOnDeath());

    private Guide() {
    }

    public static void init() {
        PayloadTypeRegistry.clientboundPlay().register(OpenManualPayload.TYPE, OpenManualPayload.CODEC);

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.getPlayer();
            if (!player.hasAttached(RECEIVED_MANUAL)) {
                player.setAttached(RECEIVED_MANUAL, true);
                ModItems.give(player, new ItemStack(ModItems.MANUAL));
            }
        });

        PlayerBlockBreakEvents.AFTER.register((level, player, pos, state, blockEntity) -> {
            if (player instanceof ServerPlayer serverPlayer && state.getBlock() instanceof CropBlock crop && crop.isMaxAge(state)) {
                Shelter.Type shelter = ShelterDetector.detect(level, pos).type();
                switch (shelter) {
                    case GREENHOUSE, GREENHOUSE_OVERHEATED -> GREENHOUSE_HARVEST.trigger(serverPlayer);
                    case GROW_ROOM -> GROW_ROOM_HARVEST.trigger(serverPlayer);
                    default -> {
                    }
                }
            }
        });
    }

    private static PlayerTrigger trigger(String name) {
        return Registry.register(BuiltInRegistries.TRIGGER_TYPES, Projectzomboid_prd.id(name), new PlayerTrigger());
    }
}
