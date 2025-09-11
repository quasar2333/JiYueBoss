package com.imoonday.ji_yue_boss.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class CelestisynthUtils {

    public static final String CONTROLLER = "csController";
    public static final String ANIMATION_BEGUN = "cs.hasAnimationBegun";
    public static final String ATTACK_INDEX = "cs.AttackIndex";
    public static final String ANIMATION_TIMER = "cs.animationTimer";

    public static void executeAnimation(Level level, Object animationContainer, Object attack, InteractionHand hand) {
        try {
            Method sameForBothHands = attack.getClass().getMethod("sameAnimationForBothHands");
            boolean same = (boolean) sameForBothHands.invoke(attack);
            Class<?> animationManager = Class.forName("com.aqutheseal.celestisynth.api.animation.player.AnimationManager");
            Method playDefault = animationManager.getMethod("playAnimation", Level.class, animationContainer.getClass());
            if (same) {
                if (hand == InteractionHand.OFF_HAND) {
                    Method playWithChannel = animationManager.getMethod("playAnimation", Level.class, animationContainer.getClass(), int.class);
                    playWithChannel.invoke(null, level, animationContainer, 2);
                } else {
                    playDefault.invoke(null, level, animationContainer);
                }
            } else {
                playDefault.invoke(null, level, animationContainer);
            }
        } catch (ReflectiveOperationException ignored) {
        }
    }

    public static void startUsing(Level level, InteractionHand hand, ItemStack stack, Object attack, int index) {
        CompoundTag data = stack.getOrCreateTagElement(CONTROLLER);
        data.putBoolean(ANIMATION_BEGUN, true);
        try {
            Method getAnimation = attack.getClass().getMethod("getAnimation");
            Object animation = getAnimation.invoke(attack);
            executeAnimation(level, animation, attack, hand);
        } catch (ReflectiveOperationException ignored) {
        }
        data.putInt(ATTACK_INDEX, index);
        try {
            Method startUsing = attack.getClass().getMethod("startUsing");
            startUsing.invoke(attack);
        } catch (ReflectiveOperationException ignored) {
        }
    }

    public static void tickSkill(ItemStack itemStack, Object... attacks) {
        if (attacks.length == 0) return;

        CompoundTag data = itemStack.getOrCreateTagElement(CONTROLLER);
        if (data.getBoolean(ANIMATION_BEGUN)) {
            int animationTimer = data.getInt(ANIMATION_TIMER);
            data.putInt(ANIMATION_TIMER, animationTimer + 1);

            int index = data.getInt(ATTACK_INDEX) - 1;
            if (index >= 0 && attacks.length > index) {
                Object attack = attacks[index];
                try {
                    Method baseTickSkill = attack.getClass().getMethod("baseTickSkill");
                    baseTickSkill.invoke(attack);
                } catch (ReflectiveOperationException ignored) {
                }
            }
        }
    }

    public static Object createBreezebreakerWhirlwindAttack(Player player, ItemStack stack) {
        try {
            Class<?> clazz;
            try {
                clazz = Class.forName("com.aqutheseal.celestisynth.common.attack.breezebreaker.BreezebreakerWhirlwindAttack");
            } catch (ClassNotFoundException e) {
                clazz = Class.forName("com.aqutheseal.celestisynth.common.attack.breezebreaker.whirlwind.BreezebreakerWhirlwindAttack");
            }
            for (Constructor<?> c : clazz.getConstructors()) {
                Class<?>[] p = c.getParameterTypes();
                if (p.length == 3 && Player.class.isAssignableFrom(p[0]) && ItemStack.class.isAssignableFrom(p[1]) && p[2] == int.class) {
                    return c.newInstance(player, stack, 0);
                }
                if (p.length == 2 && Player.class.isAssignableFrom(p[0]) && ItemStack.class.isAssignableFrom(p[1])) {
                    return c.newInstance(player, stack);
                }
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | ClassNotFoundException ignored) {
        }
        return new Object();
    }

    public static int getTimerProgress(Object attack) {
        try {
            Method m = attack.getClass().getMethod("getTimerProgress");
            Object v = m.invoke(attack);
            if (v instanceof Integer i) return i;
        } catch (ReflectiveOperationException ignored) {
        }
        return -1;
    }
}
