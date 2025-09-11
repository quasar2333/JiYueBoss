package com.imoonday.ji_yue_boss.datagen;

import com.imoonday.ji_yue_boss.JiYueBoss;
import com.imoonday.ji_yue_boss.init.ModSounds;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.SoundDefinition;
import net.minecraftforge.common.data.SoundDefinitionsProvider;
import net.minecraftforge.registries.RegistryObject;

public class SoundProvider extends SoundDefinitionsProvider {

    public SoundProvider(PackOutput output, ExistingFileHelper helper) {
        super(output, JiYueBoss.MODID, helper);
    }

    @Override
    public void registerSounds() {
        for (RegistryObject<SoundEvent> sound : ModSounds.getAllSounds()) {
            SoundDefinition definition = definition();

            if (sound.equals(ModSounds.AMON_BGM)) {
                definition.subtitle("sound.ji_yue_boss.amon_bgm");
            }

            ResourceLocation id = sound.getId();
            if (id != null) {
                definition.with(sound(id));
                this.add(id, definition);
            }
        }
    }
}
