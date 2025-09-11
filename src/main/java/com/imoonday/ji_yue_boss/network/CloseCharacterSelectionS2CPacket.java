package com.imoonday.ji_yue_boss.network;

import com.imoonday.ji_yue_boss.client.ClientUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class CloseCharacterSelectionS2CPacket implements NetworkPacket {

    public CloseCharacterSelectionS2CPacket() {

    }

    public CloseCharacterSelectionS2CPacket(FriendlyByteBuf buffer) {

    }

    @Override
    public void encode(FriendlyByteBuf buffer) {

    }

    @Override
    public void handle(NetworkEvent.Context ctx) {
        if (!ctx.getDirection().getReceptionSide().isClient()) return;

        ClientUtil.closeCharacterSelectionScreen();
    }
}
