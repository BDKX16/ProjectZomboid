package io.github.bdkx16.regenerativeagriculture.processing;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import io.github.bdkx16.regenerativeagriculture.registry.ModCrops;
import io.github.bdkx16.regenerativeagriculture.registry.ModItems;

import java.util.List;
import java.util.Optional;

/** What dries on a drying rack and what cures in a curing jar. */
public final class ProcessingRecipes {
    /** @param days drying time at normal speed ({@link DryingRules#rate} = 1) */
    public record Drying(Item input, Item output, float days) {
    }

    /** @param qualityDays days in the jar needed for 1, 2 and 3 stars */
    public record Curing(Item input, Item output, int[] qualityDays) {
        public int minDays() {
            return qualityDays[0];
        }
    }

    public static final List<Drying> DRYING = List.of(
            new Drying(ModCrops.GARLIC.produce(), ModItems.CURED_GARLIC, 2),
            new Drying(ModCrops.ONION.produce(), ModItems.CURED_ONION, 2),
            new Drying(ModCrops.TOBACCO.produce(), ModItems.DRIED_TOBACCO, 2),
            new Drying(ModCrops.CANNABIS.produce(), ModItems.DRIED_CANNABIS, 1.5f),
            new Drying(ModCrops.COFFEE.produce(), ModItems.DRIED_COFFEE, 3),
            new Drying(ModCrops.CHILI.produce(), ModItems.DRIED_CHILI, 2),
            new Drying(ModCrops.HOP.produce(), ModItems.DRIED_HOPS, 1),
            new Drying(ModCrops.YERBA_MATE.produce(), ModItems.DRIED_YERBA, 1),
            new Drying(ModCrops.CORN.produce(), ModItems.DRIED_CORN, 2),
            new Drying(Items.WHEAT, ModItems.MALT, 1),
            new Drying(ModCrops.SQUASH.produce(), ModItems.CURED_GOURD, 4));

    public static final List<Curing> CURING = List.of(
            new Curing(ModItems.DRIED_TOBACCO, ModItems.CURED_TOBACCO, new int[]{3, 6, 10}),
            new Curing(ModItems.DRIED_CANNABIS, ModItems.CURED_CANNABIS, new int[]{2, 4, 7}),
            new Curing(ModItems.DRIED_YERBA, ModItems.YERBA, new int[]{2, 5, 9}));

    private ProcessingRecipes() {
    }

    public static Optional<Drying> drying(Item input) {
        return DRYING.stream().filter(recipe -> recipe.input() == input).findFirst();
    }

    public static Optional<Curing> curing(Item input) {
        return CURING.stream().filter(recipe -> recipe.input() == input).findFirst();
    }
}
