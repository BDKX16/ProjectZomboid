package org.example.pz.projectzomboid_prd.gametest;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.example.pz.projectzomboid_prd.crop.CropProfile;
import org.example.pz.projectzomboid_prd.crop.CropProfiles;
import org.example.pz.projectzomboid_prd.crop.PlantFamily;
import org.example.pz.projectzomboid_prd.registry.ModBlocks;
import org.example.pz.projectzomboid_prd.registry.ModCrops;
import org.example.pz.projectzomboid_prd.registry.ModItems;
import org.example.pz.projectzomboid_prd.soil.SoilData;
import org.example.pz.projectzomboid_prd.soil.SoilManager;
import org.example.pz.projectzomboid_prd.soil.SoilRules;

import java.util.List;

public class SoilGameTest {

    /** Harvests change the soil stored in the chunk; compost restores it; soy fixes nitrogen. */
    @GameTest
    public void harvestsAndCompostChangeTheSoil(GameTestHelper helper) {
        BlockPos crop = new BlockPos(2, 1, 2);
        BlockPos soil = helper.absolutePos(crop.below());
        helper.setBlock(crop.below(), Blocks.FARMLAND);
        CropProfile tomato = profile(helper, ModCrops.TOMATO);

        SoilManager.onHarvest(helper.getLevel(), helper.absolutePos(crop), tomato);
        SoilManager.onHarvest(helper.getLevel(), helper.absolutePos(crop), tomato);
        SoilData drained = SoilManager.get(helper.getLevel(), soil);
        helper.assertTrue(drained.nitrogen() < SoilData.DEFAULT.nitrogen(), "tomatoes should consume nitrogen: " + drained);
        helper.assertTrue(drained.history().equals(List.of(PlantFamily.NIGHTSHADES, PlantFamily.NIGHTSHADES)),
                "history should record both harvests: " + drained);

        // Compost spread on the crop's soil through the item
        UseOnContext context = new UseOnContext(helper.getLevel(), null, InteractionHand.MAIN_HAND,
                new ItemStack(ModItems.COMPOST), new BlockHitResult(Vec3.atCenterOf(soil), Direction.UP, soil, false));
        ModItems.COMPOST.useOn(context);
        SoilData composted = SoilManager.get(helper.getLevel(), soil);
        helper.assertTrue(composted.organic() == drained.organic() + SoilRules.COMPOST_ORGANIC, "compost adds organic matter");

        SoilManager.onHarvest(helper.getLevel(), helper.absolutePos(crop), profile(helper, ModCrops.SOYBEAN));
        helper.assertTrue(SoilManager.get(helper.getLevel(), soil).nitrogen() > composted.nitrogen(), "soy should fix nitrogen");
        helper.succeed();
    }

    /** Tomato next to basil (companion) and potatoes (antagonist), three species in all. */
    @GameTest
    public void neighboursAreDetected(GameTestHelper helper) {
        BlockPos tomato = new BlockPos(3, 1, 3);
        for (int x = 1; x <= 5; x++) {
            helper.setBlock(new BlockPos(x, 0, 3), Blocks.FARMLAND);
        }
        helper.setBlock(tomato, ModCrops.TOMATO.block());
        helper.setBlock(tomato.east(), ModCrops.BASIL.block());
        helper.setBlock(tomato.west(), Blocks.POTATOES);

        SoilRules.Neighbourhood neighbours = SoilManager.neighbourhood(helper.getLevel(), helper.absolutePos(tomato),
                profile(helper, ModCrops.TOMATO));
        helper.assertTrue(neighbours.companion() && neighbours.antagonist() && neighbours.species() == 3,
                "expected companion, antagonist and 3 species: " + neighbours);
        helper.succeed();
    }

    /** A full compost bin gives compost and empties. */
    @GameTest
    public void compostBinGivesCompost(GameTestHelper helper) {
        BlockPos bin = new BlockPos(2, 1, 2);
        helper.setBlock(bin, ModBlocks.COMPOST_BIN.defaultBlockState().setValue(ComposterBlock.LEVEL, ComposterBlock.READY));
        helper.useBlock(bin, helper.makeMockPlayer(GameType.SURVIVAL));
        helper.assertItemEntityPresent(ModItems.COMPOST, bin, 2.0);
        helper.assertTrue(helper.getBlockState(bin).getValue(ComposterBlock.LEVEL) == 0, "the bin should be empty again");
        helper.succeed();
    }

    private static CropProfile profile(GameTestHelper helper, ModCrops.Crop crop) {
        return CropProfiles.get(helper.getLevel().registryAccess(), crop.block()).orElseThrow();
    }
}
