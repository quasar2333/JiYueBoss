package com.imoonday.ji_yue_boss.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * 服务器到客户端的包：同步玩家变身状态
 */
public class PoltergeistTransformationSyncS2CPacket implements NetworkPacket {

    private final UUID playerId;
    private final boolean isTransforming;

    // 客户端存储正在变身的玩家ID
    private static final Set<UUID> TRANSFORMING_PLAYERS = new HashSet<>();

    public PoltergeistTransformationSyncS2CPacket(UUID playerId, boolean isTransforming) {
        this.playerId = playerId;
        this.isTransforming = isTransforming;
    }

    public PoltergeistTransformationSyncS2CPacket(FriendlyByteBuf buf) {
        this.playerId = buf.readUUID();
        this.isTransforming = buf.readBoolean();
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(playerId);
        buf.writeBoolean(isTransforming);
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        context.enqueueWork(() -> {
            if (isTransforming) {
                TRANSFORMING_PLAYERS.add(playerId);
                
                // 如果是本地玩家，切换到第三人称
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null && mc.player.getUUID().equals(playerId)) {
                    // 保存当前视角设置和相机距离
                    ClientTransformationState.saveCurrentPerspective();
                    ClientTransformationState.saveCurrentCameraDistance();
                    // 强制切换到第三人称背后视角
                    mc.options.setCameraType(net.minecraft.client.CameraType.THIRD_PERSON_BACK);
                    // 设置更远的视角距离
                    ClientTransformationState.setCameraDistance(8.0);
                    // 客户端本地播放音效，确保必定能听到
                    try {
                        net.minecraft.sounds.SoundEvent se = com.imoonday.ji_yue_boss.init.ModSounds.POLTERGEIST_TRANSFORMATION.get();
                        // 诊断：确认客户端是否已成功加载该声音事件
                        try {
                            var found = mc.getSoundManager().getSoundEvent(se.getLocation());
                            org.slf4j.LoggerFactory.getLogger("JiYueBoss").info("[Poltergeist] Client sound event {} loaded? {}", se.getLocation(), found != null);
                        } catch (Throwable ignored0) {}
                        // 双重兜底：本地位置音效 + UI 音效
                        if (mc.level != null && mc.player != null) {
                            mc.level.playLocalSound(mc.player.getX(), mc.player.getY(), mc.player.getZ(), se,
                                net.minecraft.sounds.SoundSource.MASTER, 2.0f, 1.0f, false);
                            org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger("JiYueBoss");
                            logger.info("[Poltergeist] Client playLocalSound at {},{},{}", mc.player.getX(), mc.player.getY(), mc.player.getZ());
                        }
                        mc.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(se, 2.0f));
                        try {
                            org.slf4j.LoggerFactory.getLogger("JiYueBoss").info("[Poltergeist] Client UI sound played");
                        } catch (Throwable ignored2) {}
                        // 再播放一个原版UI声音做对照，排除设备静音
                        try {
                            mc.getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(net.minecraft.sounds.SoundEvents.NOTE_BLOCK_PLING.value(), 1.0f));
                            org.slf4j.LoggerFactory.getLogger("JiYueBoss").info("[Poltergeist] Client UI pling played");
                        } catch (Throwable ignored3) {}
                    } catch (Throwable ignored) {}
                }
            } else {
                TRANSFORMING_PLAYERS.remove(playerId);
                
                // 如果是本地玩家，恢复视角
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null && mc.player.getUUID().equals(playerId)) {
                    ClientTransformationState.restorePerspective();
                    ClientTransformationState.restoreCameraDistance();
                }
            }
        });
    }

    public static boolean isPlayerTransforming(UUID playerId) {
        return TRANSFORMING_PLAYERS.contains(playerId);
    }

    public static void clearAll() {
        TRANSFORMING_PLAYERS.clear();
    }

    /**
     * 客户端状态管理
     */
    private static class ClientTransformationState {
        private static net.minecraft.client.CameraType savedPerspective = null;
        private static double savedCameraDistance = 4.0;

        static void saveCurrentPerspective() {
            Minecraft mc = Minecraft.getInstance();
            savedPerspective = mc.options.getCameraType();
        }

        static void restorePerspective() {
            if (savedPerspective != null) {
                Minecraft mc = Minecraft.getInstance();
                mc.options.setCameraType(savedPerspective);
                savedPerspective = null;
            }
        }

        static void saveCurrentCameraDistance() {
            Minecraft mc = Minecraft.getInstance();
            if (mc.gameRenderer != null && mc.gameRenderer.getMainCamera() != null) {
                // 保存当前相机距离（虽然不能直接获取，使用默认值）
                savedCameraDistance = 4.0;
            }
        }

        static void setCameraDistance(double distance) {
            Minecraft mc = Minecraft.getInstance();
            // 通过设置渲染距离来调整第三人称视角
            // 注意：Minecraft没有直接的API来设置相机距离，但可以通过其他方式间接实现
            // 这里我们使用一个技巧：修改entity的eyeHeight暂时不可行
            // 改用Mixin方式
        }

        static void restoreCameraDistance() {
            // 恢复相机距离
        }
    }
}

