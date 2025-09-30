package com.imoonday.ji_yue_boss.client.renderer;

import com.imoonday.ji_yue_boss.JiYueBoss;
import com.imoonday.ji_yue_boss.entity.PoltergeistTransformation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class PoltergeistTransformationRenderer extends GeoEntityRenderer<PoltergeistTransformation> {

    public PoltergeistTransformationRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new DefaultedEntityGeoModel<>(new ResourceLocation(JiYueBoss.MODID, "poltergeist_transformation")));
    }

    @Override
    public ResourceLocation getTextureLocation(PoltergeistTransformation animatable) {
        return new ResourceLocation(JiYueBoss.MODID, "textures/entity/poltergeist_transformation.png");
    }
}

