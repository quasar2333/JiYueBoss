package com.imoonday.ji_yue_boss.mixin;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to remove jump boost effect from Breezebreaker weapon from Celestisynth mod
 */
@Pseudo
@Mixin(targets = {
    "com.aqutheseal.celestisynth.common.item.weapons.BreezebreakerItem"
}, remap = false)
public class BreezebreakerItemMixin {

    @Inject(method = "inventoryTick", at = @At("HEAD"), cancellable = false, remap = false)
    private void removeJumpBoostOnInventoryTick(ItemStack stack, net.minecraft.world.level.Level level, net.minecraft.world.entity.Entity entity, int slotId, boolean isSelected, CallbackInfo ci) {
        if (entity instanceof Player player) {
            // Remove jump boost effect if it was applied by breezebreaker
            if (player.hasEffect(MobEffects.JUMP)) {
                ResourceLocation itemKey = ForgeRegistries.ITEMS.getKey(stack.getItem());
                if (itemKey != null && "celestisynth".equals(itemKey.getNamespace()) && 
                    itemKey.getPath().toLowerCase().contains("breezebreaker")) {
                    player.removeEffect(MobEffects.JUMP);
                }
            }
        }
    }
}

