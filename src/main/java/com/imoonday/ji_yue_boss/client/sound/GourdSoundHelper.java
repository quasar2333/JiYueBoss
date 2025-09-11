package com.imoonday.ji_yue_boss.client.sound;

import com.imoonday.ji_yue_boss.init.ModSounds;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

public class GourdSoundHelper {

    private static final List<RegistryObject<SoundEvent>> USE_SOUNDS = ModSounds.GOURD_SOUNDS;
    private static final List<RegistryObject<SoundEvent>> EMPTY_SOUNDS = ModSounds.EMPTY_SOUNDS;

    private static final List<List<RegistryObject<SoundEvent>>> SOUNDS_LIST = List.of(USE_SOUNDS, EMPTY_SOUNDS);

    public static void playUseSound(Player player) {
        playSound(player, USE_SOUNDS);
    }

    public static void playEmptySound(Player player) {
        playSound(player, EMPTY_SOUNDS);
    }

    private static void playSound(Player player, List<RegistryObject<SoundEvent>> sounds) {
        Level level = player.level();
        if (!level.isClientSide) return;

        RandomSource random = player.getRandom();
        SoundEvent sound = Util.getRandom(sounds, random).get();
        SoundManager manager = Minecraft.getInstance().getSoundManager();
        SOUNDS_LIST.forEach(s -> s.forEach(s1 -> manager.stop(s1.get().getLocation(), SoundSource.VOICE)));
        level.playSound(player, player.blockPosition(), sound, SoundSource.VOICE);
    }
}
