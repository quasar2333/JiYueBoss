package com.imoonday.ji_yue_boss.init;

import com.google.common.collect.ImmutableList;
import com.imoonday.ji_yue_boss.JiYueBoss;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;

public class ModSounds {

    private static final List<RegistryObject<SoundEvent>> ALL_SOUNDS = new ArrayList<>();

    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, JiYueBoss.MODID);
    public static final RegistryObject<SoundEvent> AMON_BGM = registerSound("amon_bgm");
    public static final RegistryObject<SoundEvent> SWORD_DOWN = registerSound("sword_down");
    public static final RegistryObject<SoundEvent> TELEPORT_FORCE = registerSound("teleport_force");
    public static final RegistryObject<SoundEvent> ATTACK = registerSound("attack");
    public static final RegistryObject<SoundEvent> SAVOUR = registerSound("savour");
    public static final RegistryObject<SoundEvent> JADE_LANTERN = registerSound("jade_lantern");
    public static final RegistryObject<SoundEvent> NINE_TAILED_FIRE = registerSound("nine_tailed_fire");
    public static final RegistryObject<SoundEvent> SHEATH = registerSound("sheath");
    public static final RegistryObject<SoundEvent> UNSHEATH = registerSound("unsheath");
    public static final RegistryObject<SoundEvent> BOUNCE = registerSound("bounce");
    public static final RegistryObject<SoundEvent> BOUNCE_SUCCESS = registerSound("bounce_success");

    public static final List<RegistryObject<SoundEvent>> GOURD_SOUNDS = registerSounds("gourd", 3);
    public static final List<RegistryObject<SoundEvent>> EMPTY_SOUNDS = registerSounds("empty", 3);
    public static final List<RegistryObject<SoundEvent>> SWORD_SOUNDS = registerSounds("sword", 3);
    public static final List<RegistryObject<SoundEvent>> TELEPORT_SOUNDS = registerSounds("teleport", 3);
    public static final List<RegistryObject<SoundEvent>> JADE_LANTERN_SOUNDS = registerSounds("jade_lantern", 3);
    public static final List<RegistryObject<SoundEvent>> BOUNCE_SUCCESS_SOUNDS = registerSounds("bounce_success", 3);
    public static final List<RegistryObject<SoundEvent>> FIRE_GUN_SOUNDS = registerSounds("fire_gun", 3);
    public static final List<RegistryObject<SoundEvent>> FOLDING_FAN_SOUNDS = registerSounds("folding_fan", 3);
    public static final List<RegistryObject<SoundEvent>> DUAL_SWORDS_SOUNDS = registerSounds("dual_swords", 3);
    public static final List<RegistryObject<SoundEvent>> GREAT_SWORD_SOUNDS = registerSounds("great_sword", 3);
    public static final List<RegistryObject<SoundEvent>> XIA_NIANG_FAN_SOUNDS = registerSounds("xia_niang_fan", 3);
    public static final List<RegistryObject<SoundEvent>> SAVOUR_SOUNDS = registerSounds("savour", 3);
    public static final List<RegistryObject<SoundEvent>> BARK_SOUNDS = registerSounds("bark", 2);
    public static final List<RegistryObject<SoundEvent>> HOWLING_CELESTIAL_DOG_SOUNDS = registerSounds("howling_celestial_dog", 3);
    public static final List<RegistryObject<SoundEvent>> TIAN_YAN_SOUNDS = registerSounds("tian_yan", 3);

    private static RegistryObject<SoundEvent> registerSound(String name) {
        RegistryObject<SoundEvent> object = SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(JiYueBoss.MODID, name)));
        ALL_SOUNDS.add(object);
        return object;
    }

    private static List<RegistryObject<SoundEvent>> registerSounds(String baseName, int count) {
        ImmutableList.Builder<RegistryObject<SoundEvent>> builder = ImmutableList.builder();
        for (int i = 1; i <= count; i++) {
            builder.add(registerSound(baseName + "_" + i));
        }
        return builder.build();
    }

    public static void register(IEventBus modEventBus) {
        PreInitSoundRegistryEntry entry = PreInitSoundRegistryEntry.tryLoadFile();
        if (entry != null) {
            List<String> values = entry.values();
            for (String value : values) {
                registerSound(value);
            }
        }
        SOUNDS.register(modEventBus);
    }

    public static List<RegistryObject<SoundEvent>> getAllSounds() {
        return ImmutableList.copyOf(ALL_SOUNDS);
    }
}
