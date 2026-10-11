package org.example.pz.projectzomboid_prd.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.example.pz.projectzomboid_prd.block.entity.FermenterBlockEntity;
import org.example.pz.projectzomboid_prd.brewing.BrewStage;
import org.example.pz.projectzomboid_prd.registry.ModSounds;
import net.minecraft.sounds.SoundSource;

/** Conical fermenter with an airlock: wort + yeast become beer in seven days. */
public class FermenterBlock extends BreweryBlock {

    public FermenterBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FermenterBlockEntity(pos, state);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(STAGE) == BrewStage.WORKING && random.nextInt(30) == 0) {
            level.playLocalSound(pos, ModSounds.FERMENTER_BUBBLE.value(), SoundSource.BLOCKS, 1.0f, 0.9f + random.nextFloat() * 0.2f, false);
        }
        if (state.getValue(STAGE) == BrewStage.WORKING && random.nextInt(4) == 0) {
            level.addParticle(ParticleTypes.BUBBLE_POP, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5,
                    0.0, 0.02, 0.0);
        }
    }
}
