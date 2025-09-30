package com.imoonday.ji_yue_boss.event;

import com.imoonday.ji_yue_boss.JiYueBoss;
import com.imoonday.ji_yue_boss.item.PoltergeistSkillHandler;
import com.imoonday.ji_yue_boss.network.PoltergeistTransformationSyncS2CPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 事件处理器 - 完全禁止变身期间的所有玩家操作
 */
@Mod.EventBusSubscriber(modid = JiYueBoss.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PoltergeistTransformationEventHandler {

    /**
     * 禁止移动（服务器端）
     */
    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingTickEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            if (PoltergeistSkillHandler.isTransforming(player)) {
                // 完全禁止移动
                player.setDeltaMovement(0, player.getDeltaMovement().y * 0.1, 0);
                player.fallDistance = 0;
            }
        }
    }

    /**
     * 禁止攻击
     */
    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide && player instanceof ServerPlayer serverPlayer) {
            if (PoltergeistSkillHandler.isTransforming(serverPlayer)) {
                event.setCanceled(true);
            }
        }
    }

    /**
     * 禁止丢弃物品（通过Mixin已经实现，这里作为备份）
     */
    @SubscribeEvent
    public static void onItemToss(net.minecraftforge.event.entity.item.ItemTossEvent event) {
        Player player = event.getPlayer();
        if (!player.level().isClientSide && player instanceof ServerPlayer serverPlayer) {
            if (PoltergeistSkillHandler.isTransforming(serverPlayer)) {
                event.setCanceled(true);
            }
        }
    }

    /**
     * 禁止使用物品（右键）
     */
    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide && player instanceof ServerPlayer serverPlayer) {
            if (PoltergeistSkillHandler.isTransforming(serverPlayer)) {
                event.setCanceled(true);
                event.setResult(Event.Result.DENY);
            }
        }
    }

    /**
     * 禁止右键方块
     */
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide && player instanceof ServerPlayer serverPlayer) {
            if (PoltergeistSkillHandler.isTransforming(serverPlayer)) {
                event.setCanceled(true);
                event.setResult(Event.Result.DENY);
            }
        }
    }

    /**
     * 禁止左键（挖掘）
     */
    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide && player instanceof ServerPlayer serverPlayer) {
            if (PoltergeistSkillHandler.isTransforming(serverPlayer)) {
                event.setCanceled(true);
            }
        }
    }

    /**
     * 禁止打开容器/背包等
     */
    @SubscribeEvent
    public static void onContainerOpen(net.minecraftforge.event.entity.player.PlayerContainerEvent.Open event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide && player instanceof ServerPlayer serverPlayer) {
            if (PoltergeistSkillHandler.isTransforming(serverPlayer)) {
                event.setCanceled(true);
            }
        }
    }

    /**
     * 客户端：禁止玩家输入和操作
     */
    @OnlyIn(Dist.CLIENT)
    @Mod.EventBusSubscriber(modid = JiYueBoss.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ClientEvents {

        /**
         * 在客户端tick时完全重置所有输入
         */
        @SubscribeEvent
        public static void onClientPlayerTick(TickEvent.PlayerTickEvent event) {
            if (event.phase != TickEvent.Phase.START) {
                return;
            }
            
            if (event.side.isClient() && event.player.level().isClientSide) {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null && PoltergeistTransformationSyncS2CPacket.isPlayerTransforming(mc.player.getUUID())) {
                    // 重置所有移动输入
                    mc.player.input.leftImpulse = 0;
                    mc.player.input.forwardImpulse = 0;
                    mc.player.input.up = false;
                    mc.player.input.down = false;
                    mc.player.input.left = false;
                    mc.player.input.right = false;
                    mc.player.input.jumping = false;
                    mc.player.input.shiftKeyDown = false;
                    
                    // 重置速度（保留Y轴以允许重力）
                    mc.player.setDeltaMovement(0, mc.player.getDeltaMovement().y, 0);
                    
                    // 禁止跳跃
                    mc.player.setJumping(false);
                    
                    // 禁止疾跑
                    mc.player.setSprinting(false);
                }
            }
        }
        
        /**
         * 禁止鼠标按键（仅Pre事件可取消）
         */
        @SubscribeEvent
        public static void onMouseButton(InputEvent.MouseButton.Pre event) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && PoltergeistTransformationSyncS2CPacket.isPlayerTransforming(mc.player.getUUID())) {
                // 禁止所有鼠标按键（左键、右键等）- Pre事件可以取消
                event.setCanceled(true);
            }
        }

        /**
         * 禁止键盘丢弃键（Q）：使用KeyMapping直接置位为未按下
         */
        @SubscribeEvent
        public static void onClientTickForKeys(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.START) return;
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && PoltergeistTransformationSyncS2CPacket.isPlayerTransforming(mc.player.getUUID())) {
                // 主动清除主要移动与丢弃按键状态
                mc.options.keyDrop.setDown(false);
                mc.options.keyAttack.setDown(false);
                mc.options.keyUse.setDown(false);
                mc.options.keyUp.setDown(false);
                mc.options.keyDown.setDown(false);
                mc.options.keyLeft.setDown(false);
                mc.options.keyRight.setDown(false);
                mc.options.keyJump.setDown(false);
                mc.options.keyShift.setDown(false);
                mc.options.keySprint.setDown(false);
            }
        }
    }
}
