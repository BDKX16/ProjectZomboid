package org.example.pz.projectzomboid_prd.item;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.example.pz.projectzomboid_prd.network.OpenManualPayload;

/** The regenerative farming manual: right click opens the book (route map, seasons, crop sheets, guides). */
public class ManualItem extends DescribedItem {

    public ManualItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer serverPlayer) {
            ServerPlayNetworking.send(serverPlayer, OpenManualPayload.INSTANCE);
        }
        return InteractionResult.SUCCESS;
    }
}
