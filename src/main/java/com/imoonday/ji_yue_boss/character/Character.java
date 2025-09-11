package com.imoonday.ji_yue_boss.character;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.imoonday.ji_yue_boss.util.CodecSerializer;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.LowerCaseEnumTypeAdapterFactory;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record Character(
        @NotNull
        String id,
        @Nullable
        Component name,
        @Nullable
        Component description,
        @Nullable
        Map<Item, Integer> items,
        @Nullable
        List<Item> exclusiveItems,
        @Nullable
        ResourceLocation portrait,
        @Nullable
        List<SoundEvent> quotes,
        @Nullable
        ResourceLocation world,
        @Nullable
        BlockPos position,
        boolean serializeToString
) {

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping()
                                                     .registerTypeHierarchyAdapter(Component.class, new Component.Serializer())
                                                     .registerTypeHierarchyAdapter(Style.class, new Style.Serializer())
                                                     .registerTypeAdapterFactory(new LowerCaseEnumTypeAdapterFactory())
                                                     .registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer())
                                                     .registerTypeAdapter(Item.class, new CodecSerializer<>(ForgeRegistries.ITEMS.getCodec()))
                                                     .registerTypeAdapter(SoundEvent.class, new CodecSerializer<>(ForgeRegistries.SOUND_EVENTS.getCodec()))
                                                     .registerTypeAdapter(BlockPos.class, new CodecSerializer<>(BlockPos.CODEC))
                                                     .create();
    public static Character EMPTY = new Character("", null, null, null, null, null, null, null, null, false);

    public boolean isEmpty() {
        return this == EMPTY || name == null
                                && description == null
                                && items == null
                                && exclusiveItems == null
                                && portrait == null
                                && quotes == null
                                && world == null
                                && position == null;
    }

    public List<ItemStack> createItems() {
        return items != null ? items.entrySet().stream()
                                    .map(entry -> new ItemStack(entry.getKey(), entry.getValue()))
                                    .toList() : List.of();
    }

    @Nullable
    public SoundEvent getRandomQuote(RandomSource random) {
        return quotes != null && !quotes.isEmpty() ? Util.getRandom(quotes, random) : null;
    }

    public void giveItems(Player player) {
        List<ItemStack> items = createItems();
        if (!items.isEmpty()) {
            for (ItemStack itemStack : items) {
                if (!player.addItem(itemStack)) {
                    player.spawnAtLocation(itemStack);
                }
            }
        }
    }

    public void teleport(ServerPlayer player) {
        if (position != null) {
            Vec3 pos = Vec3.atBottomCenterOf(position);
            ServerLevel level = player.serverLevel();
            if (world != null) {
                MinecraftServer server = player.getServer();
                if (server != null) {
                    for (ServerLevel serverLevel : server.getAllLevels()) {
                        if (serverLevel.dimension().location().equals(world)) {
                            level = serverLevel;
                            break;
                        }
                    }
                }
            }
            player.teleportTo(level, pos.x, pos.y, pos.z, player.getYRot(), player.getXRot());
        }
    }

    public Component getNameOrDefault(String id) {
        return name != null ? name : Component.literal(id);
    }

    public String toJson() {
        return GSON.toJson(this);
    }

    public JsonElement toJsonTree() {
        return GSON.toJsonTree(this);
    }

    @Nullable
    public static Character fromJson(String json) {
        return GSON.fromJson(json, Character.class);
    }

    @Nullable
    public static Character fromJson(JsonElement json) {
        return GSON.fromJson(json, Character.class);
    }

    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putString("id", id);
        if (name != null) putComponent(tag, "name", name);
        if (description != null) putComponent(tag, "description", description);
        if (items != null) {
            CompoundTag itemsTag = new CompoundTag();
            for (Map.Entry<Item, Integer> entry : items.entrySet()) {
                ResourceLocation itemKey = ForgeRegistries.ITEMS.getKey(entry.getKey());
                if (itemKey != null) {
                    itemsTag.putInt(itemKey.toString(), entry.getValue());
                }
            }
            tag.put("items", itemsTag);
        }
        if (exclusiveItems != null) {
            ListTag exclusiveItemsTag = new ListTag();
            for (Item item : exclusiveItems) {
                ResourceLocation itemKey = ForgeRegistries.ITEMS.getKey(item);
                if (itemKey != null) {
                    exclusiveItemsTag.add(StringTag.valueOf(itemKey.toString()));
                }
            }
            tag.put("exclusiveItems", exclusiveItemsTag);
        }
        if (portrait != null) tag.putString("portrait", portrait.toString());
        if (quotes != null) {
            ListTag quotesTag = new ListTag();
            for (SoundEvent soundEvent : quotes) {
                if (soundEvent != null) {
                    quotesTag.add(StringTag.valueOf(soundEvent.getLocation().toString()));
                }
            }
            tag.put("quotes", quotesTag);
        }
        if (world != null) tag.putString("world", world.toString());
        if (position != null) BlockPos.CODEC.encodeStart(NbtOps.INSTANCE, position).result().ifPresent(tag1 -> tag.put("position", tag1));
        tag.putBoolean("serializeToString", serializeToString);
        return tag;
    }

    public static Character fromNbt(CompoundTag tag) {
        if (tag == null) return EMPTY;
        String id = tag.getString("id");
        Component name = getComponent(tag, "name");
        Component description = getComponent(tag, "description");

        Map<Item, Integer> items;
        if (tag.contains("items")) {
            Map<Item, Integer> map = new HashMap<>();
            CompoundTag itemsTag = tag.getCompound("items");
            for (String key : itemsTag.getAllKeys()) {
                Item item = ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse(key));
                if (item != null && !map.containsKey(item)) {
                    map.put(item, itemsTag.getInt(key));
                }
            }
            items = map;
        } else {
            items = null;
        }

        List<Item> exclusiveItems;
        if (tag.contains("exclusiveItems")) {
            List<Item> list = new ArrayList<>();
            ListTag exclusiveItemsTag = tag.getList("exclusiveItems", Tag.TAG_STRING);
            for (Tag exclusiveItemTag : exclusiveItemsTag) {
                Item item = ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse(exclusiveItemTag.getAsString()));
                if (item != null) {
                    list.add(item);
                }
            }
            exclusiveItems = list;
        } else {
            exclusiveItems = null;
        }

        ResourceLocation portrait = tag.contains("portrait") ? ResourceLocation.tryParse(tag.getString("portrait")) : null;

        List<SoundEvent> quotes;
        if (tag.contains("quotes")) {
            List<SoundEvent> list = new ArrayList<>();
            ListTag quotesTag = tag.getList("quotes", Tag.TAG_STRING);
            for (Tag quoteTag : quotesTag) {
                SoundEvent soundEvent = ForgeRegistries.SOUND_EVENTS.getValue(ResourceLocation.tryParse(quoteTag.getAsString()));
                if (soundEvent != null) {
                    list.add(soundEvent);
                }
            }
            quotes = list;
        } else {
            quotes = null;
        }

        ResourceLocation world = tag.contains("world") ? ResourceLocation.tryParse(tag.getString("world")) : null;
        BlockPos position = tag.contains("position") ? BlockPos.CODEC.parse(NbtOps.INSTANCE, tag.get("position")).result().orElse(null) : null;
        boolean serializeToString = tag.getBoolean("serializeToString");
        return new Character(id, name, description, items, exclusiveItems, portrait, quotes, world, position, serializeToString);
    }

    private void putComponent(CompoundTag tag, String key, Component component) {
        tag.putString(key, toJson(component));
    }

    private String toJson(Component component) {
        return Component.Serializer.toJson(serializeToString ? Component.literal(component.getString()).withStyle(component.getStyle()) : component);
    }

    @Nullable
    private static Component getComponent(CompoundTag tag, String name) {
        return tag.contains(name) ? Component.Serializer.fromJson(tag.getString(name)) : null;
    }
}
