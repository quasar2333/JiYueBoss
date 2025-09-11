package com.imoonday.ji_yue_boss.item;

import com.imoonday.ji_yue_boss.init.ModSounds;
import com.imoonday.ji_yue_boss.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
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

public class SavourItem extends Item implements GeoItem {

    private static final List<SavourItem> ITEMS = new ArrayList<>();
    private static final List<RegistryObject<SoundEvent>> SOUNDS = ModSounds.SAVOUR_SOUNDS;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final Quality quality;

    public SavourItem(Quality quality, Properties pProperties) {
        super(pProperties);
        this.quality = quality;
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
        ITEMS.add(this);
    }

    @Override
    public Component getName(ItemStack pStack) {
        return super.getName(pStack).copy().withStyle(style -> style.withColor(0xFF0000));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            int count = this.quality.getCount();
            Inventory inventory = player.getInventory();
            boolean[] hasCookie = {false};
            inventory.hasAnyMatching(stack -> {
                if (stack.is(Items.COOKIE)) {
                    hasCookie[0] = true;
                    if (stack.getCount() < count) {
                        stack.setCount(count);
                        return true;
                    }
                }
                return false;
            });
            if (!hasCookie[0]) {
                inventory.placeItemBackInInventory(new ItemStack(Items.COOKIE, count));
            }

            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 20 * 5, 4));

            level.playSound(null, player.blockPosition(), ModSounds.SAVOUR.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
            Utils.playRandomSound(SOUNDS, level, player);
            Utils.addParticlesAround(ParticleTypes.CHERRY_LEAVES, (ServerLevel) level, player, 100);
            Utils.addCooldown(player, 20 * 30, ITEMS);
        }
        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
        pTooltipComponents.add(Component.translatable("item.ji_yue_boss.savour.tooltip", this.quality.getCount()).withStyle(ChatFormatting.GRAY));
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new GeoItemExtensions<SavourItem>("savour"));
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
        COMMON(16),
        RARE(32),
        MYTHIC(64);

        private final int count;

        Quality(int count) {
            this.count = count;
        }

        public int getCount() {
            return count;
        }
    }
}
