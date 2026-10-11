package org.example.pz.projectzomboid_prd.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import org.example.pz.projectzomboid_prd.Projectzomboid_prd;
import org.example.pz.projectzomboid_prd.crop.CropClimate;
import org.example.pz.projectzomboid_prd.crop.GrowthCalculator;
import org.example.pz.projectzomboid_prd.greenhouse.MicroClimate;
import org.example.pz.projectzomboid_prd.greenhouse.Mold;
import org.example.pz.projectzomboid_prd.greenhouse.Shelter;
import org.example.pz.projectzomboid_prd.greenhouse.ShelterDetector;
import org.example.pz.projectzomboid_prd.season.ClimateModel;
import org.example.pz.projectzomboid_prd.season.Season;
import org.example.pz.projectzomboid_prd.season.SeasonCalendar;
import org.example.pz.projectzomboid_prd.season.SeasonManager;
import org.example.pz.projectzomboid_prd.season.SeasonText;
import org.example.pz.projectzomboid_prd.season.TemperatureBand;

/**
 * {@code /season} – current date and temperature;
 * {@code /season forecast [days]} – forecast at the caller's position;
 * {@code /season crop <pos>} – how the crop at a position is growing and why;
 * {@code /season set <season>} – jump to the start of a season (operators).
 */
public final class SeasonCommand {
    private static final String KEY = "commands." + Projectzomboid_prd.MOD_ID + ".season.";

    private SeasonCommand() {
    }

    public static void init() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> register(dispatcher));
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> set = Commands.literal("set")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS));
        for (Season season : Season.values()) {
            set.then(Commands.literal(season.id()).executes(ctx -> setSeason(ctx.getSource(), season)));
        }

        dispatcher.register(Commands.literal("season")
                .executes(ctx -> info(ctx.getSource()))
                .then(Commands.literal("forecast")
                        .executes(ctx -> forecast(ctx.getSource(), 7))
                        .then(Commands.argument("days", IntegerArgumentType.integer(1, 30))
                                .executes(ctx -> forecast(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "days")))))
                .then(Commands.literal("crop")
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                .executes(ctx -> crop(ctx.getSource(), BlockPosArgument.getLoadedBlockPos(ctx, "pos")))))
                .then(set));
    }

    private static int info(CommandSourceStack source) {
        MinecraftServer server = source.getServer();
        long ticks = SeasonManager.ticks(server);
        SeasonCalendar.Date date = SeasonManager.today(server);
        BlockPos pos = BlockPos.containing(source.getPosition());
        double temperature = SeasonManager.temperatureAt(source.getLevel(), pos);

        source.sendSuccess(() -> Component.translatable(KEY + "info",
                date.season().displayName(),
                date.dayOfSeason(),
                SeasonManager.daysPerSeason(server),
                SeasonText.date(date),
                SeasonText.time(ticks)), false);
        source.sendSuccess(() -> Component.translatable(KEY + "temperature",
                SeasonText.celsius(temperature).withColor(TemperatureBand.of(temperature).color()),
                TemperatureBand.of(temperature).displayName()), false);
        return date.dayOfSeason();
    }

    private static int forecast(CommandSourceStack source, int days) {
        MinecraftServer server = source.getServer();
        ServerLevel level = source.getLevel();
        BlockPos pos = BlockPos.containing(source.getPosition());
        ClimateModel climate = SeasonManager.climate(server);
        float biomeTemperature = level.getBiome(pos).value().getBaseTemperature();
        long today = SeasonCalendar.absoluteDay(SeasonManager.ticks(server));

        source.sendSuccess(() -> Component.translatable(KEY + "forecast.header", days), false);
        for (int i = 0; i < days; i++) {
            ClimateModel.DayClimate day = climate.day(biomeTemperature, pos.getY(), today + i);
            SeasonCalendar.Date date = climate.calendar().dateOfDay(day.day());
            MutableComponent line = Component.translatable(KEY + "forecast.day",
                    SeasonText.shortDate(date),
                    SeasonText.celsius(day.min()).withColor(TemperatureBand.of(day.min()).color()),
                    SeasonText.celsius(day.max()).withColor(TemperatureBand.of(day.max()).color()),
                    day.band().displayName());
            if (day.frost()) {
                line.append(Component.translatable(KEY + "forecast.frost").withColor(TemperatureBand.FROST.color()));
            }
            source.sendSuccess(() -> line, false);
        }
        return days;
    }

    private static int crop(CommandSourceStack source, BlockPos pos) {
        Block block = source.getLevel().getBlockState(pos).getBlock();
        GrowthCalculator.Growth growth = CropClimate.growth(source.getLevel(), pos, block).orElse(null);
        if (growth == null) {
            source.sendFailure(Component.translatable(KEY + "crop.none", block.getName()));
            return 0;
        }
        int percent = Math.round(growth.factor() * 100);
        source.sendSuccess(() -> Component.translatable(KEY + "crop.info",
                block.getName(),
                percent,
                growth.stress().displayName()), false);
        Shelter shelter = ShelterDetector.detect(source.getLevel(), pos);
        double humidity = MicroClimate.humidity(source.getLevel(), pos, SeasonManager.climate(source.getServer()),
                SeasonManager.ticks(source.getServer()), shelter);
        source.sendSuccess(() -> Component.translatable(KEY + "crop.shelter", shelter.displayName()), false);
        source.sendSuccess(() -> Component.translatable(Mold.atRisk(shelter, humidity) ? KEY + "crop.humidity.mold" : KEY + "crop.humidity",
                Math.round(humidity)), false);
        return percent;
    }

    private static int setSeason(CommandSourceStack source, Season season) {
        SeasonCalendar.Date date = SeasonManager.setSeason(source.getServer(), season);
        source.sendSuccess(() -> Component.translatable(KEY + "set", season.displayName(), SeasonText.date(date)), true);
        return 1;
    }
}
