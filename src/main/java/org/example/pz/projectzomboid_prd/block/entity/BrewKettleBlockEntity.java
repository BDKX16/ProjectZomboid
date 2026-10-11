package org.example.pz.projectzomboid_prd.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
 * Brew kettle: the wort from the mash tun boils with {@value BrewingRules#HOPS_PER_BATCH} dried hops over a fire
 * (campfire, fire, lava, magma or a lit furnace right below), then flows into the fermenter next to it.
 */
public class BrewKettleBlockEntity extends BreweryBlockEntity {
    private boolean wort;
    private int hops;
    private int boilTicks;
    private boolean boiling;
    private boolean boiled;

    public BrewKettleBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BREW_KETTLE, pos, state);
    }

    /** Takes a batch of wort from the mash tun if the kettle is free. */
    public boolean acceptWort() {
        if (wort || boiled) {
            return false;
        }
        wort = true;
        sound(SoundEvents.BUCKET_EMPTY);
        changed();
        return true;
    }

    public boolean boiled() {
        return boiled;
    }

    @Override
    public InteractionResult use(ItemStack stack, Player player) {
        if (!stack.is(ModItems.DRIED_HOPS) || hops >= BrewingRules.HOPS_PER_BATCH || boiled) {
            return InteractionResult.PASS;
        }
        int added = Math.min(stack.getCount(), BrewingRules.HOPS_PER_BATCH - hops);
        hops += added;
        consume(stack, player, added);
        sound(SoundEvents.COMPOSTER_FILL);
        changed();
        return InteractionResult.SUCCESS;
    }

    @Override
    public void serverTick(ServerLevel level) {
        boolean wasBoiling = boiling;
        boiling = wort && hops == BrewingRules.HOPS_PER_BATCH && Brewery.heated(level, worldPosition)
                && Brewery.complete(level, worldPosition);
        if (boiling) {
            boilTicks += BreweryBlock.TICK_INTERVAL;
            if (boilTicks >= BrewingRules.BOIL_TICKS) {
                boiled = true;
                boiling = false;
                wort = false;
                hops = 0;
                boilTicks = 0;
            }
            changed();
        } else if (wasBoiling) {
            changed();
        }
        if (boiled) {
            Brewery.neighbour(level, worldPosition, FermenterBlockEntity.class)
                    .filter(FermenterBlockEntity::acceptWort)
                    .ifPresent(fermenter -> {
                        boiled = false;
                        changed();
                    });
        }
    }

    @Override
    protected BrewStage stage() {
        if (boiled) {
            return BrewStage.DONE;
        }
        if (boiling) {
            return BrewStage.WORKING;
        }
        return wort || hops > 0 ? BrewStage.FILLED : BrewStage.EMPTY;
    }

    @Override
    public Component status() {
        if (level != null && !Brewery.complete(level, worldPosition)) {
            return Component.translatable("brewery.projectzomboid_prd.incomplete");
        }
        if (boiled) {
            return Component.translatable("block.projectzomboid_prd.brew_kettle.boiled");
        }
        boolean fire = level != null && Brewery.heated(level, worldPosition);
        return Component.translatable("block.projectzomboid_prd.brew_kettle.status",
                yesNo(wort), hops, BrewingRules.HOPS_PER_BATCH, yesNo(fire), boilTicks * 100 / BrewingRules.BOIL_TICKS);
    }

    private static Component yesNo(boolean value) {
        return Component.translatable(value ? "brewery.projectzomboid_prd.yes" : "brewery.projectzomboid_prd.no");
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        wort = input.getBooleanOr("wort", false);
        hops = input.getIntOr("hops", 0);
        boilTicks = input.getIntOr("boil_ticks", 0);
        boiled = input.getBooleanOr("boiled", false);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean("wort", wort);
        output.putInt("hops", hops);
        output.putInt("boil_ticks", boilTicks);
        output.putBoolean("boiled", boiled);
    }
}
