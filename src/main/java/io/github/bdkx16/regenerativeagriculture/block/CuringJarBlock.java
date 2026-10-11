package io.github.bdkx16.regenerativeagriculture.block;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import io.github.bdkx16.regenerativeagriculture.block.entity.CuringJarBlockEntity;
import io.github.bdkx16.regenerativeagriculture.processing.ProcessingRecipes;
import io.github.bdkx16.regenerativeagriculture.processing.Quality;

import java.util.Locale;

/**
 * Glass jar for curing dried tobacco, cannabis and yerba, and for fermenting wort into beer. Right click with the
 * dried harvest seals it; with an empty hand it shows the days and stars so far; sneaking takes the batch out
 * (cured with its stars if it reached the first one, otherwise unchanged).
 */
public class CuringJarBlock extends Block implements EntityBlock {
    public static final BooleanProperty FILLED = BooleanProperty.create("filled");
    private static final VoxelShape SHAPE = Block.box(4, 0, 4, 12, 12, 12);

    public CuringJarBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FILLED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FILLED);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CuringJarBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                          InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof CuringJarBlockEntity jar) || !jar.insert(stack)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0f, 0.8f);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof CuringJarBlockEntity jar) || jar.contents().isEmpty()) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        long now = level.getGameTime();
        if (player.isShiftKeyDown() || jar.recipe().isEmpty()) {
            DryingRackBlock.give(player, level, pos, jar.takeOut(now));
            level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0f, 1.0f);
        } else if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendSystemMessage(status(jar, now), true);
        }
        return InteractionResult.SUCCESS;
    }

    /** "Curing dried tobacco: day 2.5 · ★☆☆ · next star in 3.5 days (sneak + click to take it out)". */
    private static Component status(CuringJarBlockEntity jar, long now) {
        ProcessingRecipes.Curing recipe = jar.recipe().orElseThrow();
        double days = jar.daysCured(now);
        int quality = jar.quality(now);
        MutableComponent text = Component.translatable("block.regenerative_agriculture.curing_jar.status",
                jar.contents().getHoverName(), String.format(Locale.ROOT, "%.1f", days));
        text.append(" · ").append(Quality.stars(quality));
        if (quality < Quality.MAX) {
            double left = recipe.qualityDays()[quality] - days;
            text.append(Component.translatable("block.regenerative_agriculture.curing_jar.next",
                    String.format(Locale.ROOT, "%.1f", left)).withStyle(ChatFormatting.GRAY));
        }
        return text.append(Component.translatable("block.regenerative_agriculture.curing_jar.take").withStyle(ChatFormatting.DARK_GRAY));
    }
}
