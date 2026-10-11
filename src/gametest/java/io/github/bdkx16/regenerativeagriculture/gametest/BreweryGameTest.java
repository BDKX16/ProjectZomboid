package io.github.bdkx16.regenerativeagriculture.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import io.github.bdkx16.regenerativeagriculture.block.BreweryBlock;
import io.github.bdkx16.regenerativeagriculture.block.entity.BrewKettleBlockEntity;
import io.github.bdkx16.regenerativeagriculture.block.entity.FermenterBlockEntity;
import io.github.bdkx16.regenerativeagriculture.block.entity.MashTunBlockEntity;
import io.github.bdkx16.regenerativeagriculture.brewing.BrewStage;
import io.github.bdkx16.regenerativeagriculture.brewing.BrewingRules;
import io.github.bdkx16.regenerativeagriculture.item.MateItem;
import io.github.bdkx16.regenerativeagriculture.item.ThermosItem;
import io.github.bdkx16.regenerativeagriculture.registry.ModBlocks;
import io.github.bdkx16.regenerativeagriculture.registry.ModComponents;
import io.github.bdkx16.regenerativeagriculture.registry.ModFoods;
import io.github.bdkx16.regenerativeagriculture.registry.ModItems;

public class BreweryGameTest {
    private static final BlockPos MASH = new BlockPos(1, 1, 1);
    private static final BlockPos KETTLE = new BlockPos(2, 1, 1);
    private static final BlockPos FERMENTER = new BlockPos(3, 1, 1);

    /** A mash tun on its own does nothing: the three machines must stand together. */
    @GameTest
    public void aLoneMashTunDoesNotWork(GameTestHelper helper) {
        helper.setBlock(MASH, ModBlocks.MASH_TUN);
        MashTunBlockEntity mash = helper.getBlockEntity(MASH, MashTunBlockEntity.class);
        ServerPlayer player = player(helper);
        mash.use(new ItemStack(Items.WATER_BUCKET), player);
        mash.use(new ItemStack(ModItems.MALT, BrewingRules.MALT_PER_BATCH), player);
        for (int i = 0; i < BrewingRules.MASH_TICKS / BreweryBlock.TICK_INTERVAL + 5; i++) {
            mash.serverTick(helper.getLevel());
        }
        helper.assertFalse(mash.hasWort(), "an incomplete brewery must not mash");
        helper.succeed();
    }

    /** Water + malt → wort → boiled with hops on a campfire → fermented 7 days → 8 bottles of beer. */
    @GameTest
    public void fromMaltToBeer(GameTestHelper helper) {
        helper.setBlock(MASH, ModBlocks.MASH_TUN);
        helper.setBlock(KETTLE.below(), Blocks.CAMPFIRE);
        helper.setBlock(KETTLE, ModBlocks.BREW_KETTLE);
        helper.setBlock(FERMENTER, ModBlocks.FERMENTER);
        MashTunBlockEntity mash = helper.getBlockEntity(MASH, MashTunBlockEntity.class);
        BrewKettleBlockEntity kettle = helper.getBlockEntity(KETTLE, BrewKettleBlockEntity.class);
        FermenterBlockEntity fermenter = helper.getBlockEntity(FERMENTER, FermenterBlockEntity.class);
        ServerPlayer player = player(helper);

        mash.use(new ItemStack(Items.WATER_BUCKET), player);
        mash.use(new ItemStack(ModItems.MALT, 6), player);
        helper.assertBlockProperty(MASH, BreweryBlock.STAGE, BrewStage.WORKING);
        run(helper, mash, BrewingRules.MASH_TICKS);
        helper.assertItemEntityPresent(ModItems.SPENT_GRAIN, MASH.above(), 2.0);
        helper.assertFalse(mash.hasWort(), "the wort went straight to the kettle");
        helper.assertBlockProperty(KETTLE, BreweryBlock.STAGE, BrewStage.FILLED);
        kettle.use(new ItemStack(ModItems.DRIED_HOPS, 2), player);
        run(helper, kettle, BrewingRules.BOIL_TICKS);
        helper.assertFalse(kettle.boiled(), "boiled over the campfire and passed on");
        helper.assertBlockProperty(FERMENTER, BreweryBlock.STAGE, BrewStage.FILLED);

        fermenter.use(new ItemStack(ModItems.YEAST), player);
        helper.assertTrue(fermenter.fermenting(), "yeast pitched");
        for (int i = 0; i < 20; i++) {
            fermenter.sample(19); // ideal temperature
        }
        fermenter.ageBy(BrewingRules.FERMENT_TICKS);
        fermenter.serverTick(helper.getLevel());
        helper.assertTrue(fermenter.bottlesLeft() == BrewingRules.BOTTLES_PER_BATCH, "8 bottles");

        ItemStack bottles = new ItemStack(Items.GLASS_BOTTLE, 3);
        fermenter.use(bottles, player);
        helper.assertTrue(bottles.getCount() == 2, "one bottle filled");
        helper.assertTrue(player.getInventory().contains(stack -> stack.is(ModFoods.BEER.item())
                && stack.getOrDefault(ModComponents.QUALITY, 0) == 3), "three-star beer at the ideal temperature");
        helper.succeed();
    }

    /** The kettle will not boil without a fire under it. */
    @GameTest
    public void theKettleNeedsFire(GameTestHelper helper) {
        helper.setBlock(MASH, ModBlocks.MASH_TUN);
        helper.setBlock(KETTLE, ModBlocks.BREW_KETTLE);
        helper.setBlock(FERMENTER, ModBlocks.FERMENTER);
        BrewKettleBlockEntity kettle = helper.getBlockEntity(KETTLE, BrewKettleBlockEntity.class);
        kettle.acceptWort();
        kettle.use(new ItemStack(ModItems.DRIED_HOPS, 2), player(helper));
        run(helper, kettle, BrewingRules.BOIL_TICKS);
        helper.assertFalse(kettle.boiled(), "no fire, no boil");
        helper.succeed();
    }

    /** Loading yerba, serving from the thermos after every sip, and a single mate without thermos. */
    @GameTest
    public void mateWithThermos(GameTestHelper helper) {
        ServerPlayer player = player(helper);
        ItemStack mate = new ItemStack(ModItems.MATE);
        player.setItemInHand(InteractionHand.MAIN_HAND, mate);
        player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(ModItems.YERBA, 2));
        ModItems.MATE.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        helper.assertTrue(MateItem.yerba(mate) == MateItem.SERVINGS_PER_LOAD, "yerba loaded");

        player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(ModItems.THERMOS_HOT));
        ModItems.MATE.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        helper.assertTrue(MateItem.served(mate), "served from the thermos");
        mate.finishUsingItem(helper.getLevel(), player);
        helper.assertTrue(player.hasEffect(MobEffects.HASTE), "mate gives haste");
        helper.assertTrue(MateItem.served(mate), "served again after the sip");
        ItemStack thermos = player.getItemInHand(InteractionHand.OFF_HAND);
        helper.assertTrue(thermos.getOrDefault(ModComponents.THERMOS_SERVINGS, 0) == ThermosItem.SERVINGS - 2, "two servings used");

        player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        mate.finishUsingItem(helper.getLevel(), player);
        helper.assertFalse(MateItem.served(mate), "without thermos the mate lasts one serving");
        helper.assertTrue(MateItem.yerba(mate) == MateItem.SERVINGS_PER_LOAD - 2, "yerba used twice");

        ItemStack last = new ItemStack(ModItems.THERMOS_HOT);
        last.set(ModComponents.THERMOS_SERVINGS, 1);
        helper.assertTrue(ThermosItem.pour(last).is(ModItems.THERMOS), "an emptied thermos is empty again");
        helper.succeed();
    }

    private static ServerPlayer player(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        return player;
    }

    private static void run(GameTestHelper helper, io.github.bdkx16.regenerativeagriculture.block.entity.BreweryBlockEntity machine, int ticks) {
        for (int i = 0; i < ticks / BreweryBlock.TICK_INTERVAL; i++) {
            machine.serverTick(helper.getLevel());
        }
    }
}
