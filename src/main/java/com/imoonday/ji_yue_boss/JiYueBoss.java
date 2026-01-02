package com.imoonday.ji_yue_boss;

import com.imoonday.ji_yue_boss.character.CharacterManager;
import com.imoonday.ji_yue_boss.client.renderer.AmonRenderer;
import com.imoonday.ji_yue_boss.client.renderer.HowlingCelestialDogRenderer;
import com.imoonday.ji_yue_boss.client.renderer.NineTailedFoxRenderer;
import com.imoonday.ji_yue_boss.client.renderer.SabreRenderer;
import com.imoonday.ji_yue_boss.client.sound.BossMusicPlayer;
import com.imoonday.ji_yue_boss.data.CharacterData;
import com.imoonday.ji_yue_boss.entity.Amon;
import com.imoonday.ji_yue_boss.entity.AmonBoss;
import com.imoonday.ji_yue_boss.entity.FakeAmon;
import com.imoonday.ji_yue_boss.entity.HowlingCelestialDog;
import com.imoonday.ji_yue_boss.entity.NineTailedFox;
import com.imoonday.ji_yue_boss.init.ModEntities;
import com.imoonday.ji_yue_boss.init.ModItems;
import com.imoonday.ji_yue_boss.init.ModSounds;
import com.imoonday.ji_yue_boss.item.QixiaoItem;
import com.imoonday.ji_yue_boss.network.Network;
import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@SuppressWarnings("removal")
@Mod(JiYueBoss.MODID)
public class JiYueBoss {

    public static final String MODID = "ji_yue_boss";
    private static final Logger LOGGER = LogUtils.getLogger();
    public static boolean cyclic = ModList.get().isLoaded("cyclic");
    public static boolean fromtheshadows = ModList.get().isLoaded("fromtheshadows");

    public JiYueBoss() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        Network.register();

        modEventBus.register(this);
        ModItems.register(modEventBus);
        ModEntities.register(modEventBus);
        ModSounds.register(modEventBus);
    }

    @SubscribeEvent
    public void registerDefaultAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.AMON_BOSS.get(), AmonBoss.createAttributes().build());
        event.put(ModEntities.AMON.get(), Amon.createAttributes().build());
        event.put(ModEntities.FAKE_AMON.get(), FakeAmon.createAttributes().build());
        event.put(ModEntities.HOWLING_CELESTIAL_DOG.get(), HowlingCelestialDog.createMobAttributes().build());
        event.put(ModEntities.NINE_TAILED_FOX.get(), NineTailedFox.createAttributes().build());
        event.put(ModEntities.POLTERGEIST_TRANSFORMATION.get(), com.imoonday.ji_yue_boss.entity.PoltergeistTransformation.createAttributes().build());
        event.put(ModEntities.WU_MING_TIAN_SHEN.get(), com.imoonday.ji_yue_boss.entity.WuMingTianShen.createAttributes().build());
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ForgeEvents {

        @SubscribeEvent
        public static void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
            if (event.getLevel().isClientSide && event.getEntity() instanceof LivingEntity living) {
                BossMusicPlayer.stopBossMusic(living);
            }
        }

        @SubscribeEvent
        public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
            BossMusicPlayer.clear();
        }

        @SubscribeEvent
        public static void onLivingTick(LivingEvent.LivingTickEvent event) {
            LivingEntity entity = event.getEntity();
            if (!entity.level().isClientSide && !(entity instanceof Player)) {
                for (EquipmentSlot slot : EquipmentSlot.values()) {
                    if (CharacterManager.getInstance().isExclusivelyOwned(entity.getItemBySlot(slot).getItem())) {
                        entity.setItemSlot(slot, ItemStack.EMPTY);
                    }
                }
            }

            // DualSwords dash tick and Poltergeist transformation tick
            if (entity instanceof Player player) {
                if (com.imoonday.ji_yue_boss.item.DualSwordsItem.isDashing(player)) {
                    com.imoonday.ji_yue_boss.item.DualSwordsItem.tickDash(player);
                }
                
                // Poltergeist transformation tick (server-side)
                if (!player.level().isClientSide && player instanceof ServerPlayer serverPlayer) {
                    com.imoonday.ji_yue_boss.item.PoltergeistSkillHandler.tick(serverPlayer);
                }
            }

        }

        @SubscribeEvent
        public static void onPickupItem(EntityItemPickupEvent event) {
            Player player = event.getEntity();
            if (player.getAbilities().instabuild) return;

            if (player.level() instanceof ServerLevel level) {
                CharacterData data = CharacterData.fromServer(level.getServer());
                if (data.isInvalidOwner(player, event.getItem().getItem())) {
                    event.setCanceled(true);
                }
            }
        }
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModEvents {

        @SubscribeEvent
        public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(ModEntities.AMON_BOSS.get(), AmonRenderer::new);
            event.registerEntityRenderer(ModEntities.AMON.get(), AmonRenderer::new);
            event.registerEntityRenderer(ModEntities.FAKE_AMON.get(), AmonRenderer::new);
            event.registerEntityRenderer(ModEntities.SABRE.get(), SabreRenderer::new);
            event.registerEntityRenderer(ModEntities.HOWLING_CELESTIAL_DOG.get(), HowlingCelestialDogRenderer::new);
            event.registerEntityRenderer(ModEntities.NINE_TAILED_FOX.get(), NineTailedFoxRenderer::new);
            event.registerEntityRenderer(ModEntities.POLTERGEIST_TRANSFORMATION.get(), com.imoonday.ji_yue_boss.client.renderer.PoltergeistTransformationRenderer::new);
            event.registerEntityRenderer(ModEntities.WU_MING_TIAN_SHEN.get(), com.imoonday.ji_yue_boss.client.renderer.WuMingTianShenRenderer::new);
        }

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            QixiaoItem.registerProperties();
        }
    }
}
