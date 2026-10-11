package org.example.pz.projectzomboid_prd.registry;

import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.example.pz.projectzomboid_prd.item.DescribedItem;
import org.example.pz.projectzomboid_prd.item.PruningShearsItem;
import org.example.pz.projectzomboid_prd.item.SoilAmendmentItem;
import org.example.pz.projectzomboid_prd.item.SoilTestKitItem;
import org.example.pz.projectzomboid_prd.item.ThermohygrometerItem;
import org.example.pz.projectzomboid_prd.soil.SoilRules;

import java.util.List;

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
            properties -> new SoilAmendmentItem(properties, SoilRules::mulch, SoundEvents.ROOTED_DIRT_PLACE,
                    soil -> soil.mulch() == 0, "item.projectzomboid_prd.straw_mulch.already"),
            new Item.Properties());
    public static final Item SOIL_TEST_KIT = ModCrops.registerItem("soil_test_kit", SoilTestKitItem::new,
            new Item.Properties().stacksTo(1));


    // Drying rack products
    public static final Item CURED_GARLIC = ingredient("cured_garlic");
    public static final Item CURED_ONION = ingredient("cured_onion");
    public static final Item DRIED_TOBACCO = ingredient("dried_tobacco");
    public static final Item DRIED_CANNABIS = ingredient("dried_cannabis");
    public static final Item DRIED_COFFEE = ingredient("dried_coffee");
    public static final Item DRIED_CHILI = ingredient("dried_chili");
    public static final Item DRIED_HOPS = ingredient("dried_hops");
    public static final Item DRIED_YERBA = ingredient("dried_yerba");
    public static final Item DRIED_CORN = ingredient("dried_corn");
    /** What a harvest turns into when it stays damp on the rack for too long; only good for compost. */
    public static final Item SPOILED_HARVEST = ModCrops.registerItem("spoiled_harvest", DescribedItem::new,
            new Item.Properties().component(DataComponents.COMPOSTABLE, ModCrops.compostable("high")));

    // Curing jar products (they carry a quality)
    public static final Item CURED_TOBACCO = ingredient("cured_tobacco");
    public static final Item CURED_CANNABIS = ingredient("cured_cannabis");
    /** Aged ("estacionada") yerba mate. */
    public static final Item YERBA = ingredient("yerba");
    /** Unfermented beer: goes into a curing jar to become beer. */
    public static final Item WORT = ModCrops.registerItem("wort", DescribedItem::new,
            new Item.Properties().stacksTo(16).craftRemainder(Items.GLASS_BOTTLE));

    // Other ingredients
    public static final Item ROASTED_COFFEE = ingredient("roasted_coffee");
    public static final Item GROUND_CHILI = ingredient("ground_chili");
    public static final Item CHIMICHURRI = ModCrops.registerItem("chimichurri", DescribedItem::new,
            new Item.Properties().stacksTo(16).craftRemainder(Items.GLASS_BOTTLE));
    /** A dried squash with a metal straw: the cup for mate, given back after drinking. */
    public static final Item MATE_GOURD = ModCrops.registerItem("mate_gourd", DescribedItem::new,
            new Item.Properties().stacksTo(1));

    /** Everything that comes out of the drying rack, the curing jar or is made from them (for the creative tab and models). */
    public static final List<Item> PRODUCTS = List.of(CURED_GARLIC, CURED_ONION, DRIED_TOBACCO, DRIED_CANNABIS, DRIED_COFFEE,
            DRIED_CHILI, DRIED_HOPS, DRIED_YERBA, DRIED_CORN, SPOILED_HARVEST, CURED_TOBACCO, CURED_CANNABIS, YERBA, WORT,
            ROASTED_COFFEE, GROUND_CHILI, CHIMICHURRI, MATE_GOURD);

    private ModItems() {
    }

    public static void init() {
        // Loads the class so the static items are registered.
    }

    private static Item ingredient(String name) {
        return ModCrops.registerItem(name, DescribedItem::new, new Item.Properties());
    }
}
