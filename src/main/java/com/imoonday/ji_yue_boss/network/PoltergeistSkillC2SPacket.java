package com.imoonday.ji_yue_boss.network;

import com.imoonday.ji_yue_boss.item.PoltergeistSkillHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

/**
 * 客户端到服务器的包：Poltergeist中键技能触发
 */
public class PoltergeistSkillC2SPacket implements NetworkPacket {

    public PoltergeistSkillC2SPacket() {
    }

    public PoltergeistSkillC2SPacket(FriendlyByteBuf buf) {
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
    }

    @Override
    public void handle(NetworkEvent.Context context) {
        ServerPlayer player = context.getSender();
        if (player != null) {
            PoltergeistSkillHandler.activateSkill(player);
        }
    }
}


