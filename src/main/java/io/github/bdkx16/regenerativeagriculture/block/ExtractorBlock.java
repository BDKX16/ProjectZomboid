package io.github.bdkx16.regenerativeagriculture.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import io.github.bdkx16.regenerativeagriculture.greenhouse.ShelterDetector;

/**
 * Solar-powered air extractor. {@link #ACTIVE} (fan spinning) follows whether its vent shaft reaches the surface,
 * re-checked every {@value #CHECK_INTERVAL} ticks. Growth rules check the shaft directly; this state is visual.
 */
public class ExtractorBlock extends Block {
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    private static final int CHECK_INTERVAL = 40;

    public ExtractorBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(ACTIVE, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ACTIVE);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (!oldState.is(this)) {
            level.scheduleTick(pos, this, 1);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        boolean vented = ShelterDetector.hasVentShaft(level, pos);
        if (state.getValue(ACTIVE) != vented) {
            level.setBlock(pos, state.setValue(ACTIVE, vented), Block.UPDATE_CLIENTS);
        }
        level.scheduleTick(pos, this, CHECK_INTERVAL);
    }
}
