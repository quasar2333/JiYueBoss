package com.imoonday.ji_yue_boss.datagen;

import com.imoonday.ji_yue_boss.JiYueBoss;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = JiYueBoss.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModDataGen {

    @SubscribeEvent
    public static void onDataGen(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();

        PackOutput packOutput = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        generator.addProvider(event.includeClient(), new SoundProvider(packOutput, existingFileHelper));
    }
}
