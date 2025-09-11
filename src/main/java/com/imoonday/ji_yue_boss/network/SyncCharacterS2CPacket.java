package com.imoonday.ji_yue_boss.network;

import com.imoonday.ji_yue_boss.character.CharacterReceiver;
import com.imoonday.ji_yue_boss.client.ClientUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public record SyncCharacterS2CPacket(CharacterReceiver.Context context) implements NetworkPacket {

    public SyncCharacterS2CPacket(FriendlyByteBuf buffer) {
        this(CharacterReceiver.Context.decode(buffer));
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        context.encode(buffer);
    }

    @Override
    public void handle(NetworkEvent.Context ctx) {
        if (!ctx.getDirection().getReceptionSide().isClient()) return;

        ClientUtil.updateCharacter(context);
    }
}
