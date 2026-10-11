package io.github.bdkx16.regenerativeagriculture.brewing;

import net.minecraft.util.StringRepresentable;

import java.util.Locale;

/** What a brewery machine shows: empty, loaded and waiting, working (mashing, boiling, fermenting), done. */
public enum BrewStage implements StringRepresentable {
    EMPTY, FILLED, WORKING, DONE;

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
