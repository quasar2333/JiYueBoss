package com.imoonday.ji_yue_boss.client.renderer;

import com.imoonday.ji_yue_boss.JiYueBoss;
import com.imoonday.ji_yue_boss.entity.WuMingTianShen;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class WuMingTianShenRenderer extends GeoEntityRenderer<WuMingTianShen> {

    public WuMingTianShenRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new DefaultedEntityGeoModel<>(new ResourceLocation(JiYueBoss.MODID, "wu_ming_tian_shen")));
    }

    @Override
    public ResourceLocation getTextureLocation(WuMingTianShen animatable) {
        return new ResourceLocation(JiYueBoss.MODID, "textures/entity/wu_ming_tian_shen.png");
    }
}
