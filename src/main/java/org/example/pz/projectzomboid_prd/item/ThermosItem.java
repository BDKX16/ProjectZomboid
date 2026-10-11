package org.example.pz.projectzomboid_prd.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.example.pz.projectzomboid_prd.registry.ModComponents;
import org.example.pz.projectzomboid_prd.registry.ModItems;

import java.util.function.Consumer;

/**
 * Thermos for mate. Empty, it fills like a glass bottle from any water source block, or by holding a water bottle
 * in the other hand. With cold water it goes into a furnace (smoker or campfire) and comes out at 85 °C with
 * {@value #SERVINGS} servings; each mate poured uses one, and when it runs out it is empty again.
 */
public class ThermosItem extends DescribedItem {
    public static final int SERVINGS = 10;

    public enum Kind { EMPTY, COLD, HOT }

    private final Kind kind;

    public ThermosItem(Properties properties, Kind kind) {
        super(properties);
        this.kind = kind;
    }

    public static boolean hasHotWater(ItemStack stack) {
        return stack.getItem() instanceof ThermosItem thermos && thermos.kind == Kind.HOT
                && stack.getOrDefault(ModComponents.THERMOS_SERVINGS, 0) > 0;
    }

    /** Pours one serving; an emptied thermos turns back into an empty one. Returns the thermos as it is now. */
    public static ItemStack pour(ItemStack thermos) {
        int left = thermos.getOrDefault(ModComponents.THERMOS_SERVINGS, 0) - 1;
        if (left > 0) {
            thermos.set(ModComponents.THERMOS_SERVINGS, left);
            return thermos;
        }
        return thermos.transmuteCopy(ModItems.THERMOS, 1);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (kind != Kind.EMPTY) {
            return InteractionResult.PASS;
        }
        ItemStack stack = player.getItemInHand(hand);
        InteractionHand otherHand = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack other = player.getItemInHand(otherHand);
        PotionContents potion = other.get(net.minecraft.core.component.DataComponents.POTION_CONTENTS);
        if (other.is(Items.POTION) && potion != null && potion.is(Potions.WATER)) {
            if (!player.getAbilities().instabuild) {
                player.setItemInHand(otherHand, new ItemStack(Items.GLASS_BOTTLE));
            }
            return fill(level, player, hand, stack, player.blockPosition());
        }
        BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (hit.getType() == HitResult.Type.BLOCK && level.getFluidState(hit.getBlockPos()).is(FluidTags.WATER)) {
            return fill(level, player, hand, stack, hit.getBlockPos());
        }
        return InteractionResult.PASS;
    }

    private static InteractionResult fill(Level level, Player player, InteractionHand hand, ItemStack stack, BlockPos at) {
        level.playSound(player, at, SoundEvents.BOTTLE_FILL, SoundSource.NEUTRAL, 1.0f, 0.9f);
        player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, new ItemStack(ModItems.THERMOS_COLD)));
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        if (kind == Kind.HOT) {
            tooltip.accept(Component.translatable("item.projectzomboid_prd.thermos_hot.servings",
                    stack.getOrDefault(ModComponents.THERMOS_SERVINGS, 0), SERVINGS).withStyle(ChatFormatting.AQUA));
        }
    }
}
