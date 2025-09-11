package com.imoonday.ji_yue_boss.client.renderer;

import com.imoonday.ji_yue_boss.JiYueBoss;
import com.imoonday.ji_yue_boss.entity.HowlingCelestialDog;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class HowlingCelestialDogRenderer extends GeoEntityRenderer<HowlingCelestialDog> {

    public HowlingCelestialDogRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new DefaultedEntityGeoModel<>(new ResourceLocation(JiYueBoss.MODID, "howling_celestial_dog")));
        this.withScale(0.8f);
    }
}
