package io.github.bdkx16.regenerativeagriculture.client.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.predicates.ItemPredicate;
import net.minecraft.advancements.predicates.MobEffectsPredicate;
import net.minecraft.advancements.triggers.ConsumeItemTrigger;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.triggers.EffectsChangedTrigger;
import net.minecraft.advancements.triggers.InventoryChangeTrigger;
import net.minecraft.advancements.triggers.PlayerTrigger;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import io.github.bdkx16.regenerativeagriculture.effect.ModEffects;
import io.github.bdkx16.regenerativeagriculture.guide.Guide;
import io.github.bdkx16.regenerativeagriculture.guide.RouteNodes;
import io.github.bdkx16.regenerativeagriculture.processing.ProcessingRecipes;
import io.github.bdkx16.regenerativeagriculture.registry.ModBlocks;
import io.github.bdkx16.regenerativeagriculture.registry.ModCrops;
import io.github.bdkx16.regenerativeagriculture.registry.ModFoods;
import io.github.bdkx16.regenerativeagriculture.registry.ModItems;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/** One advancement per node of the route ({@link RouteNodes}); together they form the mod's advancement tab. */
public class ModAdvancementProvider extends FabricAdvancementProvider {
    private static final Identifier BACKGROUND = Identifier.withDefaultNamespace("gui/advancements/backgrounds/husbandry");

    public ModAdvancementProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    public void generateAdvancement(HolderLookup.Provider registries, Consumer<AdvancementHolder> consumer) {
        HolderGetter<Item> items = registries.lookupOrThrow(Registries.ITEM);
        for (RouteNodes.Node node : RouteNodes.ALL) {
            Component title = Component.translatable(node.titleKey());
            Component description = Component.translatable(node.descriptionKey());
            Advancement.Builder builder = Advancement.Builder.advancement();
            if (node.parents().isEmpty()) {
                builder.rootDisplay(node.icon().get(), title, description, BACKGROUND, node.type(), false, false, false);
            } else {
                builder.parent(RouteNodes.byId(node.parents().getFirst()).orElseThrow().advancement())
                        .display(node.icon().get(), title, description, node.type(), true, false, false);
            }
            builder.addCriterion(node.id(), criterion(node.id(), items));
            consumer.accept(builder.build(node.advancement()));
        }
    }

    private static Criterion<?> criterion(String id, HolderGetter<Item> items) {
        return switch (id) {
            case "wild_seeds" -> has(items, ModCrops.ALL.stream().map(ModCrops.Crop::seeds).toArray(ItemLike[]::new));
            case "first_harvest" -> has(items, ModCrops.ALL.stream().map(ModCrops.Crop::produce).toArray(ItemLike[]::new));
            case "pruning_shears" -> has(items, ModItems.PRUNING_SHEARS);
            case "soil_test" -> has(items, ModItems.SOIL_TEST_KIT);
            case "compost" -> has(items, ModItems.COMPOST);
            case "mulch" -> has(items, ModItems.STRAW_MULCH);
            case "salad" -> ConsumeItemTrigger.TriggerInstance.usedItem(items, ModFoods.GREEN_SALAD.item());
            case "hamburger" -> ConsumeItemTrigger.TriggerInstance.usedItem(items, ModFoods.HAMBURGER.item());
            case "nourished" -> EffectsChangedTrigger.TriggerInstance.hasEffects(MobEffectsPredicate.Builder.effects().and(ModEffects.NOURISHED));
            case "drying_rack" -> has(items, ModBlocks.DRYING_RACK);
            case "dried" -> has(items, ProcessingRecipes.DRYING.stream().map(ProcessingRecipes.Drying::output).toArray(ItemLike[]::new));
            case "cured" -> has(items, ProcessingRecipes.CURING.stream().map(ProcessingRecipes.Curing::output).toArray(ItemLike[]::new));
            case "mate" -> has(items, ModItems.MATE);
            case "thermos" -> has(items, ModItems.THERMOS_HOT);
            case "malt" -> has(items, ModItems.MALT);
            case "brewery" -> has(items, ModBlocks.FERMENTER);
            case "beer" -> has(items, ModFoods.BEER.item());
            case "weather_station" -> has(items, ModBlocks.WEATHER_STATION);
            case "thermohygrometer" -> has(items, ModItems.THERMOHYGROMETER);
            case "greenhouse" -> Guide.GREENHOUSE_HARVEST.createCriterion(new PlayerTrigger.TriggerInstance(Optional.empty()));
            case "extractor" -> has(items, ModBlocks.EXTRACTOR);
            case "grow_light" -> has(items, ModBlocks.GROW_LIGHT);
            case "grow_room" -> Guide.GROW_ROOM_HARVEST.createCriterion(new PlayerTrigger.TriggerInstance(Optional.empty()));
            default -> throw new IllegalArgumentException("no criterion for route node " + id);
        };
    }

    /** Having any one of the items. */
    private static Criterion<?> has(HolderGetter<Item> items, ItemLike... any) {
        return InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(items, any));
    }
}
