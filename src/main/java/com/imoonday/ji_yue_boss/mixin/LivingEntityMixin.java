package com.imoonday.ji_yue_boss.mixin;

import com.imoonday.ji_yue_boss.item.GourdItem;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Redirect(method = "triggerItemUseEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;playSound(Lnet/minecraft/sounds/SoundEvent;FF)V", ordinal = 0))
    private void playSound(LivingEntity instance, SoundEvent soundEvent, float volume, float pitch, ItemStack stack, int amount) {
        LivingEntity livingEntity = (LivingEntity) (Object) this;
        if (stack.getItem() instanceof GourdItem && !GourdItem.isValid(livingEntity, stack)) {
            return;
        }
        instance.playSound(soundEvent, volume, pitch);
    }
}
