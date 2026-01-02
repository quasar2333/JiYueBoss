package com.imoonday.ji_yue_boss.init;

import com.imoonday.ji_yue_boss.JiYueBoss;
import com.imoonday.ji_yue_boss.item.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, JiYueBoss.MODID);

    public static final RegistryObject<CharacterSelectorItem> CHARACTER_SELECTOR = ITEMS.register("character_selector", () -> new CharacterSelectorItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<XiaoxiangItem> XIAOXIANG = ITEMS.register("xiaoxiang", () -> new XiaoxiangItem(XiaoxiangItem.TIER, 6, -2.4f, new Item.Properties()));
    public static final RegistryObject<NamelessItem> NAMELESS = ITEMS.register("nameless", () -> new NamelessItem(NamelessItem.TIER, 3, -2.4f, new Item.Properties()));

    // 玄溟的折扇已删除
    // public static final RegistryObject<FoldingFanItem> COMMON_FOLDING_FAN = ITEMS.register("common_folding_fan", () -> new FoldingFanItem(FoldingFanItem.Quality.COMMON, new Item.Properties().stacksTo(1).rarity(Rarity.COMMON)));
    // public static final RegistryObject<FoldingFanItem> RARE_FOLDING_FAN = ITEMS.register("rare_folding_fan", () -> new FoldingFanItem(FoldingFanItem.Quality.RARE, new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
    // public static final RegistryObject<FoldingFanItem> MYTHIC_FOLDING_FAN = ITEMS.register("mythic_folding_fan", () -> new FoldingFanItem(FoldingFanItem.Quality.MYTHIC, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

    public static final RegistryObject<SabreItem> COMMON_SABRE = ITEMS.register("common_sabre", () -> new SabreItem(SabreItem.TIER, 3, -2.4f, SabreItem.Quality.COMMON, new Item.Properties().rarity(Rarity.COMMON)));
    public static final RegistryObject<SabreItem> RARE_SABRE = ITEMS.register("rare_sabre", () -> new SabreItem(SabreItem.TIER, 3, -2.4f, SabreItem.Quality.RARE, new Item.Properties().rarity(Rarity.RARE)));
    public static final RegistryObject<SabreItem> MYTHIC_SABRE = ITEMS.register("mythic_sabre", () -> new SabreItem(SabreItem.TIER, 3, -2.4f, SabreItem.Quality.MYTHIC, new Item.Properties().rarity(Rarity.EPIC)));

    public static final RegistryObject<GourdItem> COMMON_GOURD = ITEMS.register("common_gourd", () -> new GourdItem(new Item.Properties().durability(1).rarity(Rarity.COMMON)));
    public static final RegistryObject<GourdItem> RARE_GOURD = ITEMS.register("rare_gourd", () -> new GourdItem(new Item.Properties().durability(2).rarity(Rarity.RARE)));
    public static final RegistryObject<GourdItem> MYTHIC_GOURD = ITEMS.register("mythic_gourd", () -> new GourdItem(new Item.Properties().durability(3).rarity(Rarity.EPIC)));

    public static final RegistryObject<JadeLanternItem> COMMON_JADE_LANTERN = ITEMS.register("common_jade_lantern", () -> new JadeLanternItem(JadeLanternItem.Quality.COMMON, new Item.Properties().stacksTo(1).rarity(Rarity.COMMON)));
    public static final RegistryObject<JadeLanternItem> RARE_JADE_LANTERN = ITEMS.register("rare_jade_lantern", () -> new JadeLanternItem(JadeLanternItem.Quality.RARE, new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
    public static final RegistryObject<JadeLanternItem> MYTHIC_JADE_LANTERN = ITEMS.register("mythic_jade_lantern", () -> new JadeLanternItem(JadeLanternItem.Quality.MYTHIC, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

    public static final RegistryObject<BoneBladeItem> COMMON_BONE_BLADE = ITEMS.register("common_bone_blade", () -> new BoneBladeItem(BoneBladeItem.Quality.COMMON, new Item.Properties().stacksTo(1).rarity(Rarity.COMMON)));
    public static final RegistryObject<BoneBladeItem> RARE_BONE_BLADE = ITEMS.register("rare_bone_blade", () -> new BoneBladeItem(BoneBladeItem.Quality.RARE, new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
    public static final RegistryObject<BoneBladeItem> MYTHIC_BONE_BLADE = ITEMS.register("mythic_bone_blade", () -> new BoneBladeItem(BoneBladeItem.Quality.MYTHIC, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

    public static final RegistryObject<SavourItem> COMMON_SAVOUR = ITEMS.register("common_savour", () -> new SavourItem(SavourItem.Quality.COMMON, new Item.Properties().stacksTo(1).rarity(Rarity.COMMON)));
    public static final RegistryObject<SavourItem> RARE_SAVOUR = ITEMS.register("rare_savour", () -> new SavourItem(SavourItem.Quality.RARE, new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
    public static final RegistryObject<SavourItem> MYTHIC_SAVOUR = ITEMS.register("mythic_savour", () -> new SavourItem(SavourItem.Quality.MYTHIC, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

    // 稚晚的双剑（双持检测与突进返回技能）
    public static final RegistryObject<DualSwordsItem> COMMON_DUAL_SWORDS = ITEMS.register("common_dual_swords", () -> new DualSwordsItem(DualSwordsItem.Quality.COMMON, new Item.Properties().stacksTo(1).rarity(Rarity.COMMON)));
    public static final RegistryObject<DualSwordsItem> RARE_DUAL_SWORDS = ITEMS.register("rare_dual_swords", () -> new DualSwordsItem(DualSwordsItem.Quality.RARE, new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
    public static final RegistryObject<DualSwordsItem> MYTHIC_DUAL_SWORDS = ITEMS.register("mythic_dual_swords", () -> new DualSwordsItem(DualSwordsItem.Quality.MYTHIC, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

    // 玄溟的重剑（右键自我增益技能）
    public static final RegistryObject<GreatSwordItem> COMMON_GREAT_SWORD = ITEMS.register("common_great_sword", () -> new GreatSwordItem(GreatSwordItem.Quality.COMMON, new Item.Properties().stacksTo(1).rarity(Rarity.COMMON)));
    public static final RegistryObject<GreatSwordItem> RARE_GREAT_SWORD = ITEMS.register("rare_great_sword", () -> new GreatSwordItem(GreatSwordItem.Quality.RARE, new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
    public static final RegistryObject<GreatSwordItem> MYTHIC_GREAT_SWORD = ITEMS.register("mythic_great_sword", () -> new GreatSwordItem(GreatSwordItem.Quality.MYTHIC, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

    // 瑕娘的折扇（召唤九尾）
    public static final RegistryObject<XiaNiangFanItem> COMMON_XIA_NIANG_FAN = ITEMS.register("common_xia_niang_fan", () -> new XiaNiangFanItem(XiaNiangFanItem.Quality.COMMON, new Item.Properties().stacksTo(1).rarity(Rarity.COMMON)));
    public static final RegistryObject<XiaNiangFanItem> RARE_XIA_NIANG_FAN = ITEMS.register("rare_xia_niang_fan", () -> new XiaNiangFanItem(XiaNiangFanItem.Quality.RARE, new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
    public static final RegistryObject<XiaNiangFanItem> MYTHIC_XIA_NIANG_FAN = ITEMS.register("mythic_xia_niang_fan", () -> new XiaNiangFanItem(XiaNiangFanItem.Quality.MYTHIC, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

    public static final RegistryObject<QixiaoItem> COMMON_QIXIAO = ITEMS.register("common_qixiao", () -> new QixiaoItem(QixiaoItem.Quality.COMMON, new Item.Properties().stacksTo(1), false));
    public static final RegistryObject<QixiaoItem> RARE_QIXIAO = ITEMS.register("rare_qixiao", () -> new QixiaoItem(QixiaoItem.Quality.RARE, new Item.Properties().stacksTo(1), false));
    public static final RegistryObject<QixiaoItem> MYTHIC_QIXIAO = ITEMS.register("mythic_qixiao", () -> new QixiaoItem(QixiaoItem.Quality.MYTHIC, new Item.Properties().stacksTo(1), false));

    public static final RegistryObject<QixiaoItem> COMMON_UNSHEATHED_QIXIAO = ITEMS.register("common_unsheathed_qixiao", () -> new QixiaoItem(QixiaoItem.Quality.COMMON, new Item.Properties().stacksTo(1), true));
    public static final RegistryObject<QixiaoItem> RARE_UNSHEATHED_QIXIAO = ITEMS.register("rare_unsheathed_qixiao", () -> new QixiaoItem(QixiaoItem.Quality.RARE, new Item.Properties().stacksTo(1), true));
    public static final RegistryObject<QixiaoItem> MYTHIC_UNSHEATHED_QIXIAO = ITEMS.register("mythic_unsheathed_qixiao", () -> new QixiaoItem(QixiaoItem.Quality.MYTHIC, new Item.Properties().stacksTo(1), true));

    public static final RegistryObject<FireGunItem> COMMON_FIRE_GUN = ITEMS.register("common_fire_gun", () -> new FireGunItem(FireGunItem.Quality.COMMON, new Item.Properties().stacksTo(1).rarity(Rarity.COMMON)));
    public static final RegistryObject<FireGunItem> RARE_FIRE_GUN = ITEMS.register("rare_fire_gun", () -> new FireGunItem(FireGunItem.Quality.RARE, new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
    public static final RegistryObject<FireGunItem> MYTHIC_FIRE_GUN = ITEMS.register("mythic_fire_gun", () -> new FireGunItem(FireGunItem.Quality.MYTHIC, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

    public static final RegistryObject<ForgeSpawnEggItem> FAKE_AMON_SPAWN_EGG = ITEMS.register("fake_amon_spawn_egg", () -> new ForgeSpawnEggItem(ModEntities.FAKE_AMON, 0x00FF00, 0x0000FF, new Item.Properties()));
    public static final RegistryObject<ForgeSpawnEggItem> AMON_SPAWN_EGG = ITEMS.register("amon_spawn_egg", () -> new ForgeSpawnEggItem(ModEntities.AMON, 0x00FF00, 0x0000FF, new Item.Properties()));
    public static final RegistryObject<ForgeSpawnEggItem> AMON_BOSS_SPAWN_EGG = ITEMS.register("amon_boss_spawn_egg", () -> new ForgeSpawnEggItem(ModEntities.AMON_BOSS, 0x00FF00, 0x0000FF, new Item.Properties()));
    public static final RegistryObject<ForgeSpawnEggItem> WU_MING_TIAN_SHEN_SPAWN_EGG = ITEMS.register("wu_ming_tian_shen_spawn_egg", () -> new ForgeSpawnEggItem(ModEntities.WU_MING_TIAN_SHEN, 0x4169E1, 0xFFD700, new Item.Properties()));

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, JiYueBoss.MODID);
    public static final RegistryObject<CreativeModeTab> TAB = CREATIVE_MODE_TABS.register("tab", () -> CreativeModeTab.builder()
                                                                                                                      .icon(() -> CHARACTER_SELECTOR.get().getDefaultInstance())
                                                                                                                      .title(Component.literal("Ji Yue Boss"))
                                                                                                                      .displayItems((parameters, output) -> ITEMS.getEntries().forEach(object -> output.accept(object.get())))
                                                                                                                      .build()
    );

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
    }
}
