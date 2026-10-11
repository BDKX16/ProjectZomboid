package org.example.pz.projectzomboid_prd.registry;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import org.example.pz.projectzomboid_prd.item.PruningShearsItem;
import org.example.pz.projectzomboid_prd.item.SoilAmendmentItem;
import org.example.pz.projectzomboid_prd.item.SoilTestKitItem;
import org.example.pz.projectzomboid_prd.item.ThermohygrometerItem;
import org.example.pz.projectzomboid_prd.soil.SoilRules;

/** Tools and other items that are not crops. */
public final class ModItems {
    public static final Item PRUNING_SHEARS = ModCrops.registerItem("pruning_shears", PruningShearsItem::new,
            new Item.Properties().durability(238));
    public static final Item THERMOHYGROMETER = ModCrops.registerItem("thermohygrometer", ThermohygrometerItem::new,
            new Item.Properties().stacksTo(1));
    /** From the compost bin; spread on farmland for organic matter and nitrogen. */
    public static final Item COMPOST = ModCrops.registerItem("compost",
            properties -> new SoilAmendmentItem(properties, SoilRules::compost, SoundEvents.BONE_MEAL_USE),
            new Item.Properties());
    /** Straw layer on farmland: protects from light frost and adds organic matter for a few harvests. */
    public static final Item STRAW_MULCH = ModCrops.registerItem("straw_mulch",
            properties -> new SoilAmendmentItem(properties, SoilRules::mulch, SoundEvents.ROOTED_DIRT_PLACE),
            new Item.Properties());
    public static final Item SOIL_TEST_KIT = ModCrops.registerItem("soil_test_kit", SoilTestKitItem::new,
            new Item.Properties().stacksTo(1));

    private ModItems() {
    }

    public static void init() {
        // Loads the class so the static items are registered.
    }
}
