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
                    // 保存当前视角设置
                    ClientTransformationState.saveCurrentPerspective();
                    // 强制切换到第三人称背后视角
                    mc.options.setCameraType(net.minecraft.client.CameraType.THIRD_PERSON_BACK);
                }
            } else {
                TRANSFORMING_PLAYERS.remove(playerId);
                
                // 如果是本地玩家，恢复视角
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null && mc.player.getUUID().equals(playerId)) {
                    ClientTransformationState.restorePerspective();
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
    }
}


