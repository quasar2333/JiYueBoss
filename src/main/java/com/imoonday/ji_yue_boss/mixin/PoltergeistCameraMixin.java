package com.imoonday.ji_yue_boss.mixin;

import com.imoonday.ji_yue_boss.network.PoltergeistTransformationSyncS2CPacket;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin来调整变身时的第三人称相机距离
 */
@OnlyIn(Dist.CLIENT)
@Mixin(Camera.class)
public class PoltergeistCameraMixin {

    /**
     * 增加变身时的相机距离
     */
    @Inject(method = "getMaxZoom", at = @At("RETURN"), cancellable = true)
    private void increaseMaxZoomForTransformation(double defaultDistance, CallbackInfoReturnable<Double> cir) {
        Camera camera = (Camera) (Object) this;
        Entity entity = camera.getEntity();
        
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && entity != null && entity.equals(mc.player)) {
            if (PoltergeistTransformationSyncS2CPacket.isPlayerTransforming(mc.player.getUUID())) {
                // 变身时相机距离设置为8格
                cir.setReturnValue(8.0);
            }
        }
    }
}

