package com.imoonday.ji_yue_boss.util;

import com.google.gson.*;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import org.slf4j.Logger;

import java.lang.reflect.Type;

public record CodecSerializer<T>(Codec<T> codec) implements JsonSerializer<T>, JsonDeserializer<T> {

    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return codec.parse(JsonOps.INSTANCE, json).resultOrPartial(LOGGER::error).orElse(null);
    }

    @Override
    public JsonElement serialize(T src, Type typeOfSrc, JsonSerializationContext context) {
        return codec.encodeStart(JsonOps.INSTANCE, src).resultOrPartial(LOGGER::error).orElse(JsonNull.INSTANCE);
    }
}
