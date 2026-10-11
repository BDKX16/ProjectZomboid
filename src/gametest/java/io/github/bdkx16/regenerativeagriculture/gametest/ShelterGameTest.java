package io.github.bdkx16.regenerativeagriculture.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import io.github.bdkx16.regenerativeagriculture.greenhouse.Shelter;
import io.github.bdkx16.regenerativeagriculture.greenhouse.ShelterDetector;
import io.github.bdkx16.regenerativeagriculture.registry.ModBlocks;
import io.github.bdkx16.regenerativeagriculture.registry.ModCrops;
import io.github.bdkx16.regenerativeagriculture.registry.ModTags;

/** Builds a greenhouse, then turns it into an underground grow room, checking what the detector sees at each step. */
public class ShelterGameTest {
    private static final BlockPos CROP = new BlockPos(3, 1, 3);
    private static final int MIN = 1;
    private static final int MAX = 5;
    private static final int ROOF_Y = 4;
    /** Extractor in the wall; the roof block above it is the vent opening. */
    private static final BlockPos EXTRACTOR = new BlockPos(MIN, 3, 3);
    private static final BlockPos VENT = EXTRACTOR.above();

    @GameTest(skyAccess = true)
    public void sheltersAreDetected(GameTestHelper helper) {
        helper.setBlock(CROP.below(), Blocks.FARMLAND);
        helper.setBlock(CROP, ModCrops.TOMATO.block());
        expect(helper, Shelter.Type.OPEN, false, "outdoors");

        // Glass roof + stone walls = greenhouse, not ventilated.
        roof(helper, Blocks.GLASS);
        for (int y = 1; y < ROOF_Y; y++) {
            for (int i = MIN; i <= MAX; i++) {
                helper.setBlock(new BlockPos(MIN, y, i), Blocks.STONE);
                helper.setBlock(new BlockPos(MAX, y, i), Blocks.STONE);
                helper.setBlock(new BlockPos(i, y, MIN), Blocks.STONE);
                helper.setBlock(new BlockPos(i, y, MAX), Blocks.STONE);
            }
        }
        expect(helper, Shelter.Type.GREENHOUSE, false, "closed greenhouse");

        // Extractor in the wall with iron bars above it, open to the sky.
        helper.setBlock(EXTRACTOR, ModBlocks.EXTRACTOR);
        helper.setBlock(VENT, Blocks.IRON_BARS);
        expect(helper, Shelter.Type.GREENHOUSE, true, "greenhouse with extractor");

        // Opaque roof + grow light = underground grow room, ventilated through the same vent.
        roof(helper, Blocks.STONE);
        helper.setBlock(VENT, Blocks.IRON_BARS);
        helper.setBlock(CROP.above(2), ModBlocks.GROW_LIGHT);
        expect(helper, Shelter.Type.GROW_ROOM, true, "ventilated grow room");

        // Block the vent shaft: the room is no longer ventilated.
        helper.setBlock(VENT, Blocks.STONE);
        expect(helper, Shelter.Type.GROW_ROOM_UNVENTILATED, false, "grow room with blocked vent");

        helper.succeed();
    }

    private static void roof(GameTestHelper helper, Block block) {
        for (int x = MIN; x <= MAX; x++) {
            for (int z = MIN; z <= MAX; z++) {
                helper.setBlock(new BlockPos(x, ROOF_Y, z), block);
            }
        }
    }

    private static void expect(GameTestHelper helper, Shelter.Type type, boolean ventilated, String what) {
        BlockPos crop = helper.absolutePos(CROP);
        // Tests run at random places: in a desert or badlands any greenhouse overheats.
        if (type == Shelter.Type.GREENHOUSE && helper.getLevel().getBiome(crop).is(ModTags.GREENHOUSE_OVERHEATS)) {
            type = Shelter.Type.GREENHOUSE_OVERHEATED;
            ventilated = false;
        }
        Shelter shelter = ShelterDetector.detect(helper.getLevel(), crop);
        helper.assertTrue(shelter.type() == type && shelter.ventilated() == ventilated,
                what + ": expected " + type + (ventilated ? " (ventilated)" : "") + " but got " + shelter);
    }
}
