package io.github.bdkx16.regenerativeagriculture.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import io.github.bdkx16.regenerativeagriculture.processing.Quality;

import java.util.function.Consumer;

/** Item with a grey explanation line in its tooltip, from the {@code <item key>.desc} translation. */
public class DescribedItem extends Item {

    public DescribedItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable(getDescriptionId() + ".desc").withStyle(ChatFormatting.GRAY));
        int quality = Quality.of(stack);
        if (quality > 0) {
            tooltip.accept(Component.translatable("tooltip.regenerative_agriculture.quality").withStyle(ChatFormatting.GRAY)
                    .append(Quality.stars(quality)));
        }
    }
}
