package com.imoonday.ji_yue_boss.mixin;

import com.imoonday.ji_yue_boss.init.ModSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.Util;
import com.imoonday.ji_yue_boss.item.ThirstforBloodItemHandler;
import com.imoonday.ji_yue_boss.util.Utils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.sonmok14.fromtheshadows.server.items.ThirstforBloodItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ThirstforBloodItem.class)
public class ThirstforBloodItemMixin {

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void onUse(Level world, Player user, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        ThirstforBloodItemHandler.onUse((ThirstforBloodItem) (Object) this, world, user, hand, cir);
    }

    @ModifyArg(method = "use", at = @At(value = "INVOKE", target = "Lnet/sonmok14/fromtheshadows/server/entity/projectiles/PlayerBreathEntity;<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;DDDFFI)V"), index = 4)
    private double modifyY(double y) {
        return y + 0.8;
    }

    @ModifyArg(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemCooldowns;addCooldown(Lnet/minecraft/world/item/Item;I)V"), index = 1)
    private int modifyCooldown(int cooldown) {
        return 600; // 30秒 = 600 ticks
    }

    @Inject(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V"))
    private void onShoot(Level world, Player user, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        Utils.playRandomSound(ModSounds.TIAN_YAN_SOUNDS, world, user);
    }

}

