package org.example.pz.projectzomboid_prd.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.example.pz.projectzomboid_prd.Projectzomboid_prd;

/**
 * Server → client: what the client needs to compute the calendar and climate locally.
 * The climate seed is a hash of the world seed, so the world seed itself is never sent.
 */
public record SeasonConfigPayload(int daysPerSeason, long climateSeed) implements CustomPacketPayload {
    public static final Type<SeasonConfigPayload> TYPE = new Type<>(Projectzomboid_prd.id("season_config"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SeasonConfigPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SeasonConfigPayload::daysPerSeason,
            ByteBufCodecs.LONG, SeasonConfigPayload::climateSeed,
            SeasonConfigPayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
