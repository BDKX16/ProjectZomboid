package io.github.bdkx16.regenerativeagriculture.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import io.github.bdkx16.regenerativeagriculture.greenhouse.Shelter;
import io.github.bdkx16.regenerativeagriculture.greenhouse.ShelterDetector;
import io.github.bdkx16.regenerativeagriculture.registry.ModBlocks;
import io.github.bdkx16.regenerativeagriculture.registry.ModCrops;

/** Extractor capacity in a crowded grow room, and mold when the air is stale. */
public class VentilationGameTest {
    private static final int ROOF_Y = 4;
    private static final BlockPos CENTER = new BlockPos(3, 1, 3);

    /** 25 crops need two extractors: one gives half ventilation, two give full. */
    @GameTest(skyAccess = true)
    public void extractorsScaleWithPlants(GameTestHelper helper) {
        for (int x = 0; x <= 6; x++) {
            for (int z = 0; z <= 6; z++) {
                helper.setBlock(new BlockPos(x, ROOF_Y, z), Blocks.STONE);
            }
        }
        for (int x = 1; x <= 5; x++) {
            for (int z = 1; z <= 5; z++) {
                helper.setBlock(new BlockPos(x, 0, z), Blocks.FARMLAND);
                helper.setBlock(new BlockPos(x, 1, z), ModCrops.LETTUCE.block());
            }
        }
        helper.setBlock(CENTER.above(2), ModBlocks.GROW_LIGHT);
        addExtractor(helper, new BlockPos(0, 3, 0));

        Shelter shelter = detect(helper);
        helper.assertTrue(shelter.type() == Shelter.Type.GROW_ROOM, "expected a grow room, got " + shelter);
        helper.assertTrue(shelter.ventilation().plants() == 25 && shelter.ventilation().needed() == 2,
                "25 plants need 2 extractors: " + shelter.ventilation());
        helper.assertTrue(shelter.ventilationRatio() == 0.5f, "one extractor: half ventilation, got " + shelter.ventilationRatio());

        addExtractor(helper, new BlockPos(6, 3, 6));
        helper.assertTrue(detect(helper).ventilated(), "two extractors should be enough");
        helper.succeed();
    }

    /** In a grow room with no extractor the air is at 95%: crops go moldy and the mold has no drops. */
    @GameTest(skyAccess = true)
    public void staleAirGrowsMold(GameTestHelper helper) {
        for (int x = 1; x <= 5; x++) {
            for (int z = 1; z <= 5; z++) {
                helper.setBlock(new BlockPos(x, ROOF_Y, z), Blocks.STONE);
            }
        }
        helper.setBlock(CENTER.below(), Blocks.FARMLAND);
        helper.setBlock(CENTER, ModCrops.TOMATO.block());
        helper.setBlock(CENTER.above(2), ModBlocks.GROW_LIGHT);
        helper.assertTrue(detect(helper).type() == Shelter.Type.GROW_ROOM_UNVENTILATED, "expected an unventilated grow room");

        ServerLevel level = helper.getLevel();
        BlockPos crop = helper.absolutePos(CENTER);
        for (int i = 0; i < 400 && !level.getBlockState(crop).is(ModBlocks.MOLDY_CROP); i++) {
            level.getBlockState(crop).randomTick(level, crop, level.getRandom());
        }
        helper.assertTrue(level.getBlockState(crop).is(ModBlocks.MOLDY_CROP), "the tomato should have gone moldy");
        helper.succeed();
    }

    private static void addExtractor(GameTestHelper helper, BlockPos pos) {
        helper.setBlock(pos, ModBlocks.EXTRACTOR);
        helper.setBlock(pos.above(), Blocks.IRON_BARS);
    }

    private static Shelter detect(GameTestHelper helper) {
        return ShelterDetector.detect(helper.getLevel(), helper.absolutePos(CENTER));
    }
}
