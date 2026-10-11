package org.example.pz.projectzomboid_prd.registry;

import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.consume_effects.RemoveStatusEffectsConsumeEffect;
import org.example.pz.projectzomboid_prd.effect.ModEffects;
import org.example.pz.projectzomboid_prd.item.DescribedItem;
import org.example.pz.projectzomboid_prd.processing.QualityEffectsConsumeEffect;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Meals cooked from the crops, each with an effect tied to its vegetables, and the products made from cured
 * harvest (their effects last longer with a better quality).
 */
public final class ModFoods {
    private static final int SECONDS = 20;
    private static final int MINUTES = 60 * SECONDS;
    private static final List<Meal> MUTABLE_ALL = new ArrayList<>();
    public static final List<Meal> ALL = Collections.unmodifiableList(MUTABLE_ALL);

    /** Bread, beef, lettuce and tomato: fills the whole hunger bar. */
    public static final Meal HAMBURGER = meal("hamburger", 20, 0.8f, null, Consumables.defaultFood(), null,
            List.of(ModCrops.LETTUCE.produce(), ModCrops.TOMATO.produce()));
    public static final Meal GREEN_SALAD = meal("green_salad", 6, 0.6f, Items.BOWL, Consumables.defaultFood(),
            new MobEffectInstance(ModEffects.VITALITY, 2 * MINUTES),
            List.of(ModCrops.LETTUCE.produce(), ModCrops.ARUGULA.produce(), ModCrops.TOMATO.produce()));
    public static final Meal STEAMED_BROCCOLI = meal("steamed_broccoli", 6, 0.6f, Items.BOWL,
            Consumables.defaultFood().onConsume(new RemoveStatusEffectsConsumeEffect(HolderSet.direct(ModEffects.IMMUNE_TO))),
            new MobEffectInstance(ModEffects.IMMUNITY, 3 * MINUTES),
            List.of(ModCrops.BROCCOLI.produce(), ModCrops.BASIL.produce()));
    public static final Meal SOY_STEW = meal("soy_stew", 8, 0.7f, Items.BOWL, Consumables.defaultFood(),
            new MobEffectInstance(MobEffects.STRENGTH, 2 * MINUTES),
            List.of(ModCrops.SOYBEAN.produce(), Items.CARROT, Items.POTATO));
    public static final Meal SQUASH_SOUP = meal("squash_soup", 8, 0.6f, Items.BOWL, Consumables.defaultFood(),
            new MobEffectInstance(ModEffects.WARMTH, 4 * MINUTES),
            List.of(ModCrops.SQUASH.produce(), Items.POTATO));
    public static final Meal ROASTED_SUNFLOWER_SEEDS = meal("roasted_sunflower_seeds", 3, 0.4f, null,
            Consumables.defaultFood().consumeSeconds(0.8f),
            new MobEffectInstance(MobEffects.ABSORPTION, 30 * SECONDS),
            List.of(ModCrops.SUNFLOWER.produce()));
    public static final Meal PESTO_PASTA = meal("pesto_pasta", 8, 0.7f, Items.BOWL, Consumables.defaultFood(),
            new MobEffectInstance(MobEffects.HASTE, 3 * MINUTES),
            List.of(ModCrops.BASIL.produce(), ModCrops.SUNFLOWER.produce()));
    public static final Meal TOMATO_JUICE = meal("tomato_juice", 4, 0.3f, Items.GLASS_BOTTLE, Consumables.defaultDrink(),
            new MobEffectInstance(MobEffects.REGENERATION, 10 * SECONDS),
            List.of(ModCrops.TOMATO.produce()));

    /** Corn, beans, squash, chili and onion: the classic stew, warm and filling. */
    public static final Meal LOCRO = register("locro", food(10, 0.8f), Items.BOWL, Consumables.defaultFood(),
            List.of(new MobEffectInstance(ModEffects.WARMTH, 6 * MINUTES), new MobEffectInstance(MobEffects.RESISTANCE, 2 * MINUTES)),
            false, List.of(ModCrops.CORN.produce(), ModCrops.BEAN.produce(), ModCrops.SQUASH.produce(),
                    ModCrops.CHILI.produce(), ModCrops.ONION.produce()));
    /** Grilled beef with chimichurri (garlic, chili and basil). */
    public static final Meal ASADO = register("asado", food(14, 0.9f), null, Consumables.defaultFood(),
            List.of(new MobEffectInstance(MobEffects.STRENGTH, 3 * MINUTES), new MobEffectInstance(MobEffects.REGENERATION, 10 * SECONDS)),
            false, List.of(ModCrops.GARLIC.produce(), ModCrops.CHILI.produce(), ModCrops.BASIL.produce()));
    public static final Meal ROASTED_CORN = meal("roasted_corn", 6, 0.6f, null, Consumables.defaultFood(), null,
            List.of(ModCrops.CORN.produce()));
    public static final Meal COFFEE = register("coffee", food(1, 0.2f), Items.GLASS_BOTTLE, Consumables.defaultDrink(),
            List.of(new MobEffectInstance(MobEffects.HASTE, 3 * MINUTES), new MobEffectInstance(MobEffects.SPEED, 30 * SECONDS)),
            false, List.of());

    // Made from cured harvest: the quality of the jar carries over and lengthens the effects.
    /** Bottled from the fermenter of the microbrewery. */
    public static final Meal BEER = register("beer", alwaysEdible(2, 0.2f), Items.GLASS_BOTTLE, Consumables.defaultDrink(),
            List.of(new MobEffectInstance(MobEffects.ABSORPTION, 2 * MINUTES), new MobEffectInstance(MobEffects.NAUSEA, 5 * SECONDS)),
            true, List.of());
    public static final Meal JOINT = register("joint", null, null, smoke(),
            List.of(new MobEffectInstance(ModEffects.RELAXED, 90 * SECONDS)), true, List.of());
    public static final Meal BROWNIE = register("brownie", alwaysEdible(4, 0.4f), null, Consumables.defaultFood(),
            List.of(new MobEffectInstance(ModEffects.RELAXED, 3 * MINUTES)), true, List.of());
    public static final Meal CIGAR = register("cigar", null, null, smoke(),
            List.of(new MobEffectInstance(MobEffects.HASTE, MINUTES)), true, List.of());

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

    private static FoodProperties food(int nutrition, float saturation) {
        return new FoodProperties.Builder().nutrition(nutrition).saturationModifier(saturation).build();
    }

    /** Drinks and treats that can be had with a full hunger bar. */
    private static FoodProperties alwaysEdible(int nutrition, float saturation) {
        return new FoodProperties.Builder().nutrition(nutrition).saturationModifier(saturation).alwaysEdible().build();
    }

    /** Held to the mouth like a horn, with a slow breath in. */
    private static Consumable.Builder smoke() {
        return Consumable.builder()
                .consumeSeconds(2.0f)
                .animation(ItemUseAnimation.TOOT_HORN)
                .sound(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.BREEZE_INHALE))
                .hasConsumeParticles(false);
    }

    /** A meal with at most one effect. */
    private static Meal meal(String name, int nutrition, float saturation, @Nullable Item container,
                             Consumable.Builder consumable, @Nullable MobEffectInstance effect, List<Item> vegetables) {
        return register(name, food(nutrition, saturation), container, consumable,
                effect == null ? List.of() : List.of(effect), false, vegetables);
    }

    /**
     * @param food      null for things that are consumed but do not feed (smoked)
     * @param container what is left after consuming (bowl, bottle, gourd) or null
     * @param quality   effect durations grow with the item's quality
     */
    private static Meal register(String name, @Nullable FoodProperties food, @Nullable Item container, Consumable.Builder consumable,
                                 List<MobEffectInstance> effects, boolean quality, List<Item> vegetables) {
        if (!effects.isEmpty()) {
            consumable.onConsume(quality ? new QualityEffectsConsumeEffect(effects) : new ApplyStatusEffectsConsumeEffect(effects));
        }
        Item.Properties properties = new Item.Properties().stacksTo(16);
        if (food != null) {
            properties.food(food, consumable.build());
        } else {
            properties.component(DataComponents.CONSUMABLE, consumable.build());
        }
        if (container != null) {
            properties.usingConvertsTo(container);
        }
        Item item = ModCrops.registerItem(name, DescribedItem::new, properties);
        Meal meal = new Meal(name, item, List.copyOf(effects), List.copyOf(vegetables));
        MUTABLE_ALL.add(meal);
        return meal;
    }

    public record Meal(String name, Item item, List<MobEffectInstance> effects, List<Item> vegetables) {
    }
}
