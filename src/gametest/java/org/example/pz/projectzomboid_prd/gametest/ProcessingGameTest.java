package org.example.pz.projectzomboid_prd.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.example.pz.projectzomboid_prd.block.DryingRackBlock;
import org.example.pz.projectzomboid_prd.block.entity.CuringJarBlockEntity;
import org.example.pz.projectzomboid_prd.block.entity.DryingRackBlockEntity;
import org.example.pz.projectzomboid_prd.crop.CropProfile;
import org.example.pz.projectzomboid_prd.crop.CropProfiles;
import org.example.pz.projectzomboid_prd.effect.ModEffects;
import org.example.pz.projectzomboid_prd.processing.DryingRules;
import org.example.pz.projectzomboid_prd.registry.ModBlocks;
import org.example.pz.projectzomboid_prd.registry.ModComponents;
import org.example.pz.projectzomboid_prd.registry.ModCrops;
import org.example.pz.projectzomboid_prd.registry.ModFoods;
import org.example.pz.projectzomboid_prd.registry.ModItems;
import org.example.pz.projectzomboid_prd.soil.SoilManager;
import org.example.pz.projectzomboid_prd.soil.SoilRules;

public class ProcessingGameTest {

    /** Tobacco hung in dry, mild air dries in its two days; the rack shows it. */
    @GameTest
    public void tobaccoDriesOnTheRack(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, ModBlocks.DRYING_RACK);
        DryingRackBlockEntity rack = helper.getBlockEntity(pos, DryingRackBlockEntity.class);
        ItemStack leaves = new ItemStack(ModCrops.TOBACCO.produce(), 20);
        helper.assertTrue(rack.insert(leaves), "tobacco can be hung");
        helper.assertTrue(rack.contents().getCount() == 16 && leaves.getCount() == 4, "the rack takes up to 16");
        helper.assertBlockProperty(pos, DryingRackBlock.CONTENTS, DryingRackBlock.Contents.FRESH);

        rack.update(20, 50, false, DryingRules.DAY);
        helper.assertTrue(rack.contents().is(ModCrops.TOBACCO.produce()), "still drying after one day");
        rack.update(20, 50, false, DryingRules.DAY);
        helper.assertTrue(rack.contents().is(ModItems.DRIED_TOBACCO) && rack.contents().getCount() == 16, "dry after two days");
        helper.assertBlockProperty(pos, DryingRackBlock.CONTENTS, DryingRackBlock.Contents.DRY);
        helper.succeed();
    }

    @GameTest
    public void muggyAirSpoilsTheHarvest(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, ModBlocks.DRYING_RACK);
        DryingRackBlockEntity rack = helper.getBlockEntity(pos, DryingRackBlockEntity.class);
        rack.insert(new ItemStack(ModCrops.CANNABIS.produce(), 4));
        rack.update(22, 92, false, DryingRules.MOLD_TICKS);
        helper.assertTrue(rack.contents().is(ModItems.SPOILED_HARVEST), "a day at 92 percent humidity: moldy");
        helper.assertBlockProperty(pos, DryingRackBlock.CONTENTS, DryingRackBlock.Contents.SPOILED);
        helper.succeed();
    }

    /** Dried tobacco in the jar: nothing at day 1, two stars at day 6. */
    @GameTest
    public void curingRaisesQuality(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, ModBlocks.CURING_JAR);
        CuringJarBlockEntity jar = helper.getBlockEntity(pos, CuringJarBlockEntity.class);
        helper.assertFalse(jar.insert(new ItemStack(ModCrops.TOBACCO.produce())), "fresh leaves must be dried first");
        helper.assertTrue(jar.insert(new ItemStack(ModItems.DRIED_TOBACCO, 8)), "dried tobacco goes in");
        long now = helper.getLevel().getGameTime();
        jar.ageBy(1);
        helper.assertTrue(jar.quality(now) == 0, "not cured after one day");
        jar.ageBy(5);
        ItemStack cured = jar.takeOut(now);
        helper.assertTrue(cured.is(ModItems.CURED_TOBACCO) && cured.getCount() == 8, "cured: " + cured);
        helper.assertTrue(cured.getOrDefault(ModComponents.QUALITY, 0) == 2, "two stars after 6 days: " + cured);
        helper.succeed();
    }

    /** A three-star brownie relaxes twice as long as a plain one. */
    @GameTest
    public void qualityLengthensEffects(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        ItemStack brownie = new ItemStack(ModFoods.BROWNIE.item());
        brownie.set(ModComponents.QUALITY, 3);
        brownie.finishUsingItem(helper.getLevel(), player);
        MobEffectInstance relaxed = player.getEffect(ModEffects.RELAXED);
        int base = ModFoods.BROWNIE.effects().getFirst().getDuration();
        helper.assertTrue(relaxed != null && relaxed.getDuration() == base * 2, "duration: " + relaxed);
        helper.succeed();
    }

    /** Straw mulch shows on the soil, cannot be spread twice and disappears when it is used up. */
    @GameTest
    public void mulchIsVisibleAndSingle(GameTestHelper helper) {
        BlockPos soil = new BlockPos(1, 1, 1);
        helper.setBlock(soil, Blocks.FARMLAND);
        helper.setBlock(soil.above(), ModCrops.LETTUCE.block());
        BlockPos absolute = helper.absolutePos(soil);
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        ItemStack mulch = new ItemStack(ModItems.STRAW_MULCH, 2);
        player.setItemInHand(InteractionHand.MAIN_HAND, mulch);

        spread(player, absolute.above());
        helper.assertBlockPresent(ModBlocks.MULCHED_FARMLAND, soil);
        helper.assertBlockPresent(ModCrops.LETTUCE.block(), soil.above());
        helper.assertTrue(mulch.getCount() == 1, "one mulch used");

        spread(player, absolute.above());
        helper.assertTrue(mulch.getCount() == 1, "the second mulch must be refused");
        helper.assertTrue(SoilManager.get(helper.getLevel(), absolute).mulch() == SoilRules.MULCH_HARVESTS, "mulch not stacked");

        CropProfile lettuce = CropProfiles.get(helper.getLevel().registryAccess(), ModCrops.LETTUCE.block()).orElseThrow();
        for (int i = 0; i < SoilRules.MULCH_HARVESTS; i++) {
            SoilManager.onHarvest(helper.getLevel(), absolute.above(), lettuce);
        }
        helper.assertBlockPresent(Blocks.FARMLAND, soil);
        helper.succeed();
    }

    private static void spread(ServerPlayer player, BlockPos crop) {
        ModItems.STRAW_MULCH.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(crop), Direction.UP, crop, false)));
    }
}
