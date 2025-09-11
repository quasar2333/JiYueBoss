package com.imoonday.ji_yue_boss.character;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.imoonday.ji_yue_boss.JiYueBoss;
import com.imoonday.ji_yue_boss.network.CloseCharacterSelectionS2CPacket;
import com.imoonday.ji_yue_boss.network.Network;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = JiYueBoss.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CharacterManager extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = Character.GSON;
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final CharacterManager INSTANCE = new CharacterManager("characters");
    private final Map<String, Character> characters = new HashMap<>();
    private final Multimap<Item, Character> exclusiveItems = HashMultimap.create();

    public CharacterManager(String pDirectory) {
        super(GSON, pDirectory);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
        characters.clear();
        exclusiveItems.clear();
        for (Map.Entry<ResourceLocation, JsonElement> entry : map.entrySet()) {
            ResourceLocation location = entry.getKey();
            if (!location.getNamespace().equals(JiYueBoss.MODID)) continue;

            try {
                JsonElement json = entry.getValue();
                Character character = Character.fromJson(json);
                if (character != null) {
                    String id = character.id();
                    if (characters.containsKey(id)) {
                        LOGGER.warn("Duplicate character id: {}", id);
                    }
                    characters.put(id, character);
                    List<Item> items = character.exclusiveItems();
                    if (items != null && !items.isEmpty()) {
                        for (Item item : items) {
                            exclusiveItems.put(item, character);
                        }
                    }
                } else {
                    LOGGER.error("Parsing failed loading character {}: {}", location, json);
                }
            } catch (Exception e) {
                LOGGER.error("Parsing error loading character {}", location, e);
            }
        }

        LOGGER.info("Loaded {} characters", characters.size());
        Network.sendToAll(new CloseCharacterSelectionS2CPacket());
    }

    @SubscribeEvent
    public static void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(INSTANCE);
    }

    public static CharacterManager getInstance() {
        return INSTANCE;
    }

    @Nullable
    public Character getCharacter(String name) {
        return characters.get(name);
    }

    public List<Character> getCharactersWithItem(Item item) {
        return List.copyOf(exclusiveItems.get(item));
    }

    public List<Character> getCharacters() {
        return List.copyOf(characters.values());
    }

    public boolean isExclusivelyOwned(Item item) {
        return exclusiveItems.containsKey(item);
    }

    public boolean isExclusivelyOwned(Item item, Character character) {
        return exclusiveItems.containsEntry(item, character);
    }
}
