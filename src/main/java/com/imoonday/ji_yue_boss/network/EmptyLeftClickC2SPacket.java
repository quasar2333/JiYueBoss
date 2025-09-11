package com.imoonday.ji_yue_boss.network;

import com.imoonday.ji_yue_boss.item.QixiaoItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.network.NetworkEvent;

public class EmptyLeftClickC2SPacket implements NetworkPacket {

    public EmptyLeftClickC2SPacket() {

    }

    public EmptyLeftClickC2SPacket(FriendlyByteBuf buffer) {

    }

    @Override
    public void encode(FriendlyByteBuf buffer) {

    }

    @Override
    public void handle(NetworkEvent.Context ctx) {
        ServerPlayer player = ctx.getSender();
        if (player == null) return;

        // 左键技能已删除，此网络包不再需要处理任何逻辑
    }
}
