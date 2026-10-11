package io.github.bdkx16.regenerativeagriculture.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import io.github.bdkx16.regenerativeagriculture.RegenerativeAgriculture;

/** Server → client: open the farming manual. */
public record OpenManualPayload() implements CustomPacketPayload {
    public static final OpenManualPayload INSTANCE = new OpenManualPayload();
    public static final Type<OpenManualPayload> TYPE = new Type<>(RegenerativeAgriculture.id("open_manual"));
    public static final StreamCodec<ByteBuf, OpenManualPayload> CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
