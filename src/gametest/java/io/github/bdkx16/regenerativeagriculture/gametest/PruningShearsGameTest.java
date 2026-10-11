package io.github.bdkx16.regenerativeagriculture.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import io.github.bdkx16.regenerativeagriculture.block.SeasonalCropBlock;
import io.github.bdkx16.regenerativeagriculture.registry.ModCrops;
import io.github.bdkx16.regenerativeagriculture.registry.ModItems;

public class PruningShearsGameTest {
    private static final BlockPos TOMATO = new BlockPos(2, 1, 2);
    private static final BlockPos SOY = new BlockPos(4, 1, 2);

    @GameTest
    public void shearsHarvestRegrowableCropsOnly(GameTestHelper helper) {
        SeasonalCropBlock tomato = ModCrops.TOMATO.block();
        SeasonalCropBlock soy = ModCrops.SOYBEAN.block();
        plantMature(helper, TOMATO, tomato);
        plantMature(helper, SOY, soy);

        // Tomato: produce drops and the plant stays, set back to its regrow age.
        helper.assertTrue(useShears(helper, TOMATO).consumesAction(), "shears should work on a ripe tomato");
        helper.assertTrue(helper.getBlockState(TOMATO).is(tomato), "the tomato plant must not be broken");
        helper.assertTrue(tomato.getAge(helper.getBlockState(TOMATO)) == 5, "tomato should regrow from age 5");
        helper.assertItemEntityPresent(ModCrops.TOMATO.produce(), TOMATO, 2.0);

        // Unripe now: a second cut does nothing.
        helper.assertFalse(useShears(helper, TOMATO).consumesAction(), "an unripe tomato cannot be cut");

        // Soy is harvested once: shears refuse it and the plant is untouched.
        helper.assertFalse(useShears(helper, SOY).consumesAction(), "soy has no regrow age");
        helper.assertTrue(soy.isMaxAge(helper.getBlockState(SOY)), "soy must stay as it was");

        helper.succeed();
    }

    private static void plantMature(GameTestHelper helper, BlockPos pos, CropBlock crop) {
        helper.setBlock(pos.below(), Blocks.FARMLAND);
        helper.setBlock(pos, crop.getStateForAge(crop.getMaxAge()));
    }

    private static InteractionResult useShears(GameTestHelper helper, BlockPos relative) {
        BlockPos pos = helper.absolutePos(relative);
        UseOnContext context = new UseOnContext(helper.getLevel(), null, InteractionHand.MAIN_HAND,
                new ItemStack(ModItems.PRUNING_SHEARS), new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
        return ModItems.PRUNING_SHEARS.useOn(context);
    }
}
