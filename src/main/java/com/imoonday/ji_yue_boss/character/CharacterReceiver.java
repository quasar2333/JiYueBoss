package com.imoonday.ji_yue_boss.character;

import com.imoonday.ji_yue_boss.data.CharacterData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public interface CharacterReceiver {

    void updateCharacter(Context context);

    record Context(Character character, boolean selectable, boolean override, boolean isOwner) {

        public void encode(FriendlyByteBuf buffer) {
            buffer.writeNbt(character.toNbt());
            buffer.writeBoolean(selectable);
            buffer.writeBoolean(override);
            buffer.writeBoolean(isOwner);
        }

        public static Context decode(FriendlyByteBuf buffer) {
            return new Context(Character.fromNbt(buffer.readNbt()), buffer.readBoolean(), buffer.readBoolean(), buffer.readBoolean());
        }

        public static Context of(CharacterData data, Character character, Player player, boolean override) {
            String id = character.id();
            UUID uuid = player.getUUID();
            return new Context(character, !data.isSelected(id) && data.getCharacterId(uuid) == null, override, data.isSelectedBy(id, uuid));
        }
    }
}
