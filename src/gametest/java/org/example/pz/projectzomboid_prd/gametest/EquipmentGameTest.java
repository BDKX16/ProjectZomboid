package org.example.pz.projectzomboid_prd.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import org.example.pz.projectzomboid_prd.block.ExtractorBlock;
import org.example.pz.projectzomboid_prd.registry.ModBlocks;

public class EquipmentGameTest {

    /** The grow light hangs from a block and falls off when that block is removed. */
    @GameTest
    public void growLightNeedsCeiling(GameTestHelper helper) {
        BlockPos ceiling = new BlockPos(2, 4, 2);
        BlockPos light = ceiling.below();
        helper.setBlock(ceiling, Blocks.STONE);
        helper.setBlock(light, ModBlocks.GROW_LIGHT);
        helper.assertTrue(helper.getBlockState(light).canSurvive(helper.getLevel(), helper.absolutePos(light)),
                "a grow light under stone should hold");

        helper.setBlock(ceiling, Blocks.AIR);
        helper.assertTrue(helper.getBlockState(light).isAir(), "the grow light should drop without its ceiling");
        helper.succeed();
    }

    /** The extractor's fan turns on when its shaft reaches the sky and off when the shaft is blocked. */
    @GameTest(skyAccess = true, maxTicks = 200)
    public void extractorFanFollowsVentShaft(GameTestHelper helper) {
        BlockPos extractor = new BlockPos(2, 1, 2);
        BlockPos above = extractor.above();
        helper.setBlock(extractor, ModBlocks.EXTRACTOR);

        helper.runAfterDelay(10, () -> {
            helper.assertTrue(helper.getBlockState(extractor).getValue(ExtractorBlock.ACTIVE), "open shaft: fan should spin");
            helper.setBlock(above, Blocks.STONE);
        });
        helper.runAfterDelay(80, () -> {
            helper.assertFalse(helper.getBlockState(extractor).getValue(ExtractorBlock.ACTIVE), "blocked shaft: fan should stop");
            helper.succeed();
        });
    }
}
