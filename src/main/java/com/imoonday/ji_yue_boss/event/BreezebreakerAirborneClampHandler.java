package com.imoonday.ji_yue_boss.event;

import com.imoonday.ji_yue_boss.JiYueBoss;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * 额外的飞行期夹制：在玩家处于空中且向上运动时，若手持 breezebreaker，则每tick强制
 * 将Y轴上升速度限制在原版范围内，防止该武器在跳跃后再次注入额外上升速度。
 */
@Mod.EventBusSubscriber(modid = JiYueBoss.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BreezebreakerAirborneClampHandler {

    private static final double VANILLA_MAX_ASCENT = 0.42D;

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        // 同时在客户端/服务端都夹制
        if (player.onGround()) return;
        if (!isHoldingBreezebreaker(player)) return;

        var motion = player.getDeltaMovement();
        if (motion.y > VANILLA_MAX_ASCENT) {
            player.setDeltaMovement(motion.x, VANILLA_MAX_ASCENT, motion.z);
        }
    }

    private static boolean isHoldingBreezebreaker(Player player) {
        return isBreezebreaker(player.getMainHandItem()) || isBreezebreaker(player.getOffhandItem());
    }

    private static boolean isBreezebreaker(ItemStack stack) {
        if (stack.isEmpty()) return false;
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return key != null && "celestisynth".equals(key.getNamespace()) && key.getPath().toLowerCase().contains("breezebreaker");
    }
}


