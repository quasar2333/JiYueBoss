package com.imoonday.ji_yue_boss.entity;

import com.imoonday.ji_yue_boss.init.ModEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.UUID;

/**
 * Poltergeist变身实体 - 玩家变身时显示的geo模型
 */
public class PoltergeistTransformation extends PathfinderMob implements GeoEntity, OwnableEntity {

    protected static final RawAnimation IDLE_ANIMATION = RawAnimation.begin().thenLoop("idle");

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private UUID ownerUUID;
    private Player owner;
    private int lifeTime;

    public PoltergeistTransformation(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
        this.setNoGravity(true);
        this.setInvulnerable(true);
        this.noPhysics = true;
        this.lifeTime = 100; // 5秒 = 100 ticks
    }

    public PoltergeistTransformation(Level level, Player owner) {
        this(ModEntities.POLTERGEIST_TRANSFORMATION.get(), level);
        this.setOwner(owner);
    }

    public void setOwner(Player player) {
        this.ownerUUID = player.getUUID();
        this.owner = player;
    }

    @Override
    public void tick() {
        super.tick();
        
        if (!this.level().isClientSide) {
            // 同步位置到玩家
            if (owner != null && owner.isAlive()) {
                this.setPos(owner.getX(), owner.getY(), owner.getZ());
                this.setYRot(owner.getYRot());
                this.setYHeadRot(owner.getYHeadRot());
            }

            // 倒计时
            lifeTime--;
            if (lifeTime <= 0 || owner == null || !owner.isAlive()) {
                this.discard();
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean canBeLeashed(Player player) {
        return false;
    }

    @Override
    public boolean canBeSeenByAnyone() {
        return true;
    }

    @Override
    public boolean canBeHitByProjectile() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(Entity entity) {
    }

    @Override
    public boolean attackable() {
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        if (ownerUUID != null) {
            compound.putUUID("Owner", ownerUUID);
        }
        compound.putInt("LifeTime", lifeTime);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.hasUUID("Owner")) {
            ownerUUID = compound.getUUID("Owner");
        }
        if (compound.contains("LifeTime")) {
            lifeTime = compound.getInt("LifeTime");
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "idle", 5, this::getPlayState));
    }

    protected <E extends PoltergeistTransformation> PlayState getPlayState(final AnimationState<E> event) {
        return event.setAndContinue(IDLE_ANIMATION);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public @Nullable UUID getOwnerUUID() {
        return ownerUUID;
    }

    @Override
    public @Nullable LivingEntity getOwner() {
        return owner;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 1.0)
                .add(Attributes.MOVEMENT_SPEED, 0.0);
    }
}

