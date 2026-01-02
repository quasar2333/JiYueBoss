package com.imoonday.ji_yue_boss.client;

import com.imoonday.ji_yue_boss.JiYueBoss;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * 屏蔽 celestisynth 的 breezebreaker 提供的二段跳：
 * 逻辑：在客户端每tick检测，若玩家在空中且手持breezebreaker，则强制将跳跃键置为未按下，阻止二段跳触发。
 * 不影响地面正常起跳。
 */
@Mod.EventBusSubscriber(modid = JiYueBoss.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BreezebreakerJumpBlocker {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        if (!mc.player.onGround() && isHoldingBreezebreaker(mc.player.getMainHandItem(), mc.player.getOffhandItem())) {
            // 仅在空中屏蔽跳跃键，避免二段跳；不影响地面起跳
            mc.options.keyJump.setDown(false);
        }
    }

    private static boolean isHoldingBreezebreaker(ItemStack main, ItemStack off) {
        return isBreezebreaker(main) || isBreezebreaker(off);
    }

    private static boolean isBreezebreaker(ItemStack stack) {
        if (stack.isEmpty()) return false;
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return key != null && "celestisynth".equals(key.getNamespace()) && key.getPath().toLowerCase().contains("breezebreaker");
    }
}


