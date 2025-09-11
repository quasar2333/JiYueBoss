package com.imoonday.ji_yue_boss.item;

import com.imoonday.ji_yue_boss.entity.NineTailedFox;
import com.imoonday.ji_yue_boss.init.ModEntities;
import com.imoonday.ji_yue_boss.init.ModSounds;
import com.imoonday.ji_yue_boss.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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

/**
 * 瑕娘的折扇
 */
public class XiaNiangFanItem extends Item implements GeoItem {

    private static final List<XiaNiangFanItem> ITEMS = new ArrayList<>();
    private static final List<RegistryObject<SoundEvent>> SOUNDS = ModSounds.XIA_NIANG_FAN_SOUNDS;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final Quality quality;

    public XiaNiangFanItem(Quality quality, Properties properties) {
        super(properties);
        this.quality = quality;
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
        ITEMS.add(this);
    }

    @Override
    public Component getName(ItemStack pStack) {
        return super.getName(pStack).copy().withStyle(style -> style.withColor(0xeb0568));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            // 召唤九尾
            NineTailedFox fox = new NineTailedFox(ModEntities.NINE_TAILED_FOX.get(), level);
            fox.setPos(player.getX(), player.getY(), player.getZ());
            fox.setOwner(player);

            // 调试信息：打印品质和存活时间
            System.out.println("召唤九尾狐 - 品质: " + quality.name() + ", 存活时间(ticks): " + quality.getLifespanTicks() + ", 存活时间(秒): " + (quality.getLifespanTicks() / 20.0));

            fox.setLifespan(quality.getLifespanTicks());
            fox.setAttackDamage(quality.getAttackDamage());
            level.addFreshEntity(fox);
            
            // 播放语音
            Utils.playRandomSound(SOUNDS, level, player);
            
            // 添加冷却时间（30秒）
            Utils.addCooldown(player, 20 * 30, ITEMS);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
        pTooltipComponents.add(Component.translatable("item.ji_yue_boss.xia_niang_fan.tooltip").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new GeoItemExtensions<XiaNiangFanItem>("xia_niang_fan"));
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

    public enum Quality {
        COMMON(20 * 14, 3),   // 存在14秒（7+7），攻击3点
        RARE(20 * 17, 4),     // 存在17秒（10+7），攻击4点
        MYTHIC(20 * 22, 5);   // 存在22秒（15+7），攻击5点

        private final int lifespanTicks;
        private final int attackDamage;

        Quality(int lifespanTicks, int attackDamage) {
            this.lifespanTicks = lifespanTicks;
            this.attackDamage = attackDamage;
        }

        public int getLifespanTicks() {
            return lifespanTicks;
        }

        public int getAttackDamage() {
            return attackDamage;
        }
    }
}

