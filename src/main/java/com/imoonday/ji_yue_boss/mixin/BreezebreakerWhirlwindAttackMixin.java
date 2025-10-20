package com.imoonday.ji_yue_boss.mixin;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to prevent Breezebreaker Whirlwind Attack from applying jump boost
 */
@Pseudo
@Mixin(targets = {
    "com.aqutheseal.celestisynth.common.attack.breezebreaker.BreezebreakerWhirlwindAttack",
    "com.aqutheseal.celestisynth.common.attack.breezebreaker.whirlwind.BreezebreakerWhirlwindAttack"
}, remap = false)
public class BreezebreakerWhirlwindAttackMixin {

    @Shadow(remap = false)
    private Player player;

    @Inject(method = "baseTickSkill", at = @At("RETURN"), cancellable = false, remap = false)
    private void removeJumpBoostAfterSkill(CallbackInfo ci) {
        if (player == null) return;
        // 禁止任何上升速度
        var motion = player.getDeltaMovement();
        if (motion.y > 0.42D) player.setDeltaMovement(motion.x, 0.42D, motion.z);
        player.hurtMarked = true;
        player.removeEffect(MobEffects.JUMP);
    }

    @Inject(method = "startUsing", at = @At("RETURN"), cancellable = false, remap = false, require = 0)
    private void preventJumpBoostOnStart(CallbackInfo ci) {
        if (player == null) return;
        player.removeEffect(MobEffects.JUMP);
    }
}

