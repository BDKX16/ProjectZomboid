package io.github.bdkx16.regenerativeagriculture.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import io.github.bdkx16.regenerativeagriculture.RegenerativeAgriculture;

/**
 * Server → client: open the weather station screen for the station at {@code pos}.
 * The client computes the report itself from the synced climate and crop profiles.
 */
public record OpenWeatherStationPayload(BlockPos pos) implements CustomPacketPayload {
    public static final Type<OpenWeatherStationPayload> TYPE = new Type<>(RegenerativeAgriculture.id("open_weather_station"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenWeatherStationPayload> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, OpenWeatherStationPayload::pos,
            OpenWeatherStationPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
