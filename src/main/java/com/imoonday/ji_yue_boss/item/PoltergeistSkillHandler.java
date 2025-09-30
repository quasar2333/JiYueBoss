package com.imoonday.ji_yue_boss.item;

import com.imoonday.ji_yue_boss.entity.PoltergeistTransformation;
import com.imoonday.ji_yue_boss.init.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Poltergeist技能处理器
 */
public class PoltergeistSkillHandler {

    private static final Map<UUID, SkillData> ACTIVE_SKILLS = new HashMap<>();
    private static final Map<UUID, Long> COOLDOWNS = new HashMap<>();
    private static final int SKILL_DURATION = 100; // 5秒
    private static final int COOLDOWN_DURATION = 640; // 32秒
    private static final int ATTRACTION_RANGE = 17; // 边长17格
    private static final float DAMAGE_PER_SECOND = 2.0f;

    /**
     * 激活技能
     */
    public static void activateSkill(ServerPlayer player) {
        ServerLevel level = (ServerLevel) player.level();
        long now = level.getGameTime();
        
        // 检查是否手持Poltergeist
        if (!isHoldingPoltergeist(player)) {
            return;
        }

        // 检查冷却
        Long cooldownEnd = COOLDOWNS.get(player.getUUID());
        if (cooldownEnd != null && now < cooldownEnd) {
            long remaining = (cooldownEnd - now + 19) / 20; // 向上取整到秒
            player.displayClientMessage(
                Component.literal("魇渊现真冷却中，剩余" + remaining + "秒")
                    .withStyle(style -> style.withColor(0x5611d6)),
                true
            );
            return;
        }

        // 检查是否已经在变身中
        if (ACTIVE_SKILLS.containsKey(player.getUUID())) {
            return;
        }

        // 生成变身实体
        PoltergeistTransformation transformation = new PoltergeistTransformation(level, player);
        transformation.setPos(player.getX(), player.getY(), player.getZ());
        level.addFreshEntity(transformation);

        // 播放音效（服务端广播 + 直接给施法者播放）
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
            ModSounds.POLTERGEIST_TRANSFORMATION.get(), SoundSource.MASTER, 2.0f, 1.0f);
        try {
            org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger("JiYueBoss");
            logger.info("[Poltergeist] Server playSound at {},{},{}", player.getX(), player.getY(), player.getZ());
        } catch (Throwable ignored) {}
        player.playNotifySound(ModSounds.POLTERGEIST_TRANSFORMATION.get(), SoundSource.MASTER, 2.0f, 1.0f);
        // 额外对照：播放原版升级音效，排除整体静音链路
        player.playNotifySound(net.minecraft.sounds.SoundEvents.PLAYER_LEVELUP, SoundSource.MASTER, 1.0f, 1.0f);
        try {
            org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger("JiYueBoss");
            logger.info("[Poltergeist] Server playNotifySound to {}", player.getGameProfile().getName());
        } catch (Throwable ignored) {}

        // 记录技能数据（锚点固定当前位置）
        ACTIVE_SKILLS.put(player.getUUID(), new SkillData(
            now + SKILL_DURATION,
            transformation,
            player.getX(), player.getY(), player.getZ()
        ));
        
        // 设置冷却
        COOLDOWNS.put(player.getUUID(), now + COOLDOWN_DURATION);
        
        // 同步到客户端（隐藏玩家模型和切换视角）
        com.imoonday.ji_yue_boss.network.Network.sendToClient(
            new com.imoonday.ji_yue_boss.network.PoltergeistTransformationSyncS2CPacket(player.getUUID(), true),
            player
        );
    }

    /**
     * Tick处理 - 每tick调用
     */
    public static void tick(ServerPlayer player) {
        ServerLevel level = (ServerLevel) player.level();
        long now = level.getGameTime();
        UUID playerId = player.getUUID();

        SkillData skillData = ACTIVE_SKILLS.get(playerId);
        if (skillData == null) {
            return;
        }

        // 检查是否结束
        if (now >= skillData.endTime) {
            endSkill(player);
            return;
        }

        // 完全禁止玩家移动：速度清零 + 位置拉回锚点，避免缓慢滑动
        player.setDeltaMovement(0, 0, 0);
        if (skillData != null) {
            player.teleportTo(skillData.anchorX, skillData.anchorY, skillData.anchorZ);
        }
        player.hurtMarked = true;
        
        // 设置无敌状态
        player.setInvulnerable(true);

        // 每tick生成粒子 (sonic_boom 3个)
        level.sendParticles(ParticleTypes.SONIC_BOOM, 
            player.getX(), player.getY() + 0.1, player.getZ(), 
            3, 0.2, 0.05, 0.2, 0.0);

        // 每tick吸引生物（不是每秒，而是持续吸引）
        attractEntities(player, level);

        // 每秒造成伤害 (20 ticks = 1秒)
        if ((now - (skillData.endTime - SKILL_DURATION)) % 20 == 0) {
            damageEntities(player, level);
        }
    }

    /**
     * 吸引周围生物 - 每tick调用，持续吸引
     */
    private static void attractEntities(ServerPlayer player, ServerLevel level) {
        double range = ATTRACTION_RANGE / 2.0;
        AABB box = new AABB(
            player.getX() - range, player.getY() - range, player.getZ() - range,
            player.getX() + range, player.getY() + range, player.getZ() + range
        );

        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, box, 
            entity -> entity != player && !(entity instanceof PoltergeistTransformation));

        for (LivingEntity entity : entities) {
            // 计算向玩家的方向
            double dx = player.getX() - entity.getX();
            double dy = player.getY() - entity.getY();
            double dz = player.getZ() - entity.getZ();
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            
            if (distance > 1.0) {
                // 强力吸引 - 速度根据距离调整，越远速度越快
                double speed = Math.min(1.5, 0.5 + (distance / range) * 1.0);
                entity.setDeltaMovement(
                    entity.getDeltaMovement().x + dx / distance * speed * 0.15,
                    entity.getDeltaMovement().y + dy / distance * speed * 0.1,
                    entity.getDeltaMovement().z + dz / distance * speed * 0.15
                );
                entity.hurtMarked = true;
                
                // 取消实体的AI寻路，让吸引效果更明显（仅对Mob类型）
                if (entity instanceof net.minecraft.world.entity.Mob mob) {
                    mob.getNavigation().stop();
                }
            }
        }
    }

    /**
     * 伤害周围生物 - 每秒调用一次
     */
    private static void damageEntities(ServerPlayer player, ServerLevel level) {
        double range = ATTRACTION_RANGE / 2.0;
        AABB box = new AABB(
            player.getX() - range, player.getY() - range, player.getZ() - range,
            player.getX() + range, player.getY() + range, player.getZ() + range
        );

        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, box, 
            entity -> entity != player && !(entity instanceof PoltergeistTransformation));

        for (LivingEntity entity : entities) {
            entity.hurt(player.damageSources().playerAttack(player), DAMAGE_PER_SECOND);
        }
    }

    /**
     * 结束技能
     */
    private static void endSkill(ServerPlayer player) {
        SkillData skillData = ACTIVE_SKILLS.remove(player.getUUID());
        if (skillData != null && skillData.entity != null && !skillData.entity.isRemoved()) {
            skillData.entity.discard();
        }
        
        // 恢复玩家可被攻击状态
        player.setInvulnerable(false);
        
        // 同步到客户端（显示玩家模型和恢复视角）
        com.imoonday.ji_yue_boss.network.Network.sendToClient(
            new com.imoonday.ji_yue_boss.network.PoltergeistTransformationSyncS2CPacket(player.getUUID(), false),
            player
        );
    }

    /**
     * 检查是否手持Poltergeist
     */
    private static boolean isHoldingPoltergeist(ServerPlayer player) {
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();
        return isPoltergeist(mainHand) || isPoltergeist(offHand);
    }

    private static boolean isPoltergeist(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        ResourceLocation key = ForgeRegistries.ITEMS.getKey(stack.getItem());
        return key != null && "celestisynth".equals(key.getNamespace()) && 
               key.getPath().toLowerCase().contains("poltergeist");
    }

    /**
     * 检查玩家是否正在变身中
     */
    public static boolean isTransforming(ServerPlayer player) {
        return ACTIVE_SKILLS.containsKey(player.getUUID());
    }

    private static class SkillData {
        final long endTime;
        final PoltergeistTransformation entity;
        final double anchorX;
        final double anchorY;
        final double anchorZ;

        SkillData(long endTime, PoltergeistTransformation entity, double anchorX, double anchorY, double anchorZ) {
            this.endTime = endTime;
            this.entity = entity;
            this.anchorX = anchorX;
            this.anchorY = anchorY;
            this.anchorZ = anchorZ;
        }
    }
}
