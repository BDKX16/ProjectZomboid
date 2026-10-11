package org.example.pz.projectzomboid_prd.food;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Vegetables a player ate recently (item id and game time). Eating {@link #VARIETY_FOR_NOURISHED} different ones
 * within one Minecraft day earns the "Nourished" effect.
 */
public record Diet(List<Entry> eaten) {
    public static final Diet EMPTY = new Diet(List.of());
    /** One Minecraft day. */
    public static final long WINDOW = 24000;
    public static final int VARIETY_FOR_NOURISHED = 4;

    public record Entry(String vegetable, long time) {
        static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("vegetable").forGetter(Entry::vegetable),
                Codec.LONG.fieldOf("time").forGetter(Entry::time)
        ).apply(instance, Entry::new));
    }

    public static final Codec<Diet> CODEC = Entry.CODEC.listOf().xmap(Diet::new, Diet::eaten);

    public Diet {
        eaten = List.copyOf(eaten);
    }

    /** Records the vegetables of one meal; forgets what was eaten more than a day ago. */
    public Diet eat(Collection<String> vegetables, long now) {
        List<Entry> kept = new ArrayList<>();
        for (Entry entry : eaten) {
            if (now - entry.time() < WINDOW && !vegetables.contains(entry.vegetable())) {
                kept.add(entry);
            }
        }
        for (String vegetable : new LinkedHashSet<>(vegetables)) {
            kept.add(new Entry(vegetable, now));
        }
        return new Diet(kept);
    }

    /** Different vegetables eaten in the last day. */
    public Set<String> recent(long now) {
        Set<String> recent = new LinkedHashSet<>();
        for (Entry entry : eaten) {
            if (now - entry.time() < WINDOW) {
                recent.add(entry.vegetable());
            }
        }
        return recent;
    }

    public boolean nourished(long now) {
        return recent(now).size() >= VARIETY_FOR_NOURISHED;
    }
}
