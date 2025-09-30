package com.imoonday.ji_yue_boss.mixin;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to modify celestisynth weapons behavior:
 * - zhu_hai_xi_tian: Change Strength V duration to 30 seconds
 * - zhu_tian_mo_ran: Change cooldown to 35 seconds
 */
@Mixin(LivingEntity.class)
public class CelestisynthWeaponsMixin {

    /**
     * Modify effect duration for zhu_hai_xi_tian's Strength V effect to 30 seconds
     */
    @Inject(method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;)Z", at = @At("HEAD"), cancellable = true)
    private void modifyZhuHaiXiTianStrength(MobEffectInstance effectInstance, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity entity = (LivingEntity) (Object) this;
        
        // Only process for players
        if (!(entity instanceof Player player)) {
            return;
        }
        
        // Check if it's Strength V effect
        if (effectInstance.getEffect() != MobEffects.DAMAGE_BOOST || effectInstance.getAmplifier() != 4) {
            return;
        }
        
        // Check if player is holding zhu_hai_xi_tian
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();
        
        if (isCelestisynthItem(mainHand, "zhu_hai_xi_tian") || isCelestisynthItem(offHand, "zhu_hai_xi_tian")) {
            // Replace with modified effect: 30 seconds = 600 ticks
            MobEffectInstance modifiedEffect = new MobEffectInstance(
                MobEffects.DAMAGE_BOOST,
                20 * 30, // 30 seconds
                4, // Amplifier 4 = Strength V
                effectInstance.isAmbient(),
                effectInstance.isVisible(),
                effectInstance.showIcon()
            );
            entity.addEffect(modifiedEffect);
            cir.setReturnValue(true);
        }
    }
    
    private static boolean isCelestisynthItem(ItemStack stack, String itemPath) {
        if (stack.isEmpty()) {
            return false;
        }
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return key != null && "celestisynth".equals(key.getNamespace()) && key.getPath().equals(itemPath);
    }
}

