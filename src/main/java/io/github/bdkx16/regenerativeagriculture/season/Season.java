package io.github.bdkx16.regenerativeagriculture.season;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.StringRepresentable;
import io.github.bdkx16.regenerativeagriculture.RegenerativeAgriculture;

import java.util.Locale;

/**
 * The four seasons, in calendar order. The year starts in spring (southern hemisphere: 1 September).
 */
public enum Season implements StringRepresentable {
    SPRING(0x7BD35A),
    SUMMER(0xF5C542),
    AUTUMN(0xD9822B),
    WINTER(0x8FD3F4);

    public static final StringRepresentable.EnumCodec<Season> CODEC = StringRepresentable.fromEnum(Season::values);

    private final int color;

    Season(int color) {
        this.color = color;
    }

    public int color() {
        return color;
    }

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    @Override
    public String getSerializedName() {
        return id();
    }

    public MutableComponent displayName() {
        return Component.translatable("season." + RegenerativeAgriculture.MOD_ID + "." + id()).withColor(color);
    }

    public static Season byIndex(int index) {
        return values()[Math.floorMod(index, 4)];
    }
}
