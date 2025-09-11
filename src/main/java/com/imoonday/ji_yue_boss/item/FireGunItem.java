package com.imoonday.ji_yue_boss.item;

import com.imoonday.ji_yue_boss.init.ModSounds;
import com.imoonday.ji_yue_boss.util.Utils;
import net.mcreator.warriorsofpastepoch.entity.PistolBulletEntity;
import net.mcreator.warriorsofpastepoch.init.WarriorsofpastepochModParticleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow.Pickup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class FireGunItem extends Item implements GeoItem {

    private static final List<FireGunItem> ITEMS = new ArrayList<>();
    private static final List<RegistryObject<SoundEvent>> SOUNDS = ModSounds.FIRE_GUN_SOUNDS;
    private static final int RELOAD_TIME = 190;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final Quality quality;

    public FireGunItem(Quality quality, Properties properties) {
        super(properties);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
        this.quality = quality;
        ITEMS.add(this);
    }

    @Override
    public Component getName(ItemStack pStack) {
        return super.getName(pStack).copy().withStyle(style -> style.withColor(0xF2BE04));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player entity, InteractionHand hand) {
        ItemStack itemStack = entity.getItemInHand(hand);
        if (!world.isClientSide && entity instanceof Player) {
            Player player = (Player) entity;
            if (player.getCooldowns().isOnCooldown(this)) {
                return InteractionResultHolder.fail(itemStack);
            }

            // 右键爆炸技能 - 在目视方块位置爆炸
            Vec3 eyePos = player.getEyePosition();
            Vec3 lookVec = player.getLookAngle();
            double maxDistance = 50.0; // 最大射程

            // 进行射线追踪找到目标位置
            Vec3 endPos = eyePos.add(lookVec.scale(maxDistance));
            net.minecraft.world.phys.HitResult hitResult = world.clip(new net.minecraft.world.level.ClipContext(
                eyePos, endPos,
                net.minecraft.world.level.ClipContext.Block.OUTLINE,
                net.minecraft.world.level.ClipContext.Fluid.NONE,
                player
            ));

            Vec3 explosionPos = hitResult.getLocation();

            // 创建爆炸但不破坏方块
            world.explode(player, explosionPos.x, explosionPos.y, explosionPos.z,
                quality.getExplosionRadius(), false, Level.ExplosionInteraction.NONE);

            // 播放语音
            Utils.playRandomSound(SOUNDS, world, player);

            // 添加冷却时间（10秒）- 所有品质的火铳共享冷却
            // 直接设置冷却，确保在任何环境下都是10秒
            for (FireGunItem item : ITEMS) {
                player.getCooldowns().addCooldown(item, 20 * 10);
            }
        }
        return InteractionResultHolder.sidedSuccess(itemStack, world.isClientSide);
    }

    @Override
    public boolean onEntitySwing(ItemStack itemstack, LivingEntity entity) {
        // 左键射击子弹
        if (!entity.level().isClientSide && entity instanceof Player player) {
            PistolBulletEntity projectile = PistolBulletEntity.shoot(entity.level(), entity, entity.level().getRandom(), 2.6f, this.quality.getDamage(), 1);
            projectile.pickup = Pickup.CREATIVE_ONLY;

            if (entity.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(WarriorsofpastepochModParticleTypes.MUSKETSMOKE.get(), entity.getX(), entity.getY() + 1.5, entity.getZ(), 9, 0.3, 0.3, 0.3, 0.1);
            }
        }
        return super.onEntitySwing(itemstack, entity);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);
        pTooltipComponents.add(Component.literal("按左键射出子弹，右键可发射爆裂弹。").withStyle(style -> style.withColor(0x888888)));
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new GeoItemExtensions<FireGunItem>("fire_gun"));
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
        COMMON(0.6f, 5.0f, 3.0f),
        RARE(1.0f, 7.0f, 5.0f),
        MYTHIC(1.5f, 10.0f, 7.0f);

        private final float damage;
        private final float explosionDamage;
        private final float explosionRadius;

        Quality(float damage, float explosionDamage, float explosionRadius) {
            this.damage = damage;
            this.explosionDamage = explosionDamage;
            this.explosionRadius = explosionRadius;
        }

        public float getDamage() {
            return damage;
        }

        public float getExplosionDamage() {
            return explosionDamage;
        }

        public float getExplosionRadius() {
            return explosionRadius;
        }
    }
}
