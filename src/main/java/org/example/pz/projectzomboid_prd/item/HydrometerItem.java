package org.example.pz.projectzomboid_prd.item;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import org.example.pz.projectzomboid_prd.block.entity.FermenterBlockEntity;

/**
 * Hydrometer (alcohol meter for liquids): right click on a fermenter to read the gravity, alcohol, day of
 * fermentation, temperature and the quality the beer is heading for.
 */
public class HydrometerItem extends DescribedItem {

    public HydrometerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel().getBlockEntity(context.getClickedPos()) instanceof FermenterBlockEntity fermenter)) {
            return InteractionResult.PASS;
        }
        if (context.getLevel() instanceof ServerLevel level && context.getPlayer() instanceof ServerPlayer player) {
            player.sendSystemMessage(fermenter.hydrometerReading(level), true);
            level.playSound(null, context.getClickedPos(), SoundEvents.BOTTLE_FILL, SoundSource.PLAYERS, 0.5f, 1.6f);
        }
        return InteractionResult.SUCCESS;
    }
}
