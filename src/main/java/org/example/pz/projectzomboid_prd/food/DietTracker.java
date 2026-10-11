package org.example.pz.projectzomboid_prd.food;

import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.example.pz.projectzomboid_prd.Projectzomboid_prd;
import org.example.pz.projectzomboid_prd.effect.ModEffects;
import org.example.pz.projectzomboid_prd.registry.ModFoods;

import java.util.List;

/** Keeps each player's {@link Diet} and grants "Nourished" for a varied one. */
public final class DietTracker {
    public static final AttachmentType<Diet> DIET = AttachmentRegistry.create(Projectzomboid_prd.id("diet"),
            builder -> builder.persistent(Diet.CODEC).initializer(() -> Diet.EMPTY));
    /** How long "Nourished" lasts: one Minecraft day. */
    public static final int NOURISHED_TICKS = 24000;

    private DietTracker() {
    }

    public static void init() {
        // Loads the class so the attachment is registered.
    }

    /** Called when a player finishes eating anything. */
    public static void onEat(ServerPlayer player, ItemStack food) {
        List<Item> vegetables = ModFoods.vegetablesIn(food.getItem());
        if (vegetables.isEmpty()) {
            return;
        }
        long now = player.level().getGameTime();
        Diet before = get(player);
        Diet after = before.eat(vegetables.stream().map(item -> BuiltInRegistries.ITEM.getKey(item).toString()).toList(), now);
        player.setAttached(DIET, after);
        if (after.nourished(now)) {
            if (!player.hasEffect(ModEffects.NOURISHED)) {
                player.sendSystemMessage(Component.translatable("diet.projectzomboid_prd.nourished"), true);
            }
            player.addEffect(new MobEffectInstance(ModEffects.NOURISHED, NOURISHED_TICKS));
        }
    }

    public static Diet get(ServerPlayer player) {
        return player.getAttachedOrCreate(DIET);
    }
}
