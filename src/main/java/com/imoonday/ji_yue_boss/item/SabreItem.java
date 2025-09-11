package com.imoonday.ji_yue_boss.item;

import com.imoonday.ji_yue_boss.entity.Sabre;
import com.imoonday.ji_yue_boss.init.ModSounds;
import com.imoonday.ji_yue_boss.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.ForgeTier;
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

public class SabreItem extends SwordItem implements GeoItem {

    private static final List<SabreItem> ITEMS = new ArrayList<>();
    private static final List<RegistryObject<SoundEvent>> SOUNDS = ModSounds.SWORD_SOUNDS;
    public static final ForgeTier TIER = new ForgeTier(0, 0, 2.0f, 0.0f, 15, BlockTags.SWORD_EFFICIENT, () -> Ingredient.EMPTY);
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final Quality quality;

    public SabreItem(Tier pTier, int pAttackDamageModifier, float pAttackSpeedModifier, Quality quality, Properties pProperties) {
        super(pTier, pAttackDamageModifier, pAttackSpeedModifier, pProperties);
        this.quality = quality;
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
        ITEMS.add(this);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        HitResult hitResult = ProjectileUtil.getHitResultOnViewVector(player, entity -> !entity.isSpectator() && entity.isPickable(), 10.0f);
        if (hitResult instanceof EntityHitResult result) {
            if (!level.isClientSide) {
                Utils.playRandomNotifySound(SOUNDS, player);
                Vec3 position = result.getEntity().getEyePosition();
                Sabre sword = new Sabre(player, level);
                sword.setItem(stack);
                sword.setPos(position.relative(Direction.UP, 7.0));
                level.addFreshEntity(sword);
                Utils.addCooldown(player, this.quality.getCooldown(), ITEMS);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public Component getName(ItemStack pStack) {
        return super.getName(pStack).copy().withStyle(s -> s.withColor(0x89D9D9));
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
        pTooltipComponents.add(Component.translatable("item.ji_yue_boss.sabre.tooltip").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new GeoItemExtensions<SabreItem>("sabre"));
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {

    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    public Quality getQuality() {
        return quality;
    }

    public enum Quality {
        COMMON(4.0f, 20 * 60),
        RARE(6.0f, 20 * 45),
        MYTHIC(8.0f, 20 * 30);

        private final float damage;
        private final int cooldown;

        Quality(float damage, int cooldown) {
            this.damage = damage;
            this.cooldown = cooldown;
        }

        public float getDamage() {
            return damage;
        }

        public int getCooldown() {
            return cooldown;
        }
    }
}
