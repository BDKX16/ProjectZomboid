package io.github.bdkx16.regenerativeagriculture.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import io.github.bdkx16.regenerativeagriculture.processing.Quality;
import io.github.bdkx16.regenerativeagriculture.registry.ModComponents;
import io.github.bdkx16.regenerativeagriculture.registry.ModItems;
import io.github.bdkx16.regenerativeagriculture.registry.ModSounds;

import java.util.function.Consumer;

/**
 * The mate: a cured gourd with a metal straw, used over and over.
 * <ul>
 *   <li>With yerba in the other hand, right click loads it ({@value #SERVINGS_PER_LOAD} servings, keeps the yerba's
 *   quality).</li>
 *   <li>A served ("cebado") mate can be drunk once. Holding a thermos with hot water in the other hand, it is
 *   served before drinking if needed and served again after every sip, using one serving of the thermos each
 *   time.</li>
 *   <li>Drinking gives Haste (longer with better yerba) and a little food. When the yerba is washed out the mate
 *   is empty again.</li>
 * </ul>
 */
public class MateItem extends DescribedItem {
    public static final int SERVINGS_PER_LOAD = 10;
    private static final int HASTE_TICKS = 2 * 60 * 20;
    private static final int DRINK_TICKS = 32;
    private static final int SIP_EVERY = 8;

    public MateItem(Properties properties) {
        super(properties);
    }

    public static boolean served(ItemStack mate) {
        return mate.has(ModComponents.MATE_SERVED);
    }

    public static int yerba(ItemStack mate) {
        return mate.getOrDefault(ModComponents.MATE_YERBA, 0);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack mate = player.getItemInHand(hand);
        InteractionHand otherHand = other(hand);
        ItemStack other = player.getItemInHand(otherHand);

        if (yerba(mate) == 0) {
            if (!other.is(ModItems.YERBA)) {
                hint(player, "item.regenerative_agriculture.mate.needs_yerba");
                return InteractionResult.FAIL;
            }
            mate.set(ModComponents.MATE_YERBA, SERVINGS_PER_LOAD);
            int quality = Quality.of(other);
            if (quality > 0) {
                mate.set(ModComponents.QUALITY, quality);
            } else {
                mate.remove(ModComponents.QUALITY);
            }
            other.consume(1, player);
            level.playSound(player, player.blockPosition(), ModSounds.MATE_LOAD.value(), SoundSource.PLAYERS, 1.0f, 1.0f);
            return InteractionResult.SUCCESS;
        }
        if (!served(mate)) {
            if (!ThermosItem.hasHotWater(other)) {
                hint(player, "item.regenerative_agriculture.mate.needs_thermos");
                return InteractionResult.FAIL;
            }
            serve(player, mate, otherHand);
        }
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack mate, Level level, LivingEntity entity) {
        if (!served(mate)) {
            return mate;
        }
        level.playSound(null, entity.blockPosition(), ModSounds.MATE_SLURP.value(), SoundSource.PLAYERS, 1.0f, 1.0f);
        float multiplier = Quality.durationMultiplier(Quality.of(mate));
        entity.addEffect(new MobEffectInstance(MobEffects.HASTE, Math.round(HASTE_TICKS * multiplier)));
        mate.remove(ModComponents.MATE_SERVED);
        int left = yerba(mate) - 1;
        if (entity instanceof Player player) {
            player.getFoodData().eat(1, 0.6f);
        }
        if (left <= 0) {
            mate.remove(ModComponents.MATE_YERBA);
            mate.remove(ModComponents.QUALITY);
            if (entity instanceof Player player) {
                hint(player, "item.regenerative_agriculture.mate.washed_out");
            }
            return mate;
        }
        mate.set(ModComponents.MATE_YERBA, left);
        if (entity instanceof Player player) {
            InteractionHand otherHand = other(player.getUsedItemHand());
            if (ThermosItem.hasHotWater(player.getItemInHand(otherHand))) {
                serve(player, mate, otherHand);
            }
        }
        return mate;
    }

    /** Pours hot water from the thermos in {@code thermosHand} into the mate. */
    private static void serve(Player player, ItemStack mate, InteractionHand thermosHand) {
        player.setItemInHand(thermosHand, ThermosItem.pour(player.getItemInHand(thermosHand)));
        mate.set(ModComponents.MATE_SERVED, Unit.INSTANCE);
        player.level().playSound(player, player.blockPosition(), ModSounds.MATE_POUR.value(), SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    private static InteractionHand other(InteractionHand hand) {
        return hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
    }

    private static void hint(Player player, String key) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendSystemMessage(Component.translatable(key), true);
        }
    }

    /** A sip through the straw every few ticks while drinking. */
    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack mate, int ticksLeft) {
        if (!level.isClientSide() && ticksLeft % SIP_EVERY == 0 && ticksLeft > SIP_EVERY) {
            level.playSound(null, entity.blockPosition(), ModSounds.MATE_SIP.value(), SoundSource.PLAYERS,
                    1.0f, 0.9f + level.getRandom().nextFloat() * 0.2f);
        }
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.DRINK;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return DRINK_TICKS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        int yerba = yerba(stack);
        if (yerba == 0) {
            tooltip.accept(Component.translatable("item.regenerative_agriculture.mate.empty").withStyle(ChatFormatting.DARK_GRAY));
            return;
        }
        tooltip.accept(Component.translatable("item.regenerative_agriculture.mate.yerba", yerba, SERVINGS_PER_LOAD).withStyle(ChatFormatting.GREEN));
        if (served(stack)) {
            tooltip.accept(Component.translatable("item.regenerative_agriculture.mate.served").withStyle(ChatFormatting.AQUA));
        }
    }
}
