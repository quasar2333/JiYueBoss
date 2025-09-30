package com.imoonday.ji_yue_boss.init;

import com.imoonday.ji_yue_boss.JiYueBoss;
import com.imoonday.ji_yue_boss.entity.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, JiYueBoss.MODID);
    public static final RegistryObject<EntityType<FakeAmon>> FAKE_AMON = ENTITY_TYPES.register("fake_amon", () -> EntityType.Builder.of(FakeAmon::new, MobCategory.MONSTER).sized(0.6F, 1.8F).updateInterval(2).clientTrackingRange(8).build("fake_amon"));
    public static final RegistryObject<EntityType<Amon>> AMON = ENTITY_TYPES.register("amon", () -> EntityType.Builder.<Amon>of(Amon::new, MobCategory.MONSTER).sized(0.6F, 1.8F).updateInterval(2).clientTrackingRange(8).build("amon"));
    public static final RegistryObject<EntityType<AmonBoss>> AMON_BOSS = ENTITY_TYPES.register("amon_boss", () -> EntityType.Builder.<AmonBoss>of(AmonBoss::new, MobCategory.MONSTER).sized(0.6F, 1.8F).updateInterval(2).clientTrackingRange(8).fireImmune().build("amon_boss"));
    public static final RegistryObject<EntityType<Sabre>> SABRE = ENTITY_TYPES.register("sabre", () -> EntityType.Builder.<Sabre>of(Sabre::new, MobCategory.MISC).sized(0.5F, 5.0F).updateInterval(2).clientTrackingRange(8).build("sabre"));
    public static final RegistryObject<EntityType<HowlingCelestialDog>> HOWLING_CELESTIAL_DOG = ENTITY_TYPES.register("howling_celestial_dog", () -> EntityType.Builder.<HowlingCelestialDog>of(HowlingCelestialDog::new, MobCategory.CREATURE).sized(1.5F, 2.5F).updateInterval(2).clientTrackingRange(8).build("howling_celestial_dog"));
    public static final RegistryObject<EntityType<NineTailedFox>> NINE_TAILED_FOX = ENTITY_TYPES.register("nine_tailed_fox", () -> EntityType.Builder.<NineTailedFox>of(NineTailedFox::new, MobCategory.MISC).sized(1.0F, 1.8F).updateInterval(2).clientTrackingRange(8).build("nine_tailed_fox"));
    public static final RegistryObject<EntityType<PoltergeistTransformation>> POLTERGEIST_TRANSFORMATION = ENTITY_TYPES.register("poltergeist_transformation", () -> EntityType.Builder.<PoltergeistTransformation>of(PoltergeistTransformation::new, MobCategory.MISC).sized(1.0F, 2.5F).updateInterval(2).clientTrackingRange(8).build("poltergeist_transformation"));

    public static void register(IEventBus modEventBus) {
        ENTITY_TYPES.register(modEventBus);
    }
}
