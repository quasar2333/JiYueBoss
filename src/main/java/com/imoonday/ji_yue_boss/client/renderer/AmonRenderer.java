package com.imoonday.ji_yue_boss.client.renderer;

import com.imoonday.ji_yue_boss.JiYueBoss;
import com.imoonday.ji_yue_boss.entity.BaseAmon;
import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;

public class AmonRenderer extends MobRenderer<BaseAmon, PlayerModel<BaseAmon>> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(JiYueBoss.MODID, "textures/entity/amon.png");

    public AmonRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new PlayerModel<>(pContext.bakeLayer(ModelLayers.PLAYER), false), 0.5f);
        this.addLayer(new HumanoidArmorLayer<>(this, new HumanoidArmorModel<>(pContext.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)), new HumanoidArmorModel<>(pContext.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)), pContext.getModelManager()));
        this.addLayer(new CustomHeadLayer<>(this, pContext.getModelSet(), pContext.getItemInHandRenderer()));
    }

    @Override
    public ResourceLocation getTextureLocation(BaseAmon pEntity) {
        return TEXTURE;
    }
}
