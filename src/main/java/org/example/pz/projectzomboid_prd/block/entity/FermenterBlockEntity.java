package org.example.pz.projectzomboid_prd.block.entity;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.example.pz.projectzomboid_prd.brewing.BrewStage;
import org.example.pz.projectzomboid_prd.brewing.BrewingRules;
import org.example.pz.projectzomboid_prd.greenhouse.MicroClimate;
import org.example.pz.projectzomboid_prd.processing.DryingRules;
import org.example.pz.projectzomboid_prd.processing.Quality;
import org.example.pz.projectzomboid_prd.registry.ModBlockEntities;
import org.example.pz.projectzomboid_prd.registry.ModComponents;
import org.example.pz.projectzomboid_prd.registry.ModFoods;
import org.example.pz.projectzomboid_prd.registry.ModItems;
import org.example.pz.projectzomboid_prd.season.SeasonManager;
import org.example.pz.projectzomboid_prd.season.SeasonText;

import java.util.Locale;

/**
 * Fermenter: the boiled wort from the kettle plus yeast ferments for seven days. The temperature where the
 * fermenter stands is sampled all along; staying in the ideal range gives the best beer. Read the progress with
 * a hydrometer; when it is done, fill glass bottles from it.
 */
public class FermenterBlockEntity extends BreweryBlockEntity {
    /** Take a temperature sample every this many server ticks of the block (5 × 20 game ticks). */
    private static final int SAMPLE_EVERY = 5;
    private boolean wort;
    private boolean pitched;
    private long startTime;
    private double offIdealSum;
    private int samples;
    private int tickCounter;
    private int bottles;
    private int quality;

    public FermenterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FERMENTER, pos, state);
    }

    /** Takes a batch of boiled wort from the kettle if the fermenter is free. */
    public boolean acceptWort() {
        if (wort || bottles > 0) {
            return false;
        }
        wort = true;
        pitched = false;
        offIdealSum = 0;
        samples = 0;
        sound(SoundEvents.BUCKET_EMPTY);
        changed();
        return true;
    }

    public boolean fermenting() {
        return pitched;
    }

    public int bottlesLeft() {
        return bottles;
    }

    public long elapsed(long gameTime) {
        return pitched ? Math.max(0, gameTime - startTime) : 0;
    }

    @Override
    public InteractionResult use(ItemStack stack, Player player) {
        if (stack.is(ModItems.YEAST) && wort && !pitched) {
            pitched = true;
            startTime = level == null ? 0 : level.getGameTime();
            consume(stack, player, 1);
            sound(SoundEvents.BREWING_STAND_BREW);
        } else if (stack.is(Items.GLASS_BOTTLE) && bottles > 0) {
            consume(stack, player, 1);
            ItemStack beer = new ItemStack(ModFoods.BEER.item());
            beer.set(ModComponents.QUALITY, quality);
            ModItems.give(player, beer);
            bottles--;
            sound(SoundEvents.BOTTLE_FILL);
        } else {
            return InteractionResult.PASS;
        }
        changed();
        return InteractionResult.SUCCESS;
    }

    @Override
    public void serverTick(ServerLevel level) {
        if (!pitched) {
            return;
        }
        if (++tickCounter >= SAMPLE_EVERY) {
            tickCounter = 0;
            sample(measure(level).temperature());
        }
        if (elapsed(level.getGameTime()) >= BrewingRules.FERMENT_TICKS) {
            finish();
        }
    }

    /** Records the temperature the yeast is working at. */
    public void sample(double temperature) {
        offIdealSum += BrewingRules.offIdeal(temperature);
        samples++;
        setChanged();
    }

    /** Average distance to the ideal temperature so far. */
    public double averageOffIdeal() {
        return samples == 0 ? 0 : offIdealSum / samples;
    }

    private void finish() {
        pitched = false;
        wort = false;
        bottles = BrewingRules.BOTTLES_PER_BATCH;
        quality = BrewingRules.quality(averageOffIdeal());
        changed();
    }

    /** For tests: pretends the yeast went in {@code ticks} earlier. */
    public void ageBy(long ticks) {
        startTime -= ticks;
        setChanged();
    }

    private MicroClimate measure(ServerLevel level) {
        return MicroClimate.measure(level, worldPosition, SeasonManager.climate(level.getServer()), SeasonManager.ticks(level.getServer()));
    }

    /** What the hydrometer shows: gravity, alcohol, day, temperature now and the quality it is heading for. */
    public Component hydrometerReading(ServerLevel level) {
        if (bottles > 0) {
            return Component.translatable("item.projectzomboid_prd.hydrometer.done",
                    String.format(Locale.ROOT, "%.3f", BrewingRules.FINAL_GRAVITY),
                    String.format(Locale.ROOT, "%.1f", BrewingRules.alcohol(BrewingRules.FERMENT_TICKS)))
                    .append(" ").append(Quality.stars(quality));
        }
        if (!pitched) {
            return Component.translatable(wort ? "item.projectzomboid_prd.hydrometer.no_yeast" : "item.projectzomboid_prd.hydrometer.empty");
        }
        long ticks = elapsed(level.getGameTime());
        double temperature = measure(level).temperature();
        MutableComponent text = Component.translatable("item.projectzomboid_prd.hydrometer.reading",
                String.format(Locale.ROOT, "%.3f", BrewingRules.gravity(ticks)),
                String.format(Locale.ROOT, "%.1f", BrewingRules.alcohol(ticks)),
                String.format(Locale.ROOT, "%.1f", ticks / (double) DryingRules.DAY),
                BrewingRules.FERMENT_TICKS / DryingRules.DAY,
                SeasonText.celsius(temperature));
        text.append(" ").append(Quality.stars(BrewingRules.quality(averageOffIdeal())));
        if (BrewingRules.offIdeal(temperature) > 0) {
            text.append(Component.translatable(temperature < BrewingRules.IDEAL_MIN
                    ? "item.projectzomboid_prd.hydrometer.cold" : "item.projectzomboid_prd.hydrometer.hot").withStyle(ChatFormatting.RED));
        }
        return text;
    }

    @Override
    protected BrewStage stage() {
        if (bottles > 0) {
            return BrewStage.DONE;
        }
        if (pitched) {
            return BrewStage.WORKING;
        }
        return wort ? BrewStage.FILLED : BrewStage.EMPTY;
    }

    @Override
    public Component status() {
        if (bottles > 0) {
            return Component.translatable("block.projectzomboid_prd.fermenter.ready", bottles).append(" ").append(Quality.stars(quality));
        }
        if (pitched && level != null) {
            return Component.translatable("block.projectzomboid_prd.fermenter.fermenting",
                    String.format(Locale.ROOT, "%.1f", elapsed(level.getGameTime()) / (double) DryingRules.DAY),
                    BrewingRules.FERMENT_TICKS / DryingRules.DAY);
        }
        return Component.translatable(wort ? "block.projectzomboid_prd.fermenter.needs_yeast" : "block.projectzomboid_prd.fermenter.empty");
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        wort = input.getBooleanOr("wort", false);
        pitched = input.getBooleanOr("pitched", false);
        startTime = input.getLongOr("start_time", 0);
        offIdealSum = input.getDoubleOr("off_ideal_sum", 0);
        samples = input.getIntOr("samples", 0);
        bottles = input.getIntOr("bottles", 0);
        quality = input.getIntOr("quality", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean("wort", wort);
        output.putBoolean("pitched", pitched);
        output.putLong("start_time", startTime);
        output.putDouble("off_ideal_sum", offIdealSum);
        output.putInt("samples", samples);
        output.putInt("bottles", bottles);
        output.putInt("quality", quality);
    }
}
