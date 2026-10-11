package org.example.pz.projectzomboid_prd.season;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.clock.WorldClock;
import net.minecraft.world.clock.WorldClocks;
import org.example.pz.projectzomboid_prd.network.OpenWeatherStationPayload;
import org.example.pz.projectzomboid_prd.network.SeasonConfigPayload;
import org.example.pz.projectzomboid_prd.registry.ModGameRules;

/** Server-side access point to the calendar and climate of a running world. */
public final class SeasonManager {
    private SeasonManager() {
    }

    public static void init() {
        PayloadTypeRegistry.clientboundPlay().register(SeasonConfigPayload.TYPE, SeasonConfigPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(OpenWeatherStationPayload.TYPE, OpenWeatherStationPayload.CODEC);
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> sync(handler.getPlayer()));
        GameRuleEvents.changeCallback(ModGameRules.SEASON_LENGTH).register((value, server) -> syncAll(server));
    }

    public static int daysPerSeason(MinecraftServer server) {
        return server.overworld().getGameRules().get(ModGameRules.SEASON_LENGTH);
    }

    public static SeasonCalendar calendar(MinecraftServer server) {
        return new SeasonCalendar(daysPerSeason(server));
    }

    public static long climateSeed(MinecraftServer server) {
        return ClimateModel.mix(server.overworld().getSeed() ^ 0x5EA50A5L);
    }

    public static ClimateModel climate(MinecraftServer server) {
        return new ClimateModel(calendar(server), climateSeed(server));
    }

    public static long ticks(MinecraftServer server) {
        return server.overworld().getOverworldClockTime();
    }

    public static SeasonCalendar.Date today(MinecraftServer server) {
        return calendar(server).dateAt(ticks(server));
    }

    public static double temperatureAt(ServerLevel level, BlockPos pos) {
        float biomeTemperature = level.getBiome(pos).value().getBaseTemperature();
        return climate(level.getServer()).temperatureAt(biomeTemperature, pos.getY(), ticks(level.getServer()));
    }

    /** Moves the overworld clock forward to the first day of the season, keeping the time of day. */
    public static SeasonCalendar.Date setSeason(MinecraftServer server, Season season) {
        long ticks = ticks(server);
        SeasonCalendar calendar = calendar(server);
        long targetDay = calendar.nextStartOf(season, SeasonCalendar.absoluteDay(ticks) + 1);
        long newTicks = SeasonCalendar.startOfDay(targetDay) + SeasonCalendar.ticksIntoDay(ticks);
        Holder<WorldClock> clock = server.registryAccess().getOrThrow(WorldClocks.OVERWORLD);
        server.clockManager().setTotalTicks(clock, newTicks);
        return calendar.dateAt(newTicks);
    }

    public static void sync(ServerPlayer player) {
        MinecraftServer server = player.level().getServer();
        ServerPlayNetworking.send(player, new SeasonConfigPayload(daysPerSeason(server), climateSeed(server)));
    }

    public static void syncAll(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            sync(player);
        }
    }
}
