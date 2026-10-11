package io.github.bdkx16.regenerativeagriculture.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import io.github.bdkx16.regenerativeagriculture.block.DryingRackBlock;
import io.github.bdkx16.regenerativeagriculture.block.entity.DryingRackBlockEntity;
import io.github.bdkx16.regenerativeagriculture.block.entity.ProcessingBlockEntity;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Shows what is inside a drying rack (bundles hanging from the bar, more of them the fuller it is) or a curing jar
 * (the batch seen through the glass), so the change from fresh to dried or cured is visible.
 */
public class ProcessingRenderer<T extends ProcessingBlockEntity> implements BlockEntityRenderer<T, ProcessingRenderer.State> {
    /** Bundles hanging from the rack's bar, along the rack. */
    private static final float[] RACK_SLOTS = {-0.27f, 0f, 0.27f};
    private final ItemModelResolver itemModelResolver;

    public static class State extends BlockEntityRenderState {
        final List<ItemStackRenderState> items = new ArrayList<>();
        boolean rack;
        Direction facing = Direction.NORTH;
    }

    public ProcessingRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(T entity, State state, float partialTick, Vec3 camera,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(entity, state, partialTick, camera, breakProgress);
        state.items.clear();
        state.rack = entity instanceof DryingRackBlockEntity;
        state.facing = entity.getBlockState().hasProperty(DryingRackBlock.FACING)
                ? entity.getBlockState().getValue(DryingRackBlock.FACING) : Direction.NORTH;
        ItemStack contents = entity.contents();
        if (contents.isEmpty()) {
            return;
        }
        int shown = state.rack ? Math.min(RACK_SLOTS.length, (contents.getCount() + 4) / 5) : 1;
        int seed = (int) entity.getBlockPos().asLong();
        for (int i = 0; i < shown; i++) {
            ItemStackRenderState item = new ItemStackRenderState();
            itemModelResolver.updateForTopItem(item, contents, ItemDisplayContext.FIXED, entity.getLevel(), null, seed + i);
            state.items.add(item);
        }
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        for (int i = 0; i < state.items.size(); i++) {
            ItemStackRenderState item = state.items.get(i);
            if (item.isEmpty()) {
                continue;
            }
            pose.pushPose();
            pose.translate(0.5f, 0f, 0.5f);
            pose.rotateDegrees(Axis.YP, -state.facing.toYRot());
            if (state.rack) {
                // Hanging upside down from the bar, slightly tilted so the bundles do not look identical
                pose.translate(RACK_SLOTS[i], 0.52f, 0f);
                pose.rotateDegrees(Axis.ZP, 180 + (i - 1) * 8);
                pose.scale(0.42f, 0.42f, 0.42f);
            } else {
                pose.translate(0f, 0.24f, 0f);
                pose.scale(0.34f, 0.34f, 0.34f);
            }
            item.submit(pose, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            pose.popPose();
        }
    }
}
