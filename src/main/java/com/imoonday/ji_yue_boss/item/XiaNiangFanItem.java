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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
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
    private static final java.util.UUID FAN_AS_UUID = java.util.UUID.fromString("0a2a0a6a-8c5c-46c7-9e1f-7a0c0b9f8f21");
    private static final java.util.UUID FAN_AD_UUID = java.util.UUID.fromString("6c2b2c1e-9b3b-485f-9b34-6a0a5b2f9e21");


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
    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        if (slot == EquipmentSlot.MAINHAND) {
            ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
            builder.putAll(super.getDefaultAttributeModifiers(slot));
            // 需求：基础伤害为 3/4/6（总伤害）。玩家基础为 1 点，因此加成为 总伤害-1。
            double total = this.quality.getWeaponDamage();
            double add = Math.max(0.0, total - 1.0);
            if (add != 0.0) {
                builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(FAN_AD_UUID, "xia_niang_fan_attack_damage", add, AttributeModifier.Operation.ADDITION));
            }
            return builder.build();
        }
        return super.getDefaultAttributeModifiers(slot);
    }
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        // 隐藏属性词条（保持仅 COMMON 隐藏）
        if (this.quality == Quality.COMMON) {
            stack.hideTooltipPart(ItemStack.TooltipPart.MODIFIERS);
        }
        // 仅在持握时，对玩家施加临时攻速修正（-3.0），不写入物品本身，避免词条显示
        if (!(entity instanceof LivingEntity living)) return;
        boolean holding = isSelected || living.getOffhandItem() == stack;
        AttributeInstance inst = living.getAttribute(Attributes.ATTACK_SPEED);
        if (inst == null) return;
        AttributeModifier existing = inst.getModifier(FAN_AS_UUID);
        if (holding) {
            if (existing == null) {
                inst.addTransientModifier(new AttributeModifier(FAN_AS_UUID, "xia_niang_fan_attack_speed", -3.0, AttributeModifier.Operation.ADDITION));
            }
        } else {
            if (existing != null) {
                inst.removeModifier(existing);
            }
        }
    }


    public enum Quality {
        COMMON(20 * 14, 3, 3),   // 存在14秒（7+7），九尾攻击3点，武器伤害+3
        RARE(20 * 17, 4, 4),     // 存在17秒（10+7），九尾攻击4点，武器伤害+4
        MYTHIC(20 * 22, 5, 6);   // 存在22秒（15+7），九尾攻击5点，武器伤害+6

        private final int lifespanTicks;
        private final int attackDamage;
        private final int weaponDamage;

        Quality(int lifespanTicks, int attackDamage, int weaponDamage) {
            this.lifespanTicks = lifespanTicks;
            this.attackDamage = attackDamage;
            this.weaponDamage = weaponDamage;
        }

        public int getLifespanTicks() {
            return lifespanTicks;
        }

        public int getAttackDamage() {
            return attackDamage;
        }

        public double getWeaponDamage() {
            return weaponDamage;
        }
    }
}

