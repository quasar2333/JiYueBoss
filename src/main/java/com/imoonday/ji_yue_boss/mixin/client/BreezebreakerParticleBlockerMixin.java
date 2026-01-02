package com.imoonday.ji_yue_boss.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * 在客户端阻止手持 breezebreaker 且处于空中时，围绕玩家近距离生成的粒子，
 * 以去掉跳跃相关的动画/粒子表现（不影响其它情况）。
 */
@Mixin(ClientLevel.class)
public class BreezebreakerParticleBlockerMixin {

    @Inject(method = "addParticle(Lnet/minecraft/core/particles/ParticleOptions;ZDDDDDD)V", at = @At("HEAD"), cancellable = true)
    private void jiYueBoss$blockNearbyParticlesFlag(ParticleOptions options, boolean alwaysVisible,
                                                    double x, double y, double z, double dx, double dy, double dz,
                                                    CallbackInfo ci) {
        if (shouldBlock(x, y, z)) ci.cancel();
    }

    @Inject(method = "addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V", at = @At("HEAD"), cancellable = true)
    private void jiYueBoss$blockNearbyParticles(ParticleOptions options,
                                                double x, double y, double z, double dx, double dy, double dz,
                                                CallbackInfo ci) {
        if (shouldBlock(x, y, z)) ci.cancel();
    }

    private static boolean shouldBlock(double x, double y, double z) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return false;
        if (mc.player.onGround()) return false;
        if (!isHoldingBreezebreaker(mc.player.getMainHandItem()) && !isHoldingBreezebreaker(mc.player.getOffhandItem())) return false;

        double dx = mc.player.getX() - x;
        double dy = mc.player.getY() - y;
        double dz = mc.player.getZ() - z;
        // 近距离(<= 3格)的粒子认为来源于自身跳跃表现，直接阻止
        return (dx * dx + dy * dy + dz * dz) <= 9.0;
    }

    private static boolean isHoldingBreezebreaker(ItemStack stack) {
        if (stack.isEmpty()) return false;
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return key != null && "celestisynth".equals(key.getNamespace()) && key.getPath().toLowerCase().contains("breezebreaker");
    }
}


