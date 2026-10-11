package org.example.pz.projectzomboid_prd.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.example.pz.projectzomboid_prd.block.BreweryBlock;
import org.example.pz.projectzomboid_prd.brewing.BrewStage;
import org.example.pz.projectzomboid_prd.brewing.Brewery;
import org.example.pz.projectzomboid_prd.brewing.BrewingRules;
import org.example.pz.projectzomboid_prd.registry.ModBlockEntities;
import org.example.pz.projectzomboid_prd.registry.ModItems;

/**
 * Mash tun: hot water and {@value BrewingRules#MALT_PER_BATCH} malt rest for a while and become wort, which flows
 * into the kettle next to it. The spent grain is left over for compost.
 */
public class MashTunBlockEntity extends BreweryBlockEntity {
    private static final int SPENT_GRAIN = 2;
    private boolean water;
    private int malt;
    private int mashTicks;
    private boolean wort;

    public MashTunBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MASH_TUN, pos, state);
    }

    public boolean hasWort() {
        return wort;
    }

    @Override
    public InteractionResult use(ItemStack stack, Player player) {
        if (stack.is(Items.WATER_BUCKET) && !water && !wort) {
            water = true;
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
                ModItems.give(player, new ItemStack(Items.BUCKET));
            }
            sound(SoundEvents.BUCKET_EMPTY);
        } else if (stack.is(ModItems.MALT) && malt < BrewingRules.MALT_PER_BATCH && !wort) {
            int added = Math.min(stack.getCount(), BrewingRules.MALT_PER_BATCH - malt);
            malt += added;
            consume(stack, player, added);
            sound(SoundEvents.COMPOSTER_FILL);
        } else {
            return InteractionResult.PASS;
        }
        changed();
        return InteractionResult.SUCCESS;
    }

    @Override
    public void serverTick(ServerLevel level) {
        if (!wort && water && malt == BrewingRules.MALT_PER_BATCH && Brewery.complete(level, worldPosition)) {
            mashTicks += BreweryBlock.TICK_INTERVAL;
            if (mashTicks >= BrewingRules.MASH_TICKS) {
                finishMash(level);
            }
            changed();
        }
        if (wort) {
            Brewery.neighbour(level, worldPosition, BrewKettleBlockEntity.class)
                    .filter(kettle -> Brewery.complete(level, worldPosition) && kettle.acceptWort())
                    .ifPresent(kettle -> {
                        wort = false;
                        changed();
                    });
        }
    }

    /** Wort is ready; the spent grain pops out. */
    private void finishMash(ServerLevel level) {
        wort = true;
        water = false;
        malt = 0;
        mashTicks = 0;
        Block.popResource(level, worldPosition.above(), new ItemStack(ModItems.SPENT_GRAIN, SPENT_GRAIN));
    }

    @Override
    protected BrewStage stage() {
        if (wort) {
            return BrewStage.DONE;
        }
        if (water && malt == BrewingRules.MALT_PER_BATCH) {
            return BrewStage.WORKING;
        }
        return water || malt > 0 ? BrewStage.FILLED : BrewStage.EMPTY;
    }

    @Override
    public Component status() {
        if (level != null && !Brewery.complete(level, worldPosition)) {
            return Component.translatable("brewery.projectzomboid_prd.incomplete");
        }
        if (wort) {
            return Component.translatable("block.projectzomboid_prd.mash_tun.wort");
        }
        return Component.translatable("block.projectzomboid_prd.mash_tun.status",
                Component.translatable(water ? "brewery.projectzomboid_prd.yes" : "brewery.projectzomboid_prd.no"),
                malt, BrewingRules.MALT_PER_BATCH, mashTicks * 100 / BrewingRules.MASH_TICKS);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        water = input.getBooleanOr("water", false);
        malt = input.getIntOr("malt", 0);
        mashTicks = input.getIntOr("mash_ticks", 0);
        wort = input.getBooleanOr("wort", false);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean("water", water);
        output.putInt("malt", malt);
        output.putInt("mash_ticks", mashTicks);
        output.putBoolean("wort", wort);
    }
}
