package com.imoonday.ji_yue_boss.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin来隐藏变身时的玩家模型（客户端）
 */
@OnlyIn(Dist.CLIENT)
@Mixin(PlayerRenderer.class)
public class PoltergeistRenderMixin {

    /**
     * 隐藏正在变身的玩家模型
     */
    @Inject(method = "render(Lnet/minecraft/client/player/AbstractClientPlayer;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At("HEAD"), cancellable = true)
    private void hidePlayerDuringTransformation(AbstractClientPlayer player, float entityYaw, float partialTicks,
                                                 PoseStack poseStack, MultiBufferSource buffer, int packedLight,
                                                 CallbackInfo ci) {
        // 检查该玩家是否正在变身
        if (com.imoonday.ji_yue_boss.network.PoltergeistTransformationSyncS2CPacket.isPlayerTransforming(player.getUUID())) {
            // 取消渲染玩家模型
            ci.cancel();
        }
    }
}
