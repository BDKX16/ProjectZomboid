package org.example.pz.projectzomboid_prd.season;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.example.pz.projectzomboid_prd.Projectzomboid_prd;

import java.util.Locale;

/**
 * The four seasons, in calendar order. The year starts in spring (southern hemisphere: 1 September).
 */
public enum Season {
    SPRING(0x7BD35A),
    SUMMER(0xF5C542),
    AUTUMN(0xD9822B),
    WINTER(0x8FD3F4);

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

    public MutableComponent displayName() {
        return Component.translatable("season." + Projectzomboid_prd.MOD_ID + "." + id()).withColor(color);
    }

    public static Season byIndex(int index) {
        return values()[Math.floorMod(index, 4)];
    }
}
