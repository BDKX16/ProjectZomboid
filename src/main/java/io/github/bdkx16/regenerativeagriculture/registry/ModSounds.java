package io.github.bdkx16.regenerativeagriculture.registry;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import io.github.bdkx16.regenerativeagriculture.RegenerativeAgriculture;

/**
 * The mod's own sound events. What each one actually plays is set in {@code assets/regenerative_agriculture/sounds.json}:
 * until custom recordings exist they point at similar vanilla sounds.
 */
public final class ModSounds {
    /** Each sip through the straw while drinking mate. */
    public static final Holder<SoundEvent> MATE_SIP = register("item.mate.sip");
    /** The gurgle at the end of a mate. */
    public static final Holder<SoundEvent> MATE_SLURP = register("item.mate.slurp");
    /** Hot water poured from the thermos into the mate. */
    public static final Holder<SoundEvent> MATE_POUR = register("item.mate.pour");
    /** Yerba poured into the gourd. */
    public static final Holder<SoundEvent> MATE_LOAD = register("item.mate.load");
    /** Filling the thermos with water. */
    public static final Holder<SoundEvent> THERMOS_FILL = register("item.thermos.fill");
    /** Breathing in while smoking (plays during use). */
    public static final Holder<SoundEvent> SMOKE_INHALE = register("item.smoke.inhale");
    /** Breathing the smoke out (plays when done). */
    public static final Holder<SoundEvent> SMOKE_EXHALE = register("item.smoke.exhale");
    /** The brew kettle boiling. */
    public static final Holder<SoundEvent> KETTLE_BOIL = register("block.brew_kettle.boil");
    /** The fermenter's airlock bubbling. */
    public static final Holder<SoundEvent> FERMENTER_BUBBLE = register("block.fermenter.bubble");

    private ModSounds() {
    }

    public static void init() {
        // Loads the class so the static sounds are registered.
    }

    private static Holder<SoundEvent> register(String name) {
        Identifier id = RegenerativeAgriculture.id(name);
        return Registry.registerForHolder(BuiltInRegistries.SOUND_EVENT, id, SoundEvent.createVariableRangeEvent(id));
    }
}
