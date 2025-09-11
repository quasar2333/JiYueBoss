package com.imoonday.ji_yue_boss.mixin;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class KeresLifestealPreventionMixin {

    @Inject(method = "heal", at = @At("HEAD"), cancellable = true)
    private void preventKeresLifesteal(float healAmount, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;

        if (entity instanceof Player player) {
            ItemStack mainHand = player.getMainHandItem();
            ItemStack offHand = player.getOffhandItem();

            if (isCelestisynthKeres(mainHand.getItem()) || isCelestisynthKeres(offHand.getItem())) {
                StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
                for (StackTraceElement element : stackTrace) {
                    String className = element.getClassName();
                    if (className.contains("celestisynth") &&
                        (className.toLowerCase().contains("keres") || className.contains("Keres"))) {
                        ci.cancel();
                        return;
                    }
                    if (className.contains("KeresItem") || className.toLowerCase().contains("lifesteal")) {
                        ci.cancel();
                        return;
                    }
                }
            }
        }
    }

    private static boolean isCelestisynthKeres(Item item) {
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(item);
        if (key == null) {
            return false;
        }
        return "celestisynth".equals(key.getNamespace()) && key.getPath().toLowerCase().contains("keres");
    }
}
