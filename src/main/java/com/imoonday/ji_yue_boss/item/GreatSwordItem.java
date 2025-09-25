package com.imoonday.ji_yue_boss.item;

import com.imoonday.ji_yue_boss.init.ModSounds;
import com.imoonday.ji_yue_boss.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.Entity;

import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 玄溟的重剑
 */
public class GreatSwordItem extends SwordItem implements GeoItem {

    private static final List<GreatSwordItem> ITEMS = new ArrayList<>();
    private static final List<RegistryObject<SoundEvent>> SOUNDS = ModSounds.GREAT_SWORD_SOUNDS;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final Quality quality;

    public GreatSwordItem(Quality quality, Properties properties) {
        super(quality.getTier(), quality.getAttackDamage(), (quality == Quality.COMMON ? -3.2f : -2.4f), properties);
        this.quality = quality;
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
        ITEMS.add(this);
    }

    @Override
    public Component getName(ItemStack pStack) {
        return super.getName(pStack).copy().withStyle(style -> style.withColor(0x6705f0));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            // 给予自身抗性提升和伤害吸收
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * 10, quality.getResistanceAmplifier()));
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 20 * 10, quality.getAbsorptionAmplifier()));

            // 播放语音
            Utils.playRandomSound(SOUNDS, level, player);

            // 添加冷却时间（25秒）
            Utils.addCooldown(player, 20 * 25, ITEMS);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
        pTooltipComponents.add(Component.translatable("item.ji_yue_boss.great_sword.tooltip").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new GeoItemExtensions<GreatSwordItem>("great_sword"));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {

    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    public Quality getQuality() {
        return quality;
    }
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (this.quality == Quality.COMMON) {
            stack.hideTooltipPart(ItemStack.TooltipPart.MODIFIERS);
        }
        // 无限耐久
        stack.getOrCreateTag().putBoolean("Unbreakable", true);

    }


    public enum Quality {
        COMMON(Tiers.IRON, 3, 0, 2),      // 抗性提升1，伤害吸收3
        RARE(Tiers.DIAMOND, 4, 1, 3),     // 抗性提升2，伤害吸收4
        MYTHIC(Tiers.NETHERITE, 5, 2, 4); // 抗性提升3，伤害吸收5

        private final Tier tier;
        private final int attackDamage;
        private final int resistanceAmplifier;
        private final int absorptionAmplifier;

        Quality(Tier tier, int attackDamage, int resistanceAmplifier, int absorptionAmplifier) {
            this.tier = tier;
            this.attackDamage = attackDamage;
            this.resistanceAmplifier = resistanceAmplifier;
            this.absorptionAmplifier = absorptionAmplifier;
        }

        public Tier getTier() {
            return tier;
        }

        public int getAttackDamage() {
            return attackDamage;
        }

        public int getResistanceAmplifier() {
            return resistanceAmplifier;
        }

        public int getAbsorptionAmplifier() {
            return absorptionAmplifier;
        }
    }
}

