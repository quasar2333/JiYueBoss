package com.imoonday.ji_yue_boss.event;

import com.imoonday.ji_yue_boss.JiYueBoss;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

/**
 * 改变原版设定：爆炸不再摧毁地上的掉落物（ItemEntity）。
 * 通过移除爆炸将要影响的实体列表中的 ItemEntity 来实现。
 */
@Mod.EventBusSubscriber(modid = JiYueBoss.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ExplosionProtectionEventHandler {

    @SubscribeEvent
    public static void onExplosionDetonate(ExplosionEvent.Detonate event) {
        List<Entity> affected = event.getAffectedEntities();
        if (affected == null || affected.isEmpty()) return;
        affected.removeIf(entity -> entity instanceof ItemEntity);
    }
}


