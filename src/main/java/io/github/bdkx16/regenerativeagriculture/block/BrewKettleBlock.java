package io.github.bdkx16.regenerativeagriculture.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import io.github.bdkx16.regenerativeagriculture.block.entity.BrewKettleBlockEntity;
import io.github.bdkx16.regenerativeagriculture.brewing.BrewStage;
import io.github.bdkx16.regenerativeagriculture.registry.ModSounds;
import net.minecraft.sounds.SoundSource;

/** Copper brew kettle between the mash tun and the fermenter; boils the wort with hops over a fire. */
public class BrewKettleBlock extends BreweryBlock {

    public BrewKettleBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BrewKettleBlockEntity(pos, state);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(STAGE) == BrewStage.WORKING && random.nextInt(20) == 0) {
            level.playLocalSound(pos, ModSounds.KETTLE_BOIL.value(), SoundSource.BLOCKS, 1.0f, 0.9f + random.nextFloat() * 0.2f, false);
        }
        if (state.getValue(STAGE) == BrewStage.WORKING && random.nextInt(1) == 0) {
            level.addParticle(ParticleTypes.CLOUD, pos.getX() + 0.3 + random.nextDouble() * 0.4, pos.getY() + 1.05, pos.getZ() + 0.3 + random.nextDouble() * 0.4,
                    0.0, 0.05, 0.0);
        }
    }
}
