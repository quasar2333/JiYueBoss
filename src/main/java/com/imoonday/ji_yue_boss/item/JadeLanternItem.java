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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class JadeLanternItem extends Item implements GeoItem {

    private static final List<JadeLanternItem> ITEMS = new ArrayList<>();
    private static final List<RegistryObject<SoundEvent>> SOUNDS = ModSounds.JADE_LANTERN_SOUNDS;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final Quality quality;

    public JadeLanternItem(Quality quality, Properties properties) {
        super(properties);
        this.quality = quality;
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
        ITEMS.add(this);
    }

    @Override
    public Component getName(ItemStack pStack) {
        return super.getName(pStack).copy().withStyle(style -> style.withColor(0x4caf50));
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
        pTooltipComponents.add(Component.translatable("item.ji_yue_boss.jade_lantern.tooltip", quality.getLength(), quality.getHealAmount() / 2).withStyle(ChatFormatting.GRAY));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player entity, InteractionHand hand) {
        if (level instanceof ServerLevel serverLevel) {
            entity.removeAllEffects();
            int healAmount = quality.getHealAmount();
            int length = quality.getRightClickRadius();
            List<Player> players = level.getNearbyPlayers(TargetingConditions.forNonCombat(), null, AABB.ofSize(entity.position(), length, length, length));
            for (Player player : players) {
                player.heal(healAmount);
            }
            level.playSound(null, entity.blockPosition(), ModSounds.JADE_LANTERN.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
            Utils.playRandomSound(SOUNDS, level, entity);
            for (Vec3 point : getPoints(entity)) {
                serverLevel.sendParticles(ParticleTypes.ENCHANTED_HIT, point.x, point.y, point.z, 1, 0.0, 0.0, 0.0, 0.0);
            }
            Utils.addCooldown(entity, 20 * 15, ITEMS);
        }
        return InteractionResultHolder.success(entity.getItemInHand(hand));
    }

    // 左键范围攻击：以瞄准目标为中心，生成粒子并造成范围伤害（冷却3秒）
    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
        if (!(entity.level() instanceof ServerLevel serverLevel)) {
            return false;
        }
        if (!(entity instanceof Player player)) {
            return false;
        }
        if (player.getCooldowns().isOnCooldown(this)) {
            return false;
        }
        // 射线寻找目标
        double range = 16.0;
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 end = eye.add(look.scale(range));
        LivingEntity target = serverLevel.getNearestEntity(serverLevel.getEntitiesOfClass(LivingEntity.class, new AABB(eye, end).inflate(1.0)), TargetingConditions.forCombat().ignoreInvisibilityTesting(), player, eye.x, eye.y, eye.z);
        if (target == null) {
            return false;
        }
        int radius = Math.max(1, quality.getLength() - 2);
        double r = Math.max(1, radius);
        AABB area = target.getBoundingBox().inflate(r);
        for (LivingEntity mob : serverLevel.getEntitiesOfClass(LivingEntity.class, area)) {
            if (mob != player && mob.isAlive()) {
                mob.hurt(player.damageSources().playerAttack(player), quality.getLeftClickDamage());
            }
        }
        // 粒子效果
        for (int i = 0; i < 50; i++) {
            Vec3 p = target.position().add((player.getRandom().nextDouble() - 0.5) * r * 2, 0.1 + player.getRandom().nextDouble(), (player.getRandom().nextDouble() - 0.5) * r * 2);
            serverLevel.sendParticles(ParticleTypes.CRIMSON_SPORE, p.x, p.y, p.z, 1, 0.0, 0.0, 0.0, 0.0);
        }
        Utils.playRandomSound(SOUNDS, serverLevel, player);
        Utils.addCooldown(player, 20 * 3, this);
        return true;
    }

    private List<Vec3> getPoints(LivingEntity entity) {
        int length = quality.getLength();
        Vec3 center = entity.position().add(0, entity.getBbHeight() / 2.0, 0);
        List<Vec3> points = new ArrayList<>();
        for (double i = 0; i <= length; i += 0.25) {
            for (double j = 0; j <= length; j += 0.25) {
                if (i == 0 || i == length || j == 0 || j == length) {
                    points.add(center.add(i - length / 2.0, 0, j - length / 2.0));
                }
            }
        }
        return points;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new GeoItemExtensions<JadeLanternItem>("jade_lantern"));
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
        COMMON(3, 8, 3, 2),
        RARE(4, 12, 4, 3),
        MYTHIC(5, 16, 5, 3);

        private final int length;
        private final int healAmount;
        private final int leftClickDamage;
        private final int rightClickRadius;

        Quality(int length, int healAmount, int leftClickDamage, int rightClickRadius) {
            this.length = length;
            this.healAmount = healAmount;
            this.leftClickDamage = leftClickDamage;
            this.rightClickRadius = rightClickRadius;
        }

        public int getLength() {
            return length;
        }

        public int getHealAmount() {
            return healAmount;
        }

        public int getLeftClickDamage() {
            return leftClickDamage;
        }

        public int getRightClickRadius() {
            return rightClickRadius;
        }
    }
}
