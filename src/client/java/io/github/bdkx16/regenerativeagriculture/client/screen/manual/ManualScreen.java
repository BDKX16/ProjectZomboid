package io.github.bdkx16.regenerativeagriculture.client.screen.manual;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import io.github.bdkx16.regenerativeagriculture.RegenerativeAgriculture;
import io.github.bdkx16.regenerativeagriculture.block.SeasonalCropBlock;
import io.github.bdkx16.regenerativeagriculture.client.season.ClientSeasonState;
import io.github.bdkx16.regenerativeagriculture.crop.CropProfile;
import io.github.bdkx16.regenerativeagriculture.crop.CropProfiles;
import io.github.bdkx16.regenerativeagriculture.crop.GrowthRequirements;
import io.github.bdkx16.regenerativeagriculture.guide.RouteNodes;
import io.github.bdkx16.regenerativeagriculture.processing.ProcessingRecipes;
import io.github.bdkx16.regenerativeagriculture.registry.ModBlocks;
import io.github.bdkx16.regenerativeagriculture.registry.ModCrops;
import io.github.bdkx16.regenerativeagriculture.registry.ModFoods;
import io.github.bdkx16.regenerativeagriculture.registry.ModItems;
import io.github.bdkx16.regenerativeagriculture.season.ClimateModel;
import io.github.bdkx16.regenerativeagriculture.season.Season;
import io.github.bdkx16.regenerativeagriculture.season.SeasonText;
import io.github.bdkx16.regenerativeagriculture.worldgen.ModWorldGen;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * The farming manual, drawn as an open book with bookmarks: the route map, the season route, a sheet for every
 * crop, the kitchen and illustrated chapters about soil, climate, drying and brewing. Everything is computed on the
 * client from synced data (crop profiles, advancements, item descriptions).
 */
public class ManualScreen extends Screen {
    private static final String KEY = "manual." + RegenerativeAgriculture.MOD_ID + ".";
    private static final Identifier BOOK = RegenerativeAgriculture.id("textures/gui/manual.png");
    private static final int BOOK_W = 300;
    private static final int BOOK_H = 190;
    private static final int TEX_W = 512;
    private static final int TEX_H = 256;
    private static final int PAGE_W = 122;
    private static final int LEFT_PAGE_X = 18;
    private static final int RIGHT_PAGE_X = 160;
    private static final int PAGE_Y = 16;
    private static final int PAGE_BOTTOM = 172;
    private static final int LINE = 10;
    private static final int INK = 0xFF3B2A1A;
    private static final int TITLE = 0xFF7A3E10;
    private static final int MUTED = 0xFF7D6A50;
    private static final int TAB_W = 26;
    private static final int TAB_H = 20;

    private enum Tab {
        ROUTE(() -> ModItems.MANUAL),
        SEASONS(() -> ModBlocks.WEATHER_STATION.asItem()),
        CROPS(() -> ModCrops.TOMATO.produce()),
        SOIL(() -> ModItems.COMPOST),
        CLIMATE(() -> ModItems.THERMOHYGROMETER),
        PROCESSING(() -> ModBlocks.DRYING_RACK.asItem()),
        KITCHEN(() -> ModFoods.LOCRO.item()),
        BREWING(() -> ModItems.MATE);

        final Supplier<Item> icon;

        Tab(Supplier<Item> icon) {
            this.icon = icon;
        }

        String key() {
            return KEY + name().toLowerCase(java.util.Locale.ROOT);
        }
    }

    private final RouteMap routeMap = new RouteMap();
    private Tab tab = Tab.ROUTE;
    private int spread;
    private int selectedCrop;
    private int selectedMeal;
    private int left;
    private int top;
    private List<CropProfile> crops = List.of();
    private @Nullable Block pendingCrop;

    public ManualScreen() {
        super(Component.translatable("item." + RegenerativeAgriculture.MOD_ID + ".manual"));
    }

    @Override
    protected void init() {
        left = (width - BOOK_W) / 2 - TAB_W / 2;
        top = (height - BOOK_H) / 2;
        crops = minecraft.level == null ? List.of() : minecraft.level.registryAccess().lookup(CropProfiles.REGISTRY_KEY)
                .map(Registry::stream).map(stream -> stream
                        .sorted(Comparator.comparing((CropProfile crop) -> !(crop.block() instanceof SeasonalCropBlock))
                                .thenComparing(crop -> BuiltInRegistries.BLOCK.getKey(crop.block()).toString()))
                        .toList())
                .orElse(List.of());
        for (int i = 0; i < crops.size(); i++) {
            if (crops.get(i).block() == pendingCrop) {
                selectedCrop = i;
            }
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // ---------------------------------------------------------------- drawing

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        drawTabs(graphics, mouseX, mouseY, false);
        graphics.blit(RenderPipelines.GUI_TEXTURED, BOOK, left, top, 0, 0, BOOK_W, BOOK_H, TEX_W, TEX_H);
        drawTabs(graphics, mouseX, mouseY, true);

        switch (tab) {
            case ROUTE -> drawRoute(graphics, mouseX, mouseY);
            case SEASONS -> drawSeasons(graphics, mouseX, mouseY);
            case CROPS -> drawCrops(graphics, mouseX, mouseY);
            case KITCHEN -> drawKitchen(graphics, mouseX, mouseY);
            default -> drawText(graphics);
        }
    }

    /** Bookmarks sticking out of the right edge; the open one is drawn over the book. */
    private void drawTabs(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean active) {
        Tab[] tabs = Tab.values();
        for (int i = 0; i < tabs.length; i++) {
            if ((tabs[i] == tab) != active) {
                continue;
            }
            int x = tabX(tabs[i]);
            int y = tabY(i);
            int colour = TAB_COLOURS[i % TAB_COLOURS.length];
            graphics.fill(x, y, x + TAB_W, y + TAB_H, 0xFF000000 | darker(colour));
            graphics.fill(x, y + 1, x + TAB_W - 1, y + TAB_H - 1, 0xFF000000 | colour);
            graphics.item(new ItemStack(tabs[i].icon.get()), x + 6, y + 2);
            if (mouseX >= x && mouseX < x + TAB_W && mouseY >= y && mouseY < y + TAB_H) {
                graphics.setTooltipForNextFrame(Component.translatable(tabs[i].key()), mouseX, mouseY);
            }
        }
    }

    private static final int[] TAB_COLOURS = {0xB8862F, 0x4E8BBF, 0x5E9E46, 0x7B5A3A, 0x5AA3A0, 0xB0703A, 0xC0564A, 0x8A6BB0};

    private int tabX(Tab candidate) {
        return left + BOOK_W - (candidate == tab ? 6 : 2);
    }

    private int tabY(int index) {
        return top + 10 + index * (TAB_H + 2);
    }

    private static int darker(int rgb) {
        return ((rgb >> 16 & 0xFF) * 3 / 5) << 16 | ((rgb >> 8 & 0xFF) * 3 / 5) << 8 | (rgb & 0xFF) * 3 / 5;
    }

    private void title(GuiGraphicsExtractor graphics, Component text, int x, int y) {
        graphics.text(font, text, x, y, TITLE, false);
        graphics.fill(x, y + 10, x + PAGE_W, y + 11, 0x557A3E10);
    }

    // ---------------------------------------------------------------- route

    private void drawRoute(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        int x = left + 12;
        int y = top + 12;
        int w = BOOK_W - 24;
        int h = BOOK_H - 34;
        RouteNodes.Node hovered = routeMap.draw(graphics, x, y, w, h, mouseX, mouseY);
        graphics.text(font, Component.translatable(KEY + "route.progress", RouteMap.completed(), RouteNodes.ALL.size()),
                left + LEFT_PAGE_X, top + BOOK_H - 18, MUTED, false);
        graphics.text(font, Component.translatable(KEY + "route.drag"), left + RIGHT_PAGE_X, top + BOOK_H - 18, MUTED, false);
        if (hovered != null) {
            graphics.setTooltipForNextFrame(font, RouteMap.tooltip(hovered, font), Optional.empty(), mouseX, mouseY);
        }
    }

    // ---------------------------------------------------------------- seasons

    private void drawSeasons(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        int lx = left + LEFT_PAGE_X;
        int rx = left + RIGHT_PAGE_X;
        title(graphics, Component.translatable(Tab.SEASONS.key()), lx, top + PAGE_Y);
        ClimateModel climate = ClientSeasonState.climate();
        int y = top + PAGE_Y + 16;
        int days = 15;
        if (climate != null && minecraft.level != null) {
            var date = climate.calendar().dateAt(minecraft.level.getOverworldClockTime());
            y = paragraph(graphics, Component.translatable(KEY + "seasons.today", SeasonText.date(date)), lx, y, INK) + 2;
            days = climate.calendar().daysPerSeason();
        }
        y = paragraph(graphics, Component.translatable(KEY + "seasons.text", days), lx, y, MUTED) + 4;
        // The year starts in spring: spring on the left page, the other three on the right
        drawSeasonRow(graphics, Season.SPRING, lx, y, mouseX, mouseY);
        int ry = top + PAGE_Y;
        for (Season season : new Season[]{Season.SUMMER, Season.AUTUMN, Season.WINTER}) {
            ry = drawSeasonRow(graphics, season, rx, ry, mouseX, mouseY) + 4;
        }
    }

    /** Season name and the crops that grow in it; returns the y below. */
    private int drawSeasonRow(GuiGraphicsExtractor graphics, Season season, int x, int y, int mouseX, int mouseY) {
        graphics.text(font, season.displayName(), x, y, 0xFF000000 | darker(season.color()), false);
        y += LINE;
        List<ItemStack> icons = new ArrayList<>();
        for (CropProfile crop : crops) {
            if (crop.requirements().growsIn(season)) {
                icons.add(icon(crop));
            }
        }
        return drawIcons(graphics, icons, x, y, 7, mouseX, mouseY);
    }

    private int drawIcons(GuiGraphicsExtractor graphics, List<ItemStack> icons, int x, int y, int perRow, int mouseX, int mouseY) {
        for (int i = 0; i < icons.size(); i++) {
            int ix = x + (i % perRow) * 17;
            int iy = y + (i / perRow) * 17;
            graphics.item(icons.get(i), ix, iy);
            if (mouseX >= ix && mouseX < ix + 16 && mouseY >= iy && mouseY < iy + 16) {
                graphics.setTooltipForNextFrame(font, icons.get(i), mouseX, mouseY);
            }
        }
        return y + ((icons.size() + perRow - 1) / perRow) * 17;
    }

    // ---------------------------------------------------------------- crops

    private static ItemStack icon(CropProfile crop) {
        if (crop.block() instanceof SeasonalCropBlock seasonal) {
            return new ItemStack(seasonal.getProduce());
        }
        Item item = crop.block().asItem();
        return new ItemStack(item == Items.AIR ? Items.WHEAT_SEEDS : item);
    }

    private void drawCrops(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        int lx = left + LEFT_PAGE_X;
        title(graphics, Component.translatable(Tab.CROPS.key()), lx, top + PAGE_Y);
        for (int i = 0; i < crops.size(); i++) {
            int x = lx + (i % 7) * 17;
            int y = top + PAGE_Y + 16 + (i / 7) * 17;
            if (i == selectedCrop) {
                graphics.fill(x - 1, y - 1, x + 17, y + 17, 0x66B8862F);
            }
            graphics.item(icon(crops.get(i)), x, y);
        }
        graphics.text(font, Component.translatable(KEY + "crops.hint"), lx, top + PAGE_BOTTOM - 8, MUTED, false);
        if (crops.isEmpty()) {
            return;
        }
        CropProfile crop = crops.get(Math.min(selectedCrop, crops.size() - 1));
        drawHabitats(graphics, crop, lx, top + PAGE_Y + 16 + ((crops.size() + 6) / 7) * 17 + 6);
        drawCropSheet(graphics, crop, left + RIGHT_PAGE_X, top + PAGE_Y, mouseX, mouseY);
    }

    /** Under the crop grid: the biomes where the selected crop's wild plant (and so its first seeds) can be found. */
    private void drawHabitats(GuiGraphicsExtractor graphics, CropProfile crop, int x, int y) {
        Optional<ModCrops.Crop> modCrop = ModCrops.ALL.stream().filter(candidate -> candidate.block() == crop.block()).findFirst();
        if (modCrop.isEmpty() || ModWorldGen.habitats(modCrop.get()).isEmpty()) {
            return;
        }
        MutableComponent biomes = Component.empty();
        for (ModWorldGen.Habitat habitat : ModWorldGen.habitats(modCrop.get())) {
            if (!biomes.getSiblings().isEmpty()) {
                biomes.append(", ");
            }
            biomes.append(habitat.name());
        }
        y = paragraph(graphics, Component.translatable(KEY + "crops.wild", modCrop.get().wildPlant().getName()), x, y, INK);
        paragraph(graphics, biomes, x, y, MUTED);
    }

    private void drawCropSheet(GuiGraphicsExtractor graphics, CropProfile crop, int x, int y, int mouseX, int mouseY) {
        GrowthRequirements needs = crop.requirements();
        GrowthRequirements.TemperatureRange t = needs.temperature();
        title(graphics, crop.block().getName(), x, y);
        y += 15;
        y = line(graphics, Component.translatable(KEY + "crops.family", crop.family().displayName()), x, y);
        MutableComponent seasons = Component.empty();
        for (Season season : needs.seasons()) {
            if (!seasons.getSiblings().isEmpty()) {
                seasons.append(", ");
            }
            seasons.append(season.displayName().withColor(darker(season.color())));
        }
        y = line(graphics, Component.translatable(KEY + "crops.seasons", seasons), x, y);
        y = line(graphics, Component.translatable(KEY + "crops.optimal", fmt(t.optimalMin()), fmt(t.optimalMax())), x, y);
        y = line(graphics, Component.translatable(KEY + "crops.range", fmt(t.min()), fmt(t.max())), x, y);
        y = line(graphics, Component.translatable(needs.frostTolerant() ? KEY + "crops.frost_yes" : KEY + "crops.frost_no"), x, y);
        y = line(graphics, Component.translatable(KEY + "crops.speed", String.format(java.util.Locale.ROOT, "%.2f", needs.growthRate())), x, y);
        if (crop.regrowAge().isPresent()) {
            y = line(graphics, Component.translatable(KEY + "crops.regrows"), x, y);
        }
        y = line(graphics, Component.translatable(crop.nitrogen() >= 0 ? KEY + "crops.fixes" : KEY + "crops.consumes",
                Math.abs(crop.nitrogen())), x, y);
        y = iconLine(graphics, KEY + "crops.companions", crop.companions(), x, y, mouseX, mouseY);
        y = iconLine(graphics, KEY + "crops.antagonists", crop.antagonists(), x, y, mouseX, mouseY);
        ItemStack produce = icon(crop);
        Optional<ProcessingRecipes.Drying> drying = ProcessingRecipes.drying(produce.getItem());
        if (drying.isPresent()) {
            graphics.text(font, Component.translatable(KEY + "crops.dries"), x, y + 4, INK, false);
            graphics.item(new ItemStack(drying.get().output()), x + 70, y);
            hoverItem(graphics, new ItemStack(drying.get().output()), x + 70, y, mouseX, mouseY);
        }
    }

    private int iconLine(GuiGraphicsExtractor graphics, String key, List<Identifier> blocks, int x, int y, int mouseX, int mouseY) {
        if (blocks.isEmpty()) {
            return y;
        }
        graphics.text(font, Component.translatable(key), x, y + 4, INK, false);
        int ix = x + 70;
        for (Identifier id : blocks) {
            Block block = BuiltInRegistries.BLOCK.getValue(id);
            ItemStack stack = crops.stream().filter(crop -> crop.block() == block).findFirst().map(ManualScreen::icon)
                    .orElse(new ItemStack(block.asItem()));
            graphics.item(stack, ix, y);
            hoverItem(graphics, stack, ix, y, mouseX, mouseY);
            ix += 17;
        }
        return y + 18;
    }

    private void hoverItem(GuiGraphicsExtractor graphics, ItemStack stack, int x, int y, int mouseX, int mouseY) {
        if (mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16) {
            graphics.setTooltipForNextFrame(font, stack, mouseX, mouseY);
        }
    }

    private int line(GuiGraphicsExtractor graphics, Component text, int x, int y) {
        return paragraph(graphics, text, x, y, INK);
    }

    private int paragraph(GuiGraphicsExtractor graphics, Component text, int x, int y, int colour) {
        for (FormattedCharSequence part : font.split(text, PAGE_W)) {
            graphics.text(font, part, x, y, colour, false);
            y += LINE;
        }
        return y;
    }

    private static String fmt(float celsius) {
        return Math.round(celsius) + " °C";
    }

    // ---------------------------------------------------------------- kitchen

    private void drawKitchen(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        int lx = left + LEFT_PAGE_X;
        title(graphics, Component.translatable(Tab.KITCHEN.key()), lx, top + PAGE_Y);
        List<ModFoods.Meal> meals = ModFoods.ALL;
        for (int i = 0; i < meals.size(); i++) {
            int x = lx + (i % 7) * 17;
            int y = top + PAGE_Y + 16 + (i / 7) * 17;
            if (i == selectedMeal) {
                graphics.fill(x - 1, y - 1, x + 17, y + 17, 0x66C0564A);
            }
            graphics.item(new ItemStack(meals.get(i).item()), x, y);
        }
        int y = top + PAGE_Y + 16 + ((meals.size() + 6) / 7) * 17 + 6;
        paragraph(graphics, Component.translatable(KEY + "kitchen.text"), lx, y, MUTED);

        ModFoods.Meal meal = meals.get(Math.min(selectedMeal, meals.size() - 1));
        int rx = left + RIGHT_PAGE_X;
        int ry = top + PAGE_Y;
        ItemStack stack = new ItemStack(meal.item());
        title(graphics, stack.getHoverName(), rx, ry);
        ry += 15;
        ry = paragraph(graphics, Component.translatable(meal.item().getDescriptionId() + ".desc"), rx, ry, INK) + 4;
        var food = stack.get(net.minecraft.core.component.DataComponents.FOOD);
        if (food != null) {
            ry = line(graphics, Component.translatable(KEY + "kitchen.hunger", food.nutrition()), rx, ry);
        }
        if (!meal.vegetables().isEmpty()) {
            graphics.text(font, Component.translatable(KEY + "kitchen.vegetables"), rx, ry + 2, INK, false);
            ry += 12;
            drawIcons(graphics, meal.vegetables().stream().map(ItemStack::new).toList(), rx, ry, 7, mouseX, mouseY);
        }
    }

    // ---------------------------------------------------------------- text chapters

    private List<FormattedCharSequence> chapterLines() {
        return font.split(Component.translatable(tab.key() + ".text"), PAGE_W);
    }

    private int linesPerPage(boolean first) {
        return (PAGE_BOTTOM - PAGE_Y - (first ? 16 : 0) - 10) / LINE;
    }

    /** Splits the chapter into pages: the first page is shorter because of the title. */
    private List<List<FormattedCharSequence>> pages() {
        List<FormattedCharSequence> lines = chapterLines();
        List<List<FormattedCharSequence>> pages = new ArrayList<>();
        int index = 0;
        boolean first = true;
        while (index < lines.size()) {
            int count = linesPerPage(first);
            pages.add(lines.subList(index, Math.min(lines.size(), index + count)));
            index += count;
            first = false;
        }
        return pages;
    }

    private void drawText(GuiGraphicsExtractor graphics) {
        List<List<FormattedCharSequence>> pages = pages();
        int spreads = Math.max(1, (pages.size() + 1) / 2);
        spread = Math.min(spread, spreads - 1);
        for (int side = 0; side < 2; side++) {
            int page = spread * 2 + side;
            if (page >= pages.size()) {
                break;
            }
            int x = left + (side == 0 ? LEFT_PAGE_X : RIGHT_PAGE_X);
            int y = top + PAGE_Y;
            if (page == 0) {
                title(graphics, Component.translatable(tab.key()), x, y);
                y += 16;
            }
            for (FormattedCharSequence line : pages.get(page)) {
                graphics.text(font, line, x, y, INK, false);
                y += LINE;
            }
            graphics.centeredText(font, Component.literal(String.valueOf(page + 1)), x + PAGE_W / 2, top + PAGE_BOTTOM + 2, MUTED);
        }
        if (spread > 0) {
            graphics.text(font, Component.literal("◀"), left + LEFT_PAGE_X, top + PAGE_BOTTOM + 2, TITLE, false);
        }
        if (spread < spreads - 1) {
            graphics.text(font, Component.literal("▶"), left + RIGHT_PAGE_X + PAGE_W - 6, top + PAGE_BOTTOM + 2, TITLE, false);
        }
    }

    // ---------------------------------------------------------------- input

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mx = event.x();
        double my = event.y();
        Tab[] tabs = Tab.values();
        for (int i = 0; i < tabs.length; i++) {
            int x = tabX(tabs[i]);
            int y = tabY(i);
            if (mx >= x && mx < x + TAB_W && my >= y && my < y + TAB_H) {
                tab = tabs[i];
                spread = 0;
                return true;
            }
        }
        if (tab == Tab.CROPS || tab == Tab.KITCHEN) {
            int count = tab == Tab.CROPS ? crops.size() : ModFoods.ALL.size();
            for (int i = 0; i < count; i++) {
                int x = left + LEFT_PAGE_X + (i % 7) * 17;
                int y = top + PAGE_Y + 16 + (i / 7) * 17;
                if (mx >= x && mx < x + 16 && my >= y && my < y + 16) {
                    if (tab == Tab.CROPS) {
                        selectedCrop = i;
                    } else {
                        selectedMeal = i;
                    }
                    return true;
                }
            }
        }
        if (my >= top + PAGE_BOTTOM && my < top + PAGE_BOTTOM + 12) {
            if (mx < left + BOOK_W / 2.0) {
                spread = Math.max(0, spread - 1);
            } else {
                spread++;
            }
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (tab == Tab.ROUTE) {
            routeMap.drag(dx, dy, BOOK_W - 24, BOOK_H - 34);
            return true;
        }
        return super.mouseDragged(event, dx, dy);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
        if (tab == Tab.ROUTE) {
            routeMap.drag(scrollX * 10, scrollY * 10, BOOK_W - 24, BOOK_H - 34);
        } else {
            spread = Math.max(0, spread - (int) Math.signum(scrollY));
        }
        return true;
    }

    /** For screenshots and tests: opens a given chapter. */
    public ManualScreen openTab(String name) {
        tab = Tab.valueOf(name);
        return this;
    }

    /** For screenshots and tests: opens the crop sheet of {@code block}. */
    public ManualScreen selectCrop(Block block) {
        tab = Tab.CROPS;
        pendingCrop = block;
        return this;
    }
}
