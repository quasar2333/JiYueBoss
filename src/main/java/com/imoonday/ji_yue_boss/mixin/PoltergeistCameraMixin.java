package com.imoonday.ji_yue_boss.mixin;

import com.imoonday.ji_yue_boss.item.PoltergeistSkillHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin来强制第三人称视角（客户端）
 * 注意：由于客户端无法直接知道服务器端的变身状态，这个功能需要额外的同步包
 */
@OnlyIn(Dist.CLIENT)
@Mixin(Minecraft.class)
public class PoltergeistCameraMixin {

    @Shadow
    public LocalPlayer player;

    @Shadow
    public Options options;

    // 存储原始视角
    private static int savedPerspective = -1;

    /**
     * 注意：这个Mixin在客户端运行，但PoltergeistSkillHandler.isTransforming需要ServerPlayer
     * 为了简化，我们在这里不强制第三人称，而是让玩家自行切换
     * 如果需要强制切换，需要创建一个同步包从服务器发送到客户端
     */
}


