package io.github.bdkx16.regenerativeagriculture.soil;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.bdkx16.regenerativeagriculture.crop.PlantFamily;

import java.util.ArrayList;
import java.util.List;

/**
 * State of one farmland block. Immutable: every change returns a new value.
 *
 * @param nitrogen 0-100, consumed by harvests, restored by legumes, compost and organic matter
 * @param organic  0-100 organic matter: boosts growth and slowly releases nitrogen
 * @param history  plant families harvested here, most recent first (up to {@value #HISTORY})
 * @param mulch    harvests the mulch layer still lasts (0 = bare soil)
 */
public record SoilData(int nitrogen, int organic, List<PlantFamily> history, int mulch) {
    public static final int HISTORY = 3;
    public static final SoilData DEFAULT = new SoilData(60, 30, List.of(), 0);

    public static final Codec<SoilData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(0, 100).fieldOf("nitrogen").forGetter(SoilData::nitrogen),
            Codec.intRange(0, 100).fieldOf("organic").forGetter(SoilData::organic),
            PlantFamily.CODEC.listOf().optionalFieldOf("history", List.of()).forGetter(SoilData::history),
            Codec.intRange(0, 100).optionalFieldOf("mulch", 0).forGetter(SoilData::mulch)
    ).apply(instance, SoilData::new));

    public SoilData {
        nitrogen = clamp(nitrogen);
        organic = clamp(organic);
        history = List.copyOf(history.size() > HISTORY ? history.subList(0, HISTORY) : history);
        mulch = Math.max(0, mulch);
    }

    public SoilData withNitrogen(int value) {
        return new SoilData(value, organic, history, mulch);
    }

    public SoilData withOrganic(int value) {
        return new SoilData(nitrogen, value, history, mulch);
    }

    public SoilData withMulch(int value) {
        return new SoilData(nitrogen, organic, history, value);
    }

    public SoilData withHarvested(PlantFamily family) {
        List<PlantFamily> newHistory = new ArrayList<>(history.size() + 1);
        newHistory.add(family);
        newHistory.addAll(history);
        return new SoilData(nitrogen, organic, newHistory, mulch);
    }

    private static int clamp(int value) {
        return Math.clamp(value, 0, 100);
    }
}
