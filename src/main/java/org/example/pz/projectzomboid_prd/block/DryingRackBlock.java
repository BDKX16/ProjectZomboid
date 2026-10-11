package org.example.pz.projectzomboid_prd.block;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.example.pz.projectzomboid_prd.block.entity.DryingRackBlockEntity;
import org.example.pz.projectzomboid_prd.greenhouse.MicroClimate;
import org.example.pz.projectzomboid_prd.processing.DryingRules;
import org.example.pz.projectzomboid_prd.processing.ProcessingRecipes;
import org.example.pz.projectzomboid_prd.season.SeasonManager;
import org.example.pz.projectzomboid_prd.season.SeasonText;
import org.jspecify.annotations.Nullable;

import java.util.Locale;
import java.util.Optional;

/**
 * Wooden rack where a bundle of harvest hangs to dry. Every {@value #CHECK_INTERVAL} ticks it reads the temperature
 * and humidity where it stands: dry warm air is fastest, rain stops it and muggy air ends up spoiling the batch.
 * Right click with a harvest hangs it, with an empty hand shows how it goes (or takes it down when it is dry);
 * sneaking takes it down at any time.
 */
public class DryingRackBlock extends Block implements EntityBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<Contents> CONTENTS = EnumProperty.create("contents", Contents.class);
    public static final int CHECK_INTERVAL = 100;
    private static final VoxelShape SHAPE_NORTH_SOUTH = Block.box(0, 0, 5, 16, 16, 11);
    private static final VoxelShape SHAPE_EAST_WEST = Block.box(5, 0, 0, 11, 16, 16);

    public enum Contents implements StringRepresentable {
        EMPTY, FRESH, DRY, SPOILED;

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public DryingRackBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(CONTENTS, Contents.EMPTY));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, CONTENTS);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(FACING).getAxis() == Direction.Axis.Z ? SHAPE_NORTH_SOUTH : SHAPE_EAST_WEST;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DryingRackBlockEntity(pos, state);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (!oldState.is(this)) {
            level.scheduleTick(pos, this, CHECK_INTERVAL);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getBlockEntity(pos) instanceof DryingRackBlockEntity rack && rack.recipe().isPresent()) {
            MicroClimate air = measure(level, pos);
            rack.update(air.temperature(), air.humidity(), raining(level, pos), CHECK_INTERVAL);
        }
        level.scheduleTick(pos, this, CHECK_INTERVAL);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                          InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof DryingRackBlockEntity rack) || !rack.insert(stack)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        level.playSound(null, pos, SoundEvents.CROP_PLANTED, SoundSource.BLOCKS, 1.0f, 1.0f);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof DryingRackBlockEntity rack) || rack.contents().isEmpty()) {
            return InteractionResult.PASS;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }
        if (player.isShiftKeyDown() || rack.recipe().isEmpty()) {
            give(player, level, pos, rack.takeOut());
            level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.6f, 1.0f);
        } else if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendSystemMessage(status(serverLevel, pos, rack), true);
        }
        return InteractionResult.SUCCESS;
    }

    /** "Drying onion: 45 % · 18 °C · humidity 62 % · speed ×1.0" plus a warning if it rains or is too damp. */
    private static Component status(ServerLevel level, BlockPos pos, DryingRackBlockEntity rack) {
        MicroClimate air = measure(level, pos);
        boolean rain = raining(level, pos);
        float rate = DryingRules.rate(air.temperature(), air.humidity(), rain);
        Optional<ProcessingRecipes.Drying> recipe = rack.recipe();
        MutableComponent text = Component.translatable("block.projectzomboid_prd.drying_rack.status",
                rack.contents().getHoverName(), Math.round(rack.batch().progress() * 100),
                SeasonText.celsius(air.temperature()), Math.round(air.humidity()),
                String.format(Locale.ROOT, "%.1f", rate));
        if (rain) {
            text.append(Component.translatable("block.projectzomboid_prd.drying_rack.rain").withStyle(ChatFormatting.RED));
        } else if (DryingRules.damp(air.humidity(), false)) {
            text.append(Component.translatable("block.projectzomboid_prd.drying_rack.damp").withStyle(ChatFormatting.RED));
        } else if (recipe.isPresent() && rate > 0) {
            double hoursLeft = (1 - rack.batch().progress()) * recipe.get().days() * 24 / rate;
            text.append(Component.translatable("block.projectzomboid_prd.drying_rack.eta", Math.round(hoursLeft))
                    .withStyle(ChatFormatting.GRAY));
        }
        return text;
    }

    private static MicroClimate measure(ServerLevel level, BlockPos pos) {
        return MicroClimate.measure(level, pos, SeasonManager.climate(level.getServer()), SeasonManager.ticks(level.getServer()));
    }

    /** Rain falls on the rack when the sky is open right above it. */
    private static boolean raining(ServerLevel level, BlockPos pos) {
        return level.isRainingAt(pos.above());
    }

    /** Into the player's inventory, or popped out of the block if it is full. */
    static void give(Player player, Level level, BlockPos pos, ItemStack stack) {
        if (!player.getInventory().add(stack) && !stack.isEmpty()) {
            Block.popResource(level, pos, stack);
        }
    }
}
