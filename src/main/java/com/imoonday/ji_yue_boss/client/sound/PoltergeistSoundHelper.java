package com.imoonday.ji_yue_boss.client.sound;

import com.imoonday.ji_yue_boss.init.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

/**
 * 专用于播放“玄溟真身”变身音效的帮助类（客户端）。
 * 使用 playLocalSound，确保本地必定播放且不被距离衰减影响。
 */
public class PoltergeistSoundHelper {

    public static void playTransformation(LocalPlayer player) {
        if (player == null) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        SoundEvent sound = ModSounds.POLTERGEIST_TRANSFORMATION.get();

        // 先用 notify 声音直接打到本地玩家（参考其它武器的实现）
        player.playNotifySound(sound, SoundSource.VOICE, 2.0f, 1.0f);

        // 再用本地播放补一遍，确保命中（不同声道，避免静音边界）
        SoundManager manager = mc.getSoundManager();
        manager.stop(sound.getLocation(), SoundSource.PLAYERS);
        mc.level.playLocalSound(player.getX(), player.getY(), player.getZ(),
                sound, SoundSource.PLAYERS, 1.6f, 1.0f, false);
    }
}


