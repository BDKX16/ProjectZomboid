package org.example.pz.projectzomboid_prd.registry;

import net.minecraft.world.item.Item;
import org.example.pz.projectzomboid_prd.item.PruningShearsItem;
import org.example.pz.projectzomboid_prd.item.ThermohygrometerItem;

/** Tools and other items that are not crops. */
public final class ModItems {
    public static final Item PRUNING_SHEARS = ModCrops.registerItem("pruning_shears", PruningShearsItem::new,
            new Item.Properties().durability(238));
    public static final Item THERMOHYGROMETER = ModCrops.registerItem("thermohygrometer", ThermohygrometerItem::new,
            new Item.Properties().stacksTo(1));

    private ModItems() {
    }

    public static void init() {
        // Loads the class so the static items are registered.
    }
}
