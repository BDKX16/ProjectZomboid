package org.example.pz.projectzomboid_prd.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.example.pz.projectzomboid_prd.block.entity.BrewKettleBlockEntity;
import org.example.pz.projectzomboid_prd.brewing.BrewStage;

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
        if (state.getValue(STAGE) == BrewStage.WORKING && random.nextInt(1) == 0) {
            level.addParticle(ParticleTypes.CLOUD, pos.getX() + 0.3 + random.nextDouble() * 0.4, pos.getY() + 1.05, pos.getZ() + 0.3 + random.nextDouble() * 0.4,
                    0.0, 0.05, 0.0);
        }
    }
}
