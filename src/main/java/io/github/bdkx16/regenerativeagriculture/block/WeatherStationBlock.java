package io.github.bdkx16.regenerativeagriculture.block;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import io.github.bdkx16.regenerativeagriculture.network.OpenWeatherStationPayload;

/** Right-click opens the weather report: date, temperature, 7-day forecast and planting advice for this spot. */
public class WeatherStationBlock extends Block {

    public WeatherStationBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (player instanceof ServerPlayer serverPlayer) {
            ServerPlayNetworking.send(serverPlayer, new OpenWeatherStationPayload(pos));
        }
        return InteractionResult.SUCCESS;
    }
}
