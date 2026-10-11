package org.example.pz.projectzomboid_prd.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.example.pz.projectzomboid_prd.Projectzomboid_prd;
import org.example.pz.projectzomboid_prd.crop.CropProfile;
import org.example.pz.projectzomboid_prd.crop.CropProfiles;
import org.example.pz.projectzomboid_prd.crop.PlantFamily;
import org.example.pz.projectzomboid_prd.soil.SoilData;
import org.example.pz.projectzomboid_prd.soil.SoilManager;
import org.example.pz.projectzomboid_prd.soil.SoilRules;

import java.util.Optional;

/** Right-click farmland (or a crop on it) to read nitrogen, organic matter, crop history and their effect. */
public class SoilTestKitItem extends DescribedItem {
    private static final String KEY = "item." + Projectzomboid_prd.MOD_ID + ".soil_test_kit.";

    public SoilTestKitItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos soilPos = SoilAmendmentItem.farmland(level, context.getClickedPos());
        Player player = context.getPlayer();
        if (soilPos == null || player == null) {
            return InteractionResult.PASS;
        }
        if (level instanceof ServerLevel serverLevel) {
            report(serverLevel, soilPos, player);
        }
        return InteractionResult.SUCCESS;
    }

    private static void report(ServerLevel level, BlockPos soilPos, Player player) {
        SoilData soil = SoilManager.get(level, soilPos);
        player.sendSystemMessage(Component.translatable(KEY + "header").withStyle(ChatFormatting.GOLD));
        player.sendSystemMessage(Component.translatable(KEY + "nutrients", soil.nitrogen(), soil.organic(),
                Math.round(SoilRules.fertility(soil) * 100)));
        player.sendSystemMessage(soil.mulch() > 0
                ? Component.translatable(KEY + "mulch", soil.mulch())
                : Component.translatable(KEY + "no_mulch").withStyle(ChatFormatting.GRAY));

        MutableComponent history = Component.translatable(KEY + "history");
        if (soil.history().isEmpty()) {
            history.append(Component.translatable(KEY + "history.none"));
        }
        for (int i = 0; i < soil.history().size(); i++) {
            history.append(i == 0 ? Component.literal(" ") : Component.literal(" ← "));
            history.append(soil.history().get(i).displayName());
        }
        player.sendSystemMessage(history);

        BlockPos cropPos = soilPos.above();
        Optional<CropProfile> profile = CropProfiles.get(level.registryAccess(), level.getBlockState(cropPos).getBlock());
        if (profile.isEmpty()) {
            return;
        }
        PlantFamily family = profile.get().family();
        float rotation = SoilRules.rotation(soil, family);
        if (rotation < 1) {
            player.sendSystemMessage(Component.translatable(rotation <= SoilRules.MONOCULTURE ? KEY + "monoculture" : KEY + "same_family",
                    family.displayName(), percent(rotation - 1)).withStyle(ChatFormatting.RED));
        }
        SoilRules.Neighbourhood neighbours = SoilManager.neighbourhood(level, cropPos, profile.get());
        if (neighbours.companion()) {
            player.sendSystemMessage(Component.translatable(KEY + "companion", percent(SoilRules.COMPANION_BONUS - 1)).withStyle(ChatFormatting.GREEN));
        }
        if (neighbours.antagonist()) {
            player.sendSystemMessage(Component.translatable(KEY + "antagonist", percent(SoilRules.ANTAGONIST_PENALTY - 1)).withStyle(ChatFormatting.RED));
        }
        if (neighbours.species() >= SoilRules.DIVERSE_SPECIES) {
            player.sendSystemMessage(Component.translatable(KEY + "diversity", neighbours.species(), percent(SoilRules.DIVERSITY_BONUS - 1)).withStyle(ChatFormatting.GREEN));
        }
        player.sendSystemMessage(Component.translatable(KEY + "total",
                Math.round(SoilRules.multiplier(soil, family, neighbours) * 100)).withStyle(ChatFormatting.AQUA));
    }

    private static String percent(float delta) {
        int value = Math.round(delta * 100);
        return (value >= 0 ? "+" : "") + value + "%";
    }
}
