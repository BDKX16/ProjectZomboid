package org.example.pz.projectzomboid_prd.registry;

import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.ItemStack;
import org.example.pz.projectzomboid_prd.item.DescribedItem;
import org.example.pz.projectzomboid_prd.item.HydrometerItem;
import org.example.pz.projectzomboid_prd.item.ManualItem;
import org.example.pz.projectzomboid_prd.item.MateItem;
import org.example.pz.projectzomboid_prd.item.ThermosItem;
import org.example.pz.projectzomboid_prd.item.PruningShearsItem;
import org.example.pz.projectzomboid_prd.item.SoilAmendmentItem;
import org.example.pz.projectzomboid_prd.item.SoilTestKitItem;
import org.example.pz.projectzomboid_prd.item.ThermohygrometerItem;
import org.example.pz.projectzomboid_prd.soil.SoilRules;

import java.util.List;

/** Tools and other items that are not crops. */
public final class ModItems {
    /** The farming manual: route of milestones, seasons, crop sheets and guides. */
    public static final Item MANUAL = ModCrops.registerItem("manual", ManualItem::new, new Item.Properties().stacksTo(1));
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

    // Other ingredients
    public static final Item ROASTED_COFFEE = ingredient("roasted_coffee");
    public static final Item GROUND_CHILI = ingredient("ground_chili");
    public static final Item CHIMICHURRI = ModCrops.registerItem("chimichurri", DescribedItem::new,
            new Item.Properties().stacksTo(16).craftRemainder(Items.GLASS_BOTTLE));

    // Microbrewery
    /** Wheat dried on the rack: what the mash tun needs. */
    public static final Item MALT = ingredient("malt");
    /** Left over in the mash tun; good compost. */
    public static final Item SPENT_GRAIN = ModCrops.registerItem("spent_grain", DescribedItem::new,
            new Item.Properties().component(DataComponents.COMPOSTABLE, ModCrops.compostable("high")));
    public static final Item YEAST = ingredient("yeast");
    public static final Item HYDROMETER = ModCrops.registerItem("hydrometer", HydrometerItem::new,
            new Item.Properties().stacksTo(1));

    // Mate
    /** A squash dried on the rack: becomes the mate's gourd. */
    public static final Item CURED_GOURD = ingredient("cured_gourd");
    public static final Item MATE = ModCrops.registerItem("mate", MateItem::new, new Item.Properties().stacksTo(1));
    public static final Item THERMOS = ModCrops.registerItem("thermos",
            properties -> new ThermosItem(properties, ThermosItem.Kind.EMPTY), new Item.Properties().stacksTo(1));
    public static final Item THERMOS_COLD = ModCrops.registerItem("thermos_cold",
            properties -> new ThermosItem(properties, ThermosItem.Kind.COLD), new Item.Properties().stacksTo(1));
    public static final Item THERMOS_HOT = ModCrops.registerItem("thermos_hot",
            properties -> new ThermosItem(properties, ThermosItem.Kind.HOT), new Item.Properties().stacksTo(1)
                    .component(ModComponents.THERMOS_SERVINGS, ThermosItem.SERVINGS));

    /** Everything that comes out of the drying rack, the curing jar or is made from them (for the creative tab and models). */
    public static final List<Item> PRODUCTS = List.of(CURED_GARLIC, CURED_ONION, DRIED_TOBACCO, DRIED_CANNABIS, DRIED_COFFEE,
            DRIED_CHILI, DRIED_HOPS, DRIED_YERBA, DRIED_CORN, SPOILED_HARVEST, CURED_TOBACCO, CURED_CANNABIS, YERBA,
            ROASTED_COFFEE, GROUND_CHILI, CHIMICHURRI, MALT, SPENT_GRAIN, YEAST, CURED_GOURD);
    /** Tools and containers with their own 3D models. */
    public static final List<Item> MODELLED = List.of(HYDROMETER, MATE, THERMOS, THERMOS_COLD, THERMOS_HOT);

    private ModItems() {
    }

    public static void init() {
        // Loads the class so the static items are registered.
    }

    /** Into the player's inventory, or dropped at their feet if it is full. */
    public static void give(Player player, ItemStack stack) {
        if (!player.getInventory().add(stack) && !stack.isEmpty()) {
            Block.popResource(player.level(), player.blockPosition(), stack);
        }
    }

    private static Item ingredient(String name) {
        return ModCrops.registerItem(name, DescribedItem::new, new Item.Properties());
    }
}
