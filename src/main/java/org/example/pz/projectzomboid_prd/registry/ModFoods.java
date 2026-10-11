package org.example.pz.projectzomboid_prd.registry;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.consume_effects.RemoveStatusEffectsConsumeEffect;
import net.minecraft.core.HolderSet;
import org.example.pz.projectzomboid_prd.effect.ModEffects;
import org.example.pz.projectzomboid_prd.item.DescribedItem;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/** Meals cooked from the crops; each one gives an effect tied to its vegetables. */
public final class ModFoods {
    private static final int SECONDS = 20;
    private static final int MINUTES = 60 * SECONDS;
    private static final List<Meal> MUTABLE_ALL = new ArrayList<>();
    public static final List<Meal> ALL = Collections.unmodifiableList(MUTABLE_ALL);

    /** Bread, beef, lettuce and tomato: fills the whole hunger bar. */
    public static final Meal HAMBURGER = register("hamburger", 20, 0.8f, null, Consumables.defaultFood(), null,
            List.of(ModCrops.LETTUCE.produce(), ModCrops.TOMATO.produce()));
    public static final Meal GREEN_SALAD = register("green_salad", 6, 0.6f, Items.BOWL, Consumables.defaultFood(),
            new MobEffectInstance(ModEffects.VITALITY, 2 * MINUTES),
            List.of(ModCrops.LETTUCE.produce(), ModCrops.ARUGULA.produce(), ModCrops.TOMATO.produce()));
    public static final Meal STEAMED_BROCCOLI = register("steamed_broccoli", 6, 0.6f, Items.BOWL,
            Consumables.defaultFood().onConsume(new RemoveStatusEffectsConsumeEffect(HolderSet.direct(ModEffects.IMMUNE_TO))),
            new MobEffectInstance(ModEffects.IMMUNITY, 3 * MINUTES),
            List.of(ModCrops.BROCCOLI.produce(), ModCrops.BASIL.produce()));
    public static final Meal SOY_STEW = register("soy_stew", 8, 0.7f, Items.BOWL, Consumables.defaultFood(),
            new MobEffectInstance(MobEffects.STRENGTH, 2 * MINUTES),
            List.of(ModCrops.SOYBEAN.produce(), Items.CARROT, Items.POTATO));
    public static final Meal SQUASH_SOUP = register("squash_soup", 8, 0.6f, Items.BOWL, Consumables.defaultFood(),
            new MobEffectInstance(ModEffects.WARMTH, 4 * MINUTES),
            List.of(ModCrops.SQUASH.produce(), Items.POTATO));
    public static final Meal ROASTED_SUNFLOWER_SEEDS = register("roasted_sunflower_seeds", 3, 0.4f, null,
            Consumables.defaultFood().consumeSeconds(0.8f),
            new MobEffectInstance(MobEffects.ABSORPTION, 30 * SECONDS),
            List.of(ModCrops.SUNFLOWER.produce()));
    public static final Meal PESTO_PASTA = register("pesto_pasta", 8, 0.7f, Items.BOWL, Consumables.defaultFood(),
            new MobEffectInstance(MobEffects.HASTE, 3 * MINUTES),
            List.of(ModCrops.BASIL.produce(), ModCrops.SUNFLOWER.produce()));
    public static final Meal TOMATO_JUICE = register("tomato_juice", 4, 0.3f, Items.GLASS_BOTTLE, Consumables.defaultDrink(),
            new MobEffectInstance(MobEffects.REGENERATION, 10 * SECONDS),
            List.of(ModCrops.TOMATO.produce()));

    /** Vanilla food that also counts for a varied diet, mapped to the vegetable it is made of. */
    private static final Map<Item, Item> VANILLA_VEGETABLES = Map.of(
            Items.CARROT, Items.CARROT,
            Items.POTATO, Items.POTATO,
            Items.BAKED_POTATO, Items.POTATO,
            Items.BEETROOT, Items.BEETROOT,
            Items.BEETROOT_SOUP, Items.BEETROOT,
            Items.PUMPKIN_PIE, Items.PUMPKIN);

    private ModFoods() {
    }

    public static void init() {
        // Loads the class so the static items are registered.
    }

    /** The different vegetables eaten with this item, for the varied-diet bonus. */
    public static List<Item> vegetablesIn(Item food) {
        for (Meal meal : ALL) {
            if (meal.item() == food) {
                return meal.vegetables();
            }
        }
        for (ModCrops.Crop crop : ModCrops.ALL) {
            if (crop.produce() == food) {
                return List.of(food);
            }
        }
        Item vanilla = VANILLA_VEGETABLES.get(food);
        return vanilla == null ? List.of() : List.of(vanilla);
    }

    /**
     * @param container what is left after eating (bowl, bottle) or null
     * @param effect    applied on eating, or null
     */
    private static Meal register(String name, int nutrition, float saturation, @Nullable Item container,
                                 Consumable.Builder consumable, @Nullable MobEffectInstance effect, List<Item> vegetables) {
        if (effect != null) {
            consumable.onConsume(new ApplyStatusEffectsConsumeEffect(effect));
        }
        FoodProperties food = new FoodProperties.Builder().nutrition(nutrition).saturationModifier(saturation).build();
        Item.Properties properties = new Item.Properties().food(food, consumable.build()).stacksTo(16);
        if (container != null) {
            properties.usingConvertsTo(container);
        }
        Item item = ModCrops.registerItem(name, DescribedItem::new, properties);
        Meal meal = new Meal(name, item, effect == null ? null : effect.getEffect(), List.copyOf(vegetables));
        MUTABLE_ALL.add(meal);
        return meal;
    }

    public record Meal(String name, Item item, @Nullable Holder<MobEffect> effect, List<Item> vegetables) {
    }
}
