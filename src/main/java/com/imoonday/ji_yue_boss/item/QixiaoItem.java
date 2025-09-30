package com.imoonday.ji_yue_boss.item;

import com.imoonday.ji_yue_boss.JiYueBoss;
import com.imoonday.ji_yue_boss.init.ModItems;
import com.imoonday.ji_yue_boss.init.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid = JiYueBoss.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class QixiaoItem extends SwordItem implements GeoItem {

    private static Map<Item, Item> transfomations = null;
    private static final Map<UUID, ParryWindow> PARRY_WINDOWS = new HashMap<>();
    private static final Map<UUID, EarlyWindow> EARLY_WINDOWS = new HashMap<>();
    private static final List<QixiaoItem> UNSHEATHED_ITEMS = new java.util.ArrayList<>();
    private final boolean unsheathed;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final Quality quality;

    public QixiaoItem(Quality quality, Properties pProperties, boolean unsheathed) {
        super(quality.getTier(), quality.getAttackDamage(), unsheathed ? -2.8f : -2.4f, pProperties);
        this.quality = quality;
        this.unsheathed = unsheathed;
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
        if (unsheathed) {
            UNSHEATHED_ITEMS.add(this);
        }
    }

    public Quality getQuality() {
        return quality;
    }

    public static void registerProperties() {
        ItemProperties.register(ModItems.COMMON_QIXIAO.get(), new ResourceLocation("blocking"), (stack, level, entity, i) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);
        ItemProperties.register(ModItems.COMMON_UNSHEATHED_QIXIAO.get(), new ResourceLocation("blocking"), (stack, level, entity, i) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);
    }

    @Override
    public Component getName(ItemStack pStack) {
        return super.getName(pStack).copy().withStyle(style -> style.withColor(0x64BDED));
    }

    public boolean isUnsheathed() {
        return unsheathed;
    }

    @Override
    public boolean hurtEnemy(ItemStack pStack, LivingEntity pTarget, LivingEntity pAttacker) {
        // 为qi_xiao状态添加额外伤害
        if (!unsheathed && pAttacker instanceof Player) {
            float extraDamage = quality.getQiXiaoDamage();
            if (extraDamage > 0) {
                pTarget.hurt(pAttacker.damageSources().playerAttack((Player) pAttacker), extraDamage);
            }
        }
        return super.hurtEnemy(pStack, pTarget, pAttacker);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isDiscrete()) {
            stack = transform(stack);
            if (stack.getItem() instanceof QixiaoItem qixiao) {
                level.playSound(null, player, qixiao.isUnsheathed() ? ModSounds.UNSHEATH.get() : ModSounds.SHEATH.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }
        // Parry on unsheathed state
        if (!level.isClientSide && this.unsheathed) {
            boolean success = tryParry((ServerLevel) level, player, this.quality);
            if (success) {
                level.playSound(null, player.blockPosition(), ModSounds.BOUNCE_SUCCESS.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
                com.imoonday.ji_yue_boss.util.Utils.playRandomSound(com.imoonday.ji_yue_boss.init.ModSounds.BOUNCE_SUCCESS_SOUNDS, level, player);
                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK, player.getX(), player.getY() + 1.0, player.getZ(), 15, 0.5, 0.8, 0.5, 0.0);
                }
                // 成功时移除所有出鞘形态的冷却，并清除可能残留的提前窗口
                net.minecraft.world.item.ItemCooldowns cds = player.getCooldowns();
                for (QixiaoItem item : UNSHEATHED_ITEMS) {
                    cds.removeCooldown(item);
                }
                EARLY_WINDOWS.remove(player.getUUID());
            } else {
                // 未命中后向窗口：开启提前窗口（0.3s）等待即将到来的攻击
                long now = level.getGameTime();
                EARLY_WINDOWS.put(player.getUUID(), new EarlyWindow(now + 8));
            }
            return InteractionResultHolder.sidedSuccess(stack, false);
        }
        return InteractionResultHolder.pass(stack);
    }



    public ItemStack transform(ItemStack stack) {
        if (transfomations == null) {
            transfomations = new HashMap<>();
            transfomations.put(ModItems.COMMON_QIXIAO.get(), ModItems.COMMON_UNSHEATHED_QIXIAO.get());
            transfomations.put(ModItems.RARE_QIXIAO.get(), ModItems.RARE_UNSHEATHED_QIXIAO.get());
            transfomations.put(ModItems.MYTHIC_QIXIAO.get(), ModItems.MYTHIC_UNSHEATHED_QIXIAO.get());
            transfomations.put(ModItems.COMMON_UNSHEATHED_QIXIAO.get(), ModItems.COMMON_QIXIAO.get());
            transfomations.put(ModItems.RARE_UNSHEATHED_QIXIAO.get(), ModItems.RARE_QIXIAO.get());
            transfomations.put(ModItems.MYTHIC_UNSHEATHED_QIXIAO.get(), ModItems.MYTHIC_QIXIAO.get());
        }

        Item result = transfomations.get(stack.getItem());
        return result == null ? stack : copyWithItem(stack, result);
    }

    private ItemStack copyWithItem(ItemStack stack, Item item) {
        return new ItemStack(item, stack.getCount(), stack.getTag());
    }



    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new GeoItemExtensions<QixiaoItem>(this::getUniqueId));
    }

    protected String getUniqueId() {
        return unsheathed ? "unsheathed_qixiao" : "qixiao";
    }

    public static Map<Item, Item> getTransformations() {
        if (transfomations == null) {
            transfomations = new HashMap<>();
            transfomations.put(ModItems.COMMON_QIXIAO.get(), ModItems.COMMON_UNSHEATHED_QIXIAO.get());
            transfomations.put(ModItems.COMMON_UNSHEATHED_QIXIAO.get(), ModItems.COMMON_QIXIAO.get());
            transfomations.put(ModItems.RARE_QIXIAO.get(), ModItems.RARE_UNSHEATHED_QIXIAO.get());
            transfomations.put(ModItems.RARE_UNSHEATHED_QIXIAO.get(), ModItems.RARE_QIXIAO.get());
            transfomations.put(ModItems.MYTHIC_QIXIAO.get(), ModItems.MYTHIC_UNSHEATHED_QIXIAO.get());
            transfomations.put(ModItems.MYTHIC_UNSHEATHED_QIXIAO.get(), ModItems.MYTHIC_QIXIAO.get());
        }
        return transfomations;
    }



    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {

    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
        pTooltipComponents.add(Component.translatable("item.ji_yue_boss." + getUniqueId() + ".tooltip").withStyle(ChatFormatting.GRAY));
    }



    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int pSlotId, boolean pIsSelected) {
        super.inventoryTick(stack, level, entity, pSlotId, pIsSelected);
        // 隐藏属性词条（只对出鞘状态隐藏，避免显示攻速-2.8）
        if (this.unsheathed) {
            stack.hideTooltipPart(ItemStack.TooltipPart.MODIFIERS);
        }
        // 设置无限耐久
        stack.getOrCreateTag().putBoolean("Unbreakable", true);

        if (!isUnsheathed() && entity instanceof LivingEntity living && (pIsSelected || living.getOffhandItem() == stack)) {
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 21, quality.getSpeedAmplifier()));
        }
    }

    public enum Quality {
        COMMON(Tiers.IRON, 1, 3.0f, 0),
        RARE(Tiers.DIAMOND, 2, 4.0f, 1),
        MYTHIC(Tiers.NETHERITE, 3, 5.0f, 2);

        private final Tier tier;
        private final int attackDamage;
        private final float qiXiaoDamage;
        private final int speedAmplifier;

        Quality(Tier tier, int attackDamage, float qiXiaoDamage, int speedAmplifier) {
            this.tier = tier;
            this.attackDamage = attackDamage;
            this.qiXiaoDamage = qiXiaoDamage;
            this.speedAmplifier = speedAmplifier;
        }

        public Tier getTier() {
            return tier;
        }

        public int getAttackDamage() {
            return attackDamage;
        }

        public float getQiXiaoDamage() {
            return qiXiaoDamage;
        }

        public int getSpeedAmplifier() {
            return speedAmplifier;
        }
    }

    private static boolean tryParry(ServerLevel level, Player player, Quality quality) {
        ParryWindow window = PARRY_WINDOWS.get(player.getUUID());
        long now = level.getGameTime();
        if (window != null && now <= window.expireTick) {
            LivingEntity attacker = window.getAttacker();
            PARRY_WINDOWS.remove(player.getUUID());
            if (attacker == null || !attacker.isAlive()) {
                return false;
            }
            // 事后弹反：回滚生命值至受击前
            if (window.preHealth >= 0) {
                player.setHealth(Math.max(0.0f, Math.min(player.getMaxHealth(), window.preHealth)));
            }
            // 反击与击退（增大距离）
            double dx = player.getX() - attacker.getX();
            double dz = player.getZ() - attacker.getZ();
            attacker.knockback(2.6, dx, dz);
            attacker.hurtMarked = true;
            attacker.hurt(player.damageSources().playerAttack(player), switch (quality) {
                case COMMON -> 5.0f;
                case RARE -> 7.0f;
                case MYTHIC -> 10.0f;
            });
            return true;
        }
        return false;
    }

    @SubscribeEvent
    public static void onHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        Entity src = event.getSource().getEntity();
        if (!(src instanceof LivingEntity living)) return;
        ItemStack main = player.getMainHandItem();
        ItemStack off = player.getOffhandItem();
        if ((main.getItem() instanceof QixiaoItem qm && qm.unsheathed) || (off.getItem() instanceof QixiaoItem qo && qo.unsheathed)) {
            long now = player.level().getGameTime();
            // 先检查是否有提前窗口：若存在则判为成功，直接免伤并反击
            EarlyWindow early = EARLY_WINDOWS.remove(player.getUUID());
            if (early != null && now <= early.expireTick) {
                event.setCanceled(true);
                // 反击与击退（增大距离）
                double dx = player.getX() - living.getX();
                double dz = player.getZ() - living.getZ();
                living.knockback(2.6, dx, dz);
                living.hurtMarked = true;
                living.hurt(player.damageSources().playerAttack(player), (main.getItem() instanceof QixiaoItem iq ? switch (((QixiaoItem) main.getItem()).quality) { case COMMON -> 5.0f; case RARE -> 7.0f; case MYTHIC -> 10.0f; } : 5.0f));
                player.level().playSound(null, player.blockPosition(), ModSounds.BOUNCE_SUCCESS.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
                com.imoonday.ji_yue_boss.util.Utils.playRandomSound(com.imoonday.ji_yue_boss.init.ModSounds.BOUNCE_SUCCESS_SOUNDS, player.level(), player);
                if (player.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK, player.getX(), player.getY() + 1.0, player.getZ(), 15, 0.5, 0.8, 0.5, 0.0);
                }
                // 成功时清理窗口与冷却，确保“成功不受CD限制”
                PARRY_WINDOWS.remove(player.getUUID());
                net.minecraft.world.item.ItemCooldowns cds2 = player.getCooldowns();
                for (QixiaoItem item : UNSHEATHED_ITEMS) {
                    cds2.removeCooldown(item);
                }
                return;
            }
            // 否则记录事后窗口（0.1s = 2ticks），保存受击前生命值用于回滚
            PARRY_WINDOWS.put(player.getUUID(), new ParryWindow(now + 2, living, player.getHealth()));
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;
        if (player == null || player.level().isClientSide) return;
        long now = player.level().getGameTime();
        EarlyWindow early = EARLY_WINDOWS.get(player.getUUID());
        if (early != null && now > early.expireTick) {
            // 提前窗口超时：视为失败，触发失败音并添加8秒冷却
            EARLY_WINDOWS.remove(player.getUUID());
            player.level().playSound(null, player.blockPosition(), ModSounds.BOUNCE.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
            net.minecraft.world.item.ItemCooldowns cds = player.getCooldowns();
            for (QixiaoItem item : UNSHEATHED_ITEMS) {
                cds.addCooldown(item, 20 * 8);
            }
        }
    }

    private record ParryWindow(long expireTick, WeakReference<LivingEntity> ref, float preHealth) {
        private ParryWindow(long expireTick, LivingEntity attacker, float preHealth) {
            this(expireTick, new WeakReference<>(attacker), preHealth);
        }
        private LivingEntity getAttacker() {
            return ref.get();
        }
    }

    private record EarlyWindow(long expireTick) {}
}
