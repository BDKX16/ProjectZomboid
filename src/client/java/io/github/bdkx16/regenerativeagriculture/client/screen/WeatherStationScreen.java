package io.github.bdkx16.regenerativeagriculture.client.screen;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import io.github.bdkx16.regenerativeagriculture.RegenerativeAgriculture;
import io.github.bdkx16.regenerativeagriculture.block.SeasonalCropBlock;
import io.github.bdkx16.regenerativeagriculture.client.season.ClientSeasonState;
import io.github.bdkx16.regenerativeagriculture.crop.CropProfile;
import io.github.bdkx16.regenerativeagriculture.crop.CropProfiles;
import io.github.bdkx16.regenerativeagriculture.crop.PlantingAdvisor;
import io.github.bdkx16.regenerativeagriculture.season.ClimateModel;
import io.github.bdkx16.regenerativeagriculture.season.Season;
import io.github.bdkx16.regenerativeagriculture.season.SeasonCalendar;
import io.github.bdkx16.regenerativeagriculture.season.SeasonText;
import io.github.bdkx16.regenerativeagriculture.season.TemperatureBand;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Weather report for the station's position: today, a 7-day forecast with temperature bars, and which crops to sow
 * outdoors now or prepare for next season. Computed on the client from the synced climate and crop profiles.
 */
public class WeatherStationScreen extends Screen {
    private static final String KEY = "screen." + RegenerativeAgriculture.MOD_ID + ".weather_station.";
    private static final int WIDTH = 330;
    private static final int HEIGHT = 222;
    private static final int BACKGROUND = 0xE8121820;
    private static final int BORDER = 0xFF5B7086;
    private static final int SEPARATOR = 0xFF2E3B48;
    private static final int TEXT = 0xFFFFFFFF;
    private static final int MUTED = 0xFF9AA8B5;
    private static final int HEADER = 0xFFE0C068;
    /** Temperature range drawn by the forecast bars, in °C. */
    private static final double BAR_MIN = -15;
    private static final double BAR_MAX = 40;
    private static final int BAR_WIDTH = 70;
    private static final int ICON = 18;

    private final BlockPos pos;
    private @Nullable Report report;
    private int refreshTimer;

    public WeatherStationScreen(BlockPos pos) {
        super(Component.translatable("block." + RegenerativeAgriculture.MOD_ID + ".weather_station"));
        this.pos = pos;
    }

    @Override
    protected void init() {
        report = computeReport();
    }

    @Override
    public void tick() {
        if (++refreshTimer >= 20) {
            refreshTimer = 0;
            report = computeReport();
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private @Nullable Report computeReport() {
        ClimateModel climate = ClientSeasonState.climate();
        if (climate == null || minecraft.level == null) {
            return null;
        }
        long ticks = minecraft.level.getOverworldClockTime();
        long today = SeasonCalendar.absoluteDay(ticks);
        Holder<Biome> biome = minecraft.level.getBiome(pos);
        float biomeTemperature = biome.value().getBaseTemperature();
        int y = pos.getY();

        List<ClimateModel.DayClimate> forecast = new ArrayList<>();
        for (int i = 0; i < PlantingAdvisor.FORECAST_DAYS; i++) {
            forecast.add(climate.day(biomeTemperature, y, today + i));
        }

        List<CropProfile> profiles = minecraft.level.registryAccess().lookup(CropProfiles.REGISTRY_KEY)
                .map(Registry::stream).map(stream -> stream.toList()).orElse(List.of());
        List<ItemStack> plantNow = icons(PlantingAdvisor.plantNow(profiles, CropProfile::requirements, climate, biomeTemperature, y, today));
        List<ItemStack> nextSeason = icons(PlantingAdvisor.nextSeason(profiles, CropProfile::requirements, climate, today));

        Component biomeName = biome.unwrapKey()
                .map(key -> (Component) Component.translatable(Util.makeDescriptionId("biome", key.identifier())))
                .orElse(Component.empty());
        return new Report(ticks, climate.calendar(), climate.calendar().dateOfDay(today),
                climate.temperatureAt(biomeTemperature, y, ticks), biomeName, forecast, plantNow, nextSeason,
                PlantingAdvisor.daysUntilNextSeason(climate.calendar(), today));
    }

    private static List<ItemStack> icons(List<CropProfile> crops) {
        List<ItemStack> stacks = new ArrayList<>();
        for (CropProfile crop : crops) {
            // Our crops show their produce (seed sprites are too small to tell apart); vanilla ones their item.
            ItemStack stack = crop.block() instanceof SeasonalCropBlock seasonal
                    ? new ItemStack(seasonal.getProduce())
                    : new ItemStack(crop.block().asItem());
            if (!stack.isEmpty()) {
                stacks.add(stack);
            }
        }
        return stacks;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        int left = (width - WIDTH) / 2;
        int top = (height - HEIGHT) / 2;
        graphics.fill(left, top, left + WIDTH, top + HEIGHT, BACKGROUND);
        graphics.outline(left, top, WIDTH, HEIGHT, BORDER);
        graphics.centeredText(font, title, width / 2, top + 7, HEADER);

        if (report == null) {
            graphics.centeredText(font, Component.translatable(KEY + "no_data"), width / 2, top + HEIGHT / 2, MUTED);
            return;
        }
        Report r = report;
        graphics.centeredText(font, r.biomeName(), width / 2, top + 18, MUTED);
        graphics.fill(left + 6, top + 30, left + WIDTH - 6, top + 31, SEPARATOR);

        drawToday(graphics, r, left + 10, top + 36);
        drawForecast(graphics, r, left + 128, top + 36);

        graphics.fill(left + 6, top + 140, left + WIDTH - 6, top + 141, SEPARATOR);
        int rowY = top + 146;
        graphics.text(font, Component.translatable(KEY + "plant_now"), left + 10, rowY, HEADER, false);
        drawIcons(graphics, r.plantNow(), left + 10, rowY + 10, mouseX, mouseY, KEY + "plant_now.none");

        rowY += 36;
        Season next = Season.byIndex(r.date().season().ordinal() + 1);
        graphics.text(font, Component.translatable(KEY + "next_season", next.displayName(), r.daysUntilNextSeason()),
                left + 10, rowY, HEADER, false);
        drawIcons(graphics, r.nextSeason(), left + 10, rowY + 10, mouseX, mouseY, KEY + "next_season.none");
    }

    private void drawToday(GuiGraphicsExtractor graphics, Report r, int x, int y) {
        graphics.text(font, Component.translatable(KEY + "today"), x, y, HEADER, false);
        graphics.text(font, r.date().season().displayName()
                .append(Component.literal(" · ").withStyle(ChatFormatting.GRAY))
                .append(Component.translatable(KEY + "day_of_season", r.date().dayOfSeason(), r.calendar().daysPerSeason())
                        .withStyle(ChatFormatting.GRAY)), x, y + 14, TEXT, false);
        graphics.text(font, SeasonText.date(r.date()), x, y + 26, TEXT, false);
        graphics.text(font, Component.literal(SeasonText.time(r.ticks())), x, y + 38, MUTED, false);

        TemperatureBand band = TemperatureBand.of(r.temperatureNow());
        graphics.pose().pushMatrix();
        graphics.pose().translate(x, y + 54);
        graphics.pose().scale(2.0f, 2.0f);
        graphics.text(font, SeasonText.celsius(r.temperatureNow()), 0, 0, 0xFF000000 | band.color(), true);
        graphics.pose().popMatrix();
        graphics.text(font, band.displayName(), x, y + 76, TEXT, false);
    }

    private void drawForecast(GuiGraphicsExtractor graphics, Report r, int x, int y) {
        graphics.text(font, Component.translatable(KEY + "forecast"), x, y, HEADER, false);
        for (int i = 0; i < r.forecast().size(); i++) {
            ClimateModel.DayClimate day = r.forecast().get(i);
            int rowY = y + 14 + i * 13;
            Component label = i == 0 ? Component.translatable(KEY + "today_short")
                    : SeasonText.shortDate(r.calendar().dateOfDay(day.day()));
            graphics.text(font, label, x, rowY, TEXT, false);
            graphics.text(font, SeasonText.celsius(day.min()), x + 52, rowY, 0xFF000000 | TemperatureBand.of(day.min()).color(), false);

            int barX = x + 78;
            graphics.fill(barX, rowY + 2, barX + BAR_WIDTH, rowY + 6, SEPARATOR);
            int from = barX + barOffset(day.min());
            int to = Math.max(from + 1, barX + barOffset(day.max()));
            graphics.fill(from, rowY + 2, to, rowY + 6, 0xFF000000 | day.band().color());

            graphics.text(font, SeasonText.celsius(day.max()), barX + BAR_WIDTH + 4, rowY, 0xFF000000 | TemperatureBand.of(day.max()).color(), false);
            if (day.frost()) {
                graphics.text(font, Component.literal("❄"), barX + BAR_WIDTH + 30, rowY, 0xFF000000 | TemperatureBand.FROST.color(), false);
            }
        }
    }

    private static int barOffset(double celsius) {
        double t = (Math.clamp(celsius, BAR_MIN, BAR_MAX) - BAR_MIN) / (BAR_MAX - BAR_MIN);
        return (int) Math.round(t * BAR_WIDTH);
    }

    private void drawIcons(GuiGraphicsExtractor graphics, List<ItemStack> stacks, int x, int y, int mouseX, int mouseY, String emptyKey) {
        if (stacks.isEmpty()) {
            graphics.text(font, Component.translatable(emptyKey), x, y + 4, MUTED, false);
            return;
        }
        for (int i = 0; i < stacks.size(); i++) {
            int iconX = x + i * ICON;
            graphics.item(stacks.get(i), iconX, y);
            if (mouseX >= iconX && mouseX < iconX + 16 && mouseY >= y && mouseY < y + 16) {
                graphics.setTooltipForNextFrame(stacks.get(i).getHoverName(), mouseX, mouseY);
            }
        }
    }

    private record Report(long ticks, SeasonCalendar calendar, SeasonCalendar.Date date, double temperatureNow,
                          Component biomeName, List<ClimateModel.DayClimate> forecast,
                          List<ItemStack> plantNow, List<ItemStack> nextSeason, int daysUntilNextSeason) {
    }
}
