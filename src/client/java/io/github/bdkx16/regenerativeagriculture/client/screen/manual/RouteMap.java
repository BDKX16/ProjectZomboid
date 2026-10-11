package io.github.bdkx16.regenerativeagriculture.client.screen.manual;

import net.minecraft.ChatFormatting;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import io.github.bdkx16.regenerativeagriculture.guide.RouteNodes;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The route as a map of linked nodes, like the advancements screen or a skill tree: completed nodes shine gold,
 * nodes whose parents are done are open, the rest are locked. Drag to move around; hover for details.
 */
final class RouteMap {
    static final int COLUMN = 34;
    static final int ROW = 26;
    static final int NODE = 22;
    private static final int LINE_DONE = 0xFFC8962A;
    private static final int LINE_OPEN = 0xFF8C7A5C;
    private static final int LINE_LOCKED = 0xFFB9AC92;
    private static final int FRAME_DONE = 0xFFE3B341;
    private static final int FRAME_OPEN = 0xFF6B4A2B;
    private static final int FRAME_LOCKED = 0xFFA89A80;
    private static final int FILL_DONE = 0xFFFBEBB5;
    private static final int FILL_OPEN = 0xFFF6EBD2;
    private static final int FILL_LOCKED = 0xFFD8CCB2;
    private static final int GOAL = 0xFF3C7A3A;
    private static final int CHALLENGE = 0xFF7A3A86;

    enum State { DONE, OPEN, LOCKED }

    private double offsetX = 8;
    private double offsetY = 8;

    void drag(double dx, double dy, int width, int height) {
        int maxX = RouteNodes.ALL.stream().mapToInt(RouteNodes.Node::x).max().orElse(0) * COLUMN + NODE;
        int maxY = RouteNodes.ALL.stream().mapToInt(RouteNodes.Node::y).max().orElse(0) * ROW + NODE;
        offsetX = Math.clamp(offsetX + dx, Math.min(8, width - maxX - 8), 8);
        offsetY = Math.clamp(offsetY + dy, Math.min(8, height - maxY - 8), 8);
    }

    static State state(RouteNodes.Node node) {
        if (done(node)) {
            return State.DONE;
        }
        return node.parents().stream().allMatch(parent -> RouteNodes.byId(parent).map(RouteMap::done).orElse(true))
                ? State.OPEN : State.LOCKED;
    }

    static boolean done(RouteNodes.Node node) {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection == null) {
            return false;
        }
        ClientAdvancements advancements = connection.getAdvancements();
        AdvancementHolder holder = advancements.get(node.advancement());
        if (holder == null) {
            return false;
        }
        AdvancementProgress progress = advancements.progress().get(holder);
        return progress != null && progress.isDone();
    }

    static long completed() {
        return RouteNodes.ALL.stream().filter(RouteMap::done).count();
    }

    /** Draws the map inside the given area and returns the node under the mouse, if any. */
    RouteNodes.@Nullable Node draw(GuiGraphicsExtractor graphics, int x, int y, int width, int height, int mouseX, int mouseY) {
        graphics.enableScissor(x, y, x + width, y + height);
        int originX = x + (int) offsetX;
        int originY = y + (int) offsetY;
        for (RouteNodes.Node node : RouteNodes.ALL) {
            for (String parentId : node.parents()) {
                RouteNodes.byId(parentId).ifPresent(parent -> connect(graphics, originX, originY, parent, node));
            }
        }
        RouteNodes.@Nullable Node hovered = null;
        for (RouteNodes.Node node : RouteNodes.ALL) {
            int nx = originX + node.x() * COLUMN;
            int ny = originY + node.y() * ROW;
            drawNode(graphics, node, nx, ny);
            boolean inside = mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
            if (inside && mouseX >= nx && mouseX < nx + NODE && mouseY >= ny && mouseY < ny + NODE) {
                hovered = node;
                graphics.outline(nx - 1, ny - 1, NODE + 2, NODE + 2, 0xFFFFFFFF);
            }
        }
        graphics.disableScissor();
        return hovered;
    }

    /** Elbow line from the right side of the parent to the left side of the child. */
    private static void connect(GuiGraphicsExtractor graphics, int originX, int originY, RouteNodes.Node parent, RouteNodes.Node child) {
        int x1 = originX + parent.x() * COLUMN + NODE;
        int y1 = originY + parent.y() * ROW + NODE / 2;
        int x2 = originX + child.x() * COLUMN;
        int y2 = originY + child.y() * ROW + NODE / 2;
        int mid = x1 + (x2 - x1) / 2;
        int color = switch (state(child)) {
            case DONE -> LINE_DONE;
            case OPEN -> LINE_OPEN;
            case LOCKED -> LINE_LOCKED;
        };
        graphics.fill(x1, y1 - 1, mid + 1, y1 + 1, color);
        graphics.fill(mid - 1, Math.min(y1, y2) - 1, mid + 1, Math.max(y1, y2) + 1, color);
        graphics.fill(mid - 1, y2 - 1, x2, y2 + 1, color);
    }

    private static void drawNode(GuiGraphicsExtractor graphics, RouteNodes.Node node, int x, int y) {
        State state = state(node);
        int frame = switch (state) {
            case DONE -> FRAME_DONE;
            case OPEN -> FRAME_OPEN;
            case LOCKED -> FRAME_LOCKED;
        };
        int fill = switch (state) {
            case DONE -> FILL_DONE;
            case OPEN -> FILL_OPEN;
            case LOCKED -> FILL_LOCKED;
        };
        // Goals and challenges get an extra coloured ring, like the advancement frames
        if (node.type() != AdvancementType.TASK) {
            graphics.fill(x - 2, y - 2, x + NODE + 2, y + NODE + 2, node.type() == AdvancementType.GOAL ? GOAL : CHALLENGE);
        }
        graphics.fill(x, y, x + NODE, y + NODE, frame);
        graphics.fill(x + 2, y + 2, x + NODE - 2, y + NODE - 2, fill);
        graphics.item(new ItemStack(node.icon().get()), x + 3, y + 3);
        if (state == State.LOCKED) {
            graphics.fill(x + 2, y + 2, x + NODE - 2, y + NODE - 2, 0x88B5A88C);
        }
        if (state == State.DONE) {
            graphics.text(Minecraft.getInstance().font, Component.literal("✔"), x + NODE - 7, y + NODE - 9, 0xFF2E7D32, false);
        }
    }

    /** Title, description and status of a node, for its tooltip. */
    static List<Component> tooltip(RouteNodes.Node node, Font font) {
        List<Component> lines = new ArrayList<>();
        ChatFormatting colour = switch (node.type()) {
            case GOAL -> ChatFormatting.GREEN;
            case CHALLENGE -> ChatFormatting.LIGHT_PURPLE;
            default -> ChatFormatting.YELLOW;
        };
        lines.add(Component.translatable(node.titleKey()).withStyle(colour));
        lines.add(Component.translatable(node.descriptionKey()).withStyle(ChatFormatting.GRAY));
        String key = "manual.regenerative_agriculture.route.";
        switch (state(node)) {
            case DONE -> lines.add(Component.translatable(key + "done").withStyle(ChatFormatting.GOLD));
            case OPEN -> lines.add(Component.translatable(key + "open").withStyle(ChatFormatting.AQUA));
            case LOCKED -> {
                Optional<RouteNodes.Node> missing = node.parents().stream().map(RouteNodes::byId).flatMap(Optional::stream)
                        .filter(parent -> !done(parent)).findFirst();
                lines.add(Component.translatable(key + "locked",
                        missing.map(parent -> Component.translatable(parent.titleKey())).orElse(Component.empty()))
                        .withStyle(ChatFormatting.DARK_GRAY));
            }
        }
        return lines;
    }
}
