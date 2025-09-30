package com.imoonday.ji_yue_boss.mixin;

import com.imoonday.ji_yue_boss.init.ModSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.ChatFormatting;
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
        // SHIFT + 右键：走自定义处理（传统冷却，互不干扰）
        if (user.isShiftKeyDown()) {
            ThirstforBloodItemHandler.onUse((ThirstforBloodItem) (Object) this, world, user, hand, cir);
            return;
        }

        // 右键激光：独立32秒冷却（基于物品NBT），冷却中提示剩余秒数
        if (!world.isClientSide && world instanceof ServerLevel) {
            ServerLevel server = (ServerLevel) world;
            ItemStack stack = user.getItemInHand(hand);
            long now = server.getGameTime();
            long end = stack.getOrCreateTag().getLong("TFB_LASER_CD_END");
            if (end > now) {
                long sec = Math.max(1, (end - now + 19) / 20);
                user.displayClientMessage(Component.literal("天眼冷却中，剩余" + sec + "秒").withStyle(ChatFormatting.YELLOW), false);
                cir.setReturnValue(InteractionResultHolder.fail(stack));
                return;
            }
            stack.getOrCreateTag().putLong("TFB_LASER_CD_END", now + 20L * 32);
        }
    }

    @ModifyArg(method = "use", at = @At(value = "INVOKE", target = "Lnet/sonmok14/fromtheshadows/server/entity/projectiles/PlayerBreathEntity;<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;DDDFFI)V"), index = 4)
    private double modifyY(double y) {
        return y + 0.8;
    }

    @ModifyArg(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemCooldowns;addCooldown(Lnet/minecraft/world/item/Item;I)V"), index = 1)
    private int modifyCooldown(int cooldown) {
        // 取消原物品的共享冷却，由我们分别用 NBT 维护激光CD；SHIFT 技能在自定义处理内走传统CD
        return 0;
    }

    // Note: No redirection of isOnCooldown; cooldown separation is handled via NBT gates in this mixin and handler.

    @Inject(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V"))
    private void onShoot(Level world, Player user, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        Utils.playRandomSound(ModSounds.TIAN_YAN_SOUNDS, world, user);
    }

}

