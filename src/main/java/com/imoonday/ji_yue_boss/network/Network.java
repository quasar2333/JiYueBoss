package com.imoonday.ji_yue_boss.network;

import com.imoonday.ji_yue_boss.JiYueBoss;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.Optional;
import java.util.function.Supplier;

public class Network {

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(JiYueBoss.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register() {
        int i = 0;
        INSTANCE.registerMessage(i++, SyncCharacterS2CPacket.class, SyncCharacterS2CPacket::encode, SyncCharacterS2CPacket::new, Network::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        INSTANCE.registerMessage(i++, CharacterActionC2SRequest.class, CharacterActionC2SRequest::encode, CharacterActionC2SRequest::new, Network::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
        INSTANCE.registerMessage(i++, OpenCharacterSelectionS2CPacket.class, OpenCharacterSelectionS2CPacket::encode, OpenCharacterSelectionS2CPacket::new, Network::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        INSTANCE.registerMessage(i++, CloseCharacterSelectionS2CPacket.class, CloseCharacterSelectionS2CPacket::encode, CloseCharacterSelectionS2CPacket::new, Network::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        INSTANCE.registerMessage(i++, EmptyLeftClickC2SPacket.class, EmptyLeftClickC2SPacket::encode, EmptyLeftClickC2SPacket::new, Network::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));
    }

    private static <MSG extends NetworkPacket> void handle(MSG message, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        context.enqueueWork(() -> message.handle(context));
        context.setPacketHandled(true);
    }

    public static void sendToServer(NetworkPacket packet) {
        INSTANCE.sendToServer(packet);
    }

    public static void sendToClient(NetworkPacket packet, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void sendToAll(NetworkPacket packet) {
        if (ServerLifecycleHooks.getCurrentServer() != null) {
            INSTANCE.send(PacketDistributor.ALL.noArg(), packet);
        }
    }
}
