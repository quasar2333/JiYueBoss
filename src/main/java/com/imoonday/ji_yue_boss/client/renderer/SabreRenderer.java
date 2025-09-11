package com.imoonday.ji_yue_boss.client.renderer;

import com.imoonday.ji_yue_boss.entity.Sabre;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;

public class SabreRenderer extends EntityRenderer<Sabre> {

    private final ItemRenderer itemRenderer;
    public float scale = 2.5f;

    public SabreRenderer(EntityRendererProvider.Context pContext) {
        super(pContext);
        this.itemRenderer = pContext.getItemRenderer();
    }

    @Override
    protected int getBlockLightLevel(Sabre pEntity, BlockPos pPos) {
        return 15;
    }

    @Override
    public void render(Sabre pEntity, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
        pPoseStack.pushPose();
//        pPoseStack.translate(0.0f, pEntity.getBbHeight(), 0.0f);
        pPoseStack.scale(scale, scale, scale);
//        pPoseStack.mulPose(Axis.XP.rotationDegrees(180.0f));
        Vec3 vec3 = pEntity.getEyePosition();
        Vec3 camera = this.entityRenderDispatcher.camera.getPosition();
        pPoseStack.mulPose(Axis.YP.rotationDegrees(Mth.wrapDegrees((float) (Mth.atan2(camera.z - vec3.z, camera.x - vec3.x) * (double) (180F / (float) Math.PI)))));
        this.itemRenderer.renderStatic(pEntity.getItem(), ItemDisplayContext.GROUND, pPackedLight, OverlayTexture.NO_OVERLAY, pPoseStack, pBuffer, pEntity.level(), pEntity.getId());
        pPoseStack.popPose();
        super.render(pEntity, pEntityYaw, pPartialTicks, pPoseStack, pBuffer, pPackedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(Sabre pEntity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
