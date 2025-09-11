package com.imoonday.ji_yue_boss.item;
import com.imoonday.ji_yue_boss.init.ModSounds;
import com.imoonday.ji_yue_boss.util.CelestisynthUtils;
import com.imoonday.ji_yue_boss.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
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
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class FoldingFanItem extends Item implements GeoItem {

    private static final List<FoldingFanItem> ITEMS = new ArrayList<>();
    private static final List<RegistryObject<SoundEvent>> SOUNDS = ModSounds.FOLDING_FAN_SOUNDS;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final Quality quality;

    public FoldingFanItem(Quality quality, Properties pProperties) {
        super(pProperties);
        this.quality = quality;
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
        ITEMS.add(this);
    }

    @Override
    public Component getName(ItemStack pStack) {
        return super.getName(pStack).copy().withStyle(style -> style.withColor(0x5732fa));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        Object attack = getAttack(player, stack);
        CelestisynthUtils.startUsing(level, hand, stack, attack, 1);
        Utils.addCooldown(player, this.quality.getCooldown(), ITEMS);

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    public static Object getAttack(Player player, ItemStack itemStack) {
        return CelestisynthUtils.createBreezebreakerWhirlwindAttack(player, itemStack);
    }

    @Override
    public void inventoryTick(ItemStack itemStack, Level level, Entity entity, int itemSlot, boolean isSelected) {
        super.inventoryTick(itemStack, level, entity, itemSlot, isSelected);
        if (entity instanceof Player player) {
            Object attack = getAttack(player, itemStack);
            CelestisynthUtils.tickSkill(itemStack, attack);
            if (CelestisynthUtils.getTimerProgress(attack) == 10) {
                Utils.playRandomSound(SOUNDS, level, player);
                Vec3 lookAngle = Vec3.directionFromRotation(0, player.getYRot());
                player.setDeltaMovement(lookAngle.reverse());
            }
        }
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack) {
        return 72000;
    }

    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new GeoItemExtensions<FoldingFanItem>("folding_fan"));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {

    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
        pTooltipComponents.add(Component.translatable("item.ji_yue_boss.folding_fan.tooltip").withStyle(ChatFormatting.GRAY));
    }

    public Quality getQuality() {
        return quality;
    }

    public enum Quality {
        COMMON(0.3, 20 * 10) {
            @Override
            public void addSpecialEffect(LivingEntity entity) {
                super.addSpecialEffect(entity);
                entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 5));
            }
        },
        RARE(0.45, 20 * 8) {
            @Override
            public void addSpecialEffect(LivingEntity entity) {
                super.addSpecialEffect(entity);
                entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 5, 1));
            }
        },
        MYTHIC(0.675, 20 * 7) {
            @Override
            public void addSpecialEffect(LivingEntity entity) {
                super.addSpecialEffect(entity);
                entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 5));
                entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20 * 5, 1));
                entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 20 * 7));
            }
        };

        private final double strength;
        private final int cooldown;

        Quality(double strength, int cooldown) {
            this.strength = strength;
            this.cooldown = cooldown;
        }

        public double getStrength() {
            return strength;
        }

        public int getCooldown() {
            return cooldown;
        }

        public void addSpecialEffect(LivingEntity entity) {

        }
    }
}
