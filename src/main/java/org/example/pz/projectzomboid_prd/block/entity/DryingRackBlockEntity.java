package org.example.pz.projectzomboid_prd.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.example.pz.projectzomboid_prd.block.DryingRackBlock;
import org.example.pz.projectzomboid_prd.processing.DryingRules;
import org.example.pz.projectzomboid_prd.processing.ProcessingRecipes;
import org.example.pz.projectzomboid_prd.registry.ModBlockEntities;
import org.example.pz.projectzomboid_prd.registry.ModItems;

import java.util.Optional;

/** A bundle of harvest hanging to dry; see {@link DryingRules}. */
public class DryingRackBlockEntity extends ProcessingBlockEntity {
    private DryingRules.Batch batch = DryingRules.Batch.FRESH;

    public DryingRackBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DRYING_RACK, pos, state);
    }

    public DryingRules.Batch batch() {
        return batch;
    }

    /** The recipe of what is hanging, if it is still drying. */
    public Optional<ProcessingRecipes.Drying> recipe() {
        return contents.isEmpty() ? Optional.empty() : ProcessingRecipes.drying(contents.getItem());
    }

    /** Lets the batch dry for {@code ticks} at the given temperature (°C) and humidity (%). */
    public void update(double temperature, double humidity, boolean rain, int ticks) {
        Optional<ProcessingRecipes.Drying> recipe = recipe();
        if (recipe.isEmpty()) {
            return;
        }
        batch = batch.advance(recipe.get().days(), temperature, humidity, rain, ticks);
        if (batch.spoiled()) {
            contents = new ItemStack(ModItems.SPOILED_HARVEST, contents.getCount());
            batch = DryingRules.Batch.FRESH;
        } else if (batch.done()) {
            contents = new ItemStack(recipe.get().output(), contents.getCount());
            batch = DryingRules.Batch.FRESH;
        }
        changed();
    }

    @Override
    protected boolean accepts(ItemStack stack) {
        return ProcessingRecipes.drying(stack.getItem()).isPresent();
    }

    /** More of the same harvest can be hung while the batch has barely started. */
    @Override
    protected boolean canTopUp() {
        return recipe().isPresent() && batch.progress() < 0.05f;
    }

    @Override
    protected void startBatch() {
        batch = DryingRules.Batch.FRESH;
    }

    @Override
    protected BlockState visualState(BlockState state) {
        DryingRackBlock.Contents shown;
        if (contents.isEmpty()) {
            shown = DryingRackBlock.Contents.EMPTY;
        } else if (contents.is(ModItems.SPOILED_HARVEST)) {
            shown = DryingRackBlock.Contents.SPOILED;
        } else if (recipe().isPresent()) {
            shown = DryingRackBlock.Contents.FRESH;
        } else {
            shown = DryingRackBlock.Contents.DRY;
        }
        return state.hasProperty(DryingRackBlock.CONTENTS) ? state.setValue(DryingRackBlock.CONTENTS, shown) : state;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        batch = new DryingRules.Batch(input.getFloatOr("progress", 0), input.getIntOr("damp", 0));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putFloat("progress", batch.progress());
        output.putInt("damp", batch.dampTicks());
    }
}
