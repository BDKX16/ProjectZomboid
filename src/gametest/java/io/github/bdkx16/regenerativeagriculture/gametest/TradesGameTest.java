package io.github.bdkx16.regenerativeagriculture.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.trading.VillagerTrade;
import io.github.bdkx16.regenerativeagriculture.RegenerativeAgriculture;

import java.util.Optional;

public class TradesGameTest {

    /** The mod's trades load and are in the farmer's and the wandering trader's pools. */
    @GameTest
    public void tradesAreInThePools(GameTestHelper helper) {
        Registry<VillagerTrade> trades = helper.getLevel().registryAccess().lookupOrThrow(Registries.VILLAGER_TRADE);
        assertInPool(helper, trades, "farmer/1/tomato_emerald", "farmer/level_1");
        assertInPool(helper, trades, "farmer/2/emerald_lettuce_seeds", "farmer/level_2");
        assertInPool(helper, trades, "farmer/5/beer_emerald", "farmer/level_5");
        assertInPool(helper, trades, "wandering_trader/emerald_yerba_mate_seeds", "wandering_trader/common");
        helper.succeed();
    }

    private static void assertInPool(GameTestHelper helper, Registry<VillagerTrade> trades, String trade, String pool) {
        Optional<Holder.Reference<VillagerTrade>> holder = trades.get(ResourceKey.create(Registries.VILLAGER_TRADE, RegenerativeAgriculture.id(trade)));
        helper.assertTrue(holder.isPresent(), "missing trade " + trade);
        Optional<HolderSet.Named<VillagerTrade>> tag = trades.get(TagKey.create(Registries.VILLAGER_TRADE, Identifier.withDefaultNamespace(pool)));
        helper.assertTrue(tag.isPresent() && tag.get().contains(holder.get()), trade + " should be in " + pool);
    }
}
