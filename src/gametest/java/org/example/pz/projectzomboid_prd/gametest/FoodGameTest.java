package org.example.pz.projectzomboid_prd.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import org.example.pz.projectzomboid_prd.effect.ModEffects;
import org.example.pz.projectzomboid_prd.registry.ModCrops;
import org.example.pz.projectzomboid_prd.registry.ModFoods;

public class FoodGameTest {

    @GameTest
    public void hamburgerFillsTheHungerBar(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.getFoodData().setFoodLevel(0);
        eat(helper, player, ModFoods.HAMBURGER.item());
        helper.assertTrue(player.getFoodData().getFoodLevel() == 20, "hunger: " + player.getFoodData().getFoodLevel());
        helper.succeed();
    }

    @GameTest
    public void saladGivesVitalityAndReturnsTheBowl(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL); // creative players keep the bowl's contents
        double base = player.getAttributeValue(Attributes.MOVEMENT_SPEED);
        ItemStack left = eat(helper, player, ModFoods.GREEN_SALAD.item());
        helper.assertTrue(left.is(Items.BOWL), "the bowl should be given back: " + left);
        helper.assertTrue(player.hasEffect(ModEffects.VITALITY), "salad gives vitality");
        helper.succeedWhen(() -> {
            double speed = player.getAttributeValue(Attributes.MOVEMENT_SPEED);
            helper.assertTrue(Math.abs(speed - base * 1.10) < 1e-6, "speed should be +10 %: " + base + " -> " + speed);
        });
    }

    @GameTest
    public void broccoliCuresAndPreventsPoison(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.addEffect(new MobEffectInstance(MobEffects.POISON, 200));
        eat(helper, player, ModFoods.STEAMED_BROCCOLI.item());
        helper.assertFalse(player.hasEffect(MobEffects.POISON), "broccoli cures poison");
        player.addEffect(new MobEffectInstance(MobEffects.POISON, 200));
        helper.assertFalse(player.hasEffect(MobEffects.POISON), "immunity blocks new poison");
        player.addEffect(new MobEffectInstance(MobEffects.SPEED, 200));
        helper.assertTrue(player.hasEffect(MobEffects.SPEED), "other effects still apply");
        helper.succeed();
    }

    @GameTest
    public void squashSoupKeepsTheColdAway(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        helper.assertTrue(player.canFreeze(), "players freeze without warmth");
        eat(helper, player, ModFoods.SQUASH_SOUP.item());
        helper.assertFalse(player.canFreeze(), "squash soup: no freezing");
        helper.succeed();
    }

    /** Lettuce, tomato and a carrot are three vegetables; broccoli is the fourth and the diet is varied. */
    @GameTest
    public void variedDietNourishes(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        eat(helper, player, ModCrops.LETTUCE.produce());
        eat(helper, player, ModCrops.TOMATO.produce());
        eat(helper, player, Items.CARROT);
        eat(helper, player, Items.BREAD);
        helper.assertFalse(player.hasEffect(ModEffects.NOURISHED), "three vegetables are not enough");
        eat(helper, player, ModCrops.BROCCOLI.produce());
        helper.assertTrue(player.hasEffect(ModEffects.NOURISHED), "four vegetables: nourished");
        helper.succeedWhen(() -> helper.assertTrue(player.getMaxHealth() == 24, "max health: " + player.getMaxHealth()));
    }

    private static ItemStack eat(GameTestHelper helper, ServerPlayer player, Item food) {
        return new ItemStack(food).finishUsingItem(helper.getLevel(), player);
    }
}
