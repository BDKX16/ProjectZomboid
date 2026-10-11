package org.example.pz.projectzomboid_prd.soil;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.example.pz.projectzomboid_prd.Projectzomboid_prd;
import org.example.pz.projectzomboid_prd.crop.CropProfile;
import org.example.pz.projectzomboid_prd.crop.CropProfiles;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.UnaryOperator;

/**
 * Soil state of farmland blocks, stored per chunk as a persistent data attachment. Blocks never touched return
 * {@link SoilData#DEFAULT}.
 */
public final class SoilManager {
    private static final AttachmentType<Map<BlockPos, SoilData>> SOIL = AttachmentRegistry.create(
            Projectzomboid_prd.id("soil"),
            builder -> builder.persistent(Entry.CODEC.listOf().xmap(SoilManager::toMap, SoilManager::toList)));

    private SoilManager() {
    }

    public static void init() {
        PlayerBlockBreakEvents.AFTER.register((level, player, pos, state, blockEntity) -> {
            if (level instanceof ServerLevel serverLevel) {
                onCropBroken(serverLevel, pos, state);
            }
        });
    }

    /** @param pos the farmland block */
    public static SoilData get(Level level, BlockPos pos) {
        Map<BlockPos, SoilData> soil = level.getChunkAt(pos).getAttached(SOIL);
        return soil == null ? SoilData.DEFAULT : soil.getOrDefault(pos, SoilData.DEFAULT);
    }

    /** @param pos the farmland block */
    public static SoilData update(Level level, BlockPos pos, UnaryOperator<SoilData> change) {
        LevelChunk chunk = level.getChunkAt(pos);
        Map<BlockPos, SoilData> soil = new HashMap<>(chunk.getAttachedOrElse(SOIL, Map.of()));
        SoilData updated = change.apply(soil.getOrDefault(pos, SoilData.DEFAULT));
        soil.put(pos.immutable(), updated);
        chunk.setAttached(SOIL, Map.copyOf(soil));
        chunk.markUnsaved();
        return updated;
    }

    /** A ripe crop was harvested at {@code crop} (broken or cut with shears). */
    public static void onHarvest(Level level, BlockPos crop, CropProfile profile) {
        update(level, crop.below(), soil -> SoilRules.harvest(soil, profile.family(), profile.nitrogen()));
    }

    private static void onCropBroken(ServerLevel level, BlockPos pos, BlockState state) {
        if (state.getBlock() instanceof CropBlock crop && crop.isMaxAge(state)) {
            CropProfiles.get(level.registryAccess(), crop).ifPresent(profile -> onHarvest(level, pos, profile));
        }
    }

    /** Species, companions and antagonists among the crops within 2 blocks on the same level. */
    public static SoilRules.Neighbourhood neighbourhood(Level level, BlockPos pos, CropProfile profile) {
        Set<Block> species = new HashSet<>();
        species.add(profile.block());
        boolean companion = false;
        boolean antagonist = false;
        for (BlockPos other : BlockPos.betweenClosed(pos.offset(-2, 0, -2), pos.offset(2, 0, 2))) {
            Block block = level.getBlockState(other).getBlock();
            if (block instanceof CropBlock && !other.equals(pos)) {
                species.add(block);
                companion |= profile.isCompanion(block);
                antagonist |= profile.isAntagonist(block);
            }
        }
        return new SoilRules.Neighbourhood(species.size(), companion, antagonist);
    }

    private static Map<BlockPos, SoilData> toMap(List<Entry> entries) {
        Map<BlockPos, SoilData> map = new HashMap<>();
        for (Entry entry : entries) {
            map.put(entry.pos(), entry.soil());
        }
        return Map.copyOf(map);
    }

    private static List<Entry> toList(Map<BlockPos, SoilData> map) {
        return map.entrySet().stream().map(e -> new Entry(e.getKey(), e.getValue())).toList();
    }

    private record Entry(BlockPos pos, SoilData soil) {
        static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BlockPos.CODEC.fieldOf("pos").forGetter(Entry::pos),
                SoilData.CODEC.fieldOf("soil").forGetter(Entry::soil)
        ).apply(instance, Entry::new));
    }
}
