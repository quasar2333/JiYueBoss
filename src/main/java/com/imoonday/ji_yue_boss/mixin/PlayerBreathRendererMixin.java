package com.imoonday.ji_yue_boss.mixin;

import net.sonmok14.fromtheshadows.client.renderer.PlayerBreathRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(PlayerBreathRenderer.class)
public class PlayerBreathRendererMixin {

    @ModifyConstant(method = {
            "renderFlatQuad",
            "drawBeam"
    }, constant = {
            @Constant(floatValue = -0.75F),
            @Constant(floatValue = 0.75F)
    }, remap = false)
    private float modifyRadius(float original) {
        return original / 8;
    }
}
