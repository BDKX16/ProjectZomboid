package org.example.pz.projectzomboid_prd.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import org.example.pz.projectzomboid_prd.crop.CropClimate;
import org.example.pz.projectzomboid_prd.crop.CropProfiles;
import org.example.pz.projectzomboid_prd.crop.GrowthCalculator;
import org.example.pz.projectzomboid_prd.registry.ModCrops;
import org.example.pz.projectzomboid_prd.season.Season;
import org.example.pz.projectzomboid_prd.season.SeasonManager;
import org.example.pz.projectzomboid_prd.worldgen.ModWorldGen;

import java.util.Arrays;

/**
 * Runs on a real (headless) server, so it checks what unit tests cannot: registries, data loading and the mixin.
 * Kept as one sequential test because it changes the global season.
 */
public class SeasonalCropGameTest {
    private static final BlockPos SOIL = new BlockPos(1, 1, 1);
    private static final BlockPos CROP = SOIL.above();

    @GameTest
    public void seasonalCropsWork(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();

        // The mixin must be woven into CropBlock (not only "lettuce did not grow", which darkness could also cause).
        helper.assertTrue(Arrays.stream(CropBlock.class.getDeclaredMethods())
                        .anyMatch(method -> method.getName().contains("seasonalGrowth")),
                "CropBlockMixin is not applied");

        // Data: profiles for vanilla and mod crops, wild plant features.
        helper.assertTrue(CropProfiles.get(level.registryAccess(), Blocks.WHEAT).isPresent(), "wheat profile missing");
        for (ModCrops.Crop crop : ModCrops.ALL) {
            helper.assertTrue(CropProfiles.get(level.registryAccess(), crop.block()).isPresent(), crop.name() + " profile missing");
            helper.assertTrue(level.registryAccess().lookupOrThrow(Registries.PLACED_FEATURE)
                    .get(ModWorldGen.placedKey(crop)).isPresent(), crop.name() + " placed feature missing");
        }

        // Seeds only go on farmland.
        helper.setBlock(SOIL, Blocks.DIRT);
        helper.assertFalse(ModCrops.LETTUCE.block().defaultBlockState().canSurvive(level, helper.absolutePos(CROP)),
                "crops must not survive on plain dirt");

        // Lettuce is a cool-season crop: in summer it must not grow, through the mixin.
        SeasonManager.setSeason(level.getServer(), Season.SUMMER);
        helper.setBlock(SOIL, Blocks.FARMLAND);
        helper.setBlock(CROP, ModCrops.LETTUCE.block());
        BlockPos crop = helper.absolutePos(CROP);

        GrowthCalculator.Growth growth = CropClimate.growth(level, crop, ModCrops.LETTUCE.block()).orElseThrow();
        // Depending on the hour it is either out of season or, on a hot afternoon, wilting from heat.
        helper.assertTrue(growth.factor() == 0, "lettuce should not grow in summer, got " + growth);
        for (int i = 0; i < 200 && level.getBlockState(crop).is(ModCrops.LETTUCE.block()); i++) {
            level.getBlockState(crop).randomTick(level, crop, level.getRandom());
        }
        CropBlock lettuce = ModCrops.LETTUCE.block();
        helper.assertFalse(level.getBlockState(crop).is(lettuce) && lettuce.getAge(level.getBlockState(crop)) > 0,
                "lettuce grew in summer: the CropBlock mixin is not applied");

        helper.succeed();
    }
}
