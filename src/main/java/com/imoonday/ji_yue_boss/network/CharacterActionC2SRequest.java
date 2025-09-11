package com.imoonday.ji_yue_boss.network;

import com.imoonday.ji_yue_boss.character.Character;
import com.imoonday.ji_yue_boss.character.CharacterManager;
import com.imoonday.ji_yue_boss.character.CharacterReceiver;
import com.imoonday.ji_yue_boss.data.CharacterData;
import com.imoonday.ji_yue_boss.item.CharacterSelectorItem;
import com.mojang.logging.LogUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.slf4j.Logger;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public record CharacterActionC2SRequest(String id, Action action) implements NetworkPacket {

    public CharacterActionC2SRequest(Action action) {
        this("", action);
    }

    private static final Logger LOGGER = LogUtils.getLogger();

    public CharacterActionC2SRequest(FriendlyByteBuf buffer) {
        this(buffer.readUtf(), buffer.readEnum(Action.class));
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUtf(id);
        buffer.writeEnum(action);
    }

    @Override
    public void handle(NetworkEvent.Context ctx) {
        ServerPlayer player = ctx.getSender();
        if (player == null) return;

        MinecraftServer server = player.getServer();
        if (server == null) return;

        CharacterData data = CharacterData.fromServer(server);

        Character character;
        UUID uuid = player.getUUID();
        switch (action) {
            case INIT -> {
                List<Character> characters = CharacterManager.getInstance().getCharacters();
                character = characters.isEmpty() ? Character.EMPTY : characters.get(0);
            }
            case SELECT -> {
                character = CharacterManager.getInstance().getCharacter(id);
                if (character == null || character.isEmpty() || data.getCharacterId(uuid) != null || !data.setSelected(id, uuid)) return;

                CharacterSelectorItem.consumeItem(player);
                Network.sendToClient(new CloseCharacterSelectionS2CPacket(), player);
                character.teleport(player);
                character.giveItems(player);
            }
            case NEXT, PREV -> {
                int index = 0;
                List<Character> characters = CharacterManager.getInstance().getCharacters();
                int size = characters.size();
                for (int i = 0; i < size; i++) {
                    Character entry = characters.get(i);
                    if (Objects.equals(entry.id(), id)) {
                        index = action == Action.NEXT ? (i + 1) % size : (i - 1 + size) % size;
                        break;
                    }
                }
                if (index < 0 || index >= size) return;

                character = characters.get(index);
                if (character == null || character.isEmpty()) return;
            }
            case GET_ITEMS -> {
                character = data.getCharacter(uuid);
                if (character == null || character.isEmpty()) return;

                character.giveItems(player);
            }
            default -> {
                LOGGER.warn("Unknown action: {}", action);
                return;
            }
        }

        for (ServerPlayer serverPlayer : server.getPlayerList().getPlayers()) {
            Network.sendToClient(new SyncCharacterS2CPacket(CharacterReceiver.Context.of(data, character, serverPlayer, serverPlayer == player)), serverPlayer);
        }
    }

    public enum Action {
        INIT, PREV, SELECT, NEXT, GET_ITEMS
    }
}
