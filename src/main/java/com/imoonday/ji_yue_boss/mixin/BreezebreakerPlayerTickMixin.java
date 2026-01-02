package com.imoonday.ji_yue_boss.mixin;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 最终兜底：在 Player#tick 的尾声将手持 breezebreaker 的玩家上升速度夹到原版值，
 * 确保无论 Celestisynth 在本tick何时注入上升速度，最终结果都与原版一致。
 */
@Mixin(Player.class)
public class BreezebreakerPlayerTickMixin {

    private static final double VANILLA_MAX_ASCENT = 0.42D;

    @Inject(method = "tick", at = @At("TAIL"))
    private void jiYueBoss$clampBreezebreakerJump(CallbackInfo ci) {
        Player player = (Player) (Object) this;
        if (player.onGround()) return;
        if (!isHoldingBreezebreaker(player)) return;

        var motion = player.getDeltaMovement();
        if (motion.y > VANILLA_MAX_ASCENT) {
            player.setDeltaMovement(motion.x, VANILLA_MAX_ASCENT, motion.z);
            player.hurtMarked = true;
        }
    }

    private static boolean isHoldingBreezebreaker(Player player) {
        return isBreezebreaker(player.getMainHandItem()) || isBreezebreaker(player.getOffhandItem());
    }

    private static boolean isBreezebreaker(ItemStack stack) {
        if (stack.isEmpty()) return false;
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return key != null && "celestisynth".equals(key.getNamespace()) && key.getPath().toLowerCase().contains("breezebreaker");
    }
}


