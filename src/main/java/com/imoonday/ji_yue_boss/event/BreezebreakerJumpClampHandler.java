package com.imoonday.ji_yue_boss.event;

import com.imoonday.ji_yue_boss.JiYueBoss;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * 将手持 celestisynth:breezebreaker 时的跳跃高度强制夹到原版高度，
 * 以移除其“超高跳/二段跳”的增益（不影响其它武器与状态）。
 */
@Mod.EventBusSubscriber(modid = JiYueBoss.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BreezebreakerJumpClampHandler {

    private static final double VANILLA_JUMP_Y = 0.42D;

    @SubscribeEvent
    public static void onLivingJump(LivingEvent.LivingJumpEvent event) {
        LivingEntity living = event.getEntity();
        if (!(living instanceof Player player)) return;
        // 同时在客户端/服务端都夹制，以覆盖任意注入来源

        if (isHoldingBreezebreaker(player)) {
            var motion = player.getDeltaMovement();
            if (motion.y > VANILLA_JUMP_Y) {
                player.setDeltaMovement(motion.x, VANILLA_JUMP_Y, motion.z);
            }
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


