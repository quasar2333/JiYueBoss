package com.imoonday.ji_yue_boss.util;

import net.minecraft.Util;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

public class Utils {

    public static <T extends ParticleOptions> void addParticlesAround(T type, ServerLevel level, Entity entity, int count) {
        double offsetWidth = entity.getBbWidth() / 2.0;
        double offsetHeight = entity.getBbHeight() / 2.0;
        level.sendParticles(type, entity.getX(), entity.getY() + offsetHeight, entity.getZ(), count, offsetWidth, offsetHeight, offsetWidth, 0.0);
    }

    public static void playRandomSound(List<RegistryObject<SoundEvent>> sounds, Level level, Player player) {
        Util.getRandomSafe(sounds, level.random).ifPresent(object -> level.playSound(null, player.blockPosition(), object.get(), SoundSource.PLAYERS, 1.0f, 1.0f));
    }

    public static void playRandomNotifySound(List<RegistryObject<SoundEvent>> sounds, Player player) {
        Util.getRandomSafe(sounds, player.getRandom()).ifPresent(object -> player.playNotifySound(object.get(), SoundSource.VOICE, 1.0f, 1.0f));
    }

    public static void addCooldown(LivingEntity entity, int cooldown, Iterable<? extends Item> items) {
        if (entity instanceof Player player) {
            if (!FMLLoader.isProduction() && player.isCreative()) {
                cooldown = 20;
            }
            ItemCooldowns cooldowns = player.getCooldowns();
            for (Item item : items) {
                cooldowns.addCooldown(item, cooldown);
            }
        }
    }

    public static void addCooldown(LivingEntity entity, int cooldown, Item... items) {
        addCooldown(entity, cooldown, List.of(items));
    }
}
