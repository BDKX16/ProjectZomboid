package org.example.pz.projectzomboid_prd.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.example.pz.projectzomboid_prd.Projectzomboid_prd;
import org.example.pz.projectzomboid_prd.block.SeasonalCropBlock;
import org.example.pz.projectzomboid_prd.crop.CropProfile;
import org.example.pz.projectzomboid_prd.crop.CropProfiles;
import org.example.pz.projectzomboid_prd.soil.SoilManager;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Harvests a mature crop without breaking it: drops the produce and sets the plant back to the profile's
 * {@code regrow_age}, so it fruits again (tomatoes, basil, cut-and-come-again lettuce…).
 */
public class PruningShearsItem extends Item {
    private static final String KEY = "item." + Projectzomboid_prd.MOD_ID + ".pruning_shears.";

    public PruningShearsItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable(KEY + "desc").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof SeasonalCropBlock crop)) {
            return InteractionResult.PASS;
        }

        Player player = context.getPlayer();
        Optional<CropProfile> profile = CropProfiles.get(level.registryAccess(), crop);
        Optional<Integer> regrowAge = profile.flatMap(CropProfile::regrowAge);
        if (regrowAge.isEmpty()) {
            message(level, player, "single_harvest");
            return InteractionResult.FAIL;
        }
        if (!crop.isMaxAge(state)) {
            message(level, player, "not_ripe");
            return InteractionResult.FAIL;
        }

        if (level instanceof ServerLevel) {
            Block.popResource(level, pos, new ItemStack(crop.getProduce(), 1 + level.getRandom().nextInt(2)));
            level.setBlock(pos, crop.getStateForAge(regrowAge.get()), Block.UPDATE_ALL);
            SoilManager.onHarvest(level, pos, profile.get());
            level.playSound(null, pos, SoundEvents.SHEEP_SHEAR, SoundSource.BLOCKS, 1.0f, 1.2f);
            if (player != null) {
                context.getItemInHand().hurtAndBreak(1, player, context.getHand());
            }
        }
        return InteractionResult.SUCCESS;
    }

    private static void message(Level level, Player player, String key) {
        if (player != null && !level.isClientSide()) {
            player.sendOverlayMessage(Component.translatable(KEY + key));
        }
    }
}
