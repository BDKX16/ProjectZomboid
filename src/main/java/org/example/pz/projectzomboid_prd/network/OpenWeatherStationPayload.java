package org.example.pz.projectzomboid_prd.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.example.pz.projectzomboid_prd.Projectzomboid_prd;

/**
 * Server → client: open the weather station screen for the station at {@code pos}.
 * The client computes the report itself from the synced climate and crop profiles.
 */
public record OpenWeatherStationPayload(BlockPos pos) implements CustomPacketPayload {
    public static final Type<OpenWeatherStationPayload> TYPE = new Type<>(Projectzomboid_prd.id("open_weather_station"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenWeatherStationPayload> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, OpenWeatherStationPayload::pos,
            OpenWeatherStationPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
