package org.example.pz.projectzomboid_prd.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.example.pz.projectzomboid_prd.Projectzomboid_prd;

/** Server → client: open the farming manual. */
public record OpenManualPayload() implements CustomPacketPayload {
    public static final OpenManualPayload INSTANCE = new OpenManualPayload();
    public static final Type<OpenManualPayload> TYPE = new Type<>(Projectzomboid_prd.id("open_manual"));
    public static final StreamCodec<ByteBuf, OpenManualPayload> CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
