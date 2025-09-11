package com.imoonday.ji_yue_boss.init;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public record PreInitSoundRegistryEntry(List<String> values) {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Path FILE_DIR = FMLPaths.GAMEDIR.get().resolve("preinitsounds.json");

    @Nullable
    public static PreInitSoundRegistryEntry tryLoadFile() {
        try {
            if (FILE_DIR.toFile().exists()) {
                String json = Files.readString(FILE_DIR);
                return fromJson(json);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load preinitsounds.json", e);
        }
        return null;
    }

    @Nullable
    public static PreInitSoundRegistryEntry fromJson(String json) {
        return GSON.fromJson(json, PreInitSoundRegistryEntry.class);
    }
}
