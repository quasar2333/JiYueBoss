package com.imoonday.ji_yue_boss.item;

import com.imoonday.ji_yue_boss.client.sound.GourdSoundHelper;
import com.imoonday.ji_yue_boss.mixin.MobEffectInstanceAccessor;
import com.imoonday.ji_yue_boss.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class GourdItem extends Item implements GeoItem {

    private static final List<GourdItem> ITEMS = new ArrayList<>();
    private static final MobEffect[] EFFECTS = {
        // 正面效果
        MobEffects.DAMAGE_BOOST, MobEffects.MOVEMENT_SPEED, MobEffects.REGENERATION, MobEffects.DAMAGE_RESISTANCE, MobEffects.DIG_SPEED,
        // 负面效果
        MobEffects.BLINDNESS, MobEffects.CONFUSION, MobEffects.DIG_SLOWDOWN, MobEffects.POISON, MobEffects.WEAKNESS
    };
    private static final String NBT_RECOVERY_TICKS = "RecoveryTicks";
    private static final int RECOVERY_TICKS = 20 * 30;
    private static final float HEAL_AMOUNT = 6.0f;
    private static final int EFFECT_DURATION = 20 * 7;
    private static final int USE_COOLDOWN = 20 * 5;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public GourdItem(Properties pProperties) {
        super(pProperties);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
        ITEMS.add(this);
    }

    @Override
    public Component getName(ItemStack pStack) {
        return super.getName(pStack).copy().withStyle(style -> style.withColor(0xF8A2AC));
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
        pTooltipComponents.add(Component.translatable("item.ji_yue_boss.gourd.tooltip").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public int getUseDuration(ItemStack pStack) {
        return 32;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack pStack) {
        return UseAnim.DRINK;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack pStack, Level pLevel, LivingEntity pLivingEntity) {
        super.finishUsingItem(pStack, pLevel, pLivingEntity);
        boolean instabuild = !(pLivingEntity instanceof Player player) || player.getAbilities().instabuild;

        if (instabuild || pStack.getDamageValue() < pStack.getMaxDamage()) {
            if (!instabuild) {
                pStack.setDamageValue(pStack.getDamageValue() + 1);
            }
            if (!pLevel.isClientSide) {
                applyEffect(pLivingEntity);
            }
        }
        Utils.addCooldown(pLivingEntity, USE_COOLDOWN, ITEMS);
        return pStack;
    }

    public void applyEffect(LivingEntity entity) {
        entity.heal(HEAL_AMOUNT);

        MobEffect effect = EFFECTS[entity.getRandom().nextInt(EFFECTS.length)];
        MobEffectInstance instance = entity.getEffect(effect);
        if (instance != null && instance.getAmplifier() == 0) {
            ((MobEffectInstanceAccessor) instance).setDuration(instance.getDuration() + EFFECT_DURATION);
        } else {
            entity.addEffect(new MobEffectInstance(effect, EFFECT_DURATION));
        }
    }

    @Override
    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
        super.inventoryTick(pStack, pLevel, pEntity, pSlotId, pIsSelected);
        if (!pLevel.isClientSide && pStack.getDamageValue() > 0) {
            CompoundTag tag = pStack.getOrCreateTag();
            int newTicks = tag.getInt(NBT_RECOVERY_TICKS) + 1;
            tag.putInt(NBT_RECOVERY_TICKS, newTicks);
            if (newTicks >= RECOVERY_TICKS) {
                pStack.setDamageValue(pStack.getDamageValue() - 1);
                tag.putInt(NBT_RECOVERY_TICKS, 0);
            }
        }
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new GeoItemExtensions<GourdItem>("gourd"));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {

    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        if (level.isClientSide) {
            if (isValid(player, stack)) {
                GourdSoundHelper.playUseSound(player);
            } else {
                GourdSoundHelper.playEmptySound(player);
            }
        }
        return InteractionResultHolder.consume(stack);
    }

    public static boolean isValid(LivingEntity entity, ItemStack stack) {
        return !(entity instanceof Player player) || player.getAbilities().instabuild || stack.getDamageValue() < stack.getMaxDamage();
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged;
    }
}
