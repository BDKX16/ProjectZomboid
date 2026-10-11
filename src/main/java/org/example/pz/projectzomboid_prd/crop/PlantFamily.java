package org.example.pz.projectzomboid_prd.crop;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.StringRepresentable;
import org.example.pz.projectzomboid_prd.Projectzomboid_prd;

import java.util.Locale;

/** Botanical families, used for crop rotation: repeating a family on the same soil is penalised. */
public enum PlantFamily implements StringRepresentable {
    /** Poaceae: wheat, maize. */
    GRASSES,
    /** Solanaceae: tomato, potato, pepper. */
    NIGHTSHADES,
    /** Brassicaceae: broccoli, rocket, cabbage. */
    BRASSICAS,
    /** Fabaceae: soy, beans, peas. Fix nitrogen. */
    LEGUMES,
    /** Apiaceae: carrot, parsley. */
    UMBELLIFERS,
    /** Amaranthaceae: beetroot, spinach. */
    AMARANTHS,
    /** Asteraceae: lettuce, sunflower. */
    COMPOSITES,
    /** Cucurbitaceae: squash, pumpkin, melon. */
    CUCURBITS,
    /** Amaryllidaceae: garlic, onion. */
    ALLIUMS,
    /** Lamiaceae: basil, mint. */
    MINTS;

    public static final StringRepresentable.EnumCodec<PlantFamily> CODEC = StringRepresentable.fromEnum(PlantFamily::values);

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public MutableComponent displayName() {
        return Component.translatable("plant_family." + Projectzomboid_prd.MOD_ID + "." + getSerializedName());
    }
}
