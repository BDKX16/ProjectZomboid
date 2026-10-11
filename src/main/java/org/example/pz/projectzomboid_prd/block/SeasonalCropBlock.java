package org.example.pz.projectzomboid_prd.block;

import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.CropBlock;

/**
 * A crop from this mod. Growth rules (seasons, temperature, frost) come from its crop profile and are applied
 * to every {@link CropBlock} by {@code CropBlockMixin}; this class only links the block to its seed item.
 * Like vanilla crops it can only be planted on farmland.
 */
public class SeasonalCropBlock extends CropBlock {
    private ItemLike seeds = Items.AIR;

    public SeasonalCropBlock(Properties properties) {
        super(properties);
    }

    /** Called once during registration, after the seed item exists. */
    public void setSeeds(ItemLike seeds) {
        this.seeds = seeds;
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return seeds;
    }
}
