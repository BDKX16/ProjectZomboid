package io.github.bdkx16.regenerativeagriculture.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import io.github.bdkx16.regenerativeagriculture.block.entity.MashTunBlockEntity;
import io.github.bdkx16.regenerativeagriculture.brewing.BrewStage;

/** Mash tun of the microbrewery: water bucket + malt, gives wort to the kettle beside it. */
public class MashTunBlock extends BreweryBlock {

    public MashTunBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MashTunBlockEntity(pos, state);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(STAGE) == BrewStage.WORKING && random.nextInt(3) == 0) {
            level.addParticle(ParticleTypes.WHITE_SMOKE, pos.getX() + 0.3 + random.nextDouble() * 0.4, pos.getY() + 1.0, pos.getZ() + 0.3 + random.nextDouble() * 0.4,
                    0.0, 0.02, 0.0);
        }
    }
}
