package com.imoonday.ji_yue_boss.client.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class BossMusicPlayer {

    private static BossMusic music;

    public static void playBossMusic(LivingEntity entity, SoundEvent bgm, float maxDistance) {
        if (bgm == null || !entity.isAlive()) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        boolean inRange = entity.distanceTo(player) <= maxDistance;

        if (music != null) {
            if (music.boss == entity) {
                if (!inRange) {
                    music.fadeOut();
                }
            } else if (music.boss == null && music.soundEvent == bgm && inRange) {
                music.reset(entity);
            }
        } else if (inRange) {
            music = new BossMusic(bgm, entity, entity.getRandom());
            playIfNotActive(mc);
        }
    }

    private static void playIfNotActive(Minecraft mc) {
        SoundManager soundManager = mc.getSoundManager();
        if (!soundManager.isActive(music)) {
            soundManager.play(music);
            mc.getMusicManager().stopPlaying();
        }
    }

    public static void stopBossMusic(LivingEntity entity) {
        if (music != null && music.boss == entity) {
            music.fadeOut();
        }
    }

    public static void clear() {
        stopActiveMusic();
    }

    private static void stopActiveMusic() {
        if (music != null) {
            music.stopAndClear();
        }
    }

    private static class BossMusic extends AbstractTickableSoundInstance {

        private static final float FADE_STEP = 0.03F;
        private LivingEntity boss;
        private int ticksExisted = 0;
        private final SoundEvent soundEvent;

        public BossMusic(SoundEvent bgm, LivingEntity boss, RandomSource random) {
            super(bgm, SoundSource.RECORDS, random);
            this.boss = boss;
            this.soundEvent = bgm;
            this.attenuation = Attenuation.LINEAR;
            this.looping = true;
            this.delay = 0;
            this.volume = 4.5F;
            updatePosition();
        }

        @Override
        public boolean canPlaySound() {
            return BossMusicPlayer.music == this;
        }

        @Override
        public void tick() {
            if (boss != null && boss.isAlive() && !boss.isSilent()) {
                updatePosition();
            } else {
                if (volume > FADE_STEP) {
                    volume -= FADE_STEP;
                } else {
                    stopAndClear();
                }
            }

            if (ticksExisted++ % 100 == 0) {
                Minecraft.getInstance().getMusicManager().stopPlaying();
            }
        }

        private void updatePosition() {
            if (boss != null) {
                this.x = boss.getX();
                this.y = boss.getY();
                this.z = boss.getZ();
            }
        }

        public void reset(LivingEntity newBoss) {
            this.boss = newBoss;
            this.volume = 4.5F;
            updatePosition();
        }

        public void fadeOut() {
            this.boss = null;
        }

        public void stopAndClear() {
            super.stop();
            music = null;
        }
    }
}