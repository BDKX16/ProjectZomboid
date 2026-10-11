package io.github.bdkx16.regenerativeagriculture.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import io.github.bdkx16.regenerativeagriculture.block.CuringJarBlock;
import io.github.bdkx16.regenerativeagriculture.processing.DryingRules;
import io.github.bdkx16.regenerativeagriculture.processing.ProcessingRecipes;
import io.github.bdkx16.regenerativeagriculture.processing.Quality;
import io.github.bdkx16.regenerativeagriculture.registry.ModBlockEntities;
import io.github.bdkx16.regenerativeagriculture.registry.ModComponents;

import java.util.Optional;

/** A sealed jar where dried harvest (or wort) cures; the longer it stays, the better the {@link Quality}. */
public class CuringJarBlockEntity extends ProcessingBlockEntity {
    /** Game time when the current batch was sealed. */
    private long sealedAt;

    public CuringJarBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CURING_JAR, pos, state);
    }

    public Optional<ProcessingRecipes.Curing> recipe() {
        return contents.isEmpty() ? Optional.empty() : ProcessingRecipes.curing(contents.getItem());
    }

    public double daysCured(long gameTime) {
        return Math.max(0, gameTime - sealedAt) / (double) DryingRules.DAY;
    }

    /** Stars the batch would have if taken out now; 0 if not cured yet. */
    public int quality(long gameTime) {
        return recipe().map(recipe -> Quality.afterDays(daysCured(gameTime), recipe.qualityDays())).orElse(0);
    }

    /** Takes the batch out: cured with its quality if it is ready, otherwise as it went in. */
    public ItemStack takeOut(long gameTime) {
        int quality = quality(gameTime);
        Optional<ProcessingRecipes.Curing> recipe = recipe();
        ItemStack taken = takeOut();
        if (quality == 0 || recipe.isEmpty()) {
            return taken;
        }
        ItemStack cured = new ItemStack(recipe.get().output(), taken.getCount());
        cured.set(ModComponents.QUALITY, quality);
        return cured;
    }

    @Override
    protected boolean accepts(ItemStack stack) {
        return ProcessingRecipes.curing(stack.getItem()).isPresent();
    }

    /** Topping up is allowed during the first hour; it does not restart the clock. */
    @Override
    protected boolean canTopUp() {
        return level != null && daysCured(level.getGameTime()) < 1.0 / 24;
    }

    @Override
    protected void startBatch() {
        sealedAt = level == null ? 0 : level.getGameTime();
    }

    /** For tests: pretends the batch was sealed {@code days} earlier. */
    public void ageBy(double days) {
        sealedAt -= (long) (days * DryingRules.DAY);
        setChanged();
    }

    @Override
    protected BlockState visualState(BlockState state) {
        return state.hasProperty(CuringJarBlock.FILLED) ? state.setValue(CuringJarBlock.FILLED, !contents.isEmpty()) : state;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        sealedAt = input.getLongOr("sealed_at", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putLong("sealed_at", sealedAt);
    }
}
