package com.imoonday.ji_yue_boss.mixin;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to modify celestisynth weapon cooldowns:
 * - zhu_tian_mo_ran: Change cooldown to 35 seconds (700 ticks)
 */
@Mixin(ItemCooldowns.class)
public class CelestisynthItemUseMixin {

    @Shadow
    public void addCooldown(Item item, int ticks) {}

    /**
     * Intercept addCooldown and modify for zhu_tian_mo_ran
     */
    @Inject(method = "addCooldown", at = @At("HEAD"), cancellable = true)
    private void modifyZhuTianMoRanCooldown(Item item, int ticks, CallbackInfo ci) {
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(item);
        
        // Check if it's zhu_tian_mo_ran from celestisynth
        if (key != null && "celestisynth".equals(key.getNamespace()) && "zhu_tian_mo_ran".equals(key.getPath())) {
            // Replace with 35 second cooldown (700 ticks)
            ci.cancel();
            this.addCooldown(item, 20 * 35);
        }
    }
}

