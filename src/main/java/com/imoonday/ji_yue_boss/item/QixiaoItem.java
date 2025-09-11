package com.imoonday.ji_yue_boss.item;

import com.imoonday.ji_yue_boss.init.ModItems;
import com.imoonday.ji_yue_boss.init.ModSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.ForgeTier;
import org.jetbrains.annotations.Nullable;

import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class QixiaoItem extends SwordItem implements GeoItem {

    private static Map<Item, Item> transfomations = null;
    private final boolean unsheathed;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final Quality quality;

    public QixiaoItem(Quality quality, Properties pProperties, boolean unsheathed) {
        super(quality.getTier(), quality.getAttackDamage(), -2.4f, pProperties);
        this.quality = quality;
        this.unsheathed = unsheathed;
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
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
        if (!isUnsheathed() && entity instanceof LivingEntity living && (pIsSelected || living.getOffhandItem() == stack)) {
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 21, quality.getSpeedAmplifier()));
        }
    }

    public enum Quality {
        COMMON(Tiers.IRON, 3, 3.0f, 0),
        RARE(Tiers.DIAMOND, 4, 4.0f, 1),
        MYTHIC(Tiers.NETHERITE, 5, 5.0f, 2);

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
}
