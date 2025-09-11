package com.imoonday.ji_yue_boss.client.renderer;

import com.imoonday.ji_yue_boss.JiYueBoss;
import com.imoonday.ji_yue_boss.entity.NineTailedFox;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class NineTailedFoxRenderer extends GeoEntityRenderer<NineTailedFox> {

    public NineTailedFoxRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new DefaultedEntityGeoModel<>(new ResourceLocation(JiYueBoss.MODID, "nine_tailed_fox")));
        this.withScale(0.8f); // 缩小到80%
    }

    @Override
    public ResourceLocation getTextureLocation(NineTailedFox animatable) {
        return new ResourceLocation(JiYueBoss.MODID, "textures/entity/nine_tailed_fox.png");
    }
}
