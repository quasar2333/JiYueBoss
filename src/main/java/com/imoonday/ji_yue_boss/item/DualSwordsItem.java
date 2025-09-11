package com.imoonday.ji_yue_boss.item;

import com.imoonday.ji_yue_boss.init.ModSounds;
import com.imoonday.ji_yue_boss.util.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.*;
import java.util.function.Consumer;

/**
 * 稚晚的双剑
 */
public class DualSwordsItem extends SwordItem implements GeoItem {

    private static final List<DualSwordsItem> ITEMS = new ArrayList<>();
    private static final List<RegistryObject<SoundEvent>> VOICE_SOUNDS = ModSounds.DUAL_SWORDS_SOUNDS;
    private static final List<RegistryObject<SoundEvent>> SFX_SOUNDS = ModSounds.TELEPORT_SOUNDS;
    private static final String NBT_START_POS = "DualSwordsStartPos";
    private static final String NBT_START_TICK = "DualSwordsStartTick";
    private static final String NBT_DASHING = "DualSwordsDashing";
    private static final String NBT_DASH_END = "DualSwordsDashEnd";
    private static final String NBT_DASH_DIR = "DualSwordsDashDir"; // 保存突进方向

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final Quality quality;

    public DualSwordsItem(Quality quality, Properties properties) {
        super(quality.getTier(), quality.getAttackDamage(), -2.4f, properties);
        this.quality = quality;
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
        ITEMS.add(this);
    }

    @Override
    public Component getName(ItemStack pStack) {
        return super.getName(pStack).copy().withStyle(style -> style.withColor(0xff9ef7));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            if (!isDualWielding(player, stack)) {
                return InteractionResultHolder.pass(stack);
            }

            CompoundTag tag = stack.getOrCreateTag();
            long gameTime = level.getGameTime();

            if (hasStart(tag)) {
                long startTick = tag.getLong(NBT_START_TICK);
                if (gameTime - startTick <= 20L * 5) { // 5秒内返回
                    Vec3 start = readVec3(tag.getCompound(NBT_START_POS));
                    // 返回使用瞬移（需求：回退仍为瞬移）
                    player.teleportTo(start.x, start.y, start.z);
                    playSounds(level, player);
                    dealAreaDamage(level, start, quality.getDamage(), player);
                    clearStart(tag);
                    Utils.addCooldown(player, 20 * 10, ITEMS);
                    return InteractionResultHolder.sidedSuccess(stack, false);
                } else {
                    // 超时则清除记录，按前进处理
                    clearStart(tag);
                }
            }

            // 前进：改为突进（正常移动，不能穿墙）
            Vec3 look = player.getLookAngle().normalize();
            double distance = quality.getDashDistance();
            Vec3 start = player.position();
            // 记录起点与时间
            tag.put(NBT_START_POS, writeVec3(start));
            tag.putLong(NBT_START_TICK, gameTime);
            // 写入突进状态（方向与终点）
            tag.putBoolean(NBT_DASHING, true);
            tag.put(NBT_DASH_DIR, writeVec3(look));
            tag.putDouble(NBT_DASH_END, distance);
            // 突进前自动跳跃：给予向上初始速度，避免地面摩擦影响突进
            Vec3 currentMotion = player.getDeltaMovement();
            player.setDeltaMovement(currentMotion.x, Math.max(currentMotion.y, 0.42), currentMotion.z); // 0.42 是标准跳跃速度
            player.hurtMarked = true;
            // 播放语音与特效立即生效
            playSounds(level, player);
            // 自身增益（仅前进触发时应用）
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, quality.getBuffDurationTicks(), quality.getSpeedAmplifier()));
            player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, quality.getBuffDurationTicks(), 0));
            return InteractionResultHolder.sidedSuccess(stack, false);
        }
        return InteractionResultHolder.consume(stack);
    }

    private static boolean isDualWielding(Player player, ItemStack current) {
        ItemStack main = player.getMainHandItem();
        ItemStack off = player.getOffhandItem();
        return main.getItem() instanceof DualSwordsItem && off.getItem() instanceof DualSwordsItem;
    }

    private void dealAreaDamage(Level level, Vec3 center, float damage, Player source) {
        AABB box = AABB.unitCubeFromLowerCorner(center).inflate(1.5);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, box)) {
            if (target.isAlive() && target != source) {
                target.hurt(source.damageSources().playerAttack(source), damage);
            }
        }
    }

    public static boolean isDashing(Player player) {
        ItemStack main = player.getMainHandItem();
        ItemStack off = player.getOffhandItem();
        // 仅当双持并且任一持有的双剑记录了突进状态
        if (main.getItem() instanceof DualSwordsItem || off.getItem() instanceof DualSwordsItem) {
            CompoundTag tagMain = main.getOrCreateTag();
            CompoundTag tagOff = off.getOrCreateTag();
            return tagMain.getBoolean(NBT_DASHING) || tagOff.getBoolean(NBT_DASHING);
        }
        return false;
    }

    public static void tickDash(Player player) {
        // 优先从主手读取状态，否则从副手读取
        ItemStack main = player.getMainHandItem();
        ItemStack off = player.getOffhandItem();
        ItemStack src = ItemStack.EMPTY;
        if (main.getItem() instanceof DualSwordsItem && main.getOrCreateTag().getBoolean(NBT_DASHING)) src = main;
        else if (off.getItem() instanceof DualSwordsItem && off.getOrCreateTag().getBoolean(NBT_DASHING)) src = off;
        if (src.isEmpty()) return;

        CompoundTag tag = src.getOrCreateTag();
        Vec3 dir = readVec3(tag.getCompound(NBT_DASH_DIR));
        double remaining = tag.getDouble(NBT_DASH_END);
        if (remaining <= 0) {
            // 结束：清除状态并在当前位置造成到达伤害
            clearDash(tag);
            if (!player.level().isClientSide) {
                if (src.getItem() instanceof DualSwordsItem item) {
                    item.dealAreaDamage(player.level(), player.position(), item.quality.getDamage(), player);
                }
            }
            return;
        }

        // 每tick推进的距离（快速但不瞬移）：一次推进 0.6 格，5~9 tick 完成
        double step = Math.min(0.6, remaining);
        Vec3 next = player.position().add(dir.scale(step));
        // 防掉虚空：若下一步低于世界最小高度则终止
        if (next.y <= player.level().getMinBuildHeight() + 1) {
            clearDash(tag);
            return;
        }
        // 碰撞检测：如果 next 位置会窒息则提前停下
        if (player.level().collidesWithSuffocatingBlock(player, player.getBoundingBox().move(next.subtract(player.position())))) {
            clearDash(tag);
            return;
        }

        // 设置移动速度（正常移动）
        Vec3 motion = dir.scale(step * 3.5); // 放大为速度向量，保证本tick能前进 step
        player.setDeltaMovement(motion.x, player.getDeltaMovement().y, motion.z);
        player.hurtMarked = true;
        // 路径伤害：对当前位置附近的敌人造成伤害（小范围）
        if (!player.level().isClientSide) {
            if (src.getItem() instanceof DualSwordsItem item) {
                item.dealAreaDamage(player.level(), player.position(), item.quality.getDamage() * 0.6f, player);
            }
        }

        // 更新剩余距离
        tag.putDouble(NBT_DASH_END, remaining - step);
    }

    private static void clearDash(CompoundTag tag) {
        tag.remove(NBT_DASH_DIR);
        tag.remove(NBT_DASH_END);
        tag.putBoolean(NBT_DASHING, false);
    }


    private void playSounds(Level level, Player player) {
        Utils.playRandomSound(VOICE_SOUNDS, level, player);
        Utils.playRandomSound(SFX_SOUNDS, level, player);
    }

    private static boolean hasStart(CompoundTag tag) {
        return tag.contains(NBT_START_POS) && tag.contains(NBT_START_TICK);
    }

    private static void clearStart(CompoundTag tag) {
        tag.remove(NBT_START_POS);
        tag.remove(NBT_START_TICK);
    }

    private static CompoundTag writeVec3(Vec3 vec) {
        CompoundTag t = new CompoundTag();
        t.putDouble("x", vec.x);
        t.putDouble("y", vec.y);
        t.putDouble("z", vec.z);
        return t;
    }

    private static Vec3 readVec3(CompoundTag t) {
        return new Vec3(t.getDouble("x"), t.getDouble("y"), t.getDouble("z"));
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
        pTooltipComponents.add(Component.translatable("item.ji_yue_boss.dual_swords.tooltip").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new GeoItemExtensions<DualSwordsItem>("dual_swords"));
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
        COMMON(Tiers.WOOD, 3, 3, 20 * 5, 1),
        RARE(Tiers.STONE, 4, 4, 20 * 5, 2),
        MYTHIC(Tiers.IRON, 5, 5, 20 * 7, 3);

        private final Tier tier;
        private final int attackDamage;
        private final float damage;
        private final int buffDurationTicks;
        private final int speedAmplifier;

        Quality(Tier tier, int attackDamage, float damage, int buffDurationTicks, int speedAmplifier) {
            this.tier = tier;
            this.attackDamage = attackDamage;
            this.damage = damage;
            this.buffDurationTicks = buffDurationTicks;
            this.speedAmplifier = speedAmplifier - 1; // amplifier 从0开始
        }

        public Tier getTier() {
            return tier;
        }

        public int getAttackDamage() {
            return attackDamage;
        }

        public float getDamage() {
            return damage;
        }

        public int getBuffDurationTicks() {
            return buffDurationTicks;
        }

        public int getSpeedAmplifier() {
            return speedAmplifier;
        }

        public double getDashDistance() {
            return switch (this) {
                case COMMON -> 3.0;
                case RARE -> 4.0;
                case MYTHIC -> 5.0;
            };
        }
    }
}


