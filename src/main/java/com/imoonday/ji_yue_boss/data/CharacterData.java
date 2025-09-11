package com.imoonday.ji_yue_boss.data;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.imoonday.ji_yue_boss.character.Character;
import com.imoonday.ji_yue_boss.character.CharacterManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.UUID;

public class CharacterData extends SavedData {

    private final BiMap<String, UUID> selectedCharacters = HashBiMap.create();

    @Override
    public CompoundTag save(CompoundTag pCompoundTag) {
        CompoundTag tag = new CompoundTag();
        for (Map.Entry<String, UUID> entry : selectedCharacters.entrySet()) {
            tag.putUUID(entry.getKey(), entry.getValue());
        }
        pCompoundTag.put("SelectedCharacters", tag);
        return pCompoundTag;
    }

    public static CharacterData fromNbt(CompoundTag tag) {
        CharacterData data = new CharacterData();
        CompoundTag selectedCharactersTag = tag.getCompound("SelectedCharacters");
        for (String key : selectedCharactersTag.getAllKeys()) {
            UUID value = selectedCharactersTag.getUUID(key);
            data.selectedCharacters.forcePut(key, value);
        }
        return data;
    }

    public static CharacterData fromServer(MinecraftServer server) {
        CharacterData data = server.overworld().getDataStorage().computeIfAbsent(CharacterData::fromNbt, CharacterData::new, "character_data");
        data.setDirty();
        return data;
    }

    public boolean isSelected(String id) {
        return selectedCharacters.containsKey(id);
    }

    public boolean setSelected(String id, UUID uuid) {
        if (selectedCharacters.containsKey(id)) {
            return false;
        }
        selectedCharacters.forcePut(id, uuid);
        setDirty();
        return true;
    }

    public boolean removeSelected(String id) {
        if (!selectedCharacters.containsKey(id)) {
            return false;
        }
        selectedCharacters.remove(id);
        setDirty();
        return true;
    }

    public boolean removeSelectedBy(UUID uuid) {
        BiMap<UUID, String> inverse = selectedCharacters.inverse();
        if (!inverse.containsKey(uuid)) {
            return false;
        }
        inverse.remove(uuid);
        setDirty();
        return true;
    }

    public void clear() {
        selectedCharacters.clear();
        setDirty();
    }

    public boolean isSelectedBy(String id, UUID uuid) {
        return selectedCharacters.containsKey(id) && selectedCharacters.get(id).equals(uuid);
    }

    @Nullable
    public Character getCharacter(UUID uuid) {
        String id = selectedCharacters.inverse().get(uuid);
        return id != null ? CharacterManager.getInstance().getCharacter(id) : null;
    }

    @Nullable
    public String getCharacterId(UUID uuid) {
        return selectedCharacters.inverse().get(uuid);
    }

    public Map<String, UUID> getSelectedCharacters() {
        return Map.copyOf(selectedCharacters);
    }

    public boolean isInvalidOwner(Player player, ItemStack stack) {
        Item item = stack.getItem();
        if (!CharacterManager.getInstance().isExclusivelyOwned(item)) return false;

        Character character = getCharacter(player.getUUID());
        return character == null || !CharacterManager.getInstance().isExclusivelyOwned(item, character);
    }
}
